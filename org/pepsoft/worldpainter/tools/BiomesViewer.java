/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author pepijn
 */
public class BiomesViewer extends WPTileSelectionViewer {
    public BiomesViewer() {
        // Do nothing
    }

    public BiomesViewer(boolean leftClickDrags) {
        super(leftClickDrags);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        super.paintComponent(g2);
        
        g2.drawImage(LEGEND, getWidth() - LEGEND.getWidth() - 10, getHeight() - LEGEND.getHeight() - 10, this);
    }
    
    private static final BufferedImage LEGEND;
    private static final long serialVersionUID = 1L;
    
    static {
        try {
            LEGEND = ImageIO.read(ClassLoader.getSystemResource("org/pepsoft/worldpainter/resources/small_legend.png"));
        } catch (IOException e) {
            throw new RuntimeException("I/O error loading image", e);
        }
    }
}