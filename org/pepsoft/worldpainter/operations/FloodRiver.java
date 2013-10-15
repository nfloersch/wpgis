/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import java.awt.Point;
import javax.swing.SwingUtilities;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.QueueLinearFloodFiller;
import org.pepsoft.worldpainter.WorldPainter;

/**
 *
 * @author pepijn
 */
public class FloodRiver extends MouseOrTabletOperation implements AutoBiomeOperation {
    public FloodRiver(WorldPainter view, boolean floodWithLava) {
        super(floodWithLava ? "Lava" : "Flood", "Flood a River with " + (floodWithLava ? "lava" : "water"), view, "operation.floodRiver." + (floodWithLava ? "lava" : "water"));
        this.floodWithLava = floodWithLava;
    }
    
    // AutoBiomeOperation

    @Override
    public boolean isAutoBiomesEnabled() {
        return autoBiomesEnabled;
    }

    @Override
    public void setAutoBiomesEnabled(boolean autoBiomesEnabled) {
        this.autoBiomesEnabled = autoBiomesEnabled;
    }

    @Override
    protected void tick(int x, int y, boolean undo, boolean first, float dynamicLevel) {
        Dimension dimension = getDimension();
        int terrainHeight = dimension.getIntHeightAt(x, y);
        if (terrainHeight == -1) {
            // Not on a tile
            return;
        }
        int waterLevel = dimension.getWaterLevelAt(x, y);
        if (undo && (waterLevel <= terrainHeight)) {
            // No point lowering the water level if there is no water...
            return;
        }
        int height = Math.max(terrainHeight, waterLevel);
        if (undo ? (height <= 0) : (height >= (dimension.getMaxHeight() - 1))) {
            // Already at the lowest or highest possible point
            return;
        }
        dimension.setEventsInhibited(true);
        dimension.setAutoUpdateBiomes(autoBiomesEnabled);
        try {
            QueueLinearFloodFiller flooder = new QueueLinearFloodFiller(dimension, undo ? height : (height + 1), floodWithLava, undo);
            Point imageCoords = worldToImageCoordinates(x, y);
            if (! flooder.floodFill(imageCoords.x, imageCoords.y, SwingUtilities.getWindowAncestor(getView()))) {
                // Cancelled by user
                if (dimension.undoIfDirty()) {
                    dimension.clearRedo();
                    dimension.armSavePoint();
                }
            }
        } finally {
            dimension.setAutoUpdateBiomes(false);
            dimension.setEventsInhibited(false);
        }
    }

    private final boolean floodWithLava;
    private boolean autoBiomesEnabled;
}