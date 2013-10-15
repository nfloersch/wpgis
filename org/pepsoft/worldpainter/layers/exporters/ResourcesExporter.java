/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.exporters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import org.pepsoft.minecraft.Chunk;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.util.PerlinNoise;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.FirstPassLayerExporter;
import org.pepsoft.worldpainter.layers.Resources;

/**
 *
 * @author pepijn
 */
public class ResourcesExporter extends AbstractLayerExporter<Resources> implements FirstPassLayerExporter<Resources> {
    public ResourcesExporter() {
        super(Resources.INSTANCE);
    }
    
    @Override
    public void setSettings(ExporterSettings<Resources> settings) {
        super.setSettings(settings);
        ResourcesExporterSettings resourcesSettings = (ResourcesExporterSettings) getSettings();
        if (resourcesSettings != null) {
            noiseGenerators = new PerlinNoise[256];
            seedOffsets = new long[256];
            minLevels = new int[256];
            maxLevels = new int[256];
            chances = new float[256];
            activeOreCount = 0;
            for (int blockType: resourcesSettings.getBlockTypes()) {
                if (resourcesSettings.getChance(blockType) == 0) {
                    continue;
                }
                activeOreCount++;
                noiseGenerators[blockType] = new PerlinNoise(0);
                seedOffsets[blockType] = resourcesSettings.getSeedOffset(blockType);
                minLevels[blockType] = resourcesSettings.getMinLevel(blockType);
                maxLevels[blockType] = resourcesSettings.getMaxLevel(blockType);
                chances[blockType] = PerlinNoise.getLevelForPromillage(resourcesSettings.getChance(blockType));
            }
        }
    }
    
