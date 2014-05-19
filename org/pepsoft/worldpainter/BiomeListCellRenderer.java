/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.pepsoft.worldpainter.biomeschemes.BiomeHelper;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;

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
        biomeHelper = new BiomeHelper(biomeScheme, colourScheme, customBiomeManager);
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Integer) {
            int biome = (Integer) value;
            if (biome == -1) {
                setText(nullLabel);
            } else {
                setText(biomeHelper.getBiomeName(biome));
                setIcon(biomeHelper.getBiomeIcon(biome));
            }
        }
        return this;
    }
 
    private final BiomeHelper biomeHelper;
    private final String nullLabel;
    
    private static final long serialVersionUID = 1L;
}
