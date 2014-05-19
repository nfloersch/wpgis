/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import org.pepsoft.util.MathUtils;
import org.pepsoft.util.MemoryUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.TileRenderer.LightOrigin;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.operations.BrushShape;

import static org.pepsoft.worldpainter.Constants.DIM_NORMAL;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE_BITS;
import static org.pepsoft.worldpainter.Constants.VOID_COLOUR;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;

/**
 *
 * @author pepijn
 */
public class WorldPainter extends WorldPainterView implements Dimension.Listener, Tile.Listener, MouseMotionListener, Scrollable, PropertyChangeListener {
    public WorldPainter(ColourScheme colourScheme, BiomeScheme biomeScheme, CustomBiomeManager customBiomeManager) {
        this.colourScheme = colourScheme;
        this.biomeScheme = biomeScheme;
        this.customBiomeManager = customBiomeManager;
        setOpaque(true);
        addMouseMotionListener(this);
        labelFont = new Font(Font.DIALOG, Font.PLAIN, 8);
        FontMetrics fontMetrics = getFontMetrics(labelFont);
        labelAscent = fontMetrics.getAscent();
        labelLineHeight = fontMetrics.getHeight();
        enableInputMethods(false);
    }

    public WorldPainter(Dimension dimension, ColourScheme colourScheme, BiomeScheme biomeScheme, CustomBiomeManager customBiomeManager) {
        this(colourScheme, biomeScheme, customBiomeManager);
        setDimension(dimension);
    }

    @Override
    public final Dimension getDimension() {
        return dimension;
    }

    @Override
    public final void setDimension(Dimension dimension) {
        setDimension(dimension, null);
    }
    
    final void setDimension(Dimension dimension, BufferedImage image) {
        Dimension oldDimension = this.dimension;
        if (oldDimension != null) {
            oldDimension.removeDimensionListener(this);
            oldDimension.getWorld().removePropertyChangeListener("spawnPoint", this);
            unregisterTileListeners();
        }
        this.dimension = dimension;
        synchronized (dirtyTiles) {
            dirtyTiles.clear();
            dirtyTileNeighbours.clear();
        }
        if (dimension != null) {
            tileRenderer = new TileRenderer(dimension, colourScheme, biomeScheme, customBiomeManager);
            tileRenderer.setLightOrigin(lightOrigin);
            drawContours = dimension.isContoursEnabled();
            tileRenderer.setContourLines(drawContours);
            contourSeparation = dimension.getContourSeparation();
            tileRenderer.setContourSeparation(contourSeparation);
            worldRenderer = new DimensionRenderer(dimension, tileRenderer);
            for (Layer hiddenLayer: hiddenLayers) {
                worldRenderer.addHiddenLayer(hiddenLayer);
            }
            createImage(image);
            dimension.addDimensionListener(this);
            if (dimension.getDim() == 0) {
                dimension.getWorld().addPropertyChangeListener("spawnPoint", this);
            }
            registerTileListeners();

            setGridSize(dimension.getGridSize());
            setDrawGrid(dimension.isGridEnabled());
            
            setOverlayScale(dimension.getOverlayScale());
            setOverlayTransparency(dimension.getOverlayTransparency());
            setOverlayOffsetX(dimension.getOverlayOffsetX());
            setOverlayOffsetY(dimension.getOverlayOffsetY());
            if ((dimension.getOverlay() != null) && dimension.getOverlay().isFile()) {
                try {
                    BufferedImage myOverlay = ImageIO.read(dimension.getOverlay());
                    setOverlay(myOverlay);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Could not load overlay image due to I/O error", e);
                }
            } else {
                setOverlay(null);
            }
            setDrawOverlay(dimension.isOverlayEnabled());
        } else {
            worldImage = null;
            worldRenderer = null;
            tileRenderer = null;
            resize();
            setOverlay(null);
        }
        firePropertyChange("dimension", oldDimension, dimension);
        repaint();
    }

    public ColourScheme getColourScheme() {
        return colourScheme;
    }

    public void setColourScheme(ColourScheme colourScheme) {
        this.colourScheme = colourScheme;
        if (tileRenderer != null) {
            tileRenderer.setColourScheme(colourScheme);
            refreshTiles();
        }
    }

    public BiomeScheme getBiomeScheme() {
        return biomeScheme;
    }

    public void setBiomeScheme(BiomeScheme biomeScheme) {
        this.biomeScheme = biomeScheme;
        if (tileRenderer != null) {
            if (! hiddenLayers.contains(Biome.INSTANCE)) {
                refreshTiles();
            }
        }
    }

    /**
     * Converts image coordinates to world coordinates.
     *
     * @param x Horizontal image coordinate.
     * @param y Vertical image coordinate.
     * @return The corresponding point in world coordinates.
     */
    public Point imageToWorldCoordinates(int x, int y) {
        return new Point(x + imageX * TILE_SIZE, y + imageY * TILE_SIZE);
    }

    /**
     * Converts image coordinates to world coordinates.
     *
     * @param imageCoordinates The image coordinates to convert.
     * @return The corresponding point in world coordinates.
     */
    @Override
    public Point imageToWorldCoordinates(Point imageCoordinates) {
        return imageToWorldCoordinates(imageCoordinates.x, imageCoordinates.y);
    }

    /**
     * Converts world coordinates to image coordinates.
     *
     * @param x Horizontal world coordinate.
     * @param y Vertical world coordinate.
     * @return The corresponding point in image coordinates.
     */
    @Override
    public Point worldToImageCoordinates(int x, int y) {
        return new Point(x - imageX * TILE_SIZE, y - imageY * TILE_SIZE);
    }

    /**
     * Converts view coordinates (may be zoomed) to image coordinates
     * (unzoomed).
     *
     * @param viewCoordinates The view coordinates to convert.
     * @return The corresponding point in image coordinates.
     */
    @Override
    public Point viewToImageCoordinates(Point viewCoordinates) {
        if (zoom == 0) {
            return viewCoordinates;
        } else {
            return new Point((int) (viewCoordinates.x / scale), (int) (viewCoordinates.y / scale));
        }
    }

    /**
     * Converts view coordinates (may be zoomed) to image coordinates
     * (unzoomed).
     *
     * @param viewCoordinates The view coordinates to convert.
     * @return The corresponding point in image coordinates.
     */
    @Override
    public Point viewToImageCoordinates(int x, int y) {
        if (zoom == 0) {
            return new Point (x, y);
        } else {
            return new Point((int) (x / scale), (int) (y / scale));
        }
    }
    
    /**
     * Converts a rectangle in view coordinates (may be zoomed) to image
     * coordinates (unzoomed).
     *
     * @param viewRectangle The rectangle in view coordinates to convert.
     * @return The corresponding rectangle in image coordinates.
     */
    public Rectangle viewToImageCoordinates(Rectangle viewRectangle) {
        if (zoom == 0) {
            return viewRectangle;
        } else {
            return new Rectangle((int) (viewRectangle.x / scale), (int) (viewRectangle.y / scale), (int) (viewRectangle.width / scale), (int) (viewRectangle.height / scale));
        }
    }

