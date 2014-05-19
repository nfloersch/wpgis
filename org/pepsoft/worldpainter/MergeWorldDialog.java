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
public class MergeWorldDialog extends javax.swing.JDialog implements Listener {
    /** Creates new form ExportWorldDialog */
    public MergeWorldDialog(java.awt.Frame parent, World2 world, BiomeScheme biomeScheme, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, Collection<Layer> hiddenLayers, boolean contourLines, TileRenderer.LightOrigin lightOrigin) {
        super(parent, true);
        this.world = world;
        this.biomeScheme = biomeScheme;
        this.colourScheme = colourScheme;
        this.hiddenLayers = hiddenLayers;
        this.contourLines = contourLines;
        this.lightOrigin = lightOrigin;
        this.customBiomeManager = customBiomeManager;
        selectedTiles = world.getTilesToExport();
        selectedDimension = (selectedTiles != null) ? world.getDimensionToExport() : DIM_NORMAL;
        
        initComponents();

        Configuration config = Configuration.getInstance();
        if (world.getImportedFrom() != null) {
            fieldLevelDatFile.setText(world.getImportedFrom().getAbsolutePath());
        } else if ((config != null) && (config.getSavesDirectory() != null)) {
            fieldLevelDatFile.setText(config.getSavesDirectory().getAbsolutePath());
        } else {
            File minecraftDir = MinecraftUtil.findMinecraftDir();
            if (minecraftDir != null) {
                fieldLevelDatFile.setText(new File(minecraftDir, "saves").getAbsolutePath());
            } else {
                fieldLevelDatFile.setText(DesktopUtils.getDocumentsFolder().getAbsolutePath());
            }
        }
        if (selectedTiles != null) {
            radioButtonExportSelection.setText("merge " + selectedTiles.size() + " selected tiles");
            radioButtonExportSelection.setSelected(true);
        }
        
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
        fieldLevelDatFile.getDocument().addDocumentListener(documentListener);

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

        rootPane.setDefaultButton(buttonMerge);

        setControlStates();
        pack();
    }

    // ProgressComponent.Listener

