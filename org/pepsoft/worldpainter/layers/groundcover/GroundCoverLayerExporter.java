/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.groundcover;


import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.FirstPassLayerExporter;

import static org.pepsoft.minecraft.Constants.BLK_AIR;
import static org.pepsoft.minecraft.Constants.INSUBSTANTIAL_BLOCKS;
import static org.pepsoft.minecraft.Constants.VERY_INSUBSTANTIAL_BLOCKS;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.NoiseSettings;
import org.pepsoft.worldpainter.heightMaps.NoiseHeightMap;

/**
 * Algorithm:
 * 
 * The layers are rendered in order from the lowest thickness to the highest
 * thickness (which implies that layers digging into the ground are rendered
 * first).
 * 
 * 1. If the layer has a negative thickness (it digs down into the ground), the
 *    blocks is always placed, except if the existing block is air
 * 2. If the layer has positive thickness, and:
 *    a. it is insubstantial (including water), the block is only placed if the
 *       existing block is insubstantial (excluding water)
 *    b. it is substantial, the block is always placed
 * 
 * @author pepijn
 */
public class GroundCoverLayerExporter extends AbstractLayerExporter<GroundCoverLayer> implements FirstPassLayerExporter<GroundCoverLayer> {
    public GroundCoverLayerExporter(GroundCoverLayer layer) {
        super(layer);
        NoiseSettings noiseSettings = layer.getNoiseSettings();
        if (noiseSettings != null) {
            noiseHeightMap = new NoiseHeightMap(noiseSettings.getRange() * 2, noiseSettings.getScale() / 5, noiseSettings.getRoughness() + 1, NOISE_SEED_OFFSET);
            noiseOffset = noiseSettings.getRange();
        } else {
            noiseHeightMap = null;
            noiseOffset = 0;
        }
    }
    
    @Override
    public void render(Dimension dimension, Tile tile, Chunk chunk) {
        if (noiseHeightMap != null) {
            noiseHeightMap.setSeed(dimension.getSeed());
        }
        final int xOffset = (chunk.getxPos() & 7) << 4;
        final int zOffset = (chunk.getzPos() & 7) << 4;
        final int minY = dimension.isBottomless() ? 0 : 1;
        final int maxY = dimension.getMaxHeight() - 1;
        final MixedMaterial mixedMaterial = layer.getMaterial();
        final int thickness = layer.getThickness(), edgeThickness = Math.abs(thickness) - 2;
        final GroundCoverLayer.EdgeShape edgeShape = layer.getEdgeShape();
        final boolean taperedEdge = (edgeShape != GroundCoverLayer.EdgeShape.SHEER) && (Math.abs(thickness) > 1);
        final int edgeWidth = layer.getEdgeWidth(), edgeWidthPlusOne = edgeWidth + 1, edgeWidthMinusOne = edgeWidth - 1;
        final double edgeFactor = edgeThickness / 2.0, edgeOffset = 1.5 + edgeFactor;
        final long seed = dimension.getSeed();
        final boolean smooth = layer.isSmooth();
        for (int x = 0; x < 16; x++) {
            final int localX = xOffset + x;
            final int worldX = (chunk.getxPos() << 4) + x;
            for (int z = 0; z < 16; z++) {
                final int localY = zOffset + z;
                if (tile.getBitLayerValue(layer, localX, localY)) {
                    final int terrainheight = tile.getIntHeight(localX, localY);
                    final int blockBelow = chunk.getBlockType(x, terrainheight, z);
                    if ((blockBelow != BLK_AIR)
                            && (! INSUBSTANTIAL_BLOCKS.contains(blockBelow))) {
                        int effectiveThickness = Math.abs(thickness);
                        final int worldY = (chunk.getzPos() << 4) + z;
                        if (taperedEdge) {
                            float distanceToEdge = dimension.getDistanceToEdge(layer, worldX, worldY, edgeWidthPlusOne);
                            if (distanceToEdge < edgeWidthPlusOne) {
                                final double normalisedDistance = (distanceToEdge - 1) / edgeWidthMinusOne;
                                switch (edgeShape) {
                                    case LINEAR:
                                        effectiveThickness = (int) (1.5 + normalisedDistance * edgeThickness);
                                        break;
                                    case SMOOTH:
                                        effectiveThickness = (int) (edgeOffset + -Math.cos(normalisedDistance * Math.PI) * edgeFactor);
                                        break;
                                    case ROUNDED:
                                        double reversedNormalisedDistance = 1 - (distanceToEdge - 0.5) / edgeWidth;
                                        effectiveThickness = (int) (1.5 + Math.sqrt(1 - reversedNormalisedDistance * reversedNormalisedDistance) * edgeThickness);
                                        break;
                                }
                            }
                        }
                        if (noiseHeightMap != null) {
                            effectiveThickness += noiseHeightMap.getHeight(worldX, worldY) - noiseOffset;
                        }
                        if (thickness > 0) {
                            for (int dy = 0; dy < effectiveThickness; dy++) {
                                final int y = terrainheight + dy + 1;
                                if (y > maxY) {
                                    break;
                                }
                                final int existingBlockType = chunk.getBlockType(x, y, z);
                                final Material material = mixedMaterial.getMaterial(seed, worldX, worldY, y);
                                if ((material != Material.AIR)
                                        && ((! VERY_INSUBSTANTIAL_BLOCKS.contains(material.getBlockType()))
                                            || (existingBlockType == BLK_AIR)
                                            || INSUBSTANTIAL_BLOCKS.contains(existingBlockType))) {
                                    if (smooth && (dy == (effectiveThickness - 1))) {
                                        // Top layer, smooth enabled
                                        int layerHeight;
                                        final float diff = dimension.getHeightAt(worldX, worldY) + 0.5f - dimension.getIntHeightAt(worldX, worldY);
                                        if (diff > 0.125f) {
                                            if        (diff > 0.875f) {
                                                layerHeight = 7;
                                            } else if (diff > 0.750f) {
                                                layerHeight = 6;
                                            } else if (diff > 0.625f) {
                                                layerHeight = 5;
                                            } else if (diff > 0.500f) {
                                                layerHeight = 4;
                                            } else if (diff > 0.375f) {
                                                layerHeight = 3;
                                            } else if (diff > 0.250f) {
                                                layerHeight = 2;
                                            } else {
                                                layerHeight = 1;
                                            }
                                        } else {
                                            layerHeight = 0;
                                        }
                                        if (layerHeight > 0) {
                                            layerHeight = Math.max(Math.min(layerHeight, dimension.getBitLayerCount(layer, worldX, worldY, 1) - 2), 0);
                                        }
                                        chunk.setBlockType(x, y, z, material.getBlockType());
                                        chunk.setDataValue(x, y, z, layerHeight);
                                    } else {
                                        chunk.setMaterial(x, y, z, material);
                                    }
                                }
                            }
                        } else {
                            for (int dy = 0; dy < effectiveThickness; dy++) {
                                final int y = terrainheight - dy;
                                if (y < minY) {
                                    break;
                                }
                                int existingBlockType = chunk.getBlockType(x, y, z);
                                if (existingBlockType != BLK_AIR) {
                                    chunk.setMaterial(x, y, z, mixedMaterial.getMaterial(seed, worldX, worldY, y));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private final NoiseHeightMap noiseHeightMap;
    private final int noiseOffset;
    
    private static final double HALF_PI = Math.PI / 2;
    private static final long NOISE_SEED_OFFSET = 135101785L;
}
