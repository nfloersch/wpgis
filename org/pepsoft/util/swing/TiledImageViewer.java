/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author pepijn
 */
public class TiledImageViewer extends JComponent implements TileListener, MouseListener, MouseMotionListener, ComponentListener {
    public TiledImageViewer() {
        this(true);
    }
    
    public TiledImageViewer(boolean leftClickDrags) {
        this.leftClickDrags = leftClickDrags;
        addMouseListener(this);
        addMouseMotionListener(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addComponentListener(this);
    }
    
    public TileProvider getTileProvider() {
        return tileProvider;
    }

    public void setTileProvider(TileProvider tileProvider) {
        if (this.tileProvider != null) {
            synchronized (tileCache) {
                this.tileProvider.removeTileListener(this);
                tileCache = null;
                dirtyTileCache = null;
//                visibleTiles = null;
                tileRenderers.shutdownNow();
                tileRenderers = null;
            }
        }
        this.tileProvider = tileProvider;
        if (tileProvider != null) {
            tileProvider.setZoom(zoom);
            tileProvider.addTileListener(this);
            tileCache = new HashMap<Point, Reference<BufferedImage>>();
            dirtyTileCache = new HashMap<Point, Reference<BufferedImage>>();
//            visibleTiles = new HashMap<Point, BufferedImage>();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Starting " + Runtime.getRuntime().availableProcessors() + " tile rendering threads");
            }
            tileRenderers = Executors.newFixedThreadPool(threads);
        }
        repaint();
    }

    public int getViewX() {
        return viewX;
    }

