/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import org.pepsoft.util.PerlinNoise;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.SortedMap;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import static org.pepsoft.worldpainter.Constants.*;

/**
 *
 * @author pepijn
 */
public class HeightMapTileFactory implements TileFactory {
    public HeightMapTileFactory(HeightMap heightMap, int maxHeight, int waterHeight, boolean floodWithLava, boolean randomise, boolean beaches) {
        this.maxHeight = maxHeight;
        this.waterHeight = waterHeight;
        this.floodWithLava = floodWithLava;
        terrainRangesTable = new Terrain[maxHeight];
        this.randomise = randomise;
        this.beaches = beaches;
        setHeightMap(heightMap);
    }

    public HeightMapTileFactory(HeightMap heightMap, int maxHeight, SortedMap<Integer, Terrain> terrainRanges, int waterHeight, boolean floodWithLava, boolean randomise, boolean beaches) {
        this(heightMap, maxHeight, waterHeight, floodWithLava, randomise, beaches);
        setTerrainRanges(terrainRanges);
    }

    @Override
    public final int getMaxHeight() {
        return maxHeight;
    }

    public final void setMaxHeight(int maxHeight) {
        setMaxHeight(maxHeight, HeightTransform.IDENTITY);
    }
    
    public final void setMaxHeight(int maxHeight, HeightTransform transform) {
        if (maxHeight != this.maxHeight) {
            this.maxHeight = maxHeight;
            Terrain[] oldTerrainRangesTable = terrainRangesTable;
            terrainRangesTable = new Terrain[maxHeight];
            if (terrainRanges != null) {
                SortedMap<Integer, Terrain> oldTerrainRanges = this.terrainRanges;
                terrainRanges = new TreeMap<Integer, Terrain>();
                for (Map.Entry<Integer, Terrain> oldEntry: oldTerrainRanges.entrySet()) {
                    terrainRanges.put(oldEntry.getKey() < 0
                        ? oldEntry.getKey()
                        : clamp(transform.transformHeight(oldEntry.getKey()), maxHeight - 1), oldEntry.getValue());
                }
                for (int i = 0; i < maxHeight; i++) {
                    terrainRangesTable[i] = terrainRanges.get(terrainRanges.headMap(i).lastKey());
                }
            } else {
                // No terrain ranges map set; this is probably because it is
                // an old map. All we can do is extend the last entry
                System.arraycopy(oldTerrainRangesTable, 0, terrainRangesTable, 0, Math.min(oldTerrainRangesTable.length, terrainRangesTable.length));
                if (terrainRangesTable.length > oldTerrainRangesTable.length) {
                    for (int i = oldTerrainRangesTable.length; i < terrainRangesTable.length; i++) {
                        terrainRangesTable[i] = oldTerrainRangesTable[oldTerrainRangesTable.length - 1];
                    }
                }
            }
        }
    }

    public final int getWaterHeight() {
        return waterHeight;
    }

    public final void setWaterHeight(int waterHeight) {
        this.waterHeight = waterHeight;
    }

    public final boolean isFloodWithLava() {
        return floodWithLava;
    }

    public final boolean isRandomise() {
        return randomise;
    }

    public final void setRandomise(boolean randomise) {
        this.randomise = randomise;
    }

    public final boolean isBeaches() {
        return beaches;
    }

    public final void setBeaches(boolean beaches) {
        this.beaches = beaches;
    }

    public final HeightMap getHeightMap() {
        return heightMap;
    }
    
    public final float getBaseHeight() {
        return heightMap.getBaseHeight();
    }

    public final void setHeightMap(HeightMap heightMap) {
        if (heightMap == null) {
            throw new NullPointerException();
        }
        this.heightMap = heightMap;
    }

