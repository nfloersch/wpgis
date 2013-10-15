/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.pockets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.ColourScheme;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.layers.CustomLayerDialog;

/**
 *
 * @author pepijn
 */
public class UndergroundPocketsDialog extends CustomLayerDialog<UndergroundPocketsLayer> {
    /**
     * Creates new form UndergroundPocketsDialog
     */
    public UndergroundPocketsDialog(java.awt.Frame parent, Material material, ColourScheme colourScheme, int maxHeight, boolean extendedBlockIds) {
        this(parent, material, null, colourScheme, maxHeight, extendedBlockIds);
    }
    
    /**
     * Creates new form UndergroundPocketsDialog
     */
    public UndergroundPocketsDialog(java.awt.Frame parent, UndergroundPocketsLayer existingLayer, ColourScheme colourScheme, int maxHeight, boolean extendedBlockIds) {
        this(parent, null, existingLayer, colourScheme, maxHeight, extendedBlockIds);
    }
    
    /**
     * Creates new form UndergroundPocketsDialog
     */
    private UndergroundPocketsDialog(java.awt.Frame parent, Material material, UndergroundPocketsLayer existingLayer, ColourScheme colourScheme, int maxHeight, boolean extendedBlockIds) {
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
            fieldName.setText(existingLayer.getName());
            selectedColour = existingLayer.getColour();
            comboBoxBlockId.setSelectedIndex(existingLayer.getMaterial().getBlockType());
            spinnerDataValue.setValue(existingLayer.getMaterial().getData());
            spinnerMinLevel.setValue(existingLayer.getMinLevel());
            spinnerMaxLevel.setValue(existingLayer.getMaxLevel());
            spinnerOccurrence.setValue(existingLayer.getFrequency());
            spinnerScale.setValue(existingLayer.getScale());
        } else {
            comboBoxBlockId.setSelectedIndex(material.getBlockType());
            spinnerDataValue.setValue(material.getData());
            spinnerMaxLevel.setValue(maxHeight - 1);
        }
        ((SpinnerNumberModel) spinnerMinLevel.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerMaxLevel.getModel()).setMaximum(maxHeight - 1);
        spinnerDataValue.setEditor(new JSpinner.NumberEditor(spinnerDataValue, "0"));
        spinnerOccurrence.setEditor(new JSpinner.NumberEditor(spinnerOccurrence, "0"));
        JSpinner.NumberEditor scaleEditor = new JSpinner.NumberEditor(spinnerScale, "0");
        scaleEditor.getTextField().setColumns(3);
        spinnerScale.setEditor(scaleEditor);
        spinnerMinLevel.setEditor(new JSpinner.NumberEditor(spinnerMinLevel, "0"));
        spinnerMaxLevel.setEditor(new JSpinner.NumberEditor(spinnerMaxLevel, "0"));
        
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
    public UndergroundPocketsLayer getSelectedLayer() {
        return layer;
    }

