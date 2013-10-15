/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.heightMaps;

import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.HeightMap;
import static org.pepsoft.worldpainter.Constants.*;

/**
 *
 * @author pepijn
 */
public final class NoiseHeightMap implements HeightMap {
    public NoiseHeightMap(float range, double scale) {
        this.range = range;
        this.scale = scale;
    }
    
    @Override
    public long getSeed() {
        return perlinNoise.getSeed() - SEED_OFFSET;
    }

    @Override
    public void setSeed(long seed) {
        if ((perlinNoise.getSeed() + SEED_OFFSET) != seed) {
            perlinNoise.setSeed(seed + SEED_OFFSET);
        }
    }

    @Override
    public float getHeight(int x, int y) {
        return (perlinNoise.getPerlinNoise(x / LARGE_BLOBS / scale, y / LARGE_BLOBS / scale) + 0.5f) * range;
    }

    @Override
    public float getBaseHeight() {
        return 0.0f;
    }
    
    private final PerlinNoise perlinNoise = new PerlinNoise(0);
    private final float range;
    private final double scale;
    
    private static final long SEED_OFFSET = 131;
    private static final long serialVersionUID = 1L;
}