    /**
     * Converts image coordinates (unzoomed) to view coordinates (may be
     * zoomed).
     *
     * @param imageCoordinates The image coordinates to convert.
     * @return The corresponding point in view coordinates.
     */
    public Point imageToViewCoordinates(Point imageCoordinates) {
        if (zoom == 0) {
            return imageCoordinates;
        } else {
            return new Point((int) (imageCoordinates.x * scale), (int) (imageCoordinates.y * scale));
        }
    }

    /**
     * Converts a rectangle in image coordinates (unzoomed) to view coordinates
     * (may be zoomed).
     *
     * @param x The X coordinate of the rectangle to convert.
     * @param y The Y coordinate of the rectangle to convert.
     * @param width The width of the rectangle to convert.
     * @param height The height of the rectangle to convert.
     * @return The corresponding rectangle in view coordinates.
     */
    public Rectangle imageToViewCoordinates(int x, int y, int width, int height) {
        if (zoom == 0) {
            return new Rectangle(x, y, width, height);
        } else {
            return new Rectangle((int) (x * scale), (int) (y * scale), (int) (width * scale), (int) (height * scale));
        }
    }

    /**
     * Converts a rectangle in image coordinates (unzoomed) to view coordinates
     * (may be zoomed).
     *
     * @param imageRectangle The rectangle in image coordinates to convert.
     * @return The corresponding rectangle in view coordinates.
     */
    public Rectangle imageToViewCoordinates(Rectangle imageRectangle) {
        if (zoom == 0) {
            return imageRectangle;
        } else {
            return new Rectangle((int) (imageRectangle.x * scale), (int) (imageRectangle.y * scale), (int) Math.ceil(imageRectangle.width * scale), (int) Math.ceil(imageRectangle.height * scale));
        }
    }

    public boolean isDrawRadius() {
        return drawRadius;
    }

    public void setDrawRadius(boolean drawRadius) {
        if (drawRadius != this.drawRadius) {
            this.drawRadius = drawRadius;
            firePropertyChange("drawRadius", ! drawRadius, drawRadius);
            int extraPixels = Math.max((int) (1 / scale), 1);
            int diameter = radius * 2 + 1 + 2 * extraPixels; // Extra pixels because when zoomed in, Swing colours outside the lines...
            repaint(imageToViewCoordinates(mouseX - radius - extraPixels, mouseY - radius - extraPixels, diameter, diameter));
        }
    }

    public boolean isDrawViewDistance() {
        return drawViewDistance;
    }

    public void setDrawViewDistance(boolean drawViewDistance) {
        if (drawViewDistance != this.drawViewDistance) {
            this.drawViewDistance = drawViewDistance;
            firePropertyChange("drawViewDistance", !drawViewDistance, drawViewDistance);
            int extraPixels = Math.max((int) (1 / scale), 1);
            int diameter = VIEW_DISTANCE_RADIUS * 2 + 1 + 2 * extraPixels; // Extra pixels because when zoomed in, Swing colours outside the lines...
            repaint(imageToViewCoordinates(mouseX - VIEW_DISTANCE_RADIUS - extraPixels, mouseY - VIEW_DISTANCE_RADIUS - extraPixels, diameter, diameter));
        }
    }

    public boolean isDrawWalkingDistance() {
        return drawWalkingDistance;
    }

