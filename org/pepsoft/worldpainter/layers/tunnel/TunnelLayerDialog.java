/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.tunnel;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.pepsoft.worldpainter.NoiseSettings;
import org.pepsoft.worldpainter.layers.CustomLayerDialog;
import org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.Mode;

/**
 *
 * @author SchmitzP
 */
public class TunnelLayerDialog extends CustomLayerDialog<TunnelLayer> implements ChangeListener {
    /**
     * Creates new form TunnelDialog
     */
    public TunnelLayerDialog(Window parent, TunnelLayer layer, boolean extendedBlockIds, int maxHeight, int baseHeight, int waterLevel) {
        super(parent);
        this.layer = layer;
        this.baseHeight = baseHeight;
        this.waterLevel = waterLevel;
        this.maxHeight = maxHeight;
        
        initComponents();
        mixedMaterialSelectorFloor.setExtendedBlockIds(extendedBlockIds);
        mixedMaterialSelectorRoof.setExtendedBlockIds(extendedBlockIds);
        mixedMaterialSelectorWall.setExtendedBlockIds(extendedBlockIds);
        labelPreview.setPreferredSize(new Dimension(128, 0));
        ((SpinnerNumberModel) spinnerFloorLevel.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerRoofLevel.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerFloorMin.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerFloorMax.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerRoofMin.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerRoofMax.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerFloodLevel.getModel()).setMaximum(maxHeight - 1);
        
        loadSettings();
        
        updatePreview();
        
        getRootPane().setDefaultButton(buttonOK);
        
        noiseSettingsEditorFloor.addChangeListener(this);
        noiseSettingsEditorRoof.addChangeListener(this);
        
        setLocationRelativeTo(parent);
    }

    @Override
    public TunnelLayer getSelectedLayer() {
        return layer;
    }

    // ChangeListener
    
    @Override
    public void stateChanged(ChangeEvent e) {
        generatePreview();
    }

    @Override
    protected void ok() {
        saveSettingsTo(layer);
        super.ok();
    }
    
    private void updatePreview() {
//        if ((radioButtonFloorFixedLevel.isSelected() && radioButtonRoofFixedLevel.isSelected())
//                || (radioButtonFloorInverse.isSelected() && radioButtonRoofInverse.isSelected())) {
//            labelTunnelHeight.setText("(tunnel height: " + Math.max(((Integer) spinnerRoofLevel.getValue() - (Integer) spinnerFloorLevel.getValue()), 0) + ")");
//        } else if (radioButtonFloorFixedDepth.isSelected() && radioButtonRoofFixedDepth.isSelected()) {
//            labelTunnelHeight.setText("(tunnel height: " + Math.max(((Integer) spinnerFloorLevel.getValue() - (Integer) spinnerRoofLevel.getValue()), 0) + ")");
//        } else {
//            labelTunnelHeight.setText("(tunnel height: variable)");
//        }
        generatePreview();
    }

    private void generatePreview() {
        TunnelLayer layer = new TunnelLayer("tmp", 0);
        saveSettingsTo(layer);
        TunnelLayerExporter exporter = new TunnelLayerExporter(layer);
        Insets insets = labelPreview.getInsets();
        int width = labelPreview.getWidth() - insets.left - insets.right;
        int height = labelPreview.getHeight() - insets.top - insets.bottom;
        BufferedImage preview = exporter.generatePreview(width, height, waterLevel, baseHeight, Math.min(maxHeight - baseHeight, height - baseHeight));
        labelPreview.setIcon(new ImageIcon(preview));
    }
    
