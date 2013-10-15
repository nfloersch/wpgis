/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.exporters;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Point3i;

import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.TileEntity;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.objects.WPObject;

import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.minecraft.Material;
import org.pepsoft.util.Box;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.LightingCalculator;
import org.pepsoft.worldpainter.layers.Frost;
import static org.pepsoft.worldpainter.objects.WPObject.*;

/**
 *
 * @author pepijn
 */
public abstract class WPObjectExporter<L extends Layer> extends AbstractLayerExporter<L> {
    public WPObjectExporter(L layer) {
        super(layer);
    }

    public WPObjectExporter(L layer, ExporterSettings<L> defaultSettings) {
        super(layer, defaultSettings);
    }
    
    protected static void renderObject(MinecraftWorld world, Dimension dimension, WPObject object, int x, int y, int z, Placement placement) {
        final Point3i dim = object.getDimensions();
        final Point3i offset = object.getAttribute(ATTRIBUTE_OFFSET, new Point3i());
        final int undergroundMode = object.getAttribute(ATTRIBUTE_UNDERGROUND_MODE, VALUE_ALL);
        final boolean bottomless = dimension.isBottomless();
        if ((z + offset.z + dim.z - 1) >= world.getMaxHeight()) {
            // Object doesn't fit in the world vertically
            return;
        }
//        System.out.println("Object dimensions: " + dim + ", origin: " + orig);
        for (int dx = 0; dx < dim.x; dx++) {
            for (int dy = 0; dy < dim.y; dy++) {
                int xx = x + dx + offset.x;
                int yy = y + dy + offset.y;
                int terrainHeight = dimension.getIntHeightAt(xx, yy);
                for (int dz = 0; dz < dim.z; dz++) {
                    if (object.getMask(dx, dy, dz)) {
                        int zz = z + dz + offset.z;
                        if (bottomless ? (zz < 0) : (zz < 1)) {
                            continue;
                        } else {
                            int existingBlockType = world.getBlockTypeAt(xx, yy, zz);
                            if (zz <= terrainHeight) {
                                switch (undergroundMode) {
                                    case VALUE_ALL:
                                        // Replace every block
                                        world.setMaterialAt(xx, yy, zz, object.getMaterial(dx, dy, dz));
                                        break;
                                    case VALUE_SOLID:
                                        // Only replace if object block is solid
                                        Material objectMaterial = object.getMaterial(dx, dy, dz);
                                        if (! VERY_INSUBSTANTIAL_BLOCKS.contains(objectMaterial.getBlockType())) {
                                            world.setMaterialAt(xx, yy, zz, objectMaterial);
                                        }
                                        break;
                                    case VALUE_NONE:
                                        // Only replace less solid blocks
                                        if (VERY_INSUBSTANTIAL_BLOCKS.contains(existingBlockType)) {
                                            world.setMaterialAt(xx, yy, zz, object.getMaterial(dx, dy, dz));
                                        }
                                        break;
                                }
                            } else {
                                // Above ground only replace less solid blocks
                                if (VERY_INSUBSTANTIAL_BLOCKS.contains(existingBlockType)) {
                                    world.setMaterialAt(xx, yy, zz, object.getMaterial(dx, dy, dz));
                                }
                            }
                        }
                    }
                }
            }
        }
        List<Entity> entities = object.getEntities();
        if (entities != null) {
            for (Entity entity: entities) {
                double[] pos = entity.getPos();
                world.addEntity((int) (x + pos[0] + 0.5), (int) (y + pos[2] + 0.5), (int) (z + pos[1] + 0.5), entity);
            }
        }
        List<TileEntity> tileEntities = object.getTileEntities();
        if (tileEntities != null) {
            for (TileEntity tileEntity: tileEntities) {
                world.addTileEntity(x + tileEntity.getX(), y + tileEntity.getZ(), z + tileEntity.getY(), tileEntity);
            }
        }
    }
    