    public void setDrawWalkingDistance(boolean drawWalkingDistance) {
        if (drawWalkingDistance != this.drawWalkingDistance) {
            this.drawWalkingDistance = drawWalkingDistance;
            firePropertyChange("drawWalkingDistance", !drawWalkingDistance, drawWalkingDistance);
            int extraPixels = Math.max((int) (1 / scale), 1);
            int diameter = DAY_NIGHT_WALK_DISTANCE_RADIUS * 2 + 1 + 2 * extraPixels; // Extra pixels because when zoomed in, Swing colours outside the lines...
            repaint(imageToViewCoordinates(mouseX - DAY_NIGHT_WALK_DISTANCE_RADIUS - extraPixels, mouseY - DAY_NIGHT_WALK_DISTANCE_RADIUS - extraPixels, diameter, diameter));
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        int oldRadius = this.radius;
        int oldEffectiveRadius = this.effectiveRadius;
        this.radius = radius;
        if ((brushShape == BrushShape.CIRCLE) || ((brushRotation % 90) == 0)) {
            effectiveRadius = radius;
        } else {
            double a = brushRotation / 180.0 * Math.PI;
            effectiveRadius = (int) Math.ceil(Math.abs(Math.sin(a)) * radius + Math.abs(Math.cos(a)) * radius);
        }
        firePropertyChange("radius", oldRadius, radius);
        if (drawRadius) {
            int largestRadius = Math.max(oldEffectiveRadius, effectiveRadius);
            int extraPixels = Math.max((int) (1 / scale), 1);
            int diameter = largestRadius * 2 + 1 + 2 * extraPixels; // Extra pixels because when zoomed in, Swing colours outside the lines...
            repaint(imageToViewCoordinates(mouseX - largestRadius - extraPixels, mouseY - largestRadius - extraPixels, diameter, diameter));
        }
    }

    public BrushShape getBrushShape() {
        return brushShape;
    }

    public void setBrushShape(BrushShape brushShape) {
        if (brushShape != this.brushShape) {
            BrushShape oldBrushShape = this.brushShape;
            int oldEffectiveRadius = effectiveRadius;
            this.brushShape = brushShape;
            if ((brushShape == BrushShape.CIRCLE) || ((brushRotation % 90) == 0)) {
                effectiveRadius = radius;
            } else {
                double a = brushRotation / 180.0 * Math.PI;
                effectiveRadius = (int) Math.ceil(Math.abs(Math.sin(a)) * radius + Math.abs(Math.cos(a)) * radius);
            }
            firePropertyChange("brushShape", oldBrushShape, brushShape);
            if (drawRadius) {
                int largestRadius = Math.max(oldEffectiveRadius, effectiveRadius);
                int extraPixels = Math.max((int) (1 / scale), 1);
                int diameter = largestRadius * 2 + 1 + 2 * extraPixels; // Extra pixels because when zoomed in, Swing colours outside the lines...
                repaint(imageToViewCoordinates(mouseX - largestRadius - extraPixels, mouseY - largestRadius - extraPixels, diameter, diameter));
            }
        }
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if (zoom != this.zoom) {
            int oldZoom = this.zoom;
            this.zoom = zoom;
            scale = (float) Math.pow(2.0, zoom);
            resize();
            firePropertyChange("zoom", oldZoom, zoom);
        }
    }

    public boolean isDrawGrid() {
        return drawGrid;
    }

    public void setDrawGrid(boolean drawGrid) {
        if (drawGrid != this.drawGrid) {
            this.drawGrid = drawGrid;
            repaint();
            firePropertyChange("drawGrid", ! drawGrid, drawGrid);
        }
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        if (gridSize != this.gridSize) {
            int oldGridSize = this.gridSize;
            this.gridSize = gridSize;
            if (drawGrid) {
                repaint();
            }
            firePropertyChange("gridSize", oldGridSize, gridSize);
        }
    }

    public BufferedImage getOverlay() {
        return overlay;
    }

    public void setOverlay(BufferedImage overlay) {
        BufferedImage oldOverlay = this.overlay;
        this.overlay = overlay;
//        if (overlay != null) {
//            scaleOverlay();
//        } else {
//            scaledOverlay = null;
//        }
        if (overlayTransparency < 1.0f) {
            repaint();
        }
        firePropertyChange("overlay", oldOverlay, overlay);
    }

    public int getOverlayOffsetX() {
        return overlayOffsetX;
    }

    public void setOverlayOffsetX(int overlayOffsetX) {
        if (overlayOffsetX != this.overlayOffsetX) {
            int oldOverlayOffsetX = this.overlayOffsetX;
            this.overlayOffsetX = overlayOffsetX;
            if ((overlay != null) && (overlayTransparency < 1.0f)) {
                repaint();
            }
            firePropertyChange("overlayOffsetX", oldOverlayOffsetX, overlayOffsetX);
        }
    }

    public int getOverlayOffsetY() {
        return overlayOffsetY;
    }

    public void setOverlayOffsetY(int overlayOffsetY) {
        if (overlayOffsetY != this.overlayOffsetY) {
            int oldOverlayOffsetY = this.overlayOffsetY;
            this.overlayOffsetY = overlayOffsetY;
            if ((overlay != null) && (overlayTransparency < 1.0f)) {
                repaint();
            }
            firePropertyChange("overlayOffsetY", oldOverlayOffsetY, overlayOffsetY);
        }
    }

    public float getOverlayScale() {
        return overlayScale;
    }

    public void setOverlayScale(float overlayScale) {
        if (overlayScale != this.overlayScale) {
            float oldOverlayScale = this.overlayScale;
            this.overlayScale = overlayScale;
            if (overlay != null) {
//                scaleOverlay();
                if (overlayTransparency < 1.0f) {
                    repaint();
                }
            }
            firePropertyChange("overlayScale", oldOverlayScale, overlayScale);
        }
    }

    public float getOverlayTransparency() {
        return overlayTransparency;
    }

    public void setOverlayTransparency(float overlayTransparency) {
        if (overlayTransparency != this.overlayTransparency) {
            float oldOverlayTransparency = this.overlayTransparency;
            this.overlayTransparency = overlayTransparency;
            if (overlay != null) {
                repaint();
            }
            firePropertyChange("overlayTransparency", oldOverlayTransparency, overlayTransparency);
        }
    }

    public boolean isDrawOverlay() {
        return drawOverlay;
    }

    public void setDrawOverlay(boolean drawOverlay) {
        if (drawOverlay != this.drawOverlay) {
            this.drawOverlay = drawOverlay;
            if (overlay != null) {
                repaint();
            }
            firePropertyChange("drawOverlay", ! drawOverlay, drawOverlay);
        }
    }

    public int getContourSeparation() {
        return contourSeparation;
    }

    public void setContourSeparation(int contourSeparation) {
        if (contourSeparation != this.contourSeparation) {
            int oldContourSeparation = this.contourSeparation;
            this.contourSeparation = contourSeparation;
            if (dimension != null) {
                tileRenderer.setContourSeparation(contourSeparation);
                refreshTiles();
            }
            firePropertyChange("contourSeparation", oldContourSeparation, contourSeparation);
        }
        this.contourSeparation = contourSeparation;
    }

    public boolean isDrawContours() {
        return drawContours;
    }

    public void setDrawContours(boolean drawContours) {
        if (drawContours != this.drawContours) {
            this.drawContours = drawContours;
            if (tileRenderer != null) {
                tileRenderer.setContourLines(drawContours);
                refreshTiles();
            }
            firePropertyChange("drawContours", ! drawContours, drawContours);
        }
    }

    public boolean isInhibitUpdates() {
        return inhibitUpdates;
    }

    public void setInhibitUpdates(boolean inhibitUpdates) {
        if (inhibitUpdates != this.inhibitUpdates) {
            this.inhibitUpdates = inhibitUpdates;
            if (inhibitUpdates) {
                synchronized (dirtyTiles) {
                    dirtyTiles.clear();
                    dirtyTileNeighbours.clear();
                }
            } else {
                refreshTiles();
            }
            firePropertyChange("inhibitUpdates", ! inhibitUpdates, inhibitUpdates);
        }
    }

    public void refreshBrush() {
        Point mousePos = getMousePosition();
//        System.out.println("refreshBrush(): mousePos: " + mousePos);
        if (mousePos != null) {
            mouseMoved(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, mousePos.x, mousePos.y, 0, false));
        }
    }

    public void addHiddenLayer(Layer hiddenLayer) {
        if (! hiddenLayers.contains(hiddenLayer)) {
            Set<Layer> oldHiddenLayers = new HashSet<Layer>(hiddenLayers);
            hiddenLayers.add(hiddenLayer);
            if (worldRenderer != null) {
                worldRenderer.addHiddenLayer(hiddenLayer);
                refreshTiles();
            }
            firePropertyChange("hiddenLayers", oldHiddenLayers, hiddenLayers);
        }
    }

    public void removeHiddenLayer(Layer hiddenLayer) {
        if (hiddenLayers.contains(hiddenLayer)) {
            Set<Layer> oldHiddenLayers = new HashSet<Layer>(hiddenLayers);
            hiddenLayers.remove(hiddenLayer);
            if (worldRenderer != null) {
                worldRenderer.removeHiddenLayer(hiddenLayer);
                refreshTiles();
            }
            firePropertyChange("hiddenLayers", oldHiddenLayers, hiddenLayers);
        }
    }

    public Set<Layer> getHiddenLayers() {
        return (Set<Layer>) hiddenLayers.clone();
    }

    public void refreshTiles() {
        synchronized (dirtyTiles) {
            dirtyTileNeighbours.clear();
            for (Tile tile: dimension.getTiles()) {
                dirtyTiles.add(new Point(tile.getX(), tile.getY()));
            }
        }
        repaint();
    }

    public void refreshTilesForLayer(Layer layer) {
        if (hiddenLayers.contains(layer)) {
            return;
        }
        boolean repaint = false;
        synchronized (dirtyTiles) {
            for (Tile tile: dimension.getTiles()) {
                if (tile.getLayers().contains(layer)) {
                    Point coords = new Point(tile.getX(), tile.getY());
                    dirtyTileNeighbours.remove(coords);
                    dirtyTiles.add(coords);
                    repaint = true;
                }
            }
        }
        if (repaint) {
            repaint();
        }
    }
    
