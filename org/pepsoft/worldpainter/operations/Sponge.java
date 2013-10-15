/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.operations;

import org.pepsoft.minecraft.Constants;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.HeightMapTileFactory;
import org.pepsoft.worldpainter.MapDragControl;
import org.pepsoft.worldpainter.RadiusControl;
import org.pepsoft.worldpainter.TileFactory;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.layers.FloodWithLava;

/**
 *
 * @author pepijn
 */
public class Sponge extends RadiusOperation {
    public Sponge(WorldPainterView view, RadiusControl radiusControl, MapDragControl mapDragControl) {
        super("Sponge", "Dry up or reset water and lava", view, radiusControl, mapDragControl, 100, true, "operation.sponge");
    }

    @Override
    protected void tick(int x, int y, boolean inverse, boolean first, float level) {
        final int waterHeight;
        final Dimension dimension = getDimension();
        final TileFactory tileFactory = dimension.getTileFactory();
        if (tileFactory instanceof HeightMapTileFactory) {
            waterHeight = ((HeightMapTileFactory) tileFactory).getWaterHeight();
        } else {
            if (dimension.getWorld().getVersion() == Constants.SUPPORTED_VERSION_2) {
                waterHeight = 62;
            } else {
                waterHeight = tileFactory.getMaxHeight() / 2 - 2;
            }
        }
        dimension.setEventsInhibited(true);
        try {
            final int radius = getEffectiveRadius();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (getStrength(x, y, x + dx, y + dy) != 0f) {
                        if (inverse) {
                            dimension.setWaterLevelAt(x + dx, y + dy, waterHeight);
                            dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, x + dx, y + dy, false);
                        } else {
                            dimension.setWaterLevelAt(x + dx, y + dy, 0);
                        }
                    }
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }
}