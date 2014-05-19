/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.exporters;

import java.awt.Rectangle;
import java.util.List;
import static org.pepsoft.minecraft.Material.*;
import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.MixedMaterial.Row;
import org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.pepsoft.worldpainter.layers.River;
import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import org.pepsoft.worldpainter.util.GeometryUtil;
import org.pepsoft.worldpainter.util.GeometryUtil.GeometryVisitor;

/**
 *
 * @author pepijn
 */
public class RiverExporter extends AbstractLayerExporter<River> implements SecondPassLayerExporter<River> {
    public RiverExporter() {
        super(River.INSTANCE, new RiverSettings());
    }
    
    @Override
    public List<Fixup> render(final Dimension dimension, final Rectangle area, final Rectangle exportedArea, final MinecraftWorld minecraftWorld) {
        final RiverSettings settings = new RiverSettings();
        final int shoreHeight = settings.getShoreHeight();
        final boolean shore = shoreHeight > 0;
        final MixedMaterial riverBedMaterial = settings.getRiverBedMaterial();
        final int riverConnectionRadius = settings.getRiverConnectionRadius();
        final int maxDepth = shoreHeight + settings.getMaxDepth();
        final int depthVariation = settings.getMaxDepth() - settings.getMinDepth();
        final long seed = dimension.getSeed();
        if (floorNoise.getSeed() != seed) {
            floorNoise.setSeed(seed);
        }
        
        // First pass. Dig out the riverbed and flood the river
        for (int x = area.x; x < area.x + area.width; x++) {
            for (int y = area.y; y < area.y + area.height; y++) {
                if (dimension.getBitLayerValueAt(River.INSTANCE, x, y)) {
                    final float distanceToShore = dimension.getDistanceToEdge(River.INSTANCE, x, y, maxDepth);
                    final int depth = (int) Math.min(distanceToShore, maxDepth - (int) ((floorNoise.getPerlinNoise(x / TINY_BLOBS, y / TINY_BLOBS) + 0.5f) * depthVariation + 0.5f));
                    final int terrainHeight = dimension.getIntHeightAt(x, y);
                    final int waterLevel = dimension.getWaterLevelAt(x, y);
                    if ((waterLevel > terrainHeight) && (! dimension.getBitLayerValueAt(FloodWithLava.INSTANCE, x, y))) {
                        // Already flooded. Special processing: just dig out the
                        // river bed
                        for (int dz = 0; dz >= (-depth - 1); dz--) {
                            final int z = waterLevel + dz;
                            if (dz > -depth) {
                                minecraftWorld.setMaterialAt(x, y, z, STATIONARY_WATER);
                            } else if (z <= terrainHeight) {
                                minecraftWorld.setMaterialAt(x, y, z, riverBedMaterial.getMaterial(seed, x, y, z));
                            }
                        }
                    } else {
                        // Dry land (although the river connection flooding
                        // algorithm may have placed water, so we still have to
                        // check for it
                        for (int dz = 0; dz >= (-depth - 1); dz--) {
                            final int z = terrainHeight + dz;
                            if (z <= 0) {
                                break;
                            }
                            if (dz <= -depth) {
                                // River bed
                                int existingBlockType = minecraftWorld.getBlockTypeAt(x, y, z);
                                if (! VERY_INSUBSTANTIAL_BLOCKS.contains(existingBlockType)) {
                                    minecraftWorld.setMaterialAt(x, y, z, riverBedMaterial.getMaterial(seed, x, y, z));
                                }
                            } else if (dz > -shoreHeight) {
                                // The air above the river
                                int existingBlockType = minecraftWorld.getBlockTypeAt(x, y, z);
                                if ((existingBlockType != BLK_WATER) && (existingBlockType != BLK_STATIONARY_WATER)) {
                                    minecraftWorld.setMaterialAt(x, y, z, AIR);
                                }
                            } else if (dz == -shoreHeight) {
                                // The surface of the river
                                minecraftWorld.setMaterialAt(x, y, z, STATIONARY_WATER);
                            } else {
                                // The river itself below the surface
                                minecraftWorld.setMaterialAt(x, y, z, STATIONARY_WATER);
                            }
                        }
                    }
                }
            }
        }
        
        // Second pass. Look for bodies of water that connect to rivers and
        // flood the connecting areas
        if (shore) {
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    final int waterLevel = dimension.getWaterLevelAt(x, y);
                    if (dimension.getBitLayerValueAt(River.INSTANCE, x, y) // River
                            && (waterLevel > dimension.getIntHeightAt(x, y)) // Flooded
                            && (dimension.getFloodedCount(x, y, 1, false) < 9)) { // Not every surrounding block flooded
                        // We're at the edge of a flooded area. Check
                        // whether it needs to be expanded to flood the
                        // connecting riverbed, where the water will be
                        // lower (if the shore height is higher than zero)
                        final int finalX = x, finalY = y;
                        // First see if there is any river around with a
                        // water level that's higher
                        final boolean[] higherRiverAround = new boolean[1];
                        GeometryUtil.visitFilledCircle(riverConnectionRadius, new GeometryVisitor() {
                            @Override
                            public boolean visit(int dx, int dy, float d) {
                                final int x = finalX + dx, y = finalY + dy;
                                if ((dimension.getBitLayerValueAt(River.INSTANCE, x, y))
                                        && ((dimension.getIntHeightAt(x, y) - shoreHeight) > waterLevel)) {
                                    higherRiverAround[0] = true;
                                    return false;
                                } else {
                                    return true;
                                }
                            }
                        });
                        if (higherRiverAround[0]) {
                            // There is a river near with a water level
                            // that's higher, so flood the surroundings to
                            // the height of the body of water
                            GeometryUtil.visitFilledCircle(riverConnectionRadius, new GeometryVisitor() {
                                @Override
                                public boolean visit(int dx, int dy, float d) {
                                    final int x = finalX + dx, y = finalY + dy;
                                    if ((dimension.getBitLayerValueAt(River.INSTANCE, x, y))
                                            && (dimension.getWaterLevelAt(x, y) < waterLevel)) {
                                        int z = waterLevel;
                                        int existingBlockType = minecraftWorld.getBlockTypeAt(x, y, z);
                                        while ((z > 0)
                                                && (existingBlockType != BLK_WATER)
                                                && (existingBlockType != BLK_STATIONARY_WATER)
                                                && (existingBlockType != BLK_ICE)
                                                && VERY_INSUBSTANTIAL_BLOCKS.contains(existingBlockType)) {
                                            minecraftWorld.setMaterialAt(x, y, z, STATIONARY_WATER);
                                            z--;
                                            existingBlockType = minecraftWorld.getBlockTypeAt(x, y, z);
                                        }
                                    }
                                    return true;
                                }
                            });
                        } else {
                            // There is no river near with a water level
                            // that's higher, so flood the surroundings
                            // sloping down just in case there's *lower*
                            // river water which we want to connect to a bit
                            // less abruptly than with a sheer wall of water
                            GeometryUtil.visitFilledCircle(riverConnectionRadius, new GeometryVisitor() {
                                @Override
                                public boolean visit(int dx, int dy, float d) {
                                    final int x = finalX + dx, y = finalY + dy;
                                    if ((dimension.getBitLayerValueAt(River.INSTANCE, x, y))
                                            && (dimension.getWaterLevelAt(x, y) < waterLevel)) {
                                        int z = waterLevel - ((int) d);
                                        int existingBlockType = minecraftWorld.getBlockTypeAt(x, y, z);
                                        while ((z > 0)
                                                && (existingBlockType != BLK_WATER)
                                                && (existingBlockType != BLK_STATIONARY_WATER)
                                                && (existingBlockType != BLK_ICE)
                                                && VERY_INSUBSTANTIAL_BLOCKS.contains(existingBlockType)) {
                                            minecraftWorld.setMaterialAt(x, y, z, STATIONARY_WATER);
                                            z--;
                                            existingBlockType = minecraftWorld.getBlockTypeAt(x, y, z);
                                        }
                                    }
                                    return true;
                                }
                            });
                        }
                    }
                }
            }
        }
        
        // Third pass. Make sure all the water connects to at least one
        // surrounding water column, to make it connect visually, and mitigate
        // overflowing problems
        for (int x = area.x; x < area.x + area.width; x++) {
            for (int y = area.y; y < area.y + area.height; y++) {
                if (dimension.getBitLayerValueAt(River.INSTANCE, x, y)) {
                    int lowestWaterHeight = -1;
                    for (int z = dimension.getIntHeightAt(x, y); z > 0; z--) {
                        if (((minecraftWorld.getBlockTypeAt(x, y, z) == BLK_WATER) || (minecraftWorld.getBlockTypeAt(x, y, z) == BLK_STATIONARY_WATER)) && (minecraftWorld.getBlockTypeAt(x, y, z - 1) != BLK_WATER) && (minecraftWorld.getBlockTypeAt(x, y, z - 1) != BLK_STATIONARY_WATER)) {
                            lowestWaterHeight = z;
                            break;
                        }
                    }
                    if (lowestWaterHeight != -1) {
                        // There is water in this column. Raise the water in
                        // surrounding columns to at least the height of the
                        // lowest water block in this column.
                        raiseWaterTo(dimension, minecraftWorld, x + 1, y, lowestWaterHeight - 1);
                        raiseWaterTo(dimension, minecraftWorld, x, y + 1, lowestWaterHeight - 1);
                        raiseWaterTo(dimension, minecraftWorld, x - 1, y, lowestWaterHeight - 1);
                        raiseWaterTo(dimension, minecraftWorld, x, y - 1, lowestWaterHeight - 1);
                    }
                }
            }
        }
        return null;
    }

    private void raiseWaterTo(Dimension dimension, MinecraftWorld world, int x, int y, int raiseTo) {
        if (! dimension.getBitLayerValueAt(River.INSTANCE, x, y)) {
            return;
        }
        int waterLevel = -1;
        for (int z = dimension.getIntHeightAt(x, y); z > 0; z--) {
            int existingBlockType = world.getBlockTypeAt(x, y, z);
            if (existingBlockType == BLK_AIR) {
                continue;
            } else if ((existingBlockType == BLK_WATER) || (existingBlockType == BLK_STATIONARY_WATER)) {
                waterLevel = z;
                break;
            } else {
                // No water in this column (at least not on the surface)
                return;
            }
        }
        if (waterLevel != -1) {
            for (int z = waterLevel; z <= raiseTo; z++) {
                world.setMaterialAt(x, y, z, STATIONARY_WATER);
            }
        }
    }
    
    private final PerlinNoise floorNoise = new PerlinNoise(0);

    public static class RiverSettings implements ExporterSettings<River> {
        public RiverSettings() {
            riverBedMaterial = new MixedMaterial(
                "Riverbed",
                new Row[] {new Row(GRASS, 650, 1.0f),
                    new Row(GRAVEL, 200, 1.0f),
                    new Row(CLAY, 50, 1.0f),
                    new Row(STONE, 100, 1.0f)},
                AbstractMinecraft1_2BiomeScheme.BIOME_RIVER,
                false,
                3.0f,
                0x0000ff);
        }
        
        @Override
        public boolean isApplyEverywhere() {
            return false;
        }

        public int getShoreHeight() {
            return shoreHeight;
        }

        public void setShoreHeight(int shoreHeight) {
            this.shoreHeight = shoreHeight;
        }

        public int getMinDepth() {
            return minDepth;
        }

        public void setMinDepth(int minDepth) {
            this.minDepth = minDepth;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        public void setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
        }

        public MixedMaterial getRiverBedMaterial() {
            return riverBedMaterial;
        }

        public void setRiverBedMaterial(MixedMaterial riverBedMaterial) {
            this.riverBedMaterial = riverBedMaterial;
        }

        public int getRiverConnectionRadius() {
            return riverConnectionRadius;
        }

        public void setRiverConnectionRadius(int riverConnectionRadius) {
            this.riverConnectionRadius = riverConnectionRadius;
        }

        @Override
        public River getLayer() {
            return River.INSTANCE;
        }

        @Override
        public RiverSettings clone() {
            try {
                return (RiverSettings) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        
        private int shoreHeight = 2, minDepth = 4, maxDepth = 7, riverConnectionRadius = 10;
        private MixedMaterial riverBedMaterial;

        private static final long serialVersionUID = 1L;
    }
}