    @Override
    public void updateStatusBar(int x, int y) {
        App.getInstance().updateStatusBar(x, y);
    }

    public BufferedImage getImage() {
        synchronized (dirtyTiles) {
            for (Point tileCoords: dirtyTiles) {
                Tile tile = dimension.getTile(tileCoords);
                // Not sure how the tile could suddenly no longer exist, but
                // this has happened in practice, so check for it:
                if (tile != null) {
                    paintTile(tile);
                }
            }
            dirtyTiles.clear();
            for (Point tileCoords: dirtyTileNeighbours) {
                Tile tile = dimension.getTile(tileCoords);
                // Not sure how the tile could suddenly no longer exist, but
                // this has happened in practice, so check for it:
                if (tile != null) {
                    paintTile(tile);
                }
            }
            dirtyTileNeighbours.clear();
        }
        return worldImage;
    }

    public void rotateLightLeft() {
        lightOrigin = lightOrigin.left();
        if (tileRenderer != null) {
            tileRenderer.setLightOrigin(lightOrigin);
            refreshTiles();
        }
    }

    public void rotateLightRight() {
        lightOrigin = lightOrigin.right();
        if (tileRenderer != null) {
            tileRenderer.setLightOrigin(lightOrigin);
            refreshTiles();
        }
    }
    
    public LightOrigin getLightOrigin() {
        return lightOrigin;
    }
    
    public void setLightOrigin(LightOrigin lightOrigin) {
        if (lightOrigin == null) {
            throw new NullPointerException();
        }
        if (lightOrigin != this.lightOrigin) {
            this.lightOrigin = lightOrigin;
            if (tileRenderer != null) {
                tileRenderer.setLightOrigin(lightOrigin);
                refreshTiles();
            }
        }
    }
    
    public void moveToSpawn() {
        if ((dimension != null) && (dimension.getDim() == DIM_NORMAL)) {
            moveTo(dimension.getWorld().getSpawnPoint());
        }
    }

    public void moveToOrigin() {
        moveTo(0, 0);
    }

    public void moveTo(Point location) {
        moveTo(location.x, location.y);
    }
    