    private void loadSettings() {
        spinnerFloorLevel.setValue(layer.getFloorLevel());
        spinnerFloorMin.setValue(layer.getFloorMin());
        spinnerFloorMax.setValue(Math.min(layer.getFloorMax(), maxHeight - 1));
        mixedMaterialSelectorFloor.setMixedMaterial(layer.getFloorMaterial());
        switch (layer.getFloorMode()) {
            case CONSTANT_DEPTH:
                radioButtonFloorFixedDepth.setSelected(true);
                break;
            case FIXED_HEIGHT:
                radioButtonFloorFixedLevel.setSelected(true);
                break;
            case INVERTED_DEPTH:
                radioButtonFloorInverse.setSelected(true);
                break;
        }
        NoiseSettings floorNoise = layer.getFloorNoise();
        if (floorNoise == null) {
            floorNoise = new NoiseSettings();
        }
        noiseSettingsEditorFloor.setNoiseSettings(floorNoise);
        spinnerRoofLevel.setValue(layer.getRoofLevel());
        spinnerRoofMin.setValue(layer.getRoofMin());
        spinnerRoofMax.setValue(Math.min(layer.getRoofMax(), maxHeight - 1));
        mixedMaterialSelectorRoof.setMixedMaterial(layer.getRoofMaterial());
        switch (layer.getRoofMode()) {
            case CONSTANT_DEPTH:
                radioButtonRoofFixedDepth.setSelected(true);
                break;
            case FIXED_HEIGHT:
                radioButtonRoofFixedLevel.setSelected(true);
                break;
            case INVERTED_DEPTH:
                radioButtonRoofInverse.setSelected(true);
                break;
        }
        NoiseSettings roofNoise = layer.getRoofNoise();
        if (roofNoise == null) {
            roofNoise = new NoiseSettings();
        }
        noiseSettingsEditorRoof.setNoiseSettings(roofNoise);
        spinnerWallFloorDepth.setValue(layer.getFloorWallDepth());
        spinnerWallRoofDepth.setValue(layer.getRoofWallDepth());
        mixedMaterialSelectorWall.setMixedMaterial(layer.getWallMaterial());
        textFieldName.setText(layer.getName());
        colourEditor1.setColour(layer.getColour());
        checkBoxRemoveWater.setSelected(layer.isRemoveWater());
        checkBoxFlood.setSelected(layer.getFloodLevel() > 0);
        spinnerFloodLevel.setValue((layer.getFloodLevel() > 0) ? layer.getFloodLevel() : waterLevel);
        checkBoxFloodWithLava.setSelected(layer.isFloodWithLava());
        setControlStates();
    }

    private void saveSettingsTo(TunnelLayer layer) {
        layer.setFloorLevel((Integer) spinnerFloorLevel.getValue());
        layer.setFloorMin((Integer) spinnerFloorMin.getValue());
        layer.setFloorMax((Integer) spinnerFloorMax.getValue());
        layer.setFloorMaterial(mixedMaterialSelectorFloor.getMixedMaterial());
        if (radioButtonFloorFixedDepth.isSelected()) {
            layer.setFloorMode(Mode.CONSTANT_DEPTH);
        } else if (radioButtonFloorFixedLevel.isSelected()) {
            layer.setFloorMode(Mode.FIXED_HEIGHT);
        } else {
            layer.setFloorMode(Mode.INVERTED_DEPTH);
        }
        NoiseSettings floorNoiseSettings = noiseSettingsEditorFloor.getNoiseSettings();
        if (floorNoiseSettings.getRange() == 0) {
            layer.setFloorNoise(null);
        } else {
            layer.setFloorNoise(floorNoiseSettings);
        }
        layer.setRoofLevel((Integer) spinnerRoofLevel.getValue());
        layer.setRoofMin((Integer) spinnerRoofMin.getValue());
        layer.setRoofMax((Integer) spinnerRoofMax.getValue());
        layer.setRoofMaterial(mixedMaterialSelectorRoof.getMixedMaterial());
        if (radioButtonRoofFixedDepth.isSelected()) {
            layer.setRoofMode(Mode.CONSTANT_DEPTH);
        } else if (radioButtonRoofFixedLevel.isSelected()) {
            layer.setRoofMode(Mode.FIXED_HEIGHT);
        } else {
            layer.setRoofMode(Mode.INVERTED_DEPTH);
        }
        NoiseSettings roofNoiseSettings = noiseSettingsEditorRoof.getNoiseSettings();
        if (roofNoiseSettings.getRange() == 0) {
            layer.setRoofNoise(null);
        } else {
            layer.setRoofNoise(roofNoiseSettings);
        }
        layer.setFloorWallDepth((Integer) spinnerWallFloorDepth.getValue());
        layer.setRoofWallDepth((Integer) spinnerWallRoofDepth.getValue());
        layer.setWallMaterial(mixedMaterialSelectorWall.getMixedMaterial());
        layer.setName(textFieldName.getText().trim());
        layer.setColour(colourEditor1.getColour());
        layer.setRemoveWater(checkBoxRemoveWater.isSelected());
        layer.setFloodLevel(checkBoxFlood.isSelected() ? (Integer) spinnerFloodLevel.getValue() : 0);
        layer.setFloodWithLava(checkBoxFloodWithLava.isSelected());
    }
    
