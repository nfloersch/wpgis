/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.docking.DockContext;
import static com.jidesoft.docking.DockContext.*;
import com.jidesoft.docking.DockableFrame;
import static com.jidesoft.docking.DockableFrame.*;
import com.jidesoft.docking.DockableHolder;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.docking.Workspace;
import com.jidesoft.swing.JideTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import static java.awt.event.KeyEvent.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageOp;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
//import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jetbrains.annotations.NonNls;
import org.pepsoft.minecraft.Direction;
import org.pepsoft.minecraft.Material;
import org.pepsoft.util.WPCustomObjectInputStream;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.FileUtils;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.PluginManager;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.SystemUtils;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.util.undo.UndoManager;
import org.pepsoft.worldpainter.biomeschemes.AutoBiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import org.pepsoft.worldpainter.colourschemes.DynMapColourScheme;
import org.pepsoft.worldpainter.gardenofeden.GardenOfEdenOperation;
import org.pepsoft.worldpainter.importing.MapImportDialog;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.Bo2Layer;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.CustomLayerDialog;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import org.pepsoft.worldpainter.layers.GardenCategory;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.LayerManager;
import org.pepsoft.worldpainter.layers.Populate;
import org.pepsoft.worldpainter.layers.ReadOnly;
import org.pepsoft.worldpainter.layers.Resources;
import org.pepsoft.worldpainter.layers.bo2.CustomObjectDialog;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.layers.exporters.ResourcesExporter.ResourcesExporterSettings;
import org.pepsoft.worldpainter.layers.groundcover.GroundCoverDialog;
import org.pepsoft.worldpainter.layers.groundcover.GroundCoverLayer;
import org.pepsoft.worldpainter.layers.pockets.UndergroundPocketsDialog;
import org.pepsoft.worldpainter.layers.pockets.UndergroundPocketsLayer;
import org.pepsoft.worldpainter.operations.BiomePaint;
import org.pepsoft.worldpainter.operations.BitmapBrush;
import org.pepsoft.worldpainter.operations.Brush;
import org.pepsoft.worldpainter.operations.CustomTerrainPaint;
import org.pepsoft.worldpainter.operations.Filter;
import org.pepsoft.worldpainter.operations.Flatten;
import org.pepsoft.worldpainter.operations.Flood;
import org.pepsoft.worldpainter.operations.Height;
import org.pepsoft.worldpainter.operations.LayerPaint;
import org.pepsoft.worldpainter.operations.MouseOrTabletOperation;
import org.pepsoft.worldpainter.operations.Operation;
import org.pepsoft.worldpainter.operations.OperationManager;
import org.pepsoft.worldpainter.operations.RadiusOperation;
import org.pepsoft.worldpainter.operations.RaiseMountain;
import org.pepsoft.worldpainter.operations.RaisePyramid;
import org.pepsoft.worldpainter.operations.RaiseRotatedPyramid;
import org.pepsoft.worldpainter.operations.SetSpawnPoint;
import org.pepsoft.worldpainter.operations.Smooth;
import org.pepsoft.worldpainter.operations.Sponge;
import org.pepsoft.worldpainter.operations.SymmetricBrush;
import org.pepsoft.worldpainter.operations.TerrainOperation;
import org.pepsoft.worldpainter.operations.TerrainPaint;
import org.pepsoft.worldpainter.panels.BrushOptions;
import org.pepsoft.worldpainter.panels.BrushOptions.Listener;
import org.pepsoft.worldpainter.threedeeview.ThreeDeeFrame;
import org.pepsoft.worldpainter.tools.BiomesViewerFrame;
import org.pepsoft.worldpainter.tools.RespawnPlayerDialog;
import org.pepsoft.worldpainter.util.BetterAction;
import org.pepsoft.worldpainter.vo.AttributeKeyVO;
import org.pepsoft.worldpainter.vo.EventVO;
import org.pepsoft.worldpainter.vo.UsageVO;

import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.biomeschemes.CustomBiome;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager.CustomBiomeListener;
import org.pepsoft.worldpainter.layers.Annotations;
import org.pepsoft.worldpainter.layers.CombinedLayer;
import org.pepsoft.worldpainter.layers.LayerContainer;
import org.pepsoft.worldpainter.layers.combined.CombinedLayerDialog;
import org.pepsoft.worldpainter.layers.tunnel.TunnelLayerDialog;
import org.pepsoft.worldpainter.layers.tunnel.TunnelLayer;
import org.pepsoft.worldpainter.objects.AbstractObject;
import org.pepsoft.worldpainter.operations.Annotate;
import org.pepsoft.worldpainter.operations.CombinedLayerPaint;
import org.pepsoft.worldpainter.operations.RotatedBrush;

/**
 *
 * @author pepijn
 */
