/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SeedBrowser.java
 *
 * Created on 10-jun-2011, 19:05:13
 */
package org.pepsoft.worldpainter;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_0BiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_1BiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_2BiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_3LargeBiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_7_3BiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_8_1BiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_9BiomeScheme;

/**
 *
 * @author pepijn
 */
public class SeedBrowser extends javax.swing.JDialog implements ChangeListener {
    /** Creates new form SeedBrowser */
    public SeedBrowser(App parent, Dimension dimension, BiomeScheme biomeScheme) {
        super(parent, true);
        app = parent;
        this.dimension = dimension;
        this.biomeScheme = biomeScheme;
        initialSeed = dimension.getMinecraftSeed();
        initiallyDirty = dimension.isDirty();
        initialBiomeScheme = biomeScheme;
        
        initComponents();

        programmaticChange = true;
        try {
            jSpinner1.setValue(dimension.getMinecraftSeed());
            jSpinner1.addChangeListener(this);

            String[] biomeSchemes = new String[] {
                "Minecraft 1.4.7 (or 1.3.1 - 1.4.2) Large Biomes",
                "Minecraft 1.4.7 Default (or 1.2.3 - 1.4.2)",
                "Minecraft 1.1",
                "Minecraft 1.0.0",
                "Minecraft Beta 1.9 prerelease 3 to 6 or RC2",
                "Minecraft Beta 1.8.1",
                "Minecraft Beta 1.6 - 1.7.3"};
            ComboBoxModel biomeSchemeModel = new DefaultComboBoxModel(biomeSchemes);
            jComboBox1.setModel(biomeSchemeModel);
            if (biomeScheme instanceof Minecraft1_7_3BiomeScheme) {
                jComboBox1.setSelectedIndex(6);
            } else if (biomeScheme instanceof Minecraft1_8_1BiomeScheme) {
                jComboBox1.setSelectedIndex(5);
            } else if (biomeScheme instanceof Minecraft1_9BiomeScheme) {
                jComboBox1.setSelectedIndex(4);
            } else if (biomeScheme instanceof Minecraft1_0BiomeScheme) {
                jComboBox1.setSelectedIndex(3);
            } else if (biomeScheme instanceof Minecraft1_1BiomeScheme) {
                jComboBox1.setSelectedIndex(2);
            } else if (biomeScheme instanceof Minecraft1_2BiomeScheme) {
                jComboBox1.setSelectedIndex(1);
            } else if (biomeScheme instanceof Minecraft1_3LargeBiomeScheme) {
                jComboBox1.setSelectedIndex(0);
            } else {
                throw new InternalError(biomeScheme.getClass().getName());
            }
        } finally {
            programmaticChange = false;
        }
        
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("cancel", new AbstractAction("cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }

            private static final long serialVersionUID = 1L;
        });

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        rootPane.setDefaultButton(jButton2);
        
        Rectangle parentBounds = parent.getBounds();
        setLocation(parentBounds.x + (parentBounds.width - getWidth()) / 2, parentBounds.y + parentBounds.height - getHeight());
    }
    
    private void ok() {
        dispose();
    }
    
    private void cancel() {
        if (app.getBiomeScheme() != initialBiomeScheme) {
            if (dimension.getMinecraftSeed() != initialSeed) {
                dimension.setMinecraftSeed(initialSeed);
            }
            app.setBiomeScheme(initialBiomeScheme);
            if (biomeScheme instanceof Minecraft1_7_3BiomeScheme) {
                dimension.getWorld().setBiomeAlgorithm(World2.BIOME_ALGORITHM_1_7_3);
            } else if (biomeScheme instanceof Minecraft1_8_1BiomeScheme) {
                dimension.getWorld().setBiomeAlgorithm(World2.BIOME_ALGORITHM_1_8_1);
            } else if (biomeScheme instanceof Minecraft1_9BiomeScheme) {
                dimension.getWorld().setBiomeAlgorithm(World2.BIOME_ALGORITHM_1_9);
            } else if (biomeScheme instanceof Minecraft1_0BiomeScheme) {
                dimension.getWorld().setBiomeAlgorithm(World2.BIOME_ALGORITHM_1_0_0);
            } else if (biomeScheme instanceof Minecraft1_1BiomeScheme) {
                dimension.getWorld().setBiomeAlgorithm(World2.BIOME_ALGORITHM_1_1);
            } else if (biomeScheme instanceof Minecraft1_2BiomeScheme) {
                dimension.getWorld().setBiomeAlgorithm(World2.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT);
            } else if (biomeScheme instanceof Minecraft1_3LargeBiomeScheme) {
                dimension.getWorld().setBiomeAlgorithm(World2.BIOME_ALGORITHM_1_3_LARGE);
            } else {
                throw new InternalError(biomeScheme.getClass().getName());
            }
            if (! initiallyDirty) {
                dimension.setDirty(false);
            }
        } else if (dimension.getMinecraftSeed() != initialSeed) {
            dimension.setMinecraftSeed(initialSeed);
            dimension.recalculateBiomes(biomeScheme, this);
            if (! initiallyDirty) {
                dimension.setDirty(false);
            }
        }
        dispose();
    }

