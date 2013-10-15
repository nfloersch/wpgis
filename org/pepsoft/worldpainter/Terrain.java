/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import org.pepsoft.minecraft.Material;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.PerlinNoise;

import static org.pepsoft.minecraft.Constants.BLK_AIR;
import static org.pepsoft.minecraft.Constants.BLK_BEDROCK;
import static org.pepsoft.minecraft.Constants.BLK_CLAY;
import static org.pepsoft.minecraft.Constants.BLK_COBBLESTONE;
import static org.pepsoft.minecraft.Constants.BLK_DIRT;
import static org.pepsoft.minecraft.Constants.BLK_END_STONE;
import static org.pepsoft.minecraft.Constants.BLK_GRASS;
import static org.pepsoft.minecraft.Constants.BLK_GRAVEL;
import static org.pepsoft.minecraft.Constants.BLK_LAVA;
import static org.pepsoft.minecraft.Constants.BLK_MOSSY_COBBLESTONE;
import static org.pepsoft.minecraft.Constants.BLK_MYCELIUM;
import static org.pepsoft.minecraft.Constants.BLK_NETHERRACK;
import static org.pepsoft.minecraft.Constants.BLK_OBSIDIAN;
import static org.pepsoft.minecraft.Constants.BLK_SAND;
import static org.pepsoft.minecraft.Constants.BLK_SANDSTONE;
import static org.pepsoft.minecraft.Constants.BLK_SNOW;
import static org.pepsoft.minecraft.Constants.BLK_SNOW_BLOCK;
import static org.pepsoft.minecraft.Constants.BLK_SOUL_SAND;
import static org.pepsoft.minecraft.Constants.BLK_STONE;
import static org.pepsoft.minecraft.Constants.BLK_WATER;
import static org.pepsoft.minecraft.Material.AIR;
import static org.pepsoft.minecraft.Material.CACTUS;
import static org.pepsoft.minecraft.Material.COAL;
import static org.pepsoft.minecraft.Material.DANDELION;
import static org.pepsoft.minecraft.Material.DEAD_SHRUBS;
import static org.pepsoft.minecraft.Material.DIAMOND_ORE;
import static org.pepsoft.minecraft.Material.FERN;
import static org.pepsoft.minecraft.Material.FIRE;
import static org.pepsoft.minecraft.Material.GLOWSTONE;
import static org.pepsoft.minecraft.Material.GOLD_ORE;
import static org.pepsoft.minecraft.Material.IRON_ORE;
import static org.pepsoft.minecraft.Material.LAPIS_LAZULI_ORE;
import static org.pepsoft.minecraft.Material.REDSTONE_ORE;
import static org.pepsoft.minecraft.Material.ROSE;
import static org.pepsoft.minecraft.Material.TALL_GRASS;
import static org.pepsoft.worldpainter.Constants.HUGE_BLOBS;
import static org.pepsoft.worldpainter.Constants.SMALL_BLOBS;
import static org.pepsoft.worldpainter.Constants.TINY_BLOBS;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.BIOME_BEACH;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.BIOME_DESERT;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.BIOME_HELL;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.BIOME_ICE_PLAINS;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.BIOME_MUSHROOM_ISLAND;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.BIOME_PLAINS;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.BIOME_RIVER;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_2BiomeScheme.BIOME_SKY;

/**
 *
 * @author pepijn
 */

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//                                   WARNING!                                 //
//                                                                            //
// These values are saved in tiles and on disk by their name AND by their     //
// ordinal! It is therefore very important NOT to change the names OR the     //
// order, and to add new entries at the end!                                  //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

