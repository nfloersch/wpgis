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
public abstract class AbstractHeightMap implements HeightMap {
    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }
    
    protected long seed;
    
    private static final long serialVersionUID = 1L;
}