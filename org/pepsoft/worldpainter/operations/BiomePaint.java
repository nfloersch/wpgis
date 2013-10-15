/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.operations;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.pepsoft.worldpainter.BiomeScheme;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MapDragControl;
import org.pepsoft.worldpainter.RadiusControl;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.biomeschemes.AutoBiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import org.pepsoft.worldpainter.biomeschemes.CachingBiomeScheme;
import org.pepsoft.worldpainter.layers.Biome;
import static org.pepsoft.worldpainter.biomeschemes.AutoBiomeScheme.*;

/**
 *
 * @author pepijn
 */
public class BiomePaint extends LayerPaint implements BiomeOperation {
    public BiomePaint(WorldPainterView view, RadiusControl radiusControl, MapDragControl mapDragControl, ColourScheme colourScheme) {
        super(view, radiusControl, mapDragControl, Biome.INSTANCE);
        optionsPanel = new JPanel(new GridLayout(0, 4));
        ButtonGroup buttonGroup = new ButtonGroup();
        BiomeScheme autoBiomeScheme = new AutoBiomeScheme(null);
        for (int i = 0; i < BIOME_ORDER.length; i++) {
            final int biome = BIOME_ORDER[i];
            final JToggleButton button = new JToggleButton(new ImageIcon(BiomeSchemeManager.createImage(autoBiomeScheme, biome, colourScheme)));
            button.putClientProperty(KEY_BIOME, biome);
            button.setMargin(new Insets(2, 2, 2, 2));
            button.setToolTipText(AutoBiomeScheme.BIOME_NAMES[biome]);
            if (i == 0) {
                button.setSelected(true);
            }
            buttonGroup.add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (button.isSelected()) {
                        selectedBiome = biome;
                    }
                }
            });
            optionsPanel.add(button);
        }
    }

    @Override
    public void setBiomeScheme(BiomeScheme biomeScheme) {
        // If biomeScheme is null, assume that custom biomes must be enabled
        // (otherwise the Biomes layer button would not be active at all), in
        // which case use the biomes from the automatic biome scheme
        if (biomeScheme != null) {
            // The seed should always already be set correctly, but in practice
            // this does not always happen. TODO: find out why
            if (getDimension() != null) {
                biomeScheme.setSeed(getDimension().getMinecraftSeed());
            }
            this.biomeScheme = new CachingBiomeScheme(biomeScheme);
            inverseEnabled = (biomeScheme != null);
        } else {
            this.biomeScheme = new CachingBiomeScheme(new AutoBiomeScheme(null));
            inverseEnabled = false;
        }
        for (Component component: optionsPanel.getComponents()) {
            if (component instanceof JToggleButton) {
                JToggleButton button = (JToggleButton) component;
                if ((Integer) button.getClientProperty(KEY_BIOME) >= this.biomeScheme.getBiomeCount()) {
                    button.setEnabled(false);
                    if (button.isSelected()) {
                        button.setSelected(false);
                        selectedBiome = BIOME_PLAINS;
    }
                } else {
                    button.setEnabled(true);
                }
            }
        }
    }
    
    public Component getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    protected void tick(int centerX, int centerY, boolean inverse, boolean first, float level) {
        Dimension dimension = getDimension();
        dimension.setEventsInhibited(true);
        try {
            int radius = getRadius();
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    float strength = level * getStrength(centerX, centerY, x, y);
                    if ((strength > 0.95f) || (random.nextFloat() < strength)) {
                        if (inverse) {
                            if (inverseEnabled) {
                                dimension.setLayerValueAt(Biome.INSTANCE, x, y, biomeScheme.getBiome(x, y));
                            }
                        } else {
                            dimension.setLayerValueAt(Biome.INSTANCE, x, y, selectedBiome);
                        }
                    }
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }
    
    private final JPanel optionsPanel;
    private final Random random = new Random();
    private CachingBiomeScheme biomeScheme;
    private int selectedBiome = BIOME_PLAINS;
    private boolean inverseEnabled;
    
    private static final int[] BIOME_ORDER = {
        BIOME_PLAINS, BIOME_SWAMPLAND, BIOME_EXTREME_HILLS, BIOME_EXTREME_HILLS_EDGE,
        BIOME_JUNGLE, BIOME_JUNGLE_HILLS, BIOME_FOREST, BIOME_FOREST_HILLS,
        BIOME_DESERT, BIOME_DESERT_HILLS, BIOME_TAIGA, BIOME_TAIGA_HILLS,
        BIOME_OCEAN, BIOME_RIVER, BIOME_FROZEN_OCEAN, BIOME_FROZEN_RIVER,
        BIOME_MUSHROOM_ISLAND, BIOME_MUSHROOM_ISLAND_SHORE, BIOME_ICE_PLAINS, BIOME_ICE_MOUNTAINS,
        BIOME_BEACH, BIOME_HELL, BIOME_SKY
    };
    private static final String KEY_BIOME = BiomePaint.class.getName() + ".biome";
}