    @Override
    public void exceptionThrown(final Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof FileInUseException) {
            JOptionPane.showMessageDialog(MergeWorldDialog.this, "Could not merge the world because the existing map directory is in use.\nPlease close Minecraft and all other windows and try again.", "Map In Use", JOptionPane.ERROR_MESSAGE);
        } else {
            ErrorDialog dialog = new ErrorDialog(MergeWorldDialog.this);
            dialog.setException(exception);
            dialog.setVisible(true);
        }
        close();
    }

    @Override
    public void done(Object result) {
        long end = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("World merged with ").append(levelDatFile);
        long duration = (end - start) / 1000;
        int hours = (int) (duration / 3600);
        duration = duration - hours * 3600;
        int minutes = (int) (duration / 60);
        int seconds = (int) (duration - minutes * 60);
        sb.append("\nMerge took ").append(hours).append(":").append((minutes < 10) ? "0" : "").append(minutes).append(":").append((seconds < 10) ? "0" : "").append(seconds);
        sb.append("\n\nBackup of existing map created in:\n").append(backupDir);
        JOptionPane.showMessageDialog(MergeWorldDialog.this, sb.toString(), "Success", JOptionPane.INFORMATION_MESSAGE);
        close();
    }

    @Override
    public void cancelled() {
        JOptionPane.showMessageDialog(MergeWorldDialog.this, "Export cancelled by user.\n\nThe partially merged map is now probably corrupted!\nYou should delete it, and restore it from the backup at:\n" + backupDir, "Merge Cancelled", JOptionPane.WARNING_MESSAGE);
        close();
    }

    private void merge() {
        StringBuilder sb = new StringBuilder("<html>Please confirm that you want to merge the world<br>notwithstanding the following warnings:<br><ul>");
        boolean showWarning = false;
        if ((radioButtonExportSelection.isSelected()) && (! disableWarning)) {
            String dim;
            switch (selectedDimension) {
                case DIM_NORMAL:
                    dim = "Surface";
                    break;
                case DIM_NETHER:
                    dim = "Nether";
                    break;
                case DIM_END:
                    dim = "End";
                    break;
                default:
                    throw new InternalError();
            }
            sb.append("<li>A tile selection is active! Only " + selectedTiles.size() + " tiles of the<br>" + dim + " dimension are going to be merged.");
            showWarning = showWarning || (! disableWarning);
        }
        sb.append("</ul>Do you want to continue with the merge?</html>");
        if (showWarning && (JOptionPane.showConfirmDialog(this, sb.toString(), "Review Warnings", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)) {
            return;
        }

        final boolean biomesOnly = radioButtonBiomes.isSelected();
        final boolean replaceChunks = jRadioButton1.isSelected();

        fieldLevelDatFile.setEnabled(false);
        buttonSelectDirectory.setEnabled(false);
        buttonMerge.setEnabled(false);
        radioButtonAll.setEnabled(false);
        radioButtonBiomes.setEnabled(false);
        jRadioButton1.setEnabled(false);
        radioButtonExportEverything.setEnabled(false);
        radioButtonExportSelection.setEnabled(false);
        labelSelectTiles.setForeground(null);
        labelSelectTiles.setCursor(null);

        Configuration config = Configuration.getInstance();
        config.setSavesDirectory(levelDatFile.getParentFile().getParentFile());
        config.setMergeWarningDisplayed(true);
        world.setImportedFrom(levelDatFile);
        if (radioButtonExportEverything.isSelected()) {
            world.setDimensionToExport(DIM_NORMAL);
            world.setTilesToExport(null);
        } else {
            world.setDimensionToExport(selectedDimension);
            world.setTilesToExport(selectedTiles);
        }

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        start = System.currentTimeMillis();
        progressComponent1.setTask(new ProgressTask<Void>() {
            @Override
            public String getName() {
                return "Please wait";
            }

            @Override
            public Void execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                final WorldMerger merger = new WorldMerger(world, levelDatFile);
                try {
                    backupDir = merger.selectBackupDir(levelDatFile.getParentFile());
                    if (biomesOnly) {
                        merger.mergeBiomes(backupDir, progressReceiver);
                    } else {
                        if (replaceChunks) {
                            merger.setReplaceChunks(true);
                        }
                        merger.merge(backupDir, progressReceiver);
                    }
                    if (merger.getWarnings() != null) {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");
                                    Toolkit.getDefaultToolkit().beep();
                                    int selectedOption = JOptionPane.showOptionDialog(MergeWorldDialog.this, "The merge process generated warnings! The existing map may have had pre-\nexisting damage or corruption. Not all chunks may have been merged correctly.", "Merge Warnings", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, warningIcon, new Object[] {"Review warnings", "OK"}, null);
                                    if (selectedOption == 0) {
                                        ImportWarningsDialog warningsDialog = new ImportWarningsDialog(MergeWorldDialog.this, "Merge Warnings");
                                        warningsDialog.setWarnings(merger.getWarnings());
                                        warningsDialog.setVisible(true);
                                    }
                                }
                            });
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

    private void setControlStates() {
        File file = new File(fieldLevelDatFile.getText().trim());
        boolean levelDatSelected = file.isFile() && (file.getName().equalsIgnoreCase("level.dat"));
        if (levelDatSelected) {
            levelDatFile = file;
            try {
                Level level = Level.load(levelDatFile);
                if (level.getVersion() != org.pepsoft.minecraft.Constants.SUPPORTED_VERSION_2) {
                    if (radioButtonBiomes.isSelected()) {
                        radioButtonAll.setSelected(true);
                    }
                    radioButtonBiomes.setEnabled(false);
                } else {
                    radioButtonBiomes.setEnabled(true);
                }
            } catch (IOException e) {
                throw new RuntimeException("I/O error while loading level.dat", e);
            }
        }
        buttonMerge.setEnabled(levelDatSelected);
        if (radioButtonExportSelection.isSelected()) {
            labelSelectTiles.setForeground(Color.BLUE);
            labelSelectTiles.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            labelSelectTiles.setForeground(null);
            labelSelectTiles.setCursor(null);
        }
    }

    private void selectLevelDatFile() {
        JFileChooser fileChooser = new JFileChooser();
        File file = new File(fieldLevelDatFile.getText().trim());
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
                return "Minecraft level.dat files";
            }
        });
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fieldLevelDatFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void selectTiles() {
        if (radioButtonExportSelection.isSelected()) {
            ExportTileSelectionDialog dialog = new ExportTileSelectionDialog(this, world, selectedDimension, selectedTiles, colourScheme, biomeScheme, customBiomeManager, hiddenLayers, contourLines, lightOrigin);
            dialog.setVisible(true);
            selectedDimension = dialog.getSelectedDimension();
            selectedTiles = dialog.getSelectedTiles();
            radioButtonExportSelection.setText("merge " + selectedTiles.size() + " selected tiles");
            pack();
            setControlStates();
            disableWarning = true;
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
        jLabel2 = new javax.swing.JLabel();
        fieldLevelDatFile = new javax.swing.JTextField();
        buttonSelectDirectory = new javax.swing.JButton();
        buttonMerge = new javax.swing.JButton();
        radioButtonAll = new javax.swing.JRadioButton();
        radioButtonBiomes = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        progressComponent1 = new org.pepsoft.util.swing.ProgressComponent();
        radioButtonExportEverything = new javax.swing.JRadioButton();
        radioButtonExportSelection = new javax.swing.JRadioButton();
        labelSelectTiles = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Merging");

        jLabel2.setText("Existing map to merge with:");

        fieldLevelDatFile.setText("jTextField1");

        buttonSelectDirectory.setText("...");
        buttonSelectDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectDirectoryActionPerformed(evt);
            }
        });

        buttonMerge.setText("Merge");
        buttonMerge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMergeActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonAll);
        radioButtonAll.setSelected(true);
        radioButtonAll.setText("Merge old and new chunks");
        radioButtonAll.setToolTipText("Will merge everything (terrain type and height changes, new layers, biome changes, etc.). Takes a very long time.");

        buttonGroup1.add(radioButtonBiomes);
        radioButtonBiomes.setText("Only change the biomes");
        radioButtonBiomes.setToolTipText("<html>Will merge <i>only</i> biome changes. Ignores the read-only layer. Much quicker than merging everything, and with no side effects.</html>");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Completely replace chunks with new chunks");

        jLabel3.setText("<html><i>This will </i>replace<i> all non-read-only chunks,<br>destroying everything that's there in the existing map! </i></html>");

        buttonGroup2.add(radioButtonExportEverything);
        radioButtonExportEverything.setSelected(true);
        radioButtonExportEverything.setText("Merge everything");
        radioButtonExportEverything.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonExportEverythingActionPerformed(evt);
            }
        });

        buttonGroup2.add(radioButtonExportSelection);
        radioButtonExportSelection.setText("merge selected tiles");
        radioButtonExportSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonExportSelectionActionPerformed(evt);
            }
        });

        labelSelectTiles.setText("<html><u>select tiles</u></html>");
        labelSelectTiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelSelectTilesMouseClicked(evt);
            }
        });

        jLabel1.setText("Choose which part of the map to merge:");

        jLabel4.setText("Choose what kind of merge to perform:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fieldLevelDatFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectDirectory))
                    .addComponent(progressComponent1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonMerge, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel2)
                            .addComponent(radioButtonAll)
                            .addComponent(radioButtonBiomes)
                            .addComponent(jRadioButton1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(radioButtonExportEverything)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonExportSelection)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelSelectTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addComponent(jLabel4))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldLevelDatFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectDirectory))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonExportEverything)
                    .addComponent(radioButtonExportSelection)
                    .addComponent(labelSelectTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonBiomes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(progressComponent1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonMerge)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMergeActionPerformed
        merge();
    }//GEN-LAST:event_buttonMergeActionPerformed

    private void buttonSelectDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectDirectoryActionPerformed
        selectLevelDatFile();
    }//GEN-LAST:event_buttonSelectDirectoryActionPerformed

    private void radioButtonExportEverythingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonExportEverythingActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonExportEverythingActionPerformed

    private void radioButtonExportSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonExportSelectionActionPerformed
        if (radioButtonExportSelection.isSelected()) {
            selectTiles();
        } else {
            setControlStates();
        }
    }//GEN-LAST:event_radioButtonExportSelectionActionPerformed

    private void labelSelectTilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelSelectTilesMouseClicked
        selectTiles();
    }//GEN-LAST:event_labelSelectTilesMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton buttonMerge;
    private javax.swing.JButton buttonSelectDirectory;
    private javax.swing.JTextField fieldLevelDatFile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JLabel labelSelectTiles;
    private org.pepsoft.util.swing.ProgressComponent progressComponent1;
    private javax.swing.JRadioButton radioButtonAll;
    private javax.swing.JRadioButton radioButtonBiomes;
    private javax.swing.JRadioButton radioButtonExportEverything;
    private javax.swing.JRadioButton radioButtonExportSelection;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final BiomeScheme biomeScheme;
    private final ColourScheme colourScheme;
    private final Collection<Layer> hiddenLayers;
    private final boolean contourLines;
    private final TileRenderer.LightOrigin lightOrigin;
    private final CustomBiomeManager customBiomeManager;
    private File levelDatFile;
    private volatile File backupDir;
    private long start;
    private int selectedDimension;
    private Set<Point> selectedTiles;
    private boolean disableWarning;
    
    private static final long serialVersionUID = 1L;
}