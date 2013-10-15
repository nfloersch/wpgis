/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.exporters;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.pepsoft.worldpainter.layers.Frost;

import static org.pepsoft.minecraft.Constants.*;

/**
 *
 * @author pepijn
 */
public class FrostExporter extends AbstractLayerExporter<Frost> implements SecondPassLayerExporter<Frost> {
    public FrostExporter() {
        super(Frost.INSTANCE, new FrostSettings());
    }
    
    @Override
    public List<Fixup> render(final Dimension dimension, final Rectangle area, final Rectangle exportedArea, final MinecraftWorld minecraftWorld) {
        final FrostSettings settings = (FrostSettings) getSettings();
        final boolean frostEverywhere = settings.isFrostEverywhere();
        final int mode = settings.getMode();
        final int maxHeight = dimension.getMaxHeight();
        final Random random = new Random(); // Only used for random snow height, so it's not a big deal if it's different every time
        for (int x = area.x; x < area.x + area.width; x++) {
            for (int y = area.y; y < area.y + area.height; y++) {
                if (frostEverywhere || dimension.getBitLayerValueAt(Frost.INSTANCE, x, y)) {
                    int previousBlockType = minecraftWorld.getBlockTypeAt(x, y, maxHeight - 1);
                    int leafBlocksEncountered = 0;
                    for (int height = (maxHeight - 2); height >= 0; height--) {
                        int blockType = minecraftWorld.getBlockTypeAt(x, y, height);
                        if (NO_SNOW_ON.contains(blockType)) {
                            previousBlockType = blockType;
                            continue;
                        } else {
                            if (blockType == BLK_STATIONARY_WATER) {
                                minecraftWorld.setBlockTypeAt(x, y, height, BLK_ICE);
                                break;
                            } else if (blockType == BLK_LEAVES) {
                                if (previousBlockType == BLK_AIR) {
                                    minecraftWorld.setBlockTypeAt(x, y, height + 1, BLK_SNOW);
                                }
                                leafBlocksEncountered++;
                                if (leafBlocksEncountered > 1) {
                                    break;
                                }
                            } else {
                                // Obliterate tall grass, 'cause there is too
                                // much of it, and leaving it in would look
                                // strange
                                if ((previousBlockType == BLK_AIR) || (previousBlockType == BLK_TALL_GRASS)) {
                                    switch (mode) {
                                        case 0:
                                            minecraftWorld.setBlockTypeAt(x, y, height + 1, BLK_SNOW);
                                            minecraftWorld.setDataAt(     x, y, height + 1, 0);
                                            break;
                                        case 1:
                                            minecraftWorld.setBlockTypeAt(x, y, height + 1, BLK_SNOW);
                                            minecraftWorld.setDataAt(     x, y, height + 1, random.nextInt(3));
                                            break;
                                        case 2:
                                            float diff = dimension.getHeightAt(x, y) + 0.5f - height;
                                            int snowHeight;
                                            if ((diff > 0.125f) && (diff <= 1.0f)) {
                                                if        (diff > 0.875f) {
                                                    snowHeight = 7;
                                                } else if (diff > 0.750f) {
                                                    snowHeight = 6;
                                                } else if (diff > 0.625f) {
                                                    snowHeight = 5;
                                                } else if (diff > 0.500f) {
                                                    snowHeight = 4;
                                                } else if (diff > 0.325f) {
                                                    snowHeight = 3;
                                                } else if (diff > 0.250f) {
                                                    snowHeight = 2;
                                                } else {
                                                    snowHeight = 1;
                                                }
                                            } else {
                                                snowHeight = 0;
                                            }
                                            if ((snowHeight > 0) && (! frostEverywhere)) {
                                                int surroundingSnowBlockCount = 0;
                                                for (int dx = -1; dx <= 1; dx++) {
                                                    for (int dy = -1; dy <= 1; dy++) {
                                                        if (((dx != 0) || (dy != 0)) && dimension.getBitLayerValueAt(Frost.INSTANCE, x + dx, y + dy)) {
                                                            surroundingSnowBlockCount++;
                                                        }
                                                    }
                                                }
                                                snowHeight = Math.max(Math.min(snowHeight, surroundingSnowBlockCount - 1), 0);
                                            }
                                            minecraftWorld.setBlockTypeAt(x, y, height + 1, BLK_SNOW);
                                            minecraftWorld.setDataAt(     x, y, height + 1, snowHeight);
                                            break;
                                    }
                                }
                                break;
                            }
                        }
                        previousBlockType = blockType;
                    }
                }
            }
        }
        return null;
    }