    private void ok() {
        String name = fieldName.getText();
        Material material = Material.get(comboBoxBlockId.getSelectedIndex(), (Integer) spinnerDataValue.getValue());
        int occurrence = (Integer) spinnerOccurrence.getValue();
        int scale = (Integer) spinnerScale.getValue();
        int minLevel = (Integer) spinnerMinLevel.getValue();
        int maxLevel = (Integer) spinnerMaxLevel.getValue();
        if (layer == null) {
            layer = new UndergroundPocketsLayer(name, material, occurrence, minLevel, maxLevel, scale, selectedColour);
        } else {
            layer.setName(name);
            layer.setColour(selectedColour);
            layer.setMaterial(material);
            layer.setFrequency(occurrence);
            layer.setMinLevel(minLevel);
            layer.setMaxLevel(maxLevel);
            layer.setScale(scale);
        }
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
        if (fieldName.isEnabled()) {
            Material material = Material.get(comboBoxBlockId.getSelectedIndex(), (Integer) spinnerDataValue.getValue());
            fieldName.setText(material.toString());
            int colour = colourScheme.getColour(material);
            if (colour != 0) {
                selectedColour = colour;
                setLabelColour();
            }
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        spinnerDataValue = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        comboBoxBlockId = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        fieldName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        buttonPickColour = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        spinnerOccurrence = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        spinnerScale = new javax.swing.JSpinner();
        spinnerMaxLevel = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        spinnerMinLevel = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Configure Underground Pockets Layer");

        jLabel1.setText("Select the block ID and data value for your custom material:");

        jLabel4.setText("Colour:");

        spinnerDataValue.setModel(new javax.swing.SpinnerNumberModel(0, 0, 15, 1));

        jLabel3.setText("Data value:");

        comboBoxBlockId.setModel(new DefaultComboBoxModel(BLOCK_TYPES));
        comboBoxBlockId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxBlockIdActionPerformed(evt);
            }
        });

        jLabel2.setText("Block ID:");

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("OK");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        jLabel6.setText("Name:");

        fieldName.setColumns(10);

        jLabel5.setBackground(java.awt.Color.orange);
        jLabel5.setText("                 ");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel5.setOpaque(true);

        buttonPickColour.setText("...");
        buttonPickColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPickColourActionPerformed(evt);
            }
        });

        jLabel7.setText("Occurrence:");

        spinnerOccurrence.setModel(new javax.swing.SpinnerNumberModel(10, 1, 1000, 1));

        jLabel8.setText("Scale:");

        jLabel9.setText("Levels:");

        jLabel10.setText("‰");

        spinnerScale.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(100), Integer.valueOf(1), null, Integer.valueOf(1)));

        spinnerMaxLevel.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerMaxLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerMaxLevelStateChanged(evt);
            }
        });

        jLabel11.setText("%");

        spinnerMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerMinLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerMinLevelStateChanged(evt);
            }
        });

        jLabel12.setText("-");

        jLabel13.setForeground(new java.awt.Color(0, 0, 255));
        jLabel13.setText("<html><u>Look up block ID's and data values</u></html>");
        jLabel13.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel13MouseClicked(evt);
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
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spinnerMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel12)
                                .addGap(0, 0, 0)
                                .addComponent(spinnerMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel11))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(comboBoxBlockId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerDataValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spinnerOccurrence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel10))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonPickColour))))
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(buttonPickColour))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinnerOccurrence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(spinnerMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void comboBoxBlockIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxBlockIdActionPerformed
        updateNameAndColour();
    }//GEN-LAST:event_comboBoxBlockIdActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void buttonPickColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPickColourActionPerformed
        pickColour();
    }//GEN-LAST:event_buttonPickColourActionPerformed

    private void spinnerMinLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerMinLevelStateChanged
        int newMinValue = (Integer) spinnerMinLevel.getValue();
        int currentMaxValue = (Integer) spinnerMaxLevel.getValue();
        if (newMinValue > currentMaxValue) {
            spinnerMaxLevel.setValue(newMinValue);
        }
    }//GEN-LAST:event_spinnerMinLevelStateChanged

    private void spinnerMaxLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerMaxLevelStateChanged
        int newMaxValue = (Integer) spinnerMaxLevel.getValue();
        int currentMinValue = (Integer) spinnerMinLevel.getValue();
        if (newMaxValue < currentMinValue) {
            spinnerMinLevel.setValue(newMaxValue);
        }
    }//GEN-LAST:event_spinnerMaxLevelStateChanged

    private void jLabel13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MouseClicked
        try {
            DesktopUtils.open(new URL("http://www.minecraftwiki.net/wiki/Data_values"));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL exception while trying to open http://www.minecraftwiki.net/wiki/Data_values", e);
        }
    }//GEN-LAST:event_jLabel13MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonPickColour;
    private javax.swing.JComboBox comboBoxBlockId;
    private javax.swing.JTextField fieldName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner spinnerDataValue;
    private javax.swing.JSpinner spinnerMaxLevel;
    private javax.swing.JSpinner spinnerMinLevel;
    private javax.swing.JSpinner spinnerOccurrence;
    private javax.swing.JSpinner spinnerScale;
    // End of variables declaration//GEN-END:variables
    
    private final ColourScheme colourScheme;
    private UndergroundPocketsLayer layer;
    private boolean cancelled = true;
    private int selectedColour = Color.ORANGE.getRGB();
    private final String[] BLOCK_TYPES;

    private static final long serialVersionUID = 1L;
}