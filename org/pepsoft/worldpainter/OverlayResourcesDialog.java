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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.pepsoft.minecraft.Level;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressComponent.Listener;
import org.pepsoft.util.swing.ProgressTask;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.merging.WorldMerger;
import org.pepsoft.worldpainter.util.FileInUseException;
import org.pepsoft.worldpainter.util.MinecraftUtil;

/**
 *
 * @author pepijn
 */
// TODO: add support for multiple dimensions
public class OverlayResourcesDialog extends javax.swing.JDialog implements Listener {
    WorldPainter view;
    /** Creates new form ExportWorldDialog */
    public OverlayResourcesDialog(java.awt.Frame parent, WorldPainter inView) {
        super(parent, true);
        selectedDimension = DIM_NORMAL;
        view = inView;
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

        rootPane.setDefaultButton(jButton_OverlayResources_Run);

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

    private void processOverlaySelector() {
        String selectedProcess = this.jList_OverlayResources_Operations.getSelectedValue().toString();
        switch(selectedProcess) {
            case "Raise or Lower":
                processRaiseLower();
                break;
            case "Roads":
                processOverlayRoads();
                break;
            case "Water":
                processOverlayRivers();
                break;
            case "Landuse":
                processLanduse();
                break;
            case "Color Landuse":
                break;
            case "Colorize":
                break;
            default:
        }
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

    private void processRaiseLower() {
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
                    OverlayProcessor theOP = new OverlayProcessor(view);
                    theOP.RaiseLowerTerrain(
                        null,  
                        (int)jSpinner_OverlayResources_RaiseLowerTerrain.getValue(), 
                        progressReceiver);
                        
                    // DID THE WORK
                    if (theOP.getWarnings() != null) {
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
    
    private void processLanduse() {
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
                try {
                    // DOES THE WORK
                    OverlayProcessor theOP = new OverlayProcessor(view);
                    theOP.SetLanduse(
                        null,
                        jList_OverlayResources_Landuse.getSelectedValue().toString(),
                        progressReceiver);
                    // DID THE WORK
                    if (theOP.getWarnings() != null) {
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
    
    private void processOverlayRoads() {
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
                try {
                    // DOES THE WORK
                    OverlayProcessor theOP = new OverlayProcessor(view);
                    theOP.Roadwork(
                        null,
                        (int)jSpinner_OverlayResources_RaiseLowerTerrain.getValue(),
                        (int)jSpinner_OverlayResources_NewTerrainThickness.getValue(),
                        jList_OverlayResources_Landuse.getSelectedValue().toString(),
                        progressReceiver);
                    // DID THE WORK
                    if (theOP.getWarnings() != null) {
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

    private void processOverlayRivers() {
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
                try {
                    // DOES THE WORK
                    OverlayProcessor theOP = new OverlayProcessor(view);
                    theOP.WaterWorks(
                        null,
                        (int)jSpinner_OverlayResources_RaiseLowerTerrain.getValue(),
                        (int)jSpinner_OverlayResources_NewTerrainThickness.getValue(),
                        progressReceiver);
                    // DID THE WORK
                    if (theOP.getWarnings() != null) {
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
        jButton_OverlayResources_Run = new javax.swing.JButton();
        jTextField_OverlayResources_PNGPath = new javax.swing.JTextField();
        jButton_OverlayResources_SelectPNG = new javax.swing.JButton();
        progressComponent1 = new org.pepsoft.util.swing.ProgressComponent();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jSpinner_OverlayResources_RaiseLowerTerrain = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jSpinner_OverlayResources_NewTerrainThickness = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList_OverlayResources_Operations = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList_OverlayResources_Landuse = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        luStrengthSpinner = new javax.swing.JSpinner();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Overlay Resources");
        setMaximumSize(new java.awt.Dimension(500, 370));
        setMinimumSize(new java.awt.Dimension(500, 370));
        setPreferredSize(new java.awt.Dimension(500, 370));

        jLabel_OverlayResources_Title.setText("Future load image here. Now uses View Overlay image...");

        jButton_OverlayResources_Run.setText("Process World!");
        jButton_OverlayResources_Run.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OverlayResources_RunActionPerformed(evt);
            }
        });

        jTextField_OverlayResources_PNGPath.setText("Ignore This");
        jTextField_OverlayResources_PNGPath.setEnabled(false);

        jButton_OverlayResources_SelectPNG.setText("...");
        jButton_OverlayResources_SelectPNG.setEnabled(false);
        jButton_OverlayResources_SelectPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OverlayResources_SelectPNGActionPerformed(evt);
            }
        });

        jLabel1.setText("Choose How To Use The Mask...");

        jLabel4.setText("Raise or Lower Terrain?");

        jSpinner_OverlayResources_RaiseLowerTerrain.setModel(new javax.swing.SpinnerNumberModel(0, -255, 255, 1));
        jSpinner_OverlayResources_RaiseLowerTerrain.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner_OverlayResources_RaiseLowerTerrain, ""));

        jLabel2.setText("Positive Numbers Adds Levels");

        jLabel3.setText("Negative Numbers Subtracts Levels");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSpinner_OverlayResources_RaiseLowerTerrain, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3))
                    .addComponent(jSpinner_OverlayResources_RaiseLowerTerrain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSpinner_OverlayResources_NewTerrainThickness.setModel(new javax.swing.SpinnerNumberModel(0, -255, 255, 1));

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(new java.awt.Color(212, 208, 200));
        jTextPane1.setBorder(null);
        jTextPane1.setText("Number of blocks deep to insert new terrain. A negative value indicates stacking new terrain on top of existing terrain, not replacing anything.");
        jTextPane1.setAutoscrolls(false);
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSpinner_OverlayResources_NewTerrainThickness, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinner_OverlayResources_NewTerrainThickness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jList_OverlayResources_Operations.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Raise or Lower", "Roads", "Landuse", "Water", "--not working--", "Colorize" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList_OverlayResources_Operations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList_OverlayResources_Operations.setToolTipText("What operation to perform?");
        jList_OverlayResources_Operations.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList_OverlayResources_OperationsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList_OverlayResources_Operations);

        jList_OverlayResources_Landuse.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "--Roads--", "Paved", "Gravel", "Primary", "Secondary", "Cobble", "--Land Types--", "Deciduous", "Pine", "Swamp", "Grassland", "Frozen Deciduous", "Frozen Pine", "Frozen Swamp", "Ice Plains", "--Colors--", "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList_OverlayResources_Landuse.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(jList_OverlayResources_Landuse);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Landuse");

        luStrengthSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(100.0f), Float.valueOf(1.0f), Float.valueOf(100.0f), Float.valueOf(1.0f)));
        luStrengthSpinner.setMinimumSize(new java.awt.Dimension(52, 18));