    // ChangeListener
    
    @Override
    public void stateChanged(ChangeEvent e) {
        if (! programmaticChange) {
            long seed= ((Number) jSpinner1.getValue()).longValue();
            if (seed != dimension.getMinecraftSeed()) {
                dimension.setMinecraftSeed(seed);
                dimension.recalculateBiomes(biomeScheme, this);
            }
        }
    }
    
    private void changeBiomeScheme() {
        int selectedIndex = jComboBox1.getSelectedIndex();
        switch (selectedIndex) {
            case 0:
                app.selectBiomeScheme(World2.BIOME_ALGORITHM_1_3_LARGE);
                break;
            case 1:
                app.selectBiomeScheme(World2.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT);
                break;
            case 2:
                app.selectBiomeScheme(World2.BIOME_ALGORITHM_1_1);
                break;
            case 3:
                app.selectBiomeScheme(World2.BIOME_ALGORITHM_1_0_0);
                break;
            case 4:
                app.selectBiomeScheme(World2.BIOME_ALGORITHM_1_9);
                break;
            case 5:
                app.selectBiomeScheme(World2.BIOME_ALGORITHM_1_8_1);
                break;
            case 6:
                app.selectBiomeScheme(World2.BIOME_ALGORITHM_1_7_3);
                break;
            default:
                throw new InternalError();
        }
        biomeScheme = app.getBiomeScheme();
//        dimension.recalculateBiomes(biomeScheme, this);
        programmaticChange = true;
        try {
            if (biomeScheme instanceof Minecraft1_7_3BiomeScheme) {
                jComboBox1.setSelectedIndex(6);
            } else if (biomeScheme instanceof Minecraft1_8_1BiomeScheme) {
                jComboBox1.setSelectedIndex(5);
            } else if (biomeScheme instanceof Minecraft1_9BiomeScheme) {
                jComboBox1.setSelectedIndex(4);
            } else if (biomeScheme instanceof Minecraft1_0BiomeScheme) {
                jComboBox1.setSelectedIndex(3);
            } else if (biomeScheme instanceof Minecraft1_1BiomeScheme) {
                jComboBox1.setSelectedIndex(2);
            } else if (biomeScheme instanceof Minecraft1_2BiomeScheme) {
                jComboBox1.setSelectedIndex(1);
            } else if (biomeScheme instanceof Minecraft1_3LargeBiomeScheme) {
                jComboBox1.setSelectedIndex(0);
            } else {
                throw new InternalError(biomeScheme.getClass().getName());
            }
        } finally {
            programmaticChange = false;
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

        jLabel1 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Minecraft Seed Browser");

        jLabel1.setText("Minecraft seed:");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(-9223372036854775808L), null, null, Long.valueOf(1L)));
        jSpinner1.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner1, "0"));

        jLabel2.setText("Here you can change the Minecraft seed and");

        jLabel3.setText("immediately see the effect on the biomes.");

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("OK");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel4.setText("Biome scheme:");

        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox1, 0, 213, Short.MAX_VALUE)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jLabel4))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cancel();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        ok();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (! programmaticChange) {
            changeBiomeScheme();
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSpinner jSpinner1;
    // End of variables declaration//GEN-END:variables

    private final App app;
    private final Dimension dimension;
    private final long initialSeed;
    private final boolean initiallyDirty;
    private final BiomeScheme initialBiomeScheme;
    private BiomeScheme biomeScheme;
    private boolean programmaticChange = false;

    private static final long serialVersionUID = 1L;
}