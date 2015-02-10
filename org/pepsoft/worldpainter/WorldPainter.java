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
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.pepsoft.util.MemoryUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.TileRenderer.LightOrigin;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.operations.BrushShape;

import static org.pepsoft.worldpainter.Constants.DIM_NORMAL;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.tools.BiomesTileProvider;

/**
 *
 * @author pepijn
 */
public class WorldPainter extends WorldPainterView implements MouseMotionListener, PropertyChangeListener {
    public WorldPainter(ColourScheme colourScheme, BiomeScheme biomeScheme, CustomBiomeManager customBiomeManager) {
        super(false, Math.max(Runtime.getRuntime().availableProcessors() - 1, 1), false);
        this.colourScheme = colourScheme;
        this.biomeScheme = biomeScheme;
        this.customBiomeManager = customBiomeManager;
        setOpaque(true);
        addMouseMotionListener(this);
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
        Dimension oldDimension = this.dimension;
        if ((oldDimension != null) && (oldDimension.getDim() == DIM_NORMAL)) {
            oldDimension.getWorld().removePropertyChangeListener("spawnPoint", this);
        }
        this.dimension = dimension;
        if (dimension != null) {
            drawContours = dimension.isContoursEnabled();
            contourSeparation = dimension.getContourSeparation();
            if (dimension.getDim() == DIM_NORMAL) {
                dimension.getWorld().addPropertyChangeListener("spawnPoint", this);
            }

            setGridSize(dimension.getGridSize());
            setDrawGrid(dimension.isGridEnabled());
            
            // Scale is now managed by WorldPainter & ConfigureViewDialog
//            setOverlayScale(dimension.getOverlayScale());
            setOverlayTransparency(dimension.getOverlayTransparency());
            setOverlayOffsetX(dimension.getOverlayOffsetX());
            setOverlayOffsetY(dimension.getOverlayOffsetY());
            setOverlay(null);
            setDrawOverlay(dimension.isOverlayEnabled());
            setMarkerCoords((dimension.getDim() == DIM_NORMAL) ? dimension.getWorld().getSpawnPoint() : null);
        } else {
            setOverlay(null);
            setMarkerCoords(null);
        }
        firePropertyChange("dimension", oldDimension, dimension);
        refreshTiles();
    }

    public ColourScheme getColourScheme() {
        return colourScheme;
    }

    public void setColourScheme(ColourScheme colourScheme) {
        this.colourScheme = colourScheme;
        refreshTiles();
    }

    public BiomeScheme getBiomeScheme() {
        return biomeScheme;
    }

    public void setBiomeScheme(BiomeScheme biomeScheme) {
        this.biomeScheme = biomeScheme;
        if (! hiddenLayers.contains(Biome.INSTANCE)) {
            refreshTiles();
        }
    }

    @Override
    public boolean isDrawRadius() {
        return drawRadius;
    }

    @Override
    public void setDrawRadius(boolean drawRadius) {
        if (drawRadius != this.drawRadius) {
            this.drawRadius = drawRadius;
            firePropertyChange("drawRadius", ! drawRadius, drawRadius);
            int diameter = radius * 2 + 1;
            repaintWorld(mouseX - radius, mouseY - radius, diameter, diameter);
        }
    }

    public boolean isDrawViewDistance() {
        return drawViewDistance;
    }

    public void setDrawViewDistance(boolean drawViewDistance) {
        if (drawViewDistance != this.drawViewDistance) {
            this.drawViewDistance = drawViewDistance;
            firePropertyChange("drawViewDistance", !drawViewDistance, drawViewDistance);
            repaintWorld(mouseX - VIEW_DISTANCE_RADIUS, mouseY - VIEW_DISTANCE_RADIUS, VIEW_DISTANCE_DIAMETER + 1, VIEW_DISTANCE_DIAMETER + 1);
        }
    }

    public boolean isDrawWalkingDistance() {
        return drawWalkingDistance;
    }

