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
public class RaisePyramid extends MouseOrTabletOperation {
    public RaisePyramid(WorldPainter worldPainter) {
        super("Raise Pyramid", "Raises a square pyramid out of the ground", worldPainter, 100, "operation.raisePyramid");
    }

    @Override
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
        for (int i = -r; i <= r; i++) {
            float actualHeight = dimension.getHeightAt(x + i, y - r);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x + i, y - r, desiredHeight);
                dimension.setTerrainAt(x + i, y - r, Terrain.SANDSTONE);
            }
            actualHeight = dimension.getHeightAt(x + i, y + r);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x + i, y + r, desiredHeight);
                dimension.setTerrainAt(x + i, y + r, Terrain.SANDSTONE);
            }
        }
        for (int i = -r + 1; i < r; i++) {
            float actualHeight = dimension.getHeightAt(x - r, y + i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x - r, y + i, desiredHeight);
                dimension.setTerrainAt(x - r, y + i, Terrain.SANDSTONE);
            }
            actualHeight = dimension.getHeightAt(x + r, y + i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x + r, y + i, desiredHeight);
                dimension.setTerrainAt(x + r, y + i, Terrain.SANDSTONE);
            }
        }
        return raised;
    }
}