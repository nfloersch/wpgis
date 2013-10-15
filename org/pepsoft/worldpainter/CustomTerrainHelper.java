package org.pepsoft.worldpainter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.Map;
import org.pepsoft.minecraft.Material;

import static org.pepsoft.minecraft.Material.AIR;

/**
 * @author SchmitzP
 */
public class CustomTerrainHelper {
    public CustomTerrainHelper(int index) {
        this.index = index;
        icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 =icon.createGraphics();
        try {
            Map<Attribute, Object> attributes = new HashMap<Attribute, Object>();
            attributes.put(TextAttribute.FAMILY, Font.SANS_SERIF);
            attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            attributes.put(TextAttribute.SIZE, 16);
            if (index > 8) {
                attributes.put(TextAttribute.WIDTH, TextAttribute.WIDTH_CONDENSED);
            }
            g2.setFont(Font.getFont(attributes));
            g2.setColor(SHADOW_COLOUR);
            g2.drawString(Integer.toString(index + 1), (index < 9) ? 3 : 1, 15);
            g2.setColor(Color.BLACK);
            g2.drawString(Integer.toString(index + 1), (index < 9) ? 2 : 0, 14);
        } finally {
            g2.dispose();
        }
    }

    public Material getMaterial(long seed, int x, int y, int z, int height) {
        final int dz = z - height;
        if (dz > 0) {
            return AIR;
        } else {
            return Terrain.customMaterials[index].getMaterial(seed, x, y, z);
        }
    }

    public BufferedImage getIcon() {
        return icon;
    }

    public int getDefaultBiome() {
        return Terrain.customMaterials[index].getBiome();
    }
    
    public boolean isConfigured() {
        return Terrain.customMaterials[index] != null;
    }

    public String getName() {
        MixedMaterial material = Terrain.customMaterials[index];
        return (material != null) ? material.getName() : "Custom " + (index + 1);
    }
    
    public int getCustomTerrainIndex() {
        return index;
    }

    public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {
        MixedMaterial material = Terrain.customMaterials[index];
        Integer colour = (material != null) ? material.getColour() : null;
        return (colour != null) ? colour : colourScheme.getColour(getMaterial(seed, x, y, z, height));
    }
    
    private final int index;
    private final BufferedImage icon;
    
    private static final Color SHADOW_COLOUR = new Color(224, 224, 224);
}