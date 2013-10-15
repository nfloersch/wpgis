/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.pepsoft.util.MathUtils;
import org.pepsoft.util.PerlinNoise;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.util.undo.UndoManager;
import org.pepsoft.worldpainter.gardenofeden.Garden;
import org.pepsoft.worldpainter.gardenofeden.Seed;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.DeciduousForest;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.GardenCategory;
import org.pepsoft.worldpainter.layers.Jungle;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.PineForest;
import org.pepsoft.worldpainter.layers.Resources;
import org.pepsoft.worldpainter.layers.SwampLand;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.layers.exporters.ResourcesExporter.ResourcesExporterSettings;

import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.*;

/**
 *
 * @author pepijn
 */
public class Dimension extends InstanceKeeper implements TileProvider, Serializable, Tile.Listener, Cloneable {
    public Dimension(long minecraftSeed, long worldpainterSeed, TileFactory tileFactory, int dim, int maxHeight) {
        this.seed = worldpainterSeed;
        this.minecraftSeed = minecraftSeed;
        this.tileFactory = tileFactory;
        this.dim = dim;
        this.maxHeight = maxHeight;
        if (dim == 0) {
            layerSettings.put(Resources.INSTANCE, new ResourcesExporterSettings(maxHeight));
        }
        topLayerDepthNoise = new PerlinNoise(seed + TOP_LAYER_DEPTH_SEED_OFFSET);
    }

    public World2 getWorld() {
        return world;
    }
    
    void setWorld(World2 world) {
        this.world = world;
    }

    public int getDim() {
        return dim;
    }

    public String getName() {
        switch (dim) {
            case 0:
                return "Surface";
            case 1:
                return "Nether";
            case 2:
                return "End";
            default:
                return "Dimension " + dim;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public long getSeed() {
        return seed;
    }

    public Terrain getSubsurfaceMaterial() {
        return subsurfaceMaterial;
    }

    public void setSubsurfaceMaterial(Terrain subsurfaceMaterial) {
        if (subsurfaceMaterial != this.subsurfaceMaterial) {
            Terrain oldSubsurfaceMaterial = this.subsurfaceMaterial;
            this.subsurfaceMaterial = subsurfaceMaterial;
            dirty = true;
            propertyChangeSupport.firePropertyChange("subsurfaceMaterial", oldSubsurfaceMaterial, subsurfaceMaterial);
        }
    }

    public boolean isPopulate() {
        return populate;
    }

    public void setPopulate(boolean populate) {
        if (populate != this.populate) {
            this.populate = populate;
            dirty = true;
            propertyChangeSupport.firePropertyChange("populate", ! populate, populate);
        }
    }

    public Border getBorder() {
        return border;
    }

    public void setBorder(Border border) {
        if (border != this.border) {
            Border oldBorder = this.border;
            this.border = border;
            dirty = true;
            propertyChangeSupport.firePropertyChange("border", oldBorder, border);
        }
    }

    public int getBorderLevel() {
        return borderLevel;
    }

    public void setBorderLevel(int borderLevel) {
        if (borderLevel != this.borderLevel) {
            int oldBorderLevel = this.borderLevel;
            this.borderLevel = borderLevel;
            dirty = true;
            propertyChangeSupport.firePropertyChange("borderLevel", oldBorderLevel, borderLevel);
        }
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        if (borderSize != this.borderSize) {
            int oldBorderSize = this.borderSize;
            this.borderSize = borderSize;
            dirty = true;
            propertyChangeSupport.firePropertyChange("borderSize", oldBorderSize, borderSize);
        }
    }

    public boolean isDarkLevel() {
        return darkLevel;
    }

    public void setDarkLevel(boolean darkLevel) {
        if (darkLevel != this.darkLevel) {
            this.darkLevel = darkLevel;
            dirty = true;
            propertyChangeSupport.firePropertyChange("darkLevel", ! darkLevel, darkLevel);
        }
    }

    public boolean isBedrockWall() {
        return bedrockWall;
    }

    public void setBedrockWall(boolean bedrockWall) {
        if (bedrockWall != this.bedrockWall) {
            this.bedrockWall = bedrockWall;
            dirty = true;
            propertyChangeSupport.firePropertyChange("bedrockWall", ! bedrockWall, bedrockWall);
        }
    }

    public TileFactory getTileFactory() {
        return tileFactory;
    }

    @Override
    public Tile getTile(int x, int y) {
        return getTile(new Point(x, y));
    }

    public Tile getTile(Point coords) {
        return tiles.get(coords);
    }

    public int getTileCount() {
        return tiles.size();
    }
    
    public Collection<? extends Tile> getTiles() {
        return Collections.unmodifiableCollection(tiles.values());
    }

    public void addTile(Tile tile) {
        if (tile.getMaxHeight() != maxHeight) {
            throw new IllegalArgumentException("Tile has different max height (" + tile.getMaxHeight() + ") than dimension (" + maxHeight + ")");
        }
        int x = tile.getX();
        int y = tile.getY();
        Point key = new Point(x, y);
        if (tiles.containsKey(key)) {
            throw new IllegalStateException("Tile already set");
        }
        tile.addListener(this);
        if (undoManager != null) {
            tile.register(undoManager);
        }
        tiles.put(key, tile);
        if (x < lowestX) {
            lowestX = x;
        }
        if (x > highestX) {
            highestX = x;
        }
        if (y < lowestY) {
            lowestY = y;
        }
        if (y > highestY) {
            highestY = y;
        }
        fireTileAdded(tile);
        dirty = true;
//        biomesCalculated = false;
    }
    
    public void removeTile(Point coords) {
        removeTile(coords, getTile(coords));
    }
    
    public void removeTile(Tile tile) {
        removeTile(new Point(tile.getX(), tile.getY()), tile);
    }
    
    public void removeTile(Point coords, Tile tile) {
        if (! tiles.containsKey(coords)) {
            throw new IllegalStateException("Tile not set");
        }
        tile = tiles.remove(coords);
        if (undoManager != null) {
            tile.unregister();
        }
        tile.removeListener(this);
        // If the tile lies at the edge of the world it's possible the low and
        // high coordinate marks should change; so recalculate them in that case
        if ((coords.x == lowestX) || (coords.x == highestX) || (coords.y == lowestY) || (coords.y == highestY)) {
            lowestX = Integer.MAX_VALUE;
            highestX = Integer.MIN_VALUE;
            lowestY = Integer.MAX_VALUE;
            highestY = Integer.MIN_VALUE;
            for (Tile myTile: tiles.values()) {
                int tileX = myTile.getX(), tileY = myTile.getY();
                if (tileX < lowestX) {
                    lowestX = tileX;
                }
                if (tileX > highestX) {
                    highestX = tileX;
                }
                if (tileY < lowestY) {
                    lowestY = tileY;
                }
                if (tileY > highestY) {
                    highestY = tileY;
                }
            }
        }
        fireTileRemoved(tile);
        dirty = true;
    }

    public int getHighestX() {
        return highestX;
    }

    public int getHighestY() {
        return highestY;
    }

    public int getLowestX() {
        return lowestX;
    }

    public int getLowestY() {
        return lowestY;
    }

    public int getWidth() {
        if (highestX == Integer.MIN_VALUE) {
            return 0;
        } else {
            return highestX - lowestX + 1;
        }
    }

    public int getHeight() {
        if (highestY == Integer.MIN_VALUE) {
            return 0;
        } else {
            return highestY - lowestY + 1;
        }
    }
    
    public final Point getTileCoordinates(int worldX, int worldY) {
        return new Point(worldX >> 7, worldY >> 7);
    }
    
    public final Point getTileCoordinates(Point worldCoords) {
        return getTileCoordinates(worldCoords.x, worldCoords.y);
    }

    public int getIntHeightAt(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getIntHeight(x & COORD_MASK, y & COORD_MASK);
        } else {
            return -1;
        }
    }
    
    public int getIntHeightAt(Point coords) {
        return getIntHeightAt(coords.x, coords.y);
    }

    public float getHeightAt(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getHeight(x & COORD_MASK, y & COORD_MASK);
        } else {
            return Float.MIN_VALUE;
        }
    }

