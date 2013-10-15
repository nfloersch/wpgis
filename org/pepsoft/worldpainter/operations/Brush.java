/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.operations;

/**
 *
 * @author pepijn
 */
public interface Brush {
    /**
     * Get the name of the brush.
     * 
     * @return The name of the brush.
     */
    String getName();
    
    /**
     * Get the brush strength at a specific location.
     * 
     * @param centerX The absolute X coordinate of the center of the brush.
     * @param centerY The absolute Y coordinate of the center of the brush.
     * @param x The absolute X coordinate for which to get the brush strength.
     * @param y The absolute Y coordinate for which to get the brush strength.
     * @return The brush strength at the specified location, from 0.0f (no
     *     effect) to 1.0f (maximum effect) (inclusive).
     */
    float getStrength(int centerX, int centerY, int x, int y);

    /**
     * Get the brush strength at a specific point on the brush.
     * 
     * @param dx The X coordinate relative to the center of the brush.
     * @param dy The Y coordinate relative to the center of the brush.
     * @return The brush strength at the specified location, from 0.0f (no
     *     effect) to 1.0f (maximum effect) (inclusive).
     */
    float getStrength(int dx, int dy);
    
    /**
     * Get the maximum brush strength at a specific location, in other words the
     * strength if the level was set to 1.0f. Synonymous with invoking
     * <code>setLevel(1.0f); getStrength(centerX, centerY, x, y)</code>, but
     * possibly more efficient and without actually changing the value of the
     * <code>level</code> property.
     * 
     * @param centerX The absolute X coordinate of the center of the brush.
     * @param centerY The absolute Y coordinate of the center of the brush.
     * @param x The absolute X coordinate for which to get the brush strength.
     * @param y The absolute Y coordinate for which to get the brush strength.
     * @return The maxiumum brush strength at the specified location, from 0.0f
     *     (no effect) to 1.0f (maximum effect) (inclusive).
     */
    float getFullStrength(int centerX, int centerY, int x, int y);
    
    /**
     * Get the maximum brush strength at a specific point on the brush, in other
     * words the strength if the level was set to 1.0f. Synonymous with invoking
     * <code>setLevel(1.0f); getStrength(dx, dy)</code>, but possibly more
     * efficient and without actually changing the value of the
     * <code>level</code> property.
     * 
     * @param dx The X coordinate relative to the center of the brush.
     * @param dy The Y coordinate relative to the center of the brush.
     * @return The maximum brush strength at the specified location, from 0.0f
     *     (no effect) to 1.0f (maximum effect) (inclusive).
     */
    float getFullStrength(int dx, int dy);
    
    /**
     * Get the brush strength at a specific location, adding some random
     * variation.
     * 
     * @param seed The seed to use for calculating the random variation.
     * @param centerX The absolute X coordinate of the center of the brush.
     * @param centerY The absolute Y coordinate of the center of the brush.
     * @param x The absolute X coordinate for which to get the brush strength.
     * @param y The absolute Y coordinate for which to get the brush strength.
     * @return The brush strength at the specified location plus some random
     *     variation, from 0.0f (no effect) to 1.0f (maximum effect)
     *     (inclusive).
     */
    float getNoisyStrength(long seed, int centerX, int centerY, int x, int y);

    /**
     * Get the brush strength at a specific location, adding some random
     * variation, based on the maximum brush strenght, in other words the
     * strength if the level was set to 1.0f.
     * 
     * @param seed The seed to use for calculating the random variation.
     * @param centerX The absolute X coordinate of the center of the brush.
     * @param centerY The absolute Y coordinate of the center of the brush.
     * @param x The absolute X coordinate for which to get the brush strength.
     * @param y The absolute Y coordinate for which to get the brush strength.
     * @return The maximum brush strength at the specified location plus some
     *     random variation, from 0.0f (no effect) to 1.0f (maximum effect)
     *     (inclusive).
     */
    float getNoisyFullStrength(long seed, int centerX, int centerY, int x, int y);
    
    /**
     * Get the current radius of the brush.
     * 
     * @return The current radius of the brush in pixels.
     */
    int getRadius();

    /**
     * Set the radius of the brush.
     * 
     * @param radius The new radius of the brush in pixels.
     */
    void setRadius(int radius);

    /**
     * Get the current level of the brush.
     * 
     * @return The current level of the brush, from 0.0f (no effect) to 1.0f
     *     (maximum effect) (inclusive).
     */
    float getLevel();

    /**
     * Set the level of the brush.
     * 
     * @param level The new level of the brush, from 0.0f (no effect) to 1.0f
     *     (maximum effect) (inclusive).
     */
    void setLevel(float level);
    
    /**
     * Get the shape of the brush.
     * 
     * @return The shape of the brush.
     */
    BrushShape getBrushShape();
    
    /**
     * Create a deep copy of the brush. The copy will not be affected by
     * subsequent modifications to the original.
     * 
     * @return A deep copy of the brush.
     */
    Brush clone();
}