/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImportHeightMapDialog.java
 *
 * Created on 22-jan-2012, 19:47:55
 */
package org.pepsoft.worldpainter;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.heightMaps.HeightMapUtils;

import static org.pepsoft.minecraft.Constants.DEFAULT_MAX_HEIGHT_2;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE_BITS;

/**
 *
 * @author pepijn
 */
public class ImportHeightMapDialog extends javax.swing.JDialog implements DocumentListener {
    /** Creates new form ImportHeightMapDialog */
    public ImportHeightMapDialog(java.awt.Frame parent) {
        super(parent, true);
        
        tileFactory = TileFactoryFactory.createNoiseTileFactory(Terrain.GRASS, DEFAULT_MAX_HEIGHT_2, 58, 62, false, true, 20, 1.0);
        
        initComponents();
        
        heightMapTileFactoryEditor1.setTileFactory(tileFactory);
        spinnerOffsetX.setEditor(new NumberEditor(spinnerOffsetX, "0"));
        spinnerOffsetY.setEditor(new NumberEditor(spinnerOffsetY, "0"));
        pack();
        labelWarning.setVisible(false);
        
        setLocationRelativeTo(parent);
        
        fieldFilename.getDocument().addDocumentListener(this);
        
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
        
        rootPane.setDefaultButton(buttonOk);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public World2 getImportedWorld() {
        logger.info("Importing world from height map " + selectedFile.getAbsolutePath() + " (size: " + image.getWidth() + "x" + image.getHeight() + ")");
        final int scale = (Integer) spinnerScale.getValue();
        final boolean invert = checkBoxInvert.isSelected();
        final int maxHeight = Integer.parseInt((String) comboBoxHeight.getSelectedItem());
        final int imageLowLevel    = (Integer) spinnerImageLow.getValue();
        final int imageHighLevel   = (Integer) spinnerImageHigh.getValue();
        final int worldLowLevel    = (Integer) spinnerWorldLow.getValue();
        final int worldMiddleLevel = (Integer) spinnerWorldMiddle.getValue();
        final int worldHighLevel   = (Integer) spinnerWorldHigh.getValue();
        final int voidBelowLevel   = (Integer) spinnerVoidBelow.getValue();
        final int offsetX = (Integer) spinnerOffsetX.getValue(), offsetY = (Integer) spinnerOffsetY.getValue();
        final boolean useVoidBelow = checkBoxVoid.isSelected();
        return ProgressDialog.executeTask(this, new ProgressTask<World2>() {
            @Override
            public String getName() {
                return "Importing height map";
            }

            @Override
            public World2 execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                int widthInBlocks = image.getWidth() * scale / 100;
                int heightInBlocks = image.getHeight() * scale / 100;
                BufferedImage scaledImage;
                if ((scale != 100) || (image.getType() != BufferedImage.TYPE_BYTE_GRAY)) {
                    scaledImage = new BufferedImage(widthInBlocks, heightInBlocks, BufferedImage.TYPE_BYTE_GRAY);
                    Graphics2D g2 = scaledImage.createGraphics();
                    try {
                        if (scale != 100) {
                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        }
                        g2.drawImage(image, 0, 0, widthInBlocks, heightInBlocks, null);
                    } finally {
                        g2.dispose();
                    }
                } else {
                    scaledImage = image;
                }
                image = null; // The image is no longer necessary, so allow it to be garbage collected to make more space available for the import
                boolean oneOnOne = (worldLowLevel == imageLowLevel) && (worldHighLevel == imageHighLevel);
                float levelScale = (float) (worldHighLevel - worldLowLevel) / (imageHighLevel - imageLowLevel);
                long seed = new Random().nextLong();
                World2 world = new World2(seed, seed, tileFactory, maxHeight);
                String name = selectedFile.getName();
                int p = name.lastIndexOf('.');
                if (p != -1) {
                    name = name.substring(0, p);
                }
                world.setName(name);
                if (maxHeight == DEFAULT_MAX_HEIGHT_2) {
                    world.setBiomeAlgorithm(World2.BIOME_ALGORITHM_AUTO_BIOMES);
                } else {
                    world.setBiomeAlgorithm(World2.BIOME_ALGORITHM_NONE);
                }
                world.setCustomBiomes(true);
                Dimension dimension = world.getDimension(0);
                int tileX1 = offsetX >> TILE_SIZE_BITS;
                int tileY1 = offsetY >> TILE_SIZE_BITS;
                int tileX2 = (offsetX + widthInBlocks - 1) >> TILE_SIZE_BITS;
                int tileY2 = (offsetY + heightInBlocks - 1) >> TILE_SIZE_BITS;
                int widthInTiles = tileX2 - tileX1 + 1;
                int heightInTiles = tileY2 - tileY1 + 1;
                byte[] data = ((DataBufferByte) scaledImage.getRaster().getDataBuffer()).getData();
                int totalTileCount = widthInTiles * heightInTiles, tileCount = 0;
                for (int tileX = tileX1; tileX <= tileX2; tileX++) {
                    for (int tileY = tileY1; tileY <= tileY2; tileY++) {
                        Tile tile = new Tile(tileX, tileY, maxHeight);
                        int xOffset = tileX * TILE_SIZE - offsetX;
                        int yOffset = tileY * TILE_SIZE - offsetY;
                        for (int x = 0; x < TILE_SIZE; x++) {
                            for (int y = 0; y < TILE_SIZE; y++) {
                                int imageX = xOffset + x;
                                int imageY = yOffset + y;
                                int level;
                                boolean void_;
                                if ((imageX >= 0) && (imageX < widthInBlocks) && (imageY >= 0) && (imageY < heightInBlocks)) {
                                    // The & 0xFF is to convert the byte to a
                                    // positive integer
                                    int imageLevel;
                                    if (invert) {
                                        imageLevel = 255 - data[imageX + imageY * widthInBlocks] & 0xFF;
                                    } else {
                                        imageLevel = data[imageX + imageY * widthInBlocks] & 0xFF;
                                    }
                                    if (imageLevel < imageLowLevel) {
                                        level = imageLowLevel;
                                    } else if (imageLevel > imageHighLevel) {
                                        level = imageHighLevel;
                                    } else {
                                        level = imageLevel;
                                    }
                                    void_ = useVoidBelow && (imageLevel < voidBelowLevel);
                                } else {
                                    level = imageLowLevel;
                                    void_ = useVoidBelow;
                                }
                                tile.setHeight(x, y, oneOnOne
                                    ? level
                                    : (int) ((level - imageLowLevel) * levelScale + worldLowLevel));
                                tile.setWaterLevel(x, y, worldMiddleLevel);
                                if (void_) {
                                    tile.setBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, x, y, true);
                                }
                                tileFactory.applyTheme(seed, tile, x, y);
                            }
                        }
                        dimension.addTile(tile);
                        tileCount++;
                        if (progressReceiver != null) {
                            progressReceiver.setProgress((float) tileCount / totalTileCount);
                        }
                    }
                }
                Configuration config = Configuration.getInstance();
                dimension.setGridEnabled(config.isDefaultGridEnabled());
                dimension.setGridSize(config.getDefaultGridSize());
                dimension.setContoursEnabled(config.isDefaultContoursEnabled());
                dimension.setContourSeparation(config.getDefaultContourSeparation());
                world.setSpawnPoint(new Point(offsetX + widthInBlocks / 2, offsetY + heightInBlocks / 2));
                world.setDirty(false);
                return world;
            }
        }, false);
    }
    
    // DocumentListener
    
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

    private void setControlStates() {
        File file = new File(fieldFilename.getText());
        if ((file.isFile()) && ((selectedFile == null) || (! file.equals(selectedFile)))) {
            selectedFile = file;
            loadImage();
        }
        boolean fileSelected = file.isFile();
        buttonOk.setEnabled(fileSelected);
    }

    private void loadImage() {
        try {
            image = null; // Set image to null first to make more memory available for loading the new image
            image = ImageIO.read(selectedFile);
            labelImageDimensions.setText("Image size: " + image.getWidth() + " x " + image.getHeight());
            updateWorldDimensions();
        } catch (IOException e) {
            throw new RuntimeException("I/O error loading image " + selectedFile, e);
        }
    }

    private void updateWorldDimensions() {
        int scale = (Integer) spinnerScale.getValue(), offsetX = (Integer) spinnerOffsetX.getValue(), offsetY = (Integer) spinnerOffsetY.getValue();
        int tileX1 = offsetX >> TILE_SIZE_BITS;
        int tileY1 = offsetY >> TILE_SIZE_BITS;
        int tileX2 = (offsetX + (image.getWidth() * scale / 100) - 1) >> TILE_SIZE_BITS;
        int tileY2 = (offsetY + (image.getHeight() * scale / 100) - 1) >> TILE_SIZE_BITS;
        labelWorldDimensions.setText("World size: " + ((tileX2 - tileX1 + 1) * 128) + " x " + ((tileY2 - tileY1 + 1) * 128) + " blocks (in multiples of 128)");
    }
    
    private void updateWorldWaterLevel() {
        int imageLowLevel    = (Integer) spinnerImageLow.getValue();
        int imageHighLevel   = (Integer) spinnerImageHigh.getValue();
        int worldLowLevel    = (Integer) spinnerWorldLow.getValue();
        int worldMiddleLevel = (Integer) spinnerWorldMiddle.getValue();
        int worldHighLevel   = (Integer) spinnerWorldHigh.getValue();
        float levelScale = (float) (worldHighLevel - worldLowLevel) / (imageHighLevel - imageLowLevel);
        int imageMiddleLevel = (int) ((worldMiddleLevel - worldLowLevel) / levelScale + imageLowLevel);
        if (imageMiddleLevel < 0) {
            labelWorldWaterLevel.setText("< 0");
        } else if (imageMiddleLevel > 255) {
            labelWorldWaterLevel.setText("> 255");
        } else {
            labelWorldWaterLevel.setText(Integer.toString(imageMiddleLevel));
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
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        fieldFilename = new javax.swing.JTextField();
        buttonSelectFile = new javax.swing.JButton();
        labelImageDimensions = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOk = new javax.swing.JButton();
        checkBoxInvert = new javax.swing.JCheckBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        spinnerScale = new javax.swing.JSpinner();
        labelWorldDimensions = new javax.swing.JLabel();
        comboBoxHeight = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        spinnerImageLow = new javax.swing.JSpinner();
        spinnerWorldLow = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        labelWorldWaterLevel = new javax.swing.JLabel();
        spinnerWorldMiddle = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        spinnerImageHigh = new javax.swing.JSpinner();
        spinnerWorldHigh = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        checkBoxVoid = new javax.swing.JCheckBox();
        spinnerVoidBelow = new javax.swing.JSpinner();
        labelWarning = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        spinnerOffsetX = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        spinnerOffsetY = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        heightMapTileFactoryEditor1 = new org.pepsoft.worldpainter.terrainRanges.HeightMapTileFactoryEditor();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Import Height Map");

        jLabel1.setText("Select the image to import as a height map:");

        buttonSelectFile.setText("...");
        buttonSelectFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectFileActionPerformed(evt);
            }
        });

        labelImageDimensions.setText("Image size: ? x ?");

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOk.setText("OK");
        buttonOk.setEnabled(false);
        buttonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOkActionPerformed(evt);
            }
        });

        checkBoxInvert.setText("Invert (white is low, black is high)");

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jLabel3.setText("Scale:");

        spinnerScale.setModel(new javax.swing.SpinnerNumberModel(100, 1, 999, 1));
        spinnerScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerScaleStateChanged(evt);
            }
        });

        labelWorldDimensions.setText("World size: ? x ? blocks (in multiples of 128)");

        comboBoxHeight.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "32", "64", "128", "256", "512", "1024", "2048" }));
        comboBoxHeight.setSelectedIndex(3);
        comboBoxHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxHeightActionPerformed(evt);
            }
        });

        jLabel4.setText("%");

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("Image:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel5, gridBagConstraints);

        jLabel6.setText("Minecraft:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel6, gridBagConstraints);

        jLabel7.setText("Bottom:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel7, gridBagConstraints);

        spinnerImageLow.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerImageLow.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerImageLowStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerImageLow, gridBagConstraints);

        spinnerWorldLow.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        spinnerWorldLow.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWorldLowStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerWorldLow, gridBagConstraints);

        jLabel8.setText("Water level:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel8, gridBagConstraints);

        labelWorldWaterLevel.setText("62");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(labelWorldWaterLevel, gridBagConstraints);

        spinnerWorldMiddle.setModel(new javax.swing.SpinnerNumberModel(62, 0, 127, 1));
        spinnerWorldMiddle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWorldMiddleStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerWorldMiddle, gridBagConstraints);

        jLabel9.setText("Top:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel9, gridBagConstraints);

        spinnerImageHigh.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerImageHigh.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerImageHighStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerImageHigh, gridBagConstraints);

        spinnerWorldHigh.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerWorldHigh.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWorldHighStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerWorldHigh, gridBagConstraints);

        jLabel2.setText("Height:");

        jLabel10.setText("blocks");

        checkBoxVoid.setText("create Void below image value:");
        checkBoxVoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxVoidActionPerformed(evt);
            }
        });

        spinnerVoidBelow.setModel(new javax.swing.SpinnerNumberModel(1, 1, 255, 1));
        spinnerVoidBelow.setEnabled(false);

        labelWarning.setFont(labelWarning.getFont().deriveFont(labelWarning.getFont().getStyle() | java.awt.Font.BOLD));
        labelWarning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelWarning.setText("Only Minecraft 1.1, with mods!");

        jLabel11.setText("Offset:");
        jLabel11.setToolTipText("The origin of the height map will be at these coordinates in the map");

        spinnerOffsetX.setModel(new javax.swing.SpinnerNumberModel(0, -999999, 999999, 1));
        spinnerOffsetX.setToolTipText("The origin of the height map will be at these coordinates in the map");
        spinnerOffsetX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerOffsetXStateChanged(evt);
            }
        });

        jLabel12.setText(",");
        jLabel12.setToolTipText("The origin of the height map will be at these coordinates in the map");

        spinnerOffsetY.setModel(new javax.swing.SpinnerNumberModel(0, -999999, 999999, 1));
        spinnerOffsetY.setToolTipText("The origin of the height map will be at these coordinates in the map");
        spinnerOffsetY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerOffsetYStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerOffsetX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerOffsetY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(labelWorldDimensions)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(labelWarning))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(checkBoxVoid)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerVoidBelow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel11)
                    .addComponent(spinnerOffsetX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(spinnerOffsetY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelWorldDimensions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(comboBoxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(labelWarning))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxVoid)
                    .addComponent(spinnerVoidBelow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Scaling", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(heightMapTileFactoryEditor1, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(heightMapTileFactoryEditor1, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Terrain", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addComponent(jLabel1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fieldFilename)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectFile))
                    .addComponent(labelImageDimensions)
                    .addComponent(checkBoxInvert)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(buttonOk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelImageDimensions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxInvert)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOk))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void spinnerImageLowStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerImageLowStateChanged
        int lowLevel    = (Integer) spinnerImageLow.getValue();
        int highLevel   = (Integer) spinnerImageHigh.getValue();
        if (lowLevel > highLevel) {
            spinnerImageHigh.setValue(lowLevel);
        }
        updateWorldWaterLevel();
        if ((lowLevel == 0) && checkBoxVoid.isSelected()) {
            checkBoxVoid.setSelected(false);
        }
        checkBoxVoid.setEnabled(lowLevel > 0);
    }//GEN-LAST:event_spinnerImageLowStateChanged

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        dispose();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void spinnerImageHighStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerImageHighStateChanged
        int lowLevel    = (Integer) spinnerImageLow.getValue();
        int highLevel   = (Integer) spinnerImageHigh.getValue();
        if (highLevel < lowLevel) {
            spinnerImageLow.setValue(highLevel);
        }
        updateWorldWaterLevel();
    }//GEN-LAST:event_spinnerImageHighStateChanged

    private void spinnerWorldLowStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWorldLowStateChanged
        int lowLevel    = (Integer) spinnerWorldLow.getValue();