public final class App extends JFrame implements RadiusControl,
        BiomesViewerFrame.SeedListener, Listener, CustomBiomeListener,
        PaletteManager.ButtonProvider, DockableHolder {
    private App() {
        super((mode == Mode.WORLDPAINTER) ? "WorldPainter" : "MinecraftMapEditor"); // NOI18N
        setIconImage(ICON);

        colourSchemes = new ColourScheme[] {
            new DynMapColourScheme("default", true),
            new DynMapColourScheme("flames", true),
            new DynMapColourScheme("ovocean", true),
            new DynMapColourScheme("sk89q", true),
            new DynMapColourScheme("dokudark", true),
            new DynMapColourScheme("dokuhigh", true),
            new DynMapColourScheme("dokulight", true),
            new DynMapColourScheme("misa", true),
            new DynMapColourScheme("sphax", true)
        };
        Configuration config = Configuration.getInstance();
        selectedColourScheme = colourSchemes[config.getColourschemeIndex()];
        operations = OperationManager.getInstance().getOperations();
//        toolbarsLocked = config.isToolbarsLocked();
        setMaxRadius(config.getMaximumBrushSize());

        loadCustomBrushes();
        
        brushOptions = new BrushOptions();
        brushOptions.setListener(this);
        
        initComponents();
        
        hiddenLayers.add(Biome.INSTANCE);
        view.addHiddenLayer(Biome.INSTANCE);
        
        // Initialize various things
        customBiomeManager.addListener(this);
        
        int biomeCount = autoBiomeScheme.getBiomeCount();
        for (int i = 0; i < biomeCount; i++) {
            if (autoBiomeScheme.isBiomePresent(i)) {
                biomeNames[i] = autoBiomeScheme.getBiomeName(i) + " (" + i + ")";
            }
        }
        
        String sizeStr = System.getProperty("org.pepsoft.worldpainter.size");
        if (sizeStr != null) {
            String[] dims = sizeStr.split("x");
            int width = Integer.parseInt(dims[0]);
            int height = Integer.parseInt(dims[1]);
            setSize(width, height);
            setLocationRelativeTo(null);
        } else if (config.getWindowBounds() != null) {
            setBounds(config.getWindowBounds());
        } else {
            setSize(1024, 896);
            setLocationRelativeTo(null);
        }
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

//        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
//            @Override
//            public void eventDispatched(AWTEvent event) {
//                System.out.println(event);
//            }
//        }, AWTEvent.MOUSE_EVENT_MASK);

        // For some look and feels the preferred size of labels isn't set until
        // they are displayed
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                fixLabelSizes();
                maybePing();

                // Show mini map here because we only know our location now
//                JDialog miniMapDialog = new JDialog(App.this, "Mini Map");
//                final MiniMap miniMap = new MiniMap();
//                miniMap.setView(view);
//                miniMapDialog.getContentPane().add(miniMap, BorderLayout.CENTER);
//                miniMapDialog.setSize(300, 300);
//                miniMapDialog.setLocation(getX() + getWidth() - 300, getY());
//                miniMapDialog.setAlwaysOnTop(true);
//                miniMapDialog.addWindowListener(new WindowAdapter() {
//                    @Override
//                    public void windowClosing(WindowEvent e) {
//                        miniMap.setView(null);
//                    }
//                });
//                miniMapDialog.setVisible(true);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (getExtendedState() != Frame.MAXIMIZED_BOTH) {
                    Configuration config = Configuration.getInstance();
                    if (config != null) {
                        config.setWindowBounds(getBounds());
                    }
                }
            }

            @Override
            public void componentResized(ComponentEvent e) {
                if (getExtendedState() != Frame.MAXIMIZED_BOTH) {
                    Configuration config = Configuration.getInstance();
                    if (config != null) {
                        config.setWindowBounds(getBounds());
                    }
                }
            }
        });
        WindowAdapter windowAdapter = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Configuration config = Configuration.getInstance();
                if (config != null) {
                    config.setMaximised(getExtendedState() == Frame.MAXIMIZED_BOTH);
                }
                exit();
            }
        };
        addWindowListener(windowAdapter);

        if (SystemUtils.isMac()) {
            installMacCustomisations();
        }
        
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("rotateLightLeft", ACTION_ROTATE_LIGHT_LEFT);
        actionMap.put("rotateLightRight", ACTION_ROTATE_LIGHT_RIGHT);
        actionMap.put("intensity10", ACTION_INTENSITY_10_PERCENT);
        actionMap.put("intensity20", ACTION_INTENSITY_20_PERCENT);
        actionMap.put("intensity30", ACTION_INTENSITY_30_PERCENT);
        actionMap.put("intensity40", ACTION_INTENSITY_40_PERCENT);
        actionMap.put("intensity50", ACTION_INTENSITY_50_PERCENT);
        actionMap.put("intensity60", ACTION_INTENSITY_60_PERCENT);
        actionMap.put("intensity70", ACTION_INTENSITY_70_PERCENT);
        actionMap.put("intensity80", ACTION_INTENSITY_80_PERCENT);
        actionMap.put("intensity90", ACTION_INTENSITY_90_PERCENT);
        actionMap.put("intensity100", ACTION_INTENSITY_100_PERCENT);

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(ACTION_ROTATE_LIGHT_LEFT.getAcceleratorKey(), "rotateLightLeft");
        inputMap.put(ACTION_ROTATE_LIGHT_RIGHT.getAcceleratorKey(), "rotateLightRight");
        inputMap.put(ACTION_INTENSITY_10_PERCENT.getAcceleratorKey(), "intensity10");
        inputMap.put(ACTION_INTENSITY_20_PERCENT.getAcceleratorKey(), "intensity20");
        inputMap.put(ACTION_INTENSITY_30_PERCENT.getAcceleratorKey(), "intensity30");
        inputMap.put(ACTION_INTENSITY_40_PERCENT.getAcceleratorKey(), "intensity40");
        inputMap.put(ACTION_INTENSITY_50_PERCENT.getAcceleratorKey(), "intensity50");
        inputMap.put(ACTION_INTENSITY_60_PERCENT.getAcceleratorKey(), "intensity60");
        inputMap.put(ACTION_INTENSITY_70_PERCENT.getAcceleratorKey(), "intensity70");
        inputMap.put(ACTION_INTENSITY_80_PERCENT.getAcceleratorKey(), "intensity80");
        inputMap.put(ACTION_INTENSITY_90_PERCENT.getAcceleratorKey(), "intensity90");
        inputMap.put(ACTION_INTENSITY_100_PERCENT.getAcceleratorKey(), "intensity100");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD1, 0), "intensity10");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD2, 0), "intensity20");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD3, 0), "intensity30");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD4, 0), "intensity40");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD5, 0), "intensity50");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD6, 0), "intensity60");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD7, 0), "intensity70");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD8, 0), "intensity80");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD9, 0), "intensity90");
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD0, 0), "intensity100");

        // Log some information about the graphics environment
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode displayMode = graphicsDevice.getDisplayMode();
        ImageCapabilities imageCapabilities = graphicsDevice.getDefaultConfiguration().getImageCapabilities();
        logger.info("Default graphics device, ID string: " + graphicsDevice.getIDstring() + ", available accelerated memory: " + graphicsDevice.getAvailableAcceleratedMemory() + ", display mode: " + displayMode.getWidth() + "x" + displayMode.getHeight() + ", bit depth: " + ((displayMode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI) ? "multi" : displayMode.getBitDepth()) + ", refresh rate: " + ((displayMode.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN) ? "unknown" : displayMode.getRefreshRate()) + ", reported dpi: " + Toolkit.getDefaultToolkit().getScreenResolution() + ", accelerated: " + (imageCapabilities.isAccelerated() ? "yes" : "no") + ", true volatile: " + (imageCapabilities.isTrueVolatile() ? "yes" : "no"));
    }

    public World2 getWorld() {
        return world;
    }

    public void setWorld(World2 world) {
        this.world = world;
        if (world != null) {
            loadCustomMaterials();
            
            extendedBlockIdsMenuItem.setSelected(world.isExtendedBlockIds());
            
            setDimension(world.getDimension(DIM_NORMAL));
            
            Configuration config = Configuration.getInstance();
            if (config.isDefaultViewDistanceEnabled() != view.isDrawViewDistance()) {
                ACTION_VIEW_DISTANCE.actionPerformed(null);
            }
            if (config.isDefaultWalkingDistanceEnabled() != view.isDrawWalkingDistance()) {
                ACTION_WALKING_DISTANCE.actionPerformed(null);
            }
            view.setLightOrigin(config.getDefaultLightOrigin());
            
            brushOptions.setMaxHeight(world.getMaxHeight());

            if (config.isEasyMode()) {
                boolean imported = world.getImportedFrom() != null;
                ACTION_EXPORT_WORLD.setEnabled(! imported);
                ACTION_MERGE_WORLD.setEnabled(imported);
            } else {
                ACTION_EXPORT_WORLD.setEnabled(true);
                ACTION_MERGE_WORLD.setEnabled(true);
            }
        } else {
            setDimension(null);

            ACTION_EXPORT_WORLD.setEnabled(false);
            ACTION_MERGE_WORLD.setEnabled(false);
        }
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(final Dimension dimension) {
        if (this.dimension != null) {
            Point viewPosition = view.getViewCentreInWorldCoords();
            if (viewPosition != null) {
                this.dimension.setLastViewPosition(viewPosition);
            }
            
            this.dimension.unregister();
            undoManager = null;

            // Remove the existing custom object layers
            if (! paletteManager.isEmpty()) {
                boolean visibleLayersChanged = false;
                for (Palette palette: paletteManager.clear()) {
                    for (Layer layer: palette.getLayers()) {
                        if (hiddenLayers.contains(layer)) {
                            hiddenLayers.remove(layer);
                            visibleLayersChanged = true;
                        }
                        if (layer.equals(soloLayer)) {
                            soloLayer = null;
                            visibleLayersChanged = true;
                        }
                    }
                    dockingManager.removeFrame(palette.getDockableFrame().getKey());
                }
                if (visibleLayersChanged) {
                    updateLayerVisibility();
                }
//                validate(); // Doesn't happen automatically for some reason; Swing bug?
//                repaint();
                layerSoloCheckBoxes.clear();
            }
            layersWithNoButton.clear();

            saveCustomBiomes();
        }
        this.dimension = dimension;
        if (dimension != null) {
            setTitle("WorldPainter - " + world.getName() + " - " + dimension.getName()); // NOI18N
            switch (dimension.getDim()) {
                case DIM_NORMAL:
                    viewSurfaceMenuItem.setSelected(true);
                    viewNetherMenuItem.setSelected(false);
                    viewEndMenuItem.setSelected(false);
                    break;
                case DIM_NETHER:
                    viewSurfaceMenuItem.setSelected(false);
                    viewNetherMenuItem.setSelected(true);
                    viewEndMenuItem.setSelected(false);
                    break;
                case DIM_END:
                    viewSurfaceMenuItem.setSelected(false);
                    viewNetherMenuItem.setSelected(false);
                    viewEndMenuItem.setSelected(true);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            if (dimension.getTileCount() > 400) {
                BufferedImage image = ProgressDialog.executeTask(this, new ProgressTask<BufferedImage>() {
                    @Override
                    public String getName() {
                        return strings.getString("creating.world.image");
                    }

                    @Override
                    public BufferedImage execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                        return view.paintImage(dimension, progressReceiver);
                    }
                }, false);
                view.setDimension(dimension, image);
            } else {
                view.setDimension(dimension);
            }
            view.moveTo(dimension.getLastViewPosition());
            
            setDimensionControlStates();
            Configuration config = Configuration.getInstance();
            if ((! "true".equals(System.getProperty("org.pepsoft.worldpainter.disableUndo"))) && config.isUndoEnabled()) {
                undoManager = new UndoManager(ACTION_UNDO, ACTION_REDO, Math.max(config.getUndoLevels() + 1, 2));
                undoManager.setStopAtClasses(PropertyChangeListener.class, Tile.Listener.class, Biome.class, BetterAction.class);
                dimension.register(undoManager);
                dimension.armSavePoint();
            } else {
                // Still install an undo manager, because some operations depend
                // on one level of undo being available
                undoManager = new UndoManager(2);
                undoManager.setStopAtClasses(PropertyChangeListener.class, Tile.Listener.class, Biome.class, BetterAction.class);
                dimension.register(undoManager);
                ACTION_UNDO.setEnabled(false);
                ACTION_REDO.setEnabled(false);
            }
            if (threeDeeFrame != null) {
                threeDeeFrame.setDimension(dimension);
            }
                
            // Add the custom object layers from the world
            boolean missingTerrainWarningGiven = false;
            for (Layer layer: dimension.getAllLayers(true)) {
                if (layer instanceof CustomLayer) {
                    if (((CustomLayer) layer).isHide()) {
                        layersWithNoButton.add((CustomLayer) layer);
                    } else {
                        registerCustomLayer((CustomLayer) layer, false);
                    }
                    if (layer instanceof CombinedLayer) {
                        if (((CombinedLayer) layer).isMissingTerrainWarning()) {
                            if (! missingTerrainWarningGiven) {
                                JOptionPane.showMessageDialog(this, "The world contains one or more Combined Layer(s) referring to a Custom Terrain\nwhich is not present in this world. The terrain has been reset.", "Missing Custom Terrain", JOptionPane.WARNING_MESSAGE);
                                missingTerrainWarningGiven = true;
                            }
                            ((CombinedLayer) layer).resetMissingTerrainWarning();
                        }
                    }
                }
            }
            
            // Set action states
            ACTION_GRID.setSelected(view.isDrawGrid());
            ACTION_CONTOURS.setSelected(view.isDrawContours());
            ACTION_OVERLAY.setSelected(view.isDrawOverlay());
            
            // Load custom biomes. But first remove any that are now regular
            // biomes
            List<CustomBiome> customBiomes = dimension.getCustomBiomes();
            if (customBiomes != null) {
                for (Iterator<CustomBiome> i = customBiomes.iterator(); i.hasNext(); ) {
                    CustomBiome customBiome = i.next();
                    if (autoBiomeScheme.isBiomePresent(customBiome.getId())) {
                        i.remove();
                    }
                }
                if (customBiomes.isEmpty()) {
                    customBiomes = null;
                }
            }
            customBiomeManager.setCustomBiomes(customBiomes);
        } else {
            view.setDimension(null);
            setTitle("WorldPainter"); // NOI18N

            // Clear action states
            ACTION_GRID.setSelected(false);
            ACTION_CONTOURS.setSelected(false);
            ACTION_OVERLAY.setSelected(false);
            
            // Close the 3D view
            if (threeDeeFrame != null) {
                threeDeeFrame.dispose();
                threeDeeFrame = null;
            }
            
            customBiomeManager.setCustomBiomes(null);
        }
    }

    public void updateStatusBar(int x, int y) {
        locationLabel.setText(MessageFormat.format(strings.getString("location.0.1"), x, y));
        int height = dimension.getIntHeightAt(x, y);
        if (height == -1) {
            // Not on a tile
            heightLabel.setText(MessageFormat.format(strings.getString("height.of.0"), dimension.getMaxHeight() - 1));
            waterLabel.setText(" ");
            materialLabel.setText(strings.getString("material-"));
            biomeLabel.setText(strings.getString("biome-"));
            return;
        }
        if (devMode) {
            heightLabel.setText(MessageFormat.format("Height: {0} ({1}) of {2}", dimension.getHeightAt(x, y), height, dimension.getMaxHeight() - 1));
        } else {
            heightLabel.setText(MessageFormat.format(strings.getString("height.0.of.1"), height, dimension.getMaxHeight() - 1));
        }
        if ((activeOperation instanceof LayerPaint) && (! (activeOperation instanceof BiomePaint))) {
            Layer layer = ((LayerPaint) activeOperation).getLayer();
            if ((layer.getDataSize() == Layer.DataSize.BIT) || (layer.getDataSize() == Layer.DataSize.BIT_PER_CHUNK)) {
                waterLabel.setText(MessageFormat.format(strings.getString("layer.0.on.off"), layer.getName(), (dimension.getBitLayerValueAt(layer, x, y) ? 1 : 0)));
            } else {
                waterLabel.setText(MessageFormat.format(strings.getString("layer.0.level.1"), layer.getName(), dimension.getLayerValueAt(layer, x, y)));
            }
        } else if (activeOperation instanceof GardenOfEdenOperation) {
            switch(dimension.getLayerValueAt(GardenCategory.INSTANCE, x, y)) {
                case GardenCategory.CATEGORY_BUILDING:
                    waterLabel.setText(strings.getString("structure.building"));
                    break;
                case GardenCategory.CATEGORY_FIELD:
                    waterLabel.setText(strings.getString("structure.field"));
                    break;
                case GardenCategory.CATEGORY_ROAD:
                    waterLabel.setText(strings.getString("structure.road"));
                    break;
                case GardenCategory.CATEGORY_STREET_FURNITURE:
                    waterLabel.setText(strings.getString("structure.street.furniture"));
                    break;
                case GardenCategory.CATEGORY_WATER:
                    waterLabel.setText(strings.getString("structure.water"));
                    break;
                default:
                    waterLabel.setText(null);
                    break;
            }
        } else {
            int waterLevel = dimension.getWaterLevelAt(x, y);
            if (waterLevel > height) {
                waterLabel.setText(MessageFormat.format(strings.getString("fluid.level.1.depth.2"), dimension.getBitLayerValueAt(FloodWithLava.INSTANCE, x, y) ? 1 : 0, waterLevel, waterLevel - height));
            } else {
                waterLabel.setText(null);
            }
        }
        Terrain terrain = dimension.getTerrainAt(x, y);
        if (terrain.isCustom()) {
            int index = terrain.getCustomTerrainIndex();
            materialLabel.setText(MessageFormat.format(strings.getString("material.custom.1.0"), Terrain.getCustomMaterial(index), index + 1));
        } else {
            materialLabel.setText(MessageFormat.format(strings.getString("material.0"), terrain.getName()));
        }
        // TODO: apparently this was sometimes invoked at or soon after startup,
        // with biomeNames being null, causing a NPE. How is this possible?
        if (dimension.getDim() == 0) {
            int biome = dimension.getLayerValueAt(Biome.INSTANCE, x, y);
            // TODO: is this too slow?
            if (biome == 255) {
                biome = dimension.getAutoBiome(x, y);
                if (biome != -1) {
                    if (biomeNames[biome] == null) {
                        biomeLabel.setText("Auto biome: biome " + biome);
                    } else {
                        biomeLabel.setText("Auto biome: " + biomeNames[biome]);
                    }
                }
            } else if (biome != -1) {
                if (biomeNames[biome] == null) {
                    biomeLabel.setText(MessageFormat.format(strings.getString("biome.0"), biome));
                } else {
                    biomeLabel.setText(MessageFormat.format(strings.getString("biome.0"), biomeNames[biome]));
                }
            }
        } else {
            biomeLabel.setText(strings.getString("biome-"));
        }
    }

    public Operation getActiveOperation() {
        return activeOperation;
    }

//    public BrushShape getBrushShape() {
//        return brushShape;
//    }

    public Brush getBrush() {
        return brush;
    }

    public Set<Layer> getHiddenLayers() {
        return Collections.unmodifiableSet(hiddenLayers);
    }

    public float getLevel() {
        return level;
    }

    public int getRadius() {
        return radius;
    }

//    public BrushShape getToolBrushShape() {
//        return toolBrushShape;
//    }

    public Brush getToolBrush() {
        return toolBrush;
    }

    public float getToolLevel() {
        return toolLevel;
    }

    public int getZoom() {
        return zoom;
    }

    public final int getMaxRadius() {
        return maxRadius;
    }

    public final void setMaxRadius(int maxRadius) {
        this.maxRadius = maxRadius;
        if (radius > maxRadius) {
            radius = maxRadius;
            if (activeOperation instanceof RadiusOperation) {
                ((RadiusOperation) activeOperation).setRadius(radius);
            }
            view.setRadius(radius);
            radiusLabel.setText(MessageFormat.format(strings.getString("radius.0"), radius));
        }
    }

//    public boolean isToolbarsLocked() {
//        return toolbarsLocked;
//    }
//
//    public void setToolbarsLocked(boolean toolbarsLocked) {
//        if (toolbarsLocked != this.toolbarsLocked) {
//            this.toolbarsLocked = toolbarsLocked;
//            Configuration.getInstance().setToolbarsLocked(toolbarsLocked);
//            for (Component component: getContentPane().getComponents()) {
//                if (component instanceof JToolBar) {
//                    ((JToolBar) component).setFloatable(! toolbarsLocked);
//                }
//            }
//            lockToolbarsMenuItem.setSelected(toolbarsLocked);
//        }
//    }

    public void open(File file, boolean askForConfirmation) {
        if (askForConfirmation && (world != null) && world.isDirty()) {
            int action = JOptionPane.showConfirmDialog(this, strings.getString("there.are.unsaved.changes"));
            if (action == JOptionPane.YES_OPTION) {
                if (! saveAs()) {
                    // User cancelled the save
                    return;
                }
            } else if (action != JOptionPane.NO_OPTION) {
                // User closed the confirmation dialog without making a choice
                return;
            }
        }
        open(file);
    }
    
    public void open(final File file) {
        logger.info("Loading world " + file.getAbsolutePath());
        setWorld(null); // Free up memory of the world and the undo buffer
        final World2 newWorld = ProgressDialog.executeTask(this, new ProgressTask<World2>() {
            @Override
            public String getName() {
                return strings.getString("loading.world");
            }

            @Override
            public World2 execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                try {
                    WPCustomObjectInputStream in = new WPCustomObjectInputStream(new GZIPInputStream(new FileInputStream(file)), PluginManager.getPluginClassLoader(), AbstractObject.class);
                    try {
                        Object object = in.readObject();
                        if (object instanceof World2) {
                            return (World2) object;
                        } else {
                            return migrate(object);
                        }
                    } finally {
                        in.close();
                    }
                } catch (ZipException e) {
                    logger.log(java.util.logging.Level.SEVERE, "ZipException while loading " + file, e);
                    reportDamagedFile();
                    return null;
                } catch (StreamCorruptedException e) {
                    logger.log(java.util.logging.Level.SEVERE, "StreamCorruptedException while loading " + file, e);
                    reportDamagedFile();
                    return null;
                } catch (EOFException e) {
                    logger.log(java.util.logging.Level.SEVERE, "EOFException while loading " + file, e);
                    reportDamagedFile();
                    return null;
                } catch (IOException e) {
                    if (e.getMessage().equals("Not in GZIP format")) {
                        logger.log(java.util.logging.Level.SEVERE, "IOException while loading " + file, e);
                        reportDamagedFile();
                        return null;
                    } else {
                        throw new RuntimeException("I/O error while loading world", e);
                    }
                } catch (ClassNotFoundException e) {
                    logger.log(java.util.logging.Level.SEVERE, "ClassNotFoundException while loading " + file, e);
                    reportMissingPlugins();
                    return null;
                }
            }
            
            private void reportDamagedFile() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(App.this, strings.getString("the.file.is.damaged"), strings.getString("file.damaged"), JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread interrupted while reporting damaged file " + file, e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Invocation target exception while reporting damaged file " + file, e);
                }
            }

            private void reportMissingPlugins() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(App.this, strings.getString("you.don.t.have.the.right.plugins.installed"), strings.getString("missing.plugin.s"), JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread interrupted while reporting damaged file " + file, e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Invocation target exception while reporting damaged file " + file, e);
                }
            }
        }, false);
        if (newWorld == null) {
            // The file was damaged
            return;
        }
        if (! isBackupFile(file)) {
            lastSelectedFile = file;
        } else {
            lastSelectedFile = null;
        }

        // Log an event
        Configuration config = Configuration.getInstance();
        EventVO event = new EventVO(EVENT_KEY_ACTION_OPEN_WORLD).addTimestamp();
        event.setAttribute(ATTRIBUTE_KEY_MAX_HEIGHT, newWorld.getMaxHeight());
        Dimension loadedDimension = newWorld.getDimension(0);
        event.setAttribute(ATTRIBUTE_KEY_TILES, loadedDimension.getTiles().size());
        logLayers(loadedDimension, event, "");
        loadedDimension = newWorld.getDimension(1);
        if (loadedDimension != null) {
            event.setAttribute(ATTRIBUTE_KEY_NETHER_TILES, loadedDimension.getTiles().size());
            logLayers(loadedDimension, event, "nether.");
        }
        loadedDimension = newWorld.getDimension(2);
        if (loadedDimension != null) {
            event.setAttribute(ATTRIBUTE_KEY_END_TILES, loadedDimension.getTiles().size());
            logLayers(loadedDimension, event, "end.");
        }
        if (newWorld.getImportedFrom() != null) {
            event.setAttribute(ATTRIBUTE_KEY_IMPORTED_WORLD, true);
        }
        config.logEvent(event);
        
        Set<World2.Warning> warnings = newWorld.getWarnings();
        if ((warnings != null) && (! warnings.isEmpty())) {
            for (World2.Warning warning: warnings) {
                switch (warning) {
                    case AUTO_BIOMES_DISABLED:
                        if (JOptionPane.showOptionDialog(this, "Automatic Biomes were previously enabled for this world but have been disabled.\nPress More Info for more information, including how to reenable it.", "Automatic Biomes Disabled", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"More Info", "OK"}, "OK") == 0) {
                            try {
                                DesktopUtils.open(new URL("http://www.worldpainter.net/trac/wiki/NewAutomaticBiomes"));
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        break;
                    case AUTO_BIOMES_ENABLED:
                        if (JOptionPane.showOptionDialog(this, "Automatic Biomes were previously disabled for this world but have been enabled.\nPress More Info for more information, including how to disable it.", "Automatic Biomes Enabled", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"More Info", "OK"}, "OK") == 0) {
                            try {
                                DesktopUtils.open(new URL("http://www.worldpainter.net/trac/wiki/NewAutomaticBiomes"));
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        break;
                }
            }
        }
        
        if (newWorld.isAskToConvertToAnvil() && (newWorld.getMaxHeight() == DEFAULT_MAX_HEIGHT_1) && (newWorld.getImportedFrom() == null)) {
            if (JOptionPane.showConfirmDialog(this, strings.getString("this.world.is.128.blocks.high"), strings.getString("convert.world.height"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                ChangeHeightDialog.resizeWorld(newWorld, HeightTransform.IDENTITY, DEFAULT_MAX_HEIGHT_2, this);
                // Force the version to "Anvil" if it was previously exported
                // with the old format
                if (newWorld.getVersion() != 0) {
                    newWorld.setVersion(SUPPORTED_VERSION_2);
                }
                
                // Log event
                config.logEvent(new EventVO(EVENT_KEY_ACTION_MIGRATE_HEIGHT).addTimestamp());
            }
            // Don't ask again, no matter what the user answered
            newWorld.setAskToConvertToAnvil(false);
        }
        
        if (newWorld.isAskToRotate() && (newWorld.getUpIs() == Direction.WEST) && (newWorld.getImportedFrom() == null)) {
            if (JOptionPane.showConfirmDialog(this, strings.getString("this.world.was.created.when.north.was.to.the.right"), strings.getString("rotate.world"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                ProgressDialog.executeTask(this, new ProgressTask<java.lang.Void>() {
                    @Override
                    public String getName() {
                        return strings.getString("rotating.world");
                    }

                    @Override
                    public java.lang.Void execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                        newWorld.rotate(CoordinateTransform.ROTATE_CLOCKWISE_270_DEGREES, progressReceiver);
                        return null;
                    }
                }, false);
                
                // Log event
                config.logEvent(new EventVO(EVENT_KEY_ACTION_MIGRATE_ROTATION).addTimestamp());
            }
            // Don't ask again, no matter what the user answered
            newWorld.setAskToRotate(false);
        }

        // Make sure the world name is always the same as the file name, to
        // avoid confusion, unless the only difference is illegal filename
        // characters changed into underscores. Do this here as well as when
        // saving, because the file might have been renamed
        String name = isBackupFile(file) ? getOriginalFile(file).getName() : file.getName();
        int p = name.lastIndexOf('.');
        if (p != -1) {
            name = name.substring(0, p);
        }
        String worldName = newWorld.getName();
        if (worldName.length() != name.length()) {
            newWorld.setName(name);
        } else {
            for (int i = 0; i < name.length(); i++) {
                if ((name.charAt(i) != '_') && (name.charAt(i) != worldName.charAt(i))) {
                    newWorld.setName(name);
                    break;
                }
            }
        }
        newWorld.setDirty(false);
        setWorld(newWorld);

        if (newWorld.getImportedFrom() != null) {
            enableImportedWorldOperation();
        } else {
            disableImportedWorldOperation();
        }
        setDimensionControlStates();
            
        if ((config.getJideLayoutData() != null) && config.getJideLayoutData().containsKey(world.getName())) {
            dockingManager.loadLayoutFrom(new ByteArrayInputStream(config.getJideLayoutData().get(world.getName())));
        }
    }

    public static Mode getMode() {
        return mode;
    }

    public static void setMode(Mode mode) {
        App.mode = mode;
    }

    // RadiusControl

    @Override
    public void increaseRadius(int amount) {
        int oldRadius = radius;
        if (radius == 0) {
            radius = 1;
        } else {
            double factor = Math.pow(1.1, amount);
            radius = (int) (radius * factor);
            if (radius == oldRadius) {
                radius++;
            }
            if (radius > maxRadius) {
                radius = maxRadius;
            }
            if (radius == oldRadius) {
                return;
            }
        }
        if (activeOperation instanceof RadiusOperation) {
            ((RadiusOperation) activeOperation).setRadius(radius);
        }
        view.setRadius(radius);
        radiusLabel.setText(MessageFormat.format(strings.getString("radius.0"), radius));
    }

    @Override
    public void increaseRadiusByOne() {
        if (radius < maxRadius) {
            radius++;
            if (activeOperation instanceof RadiusOperation) {
                ((RadiusOperation) activeOperation).setRadius(radius);
            }
            view.setRadius(radius);
            radiusLabel.setText(MessageFormat.format(strings.getString("radius.0"), radius));
        }
    }
    
    @Override
    public void decreaseRadius(int amount) {
        if (radius > 0) {
            int oldRadius = radius;
            double factor = Math.pow(0.9, amount);
            radius = (int) (radius * factor);
            if (radius == oldRadius) {
                radius--;
            }
            if (radius < 0) {
                radius = 0;
            }
            if (radius == oldRadius) {
                return;
            }
            if (activeOperation instanceof RadiusOperation) {
                ((RadiusOperation) activeOperation).setRadius(radius);
            }
            view.setRadius(radius);
            radiusLabel.setText(MessageFormat.format(strings.getString("radius.0"), radius));
        }
    }

    @Override
    public void decreaseRadiusByOne() {
        if (radius > 0) {
            radius--;
            if (activeOperation instanceof RadiusOperation) {
                ((RadiusOperation) activeOperation).setRadius(radius);
            }
            view.setRadius(radius);
            radiusLabel.setText(MessageFormat.format(strings.getString("radius.0"), radius));
        }
    }
    
    // SeedListener
    
    @Override
    public void setSeed(long seed, Generator generator) {
        if (world != null) {
            world.setGenerator(generator);
            Dimension dim0 = world.getDimension(DIM_NORMAL);
            if (dim0 != null) {
                dim0.setMinecraftSeed(seed);
            }
        }
    }
    
    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public static App getInstanceIfExists() {
        return instance;
    }
    
    /**
     * Offer to save the current world, but only if is dirty.
     * 
     * @return <code>true</code> if there are no unsaved changes, the user saved
     *     the changes, or the user indicated that unsaved changes may be
     *     discarded (in other words, a destructive operation may proceed),
     *     <code>false</code> if there were unsaved changes and the user did not
     *     save them or indicate that they may be discarded (in other words, a
     *     destructive operation should be cancelled).
     */
    public boolean saveIfNecessary() {
        if ((world != null) && world.isDirty()) {
            int action = JOptionPane.showConfirmDialog(this, (lastSelectedFile != null) ? (MessageFormat.format(strings.getString("there.are.unsaved.changes.do.you.want.to.save.the.world.to.0"), lastSelectedFile.getName())) : strings.getString("there.are.unsaved.changes"));
            if (action == JOptionPane.YES_OPTION) {
                if (! save()) {
                    // The file was not saved for some reason
                    return false;
                }
            } else if (action != JOptionPane.NO_OPTION) {
                // User closed the confirmation dialog without making a choice
                return false;
            }
        }
        return true;
    }
    
    public boolean editCustomMaterial(int customMaterialIndex) {
        MixedMaterial oldMaterial = Terrain.getCustomMaterial(customMaterialIndex);
        CustomMaterialDialog dialog;
        if (oldMaterial == null) {
            MixedMaterial material = MixedMaterial.create(BLK_DIRT);
            dialog = new CustomMaterialDialog(App.this, material, world.isExtendedBlockIds());
        } else {
            dialog = new CustomMaterialDialog(App.this, oldMaterial, world.isExtendedBlockIds());
        }
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            MixedMaterial newMaterial = dialog.getMaterial();
            Terrain.setCustomMaterial(customMaterialIndex, newMaterial);
            customMaterialButtons[customMaterialIndex].setIcon(new ImageIcon(newMaterial.getIcon(selectedColourScheme)));
            customMaterialButtons[customMaterialIndex].setToolTipText(MessageFormat.format(strings.getString("customMaterial.0.right.click.to.change"), newMaterial));
            view.refreshTiles();
            return true;
        }
        return false;
    }

    public void deselectTool() {
        toolButtonGroup.clearSelection();
    }

    /**
     * Gets all currently loaded layers, including custom layers and including
     * hidden ones (from the panel or the view), regardless of whether they are
     * used on the map.
     */
    public Set<Layer> getAllLayers() {
        Set<Layer> allLayers = new HashSet<Layer>(layers);
        allLayers.add(Populate.INSTANCE);
        if (readOnlyToggleButton.isEnabled()) {
            allLayers.add(ReadOnly.INSTANCE);
        }
        allLayers.addAll(getCustomLayers());
        return allLayers;
    }
    
    /**
     * Gets all currently loaded custom layers, including hidden ones (from the
     * panel or the view), regardless of whether they are used on the map.
     */
    public Set<CustomLayer> getCustomLayers() {
        Set<CustomLayer> customLayers = new HashSet<CustomLayer>();
        customLayers.addAll(paletteManager.getLayers());
        customLayers.addAll(layersWithNoButton);
        return customLayers;
    }

    public ColourScheme getColourScheme() {
        return selectedColourScheme;
    }

    public void setFilter(Filter filter) {
        if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
            this.filter = filter;
        } else {
            toolFilter = filter;
        }
        if (activeOperation instanceof RadiusOperation) {
            ((RadiusOperation) activeOperation).setFilter(filter);
        }
    }

    public CustomBiomeManager getCustomBiomeManager() {
        return customBiomeManager;
    }
    
    // BrushOptions.Listener

    @Override
    public void filterChanged(Filter newFilter) {
        setFilter(newFilter);
    }

    // CustomBiomeListener
    
    @Override
    public void customBiomeAdded(CustomBiome customBiome) {
        biomeNames[customBiome.getId()] = customBiome.getName() + " (" + customBiome.getId() + ")";
    }

    @Override
    public void customBiomeChanged(CustomBiome customBiome) {
        biomeNames[customBiome.getId()] = customBiome.getName() + " (" + customBiome.getId() + ")";
    }

    @Override
    public void customBiomeRemoved(CustomBiome customBiome) {
        biomeNames[customBiome.getId()] = null;
    }
    
    // DockableHolder

    @Override
    public DockingManager getDockingManager() {
        return dockingManager;
    }
    
    void exit() {
        if (saveIfNecessary()) {
            System.exit(0);
        }
    }

    ColourScheme getColourScheme(int index) {
        return colourSchemes[index];
    }

    private void loadCustomBrushes() {
        customBrushes = new TreeMap<String, List<Brush>>();
        File brushesDir = new File(Configuration.getConfigDir(), "brushes");
        if (brushesDir.isDirectory()) {
            loadCustomBrushes(CUSTOM_BRUSHES_DEFAULT_TITLE, brushesDir);
        }
    }
    
    private void loadCustomBrushes(String category, File brushesDir) {
        File[] files = brushesDir.listFiles(new java.io.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                String name = pathname.getName();
                for (String extension: extensions) {
                    if (name.toLowerCase().endsWith(extension)) {
                        return true;
                    }
                }
                return false;
            }
            
            private final String[] extensions = ImageIO.getReaderFileSuffixes();
        });
        List<Brush> brushes = new ArrayList<Brush>();
        for (File file: files) {
            if (file.isDirectory()) {
                loadCustomBrushes(file.getName(), file);
            } else {
                brushes.add(new BitmapBrush(file));
            }
        }
        if (! brushes.isEmpty()) {
            customBrushes.put(category, brushes);
        }
    }
    
    private void maybePing() {
        Configuration config = Configuration.getInstance();
        if (config.getPingAllowed() == null) {
            int rc = JOptionPane.showConfirmDialog(this, strings.getString("may.we.have.your.permission"), strings.getString("usage.statistics.permission"), JOptionPane.YES_NO_OPTION);
            if (rc == JOptionPane.YES_OPTION) {
                config.setPingAllowed(Boolean.TRUE);
            } else if (rc == JOptionPane.NO_OPTION) {
                config.setPingAllowed(Boolean.FALSE);
            } else {
                // User closed the dialog without making a choice. Ask again
                // next time.
                return;
            }
        }
        if (config.getPingAllowed()) {
            ping();
        }
    }
    
    private void ping() {
        final UsageVO usageVO = new UsageVO();
        Configuration config = Configuration.getInstance();
        usageVO.setInstall(config.getUuid());
        usageVO.setLaunchCount(config.getLaunchCount());
        List<EventVO> eventLog = config.getEventLog();
        if ((eventLog != null) && (! eventLog.isEmpty())) {
            usageVO.setEvents(eventLog);
        }
        usageVO.setWPVersion(Version.VERSION);
        new Thread("Pinger") {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://bo.worldpainter.net:1443/ping");
//                    URL url = new URL("http://localhost:8080/ping");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setAllowUserInteraction(false);
                    connection.setRequestProperty("Content-Type", "application/octet-stream");
                    ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(connection.getOutputStream()));
                    try {
                        out.writeObject(usageVO);
                    } finally {
                        out.close();
                    }
                    if (connection.getResponseCode() != 200) {
                        logger.log(java.util.logging.Level.SEVERE, "Server returned response code " + connection.getResponseCode() + " (message: " + connection.getResponseMessage() + ") when trying to submit usage data");
                    } else {
                        InputStreamReader in = new InputStreamReader(connection.getInputStream(), "US-ASCII");
                        try {
                            char[] buffer = new char[2];
                            int bytesToRead = buffer.length;
                            while (bytesToRead > 0) {
                                bytesToRead -= in.read(buffer, buffer.length - bytesToRead, bytesToRead);
                            }
                            if ((buffer[0] != 'O') || (buffer[1] != 'K')) {
                                logger.log(java.util.logging.Level.SEVERE, "Server returned response other than \"OK\" when trying to submit usage data");
                            } else {
                                Configuration.getInstance().clearStatistics();
                                logger.info("Submitted usage data to WorldPainter HQ");
                            }
                        } finally {
                            in.close();
                        }
                    }
                } catch (MalformedURLException e) {
                    logger.log(java.util.logging.Level.SEVERE, "Malformed URL while trying to submit usage data", e);
                } catch (IOException e) {
                    logger.log(java.util.logging.Level.SEVERE, "I/O error while trying to submit usage data", e);
                }
            }
        }.start();
    }

    private void newWorld() {
        if (! saveIfNecessary()) {
            return;
        }
        final NewWorldDialog dialog = new NewWorldDialog(this, strings.getString("generated.world"), World2.DEFAULT_OCEAN_SEED, DIM_NORMAL, Configuration.getInstance().getDefaultMaxHeight());
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            setWorld(null); // Free up memory of the world and the undo buffer
            if (! dialog.checkMemoryRequirements(this)) {
                return;
            }
            World2 newWorld = ProgressDialog.executeTask(this, new ProgressTask<World2>() {
                @Override
                public String getName() {
                    return strings.getString("creating.new.world");
                }

                @Override
                public World2 execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                    return dialog.getSelectedWorld(progressReceiver);
                }
            }, false);

            // Log an event
            Configuration config = Configuration.getInstance();
            if (config != null) {
                EventVO event = new EventVO(EVENT_KEY_ACTION_NEW_WORLD).addTimestamp();
                event.setAttribute(ATTRIBUTE_KEY_MAX_HEIGHT, newWorld.getMaxHeight());
                event.setAttribute(ATTRIBUTE_KEY_TILES, newWorld.getDimension(0).getTiles().size());
                config.logEvent(event);
            }
            
            setWorld(newWorld);
            lastSelectedFile = null;
            disableImportedWorldOperation();
            setDimensionControlStates();
        }
    }

    private void open() {
        if (! saveIfNecessary()) {
            return;
        }
        File dir;
        Configuration config = Configuration.getInstance();
        if (lastSelectedFile != null) {
            dir = lastSelectedFile.getParentFile();
        } else if ((config != null) && (config.getWorldDirectory() != null)) {
            dir = config.getWorldDirectory();
        } else {
            dir = DesktopUtils.getDocumentsFolder();
        }
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory()
                    || f.getName().toLowerCase().endsWith(".world");
            }

            @Override
            public String getDescription() {
                return strings.getString("worldpainter.files.world");
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(App.this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (! file.isFile()) {
                JOptionPane.showMessageDialog(this, "The specified path does not exist or is not a file", "File Does Not Exist", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (! file.canRead()) {
                JOptionPane.showMessageDialog(this, "WorldPainter is not authorised to read the selected file", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            open(file);
            if (config != null) {
                config.setWorldDirectory(file.getParentFile());
            }
        }
    }
    
    /**
     * If the world was loaded from an existing file, and/or was previously
     * saved, save the world to the same file, without asking for confirmation.
     * Otherwise do the same thing as {@link #saveAs()}. Shows a progress
     * indicator while saving.
     * 
     * @return <code>true</code> if the file was saved.
     */
    private boolean save() {
        if (lastSelectedFile == null) {
            return saveAs();
        } else {
            return save(lastSelectedFile);
        }
    }
    
    /**
     * Ask for a filename and save the world with that name. If a file exists
     * with the name, ask for confirmation to overwrite it. Shows a progress
     * indicator while saving, and a confirmation when it is saved.
     * 
     * @return <code>true</code> if the file was saved.
     */
    private boolean saveAs() {
        Configuration config = Configuration.getInstance();
        File file = lastSelectedFile;
        if (file == null) {
            if ((config != null) && (config.getWorldDirectory() != null)) {
                file = new File(config.getWorldDirectory(), FileUtils.sanitiseName(world.getName().trim() + ".world"));
            } else {
                file = new File(DesktopUtils.getDocumentsFolder(), FileUtils.sanitiseName(world.getName().trim() + ".world"));
            }
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(file);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory()
                    || f.getName().toLowerCase().endsWith(".world");
            }

            @Override
            public String getDescription() {
                return strings.getString("worldpainter.files.world");
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showSaveDialog(App.this) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            if (! file.getName().toLowerCase().endsWith(".world")) {
                file = new File(file.getParentFile(), file.getName() + ".world");
            }
            if (file.exists() && (JOptionPane.showConfirmDialog(App.this, strings.getString("do.you.want.to.overwrite.the.file"), strings.getString("file.exists"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)) {
                return false;
            }

            if (save(file)) {
                JOptionPane.showMessageDialog(App.this, strings.getString("file.saved"), strings.getString("success"), JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Save the world to the specified file, overwriting it if it already exists
     * without asking for confirmation. Shows a progress indicator while saving.
     * 
     * @param file The file to which to save the world.
     */
    private boolean save(File file) {
        // Check for write access to directory
        if (! file.getParentFile().isDirectory()) {
            JOptionPane.showMessageDialog(this, strings.getString("the.selected.path.does.not.exist"), strings.getString("non.existant.path"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (! file.getParentFile().canWrite()) {
            JOptionPane.showMessageDialog(this, strings.getString("you.do.not.have.write.access"), strings.getString("access.denied"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Normalise the filename
        String name = file.getName();
        name = name.trim();
        if (name.isEmpty()) {
            name = strings.getString("generated.world") + ".world"; // NOI18N
        } else {
            name = FileUtils.sanitiseName(name);
        }
        final File normalisedFile = new File(file.getParentFile(), name);
        
        // Make sure the world name is always the same as the file name, to
        // avoid confusion (unless the only difference is illegal filename
        // characters changed into underscores
        int p = name.lastIndexOf('.');
        if (p != -1) {
            name = name.substring(0, p).trim();
        }
        String worldName = world.getName();
        if (worldName.length() != name.length()) {
            world.setName(name);
            setTitle("WorldPainter - " + name + " - " + dimension.getName()); // NOI18N
        } else {
            for (int i = 0; i < name.length(); i++) {
                if ((name.charAt(i) != '_') && (name.charAt(i) != worldName.charAt(i))) {
                    world.setName(name);
                    setTitle("WorldPainter - " + name + " - " + dimension.getName()); // NOI18N
                    break;
                }
            }
        }

        logger.info("Saving world " + world.getName() + " to "+ file.getAbsolutePath());

        saveCustomMaterials();
        
        saveCustomBiomes();
        
        if (dimension != null) {
            Point viewPosition = view.getViewCentreInWorldCoords();
            if (viewPosition != null) {
                this.dimension.setLastViewPosition(viewPosition);
            }
        }

        ProgressDialog.executeTask(this, new ProgressTask<java.lang.Void>() {
            @Override
            public String getName() {
                return strings.getString("saving.world");
            }

            @Override
            public java.lang.Void execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                try {
                    Configuration config = Configuration.getInstance();
                    if ((config.getWorldFileBackups() > 0) && normalisedFile.isFile()) {
                        progressReceiver.setMessage(strings.getString("creating.backup.s"));
                        for (int i = config.getWorldFileBackups(); i > 0; i--) {
                            File nextBackupFile = (i > 1) ? getBackupFile(normalisedFile, i - 1) : normalisedFile;
                            if (nextBackupFile.isFile()) {
                                File backupFile = getBackupFile(normalisedFile, i);
                                if (backupFile.isFile()) {
                                    if (! backupFile.delete()) {
                                        throw new RuntimeException("Could not delete old backup file " + backupFile);
                                    }
                                }
                                if (! nextBackupFile.renameTo(backupFile)) {
                                    throw new RuntimeException("Could not move " + nextBackupFile + " to " + backupFile);
                                }
                            }
                        }
                        progressReceiver.setMessage(null);
                    }
                    
                    ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(normalisedFile)));
                    try {
                        out.writeObject(world);
                    } finally {
                        out.close();
                    }
                    
                    Map<String, byte[]> layoutData = config.getJideLayoutData();
                    if (layoutData == null) {
                        layoutData = new HashMap<String, byte[]>();
                    }
                    layoutData.put(world.getName(), dockingManager.getLayoutRawData());
                    config.setJideLayoutData(layoutData);
                    
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException("I/O error saving file (message: " + e.getMessage() + ")", e);
                }
            }
        }, false);

        // Log an event
        Configuration config = Configuration.getInstance();
        if (config != null) {
            EventVO event = new EventVO(EVENT_KEY_ACTION_SAVE_WORLD).addTimestamp();
            event.setAttribute(ATTRIBUTE_KEY_MAX_HEIGHT, world.getMaxHeight());
            Dimension loadedDimension = world.getDimension(0);
            event.setAttribute(ATTRIBUTE_KEY_TILES, loadedDimension.getTiles().size());
            logLayers(loadedDimension, event, "");
            loadedDimension = world.getDimension(1);
            if (loadedDimension != null) {
                event.setAttribute(ATTRIBUTE_KEY_NETHER_TILES, loadedDimension.getTiles().size());
                logLayers(loadedDimension, event, "nether.");
            }
            loadedDimension = world.getDimension(2);
            if (loadedDimension != null) {
                event.setAttribute(ATTRIBUTE_KEY_END_TILES, loadedDimension.getTiles().size());
                logLayers(loadedDimension, event, "end.");
            }
            if (world.getImportedFrom() != null) {
                event.setAttribute(ATTRIBUTE_KEY_IMPORTED_WORLD, true);
            }
            config.logEvent(event);
        }

        if (undoManager != null) {
            undoManager.armSavePoint();
        }
        world.setDirty(false);
        lastSelectedFile = file;

        Configuration.getInstance().setWorldDirectory(file.getParentFile());
        
        return true;
    }
    
    private File getBackupFile(File file, int backup) {
        String filename = file.getName();
        int p = filename.lastIndexOf('.');
        if (p != -1) {
            filename = filename.substring(0, p) + "." + backup + filename.substring(p);
        } else {
            filename = filename + "." + backup;
        }
        return new File(file.getParentFile(), filename);
    }
    
    private boolean isBackupFile(File file) {
        String filename = file.getName();
        if (filename.toLowerCase().endsWith(".world")) {
            filename = filename.substring(0, filename.length() - 6);
        }
        int p = filename.length() - 1;
        while ((p > 0) && Character.isDigit(filename.charAt(p))) {
            p--;
        }
        // At this point p points to the dot in front of the backup number, if
        // there is one
        return (p > 0) && (p < (filename.length() - 1)) && (filename.charAt(p) == '.');
    }
    
    private File getOriginalFile(File backupFile) {
        String extension = "";
        String filename = backupFile.getName();
        if (filename.toLowerCase().endsWith(".world")) {
            extension = filename.substring(filename.length() - 6);
            filename = filename.substring(0, filename.length() - 6);
        }
        int p = filename.length() - 1;
        while ((p > 0) && Character.isDigit(filename.charAt(p))) {
            p--;
        }
        // At this point p points to the dot in front of the backup number
        return new File(backupFile.getParentFile(), filename.substring(0, p) + extension);
    }
    
    private void addRemoveTiles() {
        TileEditor tileEditor = new TileEditor(this, dimension, selectedColourScheme, autoBiomeScheme, customBiomeManager, hiddenLayers, false, view.getLightOrigin());
        tileEditor.setVisible(true);
    }
    
    private void importWorld() {
        if (! saveIfNecessary()) {
            return;
        }
        if (! Configuration.getInstance().isImportWarningDisplayed()) {
            JOptionPane.showMessageDialog(this, strings.getString("the.import.functionality.only.imports.the.i.landscape"), strings.getString("information"), JOptionPane.INFORMATION_MESSAGE);
        }
        MapImportDialog dialog = new MapImportDialog(this);
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            setWorld(dialog.getImportedWorld());
            lastSelectedFile = null;
            Configuration config = Configuration.getInstance();
            config.setImportWarningDisplayed(true);
            enableImportedWorldOperation();
            setDimensionControlStates();
        }
    }
    
    private void importHeightMap() {
        if (! saveIfNecessary()) {
            return;
        }
        ImportHeightMapDialog dialog = new ImportHeightMapDialog(this, selectedColourScheme);
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            setWorld(null);
            World2 importedWorld = dialog.getImportedWorld();
            setWorld(importedWorld);
            lastSelectedFile = null;
        }
    }
    
    private void merge() {
        if ((world.getImportedFrom() != null) && (! world.isAllowMerging())) {
            JOptionPane.showMessageDialog(this, strings.getString("this.world.was.imported.before.the.great.coordinate.shift"), strings.getString("merge.not.allowed"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((world.getImportedFrom() == null) && (JOptionPane.showConfirmDialog(this, strings.getString("this.world.was.not.imported"), strings.getString("not.imported"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)) {
            return;
        }
        if ((world.getDimensions().length > 1) && (JOptionPane.showConfirmDialog(this, strings.getString("merging.the.nether.or.end.is.not.yet.supported"), strings.getString("merging.nether.not.supported"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)) {
            return;
        }
        Configuration config = Configuration.getInstance();
        if (((config == null) || (! config.isMergeWarningDisplayed())) && (JOptionPane.showConfirmDialog(this, strings.getString("this.is.experimental.and.unfinished.functionality"), strings.getString("experimental.functionality"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)) {
            return;
        }
        MergeWorldDialog dialog = new MergeWorldDialog(this, world, autoBiomeScheme, selectedColourScheme, customBiomeManager, hiddenLayers, false, view.getLightOrigin());
        view.setInhibitUpdates(true);
        try {
            dialog.setVisible(true);
        } finally {
            view.setInhibitUpdates(false);
        }
    }

    private void updateZoomLabel() {
        double factor = Math.pow(2.0, zoom);
        int zoomPercentage = (int) (100 * factor);
        zoomLabel.setText(MessageFormat.format(strings.getString("zoom.0"), zoomPercentage));
        glassPane.setScale((float) factor);
    }

    private void initComponents() {
        view = new WorldPainter(selectedColourScheme, autoBiomeScheme, customBiomeManager);
        view.setRadius(radius);
        view.setBrushShape(brush.getBrushShape());
        final Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(IconUtils.loadImage("org/pepsoft/worldpainter/cursor.png"), new Point(15, 15), "Custom Crosshair");
        view.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(cursor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(null);
            }
        });
        scrollPane.setViewportView(view);
        scrollPane.getViewport().setBackground(new Color(Constants.VOID_COLOUR));

        glassPane = new GlassPane();
        JRootPane privateRootPane = new JRootPane();
        privateRootPane.setContentPane(scrollPane);
        privateRootPane.setGlassPane(glassPane);
        glassPane.setVisible(true);

        // Set up docking framework
        JPanel contentContainer = new JPanel(new BorderLayout());
        getContentPane().add(contentContainer, BorderLayout.CENTER);
        dockingManager = new DefaultDockingManager(this, contentContainer);
        if (SystemUtils.isLinux()) {
            // On Linux, at least in the GTK look and feel, the default
            // (whatever it is) doesn't work; nothing is displayed
            dockingManager.setOutlineMode(DockingManager.MIX_OUTLINE_MODE);
        }
        dockingManager.setGroupAllowedOnSidePane(false);
        dockingManager.setTabbedPaneCustomizer(new DockingManager.TabbedPaneCustomizer() {
            @Override
            public void customize(JideTabbedPane tabbedPane) {
                tabbedPane.setTabPlacement(JTabbedPane.LEFT);
            }
        });
        Workspace workspace = dockingManager.getWorkspace();
        workspace.setLayout(new BorderLayout());
        workspace.add(privateRootPane, BorderLayout.CENTER);

        setJMenuBar(createMenuBar());
        
        getContentPane().add(createToolBar(), BorderLayout.NORTH);

        getContentPane().add(createStatusBar(), BorderLayout.SOUTH);

        final ScrollController scrollController = new ScrollController();
        scrollController.install();

        mapDragControl = new MapDragControl() {
            @Override
            public boolean isMapDraggingInhibited() {
                return mapDraggingInhibited;
            }

            @Override
            public void setMapDraggingInhibited(boolean mapDraggingInhibited) {
                if (mapDraggingInhibited != this.mapDraggingInhibited) {
                    this.mapDraggingInhibited = mapDraggingInhibited;
                    if (mapDraggingInhibited) {
                        scrollController.uninstall();
                    } else {
                        scrollController.install();
                    }
                }
            }
            
            private boolean mapDraggingInhibited;
        };
        
//        getContentPane().add(createToolPanel(), BorderLayout.WEST);
        dockingManager.addFrame(createDockableFrame(createToolPanel(), "Tools", DOCK_SIDE_WEST, 1));

//        getContentPane().add(createLayerPanel(), BorderLayout.WEST);
        dockingManager.addFrame(createDockableFrame(createLayerPanel(), "Layers", DOCK_SIDE_WEST, 3));

//        getContentPane().add(createTerrainPanel(), BorderLayout.WEST);
        dockingManager.addFrame(createDockableFrame(createTerrainPanel(), "Terrain", DOCK_SIDE_WEST, 4));
//        extraTerrainToolBar = createExtraTerrainPanel();
        dockingManager.addFrame(createDockableFrame(createExtraTerrainPanel(), "stainedClays", "Stained Clays", DOCK_SIDE_WEST, 4));

//        getContentPane().add(createCustomTerrainPanel(), BorderLayout.WEST);
        dockingManager.addFrame(createDockableFrame(createCustomTerrainPanel(), "customTerrain", "Custom Terrain", DOCK_SIDE_WEST, 4));
        
//        getContentPane().add(createBrushPanel(), BorderLayout.EAST);
        dockingManager.addFrame(createDockableFrame(createBrushPanel(), "Brushes", DOCK_SIDE_EAST, 1));
        
        if (customBrushes.containsKey(CUSTOM_BRUSHES_DEFAULT_TITLE)) {
//            getContentPane().add(createCustomBrushPanel(CUSTOM_BRUSHES_DEFAULT_TITLE, customBrushes.get(CUSTOM_BRUSHES_DEFAULT_TITLE)), BorderLayout.EAST);
            dockingManager.addFrame(createDockableFrame(createCustomBrushPanel(CUSTOM_BRUSHES_DEFAULT_TITLE, customBrushes.get(CUSTOM_BRUSHES_DEFAULT_TITLE)), "customBrushesDefault", "Custom Brushes", DOCK_SIDE_EAST, 1));
        }
        for (Map.Entry<String, List<Brush>> entry: customBrushes.entrySet()) {
            if (entry.getKey().equals(CUSTOM_BRUSHES_DEFAULT_TITLE)) {
                continue;
            }
//            getContentPane().add(createCustomBrushPanel(entry.getKey(), entry.getValue()), BorderLayout.EAST);
            dockingManager.addFrame(createDockableFrame(createCustomBrushPanel(entry.getKey(), entry.getValue()), "customBrushes." + entry.getKey(), entry.getKey(), DOCK_SIDE_EAST, 1));
        }
        
//        getContentPane().add(createBrushSettingsPanel(), BorderLayout.EAST);
        dockingManager.addFrame(createDockableFrame(createBrushSettingsPanel(), "brushSettings", "Brush Settings", DOCK_SIDE_EAST, 2));

//        biomesToolBar = new JToolBar(JToolBar.VERTICAL);
//        JPanel panel = new JPanel(new GridLayout(1, 1));
//        panel.setBorder(new TitledBorder(strings.getString("biomes")));
//        panel.add(biomePaintOp.getOptionsPanel());
//        biomesToolBar.add(panel);
        biomesToolBar = createDockableFrame(biomePaintOp.getOptionsPanel(), "Biomes", DOCK_SIDE_WEST, 4);
        biomesToolBar.setInitMode(DockContext.STATE_HIDDEN);
        dockingManager.addFrame(biomesToolBar);
        
//        annotationsToolBar = new JToolBar(JToolBar.VERTICAL);
//        panel = new JPanel(new GridLayout(1, 1));
//        panel.setBorder(new TitledBorder("Annotations"));
//        panel.add(annotateOp.getOptionsPanel());
//        annotationsToolBar.add(panel);
        annotationsToolBar = createDockableFrame(annotateOp.getOptionsPanel(), "Annotations", DOCK_SIDE_WEST, 4);
        annotationsToolBar.setInitMode(DockContext.STATE_HIDDEN);
        dockingManager.addFrame(annotationsToolBar);
        
        Configuration config = Configuration.getInstance();
        if (config.getDefaultJideLayoutData() != null) {
            dockingManager.loadLayoutFrom(new ByteArrayInputStream(config.getDefaultJideLayoutData()));
        } else {
            dockingManager.resetToDefault();
        }

//        try {
//            biomesToolBar.setHidden(true);
//            annotationsToolBar.setHidden(true);
//        } catch (PropertyVetoException ex) {
//            throw new RuntimeException(ex);
//        }
        
        MouseAdapter viewListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point imageCoords = view.viewToImageCoordinates(e.getPoint());
                Point worldCoords = view.imageToWorldCoordinates(imageCoords);
                updateStatusBar(worldCoords.x, worldCoords.y);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                locationLabel.setText(strings.getString("location-"));
                heightLabel.setText(MessageFormat.format(strings.getString("height.of.0"), (dimension != null) ? (dimension.getMaxHeight() - 1) : "?"));
                waterLabel.setText(null);
                App.this.biomeLabel.setText(strings.getString("biome-"));
                materialLabel.setText(strings.getString("material-"));
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    Point mouseLocation = null, worldLocation = null;
                    Point viewMousePosition = view.getMousePosition();
                    if (viewMousePosition != null) {
                        mouseLocation = glassPane.getMousePosition();
                        worldLocation = view.imageToWorldCoordinates(view.viewToImageCoordinates(viewMousePosition));
                    }
                    if (e.getWheelRotation() < 0) {
                        zoom += -e.getWheelRotation();
                        view.setZoom(zoom);
                    } else {
                        zoom -= e.getWheelRotation();
                        view.setZoom(zoom);
                    }
                    updateZoomLabel();
                    if ((worldLocation != null) && (mouseLocation != null)) {
                        view.moveTo(worldLocation, mouseLocation);
                    }
                } else if (e.isAltDown() || e.isAltGraphDown()) {
                    if (e.getWheelRotation() < 0) {
                        ACTION_ROTATE_BRUSH_LEFT.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString()));
                    } else {
                        ACTION_ROTATE_BRUSH_RIGHT.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString()));
                    }
                } else if (activeOperation instanceof RadiusOperation) {
                    if (e.isShiftDown()) {
                        if (e.getWheelRotation() < 0) {
                            decreaseRadiusByOne();
                        } else {
                            increaseRadiusByOne();
                        }
                    } else {
                        if (e.getWheelRotation() < 0) {
                            decreaseRadius(-e.getWheelRotation());
                        } else {
                            increaseRadius(e.getWheelRotation());
                        }
                    }
                }
            }
        };
        view.addMouseMotionListener(viewListener);
        view.addMouseListener(viewListener);
        view.addMouseWheelListener(viewListener);

        JRootPane rootPane = getRootPane();
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put(ACTION_NAME_INCREASE_RADIUS, new BetterAction(ACTION_NAME_INCREASE_RADIUS, strings.getString("increase.radius")) {
            @Override
            public void performAction(ActionEvent e) {
                increaseRadius(1);
            }

            private static final long serialVersionUID = 2011090601L;
        });
        actionMap.put(ACTION_NAME_INCREASE_RADIUS_BY_ONE, new BetterAction(ACTION_NAME_INCREASE_RADIUS_BY_ONE, "Increase brush radius by one") {
            @Override
            public void performAction(ActionEvent e) {
                increaseRadiusByOne();
            }

            private static final long serialVersionUID = 2011090601L;
        });
        actionMap.put(ACTION_NAME_DECREASE_RADIUS, new BetterAction(ACTION_NAME_DECREASE_RADIUS, strings.getString("decrease.radius")) {
            @Override
            public void performAction(ActionEvent e) {
                decreaseRadius(1);
            }

            private static final long serialVersionUID = 2011090601L;
        });
        actionMap.put(ACTION_NAME_DECREASE_RADIUS_BY_ONE, new BetterAction(ACTION_NAME_DECREASE_RADIUS_BY_ONE, "Decrease brush radius by one") {
            @Override
            public void performAction(ActionEvent e) {
                decreaseRadiusByOne();
            }

            private static final long serialVersionUID = 2011090601L;
        });
        actionMap.put(ACTION_NAME_REDO, ACTION_REDO);
        actionMap.put(ACTION_NAME_ZOOM_IN, ACTION_ZOOM_IN);
        actionMap.put(ACTION_NAME_ZOOM_OUT, ACTION_ZOOM_OUT);
        actionMap.put(ACTION_ZOOM_RESET.getName(), ACTION_ZOOM_RESET);
        actionMap.put(ACTION_ROTATE_BRUSH_LEFT.getName(), ACTION_ROTATE_BRUSH_LEFT);
        actionMap.put(ACTION_ROTATE_BRUSH_RIGHT.getName(), ACTION_ROTATE_BRUSH_RIGHT);
        actionMap.put(ACTION_ROTATE_BRUSH_RESET.getName(), ACTION_ROTATE_BRUSH_RESET);
        actionMap.put(ACTION_ROTATE_BRUSH_RIGHT_30_DEGREES.getName(), ACTION_ROTATE_BRUSH_RIGHT_30_DEGREES);
        actionMap.put(ACTION_ROTATE_BRUSH_RIGHT_45_DEGREES.getName(), ACTION_ROTATE_BRUSH_RIGHT_45_DEGREES);
        actionMap.put(ACTION_ROTATE_BRUSH_RIGHT_90_DEGREES.getName(), ACTION_ROTATE_BRUSH_RIGHT_90_DEGREES);

        int platformCommandMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(VK_SUBTRACT, 0),                                     ACTION_NAME_DECREASE_RADIUS);
        inputMap.put(KeyStroke.getKeyStroke(VK_MINUS,    0),                                     ACTION_NAME_DECREASE_RADIUS);
        inputMap.put(KeyStroke.getKeyStroke(VK_ADD,      0),                                     ACTION_NAME_INCREASE_RADIUS);
        inputMap.put(KeyStroke.getKeyStroke(VK_EQUALS,   SHIFT_DOWN_MASK),                       ACTION_NAME_INCREASE_RADIUS);
        inputMap.put(KeyStroke.getKeyStroke(VK_SUBTRACT, SHIFT_DOWN_MASK),                       ACTION_NAME_DECREASE_RADIUS_BY_ONE);
        inputMap.put(KeyStroke.getKeyStroke(VK_MINUS,    SHIFT_DOWN_MASK),                       ACTION_NAME_DECREASE_RADIUS_BY_ONE);
        inputMap.put(KeyStroke.getKeyStroke(VK_ADD,      SHIFT_DOWN_MASK),                       ACTION_NAME_INCREASE_RADIUS_BY_ONE);
        inputMap.put(KeyStroke.getKeyStroke(VK_Z,        platformCommandMask | SHIFT_DOWN_MASK), ACTION_NAME_REDO);
        inputMap.put(KeyStroke.getKeyStroke(VK_MINUS,    platformCommandMask),                   ACTION_NAME_ZOOM_OUT);
        inputMap.put(KeyStroke.getKeyStroke(VK_EQUALS,   platformCommandMask | SHIFT_DOWN_MASK), ACTION_NAME_ZOOM_IN);
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD0,  platformCommandMask),                   ACTION_ZOOM_RESET.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_SUBTRACT, ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_LEFT.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_MINUS,    ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_LEFT.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_ADD,      ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RIGHT.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_EQUALS,   ALT_DOWN_MASK | SHIFT_DOWN_MASK),       ACTION_ROTATE_BRUSH_RIGHT.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_0,        ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RESET.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD0,  ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RESET.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_3,        ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RIGHT_30_DEGREES.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD3,  ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RIGHT_30_DEGREES.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_4,        ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RIGHT_45_DEGREES.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD4,  ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RIGHT_45_DEGREES.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_9,        ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RIGHT_90_DEGREES.getName());
        inputMap.put(KeyStroke.getKeyStroke(VK_NUMPAD9,  ALT_DOWN_MASK),                         ACTION_ROTATE_BRUSH_RIGHT_90_DEGREES.getName());

        programmaticChange = true;
        try {
            selectBrushButton(brush);
//            selectBrushShapeButton(brushShape);
            levelSlider.setValue((int) (level * 100));
            brushRotationSlider.setValue(brushRotation);
        } finally {
            programmaticChange = false;
        }
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new FlowLayout(FlowLayout.LEADING));
        locationLabel = new JLabel(MessageFormat.format(strings.getString("location.0.1"), -99999, -99999));
        locationLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.add(locationLabel);
        heightLabel = new JLabel(MessageFormat.format(strings.getString("height.0.of.1"), 9999, 9999));
        heightLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.add(heightLabel);
        materialLabel = new JLabel(MessageFormat.format(strings.getString("material.0"), Material.MOSSY_COBBLESTONE.toString()));
        materialLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.add(materialLabel);
        waterLabel = new JLabel(MessageFormat.format(strings.getString("fluid.level.1.depth.2"), 9999, 9999));
        waterLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.add(waterLabel);
        biomeLabel = new JLabel("Auto biome: Mega Spruce Taiga Hills (161)");
        biomeLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.add(biomeLabel);
        radiusLabel = new JLabel(MessageFormat.format(strings.getString("radius.0"), 999));
        radiusLabel.setToolTipText(strings.getString("scroll.the.mouse.wheel"));
        radiusLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.add(radiusLabel);
        zoomLabel = new JLabel(MessageFormat.format(strings.getString("zoom.0"), 100));
        zoomLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.add(zoomLabel);
        final JProgressBar memoryBar = new JProgressBar();
        memoryBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        java.awt.Dimension preferredSize = memoryBar.getPreferredSize();
        preferredSize.width = 100;
        memoryBar.setPreferredSize(preferredSize);
        statusBar.add(memoryBar);
        new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long free = runtime.freeMemory();
                long total = runtime.totalMemory();
                long max = runtime.maxMemory();
                long inUse = total - free;
                memoryBar.setValue((int) (inUse * 100 / max));
                int inUseMB = (int) (inUse / ONE_MEGABYTE);
                int maxMB = (int) (max / ONE_MEGABYTE);
                memoryBar.setToolTipText(MessageFormat.format(strings.getString("memory.usage.0.mb.of.1.mb"), inUseMB, maxMB));
            }
            
            private final Runtime runtime = Runtime.getRuntime();
        }).start();
        return statusBar;
    }

    private JPanel createToolPanel() {
        JPanel toolPanel = new JPanel();
//        toolPanel.setBorder(new TitledBorder(strings.getString("tools")));
        toolPanel.setLayout(new GridLayout(0, 4));
        JComponent button = createButtonForOperation(new RaiseRotatedPyramid(view), "pyramid");
        toolPanel.add(button);
        toolPanel.add(createButtonForOperation(new RaisePyramid(view), "pyramid"));
        toolPanel.add(createButtonForOperation(new Flood(view, false), "flood", 'f'));
        toolPanel.add(createButtonForOperation(new Flood(view, true), "flood_with_lava", 'l'));
        toolPanel.add(createButtonForOperation(new Height(view, this, mapDragControl), "height", 'h'));
        toolPanel.add(createButtonForOperation(new Flatten(view, this, mapDragControl), "flatten", 'a'));
        toolPanel.add(createButtonForOperation(new Smooth(view, this, mapDragControl), "smooth", 's'));
        toolPanel.add(createButtonForOperation(new RaiseMountain(view, this, mapDragControl), "mountain", 'm'));
        //        toolButtonPanel.add(createButtonForOperation(new Erode(view, this, mapDragControl), "erode", 'm'));
        toolPanel.add(createButtonForOperation(new SetSpawnPoint(view), "spawn"));
        toolPanel.add(createButtonForOperation(new Sponge(view, this, mapDragControl), "sponge"));
        button = new JButton(loadIcon("globals"));
        ((JButton) button).setMargin(new Insets(2, 2, 2, 2));
        ((JButton) button).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGlobalOperations();
            }
        });
        button.setToolTipText(strings.getString("global.operations.fill.or.clear.the.world.with.a.terrain.biome.or.layer"));
        toolPanel.add(button);
        for (Operation operation: operations) {
            operation.setView(view);
            toolPanel.add(createButtonForOperation(operation, operation.getName().replaceAll("\\s", "").toLowerCase()));
        }

//        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
//        if (toolbarsLocked) {
//            toolBar.setFloatable(false);
//        }
//        toolBar.add(toolPanel);

        return toolPanel;
    }
    
    private JPanel createLayerPanel() {
        JPanel layerPanel = new JPanel();
//        layerPanel.setBorder(new TitledBorder(strings.getString("layers")));
        layerPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(1, 1, 1, 1);
        Configuration config = Configuration.getInstance();
        for (Layer layer: layers) {
            layerPanel.add(createButtonForOperation(new LayerPaint(view, this, mapDragControl, layer), (layer.getIcon() != null) ? new ImageIcon(layer.getIcon()) : null, layer.getMnemonic(), true), constraints);
        }
        if (! config.isEasyMode()) {
            layerPanel.add(createButtonForOperation(new LayerPaint(view, this, mapDragControl, Populate.INSTANCE), "populate", 'p', true), constraints);
        }
        layerPanel.add(createButtonForOperation(new LayerPaint(view, this, mapDragControl, ReadOnly.INSTANCE), "readonly", 'o', true), constraints);
        disableImportedWorldOperation();


        // Biomes
        
        JPanel panel = new JPanel();
        ((FlowLayout) panel.getLayout()).setHgap(0);
        ((FlowLayout) panel.getLayout()).setVgap(0);
        biomesCheckBox = new JCheckBox();
        biomesCheckBox.setToolTipText(strings.getString("whether.or.not.to.display.this.layer"));
        biomesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (biomesCheckBox.isSelected()) {
                    hiddenLayers.remove(Biome.INSTANCE);
                } else {
                    hiddenLayers.add(Biome.INSTANCE);
                }
                updateLayerVisibility();
            }
        });
        panel.add(biomesCheckBox);
        
        biomesSoloCheckBox = new JCheckBox();
        biomesSoloCheckBox.setToolTipText("<html>Check to show <em>only</em> this layer (the other layers are still exported)</html>");
        biomesSoloCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (biomesSoloCheckBox.isSelected()) {
                    for (JCheckBox otherSoloCheckBox: layerSoloCheckBoxes.values()) {
                        if (otherSoloCheckBox != biomesSoloCheckBox) {
                            otherSoloCheckBox.setSelected(false);
                        }
                    }
                    soloLayer = Biome.INSTANCE;
                } else {
                    soloLayer = null;
                }
                updateLayerVisibility();
            }
        });
        layerSoloCheckBoxes.put(Biome.INSTANCE, biomesSoloCheckBox);
        panel.add(biomesSoloCheckBox);

        // Always use default colours for biomes:
        biomePaintOp = new BiomePaint(view, instance, mapDragControl, colourSchemes[0], customBiomeManager);
        biomesToggleButton = (JToggleButton) createButtonForOperation(biomePaintOp, "biome", 'b', false);
        biomesToggleButton.setText(strings.getString("biomes"));
        panel.add(biomesToggleButton);
        
        if (! config.isEasyMode()) {
            layerPanel.add(panel, constraints);
        }

        
        // Annotations
        
        panel = new JPanel();
        ((FlowLayout) panel.getLayout()).setHgap(0);
        ((FlowLayout) panel.getLayout()).setVgap(0);
        annotationsCheckBox = new JCheckBox();
        annotationsCheckBox.setSelected(true);
        annotationsCheckBox.setToolTipText(strings.getString("whether.or.not.to.display.this.layer"));
        annotationsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (annotationsCheckBox.isSelected()) {
                    hiddenLayers.remove(Annotations.INSTANCE);
                } else {
                    hiddenLayers.add(Annotations.INSTANCE);
                }
                updateLayerVisibility();
            }
        });
        panel.add(annotationsCheckBox);
        
        annotationsSoloCheckBox = new JCheckBox();
        annotationsSoloCheckBox.setToolTipText("<html>Check to show <em>only</em> this layer (the other layers are still exported)</html>");
        annotationsSoloCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (annotationsSoloCheckBox.isSelected()) {
                    for (JCheckBox otherSoloCheckBox: layerSoloCheckBoxes.values()) {
                        if (otherSoloCheckBox != annotationsSoloCheckBox) {
                            otherSoloCheckBox.setSelected(false);
                        }
                    }
                    soloLayer = Annotations.INSTANCE;
                } else {
                    soloLayer = null;
                }
                updateLayerVisibility();
            }
        });
        layerSoloCheckBoxes.put(Annotations.INSTANCE, annotationsSoloCheckBox);
        panel.add(annotationsSoloCheckBox);

        annotateOp = new Annotate(view, instance, mapDragControl, selectedColourScheme);
        annotationsToggleButton = (JToggleButton) createButtonForOperation(annotateOp, "annotations");
        annotationsToggleButton.setText("Annotations");
        panel.add(annotationsToggleButton);

        if (! config.isEasyMode()) {
            layerPanel.add(panel, constraints);
        }

        
        final JPopupMenu customLayerMenu = createCustomLayerMenu(null);
        
        final JButton addLayerButton = new JButton(loadIcon("plus"));
        final JPanel addLayerButtonPanel = new JPanel();
        addLayerButton.setToolTipText(strings.getString("add.a.custom.layer"));
        addLayerButton.setMargin(new Insets(2, 2, 2, 2));
        addLayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customLayerMenu.show(addLayerButtonPanel, addLayerButton.getX() + addLayerButton.getWidth(), addLayerButton.getY());
            }
        });
        ((FlowLayout) addLayerButtonPanel.getLayout()).setHgap(0);
        ((FlowLayout) addLayerButtonPanel.getLayout()).setVgap(0);
        JCheckBox checkBox = new JCheckBox();
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(checkBox.getPreferredSize());
        addLayerButtonPanel.add(spacer);
        spacer = new JPanel();
        spacer.setPreferredSize(checkBox.getPreferredSize());
        addLayerButtonPanel.add(spacer);
        addLayerButtonPanel.add(addLayerButton);
        layerPanel.add(addLayerButtonPanel, constraints);

