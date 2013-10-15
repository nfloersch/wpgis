/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MapDragControl;
import org.pepsoft.worldpainter.RadiusControl;
import org.pepsoft.worldpainter.WorldPainter;

/**
 *
 * @author pepijn
 */
public class Flatten extends RadiusOperation {
    public Flatten(WorldPainter view, RadiusControl radiusControl, MapDragControl mapDragControl) {
        super("Flatten", "Flatten an area", view, radiusControl, mapDragControl, 100, true, "operation.flatten");
    }

    @Override
    protected void tick(int centerX, int centerY, boolean undo, boolean first, float dynamicLevel) {
        Dimension dimension = getDimension();
        if (first) {
            targetHeight = dimension.getHeightAt(centerX, centerY);
        }
//        System.out.println("targetHeight: " + targetHeight);
        dimension.setEventsInhibited(true);
        try {
            int radius = getRadius();
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    float currentHeight = dimension.getHeightAt(x, y);
                    float strength = dynamicLevel * getStrength(centerX, centerY, x, y);
                    float newHeight = strength * targetHeight  + (1f - strength) * currentHeight;
                    dimension.setHeightAt(x, y, newHeight);
//                    if (y == centerY) {
//                        System.out.printf("[%5d] [%7.5f] [%5d]\n", currentHeight, strength, newHeight);
//                    }
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }
    
    private float targetHeight;
}