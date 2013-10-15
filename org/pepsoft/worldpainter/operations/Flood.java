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
import org.pepsoft.worldpainter.layers.FloodWithLava;

/**
 *
 * @author pepijn
 */
public class Flood extends MouseOrTabletOperation implements AutoBiomeOperation {
    public Flood(WorldPainter view, boolean floodWithLava) {
        super(floodWithLava ? "Lava" : "Flood", "Flood an area with " + (floodWithLava ? "lava" : "water"), view, "operation.flood." + (floodWithLava ? "lava" : "water"));
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
        boolean fluidPresent = waterLevel > terrainHeight;
        if (undo && (! fluidPresent)) {
            // No point lowering the water level if there is no water...
            return;
        }
        int height = Math.max(terrainHeight, waterLevel);
        int floodToHeight;
        if (fluidPresent && (floodWithLava != dimension.getBitLayerValueAt(FloodWithLava.INSTANCE, x, y))) {
            // There is fluid present of a different type; don't change the
            // height, just change the type
            floodToHeight = height;
            undo = false;
        } else {
            if (undo ? (height <= 0) : (height >= (dimension.getMaxHeight() - 1))) {
                // Already at the lowest or highest possible point
                return;
            }
            floodToHeight = undo ? height : (height + 1);
        }
        synchronized (dimension) {
            dimension.setEventsInhibited(true);
            dimension.setAutoUpdateBiomes(autoBiomesEnabled);
        }
        try {
            synchronized (dimension) {
                dimension.rememberChanges();
            }
            QueueLinearFloodFiller flooder = new QueueLinearFloodFiller(dimension, floodToHeight, floodWithLava, undo);
            Point imageCoords = worldToImageCoordinates(x, y);
            if (! flooder.floodFill(imageCoords.x, imageCoords.y, SwingUtilities.getWindowAncestor(getView()))) {
                // Cancelled by user
                synchronized (dimension) {
                    if (dimension.undoChanges()) {
                        dimension.clearRedo();
                        dimension.armSavePoint();
                    }
                }
            }
        } finally {
            synchronized (dimension) {
                dimension.setAutoUpdateBiomes(false);
                dimension.setEventsInhibited(false);
            }
        }
    }

    private final boolean floodWithLava;
    private boolean autoBiomesEnabled;
}
