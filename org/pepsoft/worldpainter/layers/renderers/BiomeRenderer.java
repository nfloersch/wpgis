/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.renderers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.pepsoft.util.ColourUtils;
import org.pepsoft.worldpainter.BiomeScheme;
import org.pepsoft.worldpainter.ColourScheme;

/**
 *
 * @author pepijn
 */
public class BiomeRenderer implements ByteLayerRenderer, ColourSchemeRenderer {
    public ColourScheme getColourScheme() {
        return colourScheme;
    }
    
    @Override
    public void setColourScheme(ColourScheme colourScheme) {
        this.colourScheme = colourScheme;
        if (biomeScheme != null) {
            int count = biomeScheme.getBiomeCount();
            colours = new int[count];
            for (int i = 0; i < count; i++) {
                colours[i] = biomeScheme.getColour(i, colourScheme);
            }
        }
    }
    
    public BiomeScheme getBiomeScheme() {
        return biomeScheme;
    }

    public void setBiomeScheme(BiomeScheme biomeScheme) {
        this.biomeScheme = biomeScheme;
        if (biomeScheme != null) {
            int count = biomeScheme.getBiomeCount();
            patterns = new boolean[count][][];
            for (int i = 0; i < count; i++) {
                patterns[i] = biomeScheme.getPattern(i);
            }
            if (colourScheme != null) {
                colours = new int[count];
                for (int i = 0; i < count; i++) {
                    colours[i] = biomeScheme.getColour(i, colourScheme);
                }
            }
        } else {
            patterns = null;
            colours = null;
        }
    }

    @Override
    public int getPixelColour(int x, int y, int underlyingColour, int value) {
        if (patterns == null) {
            // TODO: this should never happen, but we know it does from bug
            // reports. Investigate how this can happen!
            return underlyingColour;
        } else if (value >= patterns.length) {
            // TODO: this can happen if the user selects a Minecraft 1.1 biome
            // scheme but then tries to paint a 1.2 exclusive biome. Make that
            // impossible. It can also happen if a user imports a map with
            // custom, non-standard biomes
            return UNKNOWN_PATTERN[x & 0xF][y & 0xF] ? ColourUtils.mix(underlyingColour, BLACK) : underlyingColour;
        } else if ((patterns[value] != null) && patterns[value][x & 0xF][y & 0xF]) {
            return ColourUtils.mix(underlyingColour, BLACK);
        } else {
            return ColourUtils.mix(underlyingColour, colours[value]);
        }
    }

    private BiomeScheme biomeScheme;
    private ColourScheme colourScheme;
    private int[] colours;
    private boolean[][][] patterns;
    
    private static final boolean[][] UNKNOWN_PATTERN;
    private static final int BLACK = 0;
    
    static {
        try {
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/unknown_pattern.png"));
            UNKNOWN_PATTERN = createPattern(image);
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
}