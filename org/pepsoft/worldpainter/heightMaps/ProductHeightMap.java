/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.heightMaps;

import org.pepsoft.worldpainter.HeightMap;

/**
 * A height map which is the product of two other height maps.
 * 
 * @author pepijn
 */
public class ProductHeightMap extends CombiningHeightMap {
    public ProductHeightMap(HeightMap heightMap1, HeightMap heightMap2) {
        super(heightMap1, heightMap2);
    }

    @Override
    public float getHeight(int x, int y) {
        return heightMap1.getHeight(x, y) * heightMap2.getHeight(x, y);
    }

    @Override
    public float getBaseHeight() {
        return heightMap1.getBaseHeight() * heightMap2.getBaseHeight();
    }
    
    @Override
    public ProductHeightMap clone() {
        ProductHeightMap clone = new ProductHeightMap(heightMap1.clone(), heightMap2.clone());
        clone.setSeed(getSeed());
        return clone;
    }

    private static final long serialVersionUID = 1L;
}
