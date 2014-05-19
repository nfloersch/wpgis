/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.operations;

import java.awt.Point;
import javax.swing.JOptionPane;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Generator;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.WorldPainter;

/**
 *
 * @author pepijn
 */
public class SetSpawnPoint extends MouseOrTabletOperation {
    public SetSpawnPoint(WorldPainter view) {
        super("Spawn", "Change the spawn point", view, "operation.setSpawnPoint");
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        if (first) {
            Dimension dimension = getDimension();
            if (dimension.getDim() != 0) {
                throw new IllegalArgumentException("Cannot set spawn point on dimensions other than 0");
            }
            World2 world = dimension.getWorld();
            int spawnHeight = dimension.getIntHeightAt(centreX, centreY);
            if (spawnHeight == -1) {
                // No tile
                return;
            }
//            if (world.getGenerator() != Generator.FLAT) {
//                int minSpawnLevel = (world.getVersion() == 0)
//                    ? ((dimension.getMaxHeight() == 256) ? 63 : (dimension.getMaxHeight() / 2 - 1))
//                    : ((world.getVersion() == Constants.SUPPORTED_VERSION_1) ? (dimension.getMaxHeight() / 2 - 1) : 63);
//                if (spawnHeight < minSpawnLevel) {
//
//                    if (JOptionPane.showOptionDialog(getView(), "That spawn point is below level " + minSpawnLevel + " (namely at " + spawnHeight + ").\nThis means that you will respawn in a nearby location which is at least at level " + minSpawnLevel + ".\nDo you want to set the spawn point here?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null) != JOptionPane.YES_OPTION) {
//                        return;
//                    }
//                }
//            }
            world.setSpawnPoint(new Point(centreX, centreY));
        }
    }
}