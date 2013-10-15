/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.tunnel;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Material;
import org.pepsoft.util.MathUtils;
import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.pepsoft.worldpainter.heightMaps.NoiseHeightMap;

/**
 *
 * @author SchmitzP
 */
public class TunnelLayerExporter extends AbstractLayerExporter<TunnelLayer> implements SecondPassLayerExporter<TunnelLayer> {
    public TunnelLayerExporter(TunnelLayer layer) {
        super(layer);
        if (layer.getFloorNoise() != null) {
            floorNoise = new NoiseHeightMap(layer.getFloorNoise().getRange() * 2, layer.getFloorNoise().getScale() / 5, layer.getFloorNoise().getRoughness() + 1, FLOOR_NOISE_SEED_OFFSET);
            floorNoiseOffset = layer.getFloorNoise().getRange();
        } else {
            floorNoise = null;
            floorNoiseOffset = 0;
        }
        if (layer.getRoofNoise()!= null) {
            roofNoise = new NoiseHeightMap(layer.getRoofNoise().getRange() * 2, layer.getRoofNoise().getScale() / 5, layer.getRoofNoise().getRoughness() + 1, ROOF_NOISE_SEED_OFFSET);
            roofNoiseOffset = layer.getRoofNoise().getRange();
        } else {
            roofNoise = null;
            roofNoiseOffset = 0;
        }
//        if (layer.getWallNoise() != null) {
//            wallNoise = new PerlinNoise(layer.getWallNoise().getSeed());
//        } else {
//            wallNoise = null;
//        }
    }

