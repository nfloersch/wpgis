/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GroundCoverDialog.java
 *
 * Created on Apr 22, 2012, 9:01:10 PM
 */
package org.pepsoft.worldpainter.layers.groundcover;

import javax.swing.JColorChooser;
import java.awt.Color;
import org.pepsoft.minecraft.Material;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.pepsoft.worldpainter.ColourScheme;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.layers.CustomLayerDialog;

/**
 *
 * @author pepijn
 */
public class GroundCoverDialog extends CustomLayerDialog<GroundCoverLayer> {
    /** Creates new form GroundCoverDialog */
    public GroundCoverDialog(java.awt.Frame parent, Material material, ColourScheme colourScheme, boolean extendedBlockIds) {
        this(parent, material, null, colourScheme, extendedBlockIds);
    }
    
    /** Creates new form GroundCoverDialog */
    public GroundCoverDialog(java.awt.Frame parent, GroundCoverLayer existingLayer, ColourScheme colourScheme, boolean extendedBlockIds) {
        this(parent, null, existingLayer, colourScheme, extendedBlockIds);
    }
    
    /** Creates new form GroundCoverDialog */
    public GroundCoverDialog(java.awt.Frame parent, Material material, GroundCoverLayer existingLayer, ColourScheme colourScheme, boolean extendedBlockIds) {
        super(parent, true);
        this.colourScheme = colourScheme;
        BLOCK_TYPES = new String[extendedBlockIds ? 4096 : 256];
        for (int i = 0; i < BLOCK_TYPES.length; i++) {
            if ((i >= BLOCK_TYPE_NAMES.length) || (BLOCK_TYPE_NAMES[i] == null)) {
                BLOCK_TYPES[i] = Integer.toString(i);
            } else {
                BLOCK_TYPES[i] = i + " " + BLOCK_TYPE_NAMES[i];
            }
        }

        initComponents();
        
        if (existingLayer != null) {
            layer = existingLayer;
            comboBoxBlockId.setSelectedIndex(existingLayer.getMaterial().getBlockType());
            spinnerDataValue.setValue(existingLayer.getMaterial().getData());
            fieldName.setText(existingLayer.getName());
            spinnerThickness.setValue(existingLayer.getThickness());
            selectedColour = existingLayer.getColour();
//            if (existingLayer.isTaperedEdge()) {
//                spinnerEdgeTapering.setValue(existingLayer.getEdgeWidth());
//            } else {
//                spinnerEdgeTapering.setValue(0);
//            }
        } else {
            comboBoxBlockId.setSelectedIndex(material.getBlockType());
            spinnerDataValue.setValue(material.getData());
        }
        
        setLabelColour();
        
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("cancel", new AbstractAction("cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }

            private static final long serialVersionUID = 1L;
        });

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        rootPane.setDefaultButton(buttonOK);
        
        setLocationRelativeTo(parent);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public GroundCoverLayer getSelectedLayer() {
        return layer;
    }
    
    private void ok() {
        Material material = Material.get(comboBoxBlockId.getSelectedIndex(), (Integer) spinnerDataValue.getValue());
        if (layer == null) {
            layer = new GroundCoverLayer(fieldName.getText(), material, selectedColour);
        } else {
            layer.setName(fieldName.getText());
            layer.setMaterial(material);
            layer.setColour(selectedColour);
        }
        layer.setThickness((Integer) spinnerThickness.getValue());
//        int edgeWidth = (Integer) spinnerEdgeTapering.getValue();
//        if (edgeWidth > 0) {
//            layer.setEdgeWidth(edgeWidth);
//            layer.setTaperedEdge(true);
//        } else {
//            layer.setTaperedEdge(false);
//        }
        cancelled = false;
        dispose();
    }
    
    private void cancel() {
        dispose();
    }
    
    private void pickColour() {
        Color pick = JColorChooser.showDialog(this, "Select Colour", new Color(selectedColour));
        if (pick != null) {
            selectedColour = pick.getRGB();
            setLabelColour();
        }
    }
    
    private void setLabelColour() {
        jLabel5.setBackground(new Color(selectedColour));
    }
    
    private void updateNameAndColour() {
        Material material = Material.get(comboBoxBlockId.getSelectedIndex(), (Integer) spinnerDataValue.getValue());
        fieldName.setText(material.toString());
        int colour = colourScheme.getColour(material);
        if (colour != 0) {
            selectedColour = colour;
            setLabelColour();
        }
    }
    
    private void setControlStates() {
//        spinnerEdgeTapering.setEnabled((Integer) spinnerThickness.getValue() > 1);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        comboBoxBlockId = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        spinnerDataValue = new javax.swing.JSpinner();
        buttonOK = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        fieldName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        spinnerThickness = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Define Custom Ground Cover Layer");

        jLabel1.setText("Select the block ID and data value for your custom material:");

        jLabel2.setText("Block ID:");

        comboBoxBlockId.setModel(new DefaultComboBoxModel(BLOCK_TYPES));
        comboBoxBlockId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxBlockIdActionPerformed(evt);
            }
        });

        jLabel3.setText("Data value:");

        spinnerDataValue.setModel(new javax.swing.SpinnerNumberModel(0, 0, 15, 1));

        buttonOK.setText("OK");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        jLabel4.setText("Colour:");

        jLabel5.setBackground(java.awt.Color.orange);
        jLabel5.setText("                 ");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel5.setOpaque(true);

        jButton1.setText("...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setText("Name:");

        fieldName.setColumns(10);

        jLabel7.setText("Thickness:");

        spinnerThickness.setModel(new javax.swing.SpinnerNumberModel(1, 1, 255, 1));
        spinnerThickness.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerThicknessStateChanged(evt);
            }
        });

        jLabel8.setForeground(new java.awt.Color(0, 0, 255));
        jLabel8.setText("<html><u>Look up block ID's and data values</u></html>");
        jLabel8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxBlockId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerDataValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerThickness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 114, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(comboBoxBlockId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(spinnerDataValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinnerThickness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        pickColour();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void comboBoxBlockIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxBlockIdActionPerformed
        updateNameAndColour();
    }//GEN-LAST:event_comboBoxBlockIdActionPerformed

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        try {
            DesktopUtils.open(new URL("http://www.minecraftwiki.net/wiki/Data_values"));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL exception while trying to open http://www.minecraftwiki.net/wiki/Data_values", e);
        }
    }//GEN-LAST:event_jLabel8MouseClicked

    private void spinnerThicknessStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerThicknessStateChanged
        setControlStates();
    }//GEN-LAST:event_spinnerThicknessStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOK;
    private javax.swing.JComboBox comboBoxBlockId;
    private javax.swing.JTextField fieldName;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSpinner spinnerDataValue;
    private javax.swing.JSpinner spinnerThickness;
    // End of variables declaration//GEN-END:variables
    
    private final ColourScheme colourScheme;
    private GroundCoverLayer layer;
    private boolean cancelled = true;
    private int selectedColour = Color.ORANGE.getRGB();
    private final String[] BLOCK_TYPES;

    private static final long serialVersionUID = 1L;
}