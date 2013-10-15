/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.heightMaps;

import org.pepsoft.worldpainter.HeightMap;

/**
 *
 * @author pepijn
 */
public final class SumHeightMap implements HeightMap {
    public SumHeightMap(HeightMap heightMap1, HeightMap heightMap2) {
        this.heightMap1 = heightMap1;
        this.heightMap2 = heightMap2;
    }

    public HeightMap getHeightMap1() {
        return heightMap1;
    }

    public HeightMap getHeightMap2() {
        return heightMap2;
    }

    @Override
    public long getSeed() {
        return heightMap1.getSeed();
    }

    @Override
    public void setSeed(long seed) {
        heightMap1.setSeed(seed);
        heightMap2.setSeed(seed);
    }

    @Override
    public float getHeight(int x, int y) {
        return heightMap1.getHeight(x, y) + heightMap2.getHeight(x, y);
    }

    @Override
    public float getBaseHeight() {
        return heightMap1.getBaseHeight() + heightMap2.getBaseHeight();
    }
    
    private final HeightMap heightMap1, heightMap2;

    private static final long serialVersionUID = 1L;
}