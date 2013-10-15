/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import org.pepsoft.worldpainter.BiomeScheme;
import org.pepsoft.worldpainter.Configuration;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import static org.pepsoft.worldpainter.biomeschemes.Minecraft1_2BiomeScheme.*;

/**
 *
 * @author pepijn
 */
public class LargeContinentFinder {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Configuration config = Configuration.load();
        if (config == null) {
            config = new Configuration();
        }
        Configuration.setInstance(config);
        final BiomeScheme biomeScheme = BiomeSchemeManager.getBiomeScheme(World2.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, null);
        if (biomeScheme == null) {
            System.err.println("Can't continue without a Minecraft 1.2 or 1.3 minecraft.jar");
            System.exit(1);
        }
        long seed = 0;
        final int[] biomes = new int[TILE_SIZE * TILE_SIZE];
        final SortedSet<World> largeContinentWorlds = new TreeSet<World>();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (World world: largeContinentWorlds) {
                    System.out.println(world);
                }
            }
        });
        while (true) {
//            System.out.println("***");
//            System.out.println("*** Seed " + seed + " ***");
//            System.out.println("***");
//                System.out.print('.');
            biomeScheme.setSeed(seed);
            int continentTilesFound = visitTilesInSpiral(new TileVisitor() {
                @Override
                public boolean visitBlock(int x, int z) {
//                    System.out.println("Visiting " + x + ", " + z);
                    biomeScheme.getBiomes(x * TILE_SIZE, z * TILE_SIZE, TILE_SIZE, TILE_SIZE, biomes);
                    for (int i = 0; i < biomes.length; i++) {
                        if (biomes[i] == BIOME_OCEAN) {
                            return false;
                        }
                    }
                    return true;
                }
            });
            if (largeContinentWorlds.isEmpty() || (continentTilesFound > largeContinentWorlds.first().continentTiles)) {
                if (largeContinentWorlds.size() > 100) {
                    largeContinentWorlds.remove(largeContinentWorlds.first());
                }
                largeContinentWorlds.add(new World(seed, continentTilesFound));
            }
            seed++;
        }
    }
    
    private static int visitTilesInSpiral(TileVisitor tileVisitor) {
        int count = 0;
        if (tileVisitor.visitBlock(0, 0)) {
            count++;
            int r = 1;
spiral:     while (true) {
                for (int x = r; x > -r; x--) {
                    if (! tileVisitor.visitBlock(x, r)) {
                        break spiral;
                    }
                    count++;
                }
                for (int z = r; z > -r; z--) {
                    if (! tileVisitor.visitBlock(-r, z)) {
                        break spiral;
                    }
                    count++;
                }
                for (int x = -r; x < r; x++) {
                    if (! tileVisitor.visitBlock(x, -r)) {
                        break spiral;
                    }
                    count++;
                }
                for (int z = -r; z < r; z++) {
                    if (! tileVisitor.visitBlock(r, z)) {
                        break spiral;
                    }
                    count++;
                }
                r++;
            }
        }
        return count;
    }
    
    interface TileVisitor {
        boolean visitBlock(int x, int z);
    }
    
    static class World implements Comparable<World> {
        World(long seed, int continentTiles) {
            this.seed = seed;
            this.continentTiles = continentTiles;
        }

        @Override
        public int compareTo(World o) {
            if (continentTiles > o.continentTiles) {
                return 1;
            } else if (continentTiles < o.continentTiles) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "Seed: " + seed + ", continent tiles: " + continentTiles;
        }
        
        final long seed;
        final int continentTiles;
    }
}