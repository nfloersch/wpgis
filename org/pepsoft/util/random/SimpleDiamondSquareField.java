/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util.random;

/**
 * @author pepijn
 */
public class SimpleDiamondSquareField implements Field2DDouble {
    public SimpleDiamondSquareField(int size) {
        this.size = size;
        values = new byte[size][size];
    }

    @Override
    public double getValue(double x, double y) {
        int intX = (int) Math.round(x), intY = (int) Math.round(y);
        return (values[intX][intY] & 0xFF) / 255.0;
    }

    @Override
    public Number getValue(Number x, Number y) {
        return getValue(x.doubleValue(), y.doubleValue());
    }

    private final int size;
    private final byte[][] values;
}