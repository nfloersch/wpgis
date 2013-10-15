/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.bo2;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Point3i;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.pepsoft.worldpainter.layers.Bo2Layer;
import org.pepsoft.worldpainter.layers.exporters.WPObjectExporter;
import org.pepsoft.worldpainter.objects.MirroredObject;
import org.pepsoft.worldpainter.objects.RotatedObject;
import org.pepsoft.worldpainter.objects.WPObject;

import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import static org.pepsoft.worldpainter.objects.WPObject.*;

/**
 *
 * @author pepijn
 */
public class Bo2LayerExporter extends WPObjectExporter<Bo2Layer> implements SecondPassLayerExporter<Bo2Layer> {
    public Bo2LayerExporter(Bo2Layer layer) {
        super(layer);
    }
    
    @Override
    public List<Fixup> render(final Dimension dimension, Rectangle area, Rectangle exportedArea, MinecraftWorld minecraftWorld) {
        Bo2ObjectProvider objectProvider = layer.getObjectProvider();
        int maxHeight = dimension.getMaxHeight() - 1;
        List<Fixup> fixups = new ArrayList<Fixup>();
        for (int chunkX = area.x; chunkX < area.x + area.width; chunkX += 16) {
            for (int chunkY = area.y; chunkY < area.y + area.height; chunkY += 16) {
                // Set the seed and randomizer according to the chunk
                // coordinates to make sure the chunk is always rendered the
                // same, no matter how often it is rendered
                long seed = dimension.getSeed() + (chunkX >> 4) * 65537 + (chunkY >> 4) * 4099;
                Random random = new Random(seed);
                if (objectProvider instanceof Bo2ObjectTube) {
                    ((Bo2ObjectTube) objectProvider).setSeed(seed);
                }
                for (int x = chunkX; x < chunkX + 16; x++) {
objectLoop:         for (int y = chunkY; y < chunkY + 16; y++) {
                        int height = dimension.getIntHeightAt(x, y);
                        if ((height == -1) || (height >= maxHeight)) {
                            // height == -1 means no tile present
                            continue;
                        }
                        int strength = dimension.getLayerValueAt(layer, x, y);
                        if ((strength > 0) && (random.nextInt(1280) <= strength * strength)) {
                            WPObject object = objectProvider.getObject();
                            Placement placement = getPlacement(minecraftWorld, dimension, x, y, height + 1, object, random);
                            if (placement == Placement.NONE) {
                                continue;
                            }
//                            if (height < (maxHeight - 1)) {
//                                // Check that there is space around the stem (just assuming
//                                // the object is a tree with a one block trunk)
//                                for (int dx = -1; dx <= 1; dx++) {
//                                    for (int dy = -1; dy <= 1; dy++) {
//                                        int blockTypeAroundObject = minecraftWorld.getBlockTypeAt(x + dx, y + dy, height + 2);
//                                        if ((blockTypeAroundObject != BLK_AIR) && (! INSUBSTANTIAL_BLOCKS.contains(blockTypeAroundObject)) && (blockTypeAroundObject != BLK_LEAVES)) {
//                                            continue objectLoop;
//                                        }
//                                    }
//                                }
//                            }
                            if (object.getAttribute(ATTRIBUTE_RANDOM_ROTATION, true)) {
                                if (random.nextBoolean()) {
                                    object = new MirroredObject(object, false);
                                }
                                int rotateSteps = random.nextInt(4);
                                if (rotateSteps > 0) {
                                    object = new RotatedObject(object, rotateSteps);
                                }
                            }
                            int z = (placement == Placement.ON_LAND) ? height + 1 : dimension.getWaterLevelAt(x, y) + 1;
                            if (! isRoom(minecraftWorld, dimension, object, x, y, z, placement)) {
                                continue;
                            }
                            if (! fitsInExportedArea(exportedArea, object, x, y)) {
                                // There is room on our side of the border, but
                                // the object extends outside the exported area,
                                // so it might clash with an object from another
                                // area. Schedule a fixup to retest whether
                                // there is room after all the objects have been
                                // placed on both sides of the border
                                fixups.add(new WPObjectFixup(object, x, y, z, placement));
                                continue;
                            }
                            renderObject(minecraftWorld, dimension, object, x, y, z, placement);
                        }
                    }
                }
            }
        }
        return fixups;
    }