    private void setControlStates() {
        spinnerFloorMin.setEnabled(! radioButtonFloorFixedLevel.isSelected());
        spinnerFloorMax.setEnabled(! radioButtonFloorFixedLevel.isSelected());
        spinnerRoofMin.setEnabled(! radioButtonRoofFixedLevel.isSelected());
        spinnerRoofMax.setEnabled(! radioButtonRoofFixedLevel.isSelected());
        spinnerFloodLevel.setEnabled(checkBoxFlood.isSelected());
        checkBoxFloodWithLava.setEnabled(checkBoxFlood.isSelected());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TunnelLayer layer = new TunnelLayer("Tunnels", 0x000000);
                TunnelLayerDialog dialog = new TunnelLayerDialog(null, layer, false, 256, 58, 62);
                dialog.setVisible(true);
            }
        });
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        radioButtonFloorFixedLevel = new javax.swing.JRadioButton();
        radioButtonFloorFixedDepth = new javax.swing.JRadioButton();
        radioButtonFloorInverse = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        spinnerFloorLevel = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        noiseSettingsEditorFloor = new org.pepsoft.worldpainter.NoiseSettingsEditor();
        jLabel6 = new javax.swing.JLabel();
        radioButtonRoofFixedLevel = new javax.swing.JRadioButton();
        radioButtonRoofFixedDepth = new javax.swing.JRadioButton();
        radioButtonRoofInverse = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        spinnerRoofLevel = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        noiseSettingsEditorRoof = new org.pepsoft.worldpainter.NoiseSettingsEditor();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        spinnerWallFloorDepth = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        labelPreview = new javax.swing.JLabel();
        mixedMaterialSelectorFloor = new org.pepsoft.worldpainter.MixedMaterialSelector();
        mixedMaterialSelectorRoof = new org.pepsoft.worldpainter.MixedMaterialSelector();
        mixedMaterialSelectorWall = new org.pepsoft.worldpainter.MixedMaterialSelector();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        textFieldName = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        colourEditor1 = new org.pepsoft.worldpainter.ColourEditor();
        jLabel15 = new javax.swing.JLabel();
        spinnerWallRoofDepth = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        spinnerRoofMin = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        spinnerRoofMax = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        spinnerFloorMin = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        spinnerFloorMax = new javax.swing.JSpinner();
        buttonReset = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        checkBoxRemoveWater = new javax.swing.JCheckBox();
        checkBoxFlood = new javax.swing.JCheckBox();
        jLabel21 = new javax.swing.JLabel();
        spinnerFloodLevel = new javax.swing.JSpinner();
        checkBoxFloodWithLava = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Configure Cave/Tunnel Layer");
        setResizable(false);

        jLabel1.setText("Create underground tunnels and caves with the following properties:");

        jLabel2.setText("Floor:");

        buttonGroup1.add(radioButtonFloorFixedLevel);
        radioButtonFloorFixedLevel.setText("fixed level");
        radioButtonFloorFixedLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorFixedLevelActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonFloorFixedDepth);
        radioButtonFloorFixedDepth.setText("fixed depth");
        radioButtonFloorFixedDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorFixedDepthActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonFloorInverse);
        radioButtonFloorInverse.setText("opposite of terrain");
        radioButtonFloorInverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorInverseActionPerformed(evt);
            }
        });

        jLabel3.setText("Level:");

        spinnerFloorLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerFloorLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorLevelStateChanged(evt);
            }
        });

        jLabel7.setText("Material:");

        jLabel5.setText("Smoothness:");

        jLabel6.setText("Ceiling:");

        buttonGroup3.add(radioButtonRoofFixedLevel);
        radioButtonRoofFixedLevel.setSelected(true);
        radioButtonRoofFixedLevel.setText("fixed level");
        radioButtonRoofFixedLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofFixedLevelActionPerformed(evt);
            }
        });

        buttonGroup3.add(radioButtonRoofFixedDepth);
        radioButtonRoofFixedDepth.setText("fixed depth");
        radioButtonRoofFixedDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofFixedDepthActionPerformed(evt);
            }
        });

        buttonGroup3.add(radioButtonRoofInverse);
        radioButtonRoofInverse.setText("opposite of terrain");
        radioButtonRoofInverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofInverseActionPerformed(evt);
            }
        });

        jLabel8.setText("Level:");

        spinnerRoofLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerRoofLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofLevelStateChanged(evt);
            }
        });

        jLabel9.setText("Smoothness:");

        jLabel10.setText("Material:");

        jLabel12.setText("Walls:");

        jLabel13.setText("Bottom width:");

        spinnerWallFloorDepth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerWallFloorDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWallFloorDepthStateChanged(evt);
            }
        });

        jLabel14.setText("Material:");

        labelPreview.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

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

        jLabel4.setText("Name:");

        textFieldName.setColumns(20);
        textFieldName.setText("jTextField1");

        jLabel11.setText("Colour:");

        jLabel15.setText("Top width:");

        spinnerWallRoofDepth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerWallRoofDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWallRoofDepthStateChanged(evt);
            }
        });

        jLabel16.setText("Absolute min:");

        spinnerRoofMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerRoofMin.setEnabled(false);
        spinnerRoofMin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofMinStateChanged(evt);
            }
        });

        jLabel17.setText(", max:");

        spinnerRoofMax.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerRoofMax.setEnabled(false);
        spinnerRoofMax.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofMaxStateChanged(evt);
            }
        });

        jLabel18.setText("Absolute min:");

        spinnerFloorMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerFloorMin.setEnabled(false);
        spinnerFloorMin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorMinStateChanged(evt);
            }
        });

        jLabel19.setText(", max:");

        spinnerFloorMax.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerFloorMax.setEnabled(false);
        spinnerFloorMax.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorMaxStateChanged(evt);
            }
        });

        buttonReset.setText("Reset");
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });

        jLabel20.setText("Options:");

        checkBoxRemoveWater.setText("Remove water or lava:");
        checkBoxRemoveWater.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxRemoveWater.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxRemoveWaterActionPerformed(evt);
            }
        });

        checkBoxFlood.setText("Flood the caves/tunnels:");
        checkBoxFlood.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxFlood.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFloodActionPerformed(evt);
            }
        });

        jLabel21.setLabelFor(spinnerFloodLevel);
        jLabel21.setText("Level:");

        spinnerFloodLevel.setModel(new javax.swing.SpinnerNumberModel(1, 1, 255, 1));
        spinnerFloodLevel.setEnabled(false);
        spinnerFloodLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloodLevelStateChanged(evt);
            }
        });

        checkBoxFloodWithLava.setText("Flood with lava:");
        checkBoxFloodWithLava.setEnabled(false);
        checkBoxFloodWithLava.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxFloodWithLava.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFloodWithLavaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 540, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonReset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(colourEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel12)
                    .addComponent(jLabel6)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerWallFloorDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerWallRoofDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mixedMaterialSelectorWall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(radioButtonRoofFixedLevel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonRoofFixedDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonRoofInverse))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noiseSettingsEditorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mixedMaterialSelectorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mixedMaterialSelectorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(radioButtonFloorFixedLevel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFloorFixedDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFloorInverse))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noiseSettingsEditorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel20)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkBoxFlood)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxFloodWithLava))
                            .addComponent(checkBoxRemoveWater))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioButtonRoofFixedLevel)
                            .addComponent(radioButtonRoofFixedDepth)
                            .addComponent(radioButtonRoofInverse))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(spinnerRoofLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)
                            .addComponent(spinnerRoofMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)
                            .addComponent(spinnerRoofMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(noiseSettingsEditorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(mixedMaterialSelectorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioButtonFloorFixedLevel)
                            .addComponent(radioButtonFloorFixedDepth)
                            .addComponent(radioButtonFloorInverse))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel18)
                                .addComponent(spinnerFloorMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel19)
                                .addComponent(spinnerFloorMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(spinnerFloorLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(noiseSettingsEditorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(mixedMaterialSelectorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(spinnerWallFloorDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(spinnerWallRoofDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(mixedMaterialSelectorWall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxRemoveWater)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxFlood)
                            .addComponent(jLabel21)
                            .addComponent(spinnerFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkBoxFloodWithLava)))
                    .addComponent(labelPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(colourEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK)
                    .addComponent(buttonReset))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void radioButtonFloorFixedLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorFixedLevelActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorFixedLevelActionPerformed

    private void radioButtonFloorFixedDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorFixedDepthActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorFixedDepthActionPerformed

    private void radioButtonFloorInverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorInverseActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorInverseActionPerformed

    private void spinnerFloorLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorLevelStateChanged
        updatePreview();
    }//GEN-LAST:event_spinnerFloorLevelStateChanged

    private void radioButtonRoofFixedLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofFixedLevelActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofFixedLevelActionPerformed

    private void radioButtonRoofFixedDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofFixedDepthActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofFixedDepthActionPerformed

    private void radioButtonRoofInverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofInverseActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofInverseActionPerformed

    private void spinnerRoofLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofLevelStateChanged
        updatePreview();
    }//GEN-LAST:event_spinnerRoofLevelStateChanged

    private void spinnerWallFloorDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWallFloorDepthStateChanged
        updatePreview();
    }//GEN-LAST:event_spinnerWallFloorDepthStateChanged

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void spinnerWallRoofDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWallRoofDepthStateChanged
        updatePreview();
    }//GEN-LAST:event_spinnerWallRoofDepthStateChanged

    private void spinnerRoofMinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofMinStateChanged
        if ((Integer) spinnerRoofMax.getValue() < (Integer) spinnerRoofMin.getValue()) {
            spinnerRoofMax.setValue(spinnerRoofMin.getValue());
        }
        updatePreview();
    }//GEN-LAST:event_spinnerRoofMinStateChanged

    private void spinnerRoofMaxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofMaxStateChanged
        if ((Integer) spinnerRoofMax.getValue() < (Integer) spinnerRoofMin.getValue()) {
            spinnerRoofMin.setValue(spinnerRoofMax.getValue());
        }
        updatePreview();
    }//GEN-LAST:event_spinnerRoofMaxStateChanged

    private void spinnerFloorMinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorMinStateChanged
        if ((Integer) spinnerFloorMax.getValue() < (Integer) spinnerFloorMin.getValue()) {
            spinnerFloorMax.setValue(spinnerFloorMin.getValue());
        }
        updatePreview();
    }//GEN-LAST:event_spinnerFloorMinStateChanged

    private void spinnerFloorMaxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorMaxStateChanged
        if ((Integer) spinnerFloorMax.getValue() < (Integer) spinnerFloorMin.getValue()) {
            spinnerFloorMin.setValue(spinnerFloorMax.getValue());
        }
        updatePreview();
    }//GEN-LAST:event_spinnerFloorMaxStateChanged

    private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
        loadSettings();
    }//GEN-LAST:event_buttonResetActionPerformed

    private void checkBoxFloodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFloodActionPerformed
        setControlStates();
        updatePreview();
    }//GEN-LAST:event_checkBoxFloodActionPerformed

    private void checkBoxRemoveWaterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxRemoveWaterActionPerformed
        updatePreview();
    }//GEN-LAST:event_checkBoxRemoveWaterActionPerformed

    private void spinnerFloodLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloodLevelStateChanged
        updatePreview();
    }//GEN-LAST:event_spinnerFloodLevelStateChanged

    private void checkBoxFloodWithLavaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFloodWithLavaActionPerformed
        updatePreview();
    }//GEN-LAST:event_checkBoxFloodWithLavaActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonReset;
    private javax.swing.JCheckBox checkBoxFlood;
    private javax.swing.JCheckBox checkBoxFloodWithLava;
    private javax.swing.JCheckBox checkBoxRemoveWater;
    private org.pepsoft.worldpainter.ColourEditor colourEditor1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel labelPreview;
    private org.pepsoft.worldpainter.MixedMaterialSelector mixedMaterialSelectorFloor;
    private org.pepsoft.worldpainter.MixedMaterialSelector mixedMaterialSelectorRoof;
    private org.pepsoft.worldpainter.MixedMaterialSelector mixedMaterialSelectorWall;
    private org.pepsoft.worldpainter.NoiseSettingsEditor noiseSettingsEditorFloor;
    private org.pepsoft.worldpainter.NoiseSettingsEditor noiseSettingsEditorRoof;
    private javax.swing.JRadioButton radioButtonFloorFixedDepth;
    private javax.swing.JRadioButton radioButtonFloorFixedLevel;
    private javax.swing.JRadioButton radioButtonFloorInverse;
    private javax.swing.JRadioButton radioButtonRoofFixedDepth;
    private javax.swing.JRadioButton radioButtonRoofFixedLevel;
    private javax.swing.JRadioButton radioButtonRoofInverse;
    private javax.swing.JSpinner spinnerFloodLevel;
    private javax.swing.JSpinner spinnerFloorLevel;
    private javax.swing.JSpinner spinnerFloorMax;
    private javax.swing.JSpinner spinnerFloorMin;
    private javax.swing.JSpinner spinnerRoofLevel;
    private javax.swing.JSpinner spinnerRoofMax;
    private javax.swing.JSpinner spinnerRoofMin;
    private javax.swing.JSpinner spinnerWallFloorDepth;
    private javax.swing.JSpinner spinnerWallRoofDepth;
    private javax.swing.JTextField textFieldName;
    // End of variables declaration//GEN-END:variables

    private final TunnelLayer layer;
    private final int waterLevel, baseHeight, maxHeight;

    private static final long serialVersionUID = 1L;
}