    public void setDrawWalkingDistance(boolean drawWalkingDistance) {
        if (drawWalkingDistance != this.drawWalkingDistance) {
            this.drawWalkingDistance = drawWalkingDistance;
            firePropertyChange("drawWalkingDistance", !drawWalkingDistance, drawWalkingDistance);
            repaintWorld(mouseX - DAY_NIGHT_WALK_DISTANCE_RADIUS, mouseY - DAY_NIGHT_WALK_DISTANCE_RADIUS, DAY_NIGHT_WALK_DISTANCE_DIAMETER + 1, DAY_NIGHT_WALK_DISTANCE_DIAMETER + 1);
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
            int diameter = largestRadius * 2 + 1;
            repaintWorld(mouseX - largestRadius, mouseY - largestRadius, diameter, diameter);
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
                int diameter = largestRadius * 2 + 1;
                repaintWorld(mouseX - largestRadius, mouseY - largestRadius, diameter, diameter);
            }
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

//    public float getOverlayScale() {
//        return overlayScale;
//    }
//
//    public void setOverlayScale(float overlayScale) {
//        if (overlayScale != this.overlayScale) {
//            float oldOverlayScale = this.overlayScale;
//            this.overlayScale = overlayScale;
//            if (overlay != null) {
//                if (overlayTransparency < 1.0f) {
//                    repaint();
//                }
//            }
//            firePropertyChange("overlayScale", oldOverlayScale, overlayScale);
//        }
//    }

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
            } else if (drawOverlay && (dimension.getOverlay() != null)) {
                loadOverlay();
            }
            if (drawOverlay == this.drawOverlay) {
                firePropertyChange("drawOverlay", ! drawOverlay, drawOverlay);
            }
        }
    }
    
    private void loadOverlay() {
        File file = dimension.getOverlay();
        if (file.isFile()) {
            if (file.canRead()) {
                logger.info("Loading image");
                BufferedImage myOverlay;
                try {
                    myOverlay = ImageIO.read(file);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "I/O error while loading image " + file ,e);
                    JOptionPane.showMessageDialog(this, "An error occurred while loading the overlay image.\nIt may not be a valid or supported image file, or the file may be corrupted.", "Error Loading Image", JOptionPane.ERROR_MESSAGE);
                    this.drawOverlay = false;
                    return;
                } catch (RuntimeException e) {
                    logger.log(Level.SEVERE, e.getClass().getSimpleName() + " while loading image " + file ,e);
                    JOptionPane.showMessageDialog(this, "An error occurred while loading the overlay image.\nThere may not be enough available memory, or the image may be too large.", "Error Loading Image", JOptionPane.ERROR_MESSAGE);
                    this.drawOverlay = false;
                    return;
                } catch (Error e) {
                    logger.log(Level.SEVERE, e.getClass().getSimpleName() + " while loading image " + file ,e);
                    JOptionPane.showMessageDialog(this, "An error occurred while loading the overlay image.\nThere may not be enough available memory, or the image may be too large.", "Error Loading Image", JOptionPane.ERROR_MESSAGE);
                    this.drawOverlay = false;
                    return;
                }
                myOverlay = ConfigureViewDialog.scaleImage(myOverlay, getGraphicsConfiguration(), (int) (dimension.getOverlayScale() * 100));
                if (myOverlay != null) {
                    setOverlay(myOverlay);
                } else {
                    // The scaling failed
                    this.drawOverlay = false;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Access denied to overlay image\n" + file, "Error Enabling Overlay", JOptionPane.ERROR_MESSAGE);
                this.drawOverlay = false;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Overlay image file not found\n" + file, "Error Enabling Overlay", JOptionPane.ERROR_MESSAGE);
            this.drawOverlay = false;
        }
    }

    public int getContourSeparation() {
        return contourSeparation;
    }

    public void setContourSeparation(int contourSeparation) {
        if (contourSeparation != this.contourSeparation) {
            int oldContourSeparation = this.contourSeparation;
            this.contourSeparation = contourSeparation;
            refreshTiles();
            firePropertyChange("contourSeparation", oldContourSeparation, contourSeparation);
        }
    }

    public boolean isDrawContours() {
        return drawContours;
    }

    public void setDrawContours(boolean drawContours) {
        if (drawContours != this.drawContours) {
            this.drawContours = drawContours;
            refreshTiles();
            firePropertyChange("drawContours", ! drawContours, drawContours);
        }
    }

    @Override
    public boolean isInhibitUpdates() {
        return inhibitUpdates;
    }

    @Override
    public void setInhibitUpdates(boolean inhibitUpdates) {
        if (inhibitUpdates != this.inhibitUpdates) {
            this.inhibitUpdates = inhibitUpdates;
            if (inhibitUpdates) {
            } else {
                refresh();
            }
            firePropertyChange("inhibitUpdates", ! inhibitUpdates, inhibitUpdates);
        }
    }

    public void refreshBrush() {
        Point mousePos = getMousePosition();
        if (mousePos != null) {
            mouseMoved(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, mousePos.x, mousePos.y, 0, false));
        }
    }

    public void addHiddenLayer(Layer hiddenLayer) {
        if (! hiddenLayers.contains(hiddenLayer)) {
            Set<Layer> oldHiddenLayers = new HashSet<Layer>(hiddenLayers);
            hiddenLayers.add(hiddenLayer);
            if (dimension != null) {
                tileProvider.addHiddenLayer(hiddenLayer);
                refreshTilesForLayer(hiddenLayer, true);
            }
            firePropertyChange("hiddenLayers", oldHiddenLayers, hiddenLayers);
        }
    }

    public void removeHiddenLayer(Layer hiddenLayer) {
        if (hiddenLayers.contains(hiddenLayer)) {
            Set<Layer> oldHiddenLayers = new HashSet<Layer>(hiddenLayers);
            hiddenLayers.remove(hiddenLayer);
            if (dimension != null) {
                tileProvider.removeHiddenLayer(hiddenLayer);
                refreshTilesForLayer(hiddenLayer, true);
            }
            firePropertyChange("hiddenLayers", oldHiddenLayers, hiddenLayers);
        }
    }

    public Set<Layer> getHiddenLayers() {
        return (Set<Layer>) hiddenLayers.clone();
    }

    public void refreshTiles() {
        if (dimension != null) {
            BiomeScheme mcBiomeScheme = null;
            if (dimension.getDim() == DIM_NORMAL) {
                World2 world = dimension.getWorld();
                if (world != null) {
                    try {
                        if (world.getVersion() == org.pepsoft.minecraft.Constants.SUPPORTED_VERSION_1) {
                            mcBiomeScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_1, this, false);
                        } else if (world.getGenerator() == Generator.DEFAULT) {
                            mcBiomeScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_7_DEFAULT, this, false);
                            if (mcBiomeScheme == null) {
                                mcBiomeScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, this, false);
                            }
                        } else if (world.getGenerator() == Generator.LARGE_BIOMES) {
                            mcBiomeScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_7_LARGE, this, false);
                            if (mcBiomeScheme == null) {
                                mcBiomeScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_3_LARGE, this, false);
                            }
                        }
                        if (mcBiomeScheme != null) {
                            mcBiomeScheme.setSeed(dimension.getMinecraftSeed());
                        }
                    } catch (Exception e) {
                        // TODO: this seems to happen with the Minecraft 1.6 jar
                        // Why?
                        logger.log(Level.SEVERE, "An exception occurred while trying to load or initialize Minecraft jar; continuing without showing biomes", e);
                        mcBiomeScheme = null;
                    } catch (Error e) {
                        // TODO: this seems to happen with the Minecraft 1.6 jar
                        // Why?
                        logger.log(Level.SEVERE, "An error occurred while trying to load or initialize Minecraft jar; continuing without showing biomes", e);
                        mcBiomeScheme = null;
                    }
                }
            }
            tileProvider = new WPTileProvider(dimension, colourScheme, biomeScheme, customBiomeManager, hiddenLayers, drawContours, contourSeparation, lightOrigin, true, (mcBiomeScheme != null) ? new BiomesTileProvider(mcBiomeScheme, colourScheme, 0, true) : null, true);
            if (getTileProviderCount() == 0) {
                addTileProvider(tileProvider);
            } else {
                replaceTileProvider(0, tileProvider);
            }
        } else {
            if (getTileProviderCount() > 0) {
                removeTileProvider(0);
            }
            tileProvider = null;
        }
    }

    public void refreshTilesForLayer(Layer layer, boolean evenIfHidden) {
        if ((hiddenLayers.contains(layer) && (! evenIfHidden))
                || (dimension == null)) {
            return;
        }
        int count = 0;
        for (Tile tile: dimension.getTiles()) {
            if (tile.getLayers().contains(layer)) {
                refresh(tileProvider, tile.getX(), tile.getY());
                count++;
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Refreshing " + count + " tiles for layer " + layer.getName());
        }
    }
    
    @Override
    public void updateStatusBar(int x, int y) {
        App.getInstance().updateStatusBar(x, y);
    }

    public BufferedImage getImage() throws ProgressReceiver.OperationCancelled {
        if (dimension == null) {
            return null;
        }
        TileRenderer tileRenderer = new TileRenderer(dimension, colourScheme, biomeScheme, customBiomeManager);
        tileRenderer.setContourLines(drawContours);
        tileRenderer.setContourSeparation(contourSeparation);
        tileRenderer.setHiddenLayers(hiddenLayers);
        tileRenderer.setLightOrigin(lightOrigin);
        int xOffset = dimension.getLowestX(), yOffset = dimension.getLowestY();
        BufferedImage image = new BufferedImage(dimension.getWidth() << TILE_SIZE_BITS, dimension.getHeight() << TILE_SIZE_BITS, BufferedImage.TYPE_INT_ARGB);
        for (Tile tile: dimension.getTiles()) {
            tileRenderer.setTile(tile);
            tileRenderer.renderTile(image, (tile.getX() - xOffset) << TILE_SIZE_BITS, (tile.getY() - yOffset) << TILE_SIZE_BITS);
        }
        return image;
    }

    public void rotateLightLeft() {
        lightOrigin = lightOrigin.left();
        refreshTiles();
    }

    public void rotateLightRight() {
        lightOrigin = lightOrigin.right();
        refreshTiles();
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
            refreshTiles();
        }
    }
    
    public void moveToSpawn() {
        if ((dimension != null) && (dimension.getDim() == DIM_NORMAL)) {
            moveToMarker();
        }
    }

    public Point getViewCentreInWorldCoords() {
        return new Point(getViewX(), getViewY());
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
            int diameter = largestRadius * 2 + 1;
            repaintWorld(mouseX - largestRadius, mouseY - largestRadius, diameter, diameter);
        }
    }
    
    public void minecraftSeedChanged(Dimension dimension, long newSeed) {
        if ((! inhibitUpdates) && (! hiddenLayers.contains(Biome.INSTANCE))) {
            refreshTiles();
        }
    }

    // MouseMotionListener

    @Override
    public void mouseDragged(MouseEvent e) {
        int oldMouseX = mouseX;
        int oldMouseY = mouseY;
        Point mouseInWorld = viewToWorld(e.getPoint());
        mouseX = mouseInWorld.x;
        mouseY = mouseInWorld.y;
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
            int diameter = repaintRadius * 2 + 1;
            Rectangle oldRectangle = new Rectangle(oldMouseX - repaintRadius, oldMouseY - repaintRadius, diameter, diameter);
            final Rectangle newRectangle = new Rectangle(mouseX - repaintRadius, mouseY - repaintRadius, diameter, diameter);
            if (oldRectangle.intersects(newRectangle)) {
                Rectangle unionRectangle = oldRectangle.union(newRectangle);
                repaintWorld(unionRectangle.x, unionRectangle.y, unionRectangle.width, unionRectangle.height);
            } else {
                // Two separate repaints to avoid having to repaint a huge area
                // just because the cursor jumps a large distance for some
                // reason
                repaintWorld(oldRectangle.x, oldRectangle.y, oldRectangle.width, oldRectangle.height);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        repaintWorld(newRectangle.x, newRectangle.y, newRectangle.width, newRectangle.height);
                    }
                });
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseDragged(e);
    }

    // PropertyChangeListener

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt.getSource() == dimension.getWorld()) && evt.getPropertyName().equals("spawnPoint")) {
            setMarkerCoords((Point) evt.getNewValue());
        }
    }

    int getOverlayImageSize() {
        return (overlay != null) ? MemoryUtils.getSize(overlay, Collections.<Class<?>>emptySet()) : 0;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dimension != null) {
            final Graphics2D g2 = (Graphics2D) g;
            final Color savedColour = g2.getColor();
            final Object savedAAValue = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            final Object savedInterpolationValue = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            final Stroke savedStroke = g2.getStroke();
            final AffineTransform savedTransform = g2.getTransform();
            try {
                final float scale = transformGraphics(g2);
                final Rectangle clipBounds = g2.getClipBounds();
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Clip: " + clipBounds.width + "x" + clipBounds.height);
                }
                if (drawGrid) {
                    drawGrid(g2, clipBounds, scale);
                }
                if (drawOverlay && (overlay != null)) {
                    drawOverlay(g2);
                }
                if (drawRadius || drawViewDistance || drawWalkingDistance) {
                    g2.setColor(Color.BLACK);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                if (drawRadius) {
                    final float strokeWidth = 1 / scale;
                    g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {3 * strokeWidth, 3 * strokeWidth}, 0));
                    final int diameter = radius * 2 + 1;
                    // TODO: this draws one pixel too far to the right and down
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
                                    if (scale > 1.0f) {
                                        g2.rotate(brushRotation / 180.0 * Math.PI, mouseX + 0.5, mouseY + 0.5);
                                    } else {
                                        g2.rotate(brushRotation / 180.0 * Math.PI, mouseX, mouseY);
                                    }
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
            } finally {
                g2.setColor(savedColour);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedAAValue);
                if (savedInterpolationValue != null) {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolationValue);
                }
                g2.setStroke(savedStroke);
                g2.setTransform(savedTransform);
            }
        }
    }
    
    /**
     * Repaint an area in world coordinates, plus a few pixels extra to
     * compensate for sloppiness in painting the brush.
     * 
     * @param area The area to repaint, in world coordinates.
     */
    private void repaintWorld(int x, int y, int width, int height) {
//        System.out.print("Repainting " + x + "," + y + "->" + width + "," + height + " => ");
        Rectangle area = worldToView(x, y, width, height);
//        System.out.println(area.x + "-2," + area.y + "-2->" + area.width + "+4," + area.height + "+4");
        repaint(area.x - 2, area.y - 2, area.width + 4, area.height + 4);
    }

    private void drawGrid(Graphics2D g2, Rectangle clipBounds, float scale) {
        if (dimension == null) {
            return;
        }
//        final int xOffset = MathUtils.mod(getViewX(), gridSize), yOffset = MathUtils.mod(getViewY(), gridSize);
        final int x1 = ((clipBounds.x / gridSize) - 1) * gridSize;
        final int x2 = ((clipBounds.x + clipBounds.width) / gridSize + 1) * gridSize;
        final int y1 = ((clipBounds.y / gridSize) - 1) * gridSize;
        final int y2 = ((clipBounds.y + clipBounds.height) / gridSize + 1) * gridSize;
        g2.setColor(Color.BLACK);
        final float strokeWidth = 1 / scale;
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {2 * strokeWidth, 2 * strokeWidth}, 0.0f));
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
//                if (overlayScale == 1.0f) {
                    // 1:1 scale
                    g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, null);
