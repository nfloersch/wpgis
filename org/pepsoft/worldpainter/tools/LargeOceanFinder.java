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
public class LargeOceanFinder {
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
        final SortedSet<World> largeOceanWorlds = new TreeSet<World>();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (World world: largeOceanWorlds) {
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
            final int [] oceanTilesFound = new int[] {0};
            int oceanOrMushroomTilesFound = visitTilesInSpiral(new TileVisitor() {
                @Override
                public boolean visitBlock(int x, int z) {
//                    System.out.println("Visiting " + x + ", " + z);
                    biomeScheme.getBiomes(x * TILE_SIZE, z * TILE_SIZE, TILE_SIZE, TILE_SIZE, biomes);
                    boolean mushroomTile = false;
                    for (int i = 0; i < biomes.length; i++) {
                        if ((biomes[i] == BIOME_MUSHROOM_ISLAND_SHORE) || (biomes[i] == BIOME_MUSHROOM_ISLAND)) {
                            mushroomTile = true;
                        } else if (biomes[i] != BIOME_OCEAN) {
                            return false;
                        }
                    }
                    if (mushroomTile) {
                        mushroomTileFound = true;
                    } else if (! mushroomTileFound) {
                        oceanTilesFound[0]++;
                    }
                    return true;
                }

                private boolean mushroomTileFound;
            });
            if (largeOceanWorlds.isEmpty() || (oceanOrMushroomTilesFound > largeOceanWorlds.first().oceanOrMushroomTiles)) {
                if (largeOceanWorlds.size() > 100) {
                    largeOceanWorlds.remove(largeOceanWorlds.first());
                }
                largeOceanWorlds.add(new World(seed, oceanTilesFound[0], oceanOrMushroomTilesFound));
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
        World(long seed, int oceanTiles, int oceanOrMushroomTiles) {
            this.seed = seed;
            this.oceanTiles = oceanTiles;
            this.oceanOrMushroomTiles = oceanOrMushroomTiles;
        }

        @Override
        public int compareTo(World o) {
            if (oceanOrMushroomTiles > o.oceanOrMushroomTiles) {
                return 1;
            } else if (oceanOrMushroomTiles < o.oceanOrMushroomTiles) {
                return -1;
            } else if (oceanTiles > o.oceanTiles) {
                return 1;
            } else if (oceanTiles < o.oceanTiles) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "Seed: " + seed + ", ocean tiles: " + oceanTiles + " , ocean or mushroom island tiles: " + oceanOrMushroomTiles;
        }
        
        final long seed;
        final int oceanTiles, oceanOrMushroomTiles;
    }
}