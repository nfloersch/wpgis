/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportWorldDialog.java
 *
 * Created on Mar 29, 2011, 5:09:50 PM
 */

package org.pepsoft.worldpainter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.util.MinecraftUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.pepsoft.minecraft.Level;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressComponent.Listener;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.merging.WorldMerger;
import org.pepsoft.worldpainter.util.FileInUseException;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.layers.Layer;

/**
 *
 * @author pepijn
 */
// TODO: add support for multiple dimensions
public class OverlayResourcesDialog extends javax.swing.JDialog implements Listener {
    /** Creates new form ExportWorldDialog */
    public OverlayResourcesDialog(java.awt.Frame parent, WorldPainter view) {
        super(parent, true);
        selectedDimension = DIM_NORMAL;
        
        initComponents();

        Configuration config = Configuration.getInstance();
        
        DocumentListener documentListener = new DocumentListener() {
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
        };
        jTextField_OverlayResources_PNGPath.getDocument().addDocumentListener(documentListener);

        setLocationRelativeTo(parent);

        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("cancel", new AbstractAction("cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
            
            private static final long serialVersionUID = 1L;
        });

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        rootPane.setDefaultButton(jButton_OverlayResources_Roads);

        setControlStates();
    }

    // ProgressComponent.Listener

    @Override
    public void exceptionThrown(final Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof FileInUseException) {
            JOptionPane.showMessageDialog(OverlayResourcesDialog.this, "File In use Exception", "File In Use", JOptionPane.ERROR_MESSAGE);
        } else {
            ErrorDialog dialog = new ErrorDialog(OverlayResourcesDialog.this);
            dialog.setException(exception);
            dialog.setVisible(true);
        }
        close();
    }

    @Override
    public void done(Object result) {
        long end = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        long duration = (end - start) / 1000;
        int hours = (int) (duration / 3600);
        duration = duration - hours * 3600;
        int minutes = (int) (duration / 60);
        int seconds = (int) (duration - minutes * 60);
        sb.append("\nProcess took ").append(hours).append(":").append((minutes < 10) ? "0" : "").append(minutes).append(":").append((seconds < 10) ? "0" : "").append(seconds);
        JOptionPane.showMessageDialog(OverlayResourcesDialog.this, sb.toString(), "Success", JOptionPane.INFORMATION_MESSAGE);
        close();
    }

    @Override
    public void cancelled() {
        close();
    }

    private void processOverlay() {
        

        Configuration config = Configuration.getInstance();
        

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        start = System.currentTimeMillis();
        progressComponent1.setTask(new ProgressTask<Void>() {
            @Override
            public String getName() {
                return "Please wait";
            }

            @Override
            public Void execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                final WorldMerger merger = null;//new WorldMerger(world, levelDatFile);
                try {
                    // DOES THE WORK
                    //merger.merge(jTextField_OverlayResources_PNGPath, progressReceiver);
                    // DID THE WORK
                    if (merger.getWarnings() != null) {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");
                                    Toolkit.getDefaultToolkit().beep();
                                    
                                }
                            });
                           throw new IOException();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("I/O error while merging world", e);
                }
                return null;
            }
        });
        progressComponent1.setListener(this);
        progressComponent1.start();
    }

    private void close() {
        dispose();
    }

    // Init Form Control States
    private void setControlStates() {
        File file = new File(jTextField_OverlayResources_PNGPath.getText().trim());
        
    }

    // File Chooser Functionality
    private void selectImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        File file = new File(jTextField_OverlayResources_PNGPath.getText().trim());
        if (file.isDirectory()) {
            fileChooser.setCurrentDirectory(file);
        } else {
            fileChooser.setSelectedFile(file);
        }
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().equalsIgnoreCase("level.dat");
            }

            @Override
            public String getDescription() {
                return "Black and White Bitmaps";
            }
        });
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            jTextField_OverlayResources_PNGPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel_OverlayResources_Title = new javax.swing.JLabel();
        jTextField_OverlayResources_PNGPath = new javax.swing.JTextField();
        jButton_OverlayResources_SelectPNG = new javax.swing.JButton();
        jButton_OverlayResources_Roads = new javax.swing.JButton();
        progressComponent1 = new org.pepsoft.util.swing.ProgressComponent();
        jRadioButton_OverlayResources_RaiseLowerTerrainOption = new javax.swing.JRadioButton();
        jRadioButton_OverlayResources_ReplaceTerrainOption = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        radioButtonBiomes1 = new javax.swing.JRadioButton();
        radioButtonAll1 = new javax.swing.JRadioButton();
        jSpinner2 = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jButton_OverlayResources_Rivers = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Merging");

        jLabel_OverlayResources_Title.setText("Black and White Bitmap Image - Actionable Area Is Where Black Part of Image Is");

        jTextField_OverlayResources_PNGPath.setText("jTextField1");

        jButton_OverlayResources_SelectPNG.setText("...");
        jButton_OverlayResources_SelectPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OverlayResources_SelectPNGActionPerformed(evt);
            }
        });

        jButton_OverlayResources_Roads.setText("Roads");
        jButton_OverlayResources_Roads.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OverlayResources_RoadsActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton_OverlayResources_RaiseLowerTerrainOption);
        jRadioButton_OverlayResources_RaiseLowerTerrainOption.setText("Raise/Lower Terrain");
        jRadioButton_OverlayResources_RaiseLowerTerrainOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_OverlayResources_RaiseLowerTerrainOptionActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton_OverlayResources_ReplaceTerrainOption);
        jRadioButton_OverlayResources_ReplaceTerrainOption.setSelected(true);
        jRadioButton_OverlayResources_ReplaceTerrainOption.setText("Replace Terrain");
        jRadioButton_OverlayResources_ReplaceTerrainOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_OverlayResources_ReplaceTerrainOptionActionPerformed(evt);
            }
        });

        jLabel1.setText("Choose How To Use The Mask...");

        jLabel4.setText("Raise or Lower How Much?");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0, -255, 255, 1));
        jSpinner1.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner1, ""));

        jLabel2.setText("Positive Numbers Adds Levels");

        jLabel3.setText("Negative Numbers Subtracts Levels");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setText("Currently Choices Is To Replace w/Water or Road");

        buttonGroup1.add(radioButtonBiomes1);
        radioButtonBiomes1.setText("Road");
        radioButtonBiomes1.setToolTipText("Road");

        buttonGroup1.add(radioButtonAll1);
        radioButtonAll1.setSelected(true);
        radioButtonAll1.setText("Water");
        radioButtonAll1.setToolTipText("Water");
        radioButtonAll1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonAll1ActionPerformed(evt);
            }
        });

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(1, -3, 3, 1));

        jTextPane1.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        jTextPane1.setBorder(null);
        jTextPane1.setText("Number of blocks deep to insert new terrain. A negative value indicates stacking new terrain on top of existing terrain, not replacing anything.");
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(radioButtonBiomes1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(radioButtonAll1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonAll1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonBiomes1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jButton_OverlayResources_Rivers.setText("Rivers");
        jButton_OverlayResources_Rivers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OverlayResources_RiversActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_OverlayResources_Title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressComponent1, javax.swing.GroupLayout.DEFAULT_SIZE, 667, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jTextField_OverlayResources_PNGPath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_OverlayResources_SelectPNG))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jRadioButton_OverlayResources_RaiseLowerTerrainOption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton_OverlayResources_ReplaceTerrainOption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton_OverlayResources_Roads)
                .addGap(18, 18, 18)
                .addComponent(jButton_OverlayResources_Rivers)
                .addGap(58, 58, 58))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addComponent(jLabel_OverlayResources_Title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_OverlayResources_PNGPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_OverlayResources_SelectPNG))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton_OverlayResources_RaiseLowerTerrainOption)
                    .addComponent(jRadioButton_OverlayResources_ReplaceTerrainOption))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressComponent1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_OverlayResources_Roads)
                    .addComponent(jButton_OverlayResources_Rivers)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_OverlayResources_RoadsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OverlayResources_RoadsActionPerformed
        processOverlay();
    }//GEN-LAST:event_jButton_OverlayResources_RoadsActionPerformed

    private void jButton_OverlayResources_SelectPNGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OverlayResources_SelectPNGActionPerformed
        selectImageFile();
    }//GEN-LAST:event_jButton_OverlayResources_SelectPNGActionPerformed

    private void jRadioButton_OverlayResources_RaiseLowerTerrainOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_OverlayResources_RaiseLowerTerrainOptionActionPerformed
        setControlStates();
    }//GEN-LAST:event_jRadioButton_OverlayResources_RaiseLowerTerrainOptionActionPerformed

    private void jRadioButton_OverlayResources_ReplaceTerrainOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_OverlayResources_ReplaceTerrainOptionActionPerformed
        setControlStates();

    }//GEN-LAST:event_jRadioButton_OverlayResources_ReplaceTerrainOptionActionPerformed

    private void radioButtonAll1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonAll1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioButtonAll1ActionPerformed

    private void jButton_OverlayResources_RiversActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OverlayResources_RiversActionPerformed
        processOverlay();
    }//GEN-LAST:event_jButton_OverlayResources_RiversActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton_OverlayResources_Rivers;
    private javax.swing.JButton jButton_OverlayResources_Roads;
    private javax.swing.JButton jButton_OverlayResources_SelectPNG;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel_OverlayResources_Title;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton_OverlayResources_RaiseLowerTerrainOption;
    private javax.swing.JRadioButton jRadioButton_OverlayResources_ReplaceTerrainOption;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JTextField jTextField_OverlayResources_PNGPath;
    private javax.swing.JTextPane jTextPane1;
    private org.pepsoft.util.swing.ProgressComponent progressComponent1;
    private javax.swing.JRadioButton radioButtonAll1;
    private javax.swing.JRadioButton radioButtonBiomes1;
    // End of variables declaration//GEN-END:variables


    private volatile boolean cancelled;
    private long start;
    private int selectedDimension;

    private static final long serialVersionUID = 1L;
}