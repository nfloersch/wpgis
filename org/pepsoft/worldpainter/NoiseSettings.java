/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.io.Serializable;

/**
 * Settings for a noise generator.
 *
 * @author SchmitzP
 */
public class NoiseSettings implements Serializable {
    /**
     * The seed to use to initialise the noise generator.
     */
    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    /**
     * The range of the noise generator. Values returned by the generator will
     * be between 0 (inclusive) and <code>range</code> (exclusive).
     */
    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    /**
     * The scale of the noise. A scale of 1.0 means "regular" or "default" size
     * peaks and troughs, the actual size of which is determined by the noise
     * generator.
     */
    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * A measure of the roughness of the noise. A value of 0 produces very
     * smooth noise, higher values produce progressively rougher, more chaotic
     * noise.
     */
    public int getRoughness() {
        return roughness;
    }

    public void setRoughness(int roughness) {
        this.roughness = roughness;
    }
    
    private long seed;
    private int range, roughness;
    private float scale = 1.0f;
    
    private static final long serialVersionUID = 1L;
}