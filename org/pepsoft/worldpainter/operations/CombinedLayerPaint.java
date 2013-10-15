/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.BiomeScheme;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MapDragControl;
import org.pepsoft.worldpainter.RadiusControl;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.biomeschemes.AutoBiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.CachingBiomeScheme;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.CombinedLayer;

/**
 *
 * @author pepijn
 */
public class CombinedLayerPaint extends LayerPaint implements BiomeOperation {
    public CombinedLayerPaint(WorldPainterView view, RadiusControl radiusControl, MapDragControl mapDragControl, CombinedLayer layer) {
        super(view, radiusControl, mapDragControl, layer);
    }

    @Override
    public void setBiomeScheme(BiomeScheme biomeScheme) {
        // If biomeScheme is null, assume that custom biomes must be enabled
        // (otherwise the Biomes layer button would not be active at all), in
        // which case use the biomes from the automatic biome scheme
        if (biomeScheme != null) {
            // The seed should always already be set correctly, but in practice
            // this does not always happen. TODO: find out why
            if (getDimension() != null) {
                biomeScheme.setSeed(getDimension().getMinecraftSeed());
            }
            this.biomeScheme = new CachingBiomeScheme(biomeScheme);
            inverseBiomeEnabled = true;
        } else {
            this.biomeScheme = new CachingBiomeScheme(new AutoBiomeScheme(null));
            inverseBiomeEnabled = false;
        }
    }
    
    @Override
    protected void tick(int centerX, int centerY, boolean undo, boolean first, float dynamicLevel) {
        final Dimension dimension = getDimension();
        final CombinedLayer layer = (CombinedLayer) getLayer();
        final Terrain terrain = layer.getTerrain();
        final int biome = layer.getBiome();
        final boolean previousAutoUpdateBiomes = dimension.isAutoUpdateBiomes();
        dimension.setEventsInhibited(true);
        if (biome != -1) {
            dimension.setAutoUpdateBiomes(false);
        }
        try {
            int radius = getEffectiveRadius();
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    int currentValue = dimension.getLayerValueAt(layer, x, y);
                    float strength = dynamicLevel * (undo ? getFullStrength(centerX, centerY, x, y) : getStrength(centerX, centerY, x, y));
                    if (strength != 0f) {
                        int targetValue = undo ? (15 - (int) (strength * 14 + 1)) : (int) (strength * 14 + 1);
                        if (undo ? (targetValue < currentValue) : (targetValue > currentValue)) {
                            dimension.setLayerValueAt(layer, x, y, targetValue);
                        }
                        if ((terrain != null) && ((strength > 0.95f) || (Math.random() < strength))) {
                            if (undo) {
                                dimension.applyTheme(x, y);
                            } else {
                                dimension.setTerrainAt(x, y, terrain);
                            }
                        }
                        if ((biome != -1) && ((strength > 0.95f) || (Math.random() < strength))) {
                            if (undo) {
                                if (inverseBiomeEnabled) {
                                    dimension.setLayerValueAt(Biome.INSTANCE, x, y, biomeScheme.getBiome(x, y));
                                }
                            } else {
                                dimension.setLayerValueAt(Biome.INSTANCE, x, y, biome);
                            }
                        }
                    }
                }
            }
        } finally {
            if (biome != -1) {
                dimension.setAutoUpdateBiomes(previousAutoUpdateBiomes);
            }
            dimension.setEventsInhibited(false);
        }
    }
    
    private CachingBiomeScheme biomeScheme;
    private boolean inverseBiomeEnabled;
}