//        layerToolBar = new JToolBar(JToolBar.VERTICAL);
//        if (toolbarsLocked) {
//            layerToolBar.setFloatable(false);
//        }
//        layerToolBar.add(layerPanel);
        
        return layerPanel;
    }
    
//    private JPanel createLayerManagementButtonPanel() {
//        final JButton addLayerButton = new JButton(loadIcon("plus"));
//        final JPanel addLayerPanel = new JPanel();
//        addLayerButton.setToolTipText(strings.getString("add.a.custom.layer"));
//        addLayerButton.setMargin(new Insets(2, 2, 2, 2));
//        addLayerButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                customLayerMenu.show(addLayerPanel, addLayerButton.getX() + addLayerButton.getWidth(), addLayerButton.getY());
//            }
//        });
//        ((FlowLayout) addLayerPanel.getLayout()).setHgap(0);
//        ((FlowLayout) addLayerPanel.getLayout()).setVgap(0);
//        JCheckBox checkBox = new JCheckBox();
//        JPanel spacer = new JPanel();
//        spacer.setPreferredSize(checkBox.getPreferredSize());
//        addLayerPanel.add(spacer);
//        addLayerPanel.add(addLayerButton);
//        return addLayerPanel;
//    }

    private JPopupMenu createCustomLayerMenu(final String paletteName) {
        JPopupMenu customLayerMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(strings.getString("add.a.custom.object.layer") + "...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CustomObjectDialog dialog = new CustomObjectDialog(App.this, view.getColourScheme());
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    Bo2Layer layer = dialog.getSelectedLayer();
                    if (paletteName != null) {
                        layer.setPalette(paletteName);
                    }
                    registerCustomLayer(layer, true);
                }
            }
        });
        customLayerMenu.add(menuItem);
        
        menuItem = new JMenuItem(strings.getString("add.a.custom.ground.cover.layer") + "...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GroundCoverDialog dialog = new GroundCoverDialog(App.this, MixedMaterial.create(BLK_ROSE), selectedColourScheme, world.isExtendedBlockIds());
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    GroundCoverLayer layer = dialog.getSelectedLayer();
                    if (paletteName != null) {
                        layer.setPalette(paletteName);
                    }
                    registerCustomLayer(layer, true);
                }
            }
        });
        customLayerMenu.add(menuItem);
        
        menuItem = new JMenuItem(strings.getString("add.a.custom.underground.pockets.layer") + "...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UndergroundPocketsDialog dialog = new UndergroundPocketsDialog(App.this, Material.IRON_BLOCK, selectedColourScheme, world.getMaxHeight(), world.isExtendedBlockIds());
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    UndergroundPocketsLayer layer = dialog.getSelectedLayer();
                    if (paletteName != null) {
                        layer.setPalette(paletteName);
                    }
                    registerCustomLayer(layer, true);
                }
            }
        });
        customLayerMenu.add(menuItem);
        
        menuItem = new JMenuItem("Add a custom cave/tunnel layer...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final TunnelLayer layer = new TunnelLayer("Tunnels", 0x000000);
                final int baseHeight, waterLevel;
                final TileFactory tileFactory = dimension.getTileFactory();
                if (tileFactory instanceof HeightMapTileFactory) {
                    baseHeight = (int) ((HeightMapTileFactory) tileFactory).getBaseHeight();
                    waterLevel = ((HeightMapTileFactory) tileFactory).getWaterHeight();
                    layer.setFloodWithLava(((HeightMapTileFactory) tileFactory).isFloodWithLava());
                } else {
                    baseHeight = 58;
                    waterLevel = 62;
                }
                TunnelLayerDialog dialog = new TunnelLayerDialog(App.this, layer, world.isExtendedBlockIds(), dimension.getMaxHeight(), baseHeight, waterLevel);
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    if (paletteName != null) {
                        layer.setPalette(paletteName);
                    }
                    registerCustomLayer(layer, true);
                }
            }
        });
        customLayerMenu.add(menuItem);
        
        menuItem = new JMenuItem("Add a combined layer...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CombinedLayer layer = new CombinedLayer("Combined", "A combined layer", Color.ORANGE.getRGB());
                CombinedLayerDialog dialog = new CombinedLayerDialog(App.this, autoBiomeScheme, selectedColourScheme, customBiomeManager, layer, new ArrayList<Layer>(getAllLayers()));
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    // TODO: get saved layer
                    if (paletteName != null) {
                        layer.setPalette(paletteName);
                    }
                    registerCustomLayer(layer, true);
                }
            }
        });
        customLayerMenu.add(menuItem);
        
        menuItem = new JMenuItem("Import custom layer(s) from file...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importLayers(paletteName);
            }
        });
        customLayerMenu.add(menuItem);
        
        return customLayerMenu;
    }

    private JPanel createTerrainPanel() {
        JPanel terrainPanel = new JPanel();
//        terrainPanel.setBorder(new TitledBorder(strings.getString("terrain")));
        terrainPanel.setLayout(new GridLayout(0, 4));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.GRASS)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.PERMADIRT)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.SAND)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.DESERT)));
        
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.BARE_GRASS)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.STONE)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.ROCK)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.SANDSTONE)));
        
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.PODZOL)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.COBBLESTONE)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.MOSSY_COBBLESTONE)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.GRAVEL)));
        
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.OBSIDIAN)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.WATER)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.LAVA)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.DEEP_SNOW)));
        
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.NETHERRACK)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.SOUL_SAND)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.NETHERLIKE)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.MYCELIUM)));

        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.END_STONE)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.BEDROCK)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.CLAY)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.BEACHES)));
        
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.RED_SAND)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.HARDENED_CLAY)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.RED_DESERT)));
        terrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.MESA)));
        
