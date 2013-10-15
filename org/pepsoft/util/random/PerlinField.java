/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util.random;

import java.util.Random;
import org.pepsoft.util.PerlinNoise;

/**
 * @author pepijn
 */
public final class PerlinField implements Field2DDouble {
    public PerlinField() {
        perlinNoise = new PerlinNoise(new Random().nextLong());
    }

    public PerlinField(long seed) {
        perlinNoise = new PerlinNoise(seed);
    }

    @Override
    public double getValue(double x, double y) {
        return perlinNoise.getPerlinNoise(x / 256, y / 256) + 0.5;
    }

    @Override
    public Number getValue(Number x, Number y) {
        return getValue(x.doubleValue(), y.doubleValue());
    }

    private final PerlinNoise perlinNoise;
}