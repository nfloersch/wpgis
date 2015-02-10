/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.bo2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3i;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Material;
import org.pepsoft.minecraft.TileEntity;
import org.pepsoft.worldpainter.objects.AbstractObject;
import org.pepsoft.worldpainter.objects.WPObject;

/**
 *
 * @author pepijn
 */
public final class Bo2Object extends AbstractObject implements Bo2ObjectProvider {
    private Bo2Object(String name, Map<String, String> properties, Map<Point3i, Bo2BlockSpec> blocks, Point3i origin, Point3i dimensions, Map<String, Serializable> attributes) {
        this.name = name;
        this.properties = properties;
        this.blocks = blocks;
        this.origin = origin;
        this.dimensions = dimensions;
        if ((origin.x != 0) || (origin.y != 0) || (origin.z != 0)) {
            if (attributes == null) attributes = new HashMap<String, Serializable>();
            attributes.put(ATTRIBUTE_OFFSET, new Point3i(-origin.x, -origin.y, -origin.z));
        }
        if (properties.containsKey(KEY_RANDOM_ROTATION) && (! Boolean.valueOf(properties.get(KEY_RANDOM_ROTATION)))) {
            if (attributes == null) attributes = new HashMap<String, Serializable>();
            attributes.put(WPObject.ATTRIBUTE_RANDOM_ROTATION, false);
        }
        if (properties.containsKey(KEY_NEEDS_FOUNDATION) && ! Boolean.valueOf(properties.get(KEY_NEEDS_FOUNDATION))) {
            if (attributes == null) attributes = new HashMap<String, Serializable>();
            attributes.put(WPObject.ATTRIBUTE_NEEDS_FOUNDATION, false);
        }
        if (properties.containsKey(KEY_SPAWN_LAVA) && Boolean.valueOf(properties.get(KEY_SPAWN_LAVA))) {
            if (attributes == null) attributes = new HashMap<String, Serializable>();
            attributes.put(WPObject.ATTRIBUTE_SPAWN_IN_LAVA, true);
        }
        if (properties.containsKey(KEY_SPAWN_WATER) && Boolean.valueOf(properties.get(KEY_SPAWN_WATER))) {
            if (attributes == null) attributes = new HashMap<String, Serializable>();
            attributes.put(WPObject.ATTRIBUTE_SPAWN_IN_WATER, true);
        }
        this.attributes = attributes;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Point3i getDimensions() {
        return dimensions;
    }

    @Override
    public Material getMaterial(int x, int y, int z) {
        return blocks.get(new Point3i(x - origin.x, y - origin.y, z - origin.z)).getMaterial();
    }

    @Override
    public boolean getMask(int x, int y, int z) {
        return blocks.containsKey(new Point3i(x - origin.x, y - origin.y, z - origin.z));
    }

    @Override
    public Bo2Object getObject() {
        return this;
    }

    @Override
    public List<Entity> getEntities() {
        return null;
    }

    @Override
    public List<TileEntity> getTileEntities() {
        return null;
    }

    @Override
    public List<WPObject> getAllObjects() {
        return Collections.singletonList((WPObject) this);
    }

    @Override
    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    @Override
    public void setAttribute(String key, Serializable value) {
        if (value != null) {
            if (attributes == null) {
                attributes = new HashMap<String, Serializable>();
            }
            attributes.put(key, value);
        } else if (attributes != null) {
            attributes.remove(key);
            if (attributes.isEmpty()) {
                attributes = null;
            }
        }
    }

    @Override
    public void setSeed(long seed) {
        // Do nothing
    }

    @Override
    public Bo2Object clone() {
        Bo2Object clone = (Bo2Object) super.clone();
        clone.origin = (Point3i) origin.clone();
        clone.dimensions = (Point3i) dimensions.clone();
        if (attributes != null) {
            clone.attributes = new HashMap<String, Serializable>(attributes);
        }
        return clone;
    }
    
    public static Bo2Object load(File file) throws IOException {
        String name = file.getName();
        int p = name.lastIndexOf('.');
        if (p != -1) {
            name = name.substring(0, p);
        }
        return load(name, file);
    }

    private void readObject(ObjectInputStream in) throws  IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Legacy
        if (version == 0) {
            if ((origin.x != 0) || (origin.y != 0) || (origin.z != 0)) {
                if (attributes == null) attributes = new HashMap<String, Serializable>();
                attributes.put(ATTRIBUTE_OFFSET, new Point3i(-origin.x, -origin.y, -origin.z));
            }
            if (properties.containsKey(KEY_RANDOM_ROTATION) && (! Boolean.valueOf(properties.get(KEY_RANDOM_ROTATION)))) {
                if (attributes == null) attributes = new HashMap<String, Serializable>();
                attributes.put(WPObject.ATTRIBUTE_RANDOM_ROTATION, false);
            }
            if (properties.containsKey(KEY_NEEDS_FOUNDATION) && ! Boolean.valueOf(properties.get(KEY_NEEDS_FOUNDATION))) {
                if (attributes == null) attributes = new HashMap<String, Serializable>();
                attributes.put(WPObject.ATTRIBUTE_NEEDS_FOUNDATION, false);
            }
            if (properties.containsKey(KEY_SPAWN_LAVA) && Boolean.valueOf(properties.get(KEY_SPAWN_LAVA))) {
                if (attributes == null) attributes = new HashMap<String, Serializable>();
                attributes.put(WPObject.ATTRIBUTE_SPAWN_IN_LAVA, true);
            }
            if (properties.containsKey(KEY_SPAWN_WATER) && Boolean.valueOf(properties.get(KEY_SPAWN_WATER))) {
                if (attributes == null) attributes = new HashMap<String, Serializable>();
                attributes.put(WPObject.ATTRIBUTE_SPAWN_IN_WATER, true);
            }
            version = 1;
        }
        if (version == 1) {
            if (! attributes.containsKey(ATTRIBUTE_LEAF_DECAY_MODE)) {
                attributes.put(ATTRIBUTE_LEAF_DECAY_MODE, LEAF_DECAY_ON);
            }
            version = 2;
        }
    }
    