    public float getHeightAt(Point coords) {
        return getHeightAt(coords.x, coords.y);
    }

    public void setHeightAt(int x, int y, float height) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            if (eventsInhibited && (! dirtyTiles.contains(tile))) {
                tile.setEventsInhibited(true);
                dirtyTiles.add(tile);
            }
            tile.setHeight(x & COORD_MASK, y & COORD_MASK, height);
            if (autoUpdateBiomes) {
                updateBiome(tile, x & COORD_MASK, y & COORD_MASK);
            }
        }
    }

    public void setHeightAt(Point coords, int height) {
        setHeightAt(coords.x, coords.y, height);
    }

    public int getRawHeightAt(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getRawHeight(x & COORD_MASK, y & COORD_MASK);
        } else {
            return Integer.MIN_VALUE;
        }
    }
    
    public int getRawHeightAt(Point coords) {
        return getRawHeightAt(coords.x, coords.y);
    }

    public void setRawHeightAt(int x, int y, int rawHeight) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            if (eventsInhibited && (! dirtyTiles.contains(tile))) {
                tile.setEventsInhibited(true);
                dirtyTiles.add(tile);
            }
            tile.setRawHeight(x & COORD_MASK, y & COORD_MASK, rawHeight);
            if (autoUpdateBiomes) {
                updateBiome(tile, x & COORD_MASK, y & COORD_MASK);
            }
        }
    }

    public void setRawHeightAt(Point coords, int rawHeight) {
        setRawHeightAt(coords.x, coords.y, rawHeight);
    }
    
    public Terrain getTerrainAt(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getTerrain(x & COORD_MASK, y & COORD_MASK);
        } else {
            return null;
        }
    }

    public void setTerrainAt(int x, int y, Terrain terrain) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            if (eventsInhibited && (! dirtyTiles.contains(tile))) {
                tile.setEventsInhibited(true);
                dirtyTiles.add(tile);
            }
            tile.setTerrain(x & COORD_MASK, y & COORD_MASK, terrain);
            if (autoUpdateBiomes) {
                updateBiome(tile, x & COORD_MASK, y & COORD_MASK);
            }
        }
    }

    public void setTerrainAt(Point coords, Terrain terrain) {
        setTerrainAt(coords.x, coords.y, terrain);
    }

    public void applyTheme(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            if (eventsInhibited && (! dirtyTiles.contains(tile))) {
                tile.setEventsInhibited(true);
                dirtyTiles.add(tile);
            }
            tileFactory.applyTheme(seed, tile, x & COORD_MASK, y & COORD_MASK);
            if (autoUpdateBiomes) {
                updateBiome(tile, x & COORD_MASK, y & COORD_MASK);
            }
        }
    }

    public int getWaterLevelAt(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getWaterLevel(x & COORD_MASK, y & COORD_MASK);
        } else {
            return Integer.MIN_VALUE;
        }
    }

    public int getWaterLevelAt(Point coords) {
        return getWaterLevelAt(coords.x, coords.y);
    }

    public void setWaterLevelAt(int x, int y, int waterLevel) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            if (eventsInhibited && (! dirtyTiles.contains(tile))) {
                tile.setEventsInhibited(true);
                dirtyTiles.add(tile);
            }
            tile.setWaterLevel(x & COORD_MASK, y & COORD_MASK, waterLevel);
            if (autoUpdateBiomes) {
                updateBiome(tile, x & COORD_MASK, y & COORD_MASK);
            }
        }
    }

    public int getLayerValueAt(Layer layer, int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getLayerValue(layer, x & COORD_MASK, y & COORD_MASK);
        } else {
            return 0;
        }
    }

    public int getLayerValueAt(Layer layer, Point coords) {
        return getLayerValueAt(layer, coords.x, coords.y);
    }

    public void setLayerValueAt(Layer layer, int x, int y, int value) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            if (eventsInhibited && (! dirtyTiles.contains(tile))) {
                tile.setEventsInhibited(true);
                dirtyTiles.add(tile);
            }
            tile.setLayerValue(layer, x & COORD_MASK, y & COORD_MASK, value);
            if (autoUpdateBiomes) {
                updateBiome(tile, x & COORD_MASK, y & COORD_MASK);
            }
        }
    }

    public boolean getBitLayerValueAt(Layer layer, int x, int y) {
        Tile tile = getTile(getTileCoordinates(x, y));
        if (tile != null) {
            return tile.getBitLayerValue(layer, x & COORD_MASK, y & COORD_MASK);
        } else {
            return false;
        }
    }


    public synchronized int getBitLayerCount(Layer layer, int x, int y, int r) {
        if (((x - r) >= 0) && (x + r < TILE_SIZE) && ((y - r) >= 0) && ((y + r) < TILE_SIZE)) {
            // The requested area is completely contained in one tile, optimise
            // by delegating to the tile
            Tile tile = getTile(getTileCoordinates(x, y));
            if (tile != null) {
                return tile.getBitLayerCount(layer, x & COORD_MASK, y & COORD_MASK, r);
            } else {
                return 0;
            }
        } else {
            // The requested area overlaps tile boundaries; do it the slow way
            int count = 0;
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (getBitLayerValueAt(layer, x + dx, y + dy)) {
                        count++;
                    }
                }
            }
            return count;
        }
    }

    /**
     * Get the distance from the specified coordinate to the nearest pixel where
     * the specified layer is <em>not</em> set.
     *
     * @param layer The layer for which to find the distance to the nearest
     *              edge.
     * @param x The X coordinate of the location towards which to determine the distance.
     * @param y The Y coordinate of the location towards which to determine the distance.
     * @param maxDistance The maximum distance to return. If the actual distance is further, this value will be returned.
     * @return The distance from the specified location to the nearest pixel
     *     where the specified layer is not set, or maxDistance, whichever is
     *     smaller.
     */
    public synchronized float getDistanceToEdge(Layer layer, int x, int y, float maxDistance) {
        int r = (int) (maxDistance + 0.999f);
        if (((x - r) >= 0) && (x + r < TILE_SIZE) && ((y - r) >= 0) && ((y + r) < TILE_SIZE)) {
            // The requested area is completely contained in one tile, optimise
            // by delegating to the tile
            Tile tile = getTile(getTileCoordinates(x, y));
            if (tile != null) {
                return tile.getDistanceToEdge(layer, x & COORD_MASK, y & COORD_MASK, maxDistance);
            } else {
                return maxDistance;
            }
        } else {
            float distance = maxDistance;
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (! getBitLayerValueAt(layer, x + dx, y + dy)) {
                        float tDistance = MathUtils.getDistance(dx, dy);
                        if (tDistance < distance) {
                            distance = tDistance;
                        }
                    }
                }
            }
            return distance;
