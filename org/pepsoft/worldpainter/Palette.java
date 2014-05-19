/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;

/**
 *
 * @author SchmitzP
 */
public class Palette {
    Palette(String name, JPanel buttonPanel) {
        super();
        this.name = name;
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
//        group.setBorder(new TitledBorder(name));
//        addPropertyChangeListener("orientation", new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                switch ((Integer) evt.getNewValue()) {
//                    case HORIZONTAL:
//                        group.setLayout(new BoxLayout(group, BoxLayout.LINE_AXIS));
//                        break;
//                    case VERTICAL:
//                        group.setLayout(new BoxLayout(group, BoxLayout.PAGE_AXIS));
//                        break;
//                }
//            }
//        });
//        getContentPane().add(panel, BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(1, 1, 1, 1);
        panel.add(buttonPanel, constraints);

        dockableFrame = App.createDockableFrame(panel, name, DockContext.DOCK_SIDE_WEST, 3);
        dockableFrame.setKey("customLayerPalette." + name);
    }

    String getName() {
        return name;
    }

    List<CustomLayer> getLayers() {
        return Collections.unmodifiableList(layers);
    }
    
    @SuppressWarnings("element-type-mismatch")
    boolean contains(Layer layer) {
        return layers.contains(layer);
    }
    
    void add(CustomLayer layer, JPanel buttonPanel) {
        layers.add(layer);
        layerButtonPanels.put(layer, buttonPanel);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(1, 1, 1, 1);
        panel.add(buttonPanel, constraints, panel.getComponentCount() - 1);
    }
    
    JPanel remove(CustomLayer layer) {
        if (layerButtonPanels.containsKey(layer)) {
            layers.remove(layer);
            JPanel buttonPanel = layerButtonPanels.remove(layer);
            panel.remove(buttonPanel);
            return buttonPanel;
        } else {
            return null;
        }
    }
    
    void activate(CustomLayer layer) {
        ((JToggleButton) layerButtonPanels.get(layer).getComponent(2)).setSelected(true);
    }

    void deactivate(CustomLayer layer) {
        ((JToggleButton) layerButtonPanels.get(layer).getComponent(2)).setSelected(false);
    }
    
    boolean isEmpty() {
        return layers.isEmpty();
    }
    
    DockableFrame getDockableFrame() {
        return dockableFrame;
    }
    
    private String name;
    private final JPanel panel;
    private final List<CustomLayer> layers = new ArrayList<CustomLayer>();
    private final Map<CustomLayer, JPanel> layerButtonPanels = new HashMap<CustomLayer, JPanel>();
    private final DockableFrame dockableFrame;
}