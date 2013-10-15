/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.threedeeview;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import org.pepsoft.minecraft.Direction;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.ColourScheme;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.util.BetterAction;

/**
 *
 * @author pepijn
 */
public class ThreeDeeFrame extends JFrame implements WindowListener {
    public ThreeDeeFrame(Dimension dimension, ColourScheme colourScheme, Point initialCoords) throws HeadlessException {
        super("WorldPainter - 3D View");
        setIconImage(App.ICON);
        this.colourScheme = colourScheme;
        this.coords = initialCoords;
        
        scrollPane = new JScrollPane();
        
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                previousX = e.getX();
                previousY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - previousX;
                int dy = e.getY() - previousY;
                previousX = e.getX();
                previousY = e.getY();
                JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
                scrollBar.setValue(scrollBar.getValue() - dx);
                scrollBar = scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getValue() - dy);
            }

            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            @Override public void mouseMoved(MouseEvent e) {}
            
            private int previousX, previousY;
        };
        scrollPane.addMouseListener(mouseAdapter);
        scrollPane.addMouseMotionListener(mouseAdapter);
        
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        final JToggleButton alwaysOnTopButton = new JToggleButton(ICON_ALWAYS_ON_TOP);
        alwaysOnTopButton.setToolTipText("Set the 3D view window to be always on top");
        alwaysOnTopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (alwaysOnTopButton.isSelected()) {
                    ThreeDeeFrame.this.setAlwaysOnTop(true);
                } else {
                    ThreeDeeFrame.this.setAlwaysOnTop(false);
                }
            }
        });
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(alwaysOnTopButton);
        toolBar.add(ROTATE_LEFT_ACTION);
        toolBar.add(ROTATE_RIGHT_ACTION);
        toolBar.add(EXPORT_IMAGE_ACTION);
        toolBar.add(MOVE_TO_SPAWN_ACTION);
        toolBar.add(MOVE_TO_ORIGIN_ACTION);
        getContentPane().add(toolBar, BorderLayout.NORTH);

        glassPane = new GlassPane();
        setGlassPane(glassPane);
        getGlassPane().setVisible(true);
        
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("rotateLeft", ROTATE_LEFT_ACTION);
        actionMap.put("rotateRight", ROTATE_RIGHT_ACTION);

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke('l'), "rotateLeft");
        inputMap.put(KeyStroke.getKeyStroke('r'), "rotateRight");
        
        setSize(800, 600);
        
        setDimension(dimension);
        
        addWindowListener(this);
    }

    public final Dimension getDimension() {
        return dimension;
    }

    public final void setDimension(Dimension dimension) {
        this.dimension = dimension;
        if (dimension != null) {
            threeDeeView = new ThreeDeeView(dimension, colourScheme, null, rotation);
            scrollPane.setViewportView(threeDeeView);
            MOVE_TO_SPAWN_ACTION.setEnabled(dimension.getDim() == DIM_NORMAL);
        }
    }

    public void moveTo(Point coords) {
        this.coords = coords;
        threeDeeView.moveTo(coords.x, coords.y);
    }
    
    // WindowListener

    @Override
    public void windowOpened(WindowEvent e) {
        moveTo(coords);
    }

    @Override public void windowClosing(WindowEvent e) {}
    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}
    
    private final Action ROTATE_LEFT_ACTION = new BetterAction("rotate3DViewLeft", "Rotate left", ICON_ROTATE_LEFT) {
        {
            setShortDescription("Rotate the view 90 degrees anticlockwise (l)");
        }
        
        @Override
        public void performAction(ActionEvent e) {
            rotation--;
            if (rotation < 0) {
                rotation = 3;
            }
            Point centreMostTile = threeDeeView.getCentreMostTile();
            threeDeeView = new ThreeDeeView(dimension, colourScheme, null, rotation);
            scrollPane.setViewportView(threeDeeView);
//            scrollPane.getViewport().setViewPosition(new Point((threeDeeView.getWidth() - scrollPane.getWidth()) / 2, (threeDeeView.getHeight() - scrollPane.getHeight()) / 2));
            threeDeeView.moveToTile(centreMostTile.x, centreMostTile.y);
            glassPane.setRotation(DIRECTIONS[rotation]);
        }
        
        private static final long serialVersionUID = 1L;
    };
    
    private final Action ROTATE_RIGHT_ACTION = new BetterAction("rotate3DViewRight", "Rotate right", ICON_ROTATE_RIGHT) {
        {
            setShortDescription("Rotate the view 90 degrees clockwise (r)");
        }
        
        @Override
        public void performAction(ActionEvent e) {
            rotation++;
            if (rotation > 3) {
                rotation = 0;
            }
            Point centreMostTile = threeDeeView.getCentreMostTile();
            threeDeeView = new ThreeDeeView(dimension, colourScheme, null, rotation);
            scrollPane.setViewportView(threeDeeView);
//            scrollPane.getViewport().setViewPosition(new Point((threeDeeView.getWidth() - scrollPane.getWidth()) / 2, (threeDeeView.getHeight() - scrollPane.getHeight()) / 2));
            threeDeeView.moveToTile(centreMostTile.x, centreMostTile.y);
            glassPane.setRotation(DIRECTIONS[rotation]);
        }
        
        private static final long serialVersionUID = 1L;
    };

    private final Action EXPORT_IMAGE_ACTION = new BetterAction("export3DViewImage", "Export image", ICON_EXPORT_IMAGE) {
        {
            setShortDescription("Export to an image file");
        }
        
        @Override
        public void performAction(ActionEvent e) {
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
            String defaultname = dimension.getWorld().getName().replaceAll("\\s", "").toLowerCase() + ((dimension.getDim() == DIM_NORMAL) ? "" : ("_" + dimension.getName().toLowerCase())) + "_3d.png";
            fileChooser.setSelectedFile(new File(defaultname));
            if (fileChooser.showSaveDialog(ThreeDeeFrame.this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                final String type;
                int p = selectedFile.getName().lastIndexOf('.');
                if (p != -1) {
                    type = selectedFile.getName().substring(p + 1).toUpperCase();
                } else {
                    type = "PNG";
                    selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
                }
                if (selectedFile.exists()) {
                    if (JOptionPane.showConfirmDialog(ThreeDeeFrame.this, "The file already exists!\nDo you want to overwrite it?", "Overwrite File?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                final File file = selectedFile;
                Boolean result = ProgressDialog.executeTask(ThreeDeeFrame.this, new ProgressTask<Boolean>() {
                        @Override
                        public String getName() {
                            return "Exporting image...";
                        }

                        @Override
                        public Boolean execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                            try {
                                return ImageIO.write(threeDeeView.getImage(progressReceiver), type, file);
                            } catch (IOException e) {
                                throw new RuntimeException("I/O error while exporting image", e);
                            }
                        }
                    });
                if ((result != null) && result.equals(Boolean.FALSE)) {
                    JOptionPane.showMessageDialog(ThreeDeeFrame.this, "Format " + type + " not supported!");
                }
            }
        }
        
        private static final long serialVersionUID = 1L;
    };
    
    private final Action MOVE_TO_SPAWN_ACTION = new BetterAction("move3DViewToSpawn", "Move to spawn", ICON_MOVE_TO_SPAWN) {
        {
            setShortDescription("Move the view to the spawn location");
        }
        
        @Override
        public void performAction(ActionEvent e) {
            if (dimension.getDim() == DIM_NORMAL) {
                Point spawn = dimension.getWorld().getSpawnPoint();
                threeDeeView.moveTo(spawn.x, spawn.y);
            }
        }
        
        private static final long serialVersionUID = 1L;
    };
    
    private final Action MOVE_TO_ORIGIN_ACTION = new BetterAction("move3DViewToOrigin", "Move to origin", ICON_MOVE_TO_ORIGIN) {
        {
            setShortDescription("Move the view to the origin (coordinates 0,0)");
        }
        
        @Override
        public void performAction(ActionEvent e) {
            threeDeeView.moveTo(0, 0);
        }
        
        private static final long serialVersionUID = 1L;
    };
    
    private Dimension dimension;
    private final JScrollPane scrollPane;
    private ThreeDeeView threeDeeView;
    private ColourScheme colourScheme;
    private int rotation = 3;
    private final GlassPane glassPane;
    private Point coords;
    
    private static final Direction[] DIRECTIONS = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
    
    private static final Icon ICON_ROTATE_LEFT    = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_rotate_anticlockwise.png");
    private static final Icon ICON_ROTATE_RIGHT   = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_rotate_clockwise.png");
    private static final Icon ICON_EXPORT_IMAGE   = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/picture_save.png");
    private static final Icon ICON_MOVE_TO_SPAWN  = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/spawn_red.png");
    private static final Icon ICON_MOVE_TO_ORIGIN = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/arrow_in.png");
    private static final Icon ICON_ALWAYS_ON_TOP  = IconUtils.loadIcon("org/pepsoft/worldpainter/icons/lock.png");
    
    private static final long serialVersionUID = 1L;
}