/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.exporting;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.pepsoft.minecraft.ChunkImpl;
import org.pepsoft.minecraft.ChunkFactory;
import org.pepsoft.minecraft.ChunkImpl2;
import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.Populate;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.minecraft.Material;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme;
import org.pepsoft.worldpainter.layers.Frost;

/**
 *
 * @author pepijn
 */
public class WorldPainterChunkFactory implements ChunkFactory {
    public WorldPainterChunkFactory(Dimension dimension, Map<Layer, LayerExporter<Layer>> exporters, int version, int maxHeight) {
        if ((version != SUPPORTED_VERSION_1) && (version != SUPPORTED_VERSION_2)) {
            throw new IllegalArgumentException("Not a supported version: 0x" + Integer.toHexString(version));
        }
        this.dimension = dimension;
        this.exporters = exporters;
        this.version = version;
        this.maxHeight = maxHeight;
        minimumLayers = dimension.getMinimumLayers();
    }

    @Override
    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public ChunkCreationResult createChunk(int chunkX, int chunkZ) {
        Tile tile = dimension.getTile(chunkX >> 3, chunkZ >> 3);
        if (tile != null) {
            return createChunk(tile, chunkX, chunkZ);
        } else {
            return null;
        }
    }
    
    public ChunkCreationResult createChunk(Tile tile, int chunkX, int chunkZ) {
        final long seed = dimension.getSeed();
        if (sugarCaneNoise.getSeed() != (seed + SUGAR_CANE_SEED_OFFSET)) {
            sugarCaneNoise.setSeed(seed + SUGAR_CANE_SEED_OFFSET);
        }
        final Terrain subsurfaceMaterial = dimension.getSubsurfaceMaterial();
        final boolean dark = dimension.isDarkLevel();
        final boolean bedrock = ! dimension.isBottomless();

        final int tileX = tile.getX();
        final int tileY = tile.getY();
        final int xOffsetInTile = (chunkX & 7) << 4;
        final int yOffsetInTile = (chunkZ & 7) << 4;
        final Random random = new Random(seed + xOffsetInTile * 3 + yOffsetInTile * 5);
        final boolean populate = dimension.isPopulate() || tile.getBitLayerValue(Populate.INSTANCE, xOffsetInTile, yOffsetInTile);
        final ChunkCreationResult result = new ChunkCreationResult();
        result.chunk = (version == SUPPORTED_VERSION_1) ? new ChunkImpl(chunkX, chunkZ, maxHeight) : new ChunkImpl2(chunkX, chunkZ, maxHeight);
        final int maxY = maxHeight - 1;
        final boolean copyBiomes =
            (version == SUPPORTED_VERSION_2)
            && (dimension.getDim() == DIM_NORMAL)
            && (dimension.getWorld() != null)
            && ((dimension.getWorld().isCustomBiomes())
                || ((dimension.getWorld().getBiomeAlgorithm() != World2.BIOME_ALGORITHM_NONE)
                    && (dimension.getWorld().getBiomeAlgorithm() != World2.BIOME_ALGORITHM_1_7_3)
                    && dimension.isBiomesCalculated()));
        final int defaultBiome;
        switch (dimension.getDim()) {
            case DIM_NORMAL:
                defaultBiome = AbstractMinecraft1_2BiomeScheme.BIOME_PLAINS;
                break;
            case DIM_NETHER:
                defaultBiome = AbstractMinecraft1_2BiomeScheme.BIOME_HELL;
                break;
            case DIM_END:
                defaultBiome = AbstractMinecraft1_2BiomeScheme.BIOME_SKY;
                break;
            default:
                throw new InternalError();
        }
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int xInTile = xOffsetInTile + x;
                final int yInTile = yOffsetInTile + z;
                final int worldX = tileX * TILE_SIZE + xInTile;
                final int worldY = tileY * TILE_SIZE + yInTile;

                if (copyBiomes) {
                    ((ChunkImpl2) result.chunk).setBiome(x, z, dimension.getLayerValueAt(Biome.INSTANCE, worldX, worldY));
                } else if (result.chunk instanceof ChunkImpl2) {
                    ((ChunkImpl2) result.chunk).setBiome(x, z, defaultBiome);
                }

                final int height = tile.getIntHeight(xInTile, yInTile);
                final int waterLevel = tile.getWaterLevel(xInTile, yInTile);
                final boolean _void = tile.getBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, xInTile, yInTile);
                if (! _void) {
                    final Terrain terrain = tile.getTerrain(xInTile, yInTile);
                    final int topLayerDepth = dimension.getTopLayerDepth(worldX, worldY, height);
                    final boolean floodWithLava;
                    final boolean underWater = waterLevel > height;
                    if (underWater) {
                        floodWithLava = tile.getBitLayerValue(FloodWithLava.INSTANCE, xInTile, yInTile);
                        result.stats.waterArea++;
                    } else {
                        floodWithLava = false;
                        result.stats.landArea++;
                    }
                    if (bedrock) {
                        result.chunk.setBlockType(x, 0, z, BLK_BEDROCK);
                    }
                    for (int y = bedrock ? 1 : 0; y <= maxY; y++) {
                        if (y <= (height - topLayerDepth)) {
                            result.chunk.setMaterial(x, y, z, subsurfaceMaterial.getMaterial(seed, worldX, worldY, y, height));
                        } else if (y <= height) {
                            result.chunk.setMaterial(x, y, z, terrain.getMaterial(seed, worldX, worldY, y, height));
                        } else if (y <= waterLevel) {
                            if (floodWithLava) {
                                result.chunk.setBlockType(x, y, z, BLK_STATIONARY_LAVA);
                            } else {
                                result.chunk.setBlockType(x, y, z, BLK_STATIONARY_WATER);
                            }
                        } else if (! underWater) {
                            if ((y > 0) && ((y - height) <= 3) && ((terrain == Terrain.GRASS) || (terrain == Terrain.DESERT) || (terrain == Terrain.BEACHES))
                                    && ((sugarCaneNoise.getPerlinNoise(worldX / TINY_BLOBS, worldY / TINY_BLOBS, z / TINY_BLOBS) * sugarCaneNoise.getPerlinNoise(worldX / SMALL_BLOBS, worldY / SMALL_BLOBS, z / SMALL_BLOBS)) > SUGAR_CANE_CHANCE)
                                    && (isAdjacentWater(tile, height, xInTile - 1, yInTile)
                                        || isAdjacentWater(tile, height, xInTile + 1, yInTile)
                                        || isAdjacentWater(tile, height, xInTile, yInTile - 1)
                                        || isAdjacentWater(tile, height, xInTile, yInTile + 1))) {
                                int blockTypeBelow = result.chunk.getBlockType(x, y - 1, z);
                                if ((random.nextInt(5) > 0) && ((blockTypeBelow == BLK_GRASS) || (blockTypeBelow == BLK_DIRT) || (blockTypeBelow == BLK_SAND) || (blockTypeBelow == BLK_SUGAR_CANE))) {
                                    result.chunk.setMaterial(x, y, z, Material.SUGAR_CANE);
                                } else {
                                    result.chunk.setMaterial(x, y, z, terrain.getMaterial(seed, worldX, worldY, y, height));
                                }
                            } else {
                                result.chunk.setMaterial(x, y, z, terrain.getMaterial(seed, worldX, worldY, y, height));
                            }
                        }
                    }
                }
                if (dark) {
                    result.chunk.setBlockType(x, maxY, z, BLK_BEDROCK);
                    result.chunk.setHeight(x, z, maxY);
                } else if (_void) {
                    result.chunk.setHeight(x, z, 0);
                } else if (waterLevel > height) {
                    result.chunk.setHeight(x, z, (waterLevel < maxY) ? (waterLevel + 1): maxY);
                } else {
                    result.chunk.setHeight(x, z, (height < maxY) ? (height + 1): maxY);
                }
            }
        }
        if (! populate) {
            result.chunk.setTerrainPopulated(true);
        }
        for (Layer layer: tile.getLayers(minimumLayers)) {
            LayerExporter layerExporter = exporters.get(layer);
            if (layerExporter instanceof FirstPassLayerExporter) {
                ((FirstPassLayerExporter) layerExporter).render(dimension, tile, result.chunk);
            }
        }
        result.stats.surfaceArea = 256;
        return result;
    }
    
    private boolean isAdjacentWater(Tile tile, int height, int x, int y) {
        if ((x < 0) || (x >= TILE_SIZE) || (y < 0) || (y >= TILE_SIZE)) {
            return false;
        }
        return (tile.getWaterLevel(x, y) == height)
                && (! tile.getBitLayerValue(FloodWithLava.INSTANCE, x, y))
                && (! tile.getBitLayerValue(Frost.INSTANCE, x, y))
                && (tile.getIntHeight(x, y) < height);
    }

    private final int version, maxHeight;
    private final Dimension dimension;
    private final Set<Layer> minimumLayers;
    private final PerlinNoise sugarCaneNoise = new PerlinNoise(0);
    private final Map<Layer, LayerExporter<Layer>> exporters;

    private static final long SUGAR_CANE_SEED_OFFSET = 127411424;
    private static final float SUGAR_CANE_CHANCE = PerlinNoise.getLevelForPromillage(325);
}