/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomMaterialDialog.java
 *
 * Created on 11-okt-2011, 15:21:20
 */
package org.pepsoft.worldpainter;

import java.awt.Color;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JColorChooser;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import org.pepsoft.minecraft.Material;

import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.MixedMaterial.Row;

import org.pepsoft.worldpainter.themes.JSpinnerTableCellEditor;
import static org.pepsoft.worldpainter.MixedMaterialTableModel.*;
import static org.pepsoft.minecraft.Constants.*;

/**
 *
 * @author pepijn
 */
public class CustomMaterialDialog extends WorldPainterDialog {
    /** Creates new form CustomMaterialDialog */
    public CustomMaterialDialog(Window parent, MixedMaterial mixedMaterial, boolean extendedBlockIds) {
        super(parent);
        this.extendedBlockIds = extendedBlockIds;
        this.biome = mixedMaterial.getBiome();
        
        initComponents();

        fieldName.setText(mixedMaterial.getName());
        tableModel = new MixedMaterialTableModel(mixedMaterial);
        configureTable();
        fieldName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setControlStates();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setControlStates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setControlStates();
            }
        });
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if ((! checkBoxColour.isSelected()) && isExtendedBlockIds()) {
                    checkBoxColour.setSelected(true);
                }
                setControlStates();
                if (fieldName.getText().equals(previousCalculatedName)) {
                    String calculatedName = createName();
                    fieldName.setText(createName());
                    previousCalculatedName = calculatedName;
                }
            }
        });
        tableMaterialRows.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setControlStates();
            }
        });
        if (mixedMaterial.isNoise()) {
            radioButtonNoise.setSelected(true);
        } else {
            radioButtonBlobs.setSelected(true);
        }
        spinnerScale.setValue((int) (mixedMaterial.getScale() * 100 + 0.5f));
        previousCalculatedName = createName();
        selectedColour = (mixedMaterial.getColour() != null) ? mixedMaterial.getColour() : Color.ORANGE.getRGB();
        checkBoxColour.setSelected(mixedMaterial.getColour() != null);
        
        setControlStates();

        rootPane.setDefaultButton(buttonOK);
        
        setLocationRelativeTo(parent);
    }
    
    public MixedMaterial getMaterial() {
        return new MixedMaterial(fieldName.getText(), tableModel.getRows(), biome, radioButtonNoise.isSelected(), (Integer) spinnerScale.getValue() / 100.0f, checkBoxColour.isSelected() ? selectedColour : null);
    }
    
    @Override
    protected void ok() {
        if (tableMaterialRows.isEditing()) {
            tableMaterialRows.getCellEditor().stopCellEditing();
        }
        super.ok();
    }
    
    private void addMaterial() {
        tableModel.addMaterial(new Row(Material.DIRT, 100, 1.0f));
    }
    
    private void removeMaterial() {
        if (tableMaterialRows.isEditing()) {
            tableMaterialRows.getCellEditor().stopCellEditing();
        }
        int[] selectedRows = tableMaterialRows.getSelectedRows();
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            tableModel.removeMaterial(selectedRows[i]);
        }
    }
    
    private void setControlStates() {
        boolean nameSet = ! fieldName.getText().trim().isEmpty();
        boolean occurrenceValid = (Integer) tableModel.getValueAt(0, COLUMN_OCCURRENCE) >= 0;
        buttonOK.setEnabled(nameSet && occurrenceValid);
        int[] selectedRows = tableMaterialRows.getSelectedRows();
        boolean validRowsSelected = selectedRows.length > 0;
        for (int row: selectedRows) {
            if (row == 0) {
                validRowsSelected = false;
                break;
            }
        }
        buttonRemoveMaterial.setEnabled(validRowsSelected);
        spinnerScale.setEnabled(radioButtonBlobs.isSelected());
        tableModel.setScaleEnabled(radioButtonBlobs.isSelected());
        if (checkBoxColour.isSelected()) {
            setLabelColour();
            buttonSelectColour.setEnabled(true);
        } else {
            labelColour.setBackground(null);
            buttonSelectColour.setEnabled(false);
        }
    }
    
    private String createName() {
        Row[] rows = tableModel.getRows();
        rows = Arrays.copyOf(rows, rows.length);
        Arrays.sort(rows, new Comparator<Row>() {
            @Override
            public int compare(Row r1, Row r2) {
                return r2.occurrence - r1.occurrence;
            }
        });
        StringBuilder sb = new StringBuilder();
        for (Row row: rows) {
            if (sb.length() > 0) {
                sb.append('/');
            }
            int blockId = row.material.getBlockType();
            if ((blockId < BLOCK_TYPE_NAMES.length) && (BLOCK_TYPE_NAMES[blockId] != null)) {
                sb.append(BLOCK_TYPE_NAMES[blockId]);
            } else {
                sb.append(blockId);
            }
            int data = row.material.getData();
            if (data != 0) {
                sb.append(" (");
                sb.append(data);
                sb.append(')');
            }
        }
        return sb.toString();
    }
    
    private boolean isExtendedBlockIds() {
        for (Row row: tableModel.getRows()) {
            if (row.material.getBlockType() > HIGHEST_KNOWN_BLOCK_ID) {
                return true;
            }
        }
        return false;
    }
    
    private void pickColour() {
        Color pick = JColorChooser.showDialog(this, "Select Colour", new Color(selectedColour));
        if (pick != null) {
            selectedColour = pick.getRGB();
            setLabelColour();
        }
    }

    private void setLabelColour() {
        labelColour.setBackground(new Color(selectedColour));
    }
    
    private void configureTable() {
        tableMaterialRows.setModel(tableModel);
        TableColumn blockIDColumn = tableMaterialRows.getColumnModel().getColumn(COLUMN_BLOCK_ID);
        blockIDColumn.setCellEditor(new BlockIDTableCellEditor(extendedBlockIds));
        blockIDColumn.setCellRenderer(new BlockIDTableCellRenderer());
        SpinnerModel dataValueSpinnerModel = new SpinnerNumberModel(0, 0, 15, 1);
        tableMaterialRows.getColumnModel().getColumn(COLUMN_DATA_VALUE).setCellEditor(new JSpinnerTableCellEditor(dataValueSpinnerModel));
        SpinnerModel occurrenceSpinnerModel = new SpinnerNumberModel(1000, 0, 1000, 1);
        tableMaterialRows.getColumnModel().getColumn(COLUMN_OCCURRENCE).setCellEditor(new JSpinnerTableCellEditor(occurrenceSpinnerModel));
        if (tableModel.isScaleEnabled()) {
            SpinnerModel scaleSpinnerModel = new SpinnerNumberModel(100, 1, 9999, 1);
            tableMaterialRows.getColumnModel().getColumn(COLUMN_SCALE).setCellEditor(new JSpinnerTableCellEditor(scaleSpinnerModel));
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        fieldName = new javax.swing.JTextField();
        buttonAddMaterial = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableMaterialRows = new javax.swing.JTable();
        buttonRemoveMaterial = new javax.swing.JButton();
        radioButtonNoise = new javax.swing.JRadioButton();
        radioButtonBlobs = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        spinnerScale = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        labelColour = new javax.swing.JLabel();
        buttonSelectColour = new javax.swing.JButton();
        checkBoxColour = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Custom Material");

        jLabel1.setText("Select the block ID and data value(s) for your custom material:");

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

        jLabel4.setForeground(new java.awt.Color(0, 0, 255));
        jLabel4.setText("<html><u>Look up block ID's and data values</u></html>");
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        jLabel2.setText("Name:");

        fieldName.setColumns(20);

        buttonAddMaterial.setText("Add Material");
        buttonAddMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddMaterialActionPerformed(evt);
            }
        });

        tableMaterialRows.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tableMaterialRows);

        buttonRemoveMaterial.setText("Remove Material");
        buttonRemoveMaterial.setEnabled(false);
        buttonRemoveMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveMaterialActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonNoise);
        radioButtonNoise.setSelected(true);
        radioButtonNoise.setText("Noise");
        radioButtonNoise.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonNoiseActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonBlobs);
        radioButtonBlobs.setText("Blobs");
        radioButtonBlobs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBlobsActionPerformed(evt);
            }
        });

        jLabel3.setText("Scale:");

        spinnerScale.setModel(new javax.swing.SpinnerNumberModel(100, 1, 9999, 1));
        spinnerScale.setEnabled(false);

        jLabel5.setText("Colour:");

        labelColour.setText("                 ");
        labelColour.setToolTipText("Select to override actual block colours");
        labelColour.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        labelColour.setOpaque(true);

        buttonSelectColour.setText("...");
        buttonSelectColour.setEnabled(false);
        buttonSelectColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectColourActionPerformed(evt);
            }
        });

        checkBoxColour.setText(" ");
        checkBoxColour.setToolTipText("Select to override actual block colours");
        checkBoxColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxColourActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonRemoveMaterial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonAddMaterial))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(radioButtonNoise)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonBlobs)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(checkBoxColour)
                                        .addGap(0, 0, 0)
                                        .addComponent(labelColour)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buttonSelectColour))
                                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(labelColour)
                    .addComponent(buttonSelectColour)
                    .addComponent(checkBoxColour))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonAddMaterial)
                    .addComponent(buttonRemoveMaterial))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioButtonNoise)
                            .addComponent(radioButtonBlobs)
                            .addComponent(jLabel3)
                            .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buttonCancel)
                            .addComponent(buttonOK))
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        try {
            DesktopUtils.open(new URL("http://www.minecraftwiki.net/wiki/Data_values"));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL exception while trying to open http://www.minecraftwiki.net/wiki/Data_values", e);
        }
    }//GEN-LAST:event_jLabel4MouseClicked

    private void buttonAddMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddMaterialActionPerformed
        addMaterial();
    }//GEN-LAST:event_buttonAddMaterialActionPerformed

    private void buttonRemoveMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveMaterialActionPerformed
        removeMaterial();
    }//GEN-LAST:event_buttonRemoveMaterialActionPerformed

    private void radioButtonNoiseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonNoiseActionPerformed
        setControlStates();
        configureTable();
    }//GEN-LAST:event_radioButtonNoiseActionPerformed

    private void radioButtonBlobsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonBlobsActionPerformed
        setControlStates();
        configureTable();
    }//GEN-LAST:event_radioButtonBlobsActionPerformed

    private void checkBoxColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxColourActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxColourActionPerformed

    private void buttonSelectColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectColourActionPerformed
        pickColour();
    }//GEN-LAST:event_buttonSelectColourActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddMaterial;
    private javax.swing.JButton buttonCancel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonRemoveMaterial;
    private javax.swing.JButton buttonSelectColour;
    private javax.swing.JCheckBox checkBoxColour;
    private javax.swing.JTextField fieldName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelColour;
    private javax.swing.JRadioButton radioButtonBlobs;
    private javax.swing.JRadioButton radioButtonNoise;
    private javax.swing.JSpinner spinnerScale;
    private javax.swing.JTable tableMaterialRows;
    // End of variables declaration//GEN-END:variables

    private final MixedMaterialTableModel tableModel;
    private final boolean extendedBlockIds;
    private int biome;
    private String previousCalculatedName;
    private int selectedColour = Color.ORANGE.getRGB();
    
    private static final long serialVersionUID = 1L;
}