    public void moveTo(final int x, final int y) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point viewCoords = imageToViewCoordinates(worldToImageCoordinates(x, y));
                Rectangle visibleRect = getVisibleRect();
                Rectangle rectangle = new Rectangle(viewCoords.x - visibleRect.width / 2, viewCoords.y - visibleRect.height / 2, visibleRect.width, visibleRect.height);
                scrollRectToVisible(rectangle);
                refreshBrush();
            }
        });
    }
    
    public void moveTo(Point location, Point viewLocation) {
        moveTo(location.x, location.y, viewLocation.x, viewLocation.y);
    }
    
    public void moveTo(final int x, final int y, final int viewX, final int viewY) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point viewCoords = imageToViewCoordinates(worldToImageCoordinates(x, y));
                Rectangle visibleRect = getVisibleRect();
                Rectangle rectangle = new Rectangle(viewCoords.x - viewX, viewCoords.y - viewY, visibleRect.width, visibleRect.height);
                scrollRectToVisible(rectangle);
                refreshBrush();
            }
        });
    }

    public Point getViewCentreInWorldCoords() {
        if (worldImage != null) {
            Rectangle visibleRect = getVisibleRect();
            int worldImageWidth = (int) (worldImage.getWidth() * scale);
            int worldImageHeight = (int) (worldImage.getHeight() * scale);
            int xCentre = (worldImageWidth < visibleRect.width)   ? worldImageWidth / 2  : visibleRect.x + visibleRect.width  / 2;
            int yCentre = (worldImageHeight < visibleRect.height) ? worldImageHeight / 2 : visibleRect.y + visibleRect.height / 2;
            return imageToWorldCoordinates(viewToImageCoordinates(new Point(xCentre, yCentre)));
        } else {
            return null;
        }
    }

    public int getBrushRotation() {
        return brushRotation;
    }
    
    public void setBrushRotation(int brushRotation) {
        int oldBrushRotation = this.brushRotation;
        int oldEffectiveRadius = effectiveRadius;
        this.brushRotation = brushRotation;
        if ((brushShape == BrushShape.CIRCLE) || ((brushRotation % 90) == 0)) {
            effectiveRadius = radius;
        } else {
            double a = brushRotation / 180.0 * Math.PI;
            effectiveRadius = (int) Math.ceil(Math.abs(Math.sin(a)) * radius + Math.abs(Math.cos(a)) * radius);
        }
        firePropertyChange("brushRotation", oldBrushRotation, brushRotation);
        if (drawRadius && (brushShape != BrushShape.CIRCLE)) {
            int largestRadius = Math.max(oldEffectiveRadius, effectiveRadius);
            int extraPixels = Math.max((int) (1 / scale), 1);
            int diameter = largestRadius * 2 + 1 + 2 * extraPixels; // Extra pixels because when zoomed in, Swing colours outside the lines...
            repaint(imageToViewCoordinates(mouseX - largestRadius - extraPixels, mouseY - largestRadius - extraPixels, diameter, diameter));
        }
    }
    
    // Dimension.Listener

    @Override
    public void tileAdded(Dimension dimension, Tile tile) {
        tile.addListener(this);
        if (! inhibitUpdates) {
            queueDirtyTiles(tile);
        }
    }

    @Override
    public void tileRemoved(Dimension dimension, Tile tile) {
        tile.removeListener(this);
        synchronized (dirtyTiles) {
            Point coords = new Point(tile.getX(), tile.getY());
            dirtyTiles.remove(coords);
            dirtyTileNeighbours.remove(coords);
        }
        resizeImageIfNecessary();
        if ((tile.getX() >= dimension.getLowestX())
                && (tile.getX() <= dimension.getHighestX())
                && (tile.getY() >= dimension.getLowestY())
                && (tile.getY() <= dimension.getHighestY())) {
            Graphics2D g2 = worldImage.createGraphics();
            try {
                g2.setColor(new Color(VOID_COLOUR));
                g2.fillRect((tile.getX() - imageX) * TILE_SIZE, (tile.getY() - imageY) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            } finally {
                g2.dispose();
            }
        }
        repaint();
    }
    
    public void minecraftSeedChanged(Dimension dimension, long newSeed) {
        if ((! inhibitUpdates) && (! hiddenLayers.contains(Biome.INSTANCE))) {
            refreshTiles();
        }
    }

    // Tile.Listener

    @Override
    public void heightMapChanged(Tile tile) {
        if (! inhibitUpdates) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(tile + " height map changed; queueing for repaint");
            }
            queueDirtyTiles(tile);
        }
    }

    @Override
    public void terrainChanged(Tile tile) {
        if (! inhibitUpdates) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(tile + " terrain changed; queueing for repaint");
            }
            queueDirtyTiles(tile);
        }
    }

    @Override
    public void waterLevelChanged(Tile tile) {
        if (! inhibitUpdates) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(tile + " water level changed; queueing for repaint");
            }
            queueDirtyTiles(tile);
        }
    }

    @Override
    public void seedsChanged(Tile tile) {
        if (! inhibitUpdates) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(tile + " seeds changed; queueing for repaint");
            }
            queueDirtyTiles(tile);
        }
    }

    @Override
    public void layerDataChanged(Tile tile, Set<Layer> changedLayers) {
        if (! inhibitUpdates) {
            for (Layer layer: changedLayers) {
                if (! hiddenLayers.contains(layer)) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer(tile + " " + changedLayers + " layer data changed; queueing for repaint");
                    }
                    queueDirtyTiles(tile);
                    return;
                }
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(tile + " " + changedLayers + " layer data changed; none are visible, not queueing for repaint");
            }
        }
    }

    @Override
    public void allBitLayerDataChanged(Tile tile) {
        if (! inhibitUpdates) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(tile + " all bit layer data changed; queueing for repaint");
            }
            queueDirtyTiles(tile);
        }
    }

    @Override
    public void allNonBitlayerDataChanged(Tile tile) {
        if (! inhibitUpdates) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(tile + " all non-bit layer data changed; queueing for repaint");
            }
            queueDirtyTiles(tile);
        }
    }
    
    // MouseMotionListener

    @Override
    public void mouseDragged(MouseEvent e) {
        int oldMouseX = mouseX;
        int oldMouseY = mouseY;
        mouseX = (int) (e.getX() / scale);
        mouseY = (int) (e.getY() / scale);
        if ((mouseX == oldMouseX) && (mouseY == oldMouseY)) {
            return;
        }
        int repaintRadius = -1;
        if (drawRadius) {
            repaintRadius = effectiveRadius;
        }
        if (drawViewDistance && (VIEW_DISTANCE_RADIUS > repaintRadius)) {
            repaintRadius = VIEW_DISTANCE_RADIUS;
        }
        if (drawWalkingDistance && (DAY_NIGHT_WALK_DISTANCE_RADIUS > repaintRadius)) {
            repaintRadius = DAY_NIGHT_WALK_DISTANCE_RADIUS;
        }
        if (repaintRadius != -1) {
            int extraPixels = Math.max((int) (1 / scale), 1);  // Extra pixels because when zoomed in, Swing colours outside the lines...
            int diameter = repaintRadius * 2 + 1;
            Rectangle oldRectangle = new Rectangle(oldMouseX - repaintRadius, oldMouseY - repaintRadius, diameter, diameter);
            Rectangle newRectangle = new Rectangle(mouseX - repaintRadius, mouseY - repaintRadius, diameter, diameter);
            Rectangle unionRectangle = oldRectangle.union(newRectangle);
            Rectangle viewRectangle = imageToViewCoordinates(unionRectangle);
            Rectangle adjustedRectangle = new Rectangle(viewRectangle.x - extraPixels, viewRectangle.y - extraPixels, viewRectangle.width + extraPixels * 2, viewRectangle.height + extraPixels * 2);
            repaint(adjustedRectangle);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseDragged(e);
    }

    // Scrollable

    @Override
    public java.awt.Dimension getPreferredScrollableViewportSize() {
        if (worldImage != null) {
            return new java.awt.Dimension((int) (worldImage.getWidth() * scale), (int) (worldImage.getHeight() * scale));
        } else {
            return new java.awt.Dimension(0, 0);
        }
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 100;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    // PropertyChangeListener

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt.getSource() == dimension.getWorld()) && evt.getPropertyName().equals("spawnPoint")) {
            repaint();
        }
    }

    int getImageSize() {
        return (worldImage != null) ? MemoryUtils.getSize(worldImage, Collections.<Class<?>>emptySet()) : 0;
    }

    int getOverlayImageSize() {
        return (overlay != null) ? MemoryUtils.getSize(overlay, Collections.<Class<?>>emptySet()) : 0;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (worldImage != null) {
//            long start = System.nanoTime();
            Graphics2D g2 = (Graphics2D) g;
            Color savedColour = g2.getColor();
            Object savedAAValue = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            Object savedInterpolationValue = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            Stroke savedStroke = g2.getStroke();
            AffineTransform savedTransform = g2.getTransform();
//            long start2 = 0, end2 = 0, start3 = 0, end3 = 0, start4 = 0, end4 = 0, start5 = 0, end5 = 0;
            try {
                if (zoom != 0) {
                    g2.scale(scale, scale);
                }
                Rectangle clipBounds = g.getClipBounds();
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Clip: " + clipBounds.width + "x" + clipBounds.height);
                }
//                start2 = System.nanoTime();
                updateImage(clipBounds);
//                end2 = System.nanoTime();
                if (zoom < 0) {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                }
//                start3 = System.nanoTime();
                g2.drawImage(worldImage, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height, null);
//                end3 = System.nanoTime();
                if (drawGrid) {
//                    start4 = System.nanoTime();
                    drawGrid(g2, clipBounds);
//                    end4 = System.nanoTime();
                }
                if (dimension.getDim() == 0) {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke());
                    Point spawnPoint = dimension.getWorld().getSpawnPoint();
                    g2.fillRect(-imageX * TILE_SIZE + spawnPoint.x - 1, -imageY * TILE_SIZE + spawnPoint.y    , 3, 1);
                    g2.fillRect(-imageX * TILE_SIZE + spawnPoint.x    , -imageY * TILE_SIZE + spawnPoint.y - 1, 1, 3);
                }
                if (drawOverlay && (overlay != null)) {
//                    start5 = System.nanoTime();
                    drawOverlay(g2);
//                    end5 = System.nanoTime();
                }
                if (drawRadius || drawViewDistance || drawWalkingDistance || drawLabels) {
                    g2.setColor(Color.BLACK);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                if (drawRadius) {
                    float strokeWidth = 1 / scale;
                    g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {3 * strokeWidth, 3 * strokeWidth}, 0));
                    int diameter = radius * 2 + 1;
                    switch (brushShape) {
                        case CIRCLE:
                            g2.drawOval(mouseX - radius, mouseY - radius, diameter, diameter);
                            break;
                        case SQUARE:
                            if (brushRotation % 90 == 0) {
                                g2.drawRect(mouseX - radius, mouseY - radius, diameter, diameter);
                            } else {
                                AffineTransform existingTransform = g2.getTransform();
                                try {
                                    g2.rotate(brushRotation / 180.0 * Math.PI, mouseX, mouseY);
                                    g2.drawRect(mouseX - radius, mouseY - radius, diameter, diameter);
                                } finally {
                                    g2.setTransform(existingTransform);
                                }
                            }
                            break;
                    }
                }
                if (drawViewDistance) {
                    float strokeWidth = 1 / scale;
                    g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {10 * strokeWidth, 10 * strokeWidth}, 0));
                    g2.drawOval(mouseX - VIEW_DISTANCE_RADIUS, mouseY - VIEW_DISTANCE_RADIUS, VIEW_DISTANCE_DIAMETER, VIEW_DISTANCE_DIAMETER);
                }
                if (drawWalkingDistance) {
                    float strokeWidth = 1 / scale;
                    g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {20 * strokeWidth, 20 * strokeWidth}, 0));
                    g2.drawOval(mouseX - DAY_NIGHT_WALK_DISTANCE_RADIUS, mouseY - DAY_NIGHT_WALK_DISTANCE_RADIUS, DAY_NIGHT_WALK_DISTANCE_DIAMETER, DAY_NIGHT_WALK_DISTANCE_DIAMETER);
                    g2.drawOval(mouseX - DAY_WALK_DISTANCE_RADIUS, mouseY - DAY_WALK_DISTANCE_RADIUS, DAY_WALK_DISTANCE_DIAMETER, DAY_WALK_DISTANCE_DIAMETER);
                    g2.drawOval(mouseX - FIVE_MINUTE_WALK_DISTANCE_RADIUS, mouseY - FIVE_MINUTE_WALK_DISTANCE_RADIUS, FIVE_MINUTE_WALK_DISTANCE_DIAMETER, FIVE_MINUTE_WALK_DISTANCE_DIAMETER);
                }
                if (drawLabels && ((mouseX > 0) ||(mouseY > 0))) {
                    Point worldCoords = imageToWorldCoordinates(mouseX, mouseY);
                    int height = dimension.getIntHeightAt(worldCoords);
                    int labelY = mouseY + labelAscent + 2;
                    g2.setFont(labelFont);
                    g2.setColor(Color.WHITE);
                    g2.drawString(Integer.toString(height), mouseX + 2, labelY);
                    labelY += labelLineHeight;
                    Terrain terrain = dimension.getTerrainAt(worldCoords.x, worldCoords.y);
                    if (terrain != null) {
                        g2.drawString(terrain.getName(), mouseX + 2, labelY);
                        labelY += labelLineHeight;
                    }
                }
            } finally {
                g2.setColor(savedColour);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedAAValue);
                if (savedInterpolationValue != null) {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolationValue);
                }
                g2.setStroke(savedStroke);
                g2.setTransform(savedTransform);
            }