//        final JToggleButton showExtraTerrainsButton = new JToggleButton(loadIcon("plus"));
//        showExtraTerrainsButton.setMargin(new Insets(2, 2, 2, 2));
//        showExtraTerrainsButton.setToolTipText("Show the stained clay terrain types");
//        showExtraTerrainsButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (showExtraTerrainsButton.isSelected()) {
//                    getContentPane().add(extraTerrainToolBar, BorderLayout.WEST);
//                } else {
//                    getContentPane().remove(extraTerrainToolBar);
//                }
//                App.this.validate(); // Doesn't happen automatically for some reason; Swing bug?
//                getContentPane().repaint(); // Otherwise a ghost image is left behind for some reason; Swing bug?
//            }
//        });
//        terrainPanel.add(showExtraTerrainsButton);
        
//        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
//        if (toolbarsLocked) {
//            toolBar.setFloatable(false);
//        }
//        toolBar.add(terrainPanel);

        return terrainPanel;
    }
    
    private JPanel createExtraTerrainPanel() {
        JPanel extraTerrainPanel = new JPanel();
//        extraTerrainPanel.setBorder(new TitledBorder("More Terrain"));
        extraTerrainPanel.setLayout(new GridLayout(0, 4));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.WHITE_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.ORANGE_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.MAGENTA_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.LIGHT_BLUE_STAINED_CLAY)));
        
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.YELLOW_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.LIME_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.PINK_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.GREY_STAINED_CLAY)));
        
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.LIGHT_GREY_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.CYAN_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.PURPLE_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.BLUE_STAINED_CLAY)));
        
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.BROWN_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.GREEN_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.RED_STAINED_CLAY)));
        extraTerrainPanel.add(createButtonForOperation(new TerrainPaint(view, this, mapDragControl, Terrain.BLACK_STAINED_CLAY)));
        
//        extraTerrainToolBar = new JToolBar(JToolBar.VERTICAL);
//        if (toolbarsLocked) {
//            extraTerrainToolBar.setFloatable(false);
//        }
//        extraTerrainToolBar.add(extraTerrainPanel);

        return extraTerrainPanel;
    }
    
    private JPanel createCustomTerrainPanel() {
        JPanel customTerrainPanel = new JPanel();
//        customTerrainPanel.setBorder(new TitledBorder("Custom Terrain"));
        customTerrainPanel.setLayout(new GridLayout(0, 4));
    
        for (int i = 0; i < Terrain.CUSTOM_TERRAIN_COUNT; i++) {
            customMaterialButtons[i] = (JToggleButton) createButtonForOperation(new CustomTerrainPaint(view, this, mapDragControl, i));
            customMaterialButtons[i].setIcon(ICON_UNKNOWN_PATTERN);
            customMaterialButtons[i].setToolTipText(strings.getString("not.set.click.to.set"));
            addMaterialSelectionTo(customMaterialButtons[i], i);
            customTerrainPanel.add(customMaterialButtons[i]);
        }

//        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
//        if (toolbarsLocked) {
//            toolBar.setFloatable(false);
//        }
//        toolBar.add(customTerrainPanel);

        return customTerrainPanel;
    }
    
    private JPanel createBrushPanel() {
        JPanel optionsPanel = new JPanel();
//        optionsPanel.setBorder(new TitledBorder(strings.getString("brushes")));
        optionsPanel.setLayout(new GridBagLayout());
        JPanel brushPanel = new JPanel(new GridLayout(0, 3));
        brushPanel.add(createBrushButton(SymmetricBrush.SPIKE_CIRCLE));
        brushPanel.add(createBrushButton(SymmetricBrush.SPIKE_SQUARE));
        brushPanel.add(createBrushButton(new BitmapBrush(App.class.getResourceAsStream("resources/brush_noise.png"), strings.getString("noise"))));

        brushPanel.add(createBrushButton(SymmetricBrush.LINEAR_CIRCLE));
        brushPanel.add(createBrushButton(SymmetricBrush.LINEAR_SQUARE));
        brushPanel.add(createBrushButton(new BitmapBrush(App.class.getResourceAsStream("resources/brush_cracked_earth.png"), strings.getString("cracks"))));
        
        brushPanel.add(createBrushButton(SymmetricBrush.COSINE_CIRCLE));
        brushPanel.add(createBrushButton(SymmetricBrush.COSINE_SQUARE));
        brushPanel.add(createBrushButton(SymmetricBrush.CONSTANT_CIRCLE));

        brushPanel.add(createBrushButton(SymmetricBrush.PLATEAU_CIRCLE));
        brushPanel.add(createBrushButton(SymmetricBrush.PLATEAU_SQUARE));
        brushPanel.add(createBrushButton(SymmetricBrush.CONSTANT_SQUARE));
        
        brushPanel.add(createBrushButton(SymmetricBrush.DOME_CIRCLE));
        brushPanel.add(createBrushButton(SymmetricBrush.DOME_SQUARE));
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(1, 1, 1, 1);
        optionsPanel.add(brushPanel, constraints);
        
//        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
//        if (toolbarsLocked) {
//            toolBar.setFloatable(false);
//        }
//        toolBar.add(optionsPanel);
        
        return optionsPanel;
    }

    private JPanel createCustomBrushPanel(String title, List<Brush> customBrushes) {
        JPanel customBrushesPanel = new JPanel();
//        customBrushesPanel.setBorder(new TitledBorder(title));
        customBrushesPanel.setLayout(new GridBagLayout());
        JPanel customBrushPanel = new JPanel(new GridLayout(0, 3));
        for (Brush customBrush: customBrushes) {
            customBrushPanel.add(createBrushButton(customBrush));
        }
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(1, 1, 1, 1);
        customBrushesPanel.add(customBrushPanel, constraints);

//        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
//        if (toolbarsLocked) {
//            toolBar.setFloatable(false);
//        }
//        toolBar.add(customBrushesPanel);
        
        return customBrushesPanel;
    }
    
    private JPanel createBrushSettingsPanel() {
        JPanel brushSettingsPanel = new JPanel();
//        brushSettingsPanel.setBorder(new TitledBorder("Brush Settings"));
        brushSettingsPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(1, 1, 1, 1);

        levelSlider = new JSlider(2, 100);
        levelSlider.setMajorTickSpacing(49);
        levelSlider.setMinorTickSpacing(7);
        levelSlider.setPaintTicks(true);
        levelSlider.setSnapToTicks(true);
        levelSlider.setPaintLabels(false);
        levelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = levelSlider.getValue();
                levelLabel.setText("Intensity: " + ((value < 52) ? (value - 1) : value) + " %");
                if ((! programmaticChange) && (! levelSlider.getValueIsAdjusting())) {
                    if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                        level = value / 100.0f;
                        ((MouseOrTabletOperation) activeOperation).setLevel(level);
                    } else {
                        toolLevel = value / 100.0f;
                        if (activeOperation instanceof MouseOrTabletOperation) {
                            ((MouseOrTabletOperation) activeOperation).setLevel(toolLevel);
                        }
                    }
                }
            }
        });
        
        brushRotationSlider = new JSlider(-180, 180);
        brushRotationSlider.setMajorTickSpacing(45);
        brushRotationSlider.setMinorTickSpacing(15);
        brushRotationSlider.setPaintTicks(true);
        brushRotationSlider.setSnapToTicks(true);
        brushRotationSlider.setPaintLabels(false);
        brushRotationSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = brushRotationSlider.getValue();
                brushRotationLabel.setText("Rotation: " + ((value < 0) ? (((value - 7) / 15) * 15) : (((value + 7) / 15) * 15)) + "°");
                if ((! programmaticChange) && (! brushRotationSlider.getValueIsAdjusting())) {
                    if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                        brushRotation = value;
                    } else {
                        toolBrushRotation = value;
                    }
                    updateBrushRotation();
                }
            }
        });
        
        constraints.insets = new Insets(3, 1, 1, 1);
        brushRotationLabel = new JLabel("Rotation: 0°");
        brushSettingsPanel.add(brushRotationLabel, constraints);
        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(1, 1, 1, 1);
        // The preferred width of the slider is way too much. Make it smaller, and
        // then fill the available width created by the buttons
        java.awt.Dimension preferredSize = brushRotationSlider.getPreferredSize();
        preferredSize.width = 1;
        brushRotationSlider.setPreferredSize(preferredSize);
        brushSettingsPanel.add(brushRotationSlider, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(3, 1, 1, 1);
        levelLabel = new JLabel("Intensity: 50 %");
        brushSettingsPanel.add(levelLabel, constraints);
        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(1, 1, 1, 1);
        // The preferred width of the slider is way too much. Make it smaller, and
        // then fill the available width created by the buttons
        preferredSize = levelSlider.getPreferredSize();
        preferredSize.width = 1;
        levelSlider.setPreferredSize(preferredSize);
        brushSettingsPanel.add(levelSlider, constraints);
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(3, 1, 1, 1);
        brushSettingsPanel.add(new JLabel("Options"), constraints);
        
        constraints.insets = new Insets(1, 1, 1, 1);
        brushSettingsPanel.add(brushOptions, constraints);
        
//        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
//        if (toolbarsLocked) {
//            toolBar.setFloatable(false);
//        }
//        toolBar.add(brushSettingsPanel);
        
        return brushSettingsPanel;
    }

    private void updateBrushRotation() {
        int desiredBrushRotation = ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) ? brushRotation : toolBrushRotation;
        if (desiredBrushRotation != previousBrushRotation) {
            long start = System.currentTimeMillis();
            if (desiredBrushRotation == 0) {
                for (Map.Entry<Brush, JToggleButton> entry: brushButtons.entrySet()) {
                    Brush brush = entry.getKey();
                    JToggleButton button = entry.getValue();
                    button.setIcon(createBrushIcon(brush, 0));
                    if (button.isSelected() && (activeOperation instanceof RadiusOperation)) {
                        ((RadiusOperation) activeOperation).setBrush(brush);
                    }
                }
            } else {
                for (Map.Entry<Brush, JToggleButton> entry: brushButtons.entrySet()) {
                    Brush brush = entry.getKey();
                    JToggleButton button = entry.getValue();
                    button.setIcon(createBrushIcon(brush, desiredBrushRotation));
                    if (button.isSelected() && (activeOperation instanceof RadiusOperation)) {
                        Brush rotatedBrush = RotatedBrush.rotate(brush, desiredBrushRotation);
                        ((RadiusOperation) activeOperation).setBrush(rotatedBrush);
                    }
                }
            }
            view.setBrushRotation(desiredBrushRotation);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Updating brush rotation took " + (System.currentTimeMillis() - start) + " ms");
            }
            previousBrushRotation = desiredBrushRotation;
        }
    }

    private void registerCustomLayer(final CustomLayer layer, boolean activate) {
        // Add to palette, creating it if necessary
        Palette palette = paletteManager.register(layer);

        // Show the palette if it is not showing yet
        if (palette != null) {
            dockingManager.addFrame(palette.getDockableFrame());
            dockingManager.dockFrame(palette.getDockableFrame().getKey(), DockContext.DOCK_SIDE_WEST, 3);
            if (activate) {
                dockingManager.activateFrame(palette.getDockableFrame().getKey());
        }
        }

        if (activate) {
            paletteManager.activate(layer);
        }
    }

    private void unregisterCustomLayer(final CustomLayer layer) {
        // Remove from palette
        Palette palette = paletteManager.unregister(layer);

        // Remove tracked GUI components
        layerSoloCheckBoxes.remove(layer);

        // If the palette is now empty, remove it too
        if (palette.isEmpty()) {
            paletteManager.delete(palette);
            dockingManager.removeFrame(palette.getDockableFrame().getKey());
//            validate();
        }
    }

    // PaletteManager.ButtonProvider
    
    @Override
    public JPanel createCustomLayerButton(final CustomLayer layer) {
        Operation operation;
        if (layer instanceof CombinedLayer) {
            operation = new CombinedLayerPaint(view, App.this, mapDragControl, (CombinedLayer) layer);
        } else {
            operation = new LayerPaint(view, App.this, mapDragControl, layer);
        }
        final JPanel buttonPanel = (JPanel) createButtonForOperation(operation, new ImageIcon(layer.getIcon()), '\0', true);
        final JToggleButton button = (JToggleButton) buttonPanel.getComponent(2);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
            
            private void showPopup(MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem menuItem = new JMenuItem(strings.getString("edit") + "...");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        edit();
                    }
                });
                popup.add(menuItem);
                menuItem = new JMenuItem(strings.getString("remove") + "...");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        remove();
                    }
                });
                popup.add(menuItem);
                menuItem = new JMenuItem("Export to file...");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        exportLayer(layer);
                    }
                });
                popup.add(menuItem);
                
                JMenu paletteMenu = new JMenu("Move to palette");

                for (final Palette palette: paletteManager.getPalettes()) {
                    menuItem = new JMenuItem(palette.getName());
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            moveLayerToPalette(layer, palette);
                        }
                    });
                    if (palette.contains(layer)) {
                        menuItem.setEnabled(false);
                    }
                    paletteMenu.add(menuItem);
                }

                menuItem = new JMenuItem("New palette...");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        createNewLayerPalette(layer);
                    }
                });
                paletteMenu.add(menuItem);

                popup.add(paletteMenu);

                List<Action> actions = layer.getActions();
                if (actions != null) {
                    for (Action action: actions) {
                        popup.add(new JMenuItem(action));
                    }
                }
                
                popup.show(button, e.getX(), e.getY());
            }
            
            private void edit() {
                int previousColour = layer.getColour();
                CustomLayerDialog<? extends CustomLayer> dialog;
                if (layer instanceof Bo2Layer) {
                    dialog = new CustomObjectDialog(App.this, selectedColourScheme, (Bo2Layer) layer);
                } else if (layer instanceof GroundCoverLayer) {
                    dialog = new GroundCoverDialog(App.this, (GroundCoverLayer) layer, selectedColourScheme, world.isExtendedBlockIds());
                } else if (layer instanceof UndergroundPocketsLayer) {
                    dialog = new UndergroundPocketsDialog(App.this, (UndergroundPocketsLayer) layer, selectedColourScheme, dimension.getMaxHeight(), world.isExtendedBlockIds());
                } else if (layer instanceof CombinedLayer) {
                    dialog = new CombinedLayerDialog(App.this, autoBiomeScheme, selectedColourScheme, customBiomeManager, (CombinedLayer) layer, new ArrayList<Layer>(getAllLayers()));
                } else if (layer instanceof TunnelLayer) {
                    final int baseHeight, waterLevel;
                    final TileFactory tileFactory = dimension.getTileFactory();
                    if (tileFactory instanceof HeightMapTileFactory) {
                        baseHeight = (int) ((HeightMapTileFactory) tileFactory).getBaseHeight();
                        waterLevel = ((HeightMapTileFactory) tileFactory).getWaterHeight();
                    } else {
                        baseHeight = 58;
                        waterLevel = 62;
                    }
                    dialog = new TunnelLayerDialog(App.this, (TunnelLayer) layer, world.isExtendedBlockIds(), dimension.getMaxHeight(), baseHeight, waterLevel);
                } else {
                    throw new RuntimeException("Don't know how to edit " + layer.getName());
                }
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    button.setText(layer.getName());
                    button.setToolTipText(layer.getName() + ": " + layer.getDescription());
                    int newColour = layer.getColour();
                    boolean viewRefreshed = false;
                    if (newColour != previousColour) {
                        button.setIcon(new ImageIcon(layer.getIcon()));
                        view.refreshTilesForLayer(layer);
                        viewRefreshed = true;
                    }
                    dimension.setDirty(true);
                    if (layer instanceof CombinedLayer) {
                        updateHiddenLayers();
                    }
                    if ((layer instanceof TunnelLayer) && (! viewRefreshed)) {
                        view.refreshTilesForLayer(layer);
                        viewRefreshed = true;
                    }
                }
            }
            
            private void remove() {
                if (JOptionPane.showConfirmDialog(App.this, MessageFormat.format(strings.getString("are.you.sure.you.want.to.remove.the.0.layer"), layer.getName()), MessageFormat.format(strings.getString("confirm.0.removal"), layer.getName()), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if ((activeOperation instanceof LayerPaint) && (((LayerPaint) activeOperation).getLayer() == layer)) {
                        toolButtonGroup.clearSelection();
                    }
                    dimension.setEventsInhibited(true);
                    try {
                        dimension.clearLayerData(layer);
                    } finally {
                        dimension.setEventsInhibited(false);
                    }
                    unregisterCustomLayer(layer);
                    
                    boolean visibleLayersChanged = false;
                    if (hiddenLayers.contains(layer)) {
                        hiddenLayers.remove(layer);
                        visibleLayersChanged = true;
                    }
                    if (layer.equals(soloLayer)) {
                        soloLayer = null;
                        visibleLayersChanged = true;
                    }
                    if (layer instanceof LayerContainer) {
                        boolean layersUnhidden = false;
                        for (Layer subLayer: ((LayerContainer) layer).getLayers()) {
                            if ((subLayer instanceof CustomLayer) && ((CustomLayer) subLayer).isHide()) {
                                ((CustomLayer) subLayer).setHide(false);
                                layersUnhidden = true;
                            }
                        }
                        if (layersUnhidden) {
                            updateHiddenLayers();
                            visibleLayersChanged = false;
                        }
                    }
                    if (visibleLayersChanged) {
                        updateLayerVisibility();
                    }

                    App.this.validate(); // Doesn't happen automatically for some reason; Swing bug?
                }
            }
        });
        return buttonPanel;
    }

    @Override
    public JPanel createPopupMenuButton(String paletteName) {
        final JPopupMenu customLayerMenu = createCustomLayerMenu(paletteName);
        
        final JButton addLayerButton = new JButton(loadIcon("plus"));
        final JPanel addLayerButtonPanel = new JPanel();
        addLayerButton.setToolTipText(strings.getString("add.a.custom.layer"));
        addLayerButton.setMargin(new Insets(2, 2, 2, 2));
        addLayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customLayerMenu.show(addLayerButtonPanel, addLayerButton.getX() + addLayerButton.getWidth(), addLayerButton.getY());
            }
        });
        ((FlowLayout) addLayerButtonPanel.getLayout()).setHgap(0);
        ((FlowLayout) addLayerButtonPanel.getLayout()).setVgap(0);
        JCheckBox checkBox = new JCheckBox();
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(checkBox.getPreferredSize());
        addLayerButtonPanel.add(spacer);
        spacer = new JPanel();
        spacer.setPreferredSize(checkBox.getPreferredSize());
        addLayerButtonPanel.add(spacer);
        addLayerButtonPanel.add(addLayerButton);

        return addLayerButtonPanel;
    }

    private void updateHiddenLayers() {
        // Hide newly hidden layers
        for (CustomLayer layer: paletteManager.getLayers()) {
            if (layer.isHide()) {
                if ((activeOperation instanceof LayerPaint) && (((LayerPaint) activeOperation).getLayer().equals(layer))) {
                    deselectTool();
                }
                unregisterCustomLayer(layer);
                hiddenLayers.remove(layer);
                if (layer.equals(soloLayer)) {
                    soloLayer = null;
                }
                layersWithNoButton.add(layer);
            }
        }
        // Show newly unhidden layers
        for (Iterator<CustomLayer> i = layersWithNoButton.iterator(); i.hasNext(); ) {
            CustomLayer layer = i.next();
            if (! layer.isHide()) {
                i.remove();
                registerCustomLayer(layer, false);
            }
        }
        updateLayerVisibility();
    }
    
    private void createNewLayerPalette(CustomLayer layer) {
        String name;
        if ((name = JOptionPane.showInputDialog(this, "Enter a unique name for the new palette:", "New Palette", JOptionPane.QUESTION_MESSAGE)) != null) {
            if (paletteManager.getPalette(name) != null) {
                JOptionPane.showMessageDialog(this, "There is already a palette with that name!", "Duplicate Name", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Palette destPalette = paletteManager.create(name);
            dockingManager.addFrame(destPalette.getDockableFrame());
            dockingManager.dockFrame(destPalette.getDockableFrame().getKey(), DockContext.DOCK_SIDE_WEST, 3);
//            validate();
            moveLayerToPalette(layer, destPalette);
            dockingManager.activateFrame(destPalette.getDockableFrame().getKey());
        }
    }

    private void moveLayerToPalette(CustomLayer layer, Palette destPalette) {
        Palette srcPalette = paletteManager.move(layer, destPalette);
//        dockingManager.addFrame(destPalette);
//        dockingManager.dockFrame(destPalette.getKey(), DockContext.DOCK_SIDE_WEST, 3);
        if (srcPalette.isEmpty()) {
            dockingManager.removeFrame(srcPalette.getDockableFrame().getKey());
            paletteManager.delete(srcPalette);
//            validate();
        }
    }

    private JMenuBar createMenuBar() {
        JMenuItem menuItem = new JMenuItem(ACTION_NEW_WORLD);
        menuItem.setMnemonic('n');
        JMenu menu = new JMenu(strings.getString("file"));
        menu.setMnemonic('i');
        menu.add(menuItem);

        menuItem = new JMenuItem(ACTION_OPEN_WORLD);
        menuItem.setMnemonic('o');
        menu.add(menuItem);

        final Configuration config = Configuration.getInstance();
        if (! config.isEasyMode()) {
            menuItem = new JMenuItem(ACTION_IMPORT_MAP);
            menuItem.setMnemonic('m');
            menuItem.setText("Minecraft map...");
            JMenu subMenu = new JMenu(strings.getString("import"));
            subMenu.setMnemonic('i');
            subMenu.add(menuItem);

            menuItem = new JMenuItem(strings.getString("height.map") + "...");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    importHeightMap();
                }
            });
            menuItem.setMnemonic('h');
            menuItem.setAccelerator(KeyStroke.getKeyStroke(VK_M, PLATFORM_COMMAND_MASK));
            subMenu.add(menuItem);
            menuItem = new JMenuItem(ACTION_IMPORT_LAYER);
            menuItem.setMnemonic('l');
            menuItem.setText("Custom layer(s)...");
            subMenu.add(menuItem);
            menu.add(subMenu);
        } else {
            menuItem = new JMenuItem(ACTION_IMPORT_MAP);
            menuItem.setMnemonic('m');
            menu.add(menuItem);
        }

        menu.addSeparator();

        menuItem = new JMenuItem(ACTION_SAVE_WORLD);
        menuItem.setMnemonic('s');
        menu.add(menuItem);

        menuItem = new JMenuItem(ACTION_SAVE_WORLD_AS);
        menuItem.setMnemonic('a');
        menu.add(menuItem);

        menuItem = new JMenuItem(ACTION_EXPORT_WORLD);
        menuItem.setMnemonic('m');
        if (config.isEasyMode()) {
            menu.add(menuItem);
        } else {
            JMenu exportMenu = new JMenu(strings.getString("export"));
            exportMenu.setMnemonic('e');
            exportMenu.add(menuItem);

            menuItem = new JMenuItem(strings.getString("export.as.image.file") + "...");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    exportImage();
                }
            });
            menuItem.setMnemonic('i');
            exportMenu.add(menuItem);

            menuItem = new JMenuItem(strings.getString("export.as.height.map") + "...");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    exportHeightMap();
                }
            });
            menuItem.setMnemonic('h');
            exportMenu.add(menuItem);

            menu.add(exportMenu);
        }

        menuItem = new JMenuItem(ACTION_MERGE_WORLD);
        menuItem.setMnemonic('m');
        menu.add(menuItem);

        if (! SystemUtils.isMac()) {
            menu.addSeparator();

            menuItem = new JMenuItem(ACTION_EXIT);
            menuItem.setMnemonic('x');
            menu.add(menuItem);
        }
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        menuItem = new JMenuItem(ACTION_UNDO);
        menuItem.setMnemonic('u');
        menu = new JMenu(strings.getString("edit"));
        menu.setMnemonic('e');
        menu.add(menuItem);

        menuItem = new JMenuItem(ACTION_REDO);
        menuItem.setMnemonic('r');
        menu.add(menuItem);

        menu.addSeparator();

        extendedBlockIdsMenuItem = new JCheckBoxMenuItem("Extended block IDs");
        extendedBlockIdsMenuItem.setToolTipText("Allow block IDs from 0 to 4095 (inclusive) as used by some mods");
        extendedBlockIdsMenuItem.setMnemonic('e');
        extendedBlockIdsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (world != null) {
                    world.setExtendedBlockIds(extendedBlockIdsMenuItem.isSelected());
                }
            }
        });
        menu.add(extendedBlockIdsMenuItem);
        
        menuItem = new JMenuItem(ACTION_DIMENSION_PROPERTIES);
        menuItem.setMnemonic('p');
        menu.add(menuItem);

        if (! config.isEasyMode()) {
            menuItem = new JMenuItem(ACTION_CHANGE_HEIGHT);
            menuItem.setMnemonic('h');
            menu.add(menuItem);

            menuItem = new JMenuItem(ACTION_ROTATE_WORLD);
            menuItem.setMnemonic('o');
            menu.add(menuItem);

            menu.addSeparator();
        }

        menuItem = new JMenuItem(strings.getString("global.operations") + "...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showGlobalOperations();
            }
        });
        menuItem.setMnemonic('g');
        menuItem.setAccelerator(KeyStroke.getKeyStroke(VK_G, PLATFORM_COMMAND_MASK));
        menu.add(menuItem);
        
        menu.addSeparator();

        menuItem = new JMenuItem(ACTION_EDIT_TILES);
        menuItem.setMnemonic('t');
        menu.add(menuItem);
        
        addNetherMenuItem = new JMenuItem(strings.getString("add.nether") + "...");
        addNetherMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNether();
            }
        });
        addNetherMenuItem.setMnemonic('n');
        menu.add(addNetherMenuItem);

        addEndMenuItem = new JMenuItem(strings.getString("add.end") + "...");
        addEndMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEnd();
            }
        });
        addEndMenuItem.setMnemonic('d');
        menu.add(addEndMenuItem);
        
        menu.addSeparator();

