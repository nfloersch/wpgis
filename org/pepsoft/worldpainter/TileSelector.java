/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TileSelector.java
 *
 * Created on Apr 7, 2012, 5:58:41 PM
 */
package org.pepsoft.worldpainter;

import java.awt.event.KeyEvent;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import java.util.HashSet;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.tools.WPTileSelectionViewer;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.TileRenderer.LightOrigin;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;

/**
 *
 * @author pepijn
 */
public class TileSelector extends javax.swing.JPanel {
    /** Creates new form TileSelector */
    public TileSelector() {
        initComponents();
        viewer.setZoom(zoom);
        viewer.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getWheelRotation();
                if (rotation < 0) {
                    for (int i = 0; i > rotation; i--) {
                        if (zoom > 1) {
                            zoom /= 2;
                        }
                    }
                } else {
                    for (int i = 0; i < rotation; i++) {
                        if (zoom < 16) {
                            zoom *= 2;
                        }
                    }
                }
                viewer.setZoom(zoom);
            }
        });
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                Point tileLocation = getTileLocation(e.getX(), e.getY());
                if (viewer.isSelectedTile(tileLocation)) {
                    viewer.removeSelectedTile(tileLocation);
                } else {
                    viewer.addSelectedTile(tileLocation);
                }
                viewer.setSelectedRectangleCorner1(null);
                viewer.setSelectedRectangleCorner2(null);
                setControlStates();
                notifyListeners();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                selecting = true;
                selectionCorner1 = getTileLocation(e.getX(), e.getY());
                selectionCorner2 = null;
                viewer.setSelectedRectangleCorner1(null);
                viewer.setSelectedRectangleCorner2(null);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                if ((selectionCorner1 != null) && (selectionCorner2 != null)) {
                    int tileX1 = Math.min(selectionCorner1.x, selectionCorner2.x);
                    int tileX2 = Math.max(selectionCorner1.x, selectionCorner2.x);
                    int tileY1 = Math.min(selectionCorner1.y, selectionCorner2.y);
                    int tileY2 = Math.max(selectionCorner1.y, selectionCorner2.y);
                    for (int x = tileX1; x <= tileX2; x++) {
                        for (int y = tileY1; y <= tileY2; y++) {
                            Point tileLocation = new Point(x, y);
                            if (viewer.isSelectedTile(tileLocation)) {
                                viewer.removeSelectedTile(tileLocation);
                            } else {
                                viewer.addSelectedTile(tileLocation);
                            }
                        }
                    }
                    setControlStates();
                    notifyListeners();
                }
                viewer.setSelectedRectangleCorner1(null);
                viewer.setSelectedRectangleCorner2(null);
                selecting = false;
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                viewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                viewer.setHighlightedTileLocation(null);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                viewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                viewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
                if (selecting) {
                    selectionCorner2 = getTileLocation(e.getX(), e.getY());
                    viewer.setSelectedRectangleCorner1(selectionCorner1);
                    viewer.setSelectedRectangleCorner2(selectionCorner2);
                }
            }
            
            private Point getTileLocation(int x, int y) {
                int viewX = viewer.getViewX();
                int viewY = viewer.getViewY();
                int zoom = viewer.getZoom();
                return new Point((x + viewX) * zoom / TILE_SIZE - ((x < -viewX) ? 1 : 0), (y + viewY) * zoom / TILE_SIZE - ((y < -viewY) ? 1 : 0));
            }
            
            private boolean selecting;
            private Point selectionCorner1, selectionCorner2;
        };
        viewer.addMouseListener(mouseAdapter);
        viewer.addMouseMotionListener(mouseAdapter);
        jPanel1.add(viewer, BorderLayout.CENTER);
        
        setControlStates();
        
        ActionMap actionMap = getActionMap();
        actionMap.put("zoomIn", new AbstractAction("zoomIn") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (zoom > 1) {
                    zoom /= 2;
                }
                viewer.setZoom(zoom);
            }
            
            private static final long serialVersionUID = 1L;
        });
        actionMap.put("zoomOut", new AbstractAction("zoomOut") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (zoom < 16) {
                    zoom *= 2;
                }
                viewer.setZoom(zoom);
            }
            
            private static final long serialVersionUID = 1L;
        });
        
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "zoomOut");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoomOut");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "zoomIn");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.SHIFT_DOWN_MASK), "zoomIn");
    }

    public BiomeScheme getBiomeScheme() {
        return biomeScheme;
    }

    public void setBiomeScheme(BiomeScheme biomeScheme) {
        this.biomeScheme = biomeScheme;
    }

    public ColourScheme getColourScheme() {
        return colourScheme;
    }

    public void setColourScheme(ColourScheme colourScheme) {
        this.colourScheme = colourScheme;
    }

    public boolean isContourLines() {
        return contourLines;
    }

    public void setContourLines(boolean contourLines) {
        this.contourLines = contourLines;
    }

    public LightOrigin getLightOrigin() {
        return lightOrigin;
    }

    public void setLightOrigin(LightOrigin lightOrigin) {
        this.lightOrigin = lightOrigin;
    }

    public Point getCurrentLocation() {
        return viewer.getCurrentLocation();
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
        if (dimension != null) {
            WPTileProvider tileProvider = new WPTileProvider(dimension, colourScheme, biomeScheme, customBiomeManager, hiddenLayers, contourLines, lightOrigin);
            tileProvider.setZoom(zoom);
            viewer.setTileProvider(tileProvider);
            viewer.setMarkerCoords((dimension.getDim() == DIM_NORMAL) ? dimension.getWorld().getSpawnPoint() : null);
            buttonSpawn.setEnabled(true);
//            moveToCentre();
        } else {
            viewer.setTileProvider(null);
            viewer.setMarkerCoords(null);
            buttonSpawn.setEnabled(false);
        }
        viewer.clearSelectedTiles();
        setControlStates();
    }
    
    public void refresh() {
        if ((dimension != null) && (dimension.getDim() == DIM_NORMAL)) {
            viewer.setMarkerCoords(dimension.getWorld().getSpawnPoint());
        }
        viewer.refresh();
    }

    public Collection<Layer> getHiddenLayers() {
        return hiddenLayers;
    }

    public void setHiddenLayers(Collection<Layer> hiddenLayers) {
        this.hiddenLayers = hiddenLayers;
    }

    public CustomBiomeManager getCustomBiomeManager() {
        return customBiomeManager;
    }

    public void setCustomBiomeManager(CustomBiomeManager customBiomeManager) {
        this.customBiomeManager = customBiomeManager;
    }
    
    public Set<Point> getSelectedTiles() {
        return viewer.getSelectedTiles();
    }
    
    public void setSelectedTiles(Set<Point> selectedTiles) {
        viewer.setSelectedTiles(selectedTiles);
        notifyListeners();
    }

    public void selectAllTiles() {
        boolean selectionChanged = false;
        for (Tile tile: dimension.getTiles()) {
            Point tileCoords = new Point(tile.getX(), tile.getY());
            if (! viewer.isSelectedTile(tileCoords)) {
                viewer.addSelectedTile(tileCoords);
                selectionChanged = true;
            }
        }
        if (selectionChanged) {
            setControlStates();
            notifyListeners();
        }
    }
    
    public void clearSelection() {
        viewer.clearSelectedTiles();
        setControlStates();
        notifyListeners();
    }
    
    public void invertSelection() {
        for (Tile tile: dimension.getTiles()) {
            Point tileCoords = new Point(tile.getX(), tile.getY());
            if (! viewer.isSelectedTile(tileCoords)) {
                viewer.addSelectedTile(tileCoords);
            } else {
                viewer.removeSelectedTile(tileCoords);
            }
        }
        for (Point tileCoords: new HashSet<Point>(viewer.getSelectedTiles())) {
            if (dimension.getTile(tileCoords) == null) {
                viewer.removeSelectedTile(tileCoords);
            }
        }
        setControlStates();
        notifyListeners();
    }
    
    public void moveToSpawn() {
        viewer.moveToMarker();
    }

    public void moveToCentre() {
        viewer.moveToOrigin();
    }
    
    public void destroy() {
        viewer.setTileProvider(null);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
    
    private void setControlStates() {
        Set<Point> selectedTiles = viewer.getSelectedTiles();
        boolean allowSelectAll, allowInvertSelection, allowClearSelection;
        if (dimension == null) {
            allowSelectAll = allowInvertSelection = false;
        } else if (selectedTiles.isEmpty()) {
            allowSelectAll = allowInvertSelection = true;
        } else {
            int existingTileCount = dimension.getTiles().size(), selectedExistingTileCount = 0;
            for (Point selectedTile: selectedTiles) {
                if (dimension.getTile(selectedTile) != null) {
                    selectedExistingTileCount++;
                }
            }
            allowSelectAll = selectedExistingTileCount < existingTileCount;
            allowInvertSelection = true;
        }
        allowClearSelection = ! selectedTiles.isEmpty();
        buttonSelectAll.setEnabled(allowSelectAll);
        buttonInvertSelection.setEnabled(allowInvertSelection);
        buttonClearSelection.setEnabled(allowClearSelection);
    }
    
    private void notifyListeners() {
        for (Listener listener: listeners) {
            listener.selectionChanged(this, viewer.getSelectedTiles());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        buttonSpawn = new javax.swing.JButton();
        buttonOrigin = new javax.swing.JButton();
        buttonSelectAll = new javax.swing.JButton();
        buttonInvertSelection = new javax.swing.JButton();
        buttonClearSelection = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(192, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.setLayout(new java.awt.BorderLayout());

        buttonSpawn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/spawn_red.png"))); // NOI18N
        buttonSpawn.setEnabled(false);
        buttonSpawn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSpawnActionPerformed(evt);
            }
        });

        buttonOrigin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/arrow_in.png"))); // NOI18N
        buttonOrigin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOriginActionPerformed(evt);
            }
        });

        buttonSelectAll.setText("Select all tiles");
        buttonSelectAll.setEnabled(false);
        buttonSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectAllActionPerformed(evt);
            }
        });

        buttonInvertSelection.setText("Invert selection");
        buttonInvertSelection.setEnabled(false);
        buttonInvertSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonInvertSelectionActionPerformed(evt);
            }
        });

        buttonClearSelection.setText("Clear selection");
        buttonClearSelection.setEnabled(false);
        buttonClearSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClearSelectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(buttonSpawn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonOrigin)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(buttonSelectAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonInvertSelection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClearSelection))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSpawn)
                    .addComponent(buttonOrigin)
                    .addComponent(buttonSelectAll)
                    .addComponent(buttonInvertSelection)
                    .addComponent(buttonClearSelection)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSpawnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSpawnActionPerformed
        moveToSpawn();
    }//GEN-LAST:event_buttonSpawnActionPerformed

    private void buttonOriginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOriginActionPerformed
        moveToCentre();
    }//GEN-LAST:event_buttonOriginActionPerformed

    private void buttonSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectAllActionPerformed
        selectAllTiles();
    }//GEN-LAST:event_buttonSelectAllActionPerformed

    private void buttonInvertSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonInvertSelectionActionPerformed
        invertSelection();
    }//GEN-LAST:event_buttonInvertSelectionActionPerformed

    private void buttonClearSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearSelectionActionPerformed
        clearSelection();
    }//GEN-LAST:event_buttonClearSelectionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonClearSelection;
    private javax.swing.JButton buttonInvertSelection;
    private javax.swing.JButton buttonOrigin;
    private javax.swing.JButton buttonSelectAll;
    private javax.swing.JButton buttonSpawn;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    private final WPTileSelectionViewer viewer = new WPTileSelectionViewer(false);
    private final List<Listener> listeners = new ArrayList<Listener>();
    private Dimension dimension;
    private ColourScheme colourScheme;
    private BiomeScheme biomeScheme;
    private Collection<Layer> hiddenLayers;
    private int zoom = 4;
    private boolean contourLines;
    private TileRenderer.LightOrigin lightOrigin;
    private CustomBiomeManager customBiomeManager;
    
    private static final long serialVersionUID = 1L;
    
    public interface Listener {
        void selectionChanged(TileSelector tileSelector, Set<Point> newSelection);
    }
}