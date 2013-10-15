/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.pepsoft.util.swing.TiledImageViewer;
import static org.pepsoft.worldpainter.Constants.*;

/**
 *
 * @author pepijn
 */
public class WPTileSelectionViewer extends TiledImageViewer {
    public WPTileSelectionViewer() {
        // Do nothing
    }

    public WPTileSelectionViewer(boolean leftClickDrags) {
        super(leftClickDrags);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        super.paintComponent(g2);
        
        if (! selectedTiles.isEmpty()) {
            g2.setColor(Color.YELLOW);
            Composite previousComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            try {
                Rectangle clipBounds = g2.getClipBounds();
                for (Point selectedTile: selectedTiles) {
                    Rectangle tileBounds = getTileCoordinates(selectedTile);
                    if (tileBounds.intersects(clipBounds)) {
                        g2.fillRect(tileBounds.x, tileBounds.y, tileBounds.width, tileBounds.height);
                    }
                }
            } finally {
                g2.setComposite(previousComposite);
            }
        }
        
        if ((selectedRectangleCorner1 != null) && (selectedRectangleCorner2 != null)) {
            g2.setColor(Color.CYAN);
            Rectangle rectangleBounds = getTileRectangleCoordinates(selectedRectangleCorner1, selectedRectangleCorner2);
            g2.drawRect(rectangleBounds.x, rectangleBounds.y, rectangleBounds.width - 1, rectangleBounds.height - 1);
        }
        
        if (highlightedTileLocation != null) {
            Rectangle rect = getTileCoordinates(highlightedTileLocation);
            g2.setColor(Color.RED);
            g2.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
        }
        
        int zoom = getZoom();
        g2.drawImage(SCALE_BAR, 10, getHeight() - 10 - SCALE_BAR.getHeight(), this);
        g2.setColor(Color.BLACK);
        String str = zoom + "00 m";
        g2.drawString(str, 15 + SCALE_BAR.getWidth() + 1, getHeight() - 9);
        g2.setColor(Color.WHITE);
        g2.drawString(str, 15 + SCALE_BAR.getWidth(), getHeight() - 10);

        int middleX = getWidth() / 2;
        int middleY = getHeight() / 2;
        g2.setColor(Color.BLACK);
        str = ((getViewX() + middleX) * zoom) + ", " + ((getViewY() + middleY) * zoom);
        g2.drawString(str, 11, 21);
        g2.setColor(Color.WHITE);
        g2.drawString(str, 10, 20);
    }

    public Point getHighlightedTileLocation() {
        return highlightedTileLocation;
    }

    public void setHighlightedTileLocation(Point highlightedTileLocation) {
        if (this.highlightedTileLocation != null) {
            repaint(getTileCoordinates(this.highlightedTileLocation));
        }
        this.highlightedTileLocation = highlightedTileLocation;
        if (highlightedTileLocation != null) {
            repaint(getTileCoordinates(highlightedTileLocation));
        }
    }
    
    public void addSelectedTile(Point tileLocation) {
        if (! selectedTiles.contains(tileLocation)) {
            selectedTiles.add(tileLocation);
            repaint(getTileCoordinates(tileLocation));
        }
    }

    public boolean isSelectedTile(Point tileLocation) {
        return selectedTiles.contains(tileLocation);
    }
    
    public Set<Point> getSelectedTiles() {
        return Collections.unmodifiableSet(selectedTiles);
    }

    public void setSelectedTiles(Set<Point> selectedTiles) {
        this.selectedTiles.clear();
        this.selectedTiles.addAll(selectedTiles);
        repaint();
    }
    
    public void removeSelectedTile(Point tileLocation) {
        if (selectedTiles.contains(tileLocation)) {
            selectedTiles.remove(tileLocation);
            repaint(getTileCoordinates(tileLocation));
        }
    }
    
    public void clearSelectedTiles() {
        if (! selectedTiles.isEmpty()) {
            selectedTiles.clear();
            repaint();
        }
    }

    public Point getSelectedRectangleCorner1() {
        return selectedRectangleCorner1;
    }

    public void setSelectedRectangleCorner1(Point selectedRectangleCorner1) {
        if ((this.selectedRectangleCorner1 != null) && (this.selectedRectangleCorner2 != null)) {
            repaint(getTileRectangleCoordinates(this.selectedRectangleCorner1, this.selectedRectangleCorner2));
        }
        this.selectedRectangleCorner1 = selectedRectangleCorner1;
        if ((selectedRectangleCorner1 != null) && (this.selectedRectangleCorner2 != null)) {
            repaint(getTileRectangleCoordinates(selectedRectangleCorner1, this.selectedRectangleCorner2));
        }
    }

    public Point getSelectedRectangleCorner2() {
        return selectedRectangleCorner2;
    }

    public void setSelectedRectangleCorner2(Point selectedRectangleCorner2) {
        if ((this.selectedRectangleCorner1 != null) && (this.selectedRectangleCorner2 != null)) {
            repaint(getTileRectangleCoordinates(this.selectedRectangleCorner1, this.selectedRectangleCorner2));
        }
        this.selectedRectangleCorner2 = selectedRectangleCorner2;
        if ((this.selectedRectangleCorner1 != null) && (selectedRectangleCorner2 != null)) {
            repaint(getTileRectangleCoordinates(this.selectedRectangleCorner1, selectedRectangleCorner2));
        }
    }

    public Point getCurrentLocation() {
        final int zoom = getZoom();
        return new Point((getViewX() + getWidth() / 2) * zoom, (getViewY() + getHeight() / 2) * zoom);
    }
    
    private Rectangle getTileCoordinates(Point tileLocation) {
        int zoom = getZoom();
        return new Rectangle(tileLocation.x * TILE_SIZE / zoom - getViewX(), tileLocation.y * TILE_SIZE / zoom - getViewY(), TILE_SIZE / zoom, TILE_SIZE / zoom);
    }
    
    private Rectangle getTileRectangleCoordinates(Point tileCorner1, Point tileCorner2) {
        int zoom = getZoom();
        Rectangle corner1Coords = new Rectangle(tileCorner1.x * TILE_SIZE / zoom - getViewX(), tileCorner1.y * TILE_SIZE / zoom - getViewY(), TILE_SIZE / zoom, TILE_SIZE / zoom);
        Rectangle corner2Coords = new Rectangle(tileCorner2.x * TILE_SIZE / zoom - getViewX(), tileCorner2.y * TILE_SIZE / zoom - getViewY(), TILE_SIZE / zoom, TILE_SIZE / zoom);
        int left = Math.min(corner1Coords.x, corner2Coords.x);
        int right = Math.max(corner1Coords.x + corner1Coords.width - 1, corner2Coords.x + corner2Coords.width - 1);
        int top = Math.min(corner1Coords.y, corner2Coords.y);
        int bottom = Math.max(corner1Coords.y + corner1Coords.height - 1, corner2Coords.y + corner2Coords.height - 1);
        return new Rectangle(left, top, right - left + 1, bottom - top + 1);
    }
    
    private Point highlightedTileLocation;
    private Point selectedRectangleCorner1, selectedRectangleCorner2;
    private final Set<Point> selectedTiles = new HashSet<Point>();
    
    private static final BufferedImage SCALE_BAR;
    
    static {
        try {
            SCALE_BAR = ImageIO.read(WPTileSelectionViewer.class.getResourceAsStream("/org/pepsoft/worldpainter/scale_bar.png"));
        } catch (IOException e) {
            throw new RuntimeException("I/O error loading scale bar from classpath", e);
        }
    }

    private static final long serialVersionUID = 1L;
}