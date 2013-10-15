/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.pepsoft.worldpainter.App;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import static org.pepsoft.worldpainter.layers.Layer.DataSize.*;
import org.pepsoft.worldpainter.layers.combined.CombinedLayerExporter;

/**
 *
 * @author pepijn
 */
public class CombinedLayer extends CustomLayer implements LayerContainer {
    public CombinedLayer(String name, String description, int colour) {
        super(name, description, NIBBLE, 55, colour);
    }

    public Set<Layer> apply(Dimension dimension) {
        Set<Layer> addedLayers = new HashSet<Layer>();
        for (Tile tile : dimension.getTiles()) {
            addedLayers.addAll(apply(tile));
        }
        return addedLayers;
    }

    public Set<Layer> apply(Tile tile) {
        Set<Layer> addedLayers = new HashSet<Layer>();
        if (!tile.hasLayer(this)) {
            return Collections.emptySet();
        }
        tile.setEventsInhibited(true);
        try {
            for (Layer layer : layers) {
                boolean layerAdded = false;
                final float factor = factors.get(layer);
                DataSize dataSize = layer.getDataSize();
                if ((dataSize == BIT) || (dataSize == BIT_PER_CHUNK)) {
                    for (int x = 0; x < TILE_SIZE; x++) {
                        for (int y = 0; y < TILE_SIZE; y++) {
                            float strength = Math.min(tile.getLayerValue(this, x, y) / 15.0f * factor, 1.0f);
                            if ((strength > 0.95f) || (Math.random() < strength)) {
                                tile.setBitLayerValue(layer, x, y, true);
                                layerAdded = true;
                            }
                        }
                    }
                } else {
                    for (int x = 0; x < TILE_SIZE; x++) {
                        for (int y = 0; y < TILE_SIZE; y++) {
                            int value = (int) (tile.getLayerValue(this, x, y) * factor + 0.5f);
                            if (value > 0) {
                                tile.setLayerValue(layer, x, y, value);
                                layerAdded = true;
                            }
                        }
                    }
                }
                if (layerAdded) {
                    addedLayers.add(layer);
                }
            }
            tile.clearLayerData(this);
        } finally {
            tile.setEventsInhibited(false);
        }
        return addedLayers;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    @Override
    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        if (layers == null) {
            throw new NullPointerException();
        }
        this.layers = layers;
    }

    public Map<Layer, Float> getFactors() {
        return factors;
    }

    public void setFactors(Map<Layer, Float> factors) {
        if (factors == null) {
            throw new NullPointerException();
        }
        this.factors = factors;
    }

    @Override
    public LayerExporter<CombinedLayer> getExporter() {
        return new CombinedLayerExporter(this);
    }

    @Override
    public List<Action> getActions() {
        List<Action> actions = new ArrayList<Action>();
        List<Action> superActions = super.getActions();
        if (superActions != null) {
            actions.addAll(superActions);
        }
        actions.add(new AbstractAction("Apply") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dimension dimension = App.getInstance().getDimension();
                if (dimension.getAllLayers(false).contains(CombinedLayer.this)) {
                    dimension.armSavePoint();
                    apply(dimension);
                }
            }
            
            private static final long serialVersionUID = 1L;
        });
        return actions;
    }
    private static final long serialVersionUID = 1L;
    private Terrain terrain;
    private List<Layer> layers = Collections.emptyList();
    private Map<Layer, Float> factors = Collections.emptyMap();
}