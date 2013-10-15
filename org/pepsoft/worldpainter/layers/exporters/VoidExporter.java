/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.exporters;

import java.awt.Rectangle;
import java.util.List;
import static org.pepsoft.minecraft.Constants.*;

import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.pepsoft.worldpainter.layers.Void;

import static org.pepsoft.minecraft.Material.*;
import org.pepsoft.util.MathUtils;
import static org.pepsoft.worldpainter.Constants.*;

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

    private void processEdgeColumn(final Dimension dimension, final int x, final int y, final MinecraftWorld minecraftWorld) {
        final int maxHeight = minecraftWorld.getMaxHeight();
//        // Remove a cone shaped volume of material
//        final float terrainHeight = dimension.getHeightAt(x, y);
//        GeometryUtil.visitFilledCircle(64, new GeometryUtil.GeometryVisitor() {
//            @Override
//            public boolean visit(int dx, int dy, float d) {
//                int bottom = (int) (terrainHeight - d + 0.5f);
//                for (int z = bottom; z > 0; z--) {
//                    minecraftWorld.setMaterialAt(x, y, z, AIR);
//                }
//                return true;
//            }
//        });
        final int r = 3;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                if ((dx != 0) || (dy != 0)) {
                    final int x2 = x + dx, y2 = y + dy;
                    final float distance = MathUtils.getDistance(dx, dy);
                    final float height = dimension.getHeightAt(x2, y2);
                    final int depth = (int) (height / Math.pow(2, distance + noise.getPerlinNoise(x2 / SMALL_BLOBS, y2 / SMALL_BLOBS)) + 0.5f);
                    for (int z = 0; z < depth; z++) {
                        minecraftWorld.setMaterialAt(x2, y2, z, AIR);
                    }
                    if (((dx == 0) && ((dy == -1) || (dy == 1))) || ((dy == 0) && ((dx == -1) || (dx == 1)))) {
                        // TODO: do this for the other columns as well
                        for (int z = 0; z < maxHeight; z++) {
                            final int existingBlockType = minecraftWorld.getBlockTypeAt(x2, y2, z);
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