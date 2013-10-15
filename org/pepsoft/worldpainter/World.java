/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import java.awt.Point;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.v1_7_3.BiomeGenerator;
import org.pepsoft.util.undo.UndoManager;
import org.pepsoft.worldpainter.layers.Biome;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.layers.*;
import org.pepsoft.worldpainter.layers.exporters.CavernsExporter.CavernsSettings;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.layers.exporters.FrostExporter.FrostSettings;
import org.pepsoft.worldpainter.layers.exporters.TreesExporter.TreeLayerSettings;

/**
 * Superseded by {@link World2}. Don't use any more!
 * 
 * @author pepijn
 */
@Deprecated
public class World implements TileProvider, Serializable, Tile.Listener {
    public World(long seed, TileFactory tileFactory) {
        this.seed = seed;
        minecraftSeed = seed;
        this.tileFactory = tileFactory;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (! (name.equals(this.name))) {
            String oldName = this.name;
            this.name = name;
            dirty = true;
            propertyChangeSupport.firePropertyChange("name", oldName, name);
        }
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

    public boolean isCreateGoodiesChest() {
        return createGoodiesChest;
    }

    public void setCreateGoodiesChest(boolean createGoodiesChest) {
        if (createGoodiesChest != this.createGoodiesChest) {
            this.createGoodiesChest = createGoodiesChest;
            dirty = true;
            propertyChangeSupport.firePropertyChange("createGoodiesChest", ! createGoodiesChest, createGoodiesChest);
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
        return tiles.get(new Point(x, y));
    }

    public Tile getTile(Point coords) {
        return getTile(coords.x, coords.y);
    }

    public Collection<Tile> getTiles() {
        return new ArrayList<Tile>(tiles.values());
    }

    public void addTile(Tile tile) {
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
        BiomeGenerator biomeGenerator = new BiomeGenerator(minecraftSeed);
        recalculateBiomes(tile, biomeGenerator);
        fireTileAdded(tile);
        dirty = true;
    }
    
    public void removeTile(Tile tile) {
        int x = tile.getX();
        int y = tile.getY();
        Point key = new Point(x, y);
        if (! tiles.containsKey(key)) {
            throw new IllegalStateException("Tile not set");
        }
        tile = tiles.remove(key);
        if (undoManager != null) {
            tile.unregister();
        }
        tile.removeListener(this);
        // If the tile lies at the edge of the world it's possible the low and
        // high coordinate marks should change; so recalculate them in that case
        if ((x == lowestX) || (x == highestX) || (y == lowestY) || (y == highestY)) {
            lowestX = Integer.MAX_VALUE;
            highestY = Integer.MIN_VALUE;
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
    
    public Point getTileCoordinates(int worldX, int worldY) {
        int tileX = (int) Math.floor((double) worldX / TILE_SIZE);
        int tileY = (int) Math.floor((double) worldY / TILE_SIZE);
        return new Point(tileX, tileY);
    }
    
    public Point getTileCoordinates(Point worldCoords) {
        return getTileCoordinates(worldCoords.x, worldCoords.y);
    }

    public float getHeightAt(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getHeight(x & (TILE_SIZE - 1), y & (TILE_SIZE - 1));
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
            tile.setHeight(x & (TILE_SIZE - 1), y & (TILE_SIZE - 1), height);
        }
    }

    public void setHeightAt(Point coords, int height) {
        setHeightAt(coords.x, coords.y, height);
    }

    public Terrain getTerrainAt(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getTerrain(x & (TILE_SIZE - 1), y & (TILE_SIZE - 1));
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
            tile.setTerrain(x & (TILE_SIZE - 1), y & (TILE_SIZE - 1), terrain);
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
            tileFactory.applyTheme(seed, tile, x & (TILE_SIZE - 1), y & (TILE_SIZE - 1));
        }
    }

    public int getWaterLevelAt(int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getWaterLevel(x & (TILE_SIZE - 1), y & (TILE_SIZE - 1));
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
            tile.setWaterLevel(x & (TILE_SIZE - 1), y & (TILE_SIZE - 1), waterLevel);
        }
    }

    public int getLayerValueAt(Layer layer, int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getLayerValue(layer, x & (TILE_SIZE - 1), y & (TILE_SIZE - 1));
        } else {
            return 0;
        }
    }