//        int middleLevel = (Integer) spinnerWorldMiddle.getValue();
        int highLevel   = (Integer) spinnerWorldHigh.getValue();
//        if (lowLevel > middleLevel) {
//            spinnerWorldMiddle.setValue(lowLevel);
//        }
        if (lowLevel > highLevel) {
            spinnerWorldHigh.setValue(lowLevel);
        }
        updateWorldWaterLevel();
    }//GEN-LAST:event_spinnerWorldLowStateChanged

    private void spinnerWorldMiddleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWorldMiddleStateChanged
//        int lowLevel    = (Integer) spinnerWorldLow.getValue();
//        int middleLevel = (Integer) spinnerWorldMiddle.getValue();
//        int highLevel   = (Integer) spinnerWorldHigh.getValue();
//        if (middleLevel < lowLevel) {
//            spinnerWorldLow.setValue(middleLevel);
//        }
//        if (middleLevel > highLevel) {
//            spinnerWorldHigh.setValue(middleLevel);
//        }
        updateWorldWaterLevel();
        int waterLevel = ((Number) spinnerWorldMiddle.getValue()).intValue();
        tileFactory.setWaterHeight(waterLevel);
        float baseHeight = tileFactory.getBaseHeight();
        float transposeAmount = (waterLevel - 4) - baseHeight;
        tileFactory.setHeightMap(HeightMapUtils.transposeHeightMap(tileFactory.getHeightMap(), transposeAmount));
        heightMapTileFactoryEditor1.setTileFactory(tileFactory);
    }//GEN-LAST:event_spinnerWorldMiddleStateChanged

    private void spinnerWorldHighStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWorldHighStateChanged
        int lowLevel    = (Integer) spinnerWorldLow.getValue();