public enum Terrain {
    GRASS    ("Grass",                                    "grass with flowers, tall grass and ferns here and there", BIOME_PLAINS) {
        @Override
        public Material getMaterial(long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 1) {
                return AIR;
            } else if (dz == 1) {
                Random rnd = new Random(seed + (x * 65537) + (y * 4099));
                int rndNr = rnd.nextInt(FLOWER_INCIDENCE);
                if (rndNr == 0) {
                    if (dandelionNoise.getSeed() != (seed + DANDELION_SEED_OFFSET)) {
                        dandelionNoise.setSeed(seed + DANDELION_SEED_OFFSET);
                        roseNoise.setSeed(seed + ROSE_SEED_OFFSET);
                    }
                    if (dandelionNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, dz / SMALL_BLOBS) > FLOWER_CHANCE) {
                        return DANDELION;
                    } else if (roseNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, dz / SMALL_BLOBS) > FLOWER_CHANCE) {
                        return ROSE;
                    } else {
                        return AIR;
                    }
                } else {
                    if (grassNoise.getSeed() != (seed + GRASS_SEED_OFFSET)) {
                        grassNoise.setSeed(seed + GRASS_SEED_OFFSET);
                    }
                    float grassValue = grassNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, dz / SMALL_BLOBS) + (rnd.nextFloat() * 0.3f - 0.15f);
                    if (grassValue > FERN_CHANCE) {
                        return FERN;
                    } else if (grassValue > GRASS_CHANCE) {
                        return TALL_GRASS;
                    } else {
                        return AIR;
                    }
                }
            } else {
                return Material.GRASS;
            }
        }

        private final PerlinNoise dandelionNoise = new PerlinNoise(0);
        private final PerlinNoise roseNoise = new PerlinNoise(0);
        private final PerlinNoise grassNoise = new PerlinNoise(0);

        private static final long DANDELION_SEED_OFFSET = 145351781;
        private static final long ROSE_SEED_OFFSET = 28286488;
        private static final long GRASS_SEED_OFFSET = 169191195;
        private static final int FLOWER_INCIDENCE = 10;
    },
    DIRT     ("Dirt",      BLK_DIRT,      BLK_DIRT,       "bare dirt", BIOME_PLAINS),
    SAND     ("Sand",      BLK_SAND,      BLK_SAND,       "bare sand", BIOME_PLAINS),
    SANDSTONE("Sandstone", BLK_SANDSTONE, BLK_SANDSTONE,  "sandstone", BIOME_PLAINS),
    STONE    ("Stone",     BLK_STONE,     BLK_STONE,      "bare stone", BIOME_PLAINS),
    ROCK     ("Rock",                                     "a mix of stone and cobblestone", BIOME_PLAINS) {
        @Override
        public Material getMaterial(long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 0) {
                return AIR;
            } else {
                if (perlinNoise.getSeed() != (seed + STONE_SEED_OFFSET)) {
                    perlinNoise.setSeed(seed + STONE_SEED_OFFSET);
                }
                if (perlinNoise.getPerlinNoise(x / TINY_BLOBS, y / TINY_BLOBS, z / TINY_BLOBS) > 0) {
                    return Material.STONE;
                } else {
                    return Material.COBBLESTONE;
                }
            }
        }

        private final PerlinNoise perlinNoise = new PerlinNoise(0);

        private static final int STONE_SEED_OFFSET = 188434540;
    },
    WATER    ("Water",     BLK_WATER,     BLK_WATER,      "flowing water", BIOME_RIVER),
    LAVA     ("Lava",      BLK_LAVA,      BLK_LAVA,       "flowing lava", BIOME_PLAINS),
    SNOW     ("Snow on Rock",                             "a thin layer of snow on a mix of stone and cobblestone", BIOME_ICE_PLAINS) {
        @Override
        public Material getMaterial(long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 1) {
                return AIR;
            } else if (dz == 1) {
                return Material.SNOW;
            } else {
                if (perlinNoise.getSeed() != (seed + STONE_SEED_OFFSET)) {
                    perlinNoise.setSeed(seed + STONE_SEED_OFFSET);
                }
                if (perlinNoise.getPerlinNoise(x / TINY_BLOBS, y / TINY_BLOBS, z / TINY_BLOBS) > 0) {
                    return Material.STONE;
                } else {
                    return Material.COBBLESTONE;
                }
            }
        }

        @Override
        public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {
            return colourScheme.getColour(BLK_SNOW);
        }

        private final PerlinNoise perlinNoise = new PerlinNoise(0);

        private static final int STONE_SEED_OFFSET = 188434540;
    },
    DEEP_SNOW("Deep Snow", BLK_SNOW_BLOCK, BLK_SNOW_BLOCK, BLK_SNOW, "a thick layer of snow", BIOME_ICE_PLAINS),
    GRAVEL("Gravel",       BLK_GRAVEL,    BLK_GRAVEL,     "gravel", BIOME_PLAINS),
    CLAY("Clay",           BLK_CLAY,      BLK_CLAY,       "clay", BIOME_PLAINS),
    COBBLESTONE("Cobblestone", BLK_COBBLESTONE, BLK_COBBLESTONE, "cobblestone", BIOME_PLAINS),
    MOSSY_COBBLESTONE("Mossy Cobblestone", BLK_MOSSY_COBBLESTONE, BLK_MOSSY_COBBLESTONE, "mossy cobblestone", BIOME_PLAINS),
    NETHERRACK("Netherrack", BLK_NETHERRACK, BLK_NETHERRACK, "netherrack", BIOME_PLAINS),
    SOUL_SAND("Soul Sand", BLK_SOUL_SAND, BLK_SOUL_SAND,  "soul sand", BIOME_PLAINS),
    OBSIDIAN("Obsidian",   BLK_OBSIDIAN,  BLK_OBSIDIAN,   "extremely tough volcanic glass", BIOME_PLAINS),
    BEDROCK("Bedrock",     BLK_BEDROCK,   BLK_BEDROCK,    "unbreakable bedrock", BIOME_PLAINS),
    DESERT("Desert",                                      "sand with here and there a cactus or dead shrub", BIOME_DESERT) {
        @Override
        public Material getMaterial(long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz <= 0) {
                return Material.SAND;
            } else {
                int rnd = new Random(seed + (x * 65537) + (y * 4099)).nextInt(CACTUS_CHANCE);
                int cactusHeight;
                boolean shrub = false;
                if (rnd < 3) {
                    cactusHeight = rnd + 1;
                } else {
                    cactusHeight = 0;
                    if (rnd < 6) {
                        shrub = true;
                    }
                }
                if (dz > cactusHeight) {
                    if ((dz == 1) && shrub) {
                        return DEAD_SHRUBS;
                    } else {
                        return AIR;
                    }
                } else {
                    return CACTUS;
                }
            }
        }

        private static final int CACTUS_CHANCE = 1000;
    },
    NETHERLIKE("Netherlike",                              "netherrack with pockets of lava, soul sand and glowstone", BIOME_HELL) {
        @Override
        public Material getMaterial(long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 1) {
                return AIR;
            } else if (dz == 1) {
                int rnd = new Random(seed + (x * 65537) + (y * 4099)).nextInt(FIRE_CHANCE);
                if (rnd == 0) {
                    return FIRE;
                } else {
                    return AIR;
                }
            } else {
                if (glowstoneNoise.getSeed() != (seed + GLOWSTONE_SEED_OFFSET)) {
                    glowstoneNoise.setSeed(seed + GLOWSTONE_SEED_OFFSET);
                    soulSandNoise.setSeed(seed + SOUL_SAND_SEED_OFFSET);
                    lavaNoise.setSeed(seed + LAVA_SEED_OFFSET);
                }
                if (glowstoneNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > .4) {
                    return GLOWSTONE;
                } else if(soulSandNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > .4) {
                    return Material.SOUL_SAND;
                } else if(lavaNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > .4) {
                    return Material.LAVA;
                } else {
                    return Material.NETHERRACK;
                }
            }
        }

        private final PerlinNoise glowstoneNoise = new PerlinNoise(0);
        private final PerlinNoise soulSandNoise = new PerlinNoise(0);
        private final PerlinNoise lavaNoise = new PerlinNoise(0);

        private static final int GLOWSTONE_SEED_OFFSET =  57861047;
        private static final int LAVA_SEED_OFFSET      = 189831882;
        private static final int SOUL_SAND_SEED_OFFSET =  81867522;
        private static final int FIRE_CHANCE           =       150;
    },
    @Deprecated
    RESOURCES("Resources",                                "stone on the surface with pockets of coal, ores, gravel and dirt, lava and water, etc.", BIOME_PLAINS) {
        @Override
        public Material getMaterial(long seed, int x, int y, int z, int height) {
            if (z > height) {
                return AIR;
            } else {
                if (goldNoise.getSeed() != (seed + GOLD_SEED_OFFSET)) {
                    goldNoise.setSeed(       seed + GOLD_SEED_OFFSET);
                    ironNoise.setSeed(       seed + IRON_SEED_OFFSET);
                    coalNoise.setSeed(       seed + COAL_SEED_OFFSET);
                    lapisLazuliNoise.setSeed(seed + LAPIS_LAZULI_SEED_OFFSET);
                    diamondNoise.setSeed(    seed + DIAMOND_SEED_OFFSET);
                    redstoneNoise.setSeed(   seed + REDSTONE_SEED_OFFSET);
                    waterNoise.setSeed(      seed + WATER_SEED_OFFSET);
                    lavaNoise.setSeed(       seed + LAVA_SEED_OFFSET);
                    dirtNoise.setSeed(       seed + DIRT_SEED_OFFSET);
                    gravelNoise.setSeed(     seed + GRAVEL_SEED_OFFSET);
                }
                double dx = x / TINY_BLOBS, dy = y / TINY_BLOBS, dz = z / TINY_BLOBS;
                double dirtX = x / SMALL_BLOBS, dirtY = y / SMALL_BLOBS, dirtZ = z / SMALL_BLOBS;
                if ((z <= COAL_LEVEL) && (coalNoise.getPerlinNoise(dx, dy, dz) >= COAL_CHANCE)) {
                    return COAL;
                } else if ((z <= DIRT_LEVEL) && (dirtNoise.getPerlinNoise(dirtX, dirtY, dirtZ) >= DIRT_CHANCE)) {
                    return Material.DIRT;
                } else if ((z <= GRAVEL_LEVEL) && (gravelNoise.getPerlinNoise(dirtX, dirtY, dirtZ) >= GRAVEL_CHANCE)) {
                    return Material.GRAVEL;
                } else if ((z <= REDSTONE_LEVEL) && (redstoneNoise.getPerlinNoise(dx, dy, dz) >= REDSTONE_CHANCE)) {
                    return REDSTONE_ORE;
                } else if ((z <= IRON_LEVEL) && (ironNoise.getPerlinNoise(dx, dy, dz) >= IRON_CHANCE)) {
                    return IRON_ORE;
                } else if ((z <= WATER_LEVEL) && (waterNoise.getPerlinNoise(dx, dy, dz) >= WATER_CHANCE)) {
                    return Material.WATER;
                } else if ((z <= LAVA_LEVEL) && (lavaNoise.getPerlinNoise(dx, dy, dz) >= (LAVA_CHANCE + (z * z / 65536f)))) {
    //                System.out.println("Lava at level " + z);
    //                if (z > highestLava) {
    //                    highestLava = z;
    //                }
    //                System.out.println("Highest lava: " + highestLava);
                    return Material.LAVA;
                } else if ((z <= GOLD_LEVEL) && (goldNoise.getPerlinNoise(dx, dy, dz) >= GOLD_CHANCE)) {
                    return GOLD_ORE;
                } else if ((z <= LAPIS_LAZULI_LEVEL) && (lapisLazuliNoise.getPerlinNoise(dx, dy, dz) >= LAPIS_LAZULI_CHANCE)) {
                    return LAPIS_LAZULI_ORE;
                } else if ((z <= DIAMOND_LEVEL) && (diamondNoise.getPerlinNoise(dx, dy, dz) >= DIAMOND_CHANCE)) {
                    return DIAMOND_ORE;
                } else {
                    return Material.STONE;
                }
            }
        }
        

        private final PerlinNoise goldNoise        = new PerlinNoise(0);
        private final PerlinNoise ironNoise        = new PerlinNoise(0);
        private final PerlinNoise coalNoise        = new PerlinNoise(0);
        private final PerlinNoise lapisLazuliNoise = new PerlinNoise(0);
        private final PerlinNoise diamondNoise     = new PerlinNoise(0);
        private final PerlinNoise redstoneNoise    = new PerlinNoise(0);
        private final PerlinNoise waterNoise       = new PerlinNoise(0);
        private final PerlinNoise lavaNoise        = new PerlinNoise(0);
        private final PerlinNoise dirtNoise        = new PerlinNoise(0);
        private final PerlinNoise gravelNoise      = new PerlinNoise(0);

//        private int highestLava = 0;

        private static final long GOLD_SEED_OFFSET         = 148503743;
        private static final long IRON_SEED_OFFSET         = 171021655;
        private static final long COAL_SEED_OFFSET         = 81779663;
        private static final long LAPIS_LAZULI_SEED_OFFSET = 174377337;
        private static final long DIAMOND_SEED_OFFSET      = 14554756;
        private static final long REDSTONE_SEED_OFFSET     = 48636151;
        private static final long WATER_SEED_OFFSET        = 42845153;
        private static final long LAVA_SEED_OFFSET         = 62452072;
        private static final long DIRT_SEED_OFFSET         = 193567846;
        private static final long GRAVEL_SEED_OFFSET       = 19951397;
    },
    BEACHES("Beaches",                                    "grass with patches of sand, gravel and clay", BIOME_BEACH) {
        @Override
        public Material getMaterial(long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 0) {
                return AIR;
            } else {
                if (sandNoise.getSeed() != (seed + SAND_SEED_OFFSET)) {
                    sandNoise.setSeed(seed + SAND_SEED_OFFSET);
                    clayNoise.setSeed(seed + CLAY_SEED_OFFSET);
                }
                float noise = clayNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS);
                if (noise >= BEACH_CLAY_CHANCE) {
                    return Material.CLAY;
                } else {
                    noise = sandNoise.getPerlinNoise(x / HUGE_BLOBS, y / HUGE_BLOBS, z / SMALL_BLOBS);
                    noise += sandNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) / 2;
                    if (noise >= BEACH_SAND_CHANCE) {
                        return Material.SAND;
                    } else if (-noise >= BEACH_GRAVEL_CHANCE) {
                        return Material.GRAVEL;
                    } else if (dz == 0) {
                        return Material.GRASS;
                    } else {
                        return Material.DIRT;
                    }
                }
            }
        }
        
        private final PerlinNoise sandNoise = new PerlinNoise(0);
        private final PerlinNoise clayNoise = new PerlinNoise(0);

        private static final long SAND_SEED_OFFSET = 26796036;
        private static final long CLAY_SEED_OFFSET = 161603308;
    },
    CUSTOM_1("Custom 1",                                  "custom material one", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(0);
    },
    CUSTOM_2("Custom 2",                                  "custom material two", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(1);
    },
    CUSTOM_3("Custom 3",                                  "custom material three", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(2);
    },
    CUSTOM_4("Custom 4",                                  "custom material four", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(3);
    },
    CUSTOM_5("Custom 5",                                  "custom material five", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(4);
    },
    MYCELIUM("Mycelium", BLK_MYCELIUM, BLK_DIRT,          "mycelium", BIOME_MUSHROOM_ISLAND),
    END_STONE("End Stone", BLK_END_STONE, BLK_END_STONE,  "end stone", BIOME_SKY),
    BARE_GRASS("Bare Grass", BLK_GRASS, BLK_GRASS,         "bare grass (no flowers, etc.)", BIOME_PLAINS),
    CUSTOM_6("Custom 6",                                  "custom material six", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(5);
    },
    CUSTOM_7("Custom 7",                                  "custom material seven", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(6);
    },
    CUSTOM_8("Custom 8",                                  "custom material eight", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(7);
    },
    CUSTOM_9("Custom 9",                                  "custom material nine", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(8);
    },
    CUSTOM_10("Custom 10",                                "custom material ten", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(9);
    },
    CUSTOM_11("Custom 11",                                "custom material eleven", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(10);
    },
    CUSTOM_12("Custom 12",                                "custom material twelve", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(11);
    },
    CUSTOM_13("Custom 13",                                "custom material thirteen", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(12);
    },
    CUSTOM_14("Custom 14",                                "custom material fourteen", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(13);
    },
    CUSTOM_15("Custom 15",                                "custom material fifteen", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(14);
    },
    CUSTOM_16("Custom 16",                                "custom material sixteen", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(15);
    },
    CUSTOM_17("Custom 17",                                "custom material seventeen", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(16);
    },
    CUSTOM_18("Custom 18",                                "custom material eighteen", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(17);
    },
    CUSTOM_19("Custom 19",                                "custom material nineteen", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(18);
    },
    CUSTOM_20("Custom 20",                                "custom material twenty", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(19);
    },
    CUSTOM_21("Custom 21",                                "custom material twenty-one", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(20);
    },
    CUSTOM_22("Custom 22",                                "custom material twenty-two", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(21);
    },
    CUSTOM_23("Custom 23",                                "custom material twenty-three", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(22);
    },
    CUSTOM_24("Custom 24",                                "custom material twenty-four", BIOME_PLAINS) {
        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon() {return helper.getIcon();}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}
        
        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(23);
    };

    private Terrain(String name, String description, int defaultBiome) {
        this(name, BLK_STONE, BLK_STONE, BLK_AIR, description, defaultBiome);
    }

    private Terrain(String name, int topMaterial, int topLayerMaterial, String description, int defaultBiome) {
        this(name, topMaterial, topLayerMaterial, BLK_AIR, description, defaultBiome);
    }

    private Terrain(String name, int topMaterial, int topLayerMaterial, int topping, String description, int defaultBiome) {
        this.name = name;
        this.topMaterial = Material.get(topMaterial);
        this.topLayerMaterial = Material.get(topLayerMaterial);
        this.topping = Material.get(topping);
        this.toppingHeight = (topping == BLK_AIR) ? 0 : 1;
        this.description = description;
        this.defaultBiome = defaultBiome;
        BufferedImage largeIcon = IconUtils.loadImage("org/pepsoft/worldpainter/icons/" + name().toLowerCase() + ".png");
        icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = icon.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(largeIcon, 0, 0, 16, 16, null);
        } finally {
            g2.dispose();
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Get the block type to use for this material at a specific location in the
     * world, relative to the surface.
     *
     * @param seed The world seed.
     * @param x The absolute X position of the block in WorldPainter coordinates.
     * @param y The absolute Y position of the block in WorldPainter coordinates.
     * @param z The absolute Z position of the block in WorldPainter coordinates.
     * @param height The height of the terrain at the specified X and Y
     *     coordinates.
     * @return
     */
    public Material getMaterial(final long seed, final int x, final int y, final int z, final int height) {
        final int dz = z - height;
        if (dz > toppingHeight) {
            return Material.AIR;
        } else if (dz > 0) {
            return topping;
        } else if (dz == 0) {
            return topMaterial;
        } else {
            return topLayerMaterial;
        }
    }

    public String getDescription() {
        return description;
    }

    public BufferedImage getIcon() {
        return icon;
    }

    public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {
        return colourScheme.getColour(getMaterial(seed, x, y, z, height));
    }

    public int getDefaultBiome() {
        return defaultBiome;
    }
    
    public boolean isCustom() {
        return false;
    }

    public boolean isConfigured() {
        return true;
    }
    
    public int getCustomTerrainIndex() {
        throw new IllegalArgumentException("Not a custom terrain");
    }
    
    public static MixedMaterial getCustomMaterial(int index) {
        return customMaterials[index];
    }
    
    public static void setCustomMaterial(int index, MixedMaterial material) {
        customMaterials[index] = material;
    }
    
    public static Terrain getCustomTerrain(int index) {
        return VALUES[index + 25];
    }
    
    public static Terrain[] getConfiguredValue() {
        ArrayList<Terrain> values = new ArrayList<Terrain>(VALUES.length);
        for (Terrain terrain: VALUES) {
            if ((! terrain.isCustom()) || terrain.isConfigured()) {
                values.add(terrain);
            }
        }
        return values.toArray(new Terrain[values.size()]);
    }

    private final Material topMaterial, topLayerMaterial, topping;
    private final int toppingHeight;
    private final String name, description;
    private final BufferedImage icon;
    private final int defaultBiome;
    
    public static final int CUSTOM_TERRAIN_COUNT = 24;

    static final MixedMaterial[] customMaterials = new MixedMaterial[CUSTOM_TERRAIN_COUNT];

    static final int GOLD_LEVEL         = 32;
    static final int IRON_LEVEL         = 48;
    static final int COAL_LEVEL         = Integer.MAX_VALUE;
    static final int LAPIS_LAZULI_LEVEL = 32;
    static final int DIAMOND_LEVEL      = 16;
    static final int REDSTONE_LEVEL     = 16;
    static final int WATER_LEVEL        = Integer.MAX_VALUE;
    static final int LAVA_LEVEL         = 80;
    static final int DIRT_LEVEL         = Integer.MAX_VALUE;
    static final int GRAVEL_LEVEL       = Integer.MAX_VALUE;
        
    static final float GOLD_CHANCE         = PerlinNoise.getLevelForPromillage(1);
    static final float IRON_CHANCE         = PerlinNoise.getLevelForPromillage(5);
    static final float COAL_CHANCE         = PerlinNoise.getLevelForPromillage(9);
    static final float LAPIS_LAZULI_CHANCE = PerlinNoise.getLevelForPromillage(1);
    static final float DIAMOND_CHANCE      = PerlinNoise.getLevelForPromillage(1);
    static final float REDSTONE_CHANCE     = PerlinNoise.getLevelForPromillage(6);
    static final float WATER_CHANCE        = PerlinNoise.getLevelForPromillage(1);
    static final float LAVA_CHANCE         = PerlinNoise.getLevelForPromillage(1);
    static final float DIRT_CHANCE         = PerlinNoise.getLevelForPromillage(9);
    static final float GRAVEL_CHANCE       = PerlinNoise.getLevelForPromillage(9);
    
    static final float FLOWER_CHANCE       = PerlinNoise.getLevelForPromillage(10);
    static final float FERN_CHANCE         = PerlinNoise.getLevelForPromillage(10);
    static final float GRASS_CHANCE        = PerlinNoise.getLevelForPromillage(100);

    static final float BEACH_SAND_CHANCE   = PerlinNoise.getLevelForPromillage(400) * 1.5f;
    static final float BEACH_GRAVEL_CHANCE = PerlinNoise.getLevelForPromillage(200) * 1.5f;
    static final float BEACH_CLAY_CHANCE =   PerlinNoise.getLevelForPromillage(40);
    
    public static final Terrain[] VALUES = {
        Terrain.GRASS,
        Terrain.BARE_GRASS,
        Terrain.DIRT,
        Terrain.SAND,
        Terrain.DESERT,
        Terrain.SANDSTONE,
        Terrain.STONE,
        Terrain.ROCK,
        Terrain.COBBLESTONE,
        Terrain.MOSSY_COBBLESTONE,
        Terrain.OBSIDIAN,
        Terrain.BEDROCK,
        Terrain.GRAVEL,
        Terrain.CLAY,
        Terrain.BEACHES,
        Terrain.WATER,
        Terrain.LAVA,
        Terrain.SNOW,
        Terrain.DEEP_SNOW,
        Terrain.NETHERRACK,
        Terrain.SOUL_SAND,
        Terrain.NETHERLIKE,
        Terrain.MYCELIUM,
        Terrain.END_STONE,
        Terrain.RESOURCES,
        Terrain.CUSTOM_1,
        Terrain.CUSTOM_2,
        Terrain.CUSTOM_3,
        Terrain.CUSTOM_4,
        Terrain.CUSTOM_5,
        Terrain.CUSTOM_6,
        Terrain.CUSTOM_7,
        Terrain.CUSTOM_8,
        Terrain.CUSTOM_9,
        Terrain.CUSTOM_10,
        Terrain.CUSTOM_11,
        Terrain.CUSTOM_12,
        Terrain.CUSTOM_13,
        Terrain.CUSTOM_14,
        Terrain.CUSTOM_15,
        Terrain.CUSTOM_16,
        Terrain.CUSTOM_17,
        Terrain.CUSTOM_18,
        Terrain.CUSTOM_19,
        Terrain.CUSTOM_20,
        Terrain.CUSTOM_21,
        Terrain.CUSTOM_22,
        Terrain.CUSTOM_23,
        Terrain.CUSTOM_24
    };
}