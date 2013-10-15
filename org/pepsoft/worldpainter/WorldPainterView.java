/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.awt.Point;
import javax.swing.JComponent;

/**
 *
 * @author pepijn
 */
public abstract class WorldPainterView extends JComponent {
    public abstract Dimension getDimension();

    public abstract void setDimension(Dimension dimension);

    public abstract Point viewToImageCoordinates(Point viewCoordinates);

    public abstract Point imageToWorldCoordinates(Point imageCoordinates);

    public abstract Point viewToImageCoordinates(float x, float y);

    public abstract Point worldToImageCoordinates(int x, int y);
    
    public abstract void updateStatusBar(int x, int y);
}