    protected static boolean isRoom(final MinecraftWorld world, final Dimension dimension, final WPObject object, final int x, final int y, final int z, final Placement placement) {
        final Point3i dimensions = object.getDimensions();
        final Point3i offset = object.getAttribute(ATTRIBUTE_OFFSET, new Point3i());
        final int collisionMode = object.getAttribute(ATTRIBUTE_COLLISION_MODE, VALUE_SOLID);
        final boolean allowConnectingBlocks = false;
        // Check if the object fits vertically
        if ((z + dimensions.z - 1 + offset.z) >= world.getMaxHeight()) {
            return false;
        }
        if ((placement == Placement.ON_LAND) && (collisionMode != VALUE_NONE)) {
            // Check block by block whether there is room
            for (int dx = 0; dx < dimensions.x; dx++) {
                for (int dy = 0; dy < dimensions.y; dy++) {
                    final int worldX = x + dx + offset.x, worldY = y + dy + offset.y;
                    final int terrainHeight = dimension.getIntHeightAt(worldX, worldY);
                    final int minZ = Math.max(terrainHeight - (z + offset.z) + 1, 0);
                    for (int dz = minZ; dz < dimensions.z; dz++) {
                        if (object.getMask(dx, dy, dz)) {
                            final int objectBlock = object.getMaterial(dx, dy, dz).getBlockType();
                            if (! VERY_INSUBSTANTIAL_BLOCKS.contains(objectBlock)) {
                                final int worldZ = z + dz + offset.z;
                                if ((collisionMode == VALUE_ALL)
                                        ? (! AIR_AND_FLUIDS.contains(world.getBlockTypeAt(worldX, worldY, worldZ)))
                                        : (! VERY_INSUBSTANTIAL_BLOCKS.contains(world.getBlockTypeAt(worldX, worldY, worldZ)))) {
                                    // The block is above ground, it is present in the
                                    // custom object, is substantial, and there is already a
                                    // substantial block at the same location in the world;
                                    // there is no room for this object
                                    if (logger.isLoggable(Level.FINER)) {
                                        logger.finer("No room for object " + object.getName() + " @ " + x + "," + y + "," + z + " with placement " + placement + " due to collision with existing above ground block of type " + BLOCK_TYPE_NAMES[world.getBlockTypeAt(worldX, worldY, worldZ)]);
                                    }
                                    return false;
                                }
                                if ((! allowConnectingBlocks) && wouldConnect(world, worldX, worldY, worldZ, objectBlock)) {
                                    if (logger.isLoggable(Level.FINER)) {
                                        logger.finer("No room for object " + object.getName() + " @ " + x + "," + y + "," + z + " with placement " + placement + " because it would cause a connecting block");
                                    }
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        } else if (placement == Placement.FLOATING) {
            // When floating on fluid, the object is not allowed to collide
            // with the floor
            for (int dx = 0; dx < dimensions.x; dx++) {
                for (int dy = 0; dy < dimensions.y; dy++) {
                    final int worldX = x + dx + offset.x, worldY = y + dy + offset.y;
                    final int terrainHeight = dimension.getIntHeightAt(worldX, worldY);
                    for (int dz = 0; dz < dimensions.z; dz++) {
                        if (object.getMask(dx, dy, dz)) {
                            final int worldZ = z + dz + offset.z;
                            if ((worldZ <= terrainHeight) && (! VERY_INSUBSTANTIAL_BLOCKS.contains(object.getMaterial(dx, dy, dz).getBlockType()))) {
                                // A solid block in the object collides with
                                // the floor
                                if (logger.isLoggable(Level.FINER)) {
                                    logger.finer("No room for object " + object.getName() + " @ " + x + "," + y + "," + z + " with placement " + placement + " due to collision with floor");
                                }
                                return false;
                            } else if ((worldZ > terrainHeight) && (collisionMode != VALUE_NONE)) {
                                final int objectBlock = object.getMaterial(dx, dy, dz).getBlockType();
                                if (! VERY_INSUBSTANTIAL_BLOCKS.contains(objectBlock)) {
                                    if ((collisionMode == VALUE_ALL)
                                            ? (! AIR_AND_FLUIDS.contains(world.getBlockTypeAt(worldX, worldY, worldZ)))
                                            : (! VERY_INSUBSTANTIAL_BLOCKS.contains(world.getBlockTypeAt(worldX, worldY, worldZ)))) {
                                        // The block is present in the custom object, is
                                        // substantial, and there is already a
                                        // substantial block at the same location in the
                                        // world; there is no room for this object
                                        if (logger.isLoggable(Level.FINER)) {
                                            logger.finer("No room for object " + object.getName() + " @ " + x + "," + y + "," + z + " with placement " + placement + " due to collision with existing above ground block of type " + BLOCK_TYPE_NAMES[world.getBlockTypeAt(worldX, worldY, worldZ)]);
                                        }
                                        return false;
                                    }
                                    if ((! allowConnectingBlocks) && wouldConnect(world, worldX, worldY, worldZ, objectBlock)) {
                                        if (logger.isLoggable(Level.FINER)) {
                                            logger.finer("No room for object " + object.getName() + " @ " + x + "," + y + "," + z + " with placement " + placement + " because it would cause a connecting block");
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("There is room for object " + object.getName() + " @ " + x + "," + y + "," + z + " with placement " + placement);
        }
        return true;
    }
    
    /**
     * Determine whether placing a block of the specified type at the specified
     * location in the specified world would cause the block to connect to
     * surrounding blocks (for instance a fence block to a solid block, or
     * another fence block, but not another fence of a different type).
     */
    private static boolean wouldConnect(MinecraftWorld world, int worldX, int worldY, int worldZ, int objectBlock) {
        if (wouldConnect(objectBlock, world.getBlockTypeAt(worldX - 1, worldY, worldZ))) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(BLOCK_TYPE_NAMES[objectBlock] + " @ " + worldX + "," + worldY + "," + worldZ + " would connect to " + BLOCK_TYPE_NAMES[world.getBlockTypeAt(worldX - 1, worldY, worldZ)] + " @ dx = -1");
            }
            return true;
        } else if (wouldConnect(objectBlock, world.getBlockTypeAt(worldX, worldY - 1, worldZ))) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(BLOCK_TYPE_NAMES[objectBlock] + " @ " + worldX + "," + worldY + "," + worldZ + " would connect to " + BLOCK_TYPE_NAMES[world.getBlockTypeAt(worldX, worldY - 1, worldZ)] + " @ dy = -1");
            }
            return true;
        } else if (wouldConnect(objectBlock, world.getBlockTypeAt(worldX + 1, worldY, worldZ))) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(BLOCK_TYPE_NAMES[objectBlock] + " @ " + worldX + "," + worldY + "," + worldZ + " would connect to " + BLOCK_TYPE_NAMES[world.getBlockTypeAt(worldX + 1, worldY, worldZ)] + " @ dx = 1");
            }
            return true;
        } else if (wouldConnect(objectBlock, world.getBlockTypeAt(worldX, worldY + 1, worldZ))) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(BLOCK_TYPE_NAMES[objectBlock] + " @ " + worldX + "," + worldY + "," + worldZ + " would connect to " + BLOCK_TYPE_NAMES[world.getBlockTypeAt(worldX, worldY + 1, worldZ)] + " @ dy = 1");
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Determine whether two blocks would connect to each other in some way
     * (forming a fence, for instance).
     */
    private static boolean wouldConnect(int blockTypeOne, int blockTypeTwo) {
        return ((blockTypeOne == BLK_FENCE) && ((blockTypeTwo == BLK_FENCE) || (! BLOCK_TRANSPARENCY.containsKey(blockTypeTwo))))
            || ((blockTypeOne == BLK_NETHER_BRICK_FENCE) && ((blockTypeTwo == BLK_NETHER_BRICK_FENCE) || (! BLOCK_TRANSPARENCY.containsKey(blockTypeTwo))))
            || ((blockTypeOne == BLK_COBBLESTONE_WALL) && ((blockTypeTwo == BLK_COBBLESTONE_WALL) || (! BLOCK_TRANSPARENCY.containsKey(blockTypeTwo))))
            || ((blockTypeOne == BLK_IRON_BARS) && ((blockTypeTwo == BLK_IRON_BARS) || (! BLOCK_TRANSPARENCY.containsKey(blockTypeTwo))))
            || ((blockTypeOne == BLK_GLASS_PANE) && ((blockTypeTwo == BLK_GLASS_PANE) || (! BLOCK_TRANSPARENCY.containsKey(blockTypeTwo))));
    }
    
    private static Box getBounds(WPObject object, int x, int y, int z) {
        Point3i dimensions = object.getDimensions();
        Point3i offset = object.getAttribute(ATTRIBUTE_OFFSET, new Point3i());
        return new Box(x + offset.x, x + offset.x + dimensions.x - 1,
                y + offset.y, y + offset.y + dimensions.y - 1,
                z + offset.z, z + offset.z + dimensions.z - 1);
    }

    private static final Set<Integer> AIR_AND_FLUIDS = new HashSet<Integer>(Arrays.asList(BLK_AIR, BLK_WATER, BLK_STATIONARY_WATER, BLK_LAVA, BLK_STATIONARY_LAVA));
    private static final Logger logger = Logger.getLogger(WPObjectExporter.class.getName());

    public static class WPObjectFixup implements Fixup {
        public WPObjectFixup(WPObject object, int x, int y, int z, Placement placement) {
            this.object = object;
            this.x = x;
            this.y = y;
            this.z = z;
            this.placement = placement;
        }

        @Override
        public void fixup(MinecraftWorld world, Dimension dimension) {
            // Recheck whether there is room
            if (isRoom(world, dimension, object, x, y, z, placement)) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Placing custom object " + object.getName() + " @ " + x + "," + y + "," + z + " in fixup");
                }
                WPObjectExporter.renderObject(world, dimension, object, x, y, z, placement);
                
                // Reapply the Frost layer to the area, if necessary
                frostExporter.setSettings(dimension.getLayerSettings(Frost.INSTANCE));
                Point3i offset = object.getAttribute(WPObject.ATTRIBUTE_OFFSET, new Point3i());
                Point3i dim = object.getDimensions();
                Rectangle area = new Rectangle(x + offset.x, y + offset.y, dim.x, dim.y);
                frostExporter.render(dimension, area, null, world);
                
                // Fixups are done *after* lighting,
                // so we have to relight the area
                recalculateLight(world, getBounds(object, x, y, z));
            } else if (logger.isLoggable(Level.FINER)) {
                logger.finer("No room for custom object " + object.getName() + " @ " + x + "," + y + "," + z + " in fixup");
            }
        }

        private void recalculateLight(final MinecraftWorld world, final Box lightBox) {
            // Quick and dirty; only recalculate secondary light, and only
            // inside the bounds of the object. Known downsides: light emitting
            // blocks in the object will have no effect, and the object will not
            // cut off light to its surroundings
            // TODO: this is not good enough! The preexisting primary light
            // overrides everything and makes it too bright. It has to be reset
            // first
            LightingCalculator lightingCalculator = new LightingCalculator(world);
            // Transpose coordinates from WP to MC coordinate system. Also
            // expand the box to light around it and try to account for uneven
            // terrain underneath the object
            lightingCalculator.setDirtyArea(new Box(lightBox.getX1() - 1, lightBox.getX2() + 1, Math.max(lightBox.getZ1() - 5, 0), Math.min(lightBox.getZ2() + 1, world.getMaxHeight() - 1), lightBox.getY1() - 1, lightBox.getY2() + 1));
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Recalculating light in " + lightingCalculator.getDirtyArea());
            }
            lightingCalculator.recalculatePrimaryLight();
            while (lightingCalculator.calculateSecondaryLight());
        }

        private final WPObject object;
        private final int x, y, z;
        private final Placement placement;

        private static final FrostExporter frostExporter = new FrostExporter();
    }

    public enum Placement { NONE, FLOATING, ON_LAND }
}