    @Override
    public List<Fixup> render(Dimension dimension, Rectangle area, Rectangle exportedArea, MinecraftWorld world) {
        final TunnelLayer.Mode floorMode = layer.getFloorMode(), roofMode = layer.getRoofMode();
        final int floorWallDepth = layer.getFloorWallDepth(), roofWallDepth = layer.getRoofWallDepth(),
                floorLevel = layer.getFloorLevel(), roofLevel = layer.getRoofLevel(),
                maxWallDepth = Math.max(floorWallDepth, roofWallDepth) + 1,
                floorMin = layer.getFloorMin(), floorMax = layer.getFloorMax(), roofMin = layer.getRoofMin(), roofMax = layer.getRoofMax(),
                floodLevel = layer.getFloodLevel();
        final int minZ = dimension.isBottomless() ? 0 : 1, maxZ = dimension.getMaxHeight() - 1;
        final boolean removeWater = layer.isRemoveWater(), floodWithLava = layer.isFloodWithLava();
        final MixedMaterial floorMaterial = layer.getFloorMaterial(), wallMaterial = layer.getWallMaterial(), roofMaterial = layer.getRoofMaterial();
        if (floorNoise != null) {
            floorNoise.setSeed(dimension.getSeed());
        }
        if (roofNoise != null) {
            roofNoise.setSeed(dimension.getSeed());
        }
        if ((floorMaterial == null) && (wallMaterial == null) && (roofMaterial == null)) {
            // One pass: just remove blocks
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (dimension.getBitLayerValueAt(layer, x, y)) {
                        final int terrainHeight = dimension.getIntHeightAt(x, y);
                        int actualFloorLevel = calculateLevel(floorMode, floorLevel, terrainHeight, floorMin, floorMax, minZ, maxZ, (floorNoise != null) ? ((int) floorNoise.getHeight(x, y) - floorNoiseOffset) : 0);
                        int actualRoofLevel = calculateLevel(roofMode, roofLevel, terrainHeight, roofMin, roofMax, minZ, maxZ, (roofNoise != null) ? ((int) roofNoise.getHeight(x, y) - roofNoiseOffset) : 0);
                        if (actualRoofLevel <= actualFloorLevel) {
                            continue;
                        }
                        final float distanceToWall = dimension.getDistanceToEdge(layer, x, y, maxWallDepth) - 1;
                        final int floorLedgeHeight = calculateLedgeHeight(floorWallDepth, distanceToWall);
                        final int roofLedgeHeight = calculateLedgeHeight(roofWallDepth, distanceToWall);
                        actualFloorLevel += floorLedgeHeight;
                        actualRoofLevel -= roofLedgeHeight;
                        if (actualRoofLevel <= actualFloorLevel) {
                            continue;
                        }
                        final int waterLevel = dimension.getWaterLevelAt(x, y);
                        for (int z = Math.min(removeWater ? Math.max(terrainHeight, waterLevel) : terrainHeight, actualRoofLevel); z > actualFloorLevel; z--) {
                            if (removeWater || (z <= terrainHeight) || (z > waterLevel)) {
                                if (z <= floodLevel) {
                                    world.setMaterialAt(x, y, z, floodWithLava ? Material.LAVA : Material.WATER);
                                } else {
                                    world.setMaterialAt(x, y, z, Material.AIR);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Two passes: first place floor, wall and roof materials, then
            // excavate the interior
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (dimension.getBitLayerValueAt(layer, x, y)) {
                        int terrainHeight = dimension.getIntHeightAt(x, y);
                        int actualFloorLevel = calculateLevel(floorMode, floorLevel, terrainHeight, floorMin, floorMax, minZ, maxZ, (floorNoise != null) ? ((int) floorNoise.getHeight(x, y) - floorNoiseOffset) : 0);
                        int actualRoofLevel = calculateLevel(roofMode, roofLevel, terrainHeight, roofMin, roofMax, minZ, maxZ, (roofNoise != null) ? ((int) roofNoise.getHeight(x, y) - roofNoiseOffset) : 0);
                        if (actualRoofLevel <= actualFloorLevel) {
                            continue;
                        }
                        final float distanceToWall = dimension.getDistanceToEdge(layer, x, y, maxWallDepth) - 1;
                        final int floorLedgeHeight = calculateLedgeHeight(floorWallDepth, distanceToWall);
                        final int roofLedgeHeight = calculateLedgeHeight(roofWallDepth, distanceToWall);
                        actualFloorLevel += floorLedgeHeight;
                        actualRoofLevel -= roofLedgeHeight;
                        if (actualRoofLevel <= actualFloorLevel) {
                            continue;
                        }
                        int waterLevel = dimension.getWaterLevelAt(x, y);
                        boolean flooded = waterLevel > terrainHeight;
                        final int startZ = Math.min(removeWater ? Math.max(terrainHeight, waterLevel) : terrainHeight, actualRoofLevel);
                        for (int z = startZ; z > actualFloorLevel; z--) {
                            if ((floorLedgeHeight == 0) && (floorMaterial != null)) {
                                setIfSolid(world, x, y, z - 1, minZ, maxZ, floorMaterial, flooded, terrainHeight, waterLevel, removeWater);
                            }
                            if (wallMaterial != null) {
                                if (floorLedgeHeight > 0) {
                                    setIfSolid(world, x, y, z - 1, minZ, maxZ, wallMaterial, flooded, terrainHeight, waterLevel, removeWater);
                                }
                                if (roofLedgeHeight > 0) {
                                    setIfSolid(world, x, y, z + 1, minZ, maxZ, wallMaterial, flooded, terrainHeight, waterLevel, removeWater);
                                }
                            }
                            if ((roofLedgeHeight == 0) && (roofMaterial != null)) {
                                setIfSolid(world, x, y, z + 1, minZ, maxZ, roofMaterial, flooded, terrainHeight, waterLevel, removeWater);
                            }
                        }
                        if (wallMaterial != null) {
                            terrainHeight = dimension.getIntHeightAt(x - 1, y);
                            waterLevel = dimension.getWaterLevelAt(x - 1, y);
                            flooded = waterLevel > terrainHeight;
                            for (int z = startZ; z > actualFloorLevel; z--) {
                                setIfSolid(world, x - 1, y, z, minZ, maxZ, wallMaterial, flooded, terrainHeight, waterLevel, removeWater);
                            }
                            terrainHeight = dimension.getIntHeightAt(x, y - 1);
                            waterLevel = dimension.getWaterLevelAt(x, y - 1);
                            flooded = waterLevel > terrainHeight;
                            for (int z = startZ; z > actualFloorLevel; z--) {
                                setIfSolid(world, x, y - 1, z, minZ, maxZ, wallMaterial, flooded, terrainHeight, waterLevel, removeWater);
                            }
                            terrainHeight = dimension.getIntHeightAt(x + 1, y);
                            waterLevel = dimension.getWaterLevelAt(x + 1, y);
                            flooded = waterLevel > terrainHeight;
                            for (int z = startZ; z > actualFloorLevel; z--) {
                                setIfSolid(world, x + 1, y, z, minZ, maxZ, wallMaterial, flooded, terrainHeight, waterLevel, removeWater);
                            }
                            terrainHeight = dimension.getIntHeightAt(x, y + 1);
                            waterLevel = dimension.getWaterLevelAt(x, y + 1);
                            flooded = waterLevel > terrainHeight;
                            for (int z = startZ; z > actualFloorLevel; z--) {
                                setIfSolid(world, x, y + 1, z, minZ, maxZ, wallMaterial, flooded, terrainHeight, waterLevel, removeWater);
                            }
                        }
                    }
                }
            }
            
            // Second pass: excavate interior
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (dimension.getBitLayerValueAt(layer, x, y)) {
                        final int terrainHeight = dimension.getIntHeightAt(x, y);
                        int actualFloorLevel = calculateLevel(floorMode, floorLevel, terrainHeight, floorMin, floorMax, minZ, maxZ, (floorNoise != null) ? ((int) floorNoise.getHeight(x, y) - floorNoiseOffset) : 0);
                        int actualRoofLevel = calculateLevel(roofMode, roofLevel, terrainHeight, roofMin, roofMax, minZ, maxZ, (roofNoise != null) ? ((int) roofNoise.getHeight(x, y) - roofNoiseOffset) : 0);
                        if (actualRoofLevel <= actualFloorLevel) {
                            continue;
                        }
                        final float distanceToWall = dimension.getDistanceToEdge(layer, x, y, maxWallDepth) - 1;
                        final int floorLedgeHeight = calculateLedgeHeight(floorWallDepth, distanceToWall);
                        final int roofLedgeHeight = calculateLedgeHeight(roofWallDepth, distanceToWall);
                        actualFloorLevel += floorLedgeHeight;
                        actualRoofLevel -= roofLedgeHeight;
                        if (actualRoofLevel <= actualFloorLevel) {
                            continue;
                        }
                        final int waterLevel = dimension.getWaterLevelAt(x, y);
                        for (int z = actualRoofLevel; z > actualFloorLevel; z--) {
                            if (removeWater || (z <= terrainHeight) || (z > waterLevel)) {
                                if (z <= floodLevel) {
                                    world.setMaterialAt(x, y, z, floodWithLava ? Material.LAVA : Material.WATER);
                                } else {
                                    world.setMaterialAt(x, y, z, Material.AIR);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
//    private void excavateDisc(final MinecraftWorld world, final int x, final int y, final int z, int r, final Material materialAbove, final Material materialBesides, final Material materialBelow) {
//        GeometryUtil.visitFilledCircle(r, new GeometryVisitor() {
//            @Override
//            public boolean visit(int dx, int dy, float d) {
//                world.setMaterialAt(x + dx, y + dy, z, Material.AIR);
//                if (materialAbove != null) {
//                    setIfSolid(world, x + dx, y + dy, z - 1, 0, Integer.MAX_VALUE, materialAbove);
//                }
//                if (materialBesides != null) {
//                    setIfSolid(world, x + dx - 1, y + dy, z, 0, Integer.MAX_VALUE, materialBesides);
//                    setIfSolid(world, x + dx, y + dy - 1, z, 0, Integer.MAX_VALUE, materialBesides);
//                    setIfSolid(world, x + dx + 1, y + dy, z, 0, Integer.MAX_VALUE, materialBesides);
//                    setIfSolid(world, x + dx, y + dy + 1, z, 0, Integer.MAX_VALUE, materialBesides);
//                }
//                if (materialBelow != null) {
//                    setIfSolid(world, x + dx, y + dy, z + 1, 0, Integer.MAX_VALUE, materialBelow);
//                }
//                return true;
//            }
//        });
//    }
    
    public BufferedImage generatePreview(int width, int height, int waterLevel, int baseHeight, int heightDifference) {
        final TunnelLayer.Mode floorMode = layer.getFloorMode(), roofMode = layer.getRoofMode();
        final int floorWallDepth = layer.getFloorWallDepth(), roofWallDepth = layer.getRoofWallDepth(),
                floorLevel = layer.getFloorLevel(), roofLevel = layer.getRoofLevel(), tunnelExtent = width - 24,
                floorMin = layer.getFloorMin(), floorMax = layer.getFloorMax(), roofMin = layer.getRoofMin(), roofMax = layer.getRoofMax(),
                floodLevel = layer.getFloodLevel();
        final boolean removeWater = layer.isRemoveWater(), floodWithLava = layer.isFloodWithLava();
        final PerlinNoise noise = new PerlinNoise(0);
        final BufferedImage preview = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            // Clear the sky
            final int terrainHeight = MathUtils.clamp(0, (int) (Math.sin((x / (double) width * 1.5 + 1.25) * Math.PI) * heightDifference / 2 + heightDifference / 2 + baseHeight + noise.getPerlinNoise(x / 20.0) * 32 + noise.getPerlinNoise(x / 10.0) * 16 + noise.getPerlinNoise(x / 5.0) * 8), baseHeight + heightDifference - 1);
            for (int z = height - 1; z > terrainHeight; z--) {
                preview.setRGB(x, height - 1 - z, (z <= waterLevel) ? 0x0000ff : 0xffffff);
            }
            
            if (x <= tunnelExtent) {
                // Draw the tunnel
                int actualFloorLevel = calculateLevel(floorMode, floorLevel, terrainHeight, floorMin, floorMax, 1, height - 1, (floorNoise != null) ? ((int) floorNoise.getHeight(x, 0) - floorNoiseOffset) : 0);
                int actualRoofLevel = calculateLevel(roofMode, roofLevel, terrainHeight, roofMin, roofMax, 1, height - 1, (roofNoise != null) ? ((int) roofNoise.getHeight(x, 0) - roofNoiseOffset) : 0);
                if (actualRoofLevel > actualFloorLevel) {
                    final float distanceToWall = tunnelExtent - x;
                    final int floorLedgeHeight = calculateLedgeHeight(floorWallDepth, distanceToWall);
                    final int roofLedgeHeight = calculateLedgeHeight(roofWallDepth, distanceToWall);
                    actualFloorLevel += floorLedgeHeight;
                    actualRoofLevel = Math.min(actualRoofLevel - roofLedgeHeight, Math.max(terrainHeight, 62));
                    for (int z = actualFloorLevel + 1; z <= actualRoofLevel; z++) {
                        if (z <= floodLevel) {
                            if (floodWithLava) {
                                preview.setRGB(x, height - 1 - z, 0xff8000);
                            } else {
                                preview.setRGB(x, height - 1 - z, 0x0000ff);
                            }
                        } else {
                            if (z > terrainHeight) {
                                if (removeWater) {
                                    preview.setRGB(x, height - 1 - z, 0x7f7fff);
                                }
                            } else {
                                preview.setRGB(x, height - 1 - z, 0x7f7f7f);
                            }
                        }
                    }
                }
            }
        }
        return preview;
    }

    private int calculateLevel(TunnelLayer.Mode mode, int level, int terrainHeight, int minLevel, int maxLevel, int minZ, int maxZ, int offset) {
        switch (mode) {
            case CONSTANT_DEPTH:
                return MathUtils.clamp(minZ, MathUtils.clamp(minLevel, terrainHeight - level, maxLevel) + offset, maxZ);
            case FIXED_HEIGHT:
                return MathUtils.clamp(minZ, level + offset, maxZ);
            case INVERTED_DEPTH:
                return MathUtils.clamp(minZ, MathUtils.clamp(minLevel, level - (terrainHeight - level), maxLevel) + offset, maxZ);
            default:
                throw new InternalError();
        }
    }
    
    private int calculateLedgeHeight(int wallDepth, float distanceToWall) {
        if (distanceToWall > wallDepth) {
            return 0;
        } else {
            final float a = wallDepth - distanceToWall;
            return (int) (wallDepth - Math.sqrt(wallDepth * wallDepth - a * a) + 0.5);
        }
    }
    
    private void setIfSolid(MinecraftWorld world, int x, int y, int z, int minZ, int maxZ, MixedMaterial material, boolean flooded, int terrainHeight, int waterLevel, boolean removeWater) {
        if ((z >= minZ) && (z <= maxZ)) {
            if (removeWater || (! flooded) || (z <= terrainHeight) || (z > waterLevel)) {
                final int existingBlock = world.getBlockTypeAt(x, y, z);
                if ((existingBlock != Constants.BLK_AIR)
                        && (! Constants.INSUBSTANTIAL_BLOCKS.contains(existingBlock))) {
                    // The coordinates are within bounds and the existing block is solid
                    world.setMaterialAt(x, y, z, material.getMaterial(MATERIAL_SEED, x, y, z));
                }
            }
        }
    }
    
    private final NoiseHeightMap floorNoise, roofNoise;
    private final int floorNoiseOffset, roofNoiseOffset;
//    private final PerlinNoise wallNoise;
    
    private static final long MATERIAL_SEED = 0x688b2af137c77e0cL;
    private static final long FLOOR_NOISE_SEED_OFFSET = 177766561L;
    private static final long ROOF_NOISE_SEED_OFFSET = 184818453L;
    
    enum RenderState {AIR, WATER, LAVA, UNDERGROUND}
}