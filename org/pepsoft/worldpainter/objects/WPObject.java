/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.objects;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3i;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Material;
import org.pepsoft.minecraft.TileEntity;

/**
 *
 * @author pepijn
 */
public interface WPObject extends Serializable {
    /**
     * Get the name of the object.
     * 
     * @return The name of the object.
     */
    String getName();
    
    /**
     * Set the name of the object.
     * 
     * @param name The new name of the object.
     */
    void setName(String name);
    
    /**
     * Get the dimensions of the object.
     * 
     * @return The dimensions of the object.
     */
    Point3i getDimensions();
    
    /**
     * Get the material to place at the specified relative coordinates. Should
     * only be invoked for coordinates for which {@link #getMask(int, int, int)}
     * returns <code>true</code>.
     * 
     * @param x The relative X coordinate.
     * @param y The relative Y coordinate.
     * @param z The relative Z coordinate.
     * @return The material to place at the specified relative coordinates.
     */
    Material getMaterial(int x, int y, int z);
    
    /**
     * Determine whether a block should be placed at the specified relative
     * coordinates.
     * 
     * @param x The relative X coordinate.
     * @param y The relative Y coordinate.
     * @param z The relative Z coordinate.
     * @return <code>true</code> if a block should be placed at the specified
     *     relative coordinates.
     */
    boolean getMask(int x, int y, int z);
    
    /**
     * Get any entities contained in the object. The entities' coordinates
     * should be relative to the object, not absolute.
     * 
     * @return Any entities contained in the object. May be <code>null</code>.
     */
    List<Entity> getEntities();
    
    /**
     * Get any tile entities contained in the object. The entities' coordinates
     * should be relative to the object, not absolute.
     * 
     * @return Any tile entities contained in the object. May be
     *     <code>null</code>.
     */
    List<TileEntity> getTileEntities();
    
    /**
     * Get a copy of previously stored external metadata about the object.
     * 
     * @return A copy of previously stored external metadata about the object.
     *     May be <code>null</code>.
     */
    Map<String, Serializable> getAttributes();
    
    /**
     * Convencience method for getting the value of an attribute stored in the
     * external metadata, if any. Should return the value of the attribute if it
     * is present, or the specified default value if it is not.
     * 
     * @param <T> The type of the attribute.
     * @param key The key of the attribute.
     * @param _default The value to return if the attribute is not set.
     * @return The value of the specified attribute, or the specified default
     *     value if the attribute is not set.
     */
    <T extends Serializable> T getAttribute(String key, T _default);
    
    /**
     * Store external metadata about the object.
     * 
     * @param properties The external metadata to store.
     */
    void setAttributes(Map<String, Serializable> attributes);
    
    // Standard attribute keys
    static final String ATTRIBUTE_FILE             = "WPObject.file";            // Type String
    static final String ATTRIBUTE_OFFSET           = "WPObject.offset";          // Type Point3i
    static final String ATTRIBUTE_RANDOM_ROTATION  = "WPObject.randomRotation";  // Type Boolean
    static final String ATTRIBUTE_NEEDS_FOUNDATION = "WPObject.needsFoundation"; // Type Boolean; default: true
    static final String ATTRIBUTE_SPAWN_IN_WATER   = "WPObject.spawnInWater";    // Type Boolean; default: false
    static final String ATTRIBUTE_SPAWN_IN_LAVA    = "WPObject.spawnInLava";     // Type Boolean; default: false
    static final String ATTRIBUTE_SPAWN_ON_LAND    = "WPObject.spawnOnLand";     // Type Boolean; default: true
    static final String ATTRIBUTE_SPAWN_ON_WATER   = "WPObject.spawnOnWater";    // Type Boolean; default: false
    static final String ATTRIBUTE_SPAWN_ON_LAVA    = "WPObject.spawnOnLava";     // Type Boolean; default: false
    static final String ATTRIBUTE_FREQUENCY        = "WPObject.frequency";       // Type Integer
    /**
     * Collision mode. Possible values:
     * 
     * <p><table><tr><th>Value</th><th>Meaning</th></tr>
     * <tr><td>{@link #VALUE_ALL}</td><td>Will collide with (and therefore not render) any above ground block other than air</td></tr>
     * <tr><td><strong>{@link #VALUE_SOLID}</strong></td><td>Will collide with (and therefore not render) any above ground <em>solid</em> block (i.e. not air, grass, water, flowers, leaves, etc.). Default value</td></tr>
     * <tr><td>{@link #VALUE_NONE}</td><td>Will not collide with <em>any</em> above ground block (and therefore intersect any other object already there!)</td></tr></table>
     */
    static final String ATTRIBUTE_COLLISION_MODE   = "WPObject.collisionMode";   // Type Integer; see VALUE_* constants
    /**
     * Underground rendering mode. Possible values:
     * 
     * <p><table><tr><th>Value</th><th>Meaning</th></tr>
     * <tr><td><strong>{@link #VALUE_ALL}</strong></td><td>Every underground block belonging to the object will be rendered (including air blocks), regardless of what is already there. Default value</td></tr>
     * <tr><td>{@link #VALUE_SOLID}</td><td>Every <em>solid</em> (i.e. not air, grass, water, flowers, leaves, etc.) underground block belonging to the object will be rendered regardless of what is already there. Non-solid blocks will be rendered only if the existing block is air</td></tr>
     * <tr><td>{@link #VALUE_NONE}</td><td>Underground blocks belonging to the object will only be rendered if the existing block is air</td></tr></table>
     */
    static final String ATTRIBUTE_UNDERGROUND_MODE = "WPObject.undergroundMode"; // Type Integer; see VALUE_* constants
    
    static final int VALUE_ALL   = 1;
    static final int VALUE_SOLID = 2;
    static final int VALUE_NONE  = 3;
}