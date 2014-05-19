/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.heightMaps;

import org.pepsoft.worldpainter.HeightMap;

/**
 * An abstract base class for a height map which somehow combines two
 * subordinate height maps.
 * 
 * @author SchmitzP
 */
public abstract class CombiningHeightMap implements HeightMap {
    public CombiningHeightMap(HeightMap heightMap1, HeightMap heightMap2) {
        this.heightMap1 = heightMap1;
        this.heightMap2 = heightMap2;
    }

    public final HeightMap getHeightMap1() {
        return heightMap1;
    }

    public final HeightMap getHeightMap2() {
        return heightMap2;
    }

    @Override
    public final long getSeed() {
        return heightMap1.getSeed();
    }

    @Override
    public final void setSeed(long seed) {
        heightMap1.setSeed(seed);
        heightMap2.setSeed(seed);
    }

    public abstract CombiningHeightMap clone();
    
    protected final HeightMap heightMap1, heightMap2;

    private static final long serialVersionUID = 1L;
}