//                } else {
//                    int width = Math.round(overlay.getWidth() * overlayScale);
//                    int height = Math.round(overlay.getHeight() * overlayScale);
//                    Object savedInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
//                    try {
//                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//                        g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, width, height, null);
//                    } finally {
//                        if (savedInterpolation != null) {
//                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolation);
//                        }
//                    }
//                }
            } finally {
                g2.setComposite(savedComposite);
            }
        } else {
            // Fully opaque
//            if (overlayScale == 1.0f) {
                // 1:1 scale
                g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, null);
//            } else {
//                int width = Math.round(overlay.getWidth() * overlayScale);
//                int height = Math.round(overlay.getHeight() * overlayScale);
//                Object savedInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
//                try {
//                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//                    g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, width, height, null);
//                } finally {
//                    if (savedInterpolation != null) {
//                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolation);
//                    }
//                }
//            }
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
    
    private final HashSet<Layer> hiddenLayers = new HashSet<Layer>();
    private final CustomBiomeManager customBiomeManager;
    private Dimension dimension;
    private int mouseX, mouseY, radius, effectiveRadius, overlayOffsetX, overlayOffsetY, gridSize, contourSeparation, brushRotation;
    private boolean drawRadius, drawGrid, drawOverlay, drawContours, drawViewDistance, drawWalkingDistance;
    private BrushShape brushShape;
//    private float overlayScale = 1.0f;
    private float overlayTransparency = 0.5f;
    private ColourScheme colourScheme;
    private BiomeScheme biomeScheme;
    private BufferedImage overlay;
    private LightOrigin lightOrigin = LightOrigin.NORTHWEST;
    private WPTileProvider tileProvider;
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