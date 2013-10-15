/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ErrorDialog.java
 *
 * Created on Apr 17, 2011, 8:22:55 PM
 */

package org.pepsoft.worldpainter;

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.pepsoft.worldpainter.layers.Layer;

/**
 *
 * @author pepijn
 */
public class ErrorDialog extends javax.swing.JDialog {
    /** Creates new form ErrorDialog */
    public ErrorDialog(Dialog parent) {
        super(parent, true);
        init(parent);
    }

    /** Creates new form ErrorDialog */
    public ErrorDialog(Frame parent) {
        super(parent, true);
        init(parent);
    }

    public void setException(Throwable exception) {
        logger.log(Level.SEVERE, exception.getClass().getSimpleName() + ": " + exception.getMessage(), exception);
        
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        if (rootCause instanceof OutOfMemoryError) {
            setTitle("Out of Memory");
            jTextArea1.setText("Not enough memory available for that operation!\n\n"
                + "WorldPainter is already using the recommended maximum\n"
                + "amount of memory, so it is not recommended to give it\n"
                + "more. To be able to perform the operation you should\n"
                + "install more memory.");
            jButton1.setEnabled(false);
            jButton1.setToolTipText("Not necessary to mail details of out of memory errors");
            jButton3.setEnabled(false);
        } else {
            String message = rootCause.getMessage();
            if ((message != null) && (message.length() > 250)) {
                message = message.substring(0, 247) + "...";
            }
            String text = "An unexpected error has occurred.\n\n"
                + "Type: " + rootCause.getClass().getName() + "\n"
                + "Message: " + message + "\n\n"
                + "Please help debug the problem by using the button below to email the details of this error to the creator of this program.\n\n"
                + "The program may now be in an unstable state. It is recommended to restart it as soon as possible.";
            jTextArea1.setText(text);
        }
        pack();

        StringBuilder sb = new StringBuilder();
        String eol = System.getProperty("line.separator");
        sb.append(exception.getClass().getName());
        sb.append(": ");
        sb.append(exception.getMessage());
        sb.append(eol);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (int i = 0; i < Math.min(stackTrace.length, 5); i++) {
            sb.append("\tat ");
            sb.append(stackTrace[i].getClassName());
            sb.append('.');
            sb.append(stackTrace[i].getMethodName());
            sb.append('(');
            sb.append(stackTrace[i].getFileName());
            sb.append(':');
            sb.append(stackTrace[i].getLineNumber());
            sb.append(')');
            sb.append(eol);
        }
        sb.append(eol);
        if (rootCause != exception) {
            sb.append("Root cause:");
            sb.append(eol);
            sb.append(rootCause.getClass().getName());
            sb.append(": ");
            sb.append(rootCause.getMessage());
            sb.append(eol);
            stackTrace = rootCause.getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 5); i++) {
                sb.append("\tat ");
                sb.append(stackTrace[i].getClassName());
                sb.append('.');
                sb.append(stackTrace[i].getMethodName());
                sb.append('(');
                sb.append(stackTrace[i].getFileName());
                sb.append(':');
                sb.append(stackTrace[i].getLineNumber());
                sb.append(')');
                sb.append(eol);
            }
            sb.append(eol);
        }
        sb.append("WorldPainter version: " + Version.VERSION + eol);
        sb.append(eol);
        for (String propertyName: SYSTEM_PROPERTIES) {
            sb.append(propertyName);
            sb.append(": ");
            sb.append(System.getProperty(propertyName));
            sb.append(eol);
        }
        sb.append(eol);
        Runtime runtime = Runtime.getRuntime();
        sb.append("Free memory: " + runtime.freeMemory() + " bytes" + eol);
        sb.append("Total memory size: " + runtime.totalMemory() + " bytes" + eol);
        sb.append("Max memory size: " + runtime.maxMemory() + " bytes" + eol);
        App app = App.getInstanceIfExists();
        World2 world = (app != null) ? app.getWorld() : null;
        Dimension dimension = (app != null) ? app.getDimension() : null;
        if ((world != null) && (dimension != null)) {
            sb.append(eol);
            sb.append("World name: " + world.getName() + eol);
            sb.append("Seed: " + dimension.getSeed() + eol);
            sb.append("Bounds: " + dimension.getLowestX() + ", " + dimension.getLowestY() + " => " + dimension.getHighestX() + ", " + dimension.getHighestY() + eol);
            sb.append("Height: " + world.getMaxHeight() + eol);
            sb.append("Number of tiles: " + dimension.getTiles().size() + eol);
            Set<Layer> layers = new HashSet<Layer>();
            for (Tile tile: dimension.getTiles()) {
                layers.addAll(tile.getLayers());
            }
            sb.append("Layers in use: ");
            boolean first = true;
            for (Layer layer: layers) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(layer.getName());
            }
            sb.append(eol);
            sb.append("Border: " + dimension.getBorder() + " @ " + dimension.getBorderLevel() + eol);
            sb.append("Sub surface material: " + dimension.getSubsurfaceMaterial() + eol);
            TileFactory tileFactory = dimension.getTileFactory();
            if (tileFactory instanceof HeightMapTileFactory) {
                HeightMapTileFactory heightMapTileFactory = (HeightMapTileFactory) tileFactory;
                sb.append("Water height: " + heightMapTileFactory.getWaterHeight() + eol);
            }
            sb.append("Biome scheme: " + ((world.getBiomeAlgorithm() >= 0) ? World2.BIOME_ALGORITHM_NAMES[world.getBiomeAlgorithm()] : "None") + eol);
            sb.append("Custom biomes " + (world.isCustomBiomes() ? "" : "not ") + "enabled" + eol);
            if (world.getImportedFrom() != null) {
                sb.append("World imported from " + world.getImportedFrom() + eol);
            }
            if (! world.isAllowMerging()) {
                sb.append("World created in old coordinate system" + eol);
            }
        }
        if (app != null) {
            sb.append(eol);
            sb.append("Operation: " + app.getActiveOperation() + eol);
            sb.append("Radius: " + app.getRadius() + eol);
    //        sb.append("Brush shape: " + app.getBrushShape() + "/" + app.getToolBrushShape() + eol);
            sb.append("Brush: " + app.getBrush() + "/" + app.getToolBrush() + eol);
            sb.append("Level: " + app.getLevel() + "/" + app.getToolLevel() + eol);
            sb.append("Zoom: " + app.getZoom() + eol);
            sb.append("Hidden layers: " + app.getHiddenLayers());
        }
        body = sb.toString();

        if (! "true".equals(System.getProperty("org.pepsoft.worldpainter.devMode"))) {
            logger.severe(body);
        }
    }

    private void init(Window parent) {
        initComponents();

        getRootPane().setDefaultButton(jButton2);
        if ((! Desktop.isDesktopSupported()) || (! Desktop.getDesktop().isSupported(Desktop.Action.MAIL))) {
            jButton1.setEnabled(false);
            jButton1.setToolTipText("Emailing not supported on this system; please use the \"copy to clipboard\" button and mail the details to worldpainter@pepsoft.org.");
        }

        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("cancel", new AbstractAction("cancel") {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
            
            private static final long serialVersionUID = 1L;
        });

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        
        setLocationRelativeTo(parent);
    }

    private void close() {
        dispose();
    }

    private void email() {
        try {
            URI uri = new URI("mailto", "worldpainter@pepsoft.org?subject=WorldPainter error report&body=" + body, null);
            Desktop desktop = Desktop.getDesktop();
            desktop.mail(uri);
            JOptionPane.showMessageDialog(this, "A new email message should have been opened now for you to send.\nIf it did not work, please use the \"copy to clipboard\" button\nand manually mail the information to worldpainter@pepsoft.org.", "Email Created", JOptionPane.INFORMATION_MESSAGE);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "URI syntax error while trying to send email", e);
            JOptionPane.showMessageDialog(this, "Could not create email message with error details!\nPlease use the \"copy to clipboard\" button and mail\nthe information to worldpainter@pepsoft.org.", "Could Not Create Email", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while trying to send email", e);
            JOptionPane.showMessageDialog(this, "Could not create email message with error details!\nPlease use the \"copy to clipboard\" button and mail\nthe information to worldpainter@pepsoft.org.", "Could Not Create Email", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void copyToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection data = new StringSelection(body);
        clipboard.setContents(data, data);
        JOptionPane.showMessageDialog(this, "The information has been copied to the clipboard. Please paste\nit in a new email and send it to worldpainter@pepsoft.org.", "Information Copied", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        ErrorDialog dialog = new ErrorDialog((Frame) null);
        dialog.setException(new OutOfMemoryError("test"));
        dialog.setVisible(true);
    }

    private File findInstallDir() {
        ClassLoader classLoader = ErrorDialog.class.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            for (URL url: ((URLClassLoader) classLoader).getURLs()) {
                if (url.getPath().endsWith("WorldPainter.jar")) {
                    try {
                        return new File(url.toURI()).getParentFile();
                    } catch (URISyntaxException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextArea1 = new javax.swing.JTextArea();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Unexpected Error");

        jButton1.setText("Email Details...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Close");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setFont(getFont());
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(10);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setOpaque(false);

        jButton3.setText("Copy details to clipboard");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextArea1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextArea1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        email();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        close();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        copyToClipboard();
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    private String body;

    private static final double GB = 1024.0 * 1024.0 * 1024.0;
    
    private static final String[] SYSTEM_PROPERTIES = {
        "java.version",
        "java.vendor",
        "java.vm.version",
        "java.vm.vendor",
        "java.vm.name",
        "os.name",
        "os.arch",
        "os.version",
        "user.home",
        "user.dir",
        "user.country",
        "user.language",
    };

    private static final Logger logger = Logger.getLogger(ErrorDialog.class.getName());
    private static final long serialVersionUID = 1L;
}