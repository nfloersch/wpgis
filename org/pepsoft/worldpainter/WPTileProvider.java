/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pepsoft.util.swing.TileListener;
import org.pepsoft.worldpainter.layers.Layer;
import static org.pepsoft.worldpainter.Constants.*;

/**
 *
 * @author pepijn
 */
public class WPTileProvider implements org.pepsoft.util.swing.TileProvider, Dimension.Listener, Tile.Listener {
    public WPTileProvider(Dimension dimension, ColourScheme colourScheme, BiomeScheme biomeScheme, Collection<Layer> hiddenLayers, boolean contourLines, TileRenderer.LightOrigin lightOrigin, boolean active) {
        tileProvider = dimension;
        this.colourScheme = colourScheme;
        this.biomeScheme = biomeScheme;
        this.hiddenLayers = hiddenLayers;
        this.contourLines = contourLines;
        this.lightOrigin = lightOrigin;
        this.active = active;
    }

    public WPTileProvider(TileProvider tileProvider, ColourScheme colourScheme, BiomeScheme biomeScheme, Collection<Layer> hiddenLayers, boolean contourLines, TileRenderer.LightOrigin lightOrigin) {
        this.tileProvider = tileProvider;
        this.colourScheme = colourScheme;
        this.biomeScheme = biomeScheme;
        this.hiddenLayers = hiddenLayers;
        this.contourLines = contourLines;
        this.lightOrigin = lightOrigin;
        active = false;
    }
    
    @Override
    public int getTileSize() {
        return TILE_SIZE;
    }

    @Override
    public BufferedImage getTile(int x, int y) {
        try {
            if (zoom == 1) {
                Tile tile = tileProvider.getTile(x, y);
                if (tile != null) {
                    TileRenderer tileRenderer;
                    synchronized (tileRendererRefLock) {
                        tileRenderer = tileRendererRef.get();
                        if (tileRenderer == null) {
                            tileRenderer = createRenderer();
                            tileRendererRef.set(tileRenderer);
                        }
                    }
                    tileRenderer.setTile(tile);
                    BufferedImage tileImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(TILE_SIZE, TILE_SIZE);
                    tileRenderer.renderTile(tileImage, 0, 0);
                    return tileImage;
                } else {
                    return null;
                }
            } else {
                Tile[][] tiles = new Tile[zoom][zoom];
                boolean tileFound = false, allTilesPresent = true;
                for (int dx = 0; dx < zoom; dx++) {
                    for (int dy = 0; dy < zoom; dy++) {
                        Tile tile = tileProvider.getTile(x * zoom + dx, y * zoom + dy);
                        if (tile != null) {
                            tileFound = true;
                            tiles[dx][dy] = tile;
                        } else {
                            allTilesPresent = false;
                        }
                    }
                }
                if (tileFound) {
                    TileRenderer tileRenderer;
                    synchronized (tileRendererRefLock) {
                        tileRenderer = tileRendererRef.get();
                        if (tileRenderer == null) {
                            tileRenderer = createRenderer();
                            tileRendererRef.set(tileRenderer);
                        }
                    }
                    BufferedImage tileImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(TILE_SIZE, TILE_SIZE);
                    if (! allTilesPresent) {
                        Graphics2D g2 = tileImage.createGraphics();
                        try {
                            g2.setColor(new Color(VOID_COLOUR));
                            g2.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
                        } finally {
                            g2.dispose();
                        }
                    }
                    int subSize = TILE_SIZE / zoom;
                    for (int dx = 0; dx < zoom; dx++) {
                        for (int dy = 0; dy < zoom; dy++) {
                            if (tiles[dx][dy] != null) {
                                tileRenderer.setTile(tiles[dx][dy]);
                                tileRenderer.renderTile(tileImage, dx * subSize, dy * subSize);
                            }
                        }
                    }
                    return tileImage;
                } else {
                    return null;
                }
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception while generating image for tile at " + x + ", " + y, e);
            return null;
        }
    }

    @Override
    public void addTileListener(TileListener tileListener) {
        if (active && listeners.isEmpty()) {
            ((Dimension) tileProvider).addDimensionListener(this);
            for (Tile tile: ((Dimension) tileProvider).getTiles()) {
                tile.addListener(this);
            }
        }
        if (! listeners.contains(tileListener)) {
            listeners.add(tileListener);
        }
    }

    @Override
    public void removeTileListener(TileListener tileListener) {
        listeners.remove(tileListener);
        if (active && listeners.isEmpty()) {
            for (Tile tile: ((Dimension) tileProvider).getTiles()) {
                tile.removeListener(this);
            }
            ((Dimension) tileProvider).removeDimensionListener(this);
        }
    }

    @Override
    public int getZoom() {
        return zoom;
    }

    @Override
    public void setZoom(int zoom) {
        if (zoom != this.zoom) {
            this.zoom = zoom;
            synchronized (tileRendererRefLock) {
                tileRendererRef = new ThreadLocal<TileRenderer>();
            }
        }
    }
    
    // Dimension.Listener

    @Override
    public void tileAdded(Dimension dimension, Tile tile) {
        tile.addListener(this);
    }

    @Override
    public void tileRemoved(Dimension dimension, Tile tile) {
        tile.removeListener(this);
    }

    // Tile.Listener
    
    @Override
    public void heightMapChanged(Tile tile) {
        fireTileChanged(tile);
    }

    @Override
    public void terrainChanged(Tile tile) {
        fireTileChanged(tile);
    }

    @Override
    public void waterLevelChanged(Tile tile) {
        fireTileChanged(tile);
    }

    @Override
    public void layerDataChanged(Tile tile, Set<Layer> changedLayers) {
        fireTileChanged(tile);
    }

    @Override
    public void allBitLayerDataChanged(Tile tile) {
        fireTileChanged(tile);
    }

    @Override
    public void allNonBitlayerDataChanged(Tile tile) {
        fireTileChanged(tile);
    }

    @Override
    public void seedsChanged(Tile tile) {
        fireTileChanged(tile);
    }
    
    private void fireTileChanged(Tile tile) {
        Point coords = getTileCoordinates(tile);
        for (TileListener listener: listeners) {
            listener.tileChanged(this, coords.x, coords.y);
        }
    }
    
    private Point getTileCoordinates(Tile tile) {
        final int tileX = tile.getX(), tileY = tile.getY();
        return new Point((tileX < 0) ? ((tileX + 1) / zoom) - 1 : tileX / zoom,
                (tileY < 0) ? ((tileY + 1) / zoom) - 1 : tileY / zoom);
    }
    
    private TileRenderer createRenderer() {
        TileRenderer tileRenderer = new TileRenderer(tileProvider, colourScheme, biomeScheme);
        tileRenderer.addHiddenLayers(hiddenLayers);
        tileRenderer.setZoom(zoom);
        tileRenderer.setContourLines(contourLines);
        tileRenderer.setLightOrigin(lightOrigin);
        return tileRenderer;
    }
 
    private final TileProvider tileProvider;
    private final ColourScheme colourScheme;
    private final BiomeScheme biomeScheme;
    private final Collection<Layer> hiddenLayers;
    private final boolean contourLines;
    private final TileRenderer.LightOrigin lightOrigin;
    private final Object tileRendererRefLock = new Object();
    private ThreadLocal<TileRenderer> tileRendererRef = new ThreadLocal<TileRenderer>();
    private int zoom = 1;
    private final boolean active;
    private final List<TileListener> listeners = new ArrayList<TileListener>();
    
    private static final int[] TILE_COORDS = new int[2];
    
    private static final Logger logger = Logger.getLogger(WPTileProvider.class.getName());
}