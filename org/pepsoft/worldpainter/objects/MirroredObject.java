/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3i;
import org.pepsoft.minecraft.Direction;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Material;
import org.pepsoft.minecraft.TileEntity;

/**
 *
 * @author pepijn
 */
public class MirroredObject extends AbstractObject {
    public MirroredObject(WPObject object, boolean mirrorYAxis) {
        this.object = object;
        this.mirrorYAxis = mirrorYAxis;
        dimensions = object.getDimensions();
        Map<String, Serializable> attributes = (object.getAttributes() != null) ? object.getAttributes() : new HashMap<String, Serializable>();
        Point3i offset = attributes.containsKey(WPObject.ATTRIBUTE_OFFSET) ? (Point3i) attributes.get(WPObject.ATTRIBUTE_OFFSET) : new Point3i();
        offset = mirrorYAxis
            ? new Point3i(offset.x, -(dimensions.y - (-offset.y) - 1), offset.z)
            : new Point3i(-(dimensions.x - (-offset.x) - 1), offset.y, offset.z);
        if ((offset.x != 0) || (offset.y != 0) || (offset.z != 0)) {
            attributes.put(ATTRIBUTE_OFFSET, offset);
        } else {
            attributes.remove(ATTRIBUTE_OFFSET);
        }
        if (! attributes.isEmpty()) {
            this.attributes = attributes;
        } else {
            this.attributes = null;
        }
    }

    @Override
    public Point3i getDimensions() {
        return object.getDimensions();
    }

    @Override
    public Material getMaterial(int x, int y, int z) {
        return mirrorYAxis
            ? object.getMaterial(x, dimensions.y - y - 1, z).mirror(Direction.NORTH)
            : object.getMaterial(dimensions.x - x - 1, y, z).mirror(Direction.EAST);
    }

    @Override
    public boolean getMask(int x, int y, int z) {
        return mirrorYAxis
            ? object.getMask(x, dimensions.y - y - 1, z)
            : object.getMask(dimensions.x - x - 1, y, z);
    }

    @Override
    public List<Entity> getEntities() {
        return object.getEntities();
    }

    @Override
    public List<TileEntity> getTileEntities() {
        List<TileEntity> objectTileEntities = object.getTileEntities();
        if (objectTileEntities != null) {
            List<TileEntity> tileEntities = new ArrayList<TileEntity>(objectTileEntities.size());
            for (TileEntity objectTileEntity: objectTileEntities) {
                TileEntity tileEntity = (TileEntity) objectTileEntity.clone();
                if (mirrorYAxis) {
                    tileEntity.setZ(dimensions.y - objectTileEntity.getZ() - 1);
                } else {
                    tileEntity.setX(dimensions.x - objectTileEntity.getX() - 1);
                }
                tileEntities.add(tileEntity);
            }
            return tileEntities;
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        return object.getName();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, Serializable> attributes) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    private final WPObject object;
    private final boolean mirrorYAxis;
    private final Point3i dimensions;
    private final Map<String, Serializable> attributes;

    private static final long serialVersionUID = 1L;
}