    private static final Set<Integer> NO_SNOW_ON = new HashSet<Integer>(Arrays.asList(
        BLK_AIR, BLK_ICE, BLK_LAVA, BLK_STATIONARY_LAVA, BLK_TORCH,
        BLK_DANDELION, BLK_ROSE, BLK_BROWN_MUSHROOM, BLK_RED_MUSHROOM,
        BLK_FIRE, BLK_TALL_GRASS, BLK_DEAD_SHRUBS, BLK_WOODEN_STAIRS,
        BLK_COBBLESTONE_STAIRS, BLK_BRICK_STAIRS, BLK_NETHER_BRICK_STAIRS,
        BLK_STONE_BRICK_STAIRS, BLK_SLAB, BLK_FENCE, BLK_FENCE_GATE,
        BLK_NETHER_BRICK_FENCE, BLK_WALL_SIGN, BLK_SIGN, BLK_VINES, BLK_SAPLING,
        BLK_WATER, BLK_BED, BLK_POWERED_RAILS, BLK_RAILS, BLK_DETECTOR_RAILS,
        BLK_COBWEB, BLK_PISTON_EXTENSION, BLK_CHEST, BLK_REDSTONE_WIRE,
        BLK_WHEAT, BLK_BURNING_FURNACE, BLK_WOODEN_DOOR, BLK_IRON_DOOR,
        BLK_IRON_BARS, BLK_LADDER, BLK_LEVER, BLK_STONE_PRESSURE_PLATE,
        BLK_WOODEN_PRESSURE_PLATE, BLK_REDSTONE_TORCH_OFF,
        BLK_REDSTONE_TORCH_ON, BLK_STONE_BUTTON, BLK_SNOW, BLK_CACTUS,
        BLK_SUGAR_CANE, BLK_CAKE, BLK_REDSTONE_REPEATER_OFF,
        BLK_REDSTONE_REPEATER_ON, BLK_TRAPDOOR, BLK_GLASS_PANE,
        BLK_PUMPKIN_STEM, BLK_MELON_STEM, BLK_LILY_PAD, BLK_NETHER_WART,
        BLK_ENCHANTMENT_TABLE, BLK_BREWING_STAND, BLK_END_PORTAL,
        BLK_END_PORTAL_FRAME, BLK_DRAGON_EGG, BLK_WOODEN_SLAB, BLK_COCOA_PLANT,
        BLK_SANDSTONE_STAIRS, BLK_ENDER_CHEST, BLK_TRIPWIRE_HOOK, BLK_TRIPWIRE,
        BLK_PINE_WOOD_STAIRS, BLK_BIRCH_WOOD_STAIRS, BLK_JUNGLE_WOOD_STAIRS,
        BLK_COBBLESTONE_WALL, BLK_FLOWER_POT, BLK_CARROTS, BLK_POTATOES,
        BLK_WOODEN_BUTTON, BLK_HEAD, BLK_ANVIL, BLK_TRAPPED_CHEST,
        BLK_WEIGHTED_PRESSURE_PLATE_HEAVY, BLK_WEIGHTED_PRESSURE_PLATE_LIGHT,
        BLK_REDSTONE_COMPARATOR_OFF, BLK_REDSTONE_COMPARATOR_ON,
        BLK_DAYLIGHT_SENSOR, BLK_ACTIVATOR_RAIL));
    
    public static class FrostSettings implements ExporterSettings<Frost> {
        @Override
        public boolean isApplyEverywhere() {
            return frostEverywhere;
        }

        @Override
        public Frost getLayer() {
            return Frost.INSTANCE;
        }

        public boolean isFrostEverywhere() {
            return frostEverywhere;
        }

        public void setFrostEverywhere(boolean frostEverywhere) {
            this.frostEverywhere = frostEverywhere;
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FrostSettings other = (FrostSettings) obj;
            if (this.frostEverywhere != other.frostEverywhere) {
                return false;
            }
            if (this.mode != other.mode) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + (this.frostEverywhere ? 1 : 0);
            hash = 23 * hash + mode;
            return hash;
        }

        @Override
        public FrostSettings clone() {
            try {
                return (FrostSettings) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        
        private boolean frostEverywhere;
        private int mode = MODE_SMOOTH;
        
        public static final int MODE_FLAT   = 0;
        public static final int MODE_RANDOM = 1;
        public static final int MODE_SMOOTH = 2;
        
        private static final long serialVersionUID = 2011060801L;
    }
}