//            long end = System.nanoTime();
//            if (logger.isLoggable(Level.FINER)) {
//                logger.finer("Total drawing time: " + ((end - start) / 1000000f) + "ms");
//                if (start2 != 0) {
//                    logger.fine("Updating image took " + ((end2 - start2) / 1000000f) + "ms");
//                    logger.fine("Drawing image took " + ((end3 - start3) / 1000000f) + "ms");
//                    if (start4 != 0) {
//                        logger.fine("Drawing grid took " + ((end4 - start4) / 1000000f) + "ms");
//                    }
//                }
//                if (start5 != 0) {
//                    logger.fine("Drawing overlay took " + ((end5 - start5) / 1000000f) + "ms");
//                }
//            }
        }
    }

    BufferedImage paintImage(Dimension dimension, ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled {
//        long start = System.currentTimeMillis();
        TileRenderer myTileRenderer = new TileRenderer(dimension, colourScheme, biomeScheme, customBiomeManager);
        myTileRenderer.setContourLines(dimension.isContoursEnabled());
        myTileRenderer.setContourSeparation(dimension.getContourSeparation());
        DimensionRenderer dimensionRenderer = new DimensionRenderer(dimension, myTileRenderer);
        for (Layer hiddenLayer: hiddenLayers) {
            dimensionRenderer.addHiddenLayer(hiddenLayer);
        }
        int width = dimension.getWidth() * TILE_SIZE;
        int height = dimension.getHeight() * TILE_SIZE;
        BufferedImage image = createOptimalBufferedImage(width, height);
        int totalTiles = dimension.getWidth() * dimension.getHeight();
        logger.info("Painting " + totalTiles + " tiles");
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setColor(new Color(Constants.VOID_COLOUR));
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            int tileCount = 0;
            for (int x = dimension.getLowestX(); x <= dimension.getHighestX(); x++) {
                for (int y = dimension.getLowestY(); y <= dimension.getHighestY(); y++) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Painting tile " + x + ", " + y + " at " + ((x - dimension.getLowestX()) * TILE_SIZE) + ", " + ((y - dimension.getLowestY()) * TILE_SIZE));
                    }
                    dimensionRenderer.renderTile(image, x, y);
                    tileCount++;
                    if (progressReceiver != null) {
                        progressReceiver.setProgress((float) tileCount / totalTiles);
                    }
                }
            }
        } finally {
            g2.dispose();
        }
