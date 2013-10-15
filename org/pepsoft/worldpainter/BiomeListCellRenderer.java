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
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;

/**
 *
 * @author pepijn
 */
public class BiomeListCellRenderer extends DefaultListCellRenderer {
    public BiomeListCellRenderer(BiomeScheme biomeScheme, ColourScheme colourScheme) {
        this.biomeScheme = biomeScheme;
        this.colourScheme = colourScheme;
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        int biome = (Integer) value;
        setText(biomeScheme.getBiomeNames()[biome]);
        if (iconCache == null) {
            iconCache = new Icon[biomeScheme.getBiomeCount()];
        }
        if (iconCache[biome] == null) {
            iconCache[biome] = new ImageIcon(BiomeSchemeManager.createImage(biomeScheme, biome, colourScheme));
        }
        setIcon(iconCache[biome]);
        return this;
    }
 
    private final BiomeScheme biomeScheme;
    private final ColourScheme colourScheme;
    private Icon[] iconCache;
    
    private static final long serialVersionUID = 1L;
}