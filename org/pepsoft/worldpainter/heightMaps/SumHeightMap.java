/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.heightMaps;

import java.io.ObjectStreamException;
import org.pepsoft.worldpainter.HeightMap;

/**
 * A height map which is the sum of two other height maps.
 * 
 * @author pepijn
 */
public final class SumHeightMap extends CombiningHeightMap {
    public SumHeightMap(HeightMap heightMap1, HeightMap heightMap2) {
        super(heightMap1, heightMap2);
    }

    @Override
    public float getHeight(int x, int y) {
        return heightMap1.getHeight(x, y) + heightMap2.getHeight(x, y);
    }

    @Override
    public float getBaseHeight() {
        return heightMap1.getBaseHeight() + heightMap2.getBaseHeight();
    }

    private Object readResolve() throws ObjectStreamException {
        // There are worlds in the wild where heightMap1 and/or heightMap2 are
        // null. No idea how that could happen, but it will cause errors, so
        // fix it as best we can
        if (heightMap1 == null) {
            if (heightMap2 == null) {
                return new SumHeightMap(new ConstantHeightMap(62), new ConstantHeightMap(0));
            } else {
                return new SumHeightMap(new ConstantHeightMap(58 - heightMap2.getBaseHeight()), heightMap2);
            }
        } else if (heightMap2 == null) {
            return new SumHeightMap(heightMap1, new ConstantHeightMap(58 - heightMap1.getBaseHeight()));
        }
        return this;
    }
    
    private static final long serialVersionUID = 1L;
}