    @Override
    public final Tile createTile(long seed, int tileX, int tileY) {
        if (seed != this.seed) {
            this.seed = seed;
            heightMap.setSeed(seed);
        }
        int maxY = getMaxHeight() - 1;
        Tile tile = new Tile(tileX, tileY, maxHeight);
        tile.setEventsInhibited(true);
        int worldTileX = tileX * TILE_SIZE, worldTileY = tileY * TILE_SIZE;
        try {
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int y = 0; y < TILE_SIZE; y++) {
                    int blockX = worldTileX + x, blockY = worldTileY + y;
                    float height = clamp(heightMap.getHeight(blockX, blockY), maxY);
                    tile.setHeight(x, y, height);
                    tile.setTerrain(x, y, getTerrain(seed, blockX, blockY, (int) (height + 0.5f)));
                    tile.setWaterLevel(x, y, waterHeight);
                    if (floodWithLava) {
                        tile.setBitLayerValue(FloodWithLava.INSTANCE, x, y, true);
                    }
                }
            }
            return tile;
        } finally {
            tile.setEventsInhibited(false);
        }
    }

    @Override
    public final void applyTheme(long seed, Tile tile, int x, int y) {
        float height = tile.getHeight(x, y);
        Terrain terrain = getTerrain(seed, x, y, Math.max(Math.min((int) (height + 0.5f), maxHeight - 1), 0));
        if (tile.getTerrain(x, y) != terrain) {
            tile.setTerrain(x, y, terrain);
        }
    }

    /**
     * Set the altitude to default terrain mapping. Every entry indicates a
     * terrain type (as the value) and the corresponding lower height limit
     * minus one for that terrain (as the key). A value lower than zero should
     * always be present, otherwise an error will occur.
     * 
     * @param terrainRanges The altitude to default terrain mapping.
     */
    public final void setTerrainRanges(SortedMap<Integer, Terrain> terrainRanges) {
        this.terrainRanges = terrainRanges;
        for (int i = 0; i < maxHeight; i++) {
            terrainRangesTable[i] = terrainRanges.get(terrainRanges.headMap(i).lastKey());
        }
    }

    public final SortedMap<Integer, Terrain> getTerrainRanges() {
        return terrainRanges;
    }

    protected Terrain getTerrain(long seed, int x, int y, int height) {
        if (beaches && (height >= (waterHeight - 2)) && (height <= (waterHeight + 1))) {
            return Terrain.BEACHES;
        } else {
            if (isRandomise()) {
                if (perlinNoise.getSeed() != (seed + SEED_OFFSET)) {
                    perlinNoise.setSeed(seed + SEED_OFFSET);
                }
                height += perlinNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, height / SMALL_BLOBS) * 5;
                height += perlinNoise.getPerlinNoise(x / TINY_BLOBS, y / TINY_BLOBS, height / TINY_BLOBS) * 5;
                return terrainRangesTable[clamp(height, getMaxHeight() - 1)];
            } else {
                return terrainRangesTable[height];
            }
        }
    }
    
    protected final int clamp(int value, int max) {
        return (value < 0)
            ? 0
            : ((value > max)
                ? max
                : value);
    }

    protected final float clamp(float value, int max) {
        return (value < 0)
            ? 0
            : ((value > max)
                ? max
                : value);
    }
    
    public static SortedMap<Integer, Terrain> createDefaultTerrainMap(Terrain topTerrain, int maxHeight, int baseHeight) {
        SortedMap<Integer, Terrain> terrainRanges = new TreeMap<Integer, Terrain>();
        float factor = maxHeight / 128f;
        terrainRanges.put(-1                              , topTerrain);
        terrainRanges.put((int) (32 * factor) + baseHeight, Terrain.DIRT);
        terrainRanges.put((int) (48 * factor) + baseHeight, Terrain.ROCK);
        terrainRanges.put((int) (64 * factor) + baseHeight, Terrain.SNOW);
        terrainRanges.put((int) (80 * factor) + baseHeight, Terrain.DEEP_SNOW);
        return terrainRanges;
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Legacy map support
        if (maxHeight == 0) {
            maxHeight = 128;
        }
        if (perlinNoise == null) {
            perlinNoise = new PerlinNoise(0);
            // beaches and randomise properties will be taken care of by
            // NoiseTileFactory.readObject() if necessary
        }
    }
    
    int waterHeight;
    
    private Terrain[] terrainRangesTable;
    private final boolean floodWithLava;
    private int maxHeight;
    private SortedMap<Integer, Terrain> terrainRanges;
    private PerlinNoise perlinNoise = new PerlinNoise(0);
    private boolean randomise, beaches;
    private long seed = Long.MIN_VALUE;
    private HeightMap heightMap;

    private static final long SEED_OFFSET = 131;
    private static final long serialVersionUID = 2011032801L;
}