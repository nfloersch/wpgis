/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.awt.Point;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.undo.UndoManager;
import org.pepsoft.worldpainter.Dimension.Border;
import org.pepsoft.worldpainter.Dimension.Listener;
import org.pepsoft.worldpainter.biomeschemes.CustomBiome;
import org.pepsoft.worldpainter.gardenofeden.Garden;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

/**
 *
 * @author pepijn
 */
public class RODelegatingDimension extends Dimension {
    public RODelegatingDimension(Dimension dimension) {
        super(dimension.getMinecraftSeed(), dimension.getTileFactory(), dimension.getDim(), dimension.getMaxHeight());
        this.dimension = dimension;
    }

    @Override
    public World2 getWorld() {
        return dimension.getWorld(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDim() {
        return dimension.getDim(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return dimension.getName(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDirty() {
        return dimension.isDirty(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getSeed() {
        return dimension.getSeed(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Terrain getSubsurfaceMaterial() {
        return dimension.getSubsurfaceMaterial(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isPopulate() {
        return dimension.isPopulate(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Border getBorder() {
        return dimension.getBorder(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getBorderLevel() {
        return dimension.getBorderLevel(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getBorderSize() {
        return dimension.getBorderSize(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDarkLevel() {
        return dimension.isDarkLevel(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBedrockWall() {
        return dimension.isBedrockWall(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TileFactory getTileFactory() {
        return dimension.getTileFactory(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getTileCount() {
        return dimension.getTileCount(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeTile(Point coords) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeTile(int tileX, int tileY) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntHeightAt(int x, int y) {
        return dimension.getIntHeightAt(x, y); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIntHeightAt(Point coords) {
        return dimension.getIntHeightAt(coords); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getHeightAt(int x, int y) {
        return dimension.getHeightAt(x, y); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getHeightAt(Point coords) {
        return dimension.getHeightAt(coords); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getRawHeightAt(int x, int y) {
        return dimension.getRawHeightAt(x, y); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getRawHeightAt(Point coords) {
        return dimension.getRawHeightAt(coords); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRawHeightAt(int x, int y, int rawHeight) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRawHeightAt(Point coords, int rawHeight) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Terrain getTerrainAt(int x, int y) {
        return super.getTerrainAt(x, y); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getWaterLevelAt(int x, int y) {
        return dimension.getWaterLevelAt(x, y); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getWaterLevelAt(Point coords) {
        return dimension.getWaterLevelAt(coords); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLayerValueAt(Layer layer, int x, int y) {
        return dimension.getLayerValueAt(layer, x, y); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLayerValueAt(Layer layer, Point coords) {
        return dimension.getLayerValueAt(layer, coords); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getBitLayerValueAt(Layer layer, int x, int y) {
        return dimension.getBitLayerValueAt(layer, x, y); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getBitLayerCount(Layer layer, int x, int y, int r) {
        return dimension.getBitLayerCount(layer, x, y, r); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getDistanceToEdge(Layer layer, int x, int y, float maxDistance) {
        return dimension.getDistanceToEdge(layer, x, y, maxDistance); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateBiome(Tile tile, int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearLayerData(Layer layer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getMinecraftSeed() {
        return dimension.getMinecraftSeed(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getOverlay() {
        return dimension.getOverlay(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOverlay(File overlay) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOverlayOffsetX() {
        return dimension.getOverlayOffsetX(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOverlayOffsetX(int overlayOffsetX) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOverlayOffsetY() {
        return dimension.getOverlayOffsetY(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOverlayOffsetY(int overlayOffsetY) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getOverlayScale() {
        return dimension.getOverlayScale(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOverlayScale(float overlayScale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getOverlayTransparency() {
        return dimension.getOverlayTransparency(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOverlayTransparency(float overlayTransparency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGridEnabled() {
        return dimension.isGridEnabled(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setGridEnabled(boolean gridEnabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getGridSize() {
        return dimension.getGridSize(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setGridSize(int gridSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOverlayEnabled() {
        return dimension.isOverlayEnabled(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOverlayEnabled(boolean overlayEnabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxHeight() {
        return dimension.getMaxHeight(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAutoUpdateBiomes() {
        return dimension.isAutoUpdateBiomes(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAutoUpdateBiomes(boolean autoUpdateBiomes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getContourSeparation() {
        return dimension.getContourSeparation(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setContourSeparation(int contourSeparation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isContoursEnabled() {
        return dimension.isContoursEnabled(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setContoursEnabled(boolean contoursEnabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTopLayerMinDepth() {
        return dimension.getTopLayerMinDepth(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTopLayerMinDepth(int topLayerMinDepth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTopLayerVariation() {
        return dimension.getTopLayerVariation(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTopLayerVariation(int topLayerVariation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBottomless() {
        return dimension.isBottomless(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBottomless(boolean bottomless) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getLastViewPosition() {
        return dimension.getLastViewPosition(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLastViewPosition(Point lastViewPosition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CustomBiome> getCustomBiomes() {
        return dimension.getCustomBiomes(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCustomBiomes(List<CustomBiome> customBiomes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Garden getGarden() {
        return dimension.getGarden(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Layer> getAllLayers(boolean applyCombinedLayers) {
        return dimension.getAllLayers(applyCombinedLayers); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Layer> getMinimumLayers() {
        return dimension.getMinimumLayers(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean undoChanges() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearUndo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearRedo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBiomesCalculated() {
        return dimension.isBiomesCalculated(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recalculateBiomes(Tile tile, BiomeScheme biomeScheme) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dimension getSnapshot() {
        return dimension.getSnapshot(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getTopLayerDepth(int x, int y, int z) {
        return dimension.getTopLayerDepth(x, y, z); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void ensureAllReadable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeDimensionListener(Listener listener) {
        // Do nothing
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // Do nothing
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // Do nothing
    }

    @Override
    public void rotate(CoordinateTransform rotation, ProgressReceiver progressReceiver) throws OperationCancelled {
        super.rotate(rotation, progressReceiver); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void heightMapChanged(Tile tile) {
        // Do nothing
    }

    @Override
    public void terrainChanged(Tile tile) {
        // Do nothing
    }

    @Override
    public void waterLevelChanged(Tile tile) {
        // Do nothing
    }

    @Override
    public void seedsChanged(Tile tile) {
        // Do nothing
    }

    @Override
    public void layerDataChanged(Tile tile, Set<Layer> changedLayers) {
        // Do nothing
    }

    @Override
    public void allBitLayerDataChanged(Tile tile) {
        // Do nothing
    }

    @Override
    public void allNonBitlayerDataChanged(Tile tile) {
        // Do nothing
    }

    @Override
    public void addDimensionListener(Listener listener) {
        // Do nothing
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // Do nothing
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // Do nothing
    }

    @Override
    public void addTile(Tile tile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyTheme(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyTheme(Point coords) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregister() {
        // Do nothing
    }

    @Override
    public void armSavePoint() {
        // Do nothing
    }

    @Override
    public void register(UndoManager undoManager) {
        // Do nothing
    }

    @Override
    public boolean isUndoAvailable() {
        return false;
    }

    @Override
    public int getHeight() {
        return dimension.getHeight();
    }

    @Override
    public int getHighestX() {
        return dimension.getHighestX();
    }

    @Override
    public int getHighestY() {
        return dimension.getHighestY();
    }

    @Override
    public <L extends Layer> ExporterSettings<L> getLayerSettings(L layer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Layer, ExporterSettings> getAllLayerSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLowestX() {
        return dimension.getLowestX();
    }

    @Override
    public int getLowestY() {
        return dimension.getLowestY();
    }

    @Override
    public synchronized int getFloodedCount(int x, int y, int r, boolean lava) {
        return dimension.getFloodedCount(x, y, r, lava);
    }

    @Override
    public float getSlope(int x, int y) {
        return dimension.getSlope(x, y);
    }

    @Override
    public Tile getTile(int x, int y) {
        return getTile(new Point(x, y));
    }

    @Override
    public Tile getTile(Point coords) {
        // In theory this is not correct, since the dimension might have gained
        // or lost tiles in the mean time. However the expected usage pattern
        // of the functionality is such that that should not happen in practice,
        // and creating tile snapshots of all tiles when the dimension snapshot
        // is created would be a performance hit
        RODelegatingTile cachedTile = tileCache.get(coords);
        if (cachedTile == null) {
            Tile tile = dimension.getTile(coords);
            if (tile != null) {
                cachedTile = new RODelegatingTile(tile);
                tileCache.put(coords, cachedTile);
            }
        }
        return cachedTile;
    }

    @Override
    public Collection<? extends Tile> getTiles() {
        Collection<? extends Tile> tiles = dimension.getTiles();
        for (Tile tile: tiles) {
            Point coords = new Point(tile.getX(), tile.getY());
            if (! tileCache.containsKey(coords)) {
                RODelegatingTile cachedTile = new RODelegatingTile(tile);
                tileCache.put(coords, cachedTile);
            }
        }
        return Collections.unmodifiableCollection(tileCache.values());
    }

    @Override
    public int getWidth() {
        return dimension.getWidth();
    }

    @Override
    public boolean isEventsInhibited() {
        return false;
    }

    @Override
    public void recalculateBiomes(BiomeScheme biomeScheme, Window parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeTile(Tile tile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBedrockWall(boolean bedrockWall) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBitLayerValueAt(Layer layer, int x, int y, boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBorder(Border border) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBorderLevel(int borderLevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBorderSize(int borderSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDarkLevel(boolean darkLevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDirty(boolean dirty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEventsInhibited(boolean eventsInhibited) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeightAt(int x, int y, float height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeightAt(Point coords, float height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLayerSettings(Layer layer, ExporterSettings settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLayerValueAt(Layer layer, int x, int y, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMinecraftSeed(long minecraftSeed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPopulate(boolean populate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubsurfaceMaterial(Terrain subsurfaceMaterial) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTerrainAt(int x, int y, Terrain terrain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTerrainAt(Point coords, Terrain terrain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWaterLevelAt(int x, int y, int waterLevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    void setWorld(World2 world) {
        throw new UnsupportedOperationException();
    }
    
    protected final Dimension dimension;
    private final Map<Point, RODelegatingTile> tileCache = new HashMap<Point, RODelegatingTile>();
    
    private static final long serialVersionUID = 1L;
}