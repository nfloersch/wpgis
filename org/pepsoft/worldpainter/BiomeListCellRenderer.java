/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.pepsoft.worldpainter.biomeschemes.AutoBiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import org.pepsoft.worldpainter.biomeschemes.CustomBiome;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.operations.BiomePaint;

/**
 *
 * @author pepijn
 */
public class BiomeListCellRenderer extends DefaultListCellRenderer {
    public BiomeListCellRenderer(BiomeScheme biomeScheme, ColourScheme colourScheme, CustomBiomeManager customBiomeManager) {
        this(biomeScheme, colourScheme, customBiomeManager, " ");
    }
    
    public BiomeListCellRenderer(BiomeScheme biomeScheme, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, String nullLabel) {
        this.nullLabel = nullLabel;
        if (biomeScheme == null) {
            biomeScheme = new AutoBiomeScheme(null);
        }
        for (int biome = 0; biome < biomeScheme.getBiomeCount(); biome++) {
            names[biome] = biomeScheme.getBiomeNames()[biome];
            icons[biome] = new ImageIcon(BiomeSchemeManager.createImage(biomeScheme, biome, colourScheme));
        }
        if (customBiomeManager.getCustomBiomes() != null) {
            for (CustomBiome customBiome: customBiomeManager.getCustomBiomes()) {
                names[customBiome.getId()] = customBiome.getName();
                icons[customBiome.getId()] = new ImageIcon(BiomePaint.createIcon(customBiome.getColour()));
            }
        }
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Integer) {
            int biome = (Integer) value;
            if (biome == -1) {
                setText(nullLabel);
            } else {
                setText(names[biome]);
                setIcon(icons[biome]);
            }
        }
        return this;
    }
 
    private final String[] names = new String[256];
    private final Icon[] icons = new Icon[256];
    private final String nullLabel;
    
    private static final long serialVersionUID = 1L;
}