//        final JMenuItem easyModeItem = new JCheckBoxMenuItem("Advanced mode");
//        if (! config.isEasyMode()) {
//            easyModeItem.setSelected(true);
//        }
//        easyModeItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                Configuration config = Configuration.getInstance();
//                if (config.isEasyMode()) {
//                    if (JOptionPane.showConfirmDialog(App.this, "Are you sure you want to switch to Advanced Mode?", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
//                        config.setEasyMode(false);
//                        try {
//                            config.save();
//                            easyModeItem.setSelected(true);
//                            JOptionPane.showMessageDialog(App.this, "Advanced Mode has been activated. You must\nrestart WorldPainter for the change to take effect!", "Restart Required", JOptionPane.INFORMATION_MESSAGE);
//                        } catch (IOException exception) {
//                            ErrorDialog errorDialog = new ErrorDialog(App.this);
//                            errorDialog.setException(exception);
//                            errorDialog.setVisible(true);
//                        }
//                    } else {
//                        easyModeItem.setSelected(false);
//                    }
//                } else {
//                    if (JOptionPane.showConfirmDialog(App.this, "Are you sure you want to switch off Advanced Mode?", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
//                        config.setEasyMode(true);
//                        try {
//                            config.save();
//                            easyModeItem.setSelected(false);
//                            JOptionPane.showMessageDialog(App.this, "Advanced Mode has been deactivated. You must\nrestart WorldPainter for the change to take effect!", "Restart Required", JOptionPane.INFORMATION_MESSAGE);
//                        } catch (IOException exception) {
//                            ErrorDialog errorDialog = new ErrorDialog(App.this);
//                            errorDialog.setException(exception);
//                            errorDialog.setVisible(true);
//                        }
//                    } else {
//                        easyModeItem.setSelected(true);
//                    }
//                }
//            }
//        });
//        menu.add(easyModeItem);

        if (! config.isEasyMode()) {
            menuItem = new JMenuItem(strings.getString("preferences") + "...");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PreferencesDialog dialog = new PreferencesDialog(App.this, selectedColourScheme);
                    dialog.setVisible(true);
                    if (! dialog.isCancelled()) {
                        setMaxRadius(Configuration.getInstance().getMaximumBrushSize());
                }
                }
            });
            menuItem.setMnemonic('f');
            menu.add(menuItem);
        }
        menuBar.add(menu);

        menuItem = new JMenuItem(ACTION_ZOOM_IN);
        menuItem.setMnemonic('i');
        menu = new JMenu(strings.getString("view"));
        menu.setMnemonic('v');
        menu.add(menuItem);

        menuItem = new JMenuItem(ACTION_ZOOM_OUT);
        menuItem.setMnemonic('o');
        menu.add(menuItem);

        menuItem = new JMenuItem(ACTION_ZOOM_RESET);
        menuItem.setMnemonic('r');
        menu.add(menuItem);
        
        menu.addSeparator();
        
        viewSurfaceMenuItem = new JCheckBoxMenuItem(strings.getString("view.surface"), true);
        viewSurfaceMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewDimension(DIM_NORMAL);
            }
        });
        viewSurfaceMenuItem.setMnemonic('s');
        viewSurfaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(VK_U, PLATFORM_COMMAND_MASK));
        viewSurfaceMenuItem.setEnabled(false);
        menu.add(viewSurfaceMenuItem);

        viewNetherMenuItem = new JCheckBoxMenuItem(strings.getString("view.nether"), false);
        viewNetherMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewDimension(DIM_NETHER);
            }
        });
        viewNetherMenuItem.setMnemonic('n');
        viewNetherMenuItem.setAccelerator(KeyStroke.getKeyStroke(VK_H, PLATFORM_COMMAND_MASK));
        viewNetherMenuItem.setEnabled(false);
        menu.add(viewNetherMenuItem);
        
        viewEndMenuItem = new JCheckBoxMenuItem(strings.getString("view.end"), false);
        viewEndMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewDimension(DIM_END);
            }
        });
        viewEndMenuItem.setMnemonic('e');
        viewEndMenuItem.setAccelerator(KeyStroke.getKeyStroke(VK_D, PLATFORM_COMMAND_MASK));
        viewEndMenuItem.setEnabled(false);
        menu.add(viewEndMenuItem);
        
        menu.addSeparator();
        
        JMenu colourSchemeMenu = new JMenu(strings.getString("change.colour.scheme"));
        String[] colourSchemeNames = {strings.getString("default"), "Flames", "Ovocean", "Sk89q", "DokuDark", "DokuHigh", "DokuLight", "Misa", "Sphax"};
        final int schemeCount = colourSchemeNames.length;
        final JCheckBoxMenuItem[] schemeMenuItems = new JCheckBoxMenuItem[schemeCount];
        for (int i = 0; i < colourSchemeNames.length; i++) {
            final int index = i;
            schemeMenuItems[index] = new JCheckBoxMenuItem(colourSchemeNames[index]);
            if (config.getColourschemeIndex() == index) {
                schemeMenuItems[index].setSelected(true);
            }
            schemeMenuItems[index].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < schemeCount; i++) {
                        if ((i != index) && schemeMenuItems[i].isSelected()) {
                            schemeMenuItems[i].setSelected(false);
                        }
                    }
                    selectedColourScheme = colourSchemes[index];
                    view.setColourScheme(selectedColourScheme);
                    config.setColourschemeIndex(index);
                }
            });
            colourSchemeMenu.add(schemeMenuItems[index]);
        }
        menu.add(colourSchemeMenu);

        menuItem = new JMenuItem(strings.getString("configure.view") + "...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigureViewDialog dialog = new ConfigureViewDialog(App.this, dimension, view);
                dialog.setVisible(true);
                ACTION_GRID.setSelected(view.isDrawGrid());
                ACTION_CONTOURS.setSelected(view.isDrawContours());
                ACTION_OVERLAY.setSelected(view.isDrawOverlay());
            }
        });
        menuItem.setMnemonic('c');
        menuItem.setAccelerator(KeyStroke.getKeyStroke(VK_V, PLATFORM_COMMAND_MASK));
        menu.add(menuItem);
        
//        lockToolbarsMenuItem = new JCheckBoxMenuItem(strings.getString("lock.toolbars"));
//        if (toolbarsLocked) {
//            lockToolbarsMenuItem.setSelected(true);
//        }
//        lockToolbarsMenuItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                setToolbarsLocked(! isToolbarsLocked());
//            }
//        });
//        lockToolbarsMenuItem.setMnemonic('l');
//        menu.add(lockToolbarsMenuItem);
        
        menu.addSeparator();

        menuItem = new JMenuItem(ACTION_RESET_DOCKS);
        menuItem.setMnemonic('d');
        menu.add(menuItem);

        ACTION_LOAD_LAYOUT.setEnabled(config.getDefaultJideLayoutData() != null);
        menuItem = new JMenuItem(ACTION_LOAD_LAYOUT);
        menuItem.setMnemonic('d');
        menu.add(menuItem);

        menuItem = new JMenuItem(ACTION_SAVE_LAYOUT);
        menuItem.setMnemonic('d');
        menu.add(menuItem);

        menu.addSeparator();
        
        menuItem = new JMenuItem(strings.getString("show.3d.view") + "...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point focusPoint = view.getViewCentreInWorldCoords();
                if (threeDeeFrame != null) {
                    threeDeeFrame.requestFocus();
                    threeDeeFrame.moveTo(focusPoint);
                } else {
                    logger.info("Opening 3D view");
                    threeDeeFrame = new ThreeDeeFrame(dimension, view.getColourScheme(), customBiomeManager, focusPoint);
                    threeDeeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    threeDeeFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            // TODO not sure how this can be null, but at least
                            // one error has been reported by a user where it
                            // was
                            if (threeDeeFrame != null) {
                                threeDeeFrame.dispose();
                                threeDeeFrame = null;
                            }
                        }
                    });
                    threeDeeFrame.setLocationRelativeTo(App.this);
                    threeDeeFrame.setVisible(true);
                }
            }
        });
        menuItem.setMnemonic('3');
        menuItem.setAccelerator(KeyStroke.getKeyStroke(VK_3, PLATFORM_COMMAND_MASK));
        menu.add(menuItem);
        
        menuBar.add(menu);

        if (! config.isEasyMode()) {
            menuItem = new JMenuItem(strings.getString("respawn.player") + "...");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RespawnPlayerDialog dialog = new RespawnPlayerDialog(App.this);
                    dialog.setVisible(true);
                }
            });
            menuItem.setMnemonic('r');
            menu = new JMenu(strings.getString("tools"));
            menu.setMnemonic('t');
            menu.add(menuItem);

            menuItem = new JMenuItem(strings.getString("open.custom.brushes.folder"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    File brushesDir = new File(Configuration.getConfigDir(), "brushes");
                    if (! brushesDir.exists()) {
                        if (! brushesDir.mkdirs()) {
                            Toolkit.getDefaultToolkit().beep();
                            return;
                        }
                    }
                    DesktopUtils.open(brushesDir);
                }
            });
            menuItem.setMnemonic('c');
            menu.add(menuItem);

            menuItem = new JMenuItem(strings.getString("open.plugins.folder"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    File pluginsDir = new File(Configuration.getConfigDir(), "plugins");
                    if (! pluginsDir.exists()) {
                        if (! pluginsDir.mkdirs()) {
                            Toolkit.getDefaultToolkit().beep();
                            return;
                        }
                    }
                    DesktopUtils.open(pluginsDir);
                }
            });
            menuItem.setMnemonic('p');
            menu.add(menuItem);

            menuItem = new JMenuItem(strings.getString("biomes.viewer") + "...");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (biomesViewerFrame != null) {
                        biomesViewerFrame.requestFocus();
                    } else {
                        BiomeScheme viewerScheme = null;
                        boolean askedFor17 = false;
                        if ((dimension != null) && (dimension.getDim() == DIM_NORMAL) && (dimension.getMaxHeight() == DEFAULT_MAX_HEIGHT_2)) {
                            if (world.getGenerator() == Generator.LARGE_BIOMES) {
                                viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_7_LARGE, null, false);
                                if (viewerScheme == null) {
                                    askedFor17 = true;
                                    viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_7_LARGE, App.this, true);
                                }
                            } else {
                                viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_7_DEFAULT, null, false);
                                if (viewerScheme == null) {
                                    askedFor17 = true;
                                    viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_7_DEFAULT, App.this, true);
                                }
                            }
                        }
                        if (viewerScheme == null) {
                            viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_7_DEFAULT, null, false);
                        }
                        if (viewerScheme == null) {
                            viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, null, false);
                        }
                        if (viewerScheme == null) {
                            viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_1, null, false);
                        }
                        if ((viewerScheme == null) && (! askedFor17)) {
                            askedFor17 = true;
                            viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_7_DEFAULT, App.this, true);
                        }
                        if (viewerScheme == null) {
                            viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, App.this, true);
                        }
                        if (viewerScheme == null) {
                            viewerScheme = BiomeSchemeManager.getBiomeScheme(BiomeSchemeManager.BIOME_ALGORITHM_1_1, App.this, true);
                        }
                        if (viewerScheme == null) {
                            JOptionPane.showMessageDialog(App.this, strings.getString("you.must.supply.an.original.minecraft.jar"), strings.getString("no.minecraft.jar.supplied"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        logger.info("Opening biomes viewer");
                        biomesViewerFrame = new BiomesViewerFrame(dimension.getMinecraftSeed(), world.getSpawnPoint(), viewerScheme, colourSchemes[0], App.this);
                        biomesViewerFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        biomesViewerFrame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                // TODO not sure how this can be null, but at least
                                // one error has been reported by a user where it
                                // was
                                if (biomesViewerFrame != null) {
                                    biomesViewerFrame.destroy();
                                    biomesViewerFrame.dispose();
                                    biomesViewerFrame = null;
                                }
                            }
                        });
                        biomesViewerFrame.setLocationRelativeTo(App.this);
                        biomesViewerFrame.setVisible(true);
                    }
                }
            });
            menuItem.setMnemonic('b');
            menu.add(menuItem);

    //        menuItem = new JMenuItem("Manage plugins...");
    //        menuItem.addActionListener(new ActionListener() {
    //            @Override
    //            public void actionPerformed(ActionEvent e) {
    //                StringBuilder url = new StringBuilder("http://bo.worldpainter.net:8081/wp/plugins/overview.jsp");
    //                url.append("?uuid=").append(Configuration.getInstance().getUuid().toString());
    //                boolean first = true;
    //                for (Plugin plugin: PluginManager.getInstance().getAllPlugins()) {
    //                    if (plugin.getName().equals("Default")) {
    //                        continue;
    //                    }
    //                    if (first) {
    //                        url.append("&plugins=");
    //                        first = false;
    //                    } else {
    //                        url.append(',');
    //                    }
    //                    url.append(plugin.getName().replaceAll("\\s", "").toLowerCase());
    //                }
    //                SimpleBrowser browser = new SimpleBrowser(App.this, true, "Manage Plugins", url.toString());
    //                browser.setVisible(true);
    //            }
    //        });
    //        menuItem.setMnemonic('p');
    //        menu.add(menuItem);
            menuBar.add(menu);
        }
        
        menuItem = new JMenuItem(ACTION_OPEN_DOCUMENTATION);
        menuItem.setMnemonic('d');
        menu = new JMenu(strings.getString("help"));