    public static Bo2Object load(String objectName, File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("US-ASCII")));
        try {
            Map<String, String> properties = new HashMap<String, String>();
            Map<Point3i, Bo2BlockSpec> blocks = new HashMap<Point3i, Bo2BlockSpec>();
            boolean readingMetaData = false, readingData = false;
            String line;
            int lowestX = Integer.MAX_VALUE, highestX = Integer.MIN_VALUE;
            int lowestY = Integer.MAX_VALUE, highestY = Integer.MIN_VALUE;
            int lowestZ = Integer.MAX_VALUE, highestZ = Integer.MIN_VALUE;
            while ((line = in.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (readingMetaData) {
                    if (line.equals("[DATA]")) {
                        readingMetaData = false;
                        readingData = true;
                    } else {
                        int p = line.indexOf('=');
                        String name = line.substring(0, p).trim();
                        String value = line.substring(p + 1).trim();
                        properties.put(name, value);
                    }
                } else if (readingData) {
                    int p = line.indexOf(':');
                    String coordinates = line.substring(0, p);
                    String spec = line.substring(p + 1);
                    p = coordinates.indexOf(',');
                    int x = Integer.parseInt(coordinates.substring(0, p));
                    int p2 = coordinates.indexOf(',', p + 1);
                    int y = Integer.parseInt(coordinates.substring(p + 1, p2));
                    int z = Integer.parseInt(coordinates.substring(p2 + 1));
                    if (x < lowestX) {
                        lowestX = x;
                    }
                    if (x > highestX) {
                        highestX = x;
                    }
                    if (y < lowestY) {
                        lowestY = y;
                    }
                    if (y > highestY) {
                        highestY = y;
                    }
                    if (z < lowestZ) {
                        lowestZ = z;
                    }
                    if (z > highestZ) {
                        highestZ = z;
                    }
                    p = spec.indexOf('.');
                    int blockId, data = 0;
                    int[] branch = null;
                    if (p == -1) {
                        blockId = Integer.parseInt(spec);
                    } else {
                        blockId = Integer.parseInt(spec.substring(0, p));
                        p2 = spec.indexOf('#', p + 1);
                        if (p2 == -1) {
                            data = Integer.parseInt(spec.substring(p + 1));
                        } else {
                            data = Integer.parseInt(spec.substring(p + 1, p2));
                            p = spec.indexOf('@', p2 + 1);
                            branch = new int[] {Integer.parseInt(spec.substring(p2 + 1, p)), Integer.parseInt(spec.substring(p + 1))};
                        }
                    }
                    Point3i coords = new Point3i(x, y, z);
                    blocks.put(coords, new Bo2BlockSpec(coords, Material.get(blockId, data), branch));
                } else {
                    if (line.equals("[META]")) {
                        readingMetaData = true;
                    }
                }
            }
            Map<String, Serializable> attributes = new HashMap<String, Serializable>();
            attributes.put(WPObject.ATTRIBUTE_FILE, file);
            return new Bo2Object(objectName, properties, blocks, new Point3i(-lowestX, -lowestY, -lowestZ), new Point3i(highestX - lowestX + 1, highestY - lowestY + 1, highestZ - lowestZ + 1), attributes);
        } finally {
            in.close();
        }
    }
 
    private String name;
    private final Map<String, String> properties;
    private final Map<Point3i, Bo2BlockSpec> blocks;
    private Point3i origin, dimensions;
    private Map<String, Serializable> attributes;
    private int version = 2;
    
    public static final String KEY_SPAWN_WATER              = "spawnWater";
    public static final String KEY_SPAWN_LAVA               = "spawnLava";
    public static final String KEY_NEEDS_FOUNDATION         = "needsFoundation";
    public static final String KEY_RANDOM_ROTATION          = "randomRotation";
    
    private static final long serialVersionUID = 1L;
}