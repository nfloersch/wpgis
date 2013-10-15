/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GlassPane.java
 *
 * Created on Apr 28, 2011, 4:29:56 PM
 */
package org.pepsoft.worldpainter;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import org.pepsoft.util.IconUtils;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;

/**
 *
 * @author pepijn
 */
public class GlassPane extends javax.swing.JPanel implements PropertyChangeListener {
    /** Creates new form GlassPane */
    public GlassPane() {
        initComponents();
//        jPanel2.add(miniMap, BorderLayout.CENTER);
    }
    
    public void setScale(float scale) {
        int scaleBarSize = (int) (100 / scale);
        jLabel1.setText(SCALE_FORMAT.format(scaleBarSize));
        repaint();
    }

    public WorldPainter getView() {
        return view;
    }

    public void setView(WorldPainter view) {
        if (this.view != null) {
            this.view.removePropertyChangeListener("hiddenLayers", this);
            for (Layer layer: hiddenLayers.keySet()) {
                jPanel1.remove(hiddenLayers.get(layer));
            }
            hiddenLayers.clear();
        }
        this.view = view;
//        miniMap.setView(view);
        if (view != null) {
            view.addPropertyChangeListener("hiddenLayers", this);
            for (Layer layer: view.getHiddenLayers()) {
                if ((! layer.equals(Biome.INSTANCE)) && (layer.getIcon() != null)) {
                    JLabel label = createLabel(layer);
                    hiddenLayers.put(layer, label);
                    jPanel1.add(label);
                }
            }
        }
        jPanel1.revalidate();
    }

    // PropertyChangeListener
    
    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        boolean layersChanged = false;
        Set<Layer> newHiddenLayers = (Set<Layer>) evt.getNewValue();
        for (Layer layer: newHiddenLayers) {
            if ((! hiddenLayers.containsKey(layer)) && (! layer.equals(Biome.INSTANCE)) && (layer.getIcon() != null)) {
                // New hidden layer (with icon)
                JLabel label = createLabel(layer);
                hiddenLayers.put(layer, label);
                jPanel1.add(label);
                layersChanged = true;
            }
        }
        for (Iterator<Map.Entry<Layer, JLabel>> i = hiddenLayers.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<Layer, JLabel> entry = i.next();
            if (! newHiddenLayers.contains(entry.getKey())) {
                JLabel label = entry.getValue();
                jPanel1.remove(label);
                i.remove();
                layersChanged = true;
            }
        }
        if (layersChanged) {
            jPanel1.revalidate();
        }
    }
    
    private JLabel createLabel(Layer layer) {
        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(20, 20, Transparency.TRANSLUCENT);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.drawImage(PROHIBITED_SIGN_BACKGROUND, 0, 0, null);
            g2.drawImage((layer instanceof CustomLayer) ? layer.getIcon() : invertImage(layer.getIcon()), 2, 2, null);
            g2.drawImage(PROHIBITED_SIGN_FOREGROUND, 0, 0, null);
        } finally {
            g2.dispose();
        }
        JLabel label = new JLabel(new ImageIcon(image));
        label.setBorder(new EmptyBorder(1, 1, 1, 1));
        label.setToolTipText(layer.getName() + " layer hidden");
        return label;
    }
    
    private BufferedImage invertImage(BufferedImage in) {
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), in.getType());
        for (int x = 0; x < in.getWidth(); x++) {
            for (int y = 0; y < in.getHeight(); y++) {
                int colour = in.getRGB(x, y);
                colour = (colour & 0xff000000)
                    | (0xff0000 - (colour & 0xff0000))
                    | (0xff00 - (colour & 0xff00))
                    | (0xff - (colour & 0xff));
                out.setRGB(x, y, colour);
            }
        }
        return out;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        setOpaque(false);

        jLabel1.setForeground(java.awt.Color.black);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/scale_bar.png"))); // NOI18N
        jLabel1.setText("100 blocks");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/north_arrow_up.png"))); // NOI18N

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    private WorldPainter view;
//    private final MiniMap miniMap = new MiniMap();
    private final Map<Layer, JLabel> hiddenLayers = new HashMap<Layer, JLabel>();
    
    private static final NumberFormat SCALE_FORMAT = new DecimalFormat("0.# blocks");
    private static final BufferedImage PROHIBITED_SIGN_BACKGROUND = IconUtils.loadImage("org/pepsoft/worldpainter/icons/prohibited_sign_background.png");
    private static final BufferedImage PROHIBITED_SIGN_FOREGROUND = IconUtils.loadImage("org/pepsoft/worldpainter/icons/prohibited_sign_foreground.png");
    private static final long serialVersionUID = 1L;
}