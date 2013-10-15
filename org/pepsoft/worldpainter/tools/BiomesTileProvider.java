/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.colourschemes.DynMapColourScheme;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.pepsoft.util.swing.TileListener;
import org.pepsoft.util.swing.TileProvider;
import org.pepsoft.worldpainter.BiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_2BiomeScheme;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.minecraft.Constants.*;

/**
 *
 * @author pepijn
 */
public class BiomesTileProvider implements TileProvider {
    public BiomesTileProvider(BiomeScheme biomeScheme) {
        this.biomeScheme = biomeScheme;
    }

    public BiomesTileProvider(BiomeScheme biomeScheme, int zoom) {
        this.biomeScheme = biomeScheme;
        this.zoom = zoom;
    }
    
    @Override
    public int getTileSize() {
        return TILE_SIZE;
    }
    
    @Override
    public int getZoom() {
        return zoom;
    }
    
    @Override
    public void setZoom(int zoom) {
        this.zoom = zoom;
        bufferRef.remove();
    }

    @Override
    public BufferedImage getTile(int tileX, int tileY) {
        try {
            BufferedImage tile;
            if (createOptimalImage) {
                tile = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(TILE_SIZE, TILE_SIZE);
            } else {
                tile = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
            }
            int[] buffer = bufferRef.get();
            if (buffer == null) {
                buffer = new int[TILE_SIZE * TILE_SIZE * zoom * zoom];
                bufferRef.set(buffer);
            }
            biomeScheme.getBiomes(tileX * TILE_SIZE * zoom, tileY * TILE_SIZE * zoom, TILE_SIZE * zoom, TILE_SIZE * zoom, buffer);
            int[][] biomeCounts = biomeCountsRef.get();
            if (biomeCounts == null) {
                biomeCounts = new int[][] {new int[23], new int[23], new int[23]};
                biomeCountsRef.set(biomeCounts);
            }
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int y = 0; y < TILE_SIZE; y++) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < biomeCounts[i].length; j++) {
                            biomeCounts[i][j] = 0;
                        }
                    }
                    for (int dx = 0; dx < zoom; dx++) {
                        for (int dz = 0; dz < zoom; dz++) {
                            int biome = buffer[x * zoom + dx + (y * zoom + dz) * TILE_SIZE * zoom];
                            biomeCounts[BIOME_PRIORITIES[biome]][biome]++;
                        }
                    }
                    int mostCommonBiome = -1;
                    for (int i = 2; i >= 0; i--) {
                        int mostCommonBiomeCount = 0;
                        for (int j = 0; j < biomeCounts[i].length; j++) {
                            if (biomeCounts[i][j] > mostCommonBiomeCount) {
                                mostCommonBiome = j;
                                mostCommonBiomeCount = biomeCounts[i][j];
                            }
                        }
                        if (mostCommonBiome != -1) {
                            break;
                        }
                    }
                    if ((BIOME_PATTERNS[mostCommonBiome] != null) && (BIOME_PATTERNS[mostCommonBiome][x % 16][y % 16])) {
                        tile.setRGB(x, y, 0);
                    } else {
                        tile.setRGB(x, y, BIOME_COLOURS[mostCommonBiome]);
                    }
                }
            }
            return tile;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    public void addTileListener(TileListener tileListener) {
        // Do nothing (tiles never change)
    }

    @Override
    public void removeTileListener(TileListener tileListener) {
        // Do nothing (tiles never change)
    }
    
    private final BiomeScheme biomeScheme;
    private final ThreadLocal<int[]> bufferRef = new ThreadLocal<int[]>();
    private final ThreadLocal<int[][]> biomeCountsRef = new ThreadLocal<int[][]>();
    private int zoom = 4;
    
    private static final int[] BIOME_PRIORITIES = {
        0,
        1,
        1,
        1,
        1,
        1,
        1,
        2,
        1,
        1,
        0,
        2,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1
    };
    private static final ColourScheme COLOUR_SCHEME = new DynMapColourScheme("default");
    private static final int[] BIOME_COLOURS = {
        COLOUR_SCHEME.getColour(BLK_WATER),
        COLOUR_SCHEME.getColour(BLK_GRASS),
        COLOUR_SCHEME.getColour(BLK_SAND),
        COLOUR_SCHEME.getColour(BLK_GRASS),
        COLOUR_SCHEME.getColour(BLK_LEAVES),
        COLOUR_SCHEME.getColour(BLK_SNOW),
        COLOUR_SCHEME.getColour(BLK_LEAVES),
        COLOUR_SCHEME.getColour(BLK_WATER),
        COLOUR_SCHEME.getColour(BLK_NETHERRACK),
        COLOUR_SCHEME.getColour(BLK_AIR),
        COLOUR_SCHEME.getColour(BLK_ICE),
        COLOUR_SCHEME.getColour(BLK_ICE),
        COLOUR_SCHEME.getColour(BLK_SNOW),
        COLOUR_SCHEME.getColour(BLK_SNOW),
        COLOUR_SCHEME.getColour(BLK_MYCELIUM),
        COLOUR_SCHEME.getColour(BLK_MYCELIUM),
        COLOUR_SCHEME.getColour(BLK_SAND),
        COLOUR_SCHEME.getColour(BLK_SAND),
        COLOUR_SCHEME.getColour(BLK_LEAVES),
        COLOUR_SCHEME.getColour(BLK_SNOW),
        COLOUR_SCHEME.getColour(BLK_GRASS),
        COLOUR_SCHEME.getColour(BLK_LEAVES),
        COLOUR_SCHEME.getColour(BLK_LEAVES)
    };

    private static final boolean[][][] BIOME_PATTERNS = new boolean[23][][];
    static {
        try {
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/swamp_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_SWAMPLAND] = createPattern(image);

            image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/mountains_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_EXTREME_HILLS] = createPattern(image);
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_ICE_MOUNTAINS] = createPattern(image);

            image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/deciduous_trees_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_FOREST] = createPattern(image);

            image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/pine_trees_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_TAIGA] = createPattern(image);

            image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/hills_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_DESERT_HILLS] = createPattern(image);
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_EXTREME_HILLS_EDGE] = createPattern(image);

            image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/deciduous_hills_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_FOREST_HILLS] = createPattern(image);

            image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/pine_hills_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_TAIGA_HILLS] = createPattern(image);

            image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/jungle_trees_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_JUNGLE] = createPattern(image);

            image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/jungle_hills_pattern.png"));
            BIOME_PATTERNS[Minecraft1_2BiomeScheme.BIOME_JUNGLE_HILLS] = createPattern(image);
        } catch (IOException e) {
            throw new RuntimeException("I/O error loading image", e);
        }
    }
    
    private static boolean[][] createPattern(BufferedImage image) {
        boolean[][] pattern = new boolean[16][];
        for (int x = 0; x < 16; x++) {
            pattern[x] = new boolean[16];
            for (int y = 0; y < 16; y++) {
                pattern[x][y] = image.getRGB(x, y) != -1;
            }
        }
        return pattern;
    }

    private final boolean createOptimalImage = ! "false".equalsIgnoreCase(System.getProperty("org.pepsoft.worldpainter.createOptimalImage"));
}