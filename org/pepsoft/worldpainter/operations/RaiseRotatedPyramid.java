/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.WorldPainter;

/**
 *
 * @author pepijn
 */
public class RaiseRotatedPyramid extends MouseOrTabletOperation {
    public RaiseRotatedPyramid(WorldPainter worldPainter) {
        super("Raise Rotated Pyramid", "Raises a square, but rotated 45 degrees, pyramid out of the ground", worldPainter, 100, "operation.raiseRotatedPyramid");
    }

    protected void tick(int x, int y, boolean undo, boolean first, float dynamicLevel) {
        Dimension dimension = getDimension();
        float height = dimension.getHeightAt(x, y);
        dimension.setEventsInhibited(true);
        try {
            if (height < (dimension.getMaxHeight() - 1.5f)) {
                dimension.setHeightAt(x, y, height + 1);
            }
            dimension.setTerrainAt(x, y, Terrain.SANDSTONE);
            int maxR = dimension.getMaxHeight();
            for (int r = 1; r < maxR; r++) {
                if (! raiseRing(dimension, x, y, r, height--)) {
                    break;
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    private boolean raiseRing(Dimension dimension, int x, int y, int r, float desiredHeight) {
        boolean raised = false;
        for (int i = 0; i < r; i++) {
            float actualHeight = dimension.getHeightAt(x - r + i, y - i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x - r + i, y - i, desiredHeight);
                dimension.setTerrainAt(x - r + i, y - i, Terrain.SANDSTONE);
            }
            actualHeight = dimension.getHeightAt(x + i, y - r + i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x + i, y - r + i, desiredHeight);
                dimension.setTerrainAt(x + i, y - r + i, Terrain.SANDSTONE);
            }
            actualHeight = dimension.getHeightAt(x + r - i, y + i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x + r - i, y + i, desiredHeight);
                dimension.setTerrainAt(x + r - i, y + i, Terrain.SANDSTONE);
            }
            actualHeight = dimension.getHeightAt(x - i, y + r - i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x - i, y + r - i, desiredHeight);
                dimension.setTerrainAt(x - i, y + r - i, Terrain.SANDSTONE);
            }
        }
        return raised;
    }
}