//        System.out.println("Painting image took " + (System.currentTimeMillis() - start) + " ms");
        return image;
    }
    
    private void createImage(BufferedImage image) {
        try {
            worldImage = (image != null) ? image : paintImage(dimension, null);
        } catch (ProgressReceiver.OperationCancelled e) {
            throw new InternalError("Should never happen");
        }
        imageX = dimension.getLowestX();
        imageY = dimension.getLowestY();
        imageWidth = dimension.getWidth();
        imageHeight = dimension.getHeight();
        resize();
    }
    
    private BufferedImage createOptimalBufferedImage(int width, int height) {
        if (createOptimalImage) {
            logger.info("Creating optimal image of size " + width + "x" + height);
            return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
        } else {
            logger.info("Creating non-optimal image of size " + width + "x" + height);
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
    }

    private void updateImage(Rectangle bounds) {
//        bounds = new Rectangle((int) (bounds.x / scale), (int) (bounds.y / scale), (int) (bounds.width / scale), (int) (bounds.height / scale));
        Rectangle tileRect = new Rectangle(0, 0, TILE_SIZE, TILE_SIZE);
        Set<Point> dirtyTilesCopy;
        Set<Point> paintedTiles = new HashSet<Point>();
        // Copy the set of dirty tiles to avoid deadlocks caused by having a
        // lock on it while a background operation is changing tiles
        synchronized (dirtyTiles) {
            dirtyTilesCopy = new HashSet<Point>(dirtyTiles);
            dirtyTilesCopy.addAll(dirtyTileNeighbours);
        }
        for (Iterator<Point> i = dirtyTilesCopy.iterator(); i.hasNext(); ) {
            Point tileCoords = i.next();
            tileRect.x = (tileCoords.x - imageX) * TILE_SIZE;
            tileRect.y = (tileCoords.y - imageY) * TILE_SIZE;
            if (bounds.intersects(tileRect)) {
                Tile tile = dimension.getTile(tileCoords);
                // Not sure how the tile could suddenly no longer exist, but
                // this has happened in practice, so check for it:
                if (tile != null) {
                    paintTile(tile);
                    paintedTiles.add(tileCoords);
                }
                i.remove();
            } else if (logger.isLoggable(Level.FINER)) {
                logger.finer("Tile at coordinates " + tileCoords + " outside clipping area; not repainting it");
            }
        }
        // TODO: race condition here: a tile could have become dirty again after
        // it was painted but before we remove it from dirtyTiles, in which case
        // it won't be repainted. Let's see how much of a problem this is in
        // practice
        synchronized (dirtyTiles) {
            dirtyTiles.removeAll(paintedTiles);
            dirtyTileNeighbours.removeAll(paintedTiles);
        }
        if (logger.isLoggable(Level.FINE) && (! paintedTiles.isEmpty())) {
            logger.fine("Painted " + paintedTiles.size() + " tiles");
        }
    }

    private void paintTile(Tile tile) {
        synchronized (tile) {
            int tileX = tile.getX();
            int tileY = tile.getY();
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Painting tile " + tileX + ", " + tileY + " at " + ((tileX - imageX) * TILE_SIZE) + ", " + ((tileY - imageY) * TILE_SIZE));
            }
            worldRenderer.renderTile(worldImage, tileX, tileY);
        }
    }

    private void resizeImageIfNecessary() {
        int newLowestX = dimension.getLowestX();
        int newHighestX = dimension.getHighestX();
        int newLowestY = dimension.getLowestY();
        int newHighestY = dimension.getHighestY();
        int newWidth = newHighestX - newLowestX + 1;
        int newHeight = newHighestY - newLowestY + 1;
        if ((newWidth != imageWidth) || (newHeight != imageHeight)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Resizing world image from " + imageX + ", " + imageY + ", " + imageWidth + "x" + imageHeight + " to " + newLowestX + ", " + newLowestY + ", " + newWidth + "x" + newHeight);
                logger.fine("Creating image of size " + (newWidth * TILE_SIZE) + "x" + (newHeight * TILE_SIZE));
            }
            BufferedImage newImage = createOptimalBufferedImage(newWidth * TILE_SIZE, newHeight * TILE_SIZE);
            int dx = imageX - newLowestX;
            int dy = imageY - newLowestY;
            Graphics2D g2 = newImage.createGraphics();
            try {
                g2.setColor(new Color(Constants.VOID_COLOUR));
                g2.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Painting old image at " + (dx * TILE_SIZE) + ", " + (dy * TILE_SIZE));
                }
                g2.drawImage(worldImage, dx * TILE_SIZE, dy * TILE_SIZE, null);
            } finally {
                g2.dispose();
            }
            worldImage = newImage;
            imageX = newLowestX;
            imageY = newLowestY;
            imageWidth = newWidth;
            imageHeight = newHeight;
            resize();
        }
    }

    private void resize() {
        setPreferredSize(getPreferredScrollableViewportSize());
        revalidate();
    }

    private void registerTileListeners() {
        for (int x = dimension.getLowestX(); x <= dimension.getHighestX(); x++) {
            for (int y = dimension.getLowestY(); y <= dimension.getHighestY(); y++) {
                Tile tile = dimension.getTile(x, y);
                if (tile != null) {
                    tile.addListener(this);
                }
            }
        }
    }

    private void unregisterTileListeners() {
        for (int x = dimension.getLowestX(); x <= dimension.getHighestX(); x++) {
            for (int y = dimension.getLowestY(); y <= dimension.getHighestY(); y++) {
                Tile tile = dimension.getTile(x, y);
                if (tile != null) {
                    tile.removeListener(this);
                }
            }
        }
    }

    private void queueDirtyTiles(final Tile tile) {
        int tileX = tile.getX(), tileY = tile.getY();
        Point tileCoords = new Point(tileX, tileY);
        synchronized (dirtyTiles) {
            if (! dirtyTiles.contains(tileCoords)) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Queueing dirty tile " + tileX + ", " + tileY);
                }
                dirtyTiles.add(tileCoords);
                Rectangle repaintArea = new Rectangle((tileX - imageX) * TILE_SIZE, (tileY - imageY) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                boolean repaintAll = false;
                if ((tileX < imageX) || (tileX >= (imageX + imageWidth)) || (tileY < imageY) || (tileY >= (imageY + imageHeight))) {
                    resizeImageIfNecessary();
                    if ((tileX < imageX) || (tileY < imageY)) {
                        repaintAll = true;
                    }
                }
                if (dirtyTileNeighbours.contains(tileCoords)) {
                    dirtyTileNeighbours.remove(tileCoords);
                }
                Point neighbourCoords = new Point(tileX - 1, tileY);
                if ((! dirtyTiles.contains(neighbourCoords)) && (! dirtyTileNeighbours.contains(neighbourCoords)) && (dimension.getTile(neighbourCoords) != null)) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Queueing dirty tile neighbour " + (tileX - 1) + ", " + tileY);
                    }
                    dirtyTileNeighbours.add(neighbourCoords);
                    if ((tileX - 1) < imageX) {
                        repaintAll = true;
                    } else {
                        repaintArea = repaintArea.union(new Rectangle((neighbourCoords.x - imageX) * TILE_SIZE, (neighbourCoords.y - imageY) * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                    }
                }
                neighbourCoords = new Point(tileX + 1, tileY);
                if ((! dirtyTiles.contains(neighbourCoords)) && (! dirtyTileNeighbours.contains(neighbourCoords)) && (dimension.getTile(neighbourCoords) != null)) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Queueing dirty tile neighbour " + (tileX + 1) + ", " + tileY);
                    }
                    dirtyTileNeighbours.add(neighbourCoords);
                    repaintArea = repaintArea.union(new Rectangle((neighbourCoords.x - imageX) * TILE_SIZE, (neighbourCoords.y - imageY) * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
                neighbourCoords = new Point(tileX, tileY - 1);
                if ((! dirtyTiles.contains(neighbourCoords)) && (! dirtyTileNeighbours.contains(neighbourCoords)) && (dimension.getTile(neighbourCoords) != null)) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Queueing dirty tile neighbour " + tileX + ", " + (tileY - 1));
                    }
                    dirtyTileNeighbours.add(neighbourCoords);
                    if ((tileY - 1) < imageX) {
                        repaintAll = true;
                    } else {
                        repaintArea = repaintArea.union(new Rectangle((neighbourCoords.x - imageX) * TILE_SIZE, (neighbourCoords.y - imageY) * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                    }
                }
                neighbourCoords = new Point(tileX, tileY + 1);
                if ((! dirtyTiles.contains(neighbourCoords)) && (! dirtyTileNeighbours.contains(neighbourCoords)) && (dimension.getTile(neighbourCoords) != null)) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Queueing dirty tile neighbour " + tileX + ", " + (tileY + 1));
                    }
                    dirtyTileNeighbours.add(neighbourCoords);
                    repaintArea = repaintArea.union(new Rectangle((neighbourCoords.x - imageX) * TILE_SIZE, (neighbourCoords.y - imageY) * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
                if (repaintAll) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Requesting repaint of entire view");
                    }
                    repaint();
                } else {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Requesting repaint for " + repaintArea);
                    }
                    repaint(imageToViewCoordinates(repaintArea));
                }
            }
        }
    }