    public void setLayerValueAt(Layer layer, int x, int y, int value) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            if (eventsInhibited && (! dirtyTiles.contains(tile))) {
                tile.setEventsInhibited(true);
                dirtyTiles.add(tile);
            }
            tile.setLayerValue(layer, x & (TILE_SIZE - 1), y & (TILE_SIZE - 1), value);
        }
    }

    public boolean getBitLayerValueAt(Layer layer, int x, int y) {
        Point tileCoords = getTileCoordinates(x, y);
        Tile tile = getTile(tileCoords);
        if (tile != null) {
            return tile.getBitLayerValue(layer, x & (TILE_SIZE - 1), y & (TILE_SIZE - 1));
        } else {
            return false;
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
            tile.setBitLayerValue(layer, x & (TILE_SIZE - 1), y & (TILE_SIZE - 1), value);
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

    public Point getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Point spawnPoint) {
        if (! spawnPoint.equals(this.spawnPoint)) {
            Point oldSpawnPoint = this.spawnPoint;
            this.spawnPoint = spawnPoint;
            dirty = true;
            propertyChangeSupport.firePropertyChange("spawnPoint", oldSpawnPoint, spawnPoint);
        }
    }

    public File getImportedFrom() {
        return importedFrom;
    }

    public void setImportedFrom(File importedFrom) {
        if ((importedFrom == null) ? (this.importedFrom != null) : (! importedFrom.equals(this.importedFrom))) {
            File oldImportedFrom = this.importedFrom;
            this.importedFrom = importedFrom;
            propertyChangeSupport.firePropertyChange("importedFrom", oldImportedFrom, importedFrom);
        }
    }

    public Map<Layer, ExporterSettings> getAllLayerSettings() {
        return Collections.unmodifiableMap(layerSettings);
    }
    
    public ExporterSettings getLayerSettings(Layer layer) {
        return layerSettings.get(layer);
    }
    
    public void setLayerSettings(Layer layer, ExporterSettings settings) {
        if ((! layerSettings.containsKey(layer)) || (! settings.equals(layerSettings.get(layer)))) {
            layerSettings.put(layer, settings);
            dirty = true;
        }
    }
    
    /**
     * Get the set of layers that has been configured to be applied everywhere.
     * 
     * @return The set of layers that has been configured to be applied
     *     everywhere.
     */
    @SuppressWarnings("unchecked")
    public Set<Layer> getMinimumLayers() {
        // TODO: solve this more elegantly
        Set<Layer> layers = new HashSet<Layer> ();
        if ((getLayerSettings(Caverns.INSTANCE) != null) && (((CavernsSettings) getLayerSettings(Caverns.INSTANCE)).getCavernsEverywhereLevel() > 0)) {
            layers.add(Caverns.INSTANCE);
        }
        if ((getLayerSettings(DeciduousForest.INSTANCE) != null) && (((TreeLayerSettings<DeciduousForest>) getLayerSettings(DeciduousForest.INSTANCE)).isApplyEverywhere())) {
            layers.add(DeciduousForest.INSTANCE);
        }
        if ((getLayerSettings(PineForest.INSTANCE) != null) && (((TreeLayerSettings<PineForest>) getLayerSettings(PineForest.INSTANCE)).isApplyEverywhere())) {
            layers.add(DeciduousForest.INSTANCE);
        }
        if ((getLayerSettings(SwampLand.INSTANCE) != null) && (((TreeLayerSettings<SwampLand>) getLayerSettings(SwampLand.INSTANCE)).isApplyEverywhere())) {
            layers.add(DeciduousForest.INSTANCE);
        }
        if ((getLayerSettings(Jungle.INSTANCE) != null) && (((TreeLayerSettings<Jungle>) getLayerSettings(Jungle.INSTANCE)).isApplyEverywhere())) {
            layers.add(DeciduousForest.INSTANCE);
        }
        if ((getLayerSettings(Frost.INSTANCE) != null) && ((FrostSettings) getLayerSettings(Frost.INSTANCE)).isFrostEverywhere()) {
            layers.add(Frost.INSTANCE);
        }
        return layers;
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
            recalculateBiomes();
            propertyChangeSupport.firePropertyChange("minecraftSeed", oldMinecraftSeed, minecraftSeed);
        }
    }

    public void applyTheme(Point coords) {
        applyTheme(coords.x, coords.y);
    }

    public void register(UndoManager undoManager) {
        this.undoManager = undoManager;
        for (Tile tile: tiles.values()) {
            tile.register(undoManager);
        }
    }

    public void destroy() {
        for (Tile tile: tiles.values()) {
            tile.removeListener(this);
            tile.unregister();
        }
    }

    public void addWorldListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeWorldListener(Listener listener) {
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
    
    private void recalculateBiomes() {
        BiomeGenerator biomeGenerator = new BiomeGenerator(minecraftSeed);
        for (Tile tile: tiles.values()) {
            recalculateBiomes(tile, biomeGenerator);
        }
    }
    
    private void recalculateBiomes(Tile tile, BiomeGenerator biomeGenerator) {
        int[] biomes = biomeGenerator.getBiomes(null, tile.getX() << TILE_SIZE_BITS, tile.getY() << TILE_SIZE_BITS, TILE_SIZE, TILE_SIZE);
        tile.setEventsInhibited(true);
        try {
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int y = 0; y < TILE_SIZE; y++) {
                    tile.setLayerValue(Biome.INSTANCE, TILE_SIZE - y - 1, x, biomes[x * TILE_SIZE + y]);
                }
            }
        } finally {
            tile.setEventsInhibited(false);
        }
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new ArrayList<Listener>();
        dirtyTiles = new HashSet<Tile>();
        addedTiles = new HashSet<Tile>();
        removedTiles = new HashSet<Tile>();
        propertyChangeSupport = new PropertyChangeSupport(this);

        for (Tile tile: tiles.values()) {
            tile.addListener(this);
        }

        // Legacy maps
        if (subsurfaceMaterial == null) {
            subsurfaceMaterial = Terrain.STONE;
            createGoodiesChest = true;
            borderLevel = 4;
        }
        if (spawnPoint == null) {
            spawnPoint = new Point(15, 0);
        }
        if (layerSettings == null) {
            layerSettings = new HashMap<Layer, ExporterSettings>();
        }
        if (minecraftSeed == Long.MIN_VALUE) {
            minecraftSeed = seed;
        }
    }

    private final long seed;
    private Map<Point, Tile> tiles = new HashMap<Point, Tile>();
    private final TileFactory tileFactory;
    private int lowestX = Integer.MAX_VALUE, highestX = Integer.MIN_VALUE, lowestY = Integer.MAX_VALUE, highestY = Integer.MIN_VALUE;
    private String name = "Generated World";
    private Terrain subsurfaceMaterial = Terrain.RESOURCES;
    private boolean createGoodiesChest = true, populate;
    private Border border;
    private int borderLevel = 4;
    private boolean darkLevel, bedrockWall;
    private Point spawnPoint = new Point(15, 0);
    private File importedFrom;
    private Map<Layer, ExporterSettings> layerSettings = new HashMap<Layer, ExporterSettings>();
    private long minecraftSeed = Long.MIN_VALUE;
    private transient List<Listener> listeners = new ArrayList<Listener>();
    private transient boolean eventsInhibited;
    private transient Set<Tile> dirtyTiles = new HashSet<Tile>();
    private transient Set<Tile> addedTiles = new HashSet<Tile>();
    private transient Set<Tile> removedTiles = new HashSet<Tile>();
    private transient boolean dirty;
    private transient UndoManager undoManager;
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private static final long serialVersionUID = 2011032801L;

    @Deprecated
    public interface Listener {
        void tileAdded(World world, Tile tile);
        void tileRemoved(World world, Tile tile);
    }

    @Deprecated
    public enum Border {VOID, WATER, LAVA}
}