//        int middleLevel = (Integer) spinnerWorldMiddle.getValue();
        int highLevel   = (Integer) spinnerWorldHigh.getValue();
        if (highLevel < lowLevel) {
            spinnerWorldLow.setValue(highLevel);
        }
//        if (highLevel < middleLevel) {
//            spinnerWorldMiddle.setValue(highLevel);
//        }
        updateWorldWaterLevel();
    }//GEN-LAST:event_spinnerWorldHighStateChanged

    private void buttonSelectFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectFileActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        final Set<String> extensions = new HashSet<String>(Arrays.asList(ImageIO.getReaderFileSuffixes()));
        StringBuilder sb = new StringBuilder("Supported image formats (");
        boolean first = true;
        for (String extension: extensions) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append("*.");
            sb.append(extension);
        }
        sb.append(')');
        final String description = sb.toString();
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String filename = f.getName();
                int p = filename.lastIndexOf('.');
                if (p != -1) {
                    String extension = filename.substring(p + 1).toLowerCase();
                    return extensions.contains(extension);
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return description;
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fieldFilename.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_buttonSelectFileActionPerformed

    private void spinnerScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerScaleStateChanged
        if (image != null) {
            updateWorldDimensions();
        }
    }//GEN-LAST:event_spinnerScaleStateChanged

    private void buttonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOkActionPerformed
        if ((jTabbedPane1.getSelectedIndex() == 0) || heightMapTileFactoryEditor1.save()) {
            spinnerWorldMiddle.setValue(tileFactory.getWaterHeight());
            cancelled = false;
            dispose();
        }
    }//GEN-LAST:event_buttonOkActionPerformed

    private void comboBoxHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxHeightActionPerformed
        int maxHeight = Integer.parseInt((String) comboBoxHeight.getSelectedItem());
        if (maxHeight != previousMaxHeight) {
            if ((Integer) spinnerWorldLow.getValue() >= maxHeight) {
                spinnerWorldLow.setValue(maxHeight - 1);
            }
            if ((Integer) spinnerWorldMiddle.getValue() >= maxHeight) {
                spinnerWorldMiddle.setValue(maxHeight - 1);
            }
            if ((Integer) spinnerWorldHigh.getValue() >= maxHeight) {
                spinnerWorldHigh.setValue(maxHeight - 1);
            }
            if ((Integer) spinnerVoidBelow.getValue() >= maxHeight) {
                spinnerVoidBelow.setValue(maxHeight - 1);
            }
            ((SpinnerNumberModel) spinnerWorldLow.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerWorldMiddle.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerWorldHigh.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerVoidBelow.getModel()).setMaximum(maxHeight - 1);
            
            HeightTransform transform = HeightTransform.get(maxHeight * 100 / previousMaxHeight, 0);
            tileFactory.setMaxHeight(maxHeight, transform);
            int waterLevel = ((Number) spinnerWorldMiddle.getValue()).intValue();
            tileFactory.setWaterHeight(waterLevel);
            float baseHeight = tileFactory.getBaseHeight();
            float transposeAmount = Math.max(waterLevel - 4, 0) - baseHeight;
            tileFactory.setHeightMap(HeightMapUtils.transposeHeightMap(tileFactory.getHeightMap(), transposeAmount));
            heightMapTileFactoryEditor1.setTileFactory(tileFactory);
            labelWarning.setVisible(maxHeight != DEFAULT_MAX_HEIGHT_2);
            
            previousMaxHeight = maxHeight;
        }
    }//GEN-LAST:event_comboBoxHeightActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        switch (jTabbedPane1.getSelectedIndex()) {
            case 0:
                heightMapTileFactoryEditor1.save();
                spinnerWorldMiddle.setValue(tileFactory.getWaterHeight());
                break;
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void checkBoxVoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxVoidActionPerformed
        spinnerVoidBelow.setEnabled(checkBoxVoid.isSelected());
    }//GEN-LAST:event_checkBoxVoidActionPerformed

    private void spinnerOffsetXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerOffsetXStateChanged
        if (image != null) {
            updateWorldDimensions();
        }
    }//GEN-LAST:event_spinnerOffsetXStateChanged

    private void spinnerOffsetYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerOffsetYStateChanged
        if (image != null) {
            updateWorldDimensions();
        }
    }//GEN-LAST:event_spinnerOffsetYStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOk;
    private javax.swing.JButton buttonSelectFile;
    private javax.swing.JCheckBox checkBoxInvert;
    private javax.swing.JCheckBox checkBoxVoid;
    private javax.swing.JComboBox comboBoxHeight;
    private javax.swing.JTextField fieldFilename;
    private org.pepsoft.worldpainter.terrainRanges.HeightMapTileFactoryEditor heightMapTileFactoryEditor1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelImageDimensions;
    private javax.swing.JLabel labelWarning;
    private javax.swing.JLabel labelWorldDimensions;
    private javax.swing.JLabel labelWorldWaterLevel;
    private javax.swing.JSpinner spinnerImageHigh;
    private javax.swing.JSpinner spinnerImageLow;
    private javax.swing.JSpinner spinnerOffsetX;
    private javax.swing.JSpinner spinnerOffsetY;
    private javax.swing.JSpinner spinnerScale;
    private javax.swing.JSpinner spinnerVoidBelow;
    private javax.swing.JSpinner spinnerWorldHigh;
    private javax.swing.JSpinner spinnerWorldLow;
    private javax.swing.JSpinner spinnerWorldMiddle;
    // End of variables declaration//GEN-END:variables

    private File selectedFile;
    private volatile BufferedImage image;
    private boolean cancelled = true;
    private HeightMapTileFactory tileFactory;
    private int previousMaxHeight = DEFAULT_MAX_HEIGHT_2;
    
    private static final Logger logger = Logger.getLogger(ImportHeightMapDialog.class.getName());
    private static final long serialVersionUID = 1L;
}