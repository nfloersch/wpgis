/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.exporters;

import java.awt.Rectangle;
import java.util.List;

import org.pepsoft.util.MathUtils;
import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.pepsoft.worldpainter.layers.Void;

import static org.pepsoft.minecraft.Constants.BLK_LAVA;
import static org.pepsoft.minecraft.Constants.BLK_STATIONARY_LAVA;
import static org.pepsoft.minecraft.Constants.BLK_STATIONARY_WATER;
import static org.pepsoft.minecraft.Constants.BLK_WATER;
import static org.pepsoft.minecraft.Material.AIR;
import static org.pepsoft.worldpainter.Constants.SMALL_BLOBS;

/**
 * This exporter does the second half of the void processing. The first half
 * has been performed in the first pass by hardcoded code which has left any
 * columns marked as Void completely empty.
 * 
 * <p>This plugin does some decoration around the void areas.
 * 
 * @author pepijn
 */
public class VoidExporter extends AbstractLayerExporter<org.pepsoft.worldpainter.layers.Void> implements SecondPassLayerExporter<org.pepsoft.worldpainter.layers.Void> {
    public VoidExporter() {
        super(Void.INSTANCE);
    }
    
    @Override
    public List<Fixup> render(Dimension dimension, Rectangle area, Rectangle exportedArea, MinecraftWorld minecraftWorld) {
        if (noise.getSeed() != (dimension.getSeed() + SEED_OFFSET)) {
            noise.setSeed(dimension.getSeed() + SEED_OFFSET);
        }
        for (int x = area.x; x < area.x + area.width; x++) {
            for (int y = area.y; y < area.y + area.height; y++) {
                if (dimension.getBitLayerValueAt(org.pepsoft.worldpainter.layers.Void.INSTANCE, x, y)) {
                    // We're in the void. Check whether we're on the edge
                    columnLoop: for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (((dx != 0) || (dy != 0)) && (! dimension.getBitLayerValueAt(org.pepsoft.worldpainter.layers.Void.INSTANCE, x + dx, y + dy))) {
                                // We're on the edge
                                processEdgeColumn(dimension, x, y, minecraftWorld);
                                break columnLoop;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void processEdgeColumn(Dimension dimension, int x, int y, MinecraftWorld minecraftWorld) {
        int maxHeight = minecraftWorld.getMaxHeight();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                if ((dx != 0) || (dy != 0)) {
                    int x2 = x + dx, y2 = y + dy;
                    float distance = MathUtils.getDistance(dx, dy);
                    float height = dimension.getHeightAt(x2, y2);
                    int depth = (int) (height / Math.pow(2, distance + noise.getPerlinNoise(x2 / SMALL_BLOBS, y2 / SMALL_BLOBS)) + 0.5f);
                    for (int z = 0; z < depth; z++) {
                        minecraftWorld.setMaterialAt(x2, y2, z, AIR);
                    }
                    if (((dx == 0) && ((dy == -1) || (dy == 1))) || ((dy == 0) && ((dx == -1) || (dx == 1)))) {
                        // TODO: do this for the other columns as well
                        for (int z = 0; z < maxHeight; z++) {
                            int existingBlockType = minecraftWorld.getBlockTypeAt(x2, y2, z);
                            if (existingBlockType == BLK_STATIONARY_WATER) {
                                minecraftWorld.setBlockTypeAt(x2, y2, z, BLK_WATER);
                            } else if (existingBlockType == BLK_STATIONARY_LAVA) {
                                minecraftWorld.setBlockTypeAt(x2, y2, z, BLK_LAVA);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private final PerlinNoise noise = new PerlinNoise(0);
    
    private static final long SEED_OFFSET = 142644289;
}