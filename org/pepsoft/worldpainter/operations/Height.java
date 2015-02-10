/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MapDragControl;
import org.pepsoft.worldpainter.RadiusControl;
import org.pepsoft.worldpainter.WorldPainter;
import org.pepsoft.worldpainter.panels.FilterImpl;

/**
 *
 * @author pepijn
 */
public class Height extends RadiusOperation {
    public Height(WorldPainter view, RadiusControl radiusControl, MapDragControl mapDragControl) {
        super("Height", "Raise or lower the terrain", view, radiusControl, mapDragControl, 100, "operation.height");
    }

//    @Override
//    protected void altPressed() {
//        dimensionSnapshot = getDimension().getSnapshot();
//    }
//
//    @Override
//    protected void altReleased() {
//        dimensionSnapshot = null;
//    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        float adjustment = (float) Math.pow(dynamicLevel * getLevel() * 2, 2.0);
        Dimension dimension = getDimension();
        final int minHeight, maxHeight;
        if (getFilter() instanceof FilterImpl) {
            FilterImpl filter = (FilterImpl) getFilter();
            if (filter.getAboveLevel() != -1) {
                minHeight = filter.getAboveLevel();
            } else {
                minHeight = Integer.MIN_VALUE;
            }
            if (filter.getBelowLevel() != -1) {
                maxHeight = filter.getBelowLevel();
            } else {
                maxHeight = Integer.MAX_VALUE;
            }
        } else {
            minHeight = Integer.MIN_VALUE;
            maxHeight = Integer.MAX_VALUE;
        }
        dimension.setEventsInhibited(true);
        try {
            int radius = getEffectiveRadius();
            float maxZ = dimension.getMaxHeight() - 1;
            for (int x = centreX - radius; x <= centreX + radius; x++) {
                for (int y = centreY - radius; y <= centreY + radius; y++) {
                    float currentHeight = dimension.getHeightAt(x, y);
                    float targetHeight = inverse ? Math.max(currentHeight - adjustment, minHeight) : Math.min(currentHeight + adjustment, maxHeight);
                    if (targetHeight < 0.0f) {
                        targetHeight = 0.0f;
                    } else if (targetHeight > maxZ) {
                        targetHeight = maxZ;
                    }
                    float strength = getFullStrength(centreX, centreY, x, y);
                    float newHeight = strength * targetHeight + (1 - strength) * currentHeight;
                    if (inverse ? (newHeight < currentHeight) : (newHeight > currentHeight)) {
                        dimension.setHeightAt(x, y, newHeight);
                    }
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

//    private Dimension dimensionSnapshot;
}