        jScrollPane5.setBorder(null);

        jTextPane2.setEditable(false);
        jTextPane2.setBackground(new java.awt.Color(212, 208, 200));
        jTextPane2.setBorder(null);
        jTextPane2.setText("Landuse/Landcover application 'strength'");
        jTextPane2.setToolTipText("");
        jTextPane2.setPreferredSize(new java.awt.Dimension(263, 42));
        jScrollPane5.setViewportView(jTextPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_OverlayResources_Title, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_OverlayResources_PNGPath, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(progressComponent1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jButton_OverlayResources_Run, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(luStrengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton_OverlayResources_SelectPNG)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 104, Short.MAX_VALUE))
                            .addComponent(jScrollPane2)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_OverlayResources_Title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_OverlayResources_PNGPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_OverlayResources_SelectPNG))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(luStrengthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 132, Short.MAX_VALUE)
                        .addComponent(jButton_OverlayResources_Run)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressComponent1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_OverlayResources_RunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OverlayResources_RunActionPerformed
        processOverlaySelector();
    }//GEN-LAST:event_jButton_OverlayResources_RunActionPerformed

    private void jButton_OverlayResources_SelectPNGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OverlayResources_SelectPNGActionPerformed
        selectImageFile();
    }//GEN-LAST:event_jButton_OverlayResources_SelectPNGActionPerformed

    private void jList_OverlayResources_OperationsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList_OverlayResources_OperationsValueChanged
        String selectedProcess = this.jList_OverlayResources_Operations.getSelectedValue().toString();
        switch(selectedProcess) {
            case "Raise or Lower":
                jSpinner_OverlayResources_NewTerrainThickness.setEnabled(false);
                luStrengthSpinner.setEnabled(false);
                jList_OverlayResources_Landuse.setEnabled(false);
                jSpinner_OverlayResources_RaiseLowerTerrain.setEnabled(true);
                jList_OverlayResources_Landuse.setModel(new javax.swing.AbstractListModel() {
                    String[] strings = { "-- N/A --", };
                    public int getSize() { return strings.length; }
                    public Object getElementAt(int i) { return strings[i]; }
                    });
                break;
            case "Roads":
                jSpinner_OverlayResources_NewTerrainThickness.setEnabled(true);
                luStrengthSpinner.setEnabled(false);
                jList_OverlayResources_Landuse.setEnabled(true);
                jSpinner_OverlayResources_RaiseLowerTerrain.setEnabled(true);
                jList_OverlayResources_Landuse.setModel(new javax.swing.AbstractListModel() {
                    String[] strings = { "Paved", "Gravel", "Primary", "Secondary", "Cobble"};
                    public int getSize() { return strings.length; }
                    public Object getElementAt(int i) { return strings[i]; }
                    });
                break;
            case "Water":
                jSpinner_OverlayResources_NewTerrainThickness.setEnabled(true);
                luStrengthSpinner.setEnabled(false);
                jList_OverlayResources_Landuse.setEnabled(false);
                jSpinner_OverlayResources_RaiseLowerTerrain.setEnabled(true);
                jList_OverlayResources_Landuse.setModel(new javax.swing.AbstractListModel() {
                    String[] strings = { "-- N/A --"};
                    public int getSize() { return strings.length; }
                    public Object getElementAt(int i) { return strings[i]; }
                    });
                break;
            case "Landuse":
                jSpinner_OverlayResources_NewTerrainThickness.setEnabled(false);
                luStrengthSpinner.setEnabled(true);
                jList_OverlayResources_Landuse.setEnabled(true);
                jSpinner_OverlayResources_RaiseLowerTerrain.setEnabled(false);
                jList_OverlayResources_Landuse.setModel(new javax.swing.AbstractListModel() {
                    String[] strings = { "Deciduous", "Pine", "Swamp", "Grassland", "Frozen Deciduous", "Frozen Pine", "Frozen Swamp", "Ice Plains"};
                    public int getSize() { return strings.length; }
                    public Object getElementAt(int i) { return strings[i]; }
                    });
                break;
            default:
        }
    }//GEN-LAST:event_jList_OverlayResources_OperationsValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton_OverlayResources_Run;
    private javax.swing.JButton jButton_OverlayResources_SelectPNG;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel_OverlayResources_Title;
    private javax.swing.JList jList_OverlayResources_Landuse;
    private javax.swing.JList jList_OverlayResources_Operations;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSpinner jSpinner_OverlayResources_NewTerrainThickness;
    private javax.swing.JSpinner jSpinner_OverlayResources_RaiseLowerTerrain;
    private javax.swing.JTextField jTextField_OverlayResources_PNGPath;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JSpinner luStrengthSpinner;
    private org.pepsoft.util.swing.ProgressComponent progressComponent1;
    // End of variables declaration//GEN-END:variables


    private volatile boolean cancelled;
    private long start;
    private int selectedDimension;

    private static final long serialVersionUID = 1L;

    }