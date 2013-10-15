/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.pockets;

import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.Material;
import org.pepsoft.util.PerlinNoise;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.FirstPassLayerExporter;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

/**
 *
 * @author pepijn
 */
public class UndergroundPocketsLayerExporter extends AbstractLayerExporter<UndergroundPocketsLayer> implements FirstPassLayerExporter<UndergroundPocketsLayer> {
    public UndergroundPocketsLayerExporter(UndergroundPocketsLayer layer) {
        super(layer);
    }

    @Override
    public void setSettings(ExporterSettings<UndergroundPocketsLayer> settings) {
        super.setSettings(settings);
        seedOffset = layer.getMaterial().hashCode();
        chance = PerlinNoise.getLevelForPromillage(layer.getFrequency());
        scale = TINY_BLOBS * (layer.getScale() / 100.0);
    }
    
    @Override
    public void render(Dimension dimension, Tile tile, Chunk chunk) {
        Material material = layer.getMaterial();
        int minLevel = layer.getMinLevel();
        int maxLevel = layer.getMaxLevel();
        
        int xOffset = (chunk.getxPos() & 7) << 4;
        int zOffset = (chunk.getzPos() & 7) << 4;
        long seed = dimension.getSeed();
        if (noiseGenerator.getSeed() != seed + seedOffset) {
            noiseGenerator.setSeed(seed + seedOffset);
        }
        // Coordinates in chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Coordinates in tile
                int localX = xOffset + x, localY = zOffset + z;
                if (tile.getBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, localX, localY)) {
                    continue;
                }
                int value = tile.getLayerValue(layer, localX, localY);
                if (value > 0) {
                    float bias = (float) Math.pow(0.9, value - 8);
                    float biasedChance = bias * chance;
                    int terrainheight = tile.getIntHeight(localX, localY);
                    // Coordinates in world
                    int worldX = tile.getX() << TILE_SIZE_BITS | localX, worldY = tile.getY() << TILE_SIZE_BITS | localY;
                    int minY = Math.max(1, minLevel);
                    int maxY = Math.min(terrainheight - dimension.getTopLayerDepth(worldX, worldY, terrainheight), maxLevel);
                    if (biasedChance <= -0.5f) {
                        // Special case: replace every block
                        for (int y = maxY; y >= minY; y--) {
                            chunk.setMaterial(x, y, z, material);
                        }
                    } else {
                        for (int y = maxY; y >= minY; y--) {
                            double dx = worldX / scale, dy = worldY / scale, dz = y / scale;
                            if (noiseGenerator.getPerlinNoise(dx, dy, dz) >= biasedChance) {
                                chunk.setMaterial(x, y, z, material);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private final PerlinNoise noiseGenerator = new PerlinNoise(0);
    private long seedOffset;
    private float chance;
    private double scale;
}
