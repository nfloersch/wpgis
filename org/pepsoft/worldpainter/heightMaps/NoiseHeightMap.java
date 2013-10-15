/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.heightMaps;

import java.util.Random;
import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.HeightMap;
import static org.pepsoft.worldpainter.Constants.*;

/**
 *
 * @author pepijn
 */
public final class NoiseHeightMap implements HeightMap {
    public NoiseHeightMap(float range, double scale, int octaves) {
        this(range, scale, octaves, new Random().nextLong());
    }
    
    public NoiseHeightMap(float range, double scale, int octaves, long seedOffset) {
        if (octaves > 10) {
            throw new IllegalArgumentException("More than 10 octaves not supported");
        }
        this.range = range;
        this.scale = scale;
        this.octaves = octaves;
        this.seedOffset = seedOffset;
        perlinNoise = new PerlinNoise(seedOffset);
    }
    
    @Override
    public long getSeed() {
        return perlinNoise.getSeed() - seedOffset;
    }

    @Override
    public void setSeed(long seed) {
        if ((perlinNoise.getSeed() + seedOffset) != seed) {
            perlinNoise.setSeed(seed + seedOffset);
        }
    }

    @Override
    public float getHeight(int x, int y) {
        if (octaves == 1) {
            return (perlinNoise.getPerlinNoise(x / LARGE_BLOBS / scale, y / LARGE_BLOBS / scale) + 0.5f) * range;
        } else {
            float noise = 0;
            for (int i = 0; i < octaves; i++) {
                noise += perlinNoise.getPerlinNoise(x / LARGE_BLOBS / scale * FACTORS[i], y / LARGE_BLOBS / scale * FACTORS[i]);
            }
            noise /= octaves;
            return (noise + 0.5f) * range;
        }
    }

    @Override
    public float getBaseHeight() {
        return 0.0f;
    }
    
    public static void main(String[] args) {
        System.out.print('{');
        for (int i = 1; i <= 10; i++) {
            System.out.print(Math.pow(2.0, i - 1));
        }
    }
    
    private final PerlinNoise perlinNoise;
    private final float range;
    private final double scale;
    private final int octaves;
    private final long seedOffset;
    
    private static final int[] FACTORS = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512};
    private static final long serialVersionUID = 1L;
}