//            for (int i = 1; i <= r; i++) {
//                if ((! getBitLayerValueAt(layer, x - i, y))
//                        || (! getBitLayerValueAt(layer, x + i, y))
//                        || (! getBitLayerValueAt(layer, x, y - i))
//                        || (! getBitLayerValueAt(layer, x, y + i))) {
//                    return Math.min(i, maxDistance);
//                }
//                for (int d = 1; d <= i; d++) {
//                    if ((! getBitLayerValueAt(layer, x - i, y - d))
//                            || (! getBitLayerValueAt(layer, x + d, y - i))
//                            || (! getBitLayerValueAt(layer, x + i, y + d))
//                            || (! getBitLayerValueAt(layer, x - d, y + i))
//                            || ((d < i) && ((! getBitLayerValueAt(layer, x - i, y + d))
//                                || (! getBitLayerValueAt(layer, x - d, y - i))
//                                || (! getBitLayerValueAt(layer, x + i, y - d))
//                                || (! getBitLayerValueAt(layer, x + d, y + i))))) {
//                        return Math.min(MathUtils.getDistance(i, d), maxDistance);
//                    }
//                }
//            }
//            return maxDistance;
        }
    }

    public void setBitLayerValueAt(Layer layer, int x, int y, boolean value) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            if (eventsInhibited && (! dirtyTiles.contains(tile))) {
                tile.setEventsInhibited(true);
                dirtyTiles.add(tile);
            }
            tile.setBitLayerValue(layer, x & COORD_MASK, y & COORD_MASK, value);
            if (autoUpdateBiomes) {
                updateBiome(tile, x & COORD_MASK, y & COORD_MASK);
            }
        }
    }
    
    public void updateBiome(Tile tile, int x, int y) {
        int biome = getAutoBiome(tile, x, y);
        if (biome != -1) {
            tile.setLayerValue(Biome.INSTANCE, x, y, biome);
        }
    }
    
    public void clearLayerData(Layer layer) {
        for (Tile tile: tiles.values()) {
            tile.clearLayerData(layer);
        }
        if (layer.equals(Biome.INSTANCE) && biomesCalculated) {
            biomesCalculated = false;
        }
    }

    public void setEventsInhibited(boolean eventsInhibited) {
        this.eventsInhibited = eventsInhibited;
        if (eventsInhibited == false) {
            for (Tile addedTile: addedTiles) {
                fireTileAdded(addedTile);
            }
            addedTiles.clear();
            for (Tile removedTile: removedTiles) {
                fireTileRemoved(removedTile);
            }
            removedTiles.clear();
            for (Tile dirtyTile: dirtyTiles) {
                dirtyTile.setEventsInhibited(false);
            }
            dirtyTiles.clear();
        }
    }

    public boolean isEventsInhibited() {
        return eventsInhibited;
    }

    public Map<Layer, ExporterSettings> getAllLayerSettings() {
        return Collections.unmodifiableMap(layerSettings);
    }
    
    @SuppressWarnings("unchecked")
    public <L extends Layer> ExporterSettings<L> getLayerSettings(L layer) {
        return layerSettings.get(layer);
    }
    
    public void setLayerSettings(Layer layer, ExporterSettings settings) {
        if ((! layerSettings.containsKey(layer)) || (! settings.equals(layerSettings.get(layer)))) {
            layerSettings.put(layer, settings);
            dirty = true;
        }
    }
    
    public long getMinecraftSeed() {
        return minecraftSeed;
    }

    public void setMinecraftSeed(long minecraftSeed) {
        if (minecraftSeed != this.minecraftSeed) {
            long oldMinecraftSeed = this.minecraftSeed;
            if (undoManager != null) {
                undoManager.clear();
            }
            this.minecraftSeed = minecraftSeed;
            biomesCalculated = false;
            dirty = true;
            propertyChangeSupport.firePropertyChange("minecraftSeed", oldMinecraftSeed, minecraftSeed);
        }
    }

    public File getOverlay() {
        return overlay;
    }

    public void setOverlay(File overlay) {
        if ((overlay != null) ? (! overlay.equals(this.overlay)) : (overlay == null)) {
            File oldOverlay = this.overlay;
            this.overlay = overlay;
            dirty = true;
            propertyChangeSupport.firePropertyChange("overlay", oldOverlay, overlay);
        }
    }

    public int getOverlayOffsetX() {
        return overlayOffsetX;
    }

    public void setOverlayOffsetX(int overlayOffsetX) {
        if (overlayOffsetX != this.overlayOffsetX) {
            int oldOverlayOffsetX = this.overlayOffsetX;
            this.overlayOffsetX = overlayOffsetX;
            dirty = true;
            propertyChangeSupport.firePropertyChange("overlayOffsetX", oldOverlayOffsetX, overlayOffsetX);
        }
    }

    public int getOverlayOffsetY() {
        return overlayOffsetY;
    }

    public void setOverlayOffsetY(int overlayOffsetY) {
        if (overlayOffsetY != this.overlayOffsetY) {
            int oldOverlayOffsetY = this.overlayOffsetY;
            this.overlayOffsetY = overlayOffsetY;
            dirty = true;
            propertyChangeSupport.firePropertyChange("overlayOffsetY", oldOverlayOffsetY, overlayOffsetY);
        }
    }

    public float getOverlayScale() {
        return overlayScale;
    }

    public void setOverlayScale(float overlayScale) {
        if (overlayScale != this.overlayScale) {
            float oldOverlayScale = this.overlayScale;
            this.overlayScale = overlayScale;
            dirty = true;
            propertyChangeSupport.firePropertyChange("overlayScale", oldOverlayScale, overlayScale);
        }
    }

    public float getOverlayTransparency() {
        return overlayTransparency;
    }

    public void setOverlayTransparency(float overlayTransparency) {
        if (overlayTransparency != this.overlayTransparency) {
            float oldOverlayTransparency = this.overlayTransparency;
            this.overlayTransparency = overlayTransparency;
            dirty = true;
            propertyChangeSupport.firePropertyChange("overlayTransparency", oldOverlayTransparency, overlayTransparency);
        }
    }

    public boolean isGridEnabled() {
        return gridEnabled;
    }

    public void setGridEnabled(boolean gridEnabled) {
        if (gridEnabled != this.gridEnabled) {
            this.gridEnabled = gridEnabled;
            dirty = true;
            propertyChangeSupport.firePropertyChange("gridEnabled", ! gridEnabled, gridEnabled);
        }
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        if (gridSize != this.gridSize) {
            int oldGridSize = this.gridSize;
            this.gridSize = gridSize;
            dirty = true;
            propertyChangeSupport.firePropertyChange("gridSize", oldGridSize, gridSize);
        }
    }

    public boolean isOverlayEnabled() {
        return overlayEnabled;
    }

    public void setOverlayEnabled(boolean overlayEnabled) {
        if (overlayEnabled != this.overlayEnabled) {
            this.overlayEnabled = overlayEnabled;
            dirty = true;
            propertyChangeSupport.firePropertyChange("overlayEnabled", ! overlayEnabled, overlayEnabled);
        }
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        if (maxHeight != this.maxHeight) {
            int oldMaxHeight = this.maxHeight;
            this.maxHeight = maxHeight;
            dirty = true;
            propertyChangeSupport.firePropertyChange("maxHeight", oldMaxHeight, maxHeight);
        }
    }

    public boolean isAutoUpdateBiomes() {
        return autoUpdateBiomes;
    }

    public void setAutoUpdateBiomes(boolean autoUpdateBiomes) {
        this.autoUpdateBiomes = autoUpdateBiomes;
    }

    public int getContourSeparation() {
        return contourSeparation;
    }

    public void setContourSeparation(int contourSeparation) {
        if (contourSeparation != this.contourSeparation) {
            int oldContourSeparation = this.contourSeparation;
            this.contourSeparation = contourSeparation;
            dirty = true;
            propertyChangeSupport.firePropertyChange("contourSeparation", oldContourSeparation, contourSeparation);
        }
    }

    public boolean isContoursEnabled() {
        return contoursEnabled;
    }

    public void setContoursEnabled(boolean contoursEnabled) {
        if (contoursEnabled != this.contoursEnabled) {
            this.contoursEnabled = contoursEnabled;
            dirty = true;
            propertyChangeSupport.firePropertyChange("contoursEnabled", ! contoursEnabled, contoursEnabled);
        }
    }

    public int getTopLayerMinDepth() {
        return topLayerMinDepth;
    }

    public void setTopLayerMinDepth(int topLayerMinDepth) {
        if (topLayerMinDepth != this.topLayerMinDepth) {
            int oldTopLayerMinDepth = this.topLayerMinDepth;
            this.topLayerMinDepth = topLayerMinDepth;
            dirty = true;
            propertyChangeSupport.firePropertyChange("topLayerMinDepth", oldTopLayerMinDepth, topLayerMinDepth);
        }
    }

    public int getTopLayerVariation() {
        return topLayerVariation;
    }

    public void setTopLayerVariation(int topLayerVariation) {
        this.topLayerVariation = topLayerVariation;
        if (topLayerVariation != this.topLayerVariation) {
            int oldTopLayerVariation = this.topLayerVariation;
            this.topLayerVariation = topLayerVariation;
            dirty = true;
            propertyChangeSupport.firePropertyChange("topLayerVariation", oldTopLayerVariation, topLayerVariation);
        }
    }

    public boolean isBottomless() {
        return bottomless;
    }

    public void setBottomless(boolean bottomless) {
        if (bottomless != this.bottomless) {
            this.bottomless = bottomless;
            dirty = true;
            propertyChangeSupport.firePropertyChange("bottomless", ! bottomless, bottomless);
        }
    }

    public Point getLastViewPosition() {
        return lastViewPosition;
    }

    public void setLastViewPosition(Point lastViewPosition) {
        if (lastViewPosition == null) {
            throw new NullPointerException();
        }
        if (! lastViewPosition.equals(this.lastViewPosition)) {
            Point oldLastViewPosition = this.lastViewPosition;
            this.lastViewPosition = lastViewPosition;
            // Don't mark dirty just for changing the view position
            propertyChangeSupport.firePropertyChange("lastViewPosition", oldLastViewPosition, lastViewPosition);
        }
    }

    public Garden getGarden() {
        return garden;
    }
    
    public Set<Layer> getAllLayers() {
        Set<Layer> allLayers = new HashSet<Layer>();
        for (Tile tile: tiles.values()) {
            allLayers.addAll(tile.getLayers());
        }
        return allLayers;
    }
    
    /**
     * Get the set of layers that has been configured to be applied everywhere.
     * 
     * @return The set of layers that has been configured to be applied
     *     everywhere.
     */
    public Set<Layer> getMinimumLayers() {
        Set<Layer> layers = new HashSet<Layer> ();
        for (ExporterSettings settings: layerSettings.values()) {
            if (settings.isApplyEverywhere()) {
                layers.add(settings.getLayer());
            }
        }
        return layers;
    }

    public void applyTheme(Point coords) {
        applyTheme(coords.x, coords.y);
    }

    public void register(UndoManager undoManager) {
        this.undoManager = undoManager;
        for (Tile tile: tiles.values()) {
            tile.register(undoManager);
        }
//        garden.register(undoManager);
    }
    
    public boolean undoIfDirty() {
        if ((undoManager != null) && undoManager.isDirty()) {
            return undoManager.undo();
        } else {
            return false;
        }
    }
    
    public void clearUndo() {
        if (undoManager != null) {
            undoManager.clear();
        }
    }

    public void armSavePoint() {
        if (undoManager != null) {
            undoManager.armSavePoint();
        }
    }
    
    public void clearRedo() {
        if (undoManager != null) {
            undoManager.clearRedo();
        }
    }

    public void unregister() {
        for (Tile tile: tiles.values()) {
            tile.removeListener(this);
            tile.unregister();
        }
        undoManager = null;
    }

    public boolean isBiomesCalculated() {
        return biomesCalculated;
    }

    public void recalculateBiomes(final BiomeScheme biomeScheme, Window parent) {
        if (dim != 0) {
            // Biomes don't apply to the Nether or the End
            return;
        }
        logger.info("Recalculating biomes for " + tiles.size() + " tiles");
//        System.out.println("Recalculating biomes...");
//        long start = System.currentTimeMillis();
        // Merely recalculating the biomes should not cause the dimension to
        // become dirty
        boolean wasDirty = dirty;
        biomeScheme.setSeed(minecraftSeed);
        setEventsInhibited(true);
        try {
            if (tiles.size() > 100) {
                ProgressDialog.executeTask(parent, new ProgressTask<Map<Point, Tile>>() {
                    @Override
                    public String getName() {
                        return "Recalculating biomes";
                    }

                    @Override
                    public Map<Point, Tile> execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                        int totalTileCount = tiles.size(), tileCount = 0;
                        for (Tile tile: tiles.values()) {
                            recalculateBiomes(tile, biomeScheme);
                            tileCount++;
                            progressReceiver.setProgress((float) tileCount / totalTileCount);
                        }
                        return tiles;
                    }
                }, false);
            } else {
                for (Tile tile: tiles.values()) {
                    recalculateBiomes(tile, biomeScheme);
                }
            }
        } finally {
            setEventsInhibited(false);
        }
        biomesCalculated = true;
        dirty = wasDirty;