    private boolean fitsInExportedArea(final Rectangle exportedArea, final WPObject object, final int x, final int y) {
        final Point3i dimensions = object.getDimensions();
        final Point3i offset = object.getAttribute(ATTRIBUTE_OFFSET, new Point3i());
        // Check whether the objects fits completely inside the exported area.
        // This is to avoid objects getting cut off at area boundaries
        return ! ((x + offset.x < exportedArea.x) || (x + offset.x + dimensions.x > exportedArea.x + exportedArea.width)
            || (y + offset.y < exportedArea.y) || (y + offset.y + dimensions.y > exportedArea.y + exportedArea.height));
    }

    private Placement getPlacement(final MinecraftWorld minecraftWorld, final Dimension dimension, final int x, final int y, final int z, final WPObject object, final Random random) {
        final boolean spawnUnderWater = object.getAttribute(ATTRIBUTE_SPAWN_IN_WATER, false), spawnUnderLava = object.getAttribute(ATTRIBUTE_SPAWN_IN_LAVA, false);
        final boolean spawnOnWater = object.getAttribute(ATTRIBUTE_SPAWN_ON_WATER, false), spawnOnLava = object.getAttribute(ATTRIBUTE_SPAWN_ON_LAVA, false);
        final int waterLevel = dimension.getWaterLevelAt(x, y);
        final boolean flooded = waterLevel >= z;
        if (flooded && (spawnUnderWater || spawnUnderLava || spawnOnWater || spawnOnLava)) {
            boolean lava = dimension.getBitLayerValueAt(FloodWithLava.INSTANCE, x, y);
            if (lava ? (spawnUnderLava && spawnOnLava) : (spawnUnderWater && spawnOnWater)) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Object " + object.getName() + " @ " + x + "," + y + "," + z + " potentially placeable under or on water or lava");
                }
                return random.nextBoolean() ? Placement.ON_LAND : Placement.FLOATING;
            } else if (lava ? spawnUnderLava : spawnUnderWater) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Object " + object.getName() + " @ " + x + "," + y + "," + z + " potentially placeable under water or lava");
                }
                return Placement.ON_LAND;
            } else if (lava ? spawnOnLava : spawnOnWater) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Object " + object.getName() + " @ " + x + "," + y + "," + z + " potentially placeable on water or lava");
                }
                return Placement.FLOATING;
            }
        } else if (! flooded) {
            int blockTypeUnderCoords = (z > 0) ? minecraftWorld.getBlockTypeAt(x, y, z - 1) : BLK_AIR;
            if (object.getAttribute(ATTRIBUTE_SPAWN_ON_LAND, true) && (! VERY_INSUBSTANTIAL_BLOCKS.contains(blockTypeUnderCoords))) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Object " + object.getName() + " @ " + x + "," + y + "," + z + " potentially placeable on land");
                }
                return Placement.ON_LAND;
            } else if ((! object.getAttribute(ATTRIBUTE_NEEDS_FOUNDATION, true)) && VERY_INSUBSTANTIAL_BLOCKS.contains(blockTypeUnderCoords)) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Object " + object.getName() + " @ " + x + "," + y + "," + z + " potentially placeable in the air");
                }
                return Placement.ON_LAND;
            }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Object " + object.getName() + " @ " + x + "," + y + "," + z + " not placeable");
        }
        return Placement.NONE;
    }
    
    private static final Logger logger = Logger.getLogger(Bo2LayerExporter.class.getName());
}