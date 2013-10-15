/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.util;

import java.util.Random;

/**
 *
 * @author pepijn
 */
public class PerlinNoiseLevelFinder {
    public static void main(String[] args) {
        PerlinNoise perlinNoise = new PerlinNoise(0);
        System.out.print('{');
        for (int i = 0; i <= 1000; i++) {
            float target = i / 1000f;
            float level = findLevelForPromillage(perlinNoise, target);
            System.out.print(level);
            System.out.print('f');
            if (i < 1000) {
                System.out.print(", ");
            }
            if ((i + 1) % 10 == 0) {
                System.out.println();
            }
        }
        System.out.print('}');
    }

    private static float findLevelForPromillage(PerlinNoise perlinNoise, float target) {
        float level = 0.0f, step = 0.25f;
        for (int i =0; i < 100; i++) {
            int hits = numberOfHits(perlinNoise, level, 10000);
            float promillage = hits / 10000.0f;
//            System.out.println("Promillage at level " + level + ": " + (promillage * 1000));
            if (promillage > target) {
                level += step;
                step /= 2;
            } else if (promillage < target) {
                level -= step;
                step /= 2;
            } else {
                return level;
            }
        }
        return level;
    }

    private static int numberOfHits(PerlinNoise perlinNoise, float level, int count) {
        int hits = 0;
        Random random = new Random(0);
        for (int i = 0; i < count; i++) {
            double x = random.nextDouble() * 256;
            double y = random.nextDouble() * 256;
            double z = random.nextDouble() * 256;
            float noise = perlinNoise.getPerlinNoise(x, y, z);
            if (noise >= level) {
                hits++;
            }
        }
        return hits;
    }
}