//        System.out.println("Recalculating biomes took " + (System.currentTimeMillis() - start) + "ms");
    }
    
    public void recalculateBiomes(Tile tile, BiomeScheme biomeScheme) {
        int[] biomes = biomeScheme.getBiomes(tile.getX() * TILE_SIZE, tile.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        if (eventsInhibited) {
            dirtyTiles.add(tile);
        }
        tile.setEventsInhibited(true);
        try {
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int y = 0; y < TILE_SIZE; y++) {
                    tile.setLayerValue(Biome.INSTANCE, x, y, biomes[x + y * TILE_SIZE]);
                }
            }
        } finally {
            if (! eventsInhibited) {
                tile.setEventsInhibited(false);
            }
        }
    }

    public final int getAutoBiome(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return getAutoBiome(tile, x & COORD_MASK, y & COORD_MASK);
        } else {
            return -1;
        }
    }
    
    public final int getAutoBiome(Tile tile, int x, int y) {
        int biome;
        if (tile.getBitLayerValue(Frost.INSTANCE, x, y)) {
            if ((tile.getLayerValue(DeciduousForest.INSTANCE, x, y) > 0)
                    || (tile.getLayerValue(PineForest.INSTANCE, x, y) > 0)
                    || (tile.getLayerValue(SwampLand.INSTANCE, x, y) > 0)
                    || (tile.getLayerValue(Jungle.INSTANCE, x, y) > 0)) {
                biome = BIOME_TAIGA;
            } else if (tile.getTerrain(x, y) == Terrain.WATER) {
                biome = BIOME_FROZEN_RIVER;
            } else {
                int waterLevel = tile.getWaterLevel(x, y) - tile.getIntHeight(x, y);
                if ((waterLevel > 0) && (! tile.getBitLayerValue(FloodWithLava.INSTANCE, x, y))) {
                    if (waterLevel <= 5) {
                        biome = BIOME_FROZEN_RIVER;
                    } else {
                        biome = BIOME_FROZEN_OCEAN;
                    }
                } else {
                    biome = BIOME_ICE_PLAINS;
                }
            }
        } else {
            if (tile.getLayerValue(SwampLand.INSTANCE, x, y) > 0) {
                biome = BIOME_SWAMPLAND;
            } else if (tile.getLayerValue(Jungle.INSTANCE, x, y) > 0) {
                biome = BIOME_JUNGLE;
            } else {
                int waterLevel = tile.getWaterLevel(x, y) - tile.getIntHeight(x, y);
                if ((waterLevel > 0) && (! tile.getBitLayerValue(FloodWithLava.INSTANCE, x, y))) {
                    if (waterLevel <= 5) {
                        biome = BIOME_RIVER;
                    } else {
                        biome = BIOME_OCEAN;
                    }
                } else if ((tile.getLayerValue(DeciduousForest.INSTANCE, x, y) > 0) || (tile.getLayerValue(PineForest.INSTANCE, x, y) > 0)) {
                    biome = BIOME_FOREST;
                } else {
                    biome = tile.getTerrain(x, y).getDefaultBiome();
                }
            }
        }
        return biome;
    }
    
    /**
     * Get a snapshot of the current state of this dimension. If you want this
     * snapshot to be truly static, you must execute a savepoint on the undo
     * manager after this.
     * 
     * @return A snapshot of the current state of this dimension.
     */
    public Dimension getSnapshot() {
        if (undoManager == null) {
            throw new IllegalStateException("No undo manager installed");
        }
        return new DimensionSnapshot(this, undoManager.getSnapshot());
    }
    
    public int getTopLayerDepth(int x, int y, int z) {
        return topLayerMinDepth + Math.round((topLayerDepthNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) + 0.5f) * topLayerVariation);
    }

    void ensureAllReadable() {
        for (Tile tile: tiles.values()) {
            tile.ensureAllReadable();
        }
    }
    
    public void addDimensionListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeDimensionListener(Listener listener) {
        listeners.remove(listener);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
    
    public void rotate(CoordinateTransform rotation, ProgressReceiver progressReceiver) throws OperationCancelled {
        if (progressReceiver != null) {
            progressReceiver.setMessage("rotating " + getName() + "...");
        }
        unregister();
        eventsInhibited = true;
        try {
            Rectangle overlayCoords = null;
            if ((overlay != null) && overlay.canRead()) {
                try {
                    java.awt.Dimension overlaySize = getImageSize(overlay);
                    overlayCoords = new Rectangle(overlayOffsetX + (lowestX << TILE_SIZE_BITS), overlayOffsetY + (lowestY << TILE_SIZE_BITS), Math.round(overlaySize.width * overlayScale), Math.round(overlaySize.height * overlayScale));
                } catch (IOException e) {
                    // Don't bother user with it, just clear the overlay
                    logger.log(Level.SEVERE, "I/O error while trying to determine size of " + overlay, e);
                    overlay = null;
                    overlayEnabled = false;
                    overlayOffsetX = 0;
                    overlayOffsetY = 0;
                    overlayScale = 1.0f;
                }
            } else {
                overlay = null;
                overlayEnabled = false;
                overlayOffsetX = 0;
                overlayOffsetY = 0;
                overlayScale = 1.0f;
            }
            
            Map<Point, Tile> oldTiles = tiles;
            tiles = new HashMap<Point, Tile>();
            lowestX = Integer.MAX_VALUE;
            highestX = Integer.MIN_VALUE;
            lowestY = Integer.MAX_VALUE;
            highestY = Integer.MIN_VALUE;
            int tileCount = oldTiles.size(), tileNo = 0;
            for (Tile tile: oldTiles.values()) {
                addTile(tile.rotate(rotation));
                tileNo++;
                if (progressReceiver != null) {
                    progressReceiver.setProgress((float) tileNo / tileCount);
                }
            }
            
            if (overlayCoords != null) {
                overlayCoords = rotation.transform(overlayCoords);
                overlayOffsetX = overlayCoords.x - (lowestX << TILE_SIZE_BITS);
                overlayOffsetY = overlayCoords.y - (lowestY << TILE_SIZE_BITS);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, "The " + getName() + " dimension has an overlay image!\n"
                            + "The coordinates have been adjusted for you,\n"
                            + "but you need to rotate the actual image yourself\n"
                            + "using a paint program.", "Adjust Overlay Image", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        } finally {
            eventsInhibited = false;
            addedTiles.clear();
            removedTiles.clear();
            dirtyTiles.clear();
        }
    }
    
    // Tile.Listener

    @Override
    public void heightMapChanged(Tile tile) {
        dirty = true;
    }

    @Override
    public void terrainChanged(Tile tile) {
        dirty = true;
    }

    @Override
    public void waterLevelChanged(Tile tile) {
        dirty = true;
    }

    @Override
    public void seedsChanged(Tile tile) {
        dirty = true;
    }

    @Override
    public void layerDataChanged(Tile tile, Set<Layer> changedLayers) {
        dirty = true;
    }

    @Override
    public void allBitLayerDataChanged(Tile tile) {
        dirty = true;
    }

    @Override
    public void allNonBitlayerDataChanged(Tile tile) {
        dirty = true;
    }
    
    private void fireTileAdded(Tile tile) {
        if (eventsInhibited) {
            addedTiles.add(tile);
        } else {
            for (Listener listener: listeners) {
                listener.tileAdded(this, tile);
            }
        }
    }

    private void fireTileRemoved(Tile tile) {
        if (eventsInhibited) {
            removedTiles.add(tile);
        } else {
            for (Listener listener: listeners) {
                listener.tileRemoved(this, tile);
            }
        }
    }
    
    private java.awt.Dimension getImageSize(File image) throws IOException {
        String filename = image.getName();
        int p = filename.lastIndexOf('.');
        if (p == -1) {
            return null;
        }
        String suffix = filename.substring(p + 1).toLowerCase();
        for (Iterator<ImageReader> i = ImageIO.getImageReadersBySuffix(suffix); i.hasNext();) {
            ImageReader reader = i.next();
            try {
                ImageInputStream in = new FileImageInputStream(image);
                try {
                    reader.setInput(in);
                    int width = reader.getWidth(reader.getMinIndex());
                    int height = reader.getHeight(reader.getMinIndex());
                    return new java.awt.Dimension(width, height);
                } finally {
                    in.close();
                }
            } finally {
                reader.dispose();
            }
        }
        return null;
    }

//    private void writeObject(ObjectOutputStream out) throws IOException {
//        Map<Point, Tile> tmpTiles = tiles;
//        tiles = null;
//        try {
//            out.defaultWriteObject();
//            out.writeInt(tmpTiles.size());
//            // Write out the tiles from the inside to the outside; so that if
//            // the file gets corrupted the tiles nearest the centre are the most
//            // likely to be recoverable
//            List<Tile> sortedTiles = new ArrayList<Tile>(tmpTiles.values());
//            final int x0 = (lowestX + highestX) / 2, y0 = (lowestY + highestY) / 2;
//            Collections.sort(sortedTiles, new Comparator<Tile>() {
//                @Override
//                public int compare(Tile t1, Tile t2) {
//                    float d1 = MathUtils.getDistance(t1.getX(), t1.getY(), x0, y0);
//                    float d2 = MathUtils.getDistance(t2.getX(), t2.getY(), x0, y0);
//                    if (d1 < d2) {
//                        return -1;
//                    } else if (d1 > d2) {
//                        return 1;
//                    } else {
//                        return 0;
//                    }
//                }
//            });
//            for (Tile tile: sortedTiles) {
//                out.writeObject(tile);
//            }
//        } finally {
//            tiles = tmpTiles;
//        }
//    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
//        if (tiles == null) {
//            tiles = new HashMap<Point, Tile>();
//            int tileCount = in.readInt();
//            for (int i = 0; i < tileCount; i++) {
//                Tile tile = (Tile) in.readObject();
//                tiles.put(new Point(tile.getX(), tile.getY()), tile);
//            }
//        }
        init();
    }
    
    private void init() {
        listeners = new ArrayList<Listener>();
        dirtyTiles = new HashSet<Tile>();
        addedTiles = new HashSet<Tile>();
        removedTiles = new HashSet<Tile>();
        propertyChangeSupport = new PropertyChangeSupport(this);
        garden = new WPGarden();
        topLayerDepthNoise = new PerlinNoise(seed + TOP_LAYER_DEPTH_SEED_OFFSET);

        for (Tile tile: tiles.values()) {
            tile.addListener(this);
            for (Seed gardenSeed: tile.getSeeds()) {
                gardenSeed.garden = garden;
            }
        }
        
        // Legacy support
        if (borderSize == 0) {
            borderSize = 2;
        }
        if (overlayScale == 0.0f) {
            overlayScale = 1.0f;
        }
        if (overlayTransparency == 0.0f) {
            overlayTransparency = 0.5f;
        }
        if (gridSize == 0) {
            gridSize = 128;
        }
        if (! biomesConverted) {
            // Convert the nibble sized biomes data from a legacy map (by
            // deleting it so that it will be recalculated
            for (Tile tile: tiles.values()) {
                tile.clearLayerData(Biome.INSTANCE);
            }
            biomesConverted = true;
            biomesCalculated = false;
        }
        if (maxHeight == 0) {
            maxHeight = 128;
        }
        if (subsurfaceMaterial == Terrain.RESOURCES) {
            subsurfaceMaterial = Terrain.STONE;
            
            // Load legacy settings
            ResourcesExporterSettings settings = new ResourcesExporterSettings(maxHeight);
            settings.setChance(BLK_GOLD_ORE,         1);
            settings.setChance(BLK_IRON_ORE,         5);
            settings.setChance(BLK_COAL,             9);
            settings.setChance(BLK_LAPIS_LAZULI_ORE, 1);
            settings.setChance(BLK_DIAMOND_ORE,      1);
            settings.setChance(BLK_REDSTONE_ORE,     6);
            settings.setChance(BLK_WATER,            1);
            settings.setChance(BLK_LAVA,             1);
            settings.setChance(BLK_DIRT,             9);
            settings.setChance(BLK_GRAVEL,           9);
            settings.setMaxLevel(BLK_GOLD_ORE,         Terrain.GOLD_LEVEL);
            settings.setMaxLevel(BLK_IRON_ORE,         Terrain.IRON_LEVEL);
            settings.setMaxLevel(BLK_COAL,             Terrain.COAL_LEVEL);
            settings.setMaxLevel(BLK_LAPIS_LAZULI_ORE, Terrain.LAPIS_LAZULI_LEVEL);
            settings.setMaxLevel(BLK_DIAMOND_ORE,      Terrain.DIAMOND_LEVEL);
            settings.setMaxLevel(BLK_REDSTONE_ORE,     Terrain.REDSTONE_LEVEL);
            settings.setMaxLevel(BLK_WATER,            Terrain.WATER_LEVEL);
            settings.setMaxLevel(BLK_LAVA,             Terrain.LAVA_LEVEL);
            settings.setMaxLevel(BLK_DIRT,             Terrain.DIRT_LEVEL);
            settings.setMaxLevel(BLK_GRAVEL,           Terrain.GRAVEL_LEVEL);
            
            layerSettings.put(Resources.INSTANCE, settings);
        }
        if (contourSeparation == 0) {
            contourSeparation = 10;
        }
        if (topLayerMinDepth == 0) {
            topLayerMinDepth = 3;
            topLayerVariation = 4;
        }
        if (lastViewPosition == null) {
            lastViewPosition = new Point();
        }
    }
    
    private World2 world;
    private final long seed;
    private final int dim;
    private Map<Point, Tile> tiles = new HashMap<Point, Tile>();
    private final TileFactory tileFactory;
    private int lowestX = Integer.MAX_VALUE, highestX = Integer.MIN_VALUE, lowestY = Integer.MAX_VALUE, highestY = Integer.MIN_VALUE;
    private Terrain subsurfaceMaterial = Terrain.STONE;
    private boolean populate;
    private Border border;
    private int borderLevel = 62, borderSize = 2;
    private boolean darkLevel, bedrockWall;
    private Map<Layer, ExporterSettings> layerSettings = new HashMap<Layer, ExporterSettings>();
    private long minecraftSeed = Long.MIN_VALUE;
    private boolean biomesCalculated;
    private File overlay;
    private float overlayScale = 1.0f, overlayTransparency = 0.5f;
    private int overlayOffsetX, overlayOffsetY, gridSize = 128;
    private boolean overlayEnabled, gridEnabled, biomesConverted = true;
    private int maxHeight = World2.DEFAULT_MAX_HEIGHT, contourSeparation = 10;
    private boolean contoursEnabled = true;
    private int topLayerMinDepth = 3, topLayerVariation = 4;
    private boolean bottomless;
    private Point lastViewPosition = new Point();
    private transient List<Listener> listeners = new ArrayList<Listener>();
    private transient boolean eventsInhibited;
    private transient Set<Tile> dirtyTiles = new HashSet<Tile>();
    private transient Set<Tile> addedTiles = new HashSet<Tile>();
    private transient Set<Tile> removedTiles = new HashSet<Tile>();
    private transient boolean dirty;
    private transient UndoManager undoManager;
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private transient WPGarden garden = new WPGarden();
    private transient boolean autoUpdateBiomes;
    private transient PerlinNoise topLayerDepthNoise;

    private static final int COORD_MASK = TILE_SIZE - 1;
    private static final long TOP_LAYER_DEPTH_SEED_OFFSET = 180728193;
    private static final int DEFAULT_BIOME = BIOME_PLAINS;
    private static final Logger logger = Logger.getLogger(Dimension.class.getName());
    private static final long serialVersionUID = 2011062401L;

    public interface Listener {
        void tileAdded(Dimension dimension, Tile tile);
        void tileRemoved(Dimension dimension, Tile tile);
    }

    public enum Border {VOID, WATER, LAVA}
    
    private class WPGarden implements Garden {
        @Override
        public void clearLayer(int x, int y, Layer layer, int radius) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    setLayerValueAt(layer, x + dx, y + dy, 0);
                }
            }
        }
        
        @Override
        public void setCategory(int x, int y, int category) {
            setLayerValueAt(GardenCategory.INSTANCE, x, y, category);
        }

        @Override
        public int getCategory(int x, int y) {
            return getLayerValueAt(GardenCategory.INSTANCE, x, y);
        }

        @Override
        public Set<Seed> getSeeds() {
            Set<Seed> allSeeds = new HashSet<Seed>();
            for (Tile tile: tiles.values()) {
                allSeeds.addAll(tile.getSeeds());
            }
            return allSeeds;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends Seed> List<T> findSeeds(Class<T> type, int x, int y, int radius) {
            List<T> seedsFound = new ArrayList<T>();
            int topLeftTileX = (x - radius) >> 7;
            int topLeftTileY = (y - radius) >> 7;
            int bottomRightTileX = (x + radius) >> 7;
            int bottomRightTileY = (y + radius) >> 7;
//            System.out.println("Finding seeds of type " + type.getSimpleName() + " " + radius + " blocks around " + x + "," + y + " in " + ((bottomRightTileX - topLeftTileX + 1) * (bottomRightTileY - topLeftTileY + 1)) + " tiles");
            for (int tileX = topLeftTileX; tileX <= bottomRightTileX; tileX++) {
                for (int tileY = topLeftTileY; tileY <= bottomRightTileY; tileY++) {
                    Tile tile = getTile(tileX, tileY);
                    if (tile != null) {
                        for (Seed seed: tile.getSeeds()) {
                            if (seed.getClass() == type) {
                                int distance = (int) MathUtils.getDistance(seed.location.x - x, seed.location.y - y);
                                if (distance <= radius) {
                                    seedsFound.add((T) seed);
                                }
                            }
                        }
                    }
                }
            }
            return seedsFound;
        }

        @Override
        public boolean isOccupied(int x, int y) {
            return (getLayerValueAt(GardenCategory.INSTANCE, x, y) != 0) || (getWaterLevelAt(x, y) > getIntHeightAt(x, y));
        }

        @Override
        public void plantSeed(Seed seed) {
            Point location = seed.getLocation();
            if ((location.x < lowestX * TILE_SIZE) || (location.x > (highestX + 1) * TILE_SIZE - 1) || (location.y < lowestY * TILE_SIZE) || (location.y > (highestY + 1) * TILE_SIZE - 1)) {
                return;
            }
            Point tileCoords = new Point(location.x >> 7, location.y >> 7);
            Tile tile = getTile(tileCoords);
            if (tile != null) {
                tile.plantSeed(seed);
                activeTiles.add(tileCoords);
            }
        }

        @Override
        public void removeSeed(Seed seed) {
            Point location = seed.getLocation();
            if ((location.x < lowestX * TILE_SIZE) || (location.x > (highestX + 1) * TILE_SIZE - 1) || (location.y < lowestY * TILE_SIZE) || (location.y > (highestY + 1) * TILE_SIZE - 1)) {
                return;
            }
            Tile tile = getTile(location.x >> 7, location.y >> 7);
            if (tile != null) {
                tile.removeSeed(seed);
            }
        }

        @Override
        public float getHeight(int x, int y) {
            return getHeightAt(x, y);
        }

        @Override
        public int getIntHeight(int x, int y) {
            return getIntHeightAt(x, y);
        }
        
        @Override
        @SuppressWarnings("unchecked") // Guaranteed by Java
        public boolean tick() {
            // Tick all seeds in active tiles. Clone the active tiles set, and
            // the seed sets from the tiles, because they may change out from
            // under us
            for (Point tileCoords: (HashSet<Point>) activeTiles.clone()) {
                Tile tile = getTile(tileCoords);
                if (tile != null) {
                    for (Seed seed: (HashSet<Seed>) tile.getSeeds().clone()) {
                        seed.tick();
                    }
                }
            }
            // Don't cache active seeds, because they might have changed
            // Groom active tile list, and determine whether all seeds are
            // finished (have either sprouted or died)
            boolean finished = true;
            for (Iterator<Point> i = activeTiles.iterator(); i.hasNext(); ) {
                Point tileCoords = i.next();
                Tile tile = getTile(tileCoords);
                boolean tileFinished = true;
                if (tile != null) {
                    for (Seed seed: tile.getSeeds()) {
                        if (! seed.isFinished()) {
                            tileFinished = false;
                            break;
                        }
                    }
                }
                if (tileFinished) {
                    i.remove();
                } else {
                    finished = false;
                }
            }
            return finished;
        }

        @Override
        public void neutralise() {
            for (Point tileCoords: activeTiles) {
                Tile tile = getTile(tileCoords);
                if (tile != null) {
                    for (Seed seed: tile.getSeeds()) {
                        if (! seed.isFinished()) {
                            seed.neutralise();
                        }
                    }
                }
            }
            activeTiles.clear();
        }
        
        private final HashSet<Point> activeTiles = new HashSet<Point>();
    }
}