//        menu.setMnemonic('h');
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem(strings.getString("about"));
        menuItem.setMnemonic('a');
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog dialog = new AboutDialog(App.this, world, view, undoManager);
                dialog.setVisible(true);
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);
        
        addStatisticsTo(menuBar, "menu", config);
        
        return menuBar;
    }
    
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
//        if (toolbarsLocked) {
//            toolBar.setFloatable(false);
//        }
        toolBar.add(ACTION_NEW_WORLD);
        toolBar.add(ACTION_OPEN_WORLD);
        toolBar.add(ACTION_SAVE_WORLD);
        toolBar.add(ACTION_EXPORT_WORLD);
        toolBar.addSeparator();
        toolBar.add(ACTION_UNDO);
        toolBar.add(ACTION_REDO);
        toolBar.addSeparator();
        toolBar.add(ACTION_ZOOM_OUT);
        toolBar.add(ACTION_ZOOM_RESET);
        toolBar.add(ACTION_ZOOM_IN);
        toolBar.addSeparator();
        toolBar.add(ACTION_DIMENSION_PROPERTIES);
        if (! Configuration.getInstance().isEasyMode()) {
            toolBar.add(ACTION_CHANGE_HEIGHT);
            toolBar.add(ACTION_ROTATE_WORLD);
        }
        toolBar.add(ACTION_EDIT_TILES);
        toolBar.addSeparator();
        toolBar.add(ACTION_MOVE_TO_SPAWN);
        toolBar.add(ACTION_MOVE_TO_ORIGIN);
        toolBar.addSeparator();
        JToggleButton button = new JToggleButton(ACTION_GRID);
        button.setHideActionText(true);
        toolBar.add(button);
        button = new JToggleButton(ACTION_CONTOURS);
        button.setHideActionText(true);
        toolBar.add(button);
        button = new JToggleButton(ACTION_OVERLAY);
        button.setHideActionText(true);
        toolBar.add(button);
        button = new JToggleButton(ACTION_VIEW_DISTANCE);
        button.setHideActionText(true);
        toolBar.add(button);
        button = new JToggleButton(ACTION_WALKING_DISTANCE);
        button.setHideActionText(true);
        toolBar.add(button);
        toolBar.add(ACTION_ROTATE_LIGHT_LEFT);
        toolBar.add(ACTION_ROTATE_LIGHT_RIGHT);
        return toolBar;
    }
    
    private void addStatisticsTo(MenuElement menuElement, @NonNls final String key, final EventLogger eventLogger) {
        if ((menuElement instanceof JMenuItem) && (! (menuElement instanceof JMenu))) {
            JMenuItem menuItem = (JMenuItem) menuElement;
            if (((! (menuItem.getAction() instanceof BetterAction)) || (! ((BetterAction) menuItem.getAction()).isLogEvent()))
                    && (! menuItem.getText().equals("Existing Minecraft map..."))
                    && (! menuItem.getText().equals("Merge World..."))){
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        eventLogger.logEvent(new EventVO(key).addTimestamp());
                    }
                });
            }
        }
        for (MenuElement subElement: menuElement.getSubElements()) {
            if (subElement instanceof JPopupMenu) {
                addStatisticsTo(subElement, key, eventLogger);
            } else if (subElement instanceof JMenuItem) {
                addStatisticsTo(subElement, key + "." + ((JMenuItem) subElement).getText().replaceAll("[ \\t\\n\\x0B\\f\\r\\.]", ""), eventLogger);
            }
        }
    }
    
    private void addNether() {
        NewWorldDialog dialog = new NewWorldDialog(this, world.getName(), dimension.getSeed() + 1, DIM_NETHER, world.getMaxHeight());
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            if (! dialog.checkMemoryRequirements(this)) {
                return;
            }
            try {
                Dimension nether = dialog.getSelectedDimension(null);
                world.addDimension(nether);
                setDimension(nether);
                setDimensionControlStates();
                DimensionPropertiesDialog propertiesDialog = new DimensionPropertiesDialog(this, nether, selectedColourScheme);
                propertiesDialog.setVisible(true);
            } catch (OperationCancelled e) {
                throw new RuntimeException("Operation cancelled by user", e);
            }
        }
    }
    
    private void addEnd() {
        NewWorldDialog dialog = new NewWorldDialog(this, world.getName(), dimension.getSeed() + 1, DIM_END, world.getMaxHeight());
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            if (! dialog.checkMemoryRequirements(this)) {
                return;
            }
            try {
                Dimension end = dialog.getSelectedDimension(null);
                world.addDimension(end);
                setDimension(end);
                setDimensionControlStates();
            } catch (OperationCancelled e) {
                throw new RuntimeException("Operation cancelled by user", e);
            }
        }
    }
    
    private void viewDimension(int dim) {
        if (dim != dimension.getDim()) {
            setDimension(world.getDimension(dim));
            setDimensionControlStates();
        }
    }
    
    private void fixLabelSizes() {
        locationLabel.setMinimumSize(locationLabel.getSize());
        locationLabel.setPreferredSize(locationLabel.getSize());
        locationLabel.setMaximumSize(locationLabel.getSize());
        heightLabel.setMinimumSize(heightLabel.getSize());
        heightLabel.setPreferredSize(heightLabel.getSize());
        heightLabel.setMaximumSize(heightLabel.getSize());
        materialLabel.setMinimumSize(materialLabel.getSize());
        materialLabel.setPreferredSize(materialLabel.getSize());
        materialLabel.setMaximumSize(materialLabel.getSize());
        waterLabel.setMinimumSize(waterLabel.getSize());
        waterLabel.setPreferredSize(waterLabel.getSize());
        waterLabel.setMaximumSize(waterLabel.getSize());
        biomeLabel.setMinimumSize(biomeLabel.getSize());
        biomeLabel.setPreferredSize(biomeLabel.getSize());
        biomeLabel.setMaximumSize(biomeLabel.getSize());
        radiusLabel.setMinimumSize(radiusLabel.getSize());
        radiusLabel.setPreferredSize(radiusLabel.getSize());
        radiusLabel.setMaximumSize(radiusLabel.getSize());
        zoomLabel.setMinimumSize(zoomLabel.getSize());
        zoomLabel.setPreferredSize(zoomLabel.getSize());
        zoomLabel.setMaximumSize(zoomLabel.getSize());

        locationLabel.setText(strings.getString("location-"));
        heightLabel.setText(MessageFormat.format(strings.getString("height.of.0"), '?'));
        waterLabel.setText(null);
        biomeLabel.setText(strings.getString("biome-"));
        materialLabel.setText(strings.getString("material-"));
        radiusLabel.setText(MessageFormat.format(strings.getString("radius.0"), radius));
    }

    private JComponent createButtonForOperation(Operation operation) {
        return createButtonForOperation(operation, null, (char) 0);
    }

    private JComponent createButtonForOperation(Operation operation, @NonNls String iconName) {
        return createButtonForOperation(operation, iconName, (char) 0);
    }

    private JComponent createButtonForOperation(Operation operation, @NonNls String iconName, char mnemonic) {
        return createButtonForOperation(operation, iconName, mnemonic, false);
    }

//    private JComponent createButtonForOperation(Operation operation, String iconName, boolean layerCheckbox) {
//        return createButtonForOperation(operation, iconName, (char) 0, layerCheckbox);
//    }

    private JComponent createButtonForOperation(final Operation operation, @NonNls String iconName, char mnemonic, boolean layerCheckbox) {
        return createButtonForOperation(operation, iconName, mnemonic, layerCheckbox, true);
    }
    
    private JComponent createButtonForOperation(final Operation operation, Icon icon, char mnemonic, boolean layerCheckbox) {
        return createButtonForOperation(operation, icon, mnemonic, layerCheckbox, true);
    }
    
    private JComponent createButtonForOperation(final Operation operation, String iconName, char mnemonic, boolean layerCheckbox, boolean checkboxEnabled) {
        Icon icon;
        if (iconName != null) {
            icon = loadIcon(operation, iconName);
        } else if (operation instanceof TerrainOperation) {
            icon = new ImageIcon(((TerrainOperation) operation).getTerrain().getIcon(selectedColourScheme));
        } else {
            icon = null;
        }
        return createButtonForOperation(operation, icon, mnemonic, layerCheckbox, checkboxEnabled);
    }
    
    private JComponent createButtonForOperation(final Operation operation, Icon icon, char mnemonic, boolean layerCheckbox, boolean checkboxEnabled) {
        boolean readOnlyOperation = (operation instanceof LayerPaint) && (((LayerPaint) operation).getLayer().equals(ReadOnly.INSTANCE));
        final JToggleButton button = new JToggleButton();
        if (readOnlyOperation) {
            readOnlyToggleButton = button;
        } else if (operation instanceof SetSpawnPoint) {
            setSpawnPointToggleButton = button;
        }
        button.setMargin(new Insets(2, 2, 2, 2));
        if (icon != null) {
            button.setIcon(icon);
        }
        if (operation.getName().equalsIgnoreCase(operation.getDescription())) {
            button.setToolTipText(operation.getName());
        } else {
            button.setToolTipText(operation.getName() + ": " + operation.getDescription());
        }
        if (mnemonic != 0) {
            button.setMnemonic(mnemonic);
        }
        button.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    if (operation instanceof RadiusOperation) {
                        view.setDrawRadius(false);
                        if (operation instanceof LayerPaint) {
                            Layer layer = ((LayerPaint) operation).getLayer();
                            if (hiddenLayers.contains(layer)) {
                                view.addHiddenLayer(layer);
                            }
                        }
                    }
                    if (operation instanceof BiomePaint) {
//                        try {
                            dockingManager.hideFrame("biomes");
//                        } catch (PropertyVetoException ex) {
//                            throw new RuntimeException(ex);
//                        }
//                        getContentPane().remove(biomesToolBar);
//                        App.this.validate(); // Doesn't happen automatically for some reason; Swing bug?
//                        getContentPane().repaint(); // Otherwise a ghost image is left behind for some reason; Swing bug?
                    } else if (operation instanceof Annotate) {
//                        try {
                            dockingManager.hideFrame("annotations");
//                            annotationsToolBar.setHidden(true);
//                        } catch (PropertyVetoException ex) {
//                            throw new RuntimeException(ex);
//                        }
//                        getContentPane().remove(annotationsToolBar);
//                        App.this.validate(); // Doesn't happen automatically for some reason; Swing bug?
//                        getContentPane().repaint(); // Otherwise a ghost image is left behind for some reason; Swing bug?
                    }
                    operation.setActive(false);
                    activeOperation = null;
                } else {
//                    if (undoManager != null) {
//                        undoManager.armSavePoint();
//                    }
                    if (operation instanceof MouseOrTabletOperation) {
                        if ((operation instanceof TerrainPaint) || (operation instanceof LayerPaint)) {
                            ((MouseOrTabletOperation) operation).setLevel(level);
                            programmaticChange = true;
                            try {
                                levelSlider.setValue((int) (level * 100));
                                brushRotationSlider.setValue(brushRotation);
                            } finally {
                                programmaticChange = false;
                            }
                            brushOptions.setFilter(filter);
                        } else {
                            ((MouseOrTabletOperation) operation).setLevel(toolLevel);
                            programmaticChange = true;
                            try {
                                levelSlider.setValue((int) (toolLevel * 100));
                                brushRotationSlider.setValue(toolBrushRotation);
                            } finally {
                                programmaticChange = false;
                            }
                            brushOptions.setFilter(toolFilter);
                        }
                        if (operation instanceof BiomePaint) {
//                            try  {
                                dockingManager.showFrame("biomes");
//                            } catch (PropertyVetoException ex) {
//                                throw new RuntimeException(ex);
//                            }
//                            biomesToolBar.setFloatable(! toolbarsLocked);
//                            getContentPane().add(biomesToolBar, BorderLayout.WEST);
//                            App.this.validate(); // Doesn't happen automatically for some reason; Swing bug?
//                            biomesToolBar.repaint(); // Otherwise it doesn't show up the second time it is displayed on Windows; yet another Swing bug?
                        } else if (operation instanceof Annotate) {
//                            try  {
                                dockingManager.showFrame("annotations");
//                            } catch (PropertyVetoException ex) {
//                                throw new RuntimeException(ex);
//                            }
//                            annotationsToolBar.setFloatable(! toolbarsLocked);
//                            getContentPane().add(annotationsToolBar, BorderLayout.WEST);
//                            App.this.validate(); // Doesn't happen automatically for some reason; Swing bug?
//                            annotationsToolBar.repaint(); // Otherwise it doesn't show up the second time it is displayed on Windows; yet another Swing bug?
                        }
                        if (operation instanceof RadiusOperation) {
                            view.setDrawRadius(true);
                            view.setRadius(radius);
                            ((RadiusOperation) operation).setRadius(radius);
                            programmaticChange = true;
                            try {
                                if ((operation instanceof TerrainPaint) || (operation instanceof LayerPaint)) {
                                    ((RadiusOperation) operation).setBrush(brushRotation == 0 ? brush : RotatedBrush.rotate(brush, brushRotation));
                                    ((RadiusOperation) operation).setFilter(filter);
    //                                ((RadiusOperation) operation).setBrushShape(brushShape);
                                    selectBrushButton(brush);
    //                                selectBrushShapeButton(brushShape);
    //                                levelSlider.setEnabled(true);
                                    view.setBrushShape(brush.getBrushShape());
                                    view.setBrushRotation(brushRotation);
                                } else {
                                    ((RadiusOperation) operation).setBrush(toolBrushRotation == 0 ? toolBrush : RotatedBrush.rotate(toolBrush, toolBrushRotation));
                                    ((RadiusOperation) operation).setFilter(toolFilter);
    //                                ((RadiusOperation) operation).setBrushShape(toolBrushShape);
                                    selectBrushButton(toolBrush);
    //                                selectBrushShapeButton(toolBrushShape);
    //                                levelSlider.setEnabled(false);
                                    view.setBrushShape(toolBrush.getBrushShape());
                                    view.setBrushRotation(toolBrushRotation);
                                }
                            } finally {
                                programmaticChange = false;
                            }
                        }
                    }
                    activeOperation = operation;
                    updateLayerVisibility();
                    updateBrushRotation();
                    operation.setActive(true);
                }
            }
        });
        toolButtonGroup.add(button);
        if (layerCheckbox) {
            JPanel panel = new JPanel();
            FlowLayout layout = (FlowLayout) panel.getLayout();
            layout.setHgap(0);
            layout.setVgap(0);
            layout.setAlignment(FlowLayout.LEFT);
            final JCheckBox checkBox = new JCheckBox();
            if (readOnlyOperation) {
                readOnlyCheckBox = checkBox;
            }
            checkBox.setToolTipText(strings.getString("whether.or.not.to.display.this.layer"));
            final Layer layer = ((LayerPaint) operation).getLayer();
            checkBox.setSelected(true);
            if (checkboxEnabled) {
                checkBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (checkBox.isSelected()) {
                            hiddenLayers.remove(layer);
                        } else {
                            hiddenLayers.add(layer);
                        }
                        updateLayerVisibility();
                    }
                });
            } else {
                checkBox.setEnabled(false);
            }
            panel.add(checkBox);
            
            final JCheckBox soloCheckBox = new JCheckBox();
            if (readOnlyOperation) {
                readOnlySoloCheckBox = soloCheckBox;
            }
            layerSoloCheckBoxes.put(layer, soloCheckBox);
            soloCheckBox.setToolTipText("<html>Check to show <em>only</em> this layer (the other layers are still exported)</html>");
            if (checkboxEnabled) {
                soloCheckBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (soloCheckBox.isSelected()) {
                            for (JCheckBox otherSoloCheckBox: layerSoloCheckBoxes.values()) {
                                if (otherSoloCheckBox != soloCheckBox) {
                                    otherSoloCheckBox.setSelected(false);
                                }
                            }
                            soloLayer = layer;
                        } else {
                            soloLayer = null;
                        }
                        updateLayerVisibility();
                    }
                });
            } else {
                soloCheckBox.setEnabled(false);
            }
            panel.add(soloCheckBox);
            
            button.setText(operation.getName());
            panel.add(button);
            return panel;
        } else {
            return button;
        }
    }

    /**
     * Configure the view to show the correct layers
     */
    private void updateLayerVisibility() {
        // Get the currently hidden layers
        Set<Layer> viewHiddenLayers = view.getHiddenLayers();
        
        // Determine which layers should be hidden
        Set<Layer> targetHiddenLayers = new HashSet<Layer>();
        // The FloodWithLava layer should *always* be hidden
        targetHiddenLayers.add(FloodWithLava.INSTANCE);
        if (soloLayer != null) {
            // Only the solo layer and the active layer (if there is one and it
            // is different than the solo layer) should be visible
            targetHiddenLayers.addAll((dimension != null) ? dimension.getAllLayers(true) : new HashSet<Layer>(layers));
            targetHiddenLayers.remove(soloLayer);
        } else {
            // The layers marked as hidden should be invisible, except the
            // currently active one, if any
            targetHiddenLayers.addAll(hiddenLayers);
        }
        // The currently active layer, if any, should always be visible
        if (activeOperation instanceof LayerPaint) {
            targetHiddenLayers.remove(((LayerPaint) activeOperation).getLayer());
        }
        
        // Hide the selected layers
        for (Layer hiddenLayer: targetHiddenLayers) {
            if (! viewHiddenLayers.contains(hiddenLayer)) {
                view.addHiddenLayer(hiddenLayer);
            }
        }
        for (Layer hiddenLayer: viewHiddenLayers) {
            if (! targetHiddenLayers.contains(hiddenLayer)) {
                view.removeHiddenLayer(hiddenLayer);
            }
        }
        
        // Configure the glass pane to show the right icons
        glassPane.setHiddenLayers(hiddenLayers);
        glassPane.setSoloLayer(soloLayer);
    }
    
    private void selectBrushButton(Brush brush) {
        brushButtons.get(brush).setSelected(true);
    }

//    private void selectBrushShapeButton(BrushShape brushShape) {
//        brushShapeButtons.get(brushShape).setSelected(true);
//    }

    private JComponent createBrushButton(final Brush brush) {
        final JToggleButton button = new JToggleButton(createBrushIcon(brush, 0));
        button.setMargin(new Insets(2, 2, 2, 2));
        button.setToolTipText(brush.getName());
        button.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if ((! programmaticChange) && (e.getStateChange() == ItemEvent.SELECTED)) {
                    int effectiveRotation;
                    if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                        App.this.brush = brush;
                        effectiveRotation = brushRotation;
                    } else {
                        toolBrush = brush;
                        effectiveRotation = toolBrushRotation;
                    }
                    if (activeOperation instanceof RadiusOperation) {
                        ((RadiusOperation) activeOperation).setBrush((effectiveRotation == 0) ? brush : RotatedBrush.rotate(brush, effectiveRotation));
                    }
                    view.setBrushShape(brush.getBrushShape());
                }
            }
        });
        brushButtonGroup.add(button);
        brushButtons.put(brush, button);
        return button;
    }
    
    private Icon createBrushIcon(Brush brush, int degrees) {
        brush = brush.clone();
        brush.setRadius(15);
        if (degrees != 0) {
            brush = RotatedBrush.rotate(brush, degrees);
        }
        return new ImageIcon(createBrushImage(brush));
    }

    private BufferedImage createBrushImage(Brush brush) {
        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(32, 32, Transparency.TRANSLUCENT);
        for (int dx = -15; dx <= 15; dx++) {
            for (int dy = -15; dy <= 15; dy++) {
                float strength = brush.getFullStrength(dx, dy);
                int alpha = (int) (strength * 255f + 0.5f);
                image.setRGB(dx + 15, dy + 15, alpha << 24);
            }
        }
        return image;
    }
    
