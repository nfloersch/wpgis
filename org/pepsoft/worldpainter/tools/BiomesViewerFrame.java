/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Level;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.util.swing.TileProvider;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.BiomeScheme;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.NewWorldDialog;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_1BiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_2BiomeScheme;
import org.pepsoft.worldpainter.util.MinecraftUtil;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.Generator;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_3LargeBiomeScheme;

/**
 *
 * @author pepijn
 */
public class BiomesViewerFrame extends JFrame {
    public BiomesViewerFrame(long seed, BiomeScheme biomeScheme, SeedListener seedListener) throws HeadlessException {
        super("WorldPainter - Biomes Viewer");
        if ((! (biomeScheme instanceof Minecraft1_1BiomeScheme)) && (! (biomeScheme instanceof Minecraft1_2BiomeScheme)) && (! (biomeScheme instanceof Minecraft1_3LargeBiomeScheme))) {
            throw new IllegalArgumentException("A Minecraft 1.5, 1.4, 1.3, 1.2 or 1.1 biome scheme must be selected");
        }
        this.biomeScheme = biomeScheme;
        this.seedListener = seedListener;
        biomeScheme.setSeed(seed);
        final BiomesTileProvider tileProvider = new BiomesTileProvider(biomeScheme);
        imageViewer = new BiomesViewer(false);
        imageViewer.setTileProvider(tileProvider);
        imageViewer.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getWheelRotation();
                int zoom = imageViewer.getZoom();
                if (rotation < 0) {
                    for (int i = 0; i > rotation; i--) {
                        if (zoom > 1) {
                            zoom /= 2;
                        }
                    }
                } else {
                    for (int i = 0; i < rotation; i++) {
                        if (zoom < 16) {
                            zoom *= 2;
                        }
                    }
                }
                imageViewer.setZoom(zoom);
            }
        });
        Controller controller = new Controller(tileProvider);
        imageViewer.addMouseListener(controller);
        imageViewer.addMouseMotionListener(controller);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().add(imageViewer, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new JLabel("Biome scheme:"));
        schemeChooser = new JComboBox(new Object[] {"Minecraft 1.5 Default (or 1.2 - 1.4)", "Minecraft 1.5 Large Biomes (or 1.3 - 1.4)", "Minecraft 1.1"});
        seedSpinner = new JSpinner(new SpinnerNumberModel(Long.valueOf(seed), Long.valueOf(Long.MIN_VALUE), Long.valueOf(Long.MAX_VALUE), Long.valueOf(1L)));
        if (biomeScheme instanceof Minecraft1_1BiomeScheme) {
            schemeChooser.setSelectedIndex(1);
        }
        schemeChooser.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int selectedIndex = schemeChooser.getSelectedIndex();
                switch (selectedIndex) {
                    case 0:
                        if (! (BiomesViewerFrame.this.biomeScheme instanceof Minecraft1_2BiomeScheme)) {
                            BiomeScheme biomeScheme = BiomeSchemeManager.getBiomeScheme(World2.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, BiomesViewerFrame.this);
                            if (biomeScheme != null) {
                                BiomesViewerFrame.this.biomeScheme = biomeScheme;
                                BiomesViewerFrame.this.biomeScheme.setSeed(((Number) seedSpinner.getValue()).longValue());
                                imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, imageViewer.getZoom()));
                            }
                        }
                        break;
                    case 1:
                        if (! (BiomesViewerFrame.this.biomeScheme instanceof Minecraft1_3LargeBiomeScheme)) {
                            BiomeScheme biomeScheme = BiomeSchemeManager.getBiomeScheme(World2.BIOME_ALGORITHM_1_3_LARGE, BiomesViewerFrame.this);
                            if (biomeScheme != null) {
                                BiomesViewerFrame.this.biomeScheme = biomeScheme;
                                BiomesViewerFrame.this.biomeScheme.setSeed(((Number) seedSpinner.getValue()).longValue());
                                imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, imageViewer.getZoom()));
                            }
                        }
                        break;
                    case 2:
                        if (! (BiomesViewerFrame.this.biomeScheme instanceof Minecraft1_1BiomeScheme)) {
                            BiomeScheme biomeScheme = BiomeSchemeManager.getBiomeScheme(World2.BIOME_ALGORITHM_1_1, BiomesViewerFrame.this);
                            if (biomeScheme != null) {
                                BiomesViewerFrame.this.biomeScheme = biomeScheme;
                                BiomesViewerFrame.this.biomeScheme.setSeed(((Number) seedSpinner.getValue()).longValue());
                                imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, imageViewer.getZoom()));
                            }
                        }
                        break;
                }
            }
        });
        toolBar.add(schemeChooser);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(new JLabel("Seed:"));
        seedSpinner.setEditor(new JSpinner.NumberEditor(seedSpinner, "0"));
        seedSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                BiomesViewerFrame.this.biomeScheme.setSeed(((Number) seedSpinner.getValue()).longValue());
                imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, imageViewer.getZoom()));
            }
        });
        toolBar.add(seedSpinner);
        getContentPane().add(toolBar, BorderLayout.NORTH);
        
        toolBar = new JToolBar();
        JButton button = new JButton("-");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int zoom = imageViewer.getZoom();
                if (zoom < 16) {
                    zoom *= 2;
                    imageViewer.setZoom(zoom);
                }
            }
        });
        toolBar.add(button);
        
        button = new JButton("+");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int zoom = imageViewer.getZoom();
                if (zoom > 1) {
                    zoom /= 2;
                    imageViewer.setZoom(zoom);
                }
            }
        });
        toolBar.add(button);
        
        toolBar.add(Box.createHorizontalStrut(5));
        createWorldButton = new JButton("Create world");
        createWorldButton.setToolTipText("Create a new WorldPainter world from the selected tiles");
        createWorldButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createWorld();
            }
        });
        createWorldButton.setEnabled(false);
        toolBar.add(createWorldButton);
        
        toolBar.add(Box.createHorizontalStrut(5));
        button = new JButton("Reset");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageViewer.reset();
            }
        });
        toolBar.add(button);
        
        if (seedListener != null) {
            toolBar.add(Box.createHorizontalStrut(5));
            button = new JButton("Copy seed to world");
            button.setToolTipText("Copy the current seed to the world currently being edited");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BiomesViewerFrame.this.seedListener.setSeed(((Number) seedSpinner.getValue()).longValue());
                }
            });
            toolBar.add(button);
        }
        
        toolBar.add(Box.createHorizontalStrut(5));
        button = new JButton("Play here");
        button.setToolTipText("Create a map in Minecraft with this seed and at this location");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String name = JOptionPane.showInputDialog(BiomesViewerFrame.this, "Type a name for the map:", "Map Name", JOptionPane.QUESTION_MESSAGE);
                if ((name == null) || (name.trim().length() == 0)) {
                    return;
                }
                name = name.trim();
                File savesDir;
                boolean minecraftDirUsed = false;
                File minecraftDir = MinecraftUtil.findMinecraftDir();
                if (minecraftDir != null) {
                    savesDir = new File(minecraftDir, "saves");
                    minecraftDirUsed = true;
                } else {
                    savesDir = DesktopUtils.getDocumentsFolder();
                }
                File worldDir = new File(savesDir, name);
                int ordinal = 1;
                while (worldDir.isDirectory()) {
                    worldDir = new File(savesDir, name + ordinal);
                    ordinal++;
                }
                if (! worldDir.mkdirs()) {
                    throw new RuntimeException("Could not create " + worldDir);
                }
                BiomeScheme biomeScheme = BiomesViewerFrame.this.biomeScheme;
                Level level = new Level(Constants.DEFAULT_MAX_HEIGHT_1, (biomeScheme instanceof Minecraft1_1BiomeScheme) ? Constants.SUPPORTED_VERSION_1 : Constants.SUPPORTED_VERSION_2);
                if (! (biomeScheme instanceof Minecraft1_1BiomeScheme)) {
                    level.setGenerator((biomeScheme instanceof Minecraft1_3LargeBiomeScheme) ? Generator.LARGE_BIOMES : Generator.DEFAULT);
                }
                level.setGameType(Constants.GAME_TYPE_SURVIVAL);
                level.setMapFeatures(true);
                level.setName(name);
                level.setSeed(((Number) seedSpinner.getValue()).longValue());
                int middleX = getWidth() / 2;
                int middleY = getHeight() / 2;
                int worldX = (imageViewer.getViewX() + middleX) * imageViewer.getZoom();
                int worldY = (imageViewer.getViewY() + middleY) * imageViewer.getZoom();
                level.setSpawnX(worldX);
                level.setSpawnZ(worldY);
                level.setSpawnY(64);
                try {
                    level.save(worldDir);
                } catch (IOException e) {
                    throw new RuntimeException("I/O error writing level.dat file", e);
                }
                if (minecraftDirUsed) {
                    JOptionPane.showMessageDialog(BiomesViewerFrame.this, "Map saved! You can find it in Minecraft under Singleplayer.");
                } else {
                    JOptionPane.showMessageDialog(BiomesViewerFrame.this, "Map saved as " + worldDir + ".\nMove it to your Minecraft saves directory to play.");
                }
            }
        });
        toolBar.add(button);
        
        toolBar.add(Box.createHorizontalGlue());
        getContentPane().add(toolBar, BorderLayout.SOUTH);

        setSize(800, 600);
    }

    public void destroy() {
        imageViewer.setTileProvider(null);
    }
    
    private void createWorld() {
        App app = App.getInstance();
        if (! app.saveIfNecessary()) {
            return;
        }
        final NewWorldDialog dialog = new NewWorldDialog(
                app,
                "Generated World",
                ((Number) seedSpinner.getValue()).longValue(),
                (schemeChooser.getSelectedIndex() < 2),
                DIM_NORMAL,
                Configuration.getInstance().getDefaultMaxHeight(),
                imageViewer.getSelectedTiles());
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            app.setWorld(null);
            if (! dialog.checkMemoryRequirements(this)) {
                return;
            }
            World2 newWorld = ProgressDialog.executeTask(this, new ProgressTask<World2>() {
                @Override
                public String getName() {
                    return "Creating new world";
                }
                
                @Override
                public World2 execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                    return dialog.getSelectedWorld(progressReceiver);
                }
            }, true);
            if (newWorld != null) {
                if (schemeChooser.getSelectedIndex() == 1) {
                    newWorld.setGenerator(Generator.LARGE_BIOMES);
                }
                app.setWorld(newWorld);
                if (newWorld.isCustomBiomes() && (app.getBiomeScheme() != null)) {
                    // Initialise the custom biomes
                    newWorld.getDimension(0).recalculateBiomes(app.getBiomeScheme(), this);
                }
            }
        }
    }
    
    private void setControlStates() {
        createWorldButton.setEnabled(! imageViewer.getSelectedTiles().isEmpty());
    }

    private final WPTileSelectionViewer imageViewer;
    private final SeedListener seedListener;
    private final JButton createWorldButton;
    private final JSpinner seedSpinner;
    private final JComboBox schemeChooser;
    private BiomeScheme biomeScheme;
    
    private static final long serialVersionUID = 1L;
    
    public static interface SeedListener {
        void selectBiomeScheme(int biomeScheme);
        void setSeed(long seed);
    }
    
    class Controller implements MouseListener, MouseMotionListener {
        public Controller(TileProvider tileProvider) {
            this.tileProvider = tileProvider;
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            Point tileLocation = getTileLocation(e.getX(), e.getY());
            if (imageViewer.isSelectedTile(tileLocation)) {
                imageViewer.removeSelectedTile(tileLocation);
            } else {
                imageViewer.addSelectedTile(tileLocation);
            }
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
            setControlStates();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            selecting = true;
            selectionCorner1 = getTileLocation(e.getX(), e.getY());
            selectionCorner2 = null;
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            if ((selectionCorner1 != null) && (selectionCorner2 != null)) {
                int tileX1 = Math.min(selectionCorner1.x, selectionCorner2.x);
                int tileX2 = Math.max(selectionCorner1.x, selectionCorner2.x);
                int tileY1 = Math.min(selectionCorner1.y, selectionCorner2.y);
                int tileY2 = Math.max(selectionCorner1.y, selectionCorner2.y);
                for (int x = tileX1; x <= tileX2; x++) {
                    for (int y = tileY1; y <= tileY2; y++) {
                        Point tileLocation = new Point(x, y);
                        if (imageViewer.isSelectedTile(tileLocation)) {
                            imageViewer.removeSelectedTile(tileLocation);
                        } else {
                            imageViewer.addSelectedTile(tileLocation);
                        }
                    }
                }
                setControlStates();
            }
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
            selecting = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(null);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
            if (selecting) {
                selectionCorner2 = getTileLocation(e.getX(), e.getY());
                imageViewer.setSelectedRectangleCorner1(selectionCorner1);
                imageViewer.setSelectedRectangleCorner2(selectionCorner2);
            }
        }

        private Point getTileLocation(int x, int y) {
            int viewX = imageViewer.getViewX();
            int viewY = imageViewer.getViewY();
            int zoom = tileProvider.getZoom();
            return new Point((x + viewX) * zoom / TILE_SIZE - ((x < -viewX) ? 1 : 0), (y + viewY) * zoom / TILE_SIZE - ((y < -viewY) ? 1 : 0));
        }

        private final TileProvider tileProvider;
        private boolean selecting;
        private Point selectionCorner1, selectionCorner2;
    }
}