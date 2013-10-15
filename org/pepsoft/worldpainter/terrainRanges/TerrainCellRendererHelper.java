/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.terrainRanges;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.pepsoft.worldpainter.Terrain;

/**
 *
 * @author pepijn
 */
class TerrainCellRendererHelper {
    void configure(JLabel label, Terrain terrain) {
        if (terrain != null) {
            BufferedImage image = terrain.getIcon();
            ImageIcon icon = iconCache.get(image);
            if (icon == null) {
                icon = new ImageIcon(image);
                iconCache.put(image, icon);
            }
            label.setIcon(icon);
            label.setText(terrain.getName());
        }
    }
    
    private final Map<BufferedImage, ImageIcon> iconCache = new HashMap<BufferedImage, ImageIcon>();
}