//    private JPanel createBrushShapeButton(final BrushShape brushShape, String name, String iconName) {
//        final JRadioButton button = new JRadioButton();
//        button.setToolTipText(name);
//        button.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                if ((! programmaticChange) && (e.getStateChange() == ItemEvent.SELECTED)) {
//                    if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
//                        App.this.brushShape = brushShape;
//                    } else  {
//                        toolBrushShape = brushShape;
//                    }
//                    if (activeOperation instanceof RadiusOperation) {
//                        ((RadiusOperation) activeOperation).setBrushShape(brushShape);
//                    }
//                    view.setBrushShape(brushShape);
//                }
//            }
//        });
//        brushShapeButtonGroup.add(button);
//        brushShapeButtons.put(brushShape, button);
//        JPanel panel = new JPanel();
//        ((FlowLayout) panel.getLayout()).setHgap(0);
//        ((FlowLayout) panel.getLayout()).setVgap(0);
//        panel.add(button);
//        if (iconName != null) {
//            JLabel label = new JLabel(loadIcon(iconName));
//            label.setLabelFor(button);
//            label.setToolTipText(name);
//            panel.add(label);
//        }
//        return panel;
//    }

    private void scroll(JScrollBar scrollBar, int amount) {
        int oldValue = scrollBar.getValue();
        int minimum = scrollBar.getMinimum();
        int maximum = scrollBar.getMaximum();
        int newValue = oldValue + amount;
        if (newValue < minimum) {
            newValue = minimum;
        } else if (newValue > maximum) {
            newValue = maximum;
        }
        if (newValue != oldValue) {
            scrollBar.setValue(newValue);
        }
    }

    private static Icon loadIcon(@NonNls String name) {
        return IconUtils.loadIcon("org/pepsoft/worldpainter/icons/" + name + ".png");
    }
    
    private static Icon loadIcon(Object plugin, String name) {
        return IconUtils.loadIcon(plugin.getClass().getClassLoader(), "org/pepsoft/worldpainter/icons/" + name + ".png");
    }
    
    private void enableImportedWorldOperation() {
        if (! alwaysEnableReadOnly) {
            readOnlyCheckBox.setEnabled(true);
            readOnlyToggleButton.setEnabled(true);
            readOnlySoloCheckBox.setEnabled(true);
        }
    }

    private void disableImportedWorldOperation() {
        if (! alwaysEnableReadOnly) {
            readOnlyCheckBox.setEnabled(false);
            readOnlyToggleButton.setEnabled(false);
            readOnlySoloCheckBox.setEnabled(false);
            if (readOnlySoloCheckBox.isSelected()) {
                readOnlySoloCheckBox.setSelected(false);
                soloLayer = null;
                updateLayerVisibility();
            }
        }
    }
    
    private World2 migrate(Object object) {
        if (object instanceof World) {
            World oldWorld = (World) object;
            World2 newWorld = new World2(oldWorld.getMinecraftSeed(), oldWorld.getTileFactory(), 128);
            newWorld.setCreateGoodiesChest(oldWorld.isCreateGoodiesChest());
            newWorld.setImportedFrom(oldWorld.getImportedFrom());
            newWorld.setName(oldWorld.getName());
            newWorld.setSpawnPoint(oldWorld.getSpawnPoint());
            Dimension dim0 = newWorld.getDimension(0);
            Generator generator = Generator.DEFAULT;
            TileFactory tileFactory = dim0.getTileFactory();
            if ((tileFactory instanceof HeightMapTileFactory)
                    && (((HeightMapTileFactory) tileFactory).getWaterHeight() < 32)
                    && (((HeightMapTileFactory) tileFactory).getBaseHeight() < 32)) {
                // Low level
                generator = Generator.FLAT;
            }
            newWorld.setGenerator(generator);
            newWorld.setAskToConvertToAnvil(true);
            newWorld.setUpIs(Direction.WEST);
            newWorld.setAskToRotate(true);
            newWorld.setAllowMerging(false);
            dim0.setEventsInhibited(true);
            try {
                dim0.setBedrockWall(oldWorld.isBedrockWall());
                dim0.setBorder((oldWorld.getBorder() != null) ? Dimension.Border.valueOf(oldWorld.getBorder().name()) : null);
                dim0.setDarkLevel(oldWorld.isDarkLevel());
                for (Map.Entry<Layer, ExporterSettings> entry: oldWorld.getAllLayerSettings().entrySet()) {
                    dim0.setLayerSettings(entry.getKey(), entry.getValue());
                }
                dim0.setMinecraftSeed(oldWorld.getMinecraftSeed());
                dim0.setPopulate(oldWorld.isPopulate());
                dim0.setContoursEnabled(false);
                Terrain subsurfaceMaterial = oldWorld.getSubsurfaceMaterial();
                ResourcesExporterSettings resourcesSettings = (ResourcesExporterSettings) dim0.getLayerSettings(Resources.INSTANCE);
                if (subsurfaceMaterial == Terrain.RESOURCES) {
                    dim0.setSubsurfaceMaterial(Terrain.STONE);
                } else {
                    dim0.setSubsurfaceMaterial(subsurfaceMaterial);
                    resourcesSettings.setMinimumLevel(0);
                }
                
                // Load legacy settings
                resourcesSettings.setChance(BLK_GOLD_ORE,         1);
                resourcesSettings.setChance(BLK_IRON_ORE,         5);
                resourcesSettings.setChance(BLK_COAL,             9);
                resourcesSettings.setChance(BLK_LAPIS_LAZULI_ORE, 1);
                resourcesSettings.setChance(BLK_DIAMOND_ORE,      1);
                resourcesSettings.setChance(BLK_REDSTONE_ORE,     6);
                resourcesSettings.setChance(BLK_WATER,            1);
                resourcesSettings.setChance(BLK_LAVA,             1);
                resourcesSettings.setChance(BLK_DIRT,             9);
                resourcesSettings.setChance(BLK_GRAVEL,           9);
                resourcesSettings.setChance(BLK_EMERALD_ORE,      0);
                resourcesSettings.setMaxLevel(BLK_GOLD_ORE,         Terrain.GOLD_LEVEL);
                resourcesSettings.setMaxLevel(BLK_IRON_ORE,         Terrain.IRON_LEVEL);
                resourcesSettings.setMaxLevel(BLK_COAL,             Terrain.COAL_LEVEL);
                resourcesSettings.setMaxLevel(BLK_LAPIS_LAZULI_ORE, Terrain.LAPIS_LAZULI_LEVEL);
                resourcesSettings.setMaxLevel(BLK_DIAMOND_ORE,      Terrain.DIAMOND_LEVEL);
                resourcesSettings.setMaxLevel(BLK_REDSTONE_ORE,     Terrain.REDSTONE_LEVEL);
                resourcesSettings.setMaxLevel(BLK_WATER,            Terrain.WATER_LEVEL);
                resourcesSettings.setMaxLevel(BLK_LAVA,             Terrain.LAVA_LEVEL);
                resourcesSettings.setMaxLevel(BLK_DIRT,             Terrain.DIRT_LEVEL);
                resourcesSettings.setMaxLevel(BLK_GRAVEL,           Terrain.GRAVEL_LEVEL);
                resourcesSettings.setMaxLevel(BLK_EMERALD_ORE,      Terrain.GOLD_LEVEL);
            
                for (Tile tile: oldWorld.getTiles()) {
                    dim0.addTile(tile);
                }
            } finally {
                dim0.setEventsInhibited(false);
            }
            newWorld.setDirty(false);

            // Log event
            Configuration config = Configuration.getInstance();
            if (config != null) {
                config.logEvent(new EventVO(EVENT_KEY_ACTION_MIGRATE_WORLD).addTimestamp());
            }

            return newWorld;
        } else {
            throw new IllegalArgumentException("Save file format not supported");
        }
    }
    
    private void setDimensionControlStates() {
        boolean imported = (world != null) && (world.getImportedFrom() != null);
        boolean nether = (world != null) && (world.getDimension(DIM_NETHER) != null);
        boolean end = (world != null) && (world.getDimension(DIM_END) != null);
        addNetherMenuItem.setEnabled((! imported) && (! nether));
        addEndMenuItem.setEnabled((! imported) && (! end));
        viewSurfaceMenuItem.setEnabled(nether || end);
        viewNetherMenuItem.setEnabled(nether);
        viewEndMenuItem.setEnabled(end);
        if (dimension != null) {
            switch (dimension.getDim()) {
                case DIM_NORMAL:
                    setSpawnPointToggleButton.setEnabled(true);
                    ACTION_MOVE_TO_SPAWN.setEnabled(true);
                    break;
                case DIM_NETHER:
                case DIM_END:
                    if (activeOperation instanceof SetSpawnPoint) {
                        toolButtonGroup.clearSelection();
                    }
                    setSpawnPointToggleButton.setEnabled(false);
                    ACTION_MOVE_TO_SPAWN.setEnabled(false);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    private void addMaterialSelectionTo(final JToggleButton button, final int customMaterialIndex) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
            
            private void showPopup(MouseEvent e) {
                JPopupMenu popupMenu = new JPopupMenu();
                MixedMaterial material = Terrain.getCustomMaterial(customMaterialIndex);
                JLabel label = new JLabel(MessageFormat.format(strings.getString("current.material.0"), (material != null) ? material : "none"));
                popupMenu.add(label);
                
                JMenuItem menuItem = new JMenuItem(strings.getString("select.custom.material") + "...");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (editCustomMaterial(customMaterialIndex)) {
                            button.setSelected(true);
                        }
                    }
                });
                popupMenu.add(menuItem);
                
                menuItem = new JMenuItem("Import from file...");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (importCustomMaterial(customMaterialIndex)) {
                            button.setSelected(true);
                        }
                    }
                });
                popupMenu.add(menuItem);
                
                if (material != null) {
                    menuItem = new JMenuItem("Export to file...");
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            exportCustomMaterial(customMaterialIndex);
                        }
                    });
                    popupMenu.add(menuItem);
                }
                
                popupMenu.show(button, e.getX(), e.getY());
            }
        });
    }

    private boolean importCustomMaterial(int customMaterialIndex) {
        Configuration config = Configuration.getInstance();
        File terrainDirectory = config.getTerrainDirectory();
        if ((terrainDirectory == null) || (! terrainDirectory.isDirectory())) {
            terrainDirectory = DesktopUtils.getDocumentsFolder();
        }
        JFileChooser fileChooser = new JFileChooser(terrainDirectory);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".terrain");
            }

            @Override
            public String getDescription() {
                return "WorldPainter Custom Terrains (*.terrain)";
            }
        });
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(selectedFile))));
                try {
                    MixedMaterial customMaterial = (MixedMaterial) in.readObject();
                    Terrain.setCustomMaterial(customMaterialIndex, customMaterial);
                    customMaterialButtons[customMaterialIndex].setIcon(new ImageIcon(customMaterial.getIcon(selectedColourScheme)));
                    customMaterialButtons[customMaterialIndex].setToolTipText(MessageFormat.format(strings.getString("customMaterial.0.right.click.to.change"), customMaterial));
                    view.refreshTiles();
                    return true;
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("I/O error while reading " + selectedFile, e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found exception while reading " + selectedFile, e);
            }
        }
        return false;
    }

    private void exportCustomMaterial(int customMaterialIndex) {
        Configuration config = Configuration.getInstance();
        File terrainDirectory = config.getTerrainDirectory();
        if ((terrainDirectory == null) || (! terrainDirectory.isDirectory())) {
            terrainDirectory = DesktopUtils.getDocumentsFolder();
        }
        MixedMaterial material = Terrain.getCustomMaterial(customMaterialIndex);
        File selectedFile = new File(terrainDirectory, FileUtils.sanitiseName(material.getName()) + ".terrain");
        JFileChooser fileChooser = new JFileChooser(terrainDirectory);
        fileChooser.setSelectedFile(selectedFile);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".terrain");
            }

            @Override
            public String getDescription() {
                return "WorldPainter Custom Terrains (*.terrain)";
            }
        });
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            if (! selectedFile.getName().toLowerCase().endsWith(".terrain")) {
                selectedFile = new File(selectedFile.getPath() + ".terrain");
            }
            if (selectedFile.isFile() && (JOptionPane.showConfirmDialog(this, "The file " + selectedFile.getName() + " already exists.\nDo you want to overwrite it?", "Overwrite File", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)) {
                return;
            }
            try {
                ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(selectedFile))));
                try {
                    out.writeObject(material);
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("I/O error while trying to write " + selectedFile, e);
            }
            config.setTerrainDirectory(selectedFile.getParentFile());
            JOptionPane.showMessageDialog(this, "Custom terrain " + material.getName() + " exported successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadCustomMaterials() {
        for (int i = 0; i < Terrain.CUSTOM_TERRAIN_COUNT; i++) {
            MixedMaterial material = world.getMixedMaterial(i);
            Terrain.setCustomMaterial(i, material);
            if (material != null) {
                customMaterialButtons[i].setIcon(new ImageIcon(material.getIcon(selectedColourScheme)));
                customMaterialButtons[i].setToolTipText(MessageFormat.format(strings.getString("customMaterial.0.right.click.to.change"), material));
            } else {
                customMaterialButtons[i].setIcon(ICON_UNKNOWN_PATTERN);
                customMaterialButtons[i].setToolTipText(strings.getString("not.set.click.to.set"));
                if (customMaterialButtons[i].isSelected()) {
                    toolButtonGroup.clearSelection();
                }
            }
        }
    }

    private void saveCustomMaterials() {
        for (int i = 0; i < Terrain.CUSTOM_TERRAIN_COUNT; i++) {
            world.setMixedMaterial(i, Terrain.getCustomMaterial(i));
        }
    }
    
    private void saveCustomBiomes() {
        if (dimension != null) {
            dimension.setCustomBiomes(customBiomeManager.getCustomBiomes());
        }
    }
    
    private void installMacCustomisations() {
        try {
            Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
            Method getApplicationMethod = applicationClass.getMethod("getApplication");
            Object application = getApplicationMethod.invoke(null);
            try {
                Class<?> quitHandlerClass = Class.forName("com.apple.eawt.QuitHandler");
                Class<?> quitResponseClass = Class.forName("com.apple.eawt.QuitResponse");
                final Method cancelQuitMethod = quitResponseClass.getMethod("cancelQuit");
                Object quitHandlerProxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[] {quitHandlerClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // QuitHandler has only one method, so no need to check
                        // the method or arguments
                        exit();
                        // If we get here the user cancelled closing the program
                        cancelQuitMethod.invoke(args[1]);
                        return null;
                    }
                });
                Method setQuitHandlerMethod = applicationClass.getMethod("setQuitHandler", quitHandlerClass);
                setQuitHandlerMethod.invoke(application, quitHandlerProxy);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                // No quit handler support. Oh well...
            }
            try {
                Class<?> aboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler");
                Object aboutHandlerProxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[] {aboutHandlerClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        AboutDialog dialog = new AboutDialog(App.this, world, view, undoManager);
                        dialog.setVisible(true);
                        return null;
                    }
                });
                Method setAboutHandlerMethod = applicationClass.getMethod("setAboutHandler", aboutHandlerClass);
                setAboutHandlerMethod.invoke(application, aboutHandlerProxy);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                // No about handler support. Oh well...
            }
            try {
                Class<?> openFilesHandlerClass = Class.forName("com.apple.eawt.OpenFilesHandler");
                Class<?> openFilesEventClass = Class.forName("com.apple.eawt.AppEvent$OpenFilesEvent");
                final Method getFilesMethod = openFilesEventClass.getMethod("getFiles");
                Object openFilesHandlerProxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[] {openFilesHandlerClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        @SuppressWarnings("unchecked") // Guaranteed by Apple/Java
                        List<File> files = (List<File>) getFilesMethod.invoke(args[0]);
                        if (files.size() > 0) {
                            open(files.get(0), true);
                        }
                        return null;
                    }
                });
                Method setOpenFilesHandlerMethod = applicationClass.getMethod("setOpenFileHandler", openFilesHandlerClass);
                setOpenFilesHandlerMethod.invoke(application, openFilesHandlerProxy);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                // No open files handler support. Oh well...
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // This means we're not on a Mac, so just ignore it
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void showGlobalOperations() {
        Set<Layer> allLayers = getAllLayers();
        List<Integer> allBiomes = new ArrayList<Integer>();
        final int biomeCount = autoBiomeScheme.getBiomeCount();
        for (int biome = 0; biome < biomeCount; biome++) {
            if (autoBiomeScheme.isBiomePresent(biome)) {
                allBiomes.add(biome);
            }
        }
        if (customBiomeManager.getCustomBiomes() != null) {
            for (CustomBiome customBiome: customBiomeManager.getCustomBiomes()) {
                allBiomes.add(customBiome.getId());
            }
        }
        FillDialog dialog = new FillDialog(App.this, dimension, allLayers.toArray(new Layer[allLayers.size()]), selectedColourScheme, allBiomes.toArray(new Integer[allBiomes.size()]), customBiomeManager);
        dialog.setVisible(true);
    }

    private void exportImage() {
        JFileChooser fileChooser = new JFileChooser();
        final Set<String> extensions = new HashSet<String>(Arrays.asList(ImageIO.getReaderFileSuffixes()));
        StringBuilder sb = new StringBuilder(strings.getString("supported.image.formats"));
        sb.append(" (");
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
        String defaultname = world.getName().replaceAll("\\s", "").toLowerCase() + ((dimension.getDim() == DIM_NORMAL) ? "" : ("_" + dimension.getName().toLowerCase())) + ".png"; // NOI18N
        fileChooser.setSelectedFile(new File(defaultname));
        if (fileChooser.showSaveDialog(App.this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            final String type;
            int p = selectedFile.getName().lastIndexOf('.');
            if (p != -1) {
                type = selectedFile.getName().substring(p + 1).toUpperCase();
            } else {
                type = "PNG"; // NOI18N
                selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
            }
            if (selectedFile.exists()) {
                if (JOptionPane.showConfirmDialog(App.this, strings.getString("the.file.already.exists"), strings.getString("overwrite.file"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            final File file = selectedFile;
            if (! ProgressDialog.executeTask(App.this, new ProgressTask<Boolean>() {
                        @Override
                        public String getName() {
                            return strings.getString("exporting.image");
                        }

                        @Override
                        public Boolean execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                            try {
                                return ImageIO.write(view.getImage(), type, file);
                            } catch (IOException e) {
                                throw new RuntimeException("I/O error while exporting image", e);
                            }
                        }
                    }, false)) {
                JOptionPane.showMessageDialog(App.this, MessageFormat.format(strings.getString("format.0.not.supported"), type));
            }
        }
    }
    
    private void exportHeightMap() {
        JFileChooser fileChooser = new JFileChooser();
        final Set<String> extensions = new HashSet<String>(Arrays.asList(ImageIO.getReaderFileSuffixes()));
        StringBuilder sb = new StringBuilder(strings.getString("supported.image.formats"));
        sb.append(" (");
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
        String defaultname = world.getName().replaceAll("\\s", "").toLowerCase() + ((dimension.getDim() == DIM_NORMAL) ? "" : ("_" + dimension.getName().toLowerCase())) + "_heightmap.png"; // NOI18N
        fileChooser.setSelectedFile(new File(defaultname));
        if (fileChooser.showSaveDialog(App.this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            final String type;
            int p = selectedFile.getName().lastIndexOf('.');
            if (p != -1) {
                type = selectedFile.getName().substring(p + 1).toUpperCase();
            } else {
                type = "PNG"; // NOI18N
                selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
            }
            if (selectedFile.exists()) {
                if (JOptionPane.showConfirmDialog(App.this, strings.getString("the.file.already.exists"), strings.getString("overwrite.file"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            final File file = selectedFile;
            if (! ProgressDialog.executeTask(App.this, new ProgressTask<Boolean>() {
                        @Override
                        public String getName() {
                            return strings.getString("exporting.height.map");
                        }

                        @Override
                        public Boolean execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                            try {
                                BufferedImage image = new BufferedImage(dimension.getWidth() * TILE_SIZE, dimension.getHeight() * TILE_SIZE, (dimension.getMaxHeight() <= 256) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_USHORT_GRAY);
                                WritableRaster raster = image.getRaster();
                                for (Tile tile: dimension.getTiles()) {
                                    int tileOffsetX = (tile.getX() - dimension.getLowestX()) * TILE_SIZE;
                                    int tileOffsetY = (tile.getY() - dimension.getLowestY()) * TILE_SIZE;
                                    for (int dx = 0; dx < TILE_SIZE; dx++) {
                                        for (int dy = 0; dy < TILE_SIZE; dy++) {
                                            int height = tile.getIntHeight(dx, dy);
                                            raster.setSample(tileOffsetX + dx, tileOffsetY + dy, 0, height);
                                        }
                                    }
                                }
                                return ImageIO.write(image, type, file);
                            } catch (IOException e) {
                                throw new RuntimeException("I/O error while exporting image", e);
                            }
                        }
                    }, false)) {
                JOptionPane.showMessageDialog(App.this, MessageFormat.format(strings.getString("format.0.not.supported"), type));
            }
        }
    }
    
    private void importLayers(String paletteName) {
        Configuration config = Configuration.getInstance();
        File layerDirectory = config.getLayerDirectory();
        if ((layerDirectory == null) || (! layerDirectory.isDirectory())) {
            layerDirectory = DesktopUtils.getDocumentsFolder();
        }
        JFileChooser fileChooser = new JFileChooser(layerDirectory);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".layer");
            }

            @Override
            public String getDescription() {
                return "WorldPainter Custom Layers (*.layer)";
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            boolean updateCustomTerrainButtons = false;
            for (File selectedFile: selectedFiles) {
                try {
                    ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(selectedFile))));
                    try {
                        CustomLayer layer = (CustomLayer) in.readObject();
                        for (Layer existingLayer: getCustomLayers()) {
                            if (layer.equals(existingLayer)) {
                                JOptionPane.showMessageDialog(this, "That layer is already present in the dimension.\nThe layer has not been added.", "Layer Already Present", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                        if (paletteName != null) {
                            layer.setPalette(paletteName);
                        }
                        registerCustomLayer(layer, true);
                        if (layer instanceof CombinedLayer) {
                            CombinedLayer combinedLayer = (CombinedLayer) layer;
                            addLayersFromCombinedLayer(combinedLayer);
                            if (combinedLayer.isMissingTerrainWarning()) {
                                JOptionPane.showMessageDialog(this, "The layer contained a Custom Terrain which is not present in this world. The terrain has been reset.", "Missing Custom Terrain", JOptionPane.WARNING_MESSAGE);
                                combinedLayer.resetMissingTerrainWarning();
                            } else if ((combinedLayer.getTerrain() != null) && combinedLayer.getTerrain().isCustom()) {
                                updateCustomTerrainButtons = true;
                            }
                        }
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("I/O error while reading " + selectedFile, e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Class not found exception while reading " + selectedFile, e);
                }
            }
            if (updateCustomTerrainButtons) {
                updateCustomTerrainButtons();
            }
        }        
    }
    
    private void updateCustomTerrainButtons() {
        for (int i = 0; i < Terrain.CUSTOM_TERRAIN_COUNT; i++) {
            if (Terrain.getCustomMaterial(i) != null) {
                MixedMaterial material = Terrain.getCustomMaterial(i);
                customMaterialButtons[i].setIcon(new ImageIcon(material.getIcon(selectedColourScheme)));
                customMaterialButtons[i].setToolTipText(MessageFormat.format(strings.getString("customMaterial.0.right.click.to.change"), material));
            } else {
                customMaterialButtons[i].setIcon(ICON_UNKNOWN_PATTERN);
                customMaterialButtons[i].setToolTipText(strings.getString("not.set.click.to.set"));
            }
        }
    }
    
    private void addLayersFromCombinedLayer(CombinedLayer combinedLayer) {
        for (Layer layer: combinedLayer.getLayers()) {
            if ((layer instanceof CustomLayer) && (! paletteManager.contains(layer)) && (! layersWithNoButton.contains(layer))) {
                CustomLayer customLayer = (CustomLayer) layer;
                if (customLayer.isHide()) {
                    layersWithNoButton.add(customLayer);
                } else {
                    registerCustomLayer((CustomLayer) customLayer, false);
                }
                if (layer instanceof CombinedLayer) {
                    addLayersFromCombinedLayer((CombinedLayer) customLayer);
                }
            }
        }
    }
    
    private void exportLayer(CustomLayer layer) {
        Configuration config = Configuration.getInstance();
        File layerDirectory = config.getLayerDirectory();
        if ((layerDirectory == null) || (! layerDirectory.isDirectory())) {
            layerDirectory = DesktopUtils.getDocumentsFolder();
        }
        JFileChooser fileChooser = new JFileChooser(layerDirectory);
        fileChooser.setSelectedFile(new File(layerDirectory, FileUtils.sanitiseName(layer.getName()) + ".layer"));
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".layer");
            }

            @Override
            public String getDescription() {
                return "WorldPainter Custom Layers (*.layer)";
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (! selectedFile.getName().toLowerCase().endsWith(".layer")) {
                selectedFile = new File(selectedFile.getPath() + ".layer");
            }
            if (selectedFile.isFile() && (JOptionPane.showConfirmDialog(this, "The file " + selectedFile.getName() + " already exists.\nDo you want to overwrite it?", "Overwrite File", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)) {
                return;
            }
            try {
                ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(selectedFile))));
                try {
                    out.writeObject(layer);
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("I/O error while trying to write " + selectedFile, e);
            }
            config.setLayerDirectory(selectedFile.getParentFile());
            JOptionPane.showMessageDialog(this, "Layer " + layer.getName() + " exported successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void logLayers(Dimension dimension, EventVO event, @NonNls String prefix) {
        StringBuilder sb = new StringBuilder();
        for (Layer layer: dimension.getAllLayers(false)) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(layer.getName());
        }
        if (sb.length() > 0) {
            event.setAttribute(new AttributeKeyVO<String>(prefix + "layers"), sb.toString());
        }
    }

    static DockableFrame createDockableFrame(Component component, String title, int side, int index) {
        String id = Character.toLowerCase(title.charAt(0)) + title.substring(1);
        return createDockableFrame(component, id, title, side, index);
    }
    
    static DockableFrame createDockableFrame(Component component, String id, String title, int side, int index) {
        DockableFrame dockableFrame = new DockableFrame(id);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, constraints);
        constraints.weighty = 1.0;
        panel.add(new JPanel(), constraints);
        dockableFrame.add(panel, BorderLayout.CENTER);
        
        // Use title everywhere
        dockableFrame.setTitle(title);
        dockableFrame.setSideTitle(title);
        dockableFrame.setTabTitle(title);
        dockableFrame.setToolTipText(title);

        // Try to find an icon to use for the tab
        if (component instanceof Container) {
            Icon icon = findIcon((Container) component);
            if (icon != null) {
                if (((icon.getIconHeight() > 16) || (icon.getIconWidth() > 16))
                        && (icon instanceof ImageIcon)
                        && (((ImageIcon) icon).getImage() instanceof BufferedImage)) {
                    float s;
                    if (icon.getIconWidth() > icon.getIconHeight()) {
                        // Wide icon
                        s = 16f / icon.getIconWidth();
                    } else {
                        // Tall (or square) icon
                        s = 16f / icon.getIconHeight();
            }
                    BufferedImageOp op = new AffineTransformOp(AffineTransform.getScaleInstance(s, s), AffineTransformOp.TYPE_BICUBIC);
                    BufferedImage iconImage = op.filter((BufferedImage) ((ImageIcon) icon).getImage(), null);
                    icon = new ImageIcon(iconImage);
            }
                dockableFrame.setFrameIcon(icon);
            }
            }

        // Use preferred size of component as much as possible
        final java.awt.Dimension preferredSize = component.getPreferredSize();
        dockableFrame.setAutohideWidth(preferredSize.width);
        dockableFrame.setDockedWidth(preferredSize.width);
        dockableFrame.setDockedHeight(preferredSize.height);
        dockableFrame.setUndockedBounds(new Rectangle(-1, -1, preferredSize.width, preferredSize.height));

        // Make hidable, but don't display hide button, so incidental panels can
        // be hidden on the fly
        dockableFrame.setHidable(true);
        dockableFrame.setAvailableButtons(BUTTON_FLOATING | BUTTON_AUTOHIDE | BUTTON_HIDE_AUTOHIDE);
        dockableFrame.setShowContextMenu(false); // Disable the context menu because it contains the Close option with no way to hide it

        // Initial location of panel
        dockableFrame.setInitMode(DockContext.STATE_FRAMEDOCKED);
        dockableFrame.setInitSide(side);
        dockableFrame.setInitIndex(index);

        // Other flags
        dockableFrame.setAutohideWhenActive(true);
        dockableFrame.setMaximizable(false);
        return dockableFrame;
    }

    static Icon findIcon(Container container) {
        for (Component component: container.getComponents()) {
            if ((component instanceof AbstractButton) && (((AbstractButton) component).getIcon() != null)) {
                return ((AbstractButton) component).getIcon();
            } else if (component instanceof Container) {
                Icon icon = findIcon((Container) component);
                if (icon != null) {
                    return icon;
                }
            }
        }
        return null;
    }

    public final IntensityAction ACTION_INTENSITY_10_PERCENT  = new IntensityAction(  2, VK_1); // 9 so that it will round to level 1 for nibble sized layers
    public final IntensityAction ACTION_INTENSITY_20_PERCENT  = new IntensityAction( 16, VK_2);
    public final IntensityAction ACTION_INTENSITY_30_PERCENT  = new IntensityAction( 23, VK_3);
    public final IntensityAction ACTION_INTENSITY_40_PERCENT  = new IntensityAction( 37, VK_4);
    public final IntensityAction ACTION_INTENSITY_50_PERCENT  = new IntensityAction( 51, VK_5);
    public final IntensityAction ACTION_INTENSITY_60_PERCENT  = new IntensityAction( 58, VK_6);
    public final IntensityAction ACTION_INTENSITY_70_PERCENT  = new IntensityAction( 65, VK_7);
    public final IntensityAction ACTION_INTENSITY_80_PERCENT  = new IntensityAction( 79, VK_8);
    public final IntensityAction ACTION_INTENSITY_90_PERCENT  = new IntensityAction( 86, VK_9);
    public final IntensityAction ACTION_INTENSITY_100_PERCENT = new IntensityAction(100, VK_0);
    
    private final BetterAction ACTION_NEW_WORLD = new BetterAction("newWorld", strings.getString("new.world") + "...", ICON_NEW_WORLD, false) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_N, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("create.a.new.world"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            newWorld();
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_OPEN_WORLD = new BetterAction("openWorld", strings.getString("open.world") + "...", ICON_OPEN_WORLD, false) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_O, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("open.an.existing.worldpainter.world"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            open();
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_SAVE_WORLD = new BetterAction("saveWorld", strings.getString("save.world") + "...", ICON_SAVE_WORLD, false) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_S, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("save.the.world.as.a.worldpainter.file.to.the.previously.used.file"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            save();
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_SAVE_WORLD_AS = new BetterAction("saveWorldAs", strings.getString("save.world.as") + "...", ICON_SAVE_WORLD, false) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_S, PLATFORM_COMMAND_MASK | SHIFT_DOWN_MASK));
            setShortDescription(strings.getString("save.the.world.as.a.worldpainter.file"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            saveAs();
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_EXPORT_WORLD = new BetterAction("exportAsMinecraftMap", strings.getString("export.as.minecraft.map") + "...", ICON_EXPORT_WORLD, false) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_E, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("export.the.world.to.a.minecraft.map"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            if (world.getImportedFrom() != null) {
                Toolkit.getDefaultToolkit().beep();
                if (JOptionPane.showConfirmDialog(App.this, strings.getString("this.is.an.imported.world"), strings.getString("imported"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            ExportWorldDialog dialog = new ExportWorldDialog(App.this, world, autoBiomeScheme, selectedColourScheme, customBiomeManager, hiddenLayers, false, view.getLightOrigin(), view);
            dialog.setVisible(true);
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_IMPORT_MAP = new BetterAction("importMinecraftMap", strings.getString("existing.minecraft.map") + "...", false) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_I, PLATFORM_COMMAND_MASK));
            setShortDescription("Import the landscape of an existing Minecraft map. Use Merge to merge your changes.");
        }

        @Override
        public void performAction(ActionEvent e) {
            importWorld();
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_MERGE_WORLD = new BetterAction("mergeWorld", strings.getString("merge.world") + "...", false) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_R, PLATFORM_COMMAND_MASK));
            setShortDescription("Merge the changes in a previously Imported world back to the original Minecraft map.");
        }

        @Override
        public void performAction(ActionEvent e) {
            merge();
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_EXIT = new BetterAction("exit", strings.getString("exit") + "...", ICON_EXIT) {
        {
            setShortDescription(strings.getString("shut.down.worldpainter"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            exit();
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_ZOOM_IN = new BetterAction("zoomIn", strings.getString("zoom.in"), ICON_ZOOM_IN) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_ADD, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("zoom.in"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            Point location = view.getViewCentreInWorldCoords();
            zoom++;
            view.setZoom(zoom);
            updateZoomLabel();
            view.moveTo(location);
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_ZOOM_RESET = new BetterAction("resetZoom", strings.getString("reset.zoom"), ICON_ZOOM_RESET) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_0, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("reset.the.zoom.level.to.1.1"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            int oldZoom = zoom;
            zoom = 0;
            view.setZoom(zoom);
            updateZoomLabel();
            Point mousePosition = view.getMousePosition();
            if (mousePosition != null) {
                // TODO: this algorithm causes flashing, because the scrollpane already
                // repaints itself before it is scrolled. See if we can do something
                // about that
                double scale = Math.pow(2.0, zoom - oldZoom);
                final int horizontalDisplacement = (int) (mousePosition.x * scale) - mousePosition.x;
                final int verticalDisplacement = (int) (mousePosition.y * scale) - mousePosition.y;
                // Schedule this for later execution, after the scrollpane
                // has revalidated its contents and resized the scrollbars:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        scroll(scrollPane.getHorizontalScrollBar(), horizontalDisplacement);
                        scroll(scrollPane.getVerticalScrollBar(), verticalDisplacement);
                        view.refreshBrush();
                    }
                });
            }
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_ZOOM_OUT = new BetterAction("zoomOut", strings.getString("zoom.out"), ICON_ZOOM_OUT) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_SUBTRACT, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("zoom.out"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            Point location = view.getViewCentreInWorldCoords();
            zoom--;
            view.setZoom(zoom);
            updateZoomLabel();
            view.moveTo(location);
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_GRID = new BetterAction("grid", strings.getString("grid"), ICON_GRID) {
        {
            setShortDescription(strings.getString("enable.or.disable.the.grid"));
            setSelected(false);
        }
        
        @Override
        public void performAction(ActionEvent e) {
            view.setDrawGrid(! view.isDrawGrid());
            dimension.setGridEnabled(view.isDrawGrid());
            setSelected(view.isDrawGrid());
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_CONTOURS = new BetterAction("contours", strings.getString("contours"), ICON_CONTOURS) {
        {
            setShortDescription(strings.getString("enable.or.disable.height.contours"));
            setSelected(false);
        }
        
        @Override
        public void performAction(ActionEvent e) {
            view.setDrawContours(! view.isDrawContours());
            dimension.setContoursEnabled(view.isDrawContours());
            setSelected(view.isDrawContours());
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_OVERLAY = new BetterAction("overlay", strings.getString("overlay"), ICON_OVERLAY) {
        {
            setShortDescription(strings.getString("enable.or.disable.image.overlay"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            if ((! dimension.isOverlayEnabled()) && (dimension.getOverlay() == null)) {
                ConfigureViewDialog dialog = new ConfigureViewDialog(App.this, dimension, view, true);
                dialog.setVisible(true);
                setSelected(view.isDrawOverlay());
                ACTION_GRID.setSelected(view.isDrawGrid());
                ACTION_CONTOURS.setSelected(view.isDrawContours());
            } else {
                view.setDrawOverlay(! view.isDrawOverlay());
                dimension.setOverlayEnabled(view.isDrawOverlay());
                setSelected(view.isDrawOverlay());
            }
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_UNDO = new BetterAction("undo", strings.getString("undo"), ICON_UNDO) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_Z, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("undo.the.most.recent.action"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            if ((undoManager != null) && undoManager.undo()) {
                undoManager.armSavePoint();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_REDO = new BetterAction("redo", strings.getString("redo"), ICON_REDO) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_Y, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("redo.the.most.recent.action"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            if ((undoManager != null) && undoManager.redo()) {
                undoManager.armSavePoint();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_EDIT_TILES = new BetterAction("editTiles", strings.getString("add.remove.tiles") + "...", ICON_EDIT_TILES) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_T, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("add.or.remove.tiles"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            addRemoveTiles();
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_CHANGE_HEIGHT = new BetterAction("changeHeight", strings.getString("change.height") + "...", ICON_CHANGE_HEIGHT) {
        {
            setShortDescription(strings.getString("raise.or.lower.the.entire.map"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            ChangeHeightDialog dialog = new ChangeHeightDialog(App.this, world);
            dialog.setVisible(true);
            if (threeDeeFrame != null) {
                threeDeeFrame.refresh();
            }
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_ROTATE_WORLD = new BetterAction("rotate", strings.getString("rotate") + "...", ICON_ROTATE_WORLD) {
        {
            setShortDescription(strings.getString("rotate.the.entire.map.by.quarter.turns"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            if ((world.getImportedFrom() != null) && (JOptionPane.showConfirmDialog(App.this, strings.getString("this.world.was.imported.from.an.existing.map"), strings.getString("imported"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)) {
                return;
            }
            RotateWorldDialog dialog = new RotateWorldDialog(App.this, world);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                setWorld(dialog.getWorld());
                if (threeDeeFrame != null) {
                    threeDeeFrame.setDimension(dimension);
                }
            }
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_DIMENSION_PROPERTIES = new BetterAction("dimensionProperties", strings.getString("dimension.properties") + "...", ICON_DIMENSION_PROPERTIES) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_P, PLATFORM_COMMAND_MASK));
            setShortDescription(strings.getString("edit.the.properties.of.this.dimension"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            boolean previousCoverSteepTerrain = dimension.isCoverSteepTerrain();
            int previousTopLayerMinDepth = dimension.getTopLayerMinDepth();
            int previousTopLayerVariation = dimension.getTopLayerVariation();
            DimensionPropertiesDialog dialog = new DimensionPropertiesDialog(App.this, dimension, selectedColourScheme);
            dialog.setVisible(true);
            if ((dimension.isCoverSteepTerrain() != previousCoverSteepTerrain)
                    || (dimension.getTopLayerMinDepth() != previousTopLayerMinDepth)
                    || (dimension.getTopLayerVariation() != previousTopLayerVariation)) {
                if (threeDeeFrame != null) {
                    threeDeeFrame.refresh();
                }
            }
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_VIEW_DISTANCE = new BetterAction("viewDistance", strings.getString("view.distance"), ICON_VIEW_DISTANCE) {
        {
            setShortDescription(strings.getString("enable.or.disable.showing.the.maximum.far.view.distance"));
            setSelected(false);
        }
        
        @Override
        public void performAction(ActionEvent e) {
            view.setDrawViewDistance(! view.isDrawViewDistance());
            setSelected(view.isDrawViewDistance());
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_WALKING_DISTANCE = new BetterAction("walkingDistances", strings.getString("walking.distances"), ICON_WALKING_DISTANCE) {
        {
            setShortDescription(strings.getString("enable.or.disable.showing.the.walking.distances"));
            setSelected(false);
        }
        
        @Override
        public void performAction(ActionEvent e) {
            view.setDrawWalkingDistance(! view.isDrawWalkingDistance());
            setSelected(view.isDrawWalkingDistance());
        }

        private static final long serialVersionUID = 1L;
    };

    private final BetterAction ACTION_ROTATE_LIGHT_RIGHT = new BetterAction("rotateLightClockwise", strings.getString("rotate.light.clockwise"), ICON_ROTATE_LIGHT_RIGHT) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_R, 0));
            setShortDescription(strings.getString("rotate.the.direction.the.light.comes.from.clockwise"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            view.rotateLightRight();
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_ROTATE_LIGHT_LEFT = new BetterAction("rotateLightAnticlockwise", strings.getString("rotate.light.anticlockwise"), ICON_ROTATE_LIGHT_LEFT) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_L, 0));
            setShortDescription(strings.getString("rotate.the.direction.the.light.comes.from.anticlockwise"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            view.rotateLightLeft();
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_MOVE_TO_SPAWN = new BetterAction("moveToSpawn", strings.getString("move.to.spawn"), ICON_MOVE_TO_SPAWN) {
        {
            setShortDescription(strings.getString("move.the.view.to.the.spawn.point"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            view.moveToSpawn();
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_MOVE_TO_ORIGIN = new BetterAction("moveToOrigin", strings.getString("move.to.origin"), ICON_MOVE_TO_ORIGIN) {
        {
            setShortDescription(strings.getString("move.the.view.to.the.origin.coordinates.0.0"));
        }
        
        @Override
        public void performAction(ActionEvent e) {
            view.moveToOrigin();
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_OPEN_DOCUMENTATION = new BetterAction("browseDocumentation", strings.getString("browse.documentation")) {
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(VK_F1, 0));
        }
        
        @Override
        public void performAction(ActionEvent event) {
            try {
                DesktopUtils.open(new URL("http://www.worldpainter.net/trac/wiki/Documentation/"));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_IMPORT_LAYER = new BetterAction("importLayer", "Import custom layer(s)") {
        @Override
        protected void performAction(ActionEvent e) {
            importLayers(null);
        }

        private static final long serialVersionUID = 1L;
    };
    
    private final BetterAction ACTION_ROTATE_BRUSH_LEFT = new BetterAction("rotateBrushLeft", "Rotate brush counterclockwise fifteen degrees") {
        @Override
        protected void performAction(ActionEvent e) {
            int rotation = brushRotationSlider.getValue() - 15;
            if (rotation < -180) {
                rotation += 360;
            }
            brushRotationSlider.setValue(rotation);
            if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                brushRotation = rotation;
            } else {
                toolBrushRotation = rotation;
            }
            updateBrushRotation();
        }
    };
    
    private final BetterAction ACTION_ROTATE_BRUSH_RIGHT = new BetterAction("rotateBrushRight", "Rotate brush clockwise fifteen degrees") {
        @Override
        protected void performAction(ActionEvent e) {
            int rotation = brushRotationSlider.getValue() + 15;
            if (rotation > 180) {
                rotation -= 360;
            }
            brushRotationSlider.setValue(rotation);
            if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                brushRotation = rotation;
            } else {
                toolBrushRotation = rotation;
            }
            updateBrushRotation();
        }
    };
    
    private final BetterAction ACTION_ROTATE_BRUSH_RESET = new BetterAction("rotateBrushReset", "Reset brush rotation to zero degrees") {
        @Override
        protected void performAction(ActionEvent e) {
            if (brushRotationSlider.getValue() != 0) {
                brushRotationSlider.setValue(0);
                if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                    brushRotation = 0;
                } else {
                    toolBrushRotation = 0;
                }
                updateBrushRotation();
            }
        }
    };

    private final BetterAction ACTION_ROTATE_BRUSH_RIGHT_30_DEGREES = new BetterAction("rotateBrushRight30Degrees", "Rotate brush clockwise 30 degrees") {
        @Override
        protected void performAction(ActionEvent e) {
            int rotation = brushRotationSlider.getValue() + 30;
            if (rotation > 180) {
                rotation -= 360;
            }
            brushRotationSlider.setValue(rotation);
            if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                brushRotation = rotation;
            } else {
                toolBrushRotation = rotation;
            }
            updateBrushRotation();
        }
    };

    private final BetterAction ACTION_ROTATE_BRUSH_RIGHT_45_DEGREES = new BetterAction("rotateBrushRight45Degrees", "Rotate brush clockwise 45 degrees") {
        @Override
        protected void performAction(ActionEvent e) {
            int rotation = brushRotationSlider.getValue() + 45;
            if (rotation > 180) {
                rotation -= 360;
            }
            brushRotationSlider.setValue(rotation);
            if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                brushRotation = rotation;
            } else {
                toolBrushRotation = rotation;
            }
            updateBrushRotation();
        }
    };
    
    private final BetterAction ACTION_ROTATE_BRUSH_RIGHT_90_DEGREES = new BetterAction("rotateBrushRight90Degrees", "Rotate brush clockwise 90 degrees") {
        @Override
        protected void performAction(ActionEvent e) {
            int rotation = brushRotationSlider.getValue() + 90;
            if (rotation > 180) {
                rotation -= 360;
            }
            brushRotationSlider.setValue(rotation);
            if ((activeOperation instanceof TerrainPaint) || (activeOperation instanceof LayerPaint)) {
                brushRotation = rotation;
            } else {
                toolBrushRotation = rotation;
            }
            updateBrushRotation();
        }
    };
    
    private final BetterAction ACTION_RESET_DOCKS = new BetterAction("resetDockLayout", "Reset workspace layout") {
        @Override
        protected void performAction(ActionEvent e) {
            dockingManager.resetToDefault();
            Configuration config = Configuration.getInstance();
            config.setDefaultJideLayoutData(null);
            ACTION_LOAD_LAYOUT.setEnabled(false);
        }
    };

    private final BetterAction ACTION_LOAD_LAYOUT = new BetterAction("loadDockLayout", "Load workspace layout") {
        @Override
        protected void performAction(ActionEvent e) {
            Configuration config = Configuration.getInstance();
            if (config.getDefaultJideLayoutData() != null) {
                dockingManager.loadLayoutFrom(new ByteArrayInputStream(config.getDefaultJideLayoutData()));
            }
        }
    };

    private final BetterAction ACTION_SAVE_LAYOUT = new BetterAction("resetDocks", "Save workspace layout") {
        @Override
        protected void performAction(ActionEvent e) {
            Configuration config = Configuration.getInstance();
            config.setDefaultJideLayoutData(dockingManager.getLayoutRawData());
            ACTION_LOAD_LAYOUT.setEnabled(true);
            JOptionPane.showMessageDialog(App.this, "Workspace layout saved", "Workspace layout saved", JOptionPane.INFORMATION_MESSAGE);
        }
    };

    private World2 world;
    private Dimension dimension;
    private WorldPainter view;
    private Operation activeOperation;
    private File lastSelectedFile;
    private JLabel heightLabel, locationLabel, waterLabel, materialLabel, radiusLabel, zoomLabel, biomeLabel, levelLabel, brushRotationLabel;
    private int radius = 50;
    private final ButtonGroup toolButtonGroup = new ButtonGroup(), brushButtonGroup = new ButtonGroup();
    private Brush brush = SymmetricBrush.PLATEAU_CIRCLE, toolBrush = SymmetricBrush.COSINE_CIRCLE;
    private final Map<Brush, JToggleButton> brushButtons = new HashMap<Brush, JToggleButton>();
    private boolean programmaticChange;
    private UndoManager undoManager;
    private JSlider levelSlider, brushRotationSlider;
    private float level = 0.51f, toolLevel = 0.51f;
    private Set<Layer> hiddenLayers = new HashSet<Layer>();
    private int zoom = 0, maxRadius = DEFAULT_MAX_RADIUS, brushRotation = 0, toolBrushRotation = 0, previousBrushRotation = 0;
    private GlassPane glassPane;
    private JCheckBox readOnlyCheckBox, biomesCheckBox, annotationsCheckBox, readOnlySoloCheckBox, biomesSoloCheckBox, annotationsSoloCheckBox;
    private JToggleButton readOnlyToggleButton, biomesToggleButton, annotationsToggleButton, setSpawnPointToggleButton;
    private JMenuItem addNetherMenuItem, addEndMenuItem;
    private JCheckBoxMenuItem viewSurfaceMenuItem, viewNetherMenuItem, viewEndMenuItem, extendedBlockIdsMenuItem;
    private final JToggleButton[] customMaterialButtons = new JToggleButton[Terrain.CUSTOM_TERRAIN_COUNT];
    private final ColourScheme[] colourSchemes;
    private ColourScheme selectedColourScheme;
    private final String[] biomeNames = new String[256];
//    private JPanel brushPanel, addLayerButtonPanel;
    private SortedMap<String, List<Brush>> customBrushes;
    private final List<Layer> layers = LayerManager.getInstance().getLayers();
    private final List<Operation> operations;
    private ThreeDeeFrame threeDeeFrame;
    private BiomesViewerFrame biomesViewerFrame;
    private MapDragControl mapDragControl;
    private BiomePaint biomePaintOp;
    private Annotate annotateOp;
    private DockableFrame biomesToolBar, annotationsToolBar;
    private final boolean devMode = "true".equalsIgnoreCase(System.getProperty("org.pepsoft.worldpainter.devMode")); // NOI18N
    private final boolean alwaysEnableReadOnly = "true".equalsIgnoreCase(System.getProperty("org.pepsoft.worldpainter.alwaysEnableReadOnly")); // NOI18N
    private final BiomeScheme autoBiomeScheme = new AutoBiomeScheme(null);
    private JScrollPane scrollPane = new JScrollPane();
//    private JPopupMenu customLayerMenu;
    private Filter filter, toolFilter;
    private final BrushOptions brushOptions;
    private final CustomBiomeManager customBiomeManager = new CustomBiomeManager(this);
    private final Set<CustomLayer> layersWithNoButton = new HashSet<CustomLayer>();
    private final Map<Layer, JCheckBox> layerSoloCheckBoxes = new HashMap<Layer, JCheckBox>();
    private Layer soloLayer;
    private final PaletteManager paletteManager = new PaletteManager(this);
    private DockingManager dockingManager;

    public static final Image ICON = IconUtils.loadImage("org/pepsoft/worldpainter/icons/shovel-icon.png");
    
    public static final int DEFAULT_MAX_RADIUS = 300;

    private static App instance;
    private static Mode mode = Mode.WORLDPAINTER;

    private static final String ACTION_NAME_INCREASE_RADIUS        = "increaseRadius"; // NOI18N
    private static final String ACTION_NAME_INCREASE_RADIUS_BY_ONE = "increaseRadiusByOne"; // NOI18N
    private static final String ACTION_NAME_DECREASE_RADIUS        = "decreaseRadius"; // NOI18N
    private static final String ACTION_NAME_DECREASE_RADIUS_BY_ONE = "decreaseRadiusByOne"; // NOI18N
    private static final String ACTION_NAME_REDO                   = "redo"; // NOI18N
    private static final String ACTION_NAME_ZOOM_IN                = "zoomIn"; // NOI18N
    private static final String ACTION_NAME_ZOOM_OUT               = "zoomPut"; // NOI18N
    
    private static final long ONE_MEGABYTE = 1024 * 1024;
    
    private static final Logger logger = Logger.getLogger(App.class.getName());

    private static final Icon ICON_NEW_WORLD            = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/page_white.png");
    private static final Icon ICON_OPEN_WORLD           = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/page_white_get.png");
    private static final Icon ICON_SAVE_WORLD           = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/disk.png");
    private static final Icon ICON_EXPORT_WORLD         = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/map_go.png");
    private static final Icon ICON_EXIT                 = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/door_in.png");
    private static final Icon ICON_ZOOM_IN              = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/magnifier_zoom_in.png");
    private static final Icon ICON_ZOOM_RESET           = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/magnifier.png");
    private static final Icon ICON_ZOOM_OUT             = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/magnifier_zoom_out.png");
    private static final Icon ICON_GRID                 = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/grid.png");
    private static final Icon ICON_CONTOURS             = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/contours.png");
    private static final Icon ICON_OVERLAY              = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/photo.png");
    private static final Icon ICON_UNDO                 = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_undo.png");
    private static final Icon ICON_REDO                 = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_redo.png");
    private static final Icon ICON_EDIT_TILES           = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/plugin.png");
    private static final Icon ICON_CHANGE_HEIGHT        = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_up_down.png");
    private static final Icon ICON_ROTATE_WORLD         = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_rotate_anticlockwise.png");
    private static final Icon ICON_DIMENSION_PROPERTIES = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/application_form.png");
    private static final Icon ICON_VIEW_DISTANCE        = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/eye.png");
    private static final Icon ICON_WALKING_DISTANCE     = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/user_go.png");
    private static final Icon ICON_ROTATE_LIGHT_RIGHT   = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_rotate_lightbulb_clockwise.png");
    private static final Icon ICON_ROTATE_LIGHT_LEFT    = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_rotate_lightbulb_anticlockwise.png");
    private static final Icon ICON_MOVE_TO_SPAWN        = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/spawn_red.png");
    private static final Icon ICON_MOVE_TO_ORIGIN       = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_in.png");
    private static final Icon ICON_UNKNOWN_PATTERN      = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/unknown_pattern.png");
    
    private static final int PLATFORM_COMMAND_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private static final String CUSTOM_BRUSHES_DEFAULT_TITLE = "Custom Brushes";
    
    private static final ResourceBundle strings = ResourceBundle.getBundle("org.pepsoft.worldpainter.resources.strings"); // NOI18N
    private static final long serialVersionUID = 1L;
    
    public class IntensityAction extends BetterAction {
        public IntensityAction(int percentage, int keyCode) {
            super("intensity" + percentage, MessageFormat.format(strings.getString("set.intensity.to.0"), percentage));
            this.percentage = percentage;
            setAcceleratorKey(KeyStroke.getKeyStroke(keyCode, 0));
        }

        @Override
        public void performAction(ActionEvent e) {
            levelSlider.setValue(percentage);
        }
        
        private final int percentage;
        
        private static final long serialVersionUID = 1L;
    }
    
    class ScrollController extends MouseAdapter implements KeyEventDispatcher {
        ScrollController() {
            timer.setRepeats(false);
        }
        
        void install() {
            view.addMouseListener(this);
            view.addMouseMotionListener(this);
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
//            App.this.addMouseListener(windowMouseListener);
        }

        void uninstall() {
            if (keyDragging || mouseDragging) {
                glassPane.setCursor(previousCursor);
            }
            mouseDragging = false;
            keyDragging = false;
//            App.this.removeMouseListener(windowMouseListener);
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
            view.removeMouseMotionListener(this);
            view.removeMouseListener(this);
        }
        
        // MouseListener / MouseMotionListener
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isMiddleMouseButton(e) && (! mouseDragging)) {
                if (! keyDragging) {
                    Point viewLocOnScreen = view.getLocationOnScreen();
                    e.translatePoint(viewLocOnScreen.x, viewLocOnScreen.y);
                    previousLocation = e.getPoint();

                    previousCursor = glassPane.getCursor();
                    glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
                
                mouseDragging = true;
//                System.out.println("Mouse dragging activated");
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (mouseDragging || keyDragging) {
//                System.out.println("Mouse moved while dragging active");
                Point viewLocOnScreen = view.getLocationOnScreen();
                e.translatePoint(viewLocOnScreen.x, viewLocOnScreen.y);
                Point location = e.getPoint();
                if (previousLocation != null) {
                    // No idea how previousLocation could be null (it
                    // implies that the mouse pressed event was never
                    // received or handled), but we have a report from the
                    // wild that it happened, so check for it
                    int dx = location.x - previousLocation.x;
                    int dy = location.y - previousLocation.y;
                    scroll(scrollPane.getHorizontalScrollBar(), -dx);
                    scroll(scrollPane.getVerticalScrollBar(), -dy);
                }
                previousLocation = location;
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (mouseDragging || keyDragging) {
//                System.out.println("Mouse dragged while dragging active");
                Point viewLocOnScreen = view.getLocationOnScreen();
                e.translatePoint(viewLocOnScreen.x, viewLocOnScreen.y);
                Point location = e.getPoint();
                if (previousLocation != null) {
                    // No idea how previousLocation could be null (it
                    // implies that the mouse pressed event was never
                    // received or handled), but we have a report from the
                    // wild that it happened, so check for it
                    int dx = location.x - previousLocation.x;
                    int dy = location.y - previousLocation.y;
                    scroll(scrollPane.getHorizontalScrollBar(), -dx);
                    scroll(scrollPane.getVerticalScrollBar(), -dy);
                }
                previousLocation = location;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isMiddleMouseButton(e) && mouseDragging) {
                mouseDragging = false;
                if (! keyDragging) {
                    glassPane.setCursor(previousCursor);
                }
//                System.out.println("Mouse dragging deactivated");
            }
        }

        // KeyEventDispatcher
        
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
//            System.out.println(e.getWhen() + ": " + e);
            if (App.this.isFocused() && (e.getKeyCode() == KeyEvent.VK_SPACE)) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if ((e.getWhen() - lastReleased) < KEY_REPEAT_GUARD_TIME) {
                        timer.stop();
//                        System.out.println("Ignoring repeated key press and release");
                        return true;
                    } else if (! keyDragging) {
                        Point mouseLocOnScreen = MouseInfo.getPointerInfo().getLocation();
                        Point scrollPaneLocOnScreen = scrollPane.getLocationOnScreen();
                        Rectangle viewBoundsOnScreen = scrollPane.getBounds();
                        viewBoundsOnScreen.translate(scrollPaneLocOnScreen.x, scrollPaneLocOnScreen.y);
                        if (! viewBoundsOnScreen.contains(mouseLocOnScreen)) {
                            // The mouse cursor is not over the view
//                            System.out.println("Spacebar pressed but mouse not over view");
                            return false;
                        }

                        if (! mouseDragging) {
                            previousLocation = mouseLocOnScreen;

                            previousCursor = glassPane.getCursor();
                            glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        }

//                        System.out.println("Key dragging activated");
                        keyDragging = true;
                        return true;
                    }
                } else if ((e.getID() == KeyEvent.KEY_RELEASED) && keyDragging) {
                    lastReleased = e.getWhen();
                    timer.start();
                    return true;
                }
            }
            return false;
        }

        private Point previousLocation;
        private boolean mouseDragging, keyDragging;
        private Cursor previousCursor;
        private long lastReleased;
        private final Timer timer = new Timer(KEY_REPEAT_GUARD_TIME, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    keyDragging = false;
                    if (! mouseDragging) {
                        glassPane.setCursor(previousCursor);
                    }
//                    System.out.println("Key dragging deactivated");
                }
            });

//        private final MouseListener windowMouseListener = new MouseListener() {
//            @Override
//            public void mouseExited(MouseEvent e) {
//                if (keyDragging) {
//                    keyDragging = false;
//                    if (! mouseDragging) {
//                        glassPane.setCursor(previousCursor);
//                    }
//                    System.out.println("Mouse left window; key dragging deactivated");
//                }
//            }
//
//            @Override public void mouseClicked(MouseEvent e) {}
//            @Override public void mousePressed(MouseEvent e) {}
//            @Override public void mouseReleased(MouseEvent e) {}
//            @Override public void mouseEntered(MouseEvent e) {}
//        };
        
        /**
         * The number of milliseconds between key press and release events below
         * which they will be considered automatic repeats
         */
        private static final int KEY_REPEAT_GUARD_TIME = 10;
    }
    
    public enum Mode {WORLDPAINTER, MINECRAFTMAPEDITOR}
}