    public int getViewY() {
        return viewY;
    }
    
    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if (zoom != this.zoom) {
            int previousZoom = this.zoom;
            this.zoom = zoom;
            int middleX = getWidth() / 2;
            int middleY = getHeight() / 2;
            int worldX = (getViewX() + middleX) * -previousZoom;
            int worldY = (getViewY() + middleY) * previousZoom;
            viewX = (worldX / -zoom) - middleX;
            viewY = (worldY / zoom) - middleY;
            tileProvider.setZoom(zoom);
            tileRenderers.shutdownNow();
            tileRenderers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            synchronized (tileCache) {
                dirtyTileCache = tileCache;
                tileCache = new HashMap<Point, Reference<BufferedImage>>();
//                visibleTiles = new HashMap<Point, BufferedImage>();
            }
            if (previousZoom > zoom) {
                int factor = previousZoom / zoom;
                if (paintMarker) {
                    markerX *= factor;
                    markerY *= factor;
                }
            } else {
                int factor = zoom / previousZoom;
                if (paintMarker) {
                    markerX /= factor;
                    markerY /= factor;
                }
            }
            repaint();
        }
    }
    
    public Point getMarkerCoords() {
        return paintMarker ? new Point(markerX * zoom, markerY * zoom) : null;
    }
    
    public void setMarkerCoords(Point markerCoords) {
        if (markerCoords != null) {
            markerX = markerCoords.x / zoom;
            markerY = markerCoords.y / zoom;
            paintMarker = true;
        } else {
            paintMarker = false;
        }
        repaint();
    }

    public void moveToMarker() {
        if (paintMarker) {
            viewX = -getWidth() / 2 + markerX;
            viewY = -getHeight() / 2 + markerY;
            repaint();
        }
    }

    public void moveToOrigin() {
        viewX = -getWidth() / 2;
        viewY = -getHeight() / 2;
        repaint();
    }
    
    public void refresh() {
        tileRenderers.shutdownNow();
        tileRenderers = Executors.newFixedThreadPool(threads);
        dirtyTileCache = tileCache;
        tileCache = new HashMap<Point, Reference<BufferedImage>>();
//        visibleTiles = new HashMap<Point, BufferedImage>();
        repaint();
    }
    
    public void reset() {
        viewX = -getWidth() / 2;
        viewY = -getHeight() / 2;
        tileProvider.setZoom(4);
        refresh();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (tileProvider == null) {
            super.paintComponent(g);
            return;
        }
        Rectangle clipBounds = g.getClipBounds();
//        System.out.println("Clip bounds: " + clipBounds.x + ", " + clipBounds.y + " - " + clipBounds.width + "x" + clipBounds.height);
        int tileSize = tileProvider.getTileSize();
        int leftTile = (clipBounds.x + viewX) / tileSize - 1;
        int rightTile = (clipBounds.x + viewX + clipBounds.width) / tileSize;
        int topTile = (clipBounds.y + viewY) / tileSize - 1;
        int bottomTile = (clipBounds.y + viewY + clipBounds.height) / tileSize;
//        System.out.println("Repainting " + leftTile + ", " + topTile + " - " + rightTile + ", " + bottomTile);
//        int dx = viewX % tileSize;
//        int dy = viewY % tileSize;
        
        synchronized (tileCache) {
            int middleTileX = (leftTile + rightTile) / 2;
            int middleTileY = (topTile + bottomTile) / 2;
            int radius = Math.max(
                Math.max(middleTileX - leftTile, rightTile - middleTileX),
                Math.max(middleTileY - topTile, bottomTile - middleTileY));
            paintTile(g, leftTile, rightTile, topTile, bottomTile, middleTileX, middleTileY);
            for (int r = 1; r <= radius; r++) {
                for (int i = 0; i <= (r * 2); i++) {
                    paintTile(g, leftTile, rightTile, topTile, bottomTile, middleTileX + i - r, middleTileY - r);
                    paintTile(g, leftTile, rightTile, topTile, bottomTile, middleTileX + r, middleTileY + i - r);
                    paintTile(g, leftTile, rightTile, topTile, bottomTile, middleTileX + r - i, middleTileY + r);
                    paintTile(g, leftTile, rightTile, topTile, bottomTile, middleTileX - r, middleTileY - i + r);
                }
            }

            // Enqueue a rim of tiles around the visible area for rendering too
//            for (int x = leftTile - 3; x <= rightTile + 3; x++) {
//                if ((x < leftTile) || (x > rightTile)) {
//                    for (int y = topTile - 3; y <= (bottomTile + 3); y++) {
//                        getTile(x, y);
//                    }
//                } else {
//                    for (int y = topTile - 3; y < topTile; y++) {
//                        getTile(x, y);
//                    }
//                    for (int y = bottomTile + 1; y <= (bottomTile + 3); y++) {
//                        getTile(x, y);
//                    }
//                }
//            }
        }
        
        // Trim visible tiles map (which exists to prevent the visible tiles
        // from being garbage collected
        
        if (paintMarker) {
            g.setColor(Color.RED);
            g.drawLine(markerX - viewX - 5, markerY - viewY,     markerX - viewX + 5, markerY - viewY);
            g.drawLine(markerX - viewX,     markerY - viewY - 5, markerX - viewX,     markerY - viewY + 5);
        }
        
        int middleX = getWidth() / 2;
        int middleY = getHeight() / 2;
        g.setColor(Color.BLACK);
        g.drawLine(middleX - 4, middleY + 1, middleX + 6, middleY + 1);
        g.drawLine(middleX + 1, middleY - 4, middleX + 1, middleY + 6);
        g.setColor(Color.WHITE);
        g.drawLine(middleX - 5, middleY, middleX + 5, middleY);
        g.drawLine(middleX, middleY - 5, middleX, middleY + 5);
    }
    
    private void paintTile(Graphics g, int leftTile, int rightTile, int topTile, int bottomTile, int x, int y) {
        if ((x < leftTile) || (x > rightTile) || (y < topTile) || (y > bottomTile)) {
            return;
        }
        int tileSize = tileProvider.getTileSize();
        BufferedImage tile = getTile(x, y);
        if (tile != null) {
            g.drawImage(tile, x * tileSize - viewX, y * tileSize - viewY, this);
        }
    }
    
    private BufferedImage getTile(int x, int y) {
        final Point coords = new Point(x, y);
        Reference<BufferedImage> ref = tileCache.get(coords);
        if (ref == RENDERING) {
//            System.out.print("Tile @ " + x + "," + y + " is rendering; ");
            Reference<BufferedImage> dirtyRef = dirtyTileCache.get(coords);
            BufferedImage dirtyTile = (dirtyRef != null) ? dirtyRef.get() : null;
//            System.out.println((dirtyTile != null) ? "painting dirty tile" : "no dirty tile available");
            return (dirtyTile != NO_TILE) ? dirtyTile : null;
        }
        BufferedImage tile = (ref != null) ? ref.get() : null;
        if (tile == null) {
//            if (ref == null) {
//                System.out.print("Tile @ " + x + "," + y + " is not yet rendered; ");
//            } else {
//                System.out.print("Tile @ " + x + "," + y + " was garbage collected; ");
//            }
            Reference<BufferedImage> dirtyRef = dirtyTileCache.get(coords);
            if (dirtyRef != null) {
                tile = dirtyRef.get();
            }
//            System.out.println((tile != null) ? "painting dirty tile" : "no dirty tile available");
            tileCache.put(coords, RENDERING);
            final Map<Point, Reference<BufferedImage>> targetCache = tileCache, targetDirtyCache = dirtyTileCache;
//            final Map<Point, BufferedImage> targetVisibleTiles = visibleTiles;
            tileRenderers.submit(new Runnable() {
                @Override
                public void run() {
//                    try {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Rendering tile " + coords);
                        }
                        BufferedImage tile = tileProvider.getTile(coords.x, coords.y);
                        if (tile == null) {
                            tile = NO_TILE;
                        }
                        synchronized (targetCache) {
    //                        System.out.print("Rendering of tile @ " + coords.x + "," + coords.y + " finished; placing in tile cache; ");
                            targetCache.put(coords, new SoftReference<BufferedImage>(tile));
    //                        targetVisibleTiles.put(coords, tile);
                            if (targetDirtyCache.containsKey(coords)) {
    //                            System.out.println("removing dirty tile");
                                targetDirtyCache.remove(coords);
    //                        } else {
    //                            System.out.println("no corresponding tile in dirty tile cache");
                            }
                        }
                        repaint();
//                    } catch (RuntimeException e) {
//                        e.printStackTrace();
//                        throw e;
//                    }
                }
            });
        }
        return (tile != NO_TILE) ? tile : null;
    }

    // ComponentListener
    
    @Override
    public void componentResized(ComponentEvent e) {
        int dw = getWidth() - previousSize.width;
        int dh = getHeight() - previousSize.height;
        viewX = viewX - dw / 2;
        viewY = viewY - dh / 2;
        previousSize = getSize();
        repaint();
    }
    
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}
    
    // TileListener

    @Override
    public void tileChanged(TileProvider source, final int x, final int y) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Point coords = new Point(x, y);
                synchronized (tileCache) {
                    Reference<BufferedImage> tileRef = tileCache.remove(coords);
//                    visibleTiles.remove(coords);
                    BufferedImage tile = (tileRef != null) ? tileRef.get() : null;
//                    System.out.print("Tile @ " + x + "," + y + " changed; scheduling for rerendering; ");
//                    System.out.println((tile != null) ? "moving tile to dirty tile cache" : "no rendered tile available");
                    if (tile != null) {
                        dirtyTileCache.put(coords, tileRef);
                    }
                }
                repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    // MouseListener
    
    @Override
    public void mousePressed(MouseEvent e) {
        if ((! leftClickDrags) && (e.getButton() == MouseEvent.BUTTON1)) {
            return;
        }
        previousX = e.getX();
        previousY = e.getY();
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        dragging = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if ((! leftClickDrags) && (e.getButton() == MouseEvent.BUTTON1)) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dragging = false;
    }
    
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // MouseMotionListener
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (! dragging) {
            return;
        }
        int dx = e.getX() - previousX;
        int dy = e.getY() - previousY;
        viewX -= dx;
        viewY -= dy;
        previousX = e.getX();
        previousY = e.getY();
        repaint();
    }

    @Override public void mouseMoved(MouseEvent e) {}
 
    private final boolean leftClickDrags;
    private final int threads = Runtime.getRuntime().availableProcessors();
    private TileProvider tileProvider;
//    private Map<Point, BufferedImage> visibleTiles;
    private Map<Point, Reference<BufferedImage>> tileCache, dirtyTileCache;
    private int viewX, viewY, previousX, previousY, markerX, markerY, zoom = 1;
    private ExecutorService tileRenderers;
    private Dimension previousSize = new Dimension(0, 0);
    private boolean dragging, paintMarker;
    
    private static final Reference<BufferedImage> RENDERING = new SoftReference<BufferedImage>(null);
    private static final BufferedImage NO_TILE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(TiledImageViewer.class.getName());
}