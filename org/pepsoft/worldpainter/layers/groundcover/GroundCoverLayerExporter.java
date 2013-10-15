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
import org.pepsoft.util.MathUtils;

/**
 *
 * @author pepijn
 */
public class GroundCoverLayerExporter extends AbstractLayerExporter<GroundCoverLayer> implements FirstPassLayerExporter<GroundCoverLayer> {
    public GroundCoverLayerExporter(GroundCoverLayer layer) {
        super(layer);
    }
    
    @Override
    public void render(Dimension dimension, Tile tile, Chunk chunk) {
        final int xOffset = (chunk.getxPos() & 7) << 4;
        final int zOffset = (chunk.getzPos() & 7) << 4;
        final int minY = dimension.isBottomless() ? 0 : 1;
        final int maxY = dimension.getMaxHeight() - 1;
        final Material material = layer.getMaterial();
        final int thickness = layer.getThickness();
//        final boolean taperedEdge = layer.isTaperedEdge() && (thickness > 1);
//        final int edgeWidth = layer.getEdgeWidth();
//        final boolean variedEdge = layer.isVariedEdge();
//        final Random random = new Random(dimension.getSeed() + tile.getX() * 65537 + tile.getY());
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
                        int effectiveThickness = thickness;
                        if (effectiveThickness > 1) {
                            int worldY = (chunk.getzPos() << 4) + z;
                            int surroundingBlockCount = dimension.getBitLayerCount(layer, worldX, worldY, 1) - 1;
//                                System.out.println("Surrounding block count @ " + worldX + "," + worldY + ": " + surroundingBlockCount);
                            if (surroundingBlockCount < 8) {
                                effectiveThickness = Math.min(MathUtils.pow(2, surroundingBlockCount), thickness);
                            }
                        }
//                        if (taperedEdge) {
//                            float distanceToEdge = dimension.getDistanceToEdge(layer, worldX, (chunk.getzPos() << 4) + z, edgeWidth + 1);
//                            if (variedEdge) {
//                                distanceToEdge = Math.max(Math.min(distanceToEdge / 2 + random.nextFloat() * edgeWidth / 2, edgeWidth), 0.0f);
//                            }
//                            if (distanceToEdge < (edgeWidth + 1)) {
//                                double normalisedDistance = distanceToEdge / (edgeWidth + 1) * Math.PI;
//                                effectiveThickness = (int) ((-Math.cos(normalisedDistance) / 2 + 0.5) * (thickness - 1) + 1.5);
//                            }
//                        }
                        if (effectiveThickness > 0) {
                            for (int dy = 0; dy < effectiveThickness; dy++) {
                                if (terrainheight + dy + 1 > maxY) {
                                    break;
                                }
                                if (VERY_INSUBSTANTIAL_BLOCKS.contains(chunk.getBlockType(x, terrainheight + dy + 1, z))) {
                                    chunk.setMaterial(x, terrainheight + dy + 1, z, material);
                                }
                            }
                        } else {
                            for (int dy = 0; dy > effectiveThickness; dy--) {
                                if (terrainheight + dy < minY) {
                                    break;
                                }
                                chunk.setMaterial(x, terrainheight + dy, z, material);
                            }
                        }
                    }
                }
            }
        }
    }
}