    @Override
    public void render(Dimension dimension, Tile tile, Chunk chunk) {
        ResourcesExporterSettings settings = (ResourcesExporterSettings) getSettings();
        if (settings == null) {
            settings = new ResourcesExporterSettings(dimension.getMaxHeight());
            setSettings(settings);
        }
        
        int minimumLevel = settings.getMinimumLevel();
        int xOffset = (chunk.getxPos() & 7) << 4;
        int zOffset = (chunk.getzPos() & 7) << 4;
        long seed = dimension.getSeed();
        int[] oreTypes = new int[activeOreCount];
        int i = 0;
        for (int oreType: settings.getBlockTypes()) {
            if (settings.getChance(oreType) == 0) {
                continue;
            }
            oreTypes[i++] = oreType;
        }
        if ((currentSeed == 0) || (currentSeed != seed)) {
            for (int blockType: oreTypes) {
                noiseGenerators[blockType].setSeed(seed + seedOffsets[blockType]);
            }
        }
        Terrain subsurfaceMaterial = dimension.getSubsurfaceMaterial();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int localX = xOffset + x, localY = zOffset + z;
                int worldX = tile.getX() * TILE_SIZE + localX, worldY = tile.getY() * TILE_SIZE + localY;
                if (tile.getBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, localX, localY)) {
                    continue;
                }
                int resourcesValue = Math.max(minimumLevel, tile.getLayerValue(Resources.INSTANCE, localX, localY));
                if (resourcesValue > 0) {
                    float bias = (float) Math.pow(0.9, resourcesValue - 8);
                    int terrainheight = tile.getIntHeight(localX, localY);
                    double dx = worldX / TINY_BLOBS, dy = worldY / TINY_BLOBS;
                    double dirtX = worldX / SMALL_BLOBS, dirtY = worldY / SMALL_BLOBS;
                    for (int y = terrainheight - dimension.getTopLayerDepth(worldX, worldY, terrainheight); y > 0; y--) {
                        int blockType = chunk.getBlockType(x, y, z);
                        int terrainBlockType = subsurfaceMaterial.getMaterial(seed, x, y, z, terrainheight).getBlockType();
                        if (blockType == terrainBlockType) {
                            double dz = y / TINY_BLOBS;
                            double dirtZ = y / SMALL_BLOBS;
                            for (int oreType: oreTypes) {
                                float chance = bias * chances[oreType];
                                if ((chance <= 0.5f) && (y >= minLevels[oreType]) && (y <= maxLevels[oreType])
                                        && (((oreType == BLK_DIRT) || (oreType == BLK_GRAVEL))
                                            ? (noiseGenerators[oreType].getPerlinNoise(dirtX, dirtY, dirtZ) >= chance)
                                            : (noiseGenerators[oreType].getPerlinNoise(dx, dy, dz) >= chance))) {
                                    chunk.setBlockType(x, y, z, oreType);
                                    chunk.setDataValue(x, y, z, 0);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
//  TODO: resource frequenties onderzoeken met Statistics tool!

    private PerlinNoise[] noiseGenerators;
    private long[] seedOffsets;
    private int[] minLevels, maxLevels;
    private float[] chances;
    private int activeOreCount;
    private long currentSeed;
    
    public static class ResourcesExporterSettings implements ExporterSettings<Resources> {
        public ResourcesExporterSettings(int maxHeight) {
            minLevels.put(BLK_GOLD_ORE,         0);
            minLevels.put(BLK_IRON_ORE,         0);
            minLevels.put(BLK_COAL,             0);
            minLevels.put(BLK_LAPIS_LAZULI_ORE, 0);
            minLevels.put(BLK_DIAMOND_ORE,      0);
            minLevels.put(BLK_REDSTONE_ORE,     0);
            minLevels.put(BLK_WATER,            0);
            minLevels.put(BLK_LAVA,             0);
            minLevels.put(BLK_DIRT,             0);
            minLevels.put(BLK_GRAVEL,           0);
            minLevels.put(BLK_EMERALD_ORE,      0);
            
            maxLevels.put(BLK_GOLD_ORE,         31);
            maxLevels.put(BLK_IRON_ORE,         63);
            maxLevels.put(BLK_COAL,             maxHeight - 1);
            maxLevels.put(BLK_LAPIS_LAZULI_ORE, 31);
            maxLevels.put(BLK_DIAMOND_ORE,      15);
            maxLevels.put(BLK_REDSTONE_ORE,     15);
            maxLevels.put(BLK_WATER,            maxHeight - 1);
            maxLevels.put(BLK_LAVA,             15);
            maxLevels.put(BLK_DIRT,             maxHeight - 1);
            maxLevels.put(BLK_GRAVEL,           maxHeight - 1);
            maxLevels.put(BLK_EMERALD_ORE,      31);
            
            chances.put(BLK_GOLD_ORE,          1);
            chances.put(BLK_IRON_ORE,          6);
            chances.put(BLK_COAL,             10);
            chances.put(BLK_LAPIS_LAZULI_ORE,  1);
            chances.put(BLK_DIAMOND_ORE,       1);
            chances.put(BLK_REDSTONE_ORE,      8);
            chances.put(BLK_WATER,             1);
            chances.put(BLK_LAVA,              2);
            chances.put(BLK_DIRT,             57);
            chances.put(BLK_GRAVEL,           28);
            if (maxHeight != DEFAULT_MAX_HEIGHT_2) {
                chances.put(BLK_EMERALD_ORE,   0);
            } else {
                chances.put(BLK_EMERALD_ORE,   1);
            }
            
            Random random = new Random();
            for (int blockType: maxLevels.keySet()) {
                seedOffsets.put(blockType, random.nextLong());
            }
        }
        
        private ResourcesExporterSettings(int minimumLevel, Map<Integer, Integer> minLevels, Map<Integer, Integer> maxLevels, Map<Integer, Integer> chances, Map<Integer, Long> seedOffsets) {
            this.minimumLevel = minimumLevel;
            this.minLevels.putAll(minLevels);
            this.maxLevels.putAll(maxLevels);
            this.chances.putAll(chances);
            this.seedOffsets.putAll(seedOffsets);
        }
        
        @Override
        public boolean isApplyEverywhere() {
            return minimumLevel > 0;
        }

        public int getMinimumLevel() {
            return minimumLevel;
        }

        public void setMinimumLevel(int minimumLevel) {
            this.minimumLevel = minimumLevel;
        }
        
        public Set<Integer> getBlockTypes() {
            return maxLevels.keySet();
        }
        
        public int getMinLevel(int blockType) {
            return minLevels.get(blockType);
        }
        
        public void setMinLevel(int blockType, int minLevel) {
            minLevels.put(blockType, minLevel);
        }
        
        public int getMaxLevel(int blockType) {
            return maxLevels.get(blockType);
        }
        
        public void setMaxLevel(int blockType, int maxLevel) {
            maxLevels.put(blockType, maxLevel);
        }
        
        public int getChance(int blockType) {
            return chances.get(blockType);
        }
        
        public void setChance(int blockType, int chance) {
            chances.put(blockType, chance);
        }

        public long getSeedOffset(int blockType) {
            return seedOffsets.get(blockType);
        }
        
        @Override
        public Resources getLayer() {
            return Resources.INSTANCE;
        }

        @Override
        public ResourcesExporterSettings clone() {
            return new ResourcesExporterSettings(minimumLevel, minLevels, maxLevels, chances, seedOffsets);
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            
            // Fix static water and lava
            if (! maxLevels.containsKey(BLK_WATER)) {
                logger.warning("Fixing water and lava settings");
                maxLevels.put(BLK_WATER, maxLevels.get(BLK_STATIONARY_WATER));
                chances.put(BLK_WATER, chances.get(BLK_STATIONARY_WATER));
                seedOffsets.put(BLK_WATER, seedOffsets.get(BLK_STATIONARY_WATER));
                maxLevels.put(BLK_LAVA, maxLevels.get(BLK_STATIONARY_LAVA));
                chances.put(BLK_LAVA, chances.get(BLK_STATIONARY_LAVA));
                seedOffsets.put(BLK_LAVA, seedOffsets.get(BLK_STATIONARY_LAVA));
                maxLevels.remove(BLK_STATIONARY_WATER);
                chances.remove(BLK_STATIONARY_WATER);
                seedOffsets.remove(BLK_STATIONARY_WATER);
                maxLevels.remove(BLK_STATIONARY_LAVA);
                chances.remove(BLK_STATIONARY_LAVA);
                seedOffsets.remove(BLK_STATIONARY_LAVA);
            }
            if (! maxLevels.containsKey(BLK_EMERALD_ORE)) {
                maxLevels.put(BLK_EMERALD_ORE, 31);
                chances.put(BLK_EMERALD_ORE, 0);
            }
            if (! seedOffsets.containsKey(BLK_EMERALD_ORE)) {
                seedOffsets.put(BLK_EMERALD_ORE, new Random().nextLong());
            }
            if (minLevels == null) {
                minLevels = new HashMap<Integer, Integer>();
                for (int blockType: maxLevels.keySet()) {
                    minLevels.put(blockType, 0);
                }
            }
        }
        
        private int minimumLevel = 8;
        private final Map<Integer, Integer> maxLevels = new HashMap<Integer, Integer>();
        private final Map<Integer, Integer> chances = new HashMap<Integer, Integer>();
        private final Map<Integer, Long> seedOffsets = new HashMap<Integer, Long>();
        private Map<Integer, Integer> minLevels = new HashMap<Integer, Integer>();

        private static final long serialVersionUID = 1L;
        private static final Logger logger = Logger.getLogger(ResourcesExporter.class.getName());
    }
}