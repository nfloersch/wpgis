/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.biomeschemes;

import java.awt.Color;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Coordinates loading, saving and editing of custom biomes.
 * 
 * @author pepijn
 */
public class CustomBiomeManager {
    public CustomBiomeManager(Window parent) {
        this.parent = parent;
    }

    public List<CustomBiome> getCustomBiomes() {
        return customBiomes;
    }

    public void setCustomBiomes(List<CustomBiome> customBiomes) {
        List<CustomBiome> oldCustomBiomes = this.customBiomes;
        this.customBiomes = customBiomes;
        if (oldCustomBiomes != null) {
            for (CustomBiome customBiome: oldCustomBiomes) {
                for (CustomBiomeListener listener: listeners) {
                    listener.customBiomeRemoved(customBiome);
                }
            }
        }
        if (customBiomes != null) {
            for (CustomBiome customBiome: customBiomes) {
                for (CustomBiomeListener listener: listeners) {
                    listener.customBiomeAdded(customBiome);
                }
            }
        }
    }
    
    public void addCustomBiome() {
        int id = -1;
        if (customBiomes != null) {
outer:      for (int i = 23; i < 256; i++) {
                for (CustomBiome customBiome: customBiomes) {
                    if (customBiome.getId() == i) {
                        continue outer;
                    }
                }
                id = i;
                break;
            }
            if (id == -1) {
                JOptionPane.showMessageDialog(parent, "Maximum number of custom biomes reached", "Maximum Reached", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            id = 23;
        }
        CustomBiome customBiome = new CustomBiome("Custom", id, Color.ORANGE.getRGB());
        CustomBiomeDialog dialog = new CustomBiomeDialog(parent, customBiome, true);
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            if (customBiomes == null) {
                customBiomes = new ArrayList<CustomBiome>();
            }
            for (CustomBiome existingCustomBiome: customBiomes) {
                if (existingCustomBiome.getId() == customBiome.getId()) {
                    JOptionPane.showMessageDialog(parent, "You already configured a custom biome with that ID (named " + existingCustomBiome.getName() + ")", "ID Already In Use", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            customBiomes.add(customBiome);
            for (CustomBiomeListener listener: listeners) {
                listener.customBiomeAdded(customBiome);
            }
        }
    }
    
    public void editCustomBiome(CustomBiome customBiome) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public void removeCustomBiome() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public void addListener(CustomBiomeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(CustomBiomeListener listener) {
        listeners.remove(listener);
    }
    
    private final Window parent;
    private List<CustomBiome> customBiomes;
    private List<CustomBiomeListener> listeners = new ArrayList<CustomBiomeListener>();
    
    public interface CustomBiomeListener {
        void customBiomeAdded(CustomBiome customBiome);
        void customBiomeChanged(CustomBiome customBiome);
        void customBiomeRemoved(CustomBiome customBiome);
    }
}