//    private void scaleOverlay() {
//        if (overlayScale == 1.0f) {
//            scaledOverlay = overlay;
//        } else {
//            if ((scalingOp == null) || (overlayScale != scalingOpScale)) {
//                scalingOp = new AffineTransformOp(AffineTransform.getScaleInstance(overlayScale, overlayScale), AffineTransformOp.TYPE_BICUBIC);
//                scalingOpScale = overlayScale;
//            }
//            scaledOverlay = scalingOp.filter(overlay, null);
//        }
//    }

    private void drawGrid(Graphics2D g2, Rectangle clipBounds) {
        if (worldImage == null) {
            return;
        }
        int xOffset = MathUtils.mod(imageX << TILE_SIZE_BITS, gridSize), yOffset = MathUtils.mod(imageY << TILE_SIZE_BITS, gridSize);
//        System.out.println("xOffset: " + xOffset + ", yOffset: " + yOffset);
//        int x1 = ((int) (clipBounds.x / scale) / gridSize) * gridSize - xOffset;
//        int x2 = ((int) ((clipBounds.x + clipBounds.width) / scale) / gridSize + 2) * gridSize - xOffset;
//        int y1 = ((int) (clipBounds.y / scale) / gridSize) * gridSize - yOffset;
//        int y2 = ((int) ((clipBounds.y + clipBounds.height) / scale) / gridSize + 2) * gridSize - yOffset;
        int x1 = clipBounds.x / gridSize * gridSize - xOffset;
        int x2 = ((clipBounds.x + clipBounds.width) / gridSize + 2) * gridSize - xOffset;
        int y1 = clipBounds.y / gridSize * gridSize - yOffset;
        int y2 = ((clipBounds.y + clipBounds.height) / gridSize + 2) * gridSize - yOffset;
//        System.out.println(x1 + "," + y1 + " -> " + x2 + "," + y2);
//        int width = worldImage.getWidth(), height = worldImage.getHeight();
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {2.0f, 2.0f}, 0.0f));
//        for (int dx = 0; dx < width + gridSize; dx += gridSize) {
//            g2.drawLine(dx - xOffset, 0, dx - xOffset, height - 1);
//        }
//        for (int dy = 0; dy < height + gridSize; dy += gridSize) {
//            g2.drawLine(0, dy - yOffset, width - 1, dy - yOffset);
//        }
        for (int x = x1; x <= x2; x+= gridSize) {
            g2.drawLine(x, y1, x, y2);
        }
        for (int y = y1; y <= y2; y+= gridSize) {
            g2.drawLine(x1, y, x2, y);
        }
    }

    private void drawOverlay(Graphics2D g2) {
        if (overlayTransparency == 1.0f) {
            // Fully transparent
            return;
        } else  if (overlayTransparency > 0.0f) {
            // Transparent
            Composite savedComposite = g2.getComposite();
            try {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - overlayTransparency));
//                g2.drawImage(scaledOverlay, overlayOffsetX, overlayOffsetY, null);
                if (overlayScale == 1.0f) {
                    // 1:1 scale
                    g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, null);
                } else {
                    int width = Math.round(overlay.getWidth() * overlayScale);
                    int height = Math.round(overlay.getHeight() * overlayScale);
                    Object savedInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                    try {
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, width, height, null);
                    } finally {
                        if (savedInterpolation != null) {
                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolation);
                        }
                    }
                }
            } finally {
                g2.setComposite(savedComposite);
            }
        } else {
            // Fully opaque
//            g2.drawImage(scaledOverlay, overlayOffsetX, overlayOffsetY, null);
            if (overlayScale == 1.0f) {
                // 1:1 scale
                g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, null);
            } else {
                int width = Math.round(overlay.getWidth() * overlayScale);
                int height = Math.round(overlay.getHeight() * overlayScale);
                Object savedInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                try {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, width, height, null);
                } finally {
                    if (savedInterpolation != null) {
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolation);
                    }
                }
            }
        }
    }

    @Override
    public Point getMousePosition() throws HeadlessException {
        Point translation = new Point(0, 0);
        Component component = this;
        while (component != null) {
            Point mousePosition = (component == this) ? super.getMousePosition() : component.getMousePosition();
            if (mousePosition != null) {
                mousePosition.translate(-translation.x, -translation.y);
                return mousePosition;
            } else {
                translation.translate(component.getX(), component.getY());
                component = component.getParent();
            }
        }
        return null;
    }
    
    private final Set<Point> dirtyTiles = new HashSet<Point>(), dirtyTileNeighbours = new HashSet<Point>();
    private final HashSet<Layer> hiddenLayers = new HashSet<Layer>();
    private final Font labelFont;
    private final int labelAscent, labelLineHeight;
    private final boolean createOptimalImage = ! "false".equalsIgnoreCase(System.getProperty("org.pepsoft.worldpainter.createOptimalImage"));
    private final CustomBiomeManager customBiomeManager;
    private Dimension dimension;
    private BufferedImage worldImage;
    private TileRenderer tileRenderer;
    private DimensionRenderer worldRenderer;
    private int imageX, imageY, imageWidth, imageHeight, mouseX, mouseY, radius, effectiveRadius, zoom, overlayOffsetX, overlayOffsetY, gridSize, contourSeparation, brushRotation;
    private boolean drawRadius, drawGrid, drawOverlay, drawContours, drawViewDistance, drawWalkingDistance, drawLabels;
    private BrushShape brushShape;
    private float scale = 1.0f, overlayScale = 1.0f, overlayTransparency = 0.5f;
    private ColourScheme colourScheme;
    private BiomeScheme biomeScheme;
    private BufferedImage overlay/*, scaledOverlay*/;
//    private BufferedImageOp scalingOp;
//    private float scalingOpScale;
    private LightOrigin lightOrigin = LightOrigin.NORTHWEST;
    private volatile boolean inhibitUpdates;
    
    private static final int VIEW_DISTANCE_RADIUS = 210;
    private static final int VIEW_DISTANCE_DIAMETER = 2 * VIEW_DISTANCE_RADIUS;
    private static final int FIVE_MINUTE_WALK_DISTANCE_RADIUS = 1280;
    private static final int FIVE_MINUTE_WALK_DISTANCE_DIAMETER = 2 * FIVE_MINUTE_WALK_DISTANCE_RADIUS;
    private static final int DAY_WALK_DISTANCE_RADIUS = 3328;
    private static final int DAY_WALK_DISTANCE_DIAMETER = 2 * DAY_WALK_DISTANCE_RADIUS;
    private static final int DAY_NIGHT_WALK_DISTANCE_RADIUS = 5120;
    private static final int DAY_NIGHT_WALK_DISTANCE_DIAMETER = 2 * DAY_NIGHT_WALK_DISTANCE_RADIUS;
    private static final Logger logger = Logger.getLogger(WorldPainter.class.getName());
    private static final long serialVersionUID = 1L;
}