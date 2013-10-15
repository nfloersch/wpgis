/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.exporting;

import java.util.Map;
import java.util.Set;
import org.pepsoft.minecraft.ChunkFactory;
import org.pepsoft.minecraft.ChunkImpl;
import org.pepsoft.minecraft.ChunkImpl2;
import org.pepsoft.util.PerlinNoise;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Dimension.Border;
import static org.pepsoft.worldpainter.Terrain.*;
import org.pepsoft.worldpainter.Tile;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.worldpainter.Terrain;
import static org.pepsoft.worldpainter.biomeschemes.Minecraft1_2BiomeScheme.*;
import org.pepsoft.worldpainter.layers.Layer;

/**
 *
 * @author pepijn
 */
public class BorderChunkFactory {
    public static ChunkFactory.ChunkCreationResult create(int chunkX, int chunkZ, Dimension dimension, Map<Layer, LayerExporter<Layer>> exporters) {
        final int maxHeight = dimension.getMaxHeight();
        final int version = dimension.getWorld().getVersion();
        final Border border = dimension.getBorder();
        final int borderLevel = dimension.getBorderLevel();
        final boolean dark = dimension.isDarkLevel();
        final boolean bottomless = dimension.isBottomless();
        final Terrain subsurfaceMaterial = dimension.getSubsurfaceMaterial();
        final PerlinNoise noiseGenerator;
        if (noiseGenerators.get() == null) {
            noiseGenerator = new PerlinNoise(0);
            noiseGenerators.set(noiseGenerator);
        } else {
            noiseGenerator = noiseGenerators.get();
        }
        final long seed = dimension.getSeed();
        if (noiseGenerator.getSeed() != seed) {
            noiseGenerator.setSeed(seed);
        }
        final int floor = Math.max(borderLevel - 20, 0);
        final int variation = Math.min(15, (borderLevel - floor) / 2);

        final ChunkFactory.ChunkCreationResult result = new ChunkFactory.ChunkCreationResult();
        result.chunk = (version == SUPPORTED_VERSION_1) ? new ChunkImpl(chunkX, chunkZ, maxHeight) : new ChunkImpl2(chunkX, chunkZ, maxHeight);
        final int maxY = maxHeight - 1;
        if (version == SUPPORTED_VERSION_2) {
            switch(border) {
                case VOID:
                case LAVA:
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            result.chunk.setBiome(x, z, BIOME_PLAINS);
                        }
                    }
                    break;
                case WATER:
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            result.chunk.setBiome(x, z, BIOME_OCEAN);
                        }
                    }
                    break;
                default:
                    throw new InternalError();
            }
        }
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (border != Border.VOID) {
                    final int worldX = (chunkX << 4) | x, worldZ = (chunkZ << 4) | z;
                    final int floorLevel = (int) (floor + (noiseGenerator.getPerlinNoise(worldX / MEDIUM_BLOBS, worldZ / MEDIUM_BLOBS) + 0.5f) * variation + 0.5f);
                    final int surfaceLayerLevel = floorLevel - dimension.getTopLayerDepth(worldX, worldZ, floorLevel);
                    for (int y = 0; y <= maxY; y++) {
                        if ((y == 0) && (! bottomless)) {
                            result.chunk.setBlockType(x, y, z, BLK_BEDROCK);
                        } else if (y <= surfaceLayerLevel) {
                            result.chunk.setMaterial(x, y, z, subsurfaceMaterial.getMaterial(seed, worldX, worldZ, y, floorLevel));
                        } else if (y <= floorLevel) {
                            result.chunk.setMaterial(x, y, z, BEACHES.getMaterial(seed, worldX, worldZ, y, floorLevel));
                        } else if (y <= borderLevel) {
                            switch(border) {
                                case WATER:
                                    result.chunk.setBlockType(x, y, z, BLK_STATIONARY_WATER);
                                    break;
                                case LAVA:
                                    result.chunk.setBlockType(x, y, z, BLK_STATIONARY_LAVA);
                                    break;
                                default:
                                    // Do nothing
                            }
                        }
                    }
                }
                if (dark) {
                    result.chunk.setBlockType(x, maxY, z, BLK_BEDROCK);
                    result.chunk.setHeight(x, z, maxY);
                } else if (border == Border.VOID) {
                    result.chunk.setHeight(x, z, 0);
                } else {
                    result.chunk.setHeight(x, z, (borderLevel < maxY) ? (borderLevel + 1) : maxY);
                }
            }
        }

        // Apply layers set to be applied everywhere, if any
        final Set<Layer> minimumLayers = dimension.getMinimumLayers();
        if (! minimumLayers.isEmpty()) {
            Tile virtualTile = new Tile(chunkX >> 3, chunkZ >> 3, dimension.getMaxHeight()) {
                @Override
                public synchronized float getHeight(int x, int y) {
                    return floor + (noiseGenerator.getPerlinNoise(((getX() << TILE_SIZE_BITS) | x) / MEDIUM_BLOBS, ((getY() << TILE_SIZE_BITS) | y) / MEDIUM_BLOBS) + 0.5f) * variation;
                }
                
                private static final long serialVersionUID = 1L;
            };
            for (Layer layer: minimumLayers) {
                LayerExporter layerExporter = exporters.get(layer);
                if (layerExporter instanceof FirstPassLayerExporter) {
                    ((FirstPassLayerExporter) layerExporter).render(dimension, virtualTile, result.chunk);
                }
            }
        }

        result.chunk.setTerrainPopulated(true);
        result.stats.surfaceArea = 256;
        if ((border == Border.WATER) || (border == Border.LAVA)) {
            result.stats.waterArea = 256;
        }
        return result;
    }

    private static final ThreadLocal<PerlinNoise> noiseGenerators = new ThreadLocal<PerlinNoise>();
}