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
        super("Height", "Raise or lower the terrain", view, radiusControl, mapDragControl, 100, true, "operation.height");
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
    protected void tick(int centerX, int centerY, boolean undo, boolean first, float dynamicLevel) {
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
            int radius = getRadius();
            float maxZ = dimension.getMaxHeight() - 1;
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    float currentHeight = dimension.getHeightAt(x, y);
                    float targetHeight = undo ? Math.max(currentHeight - adjustment, minHeight) : Math.min(currentHeight + adjustment, maxHeight);
                    if (targetHeight < 0.0f) {
                        targetHeight = 0.0f;
                    } else if (targetHeight > maxZ) {
                        targetHeight = maxZ;
                    }
                    float strength = getFullStrength(centerX, centerY, x, y);
                    float newHeight = strength * targetHeight + (1 - strength) * currentHeight;
                    if (undo ? (newHeight < currentHeight) : (newHeight > currentHeight)) {
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