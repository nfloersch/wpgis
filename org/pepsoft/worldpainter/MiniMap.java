/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.pepsoft.util.swing.TiledImageViewer;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.layers.Layer;

/**
 *
 * @author pepijn
 */
public class MiniMap extends TiledImageViewer implements PropertyChangeListener, Dimension.Listener, Tile.Listener {
    public WorldPainter getView() {
        return view;
    }

    public void setView(WorldPainter view) {
        if (this.view != null) {
            this.view.removePropertyChangeListener(this);
        }
        this.view = view;
        if (view != null) {
            view.addPropertyChangeListener(this);
        }
        setDimension((view != null) ? view.getDimension() : null);
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        if (this.dimension != null) {
            unregister(this.dimension);
        }
        this.dimension = dimension;
        if (dimension != null) {
            register(dimension);
        }
        refreshTileProvider();
    }

    public CustomBiomeManager getCustomBiomeManager() {
        return customBiomeManager;
    }

    public void setCustomBiomeManager(CustomBiomeManager customBiomeManager) {
        this.customBiomeManager = customBiomeManager;
        refreshTileProvider();
    }

    // PropertyChangeListener

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (PROPERTIES_OF_INTEREST.contains(name)) {
            if (name.equals("dimension")) {
                setDimension((Dimension) evt.getNewValue());
            }
            refreshTileProvider();
        }
    }
    
    // Dimension.Listener

    @Override
    public void tileAdded(Dimension dimension, Tile tile) {
        tile.addListener(this);
        repaint(tile);
    }

    @Override
    public void tileRemoved(Dimension dimension, Tile tile) {
        tile.removeListener(this);
        repaint(tile);
    }

    // Tile.Listener
    
    @Override
    public void heightMapChanged(Tile tile) {
        repaint(tile);
    }

    @Override
    public void terrainChanged(Tile tile) {
        repaint(tile);
    }

    @Override
    public void waterLevelChanged(Tile tile) {
        repaint(tile);
    }

    @Override
    public void layerDataChanged(Tile tile, Set<Layer> changedLayers) {
        repaint(tile);
    }

    @Override
    public void allBitLayerDataChanged(Tile tile) {
        repaint(tile);
    }

    @Override
    public void allNonBitlayerDataChanged(Tile tile) {
        repaint(tile);
    }

    @Override
    public void seedsChanged(Tile tile) {
        repaint(tile);
    }
    
    private void register(Dimension dimension) {
        dimension.addDimensionListener(this);
        for (Tile tile: dimension.getTiles()) {
            tile.addListener(this);
        }
    }
    
    private void unregister(Dimension dimension) {
        dimension.removeDimensionListener(this);
        for (Tile tile: dimension.getTiles()) {
            tile.removeListener(this);
        }
    }
    
    private void repaint(Tile tile) {
        repaint(); // TODO: only repaint tile area
    }
    
    private void refreshTileProvider() {
        if ((view != null) && (dimension != null)) {
            WPTileProvider tileProvider = new WPTileProvider(dimension, view.getColourScheme(), view.getBiomeScheme(), customBiomeManager, view.getHiddenLayers(), false, view.getLightOrigin(), true);
            tileProvider.setZoom(16);
            setTileProvider(tileProvider);
        } else {
            setTileProvider(null);
        }
    }
    
    private WorldPainter view;
    private Dimension dimension;
    private CustomBiomeManager customBiomeManager;
    
    private static final Set<String> PROPERTIES_OF_INTEREST = new HashSet<String>(Arrays.asList("dimension", "colourScheme", "biomeScheme", "hiddenLayers", "lightOrigin"));
    
    private static final long serialVersionUID = 1L;
}