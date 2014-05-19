/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DimensionPropertiesEditor.java
 *
 * Created on 8-jun-2011, 20:56:18
 */
package org.pepsoft.worldpainter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pepsoft.worldpainter.layers.Caverns;
import org.pepsoft.worldpainter.layers.Chasms;
import org.pepsoft.worldpainter.layers.DeciduousForest;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.Jungle;
import org.pepsoft.worldpainter.layers.PineForest;
import org.pepsoft.worldpainter.layers.Resources;
import org.pepsoft.worldpainter.layers.SwampLand;
import org.pepsoft.worldpainter.layers.exporters.CavernsExporter.CavernsSettings;
import org.pepsoft.worldpainter.layers.exporters.ChasmsExporter.ChasmsSettings;
import org.pepsoft.worldpainter.layers.exporters.FrostExporter.FrostSettings;
import org.pepsoft.worldpainter.layers.exporters.ResourcesExporter.ResourcesExporterSettings;
import org.pepsoft.worldpainter.layers.exporters.TreesExporter.TreeLayerSettings;
import org.pepsoft.worldpainter.themes.TerrainListCellRenderer;

import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.worldpainter.layers.Annotations;
import org.pepsoft.worldpainter.themes.SimpleTheme;
import org.pepsoft.worldpainter.layers.exporters.AnnotationsExporter.AnnotationsSettings;

/**
 *
 * @author pepijn
 */
public class DimensionPropertiesEditor extends javax.swing.JPanel {
    /** Creates new form DimensionPropertiesEditor */
    public DimensionPropertiesEditor() {
        initComponents();

        if ((Configuration.getInstance() != null) && Configuration.getInstance().isEasyMode()) {
            jLabel4.setVisible(false);
            radioButtonNoBorder.setVisible(false);
            radioButtonVoidBorder.setVisible(false);
            radioButtonWaterBorder.setVisible(false);
            jLabel5.setVisible(false);
            spinnerBorderLevel.setVisible(false);
            jLabel44.setVisible(false);
            radioButtonLavaBorder.setVisible(false);
            jLabel8.setVisible(false);
            spinnerBorderSize.setVisible(false);
            jLabel9.setVisible(false);
            checkBoxBedrockWall.setVisible(false);
            jLabel7.setVisible(false);
            spinnerMinecraftSeed.setVisible(false);

            checkBoxPopulate.setVisible(false);
            jLabel47.setVisible(false);
        }

        jSpinner2.setEditor(new NumberEditor(jSpinner2, "0"));
        jSpinner3.setEditor(new NumberEditor(jSpinner3, "0"));
        jSpinner4.setEditor(new NumberEditor(jSpinner4, "0"));
        jSpinner5.setEditor(new NumberEditor(jSpinner5, "0"));
        jSpinner6.setEditor(new NumberEditor(jSpinner6, "0"));
        jSpinner7.setEditor(new NumberEditor(jSpinner7, "0"));
        jSpinner8.setEditor(new NumberEditor(jSpinner8, "0"));
        jSpinner9.setEditor(new NumberEditor(jSpinner9, "0"));
        jSpinner10.setEditor(new NumberEditor(jSpinner10, "0"));
        jSpinner11.setEditor(new NumberEditor(jSpinner11, "0"));
        jSpinner12.setEditor(new NumberEditor(jSpinner12, "0"));
        jSpinner13.setEditor(new NumberEditor(jSpinner13, "0"));
        jSpinner14.setEditor(new NumberEditor(jSpinner14, "0"));
        jSpinner15.setEditor(new NumberEditor(jSpinner15, "0"));
        jSpinner16.setEditor(new NumberEditor(jSpinner16, "0"));
        jSpinner17.setEditor(new NumberEditor(jSpinner17, "0"));
        jSpinner18.setEditor(new NumberEditor(jSpinner18, "0"));
        jSpinner19.setEditor(new NumberEditor(jSpinner19, "0"));
        jSpinner20.setEditor(new NumberEditor(jSpinner20, "0"));
        jSpinner21.setEditor(new NumberEditor(jSpinner21, "0"));
        jSpinner22.setEditor(new NumberEditor(jSpinner22, "0"));
        jSpinner23.setEditor(new NumberEditor(jSpinner23, "0"));
        jSpinner24.setEditor(new NumberEditor(jSpinner24, "0"));
        jSpinner25.setEditor(new NumberEditor(jSpinner25, "0"));
        jSpinner26.setEditor(new NumberEditor(jSpinner26, "0"));
        jSpinner27.setEditor(new NumberEditor(jSpinner27, "0"));
        jSpinner28.setEditor(new NumberEditor(jSpinner28, "0"));
        jSpinner29.setEditor(new NumberEditor(jSpinner29, "0"));
        jSpinner30.setEditor(new NumberEditor(jSpinner30, "0"));
        jSpinner31.setEditor(new NumberEditor(jSpinner31, "0"));
        jSpinner32.setEditor(new NumberEditor(jSpinner32, "0"));
        jSpinner33.setEditor(new NumberEditor(jSpinner33, "0"));
        jSpinner34.setEditor(new NumberEditor(jSpinner34, "0"));
        spinnerCavernsMinLevel.setEditor(new NumberEditor(spinnerCavernsMinLevel, "0"));
        spinnerCavernsMaxLevel.setEditor(new NumberEditor(spinnerCavernsMaxLevel, "0"));
        spinnerChasmsMinLevel.setEditor(new NumberEditor(spinnerChasmsMinLevel, "0"));
        spinnerChasmsMaxLevel.setEditor(new NumberEditor(spinnerChasmsMaxLevel, "0"));
        
        addListeners(jSpinner24,  jSpinner3);
        addListeners(jSpinner25,  jSpinner5);
        addListeners(jSpinner26,  jSpinner7);
        addListeners(jSpinner27,  jSpinner9);
        addListeners(jSpinner28, jSpinner11);
        addListeners(jSpinner29, jSpinner23);
        addListeners(jSpinner30, jSpinner15);
        addListeners(jSpinner31, jSpinner18);
        addListeners(jSpinner32, jSpinner19);
        addListeners(jSpinner33, jSpinner21);
        addListeners(jSpinner34, jSpinner13);
        addListeners(spinnerCavernsMinLevel, spinnerCavernsMaxLevel);
        addListeners(spinnerChasmsMinLevel, spinnerChasmsMaxLevel);
    }

    public void setColourScheme(ColourScheme colourScheme) {
        comboBoxSubsurfaceMaterial.setRenderer(new TerrainListCellRenderer(colourScheme));
        themeEditor.setColourScheme(colourScheme);
    }
    
    public ColourScheme getColourScheme() {
        return themeEditor.getColourScheme();
    }
    
    public void setExportMode() {
        if (! exportMode) {
            exportMode = true;
            jTabbedPane1.remove(1);
        }
    }
    
    public void setDefaultSettingsMode() {
        if (! defaultSettingsMode) {
            defaultSettingsMode = true;
            spinnerMinecraftSeed.setEnabled(false);
        }
    }
    
    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
        if (dimension != null) {
            loadSettings();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkBoxBedrockWall.setEnabled(enabled);
        comboBoxSubsurfaceMaterial.setEnabled(enabled);
        checkBoxCavernsEverywhere.setEnabled(enabled);
        checkBoxChasmsEverywhere.setEnabled(enabled);
        checkBoxDeciduousEverywhere.setEnabled(enabled);
        jCheckBox3.setEnabled(enabled);
        checkBoxFloodCaverns.setEnabled(enabled);
        checkBoxCavernsBreakSurface.setEnabled(enabled);
        checkBoxChasmsBreakSurface.setEnabled(enabled);
        checkBoxPineEverywhere.setEnabled(enabled);
        checkBoxJungleEverywhere.setEnabled(enabled);
        checkBoxSwamplandEverywhere.setEnabled(enabled);
        jCheckBox8.setEnabled(enabled);
        jCheckBox9.setEnabled(enabled);
        jTabbedPane1.setEnabled(enabled);
        radioButtonLavaBorder.setEnabled(enabled);
        radioButtonNoBorder.setEnabled(enabled);
        radioButtonVoidBorder.setEnabled(enabled);
        radioButtonWaterBorder.setEnabled(enabled);
        jSpinner2.setEnabled(enabled);
        jSpinner3.setEnabled(enabled);
        jSpinner4.setEnabled(enabled);
        jSpinner5.setEnabled(enabled);
        jSpinner6.setEnabled(enabled);
        jSpinner7.setEnabled(enabled);
        jSpinner8.setEnabled(enabled);
        jSpinner9.setEnabled(enabled);
        jSpinner10.setEnabled(enabled);
        jSpinner11.setEnabled(enabled);
        jSpinner12.setEnabled(enabled);
        jSpinner13.setEnabled(enabled);
        jSpinner14.setEnabled(enabled);
        jSpinner15.setEnabled(enabled);
        jSpinner16.setEnabled(enabled);
        jSpinner17.setEnabled(enabled);
        jSpinner18.setEnabled(enabled);
        jSpinner19.setEnabled(enabled);
        jSpinner20.setEnabled(enabled);
        jSpinner21.setEnabled(enabled);
        jSpinner22.setEnabled(enabled);
        jSpinner23.setEnabled(enabled);
        jSpinner24.setEnabled(enabled);
        jSpinner25.setEnabled(enabled);
        jSpinner26.setEnabled(enabled);
        jSpinner27.setEnabled(enabled);
        jSpinner28.setEnabled(enabled);
        jSpinner29.setEnabled(enabled);
        jSpinner30.setEnabled(enabled);
        jSpinner31.setEnabled(enabled);
        jSpinner32.setEnabled(enabled);
        jSpinner33.setEnabled(enabled);
        jSpinner34.setEnabled(enabled);
        themeEditor.setEnabled(enabled);
        spinnerMinSurfaceDepth.setEnabled(enabled);
        spinnerMaxSurfaceDepth.setEnabled(enabled);
        checkBoxBottomless.setEnabled(enabled);
        spinnerCavernsMinLevel.setEnabled(enabled);
        spinnerCavernsMaxLevel.setEnabled(enabled);
        spinnerChasmsMinLevel.setEnabled(enabled);
        spinnerChasmsMaxLevel.setEnabled(enabled);
        checkBoxCoverSteepTerrain.setEnabled(enabled);
        checkBoxExportAnnotations.setEnabled(enabled);
        checkBoxSnowUnderTrees.setEnabled(enabled);
        setControlStates();
    }
    
    public boolean saveSettings() {
        int maxHeight = dimension.getMaxHeight() - 1;
        
        // terrain ranges
        if ((! exportMode) && (! themeEditor.save())) {
            jTabbedPane1.setSelectedIndex(1);
            return false;
        }
        
        // general
        int topLayerMinDepth = (Integer) spinnerMinSurfaceDepth.getValue();
        dimension.setTopLayerMinDepth(topLayerMinDepth);
        dimension.setTopLayerVariation((Integer) spinnerMaxSurfaceDepth.getValue() - topLayerMinDepth);
        dimension.setSubsurfaceMaterial((Terrain) comboBoxSubsurfaceMaterial.getSelectedItem());
        if (radioButtonLavaBorder.isSelected()) {
            dimension.setBorder(Dimension.Border.LAVA);
        } else if (radioButtonNoBorder.isSelected()) {
            dimension.setBorder(null);
        } else if (radioButtonVoidBorder.isSelected()) {
            dimension.setBorder(Dimension.Border.VOID);
        } else {
            dimension.setBorder(Dimension.Border.WATER);
        }
        dimension.setBorderLevel((Integer) spinnerBorderLevel.getValue());
        dimension.setBorderSize((Integer) spinnerBorderSize.getValue() / 128);
        dimension.setBedrockWall(checkBoxBedrockWall.isSelected());
        long previousSeed = dimension.getMinecraftSeed();
        long newSeed = ((Number) spinnerMinecraftSeed.getValue()).longValue();
        if (newSeed != previousSeed) {
            dimension.setMinecraftSeed(newSeed);
        }
        dimension.setBottomless(checkBoxBottomless.isSelected());
        dimension.setCoverSteepTerrain(checkBoxCoverSteepTerrain.isSelected());

        // caverns
        CavernsSettings cavernsSettings = (CavernsSettings) dimension.getLayerSettings(Caverns.INSTANCE);
        if (cavernsSettings == null) {
            cavernsSettings = new CavernsSettings();
        }
        if (checkBoxCavernsEverywhere.isSelected()) {
            int cavernsEverywhereLevel = Math.round((((Integer) sliderCavernsEverywhereLevel.getValue()) + 2) / 6.667f);
            cavernsSettings.setCavernsEverywhereLevel(cavernsEverywhereLevel);
        } else {
            cavernsSettings.setCavernsEverywhereLevel(0);
        }
        if (checkBoxFloodCaverns.isSelected()) {
            cavernsSettings.setWaterLevel((Integer) spinnerCavernsFloodLevel.getValue());
        } else {
            cavernsSettings.setWaterLevel(0);
        }
        cavernsSettings.setFloodWithLava(checkBoxCavernsFloodWithLava.isSelected());
        cavernsSettings.setSurfaceBreaking(checkBoxCavernsBreakSurface.isSelected());
        cavernsSettings.setLeaveWater(! checkBoxCavernsRemoveWater.isSelected());
        cavernsSettings.setMinimumLevel((Integer) spinnerCavernsMinLevel.getValue());
        int cavernsMaxLevel = (Integer) spinnerCavernsMaxLevel.getValue();
        cavernsSettings.setMaximumLevel((cavernsMaxLevel >= maxHeight) ? Integer.MAX_VALUE : cavernsMaxLevel);
        dimension.setLayerSettings(Caverns.INSTANCE, cavernsSettings);
        
        // chasms
        ChasmsSettings chasmsSettings = (ChasmsSettings) dimension.getLayerSettings(Chasms.INSTANCE);
        if (chasmsSettings == null) {
            chasmsSettings = new ChasmsSettings();
        }
        if (checkBoxChasmsEverywhere.isSelected()) {
            int chasmsEverywhereLevel = Math.round((((Integer) sliderChasmsEverywhereLevel.getValue()) + 2) / 6.667f);
            chasmsSettings.setChasmsEverywhereLevel(chasmsEverywhereLevel);
        } else {
            chasmsSettings.setChasmsEverywhereLevel(0);
        }
        chasmsSettings.setSurfaceBreaking(checkBoxChasmsBreakSurface.isSelected());
        // Other settings copied from Caverns layer
        if (checkBoxFloodCaverns.isSelected()) {
            chasmsSettings.setWaterLevel((Integer) spinnerCavernsFloodLevel.getValue());
        } else {
            chasmsSettings.setWaterLevel(0);
        }
        chasmsSettings.setFloodWithLava(checkBoxCavernsFloodWithLava.isSelected());
        chasmsSettings.setLeaveWater(! checkBoxCavernsRemoveWater.isSelected());
        chasmsSettings.setMinimumLevel((Integer) spinnerChasmsMinLevel.getValue());
        chasmsSettings.setMaximumLevel((Integer) spinnerChasmsMaxLevel.getValue());
        dimension.setLayerSettings(Chasms.INSTANCE, chasmsSettings);
        
        // populate
        dimension.setPopulate(checkBoxPopulate.isSelected());
        
        // deciduous
        TreeLayerSettings<DeciduousForest> deciduousSettings = (TreeLayerSettings<DeciduousForest>) dimension.getLayerSettings(DeciduousForest.INSTANCE);
        if (deciduousSettings == null) {
            deciduousSettings = new TreeLayerSettings<DeciduousForest>(DeciduousForest.INSTANCE);
        }
        if (checkBoxDeciduousEverywhere.isSelected()) {
            int minimumLevel = Math.round((((Integer) sliderDeciduousLevel.getValue()) + 2) / 6.667f);
            deciduousSettings.setMinimumLevel(minimumLevel);
        } else {
            deciduousSettings.setMinimumLevel(0);
        }
        dimension.setLayerSettings(DeciduousForest.INSTANCE, deciduousSettings);
        
        // pine
        TreeLayerSettings<PineForest> pineSettings = (TreeLayerSettings<PineForest>) dimension.getLayerSettings(PineForest.INSTANCE);
        if (pineSettings == null) {
            pineSettings = new TreeLayerSettings<PineForest>(PineForest.INSTANCE);
        }
        if (checkBoxPineEverywhere.isSelected()) {
            int minimumLevel = Math.round((((Integer) sliderPineLevel.getValue()) + 2) / 6.667f);
            pineSettings.setMinimumLevel(minimumLevel);
        } else {
            pineSettings.setMinimumLevel(0);
        }
        dimension.setLayerSettings(PineForest.INSTANCE, pineSettings);
        
        // jungle
        TreeLayerSettings<Jungle> jungleSettings = (TreeLayerSettings<Jungle>) dimension.getLayerSettings(Jungle.INSTANCE);
        if (jungleSettings == null) {
            jungleSettings = new TreeLayerSettings<Jungle>(Jungle.INSTANCE);
        }
        if (checkBoxJungleEverywhere.isSelected()) {
            int minimumLevel = Math.round((((Integer) sliderJungleLevel.getValue()) + 2) / 6.667f);
            jungleSettings.setMinimumLevel(minimumLevel);
        } else {
            jungleSettings.setMinimumLevel(0);
        }
        dimension.setLayerSettings(Jungle.INSTANCE, jungleSettings);
        
        // swampland
        TreeLayerSettings<SwampLand> swampLandSettings = (TreeLayerSettings<SwampLand>) dimension.getLayerSettings(SwampLand.INSTANCE);
        if (swampLandSettings == null) {
            swampLandSettings = new TreeLayerSettings<SwampLand>(SwampLand.INSTANCE);
        }
        if (checkBoxSwamplandEverywhere.isSelected()) {
            int minimumLevel = Math.round((((Integer) jSlider6.getValue()) + 2) / 6.667f);
            swampLandSettings.setMinimumLevel(minimumLevel);
        } else {
            swampLandSettings.setMinimumLevel(0);
        }
        dimension.setLayerSettings(SwampLand.INSTANCE, swampLandSettings);
        
        // frost
        FrostSettings frostSettings = (FrostSettings) dimension.getLayerSettings(Frost.INSTANCE);
        if (frostSettings == null) {
            frostSettings = new FrostSettings();
        }
        frostSettings.setFrostEverywhere(jCheckBox3.isSelected());
        frostSettings.setMode(jCheckBox9.isSelected() ? 2 : 0);
        frostSettings.setSnowUnderTrees(checkBoxSnowUnderTrees.isSelected());
        dimension.setLayerSettings(Frost.INSTANCE, frostSettings);
        
        // resources
        ResourcesExporterSettings resourcesSettings = (ResourcesExporterSettings) dimension.getLayerSettings(Resources.INSTANCE);
        if (resourcesSettings == null) {
            resourcesSettings = new ResourcesExporterSettings(dimension.getMaxHeight());
        }
        if (jCheckBox8.isSelected()) {
            int minimumLevel = Math.round((((Integer) jSlider4.getValue()) + 2) / 6.667f);
            resourcesSettings.setMinimumLevel(minimumLevel);
        } else {
            resourcesSettings.setMinimumLevel(0);
        }
        resourcesSettings.setChance(BLK_GOLD_ORE, (Integer) jSpinner2.getValue());
        resourcesSettings.setMinLevel(BLK_GOLD_ORE, (Integer) jSpinner24.getValue());
        resourcesSettings.setMaxLevel(BLK_GOLD_ORE, (Integer) jSpinner3.getValue());
        resourcesSettings.setChance(BLK_IRON_ORE, (Integer) jSpinner4.getValue());
        resourcesSettings.setMinLevel(BLK_IRON_ORE, (Integer) jSpinner25.getValue());
        resourcesSettings.setMaxLevel(BLK_IRON_ORE, (Integer) jSpinner5.getValue());
        resourcesSettings.setChance(BLK_COAL, (Integer) jSpinner6.getValue());
        resourcesSettings.setMinLevel(BLK_COAL, (Integer) jSpinner26.getValue());
        resourcesSettings.setMaxLevel(BLK_COAL, (Integer) jSpinner7.getValue());
        resourcesSettings.setChance(BLK_LAPIS_LAZULI_ORE, (Integer) jSpinner8.getValue());
        resourcesSettings.setMinLevel(BLK_LAPIS_LAZULI_ORE, (Integer) jSpinner27.getValue());
        resourcesSettings.setMaxLevel(BLK_LAPIS_LAZULI_ORE, (Integer) jSpinner9.getValue());
        resourcesSettings.setChance(BLK_DIAMOND_ORE, (Integer) jSpinner10.getValue());
        resourcesSettings.setMinLevel(BLK_DIAMOND_ORE, (Integer) jSpinner28.getValue());
        resourcesSettings.setMaxLevel(BLK_DIAMOND_ORE, (Integer) jSpinner11.getValue());
        resourcesSettings.setChance(BLK_REDSTONE_ORE, (Integer) jSpinner12.getValue());
        resourcesSettings.setMinLevel(BLK_REDSTONE_ORE, (Integer) jSpinner34.getValue());
        resourcesSettings.setMaxLevel(BLK_REDSTONE_ORE, (Integer) jSpinner13.getValue());
        resourcesSettings.setChance(BLK_WATER, (Integer) jSpinner14.getValue());
        resourcesSettings.setMinLevel(BLK_WATER, (Integer) jSpinner30.getValue());
        resourcesSettings.setMaxLevel(BLK_WATER, (Integer) jSpinner15.getValue());
        resourcesSettings.setChance(BLK_LAVA, (Integer) jSpinner16.getValue());
        resourcesSettings.setMinLevel(BLK_LAVA, (Integer) jSpinner31.getValue());
        resourcesSettings.setMaxLevel(BLK_LAVA, (Integer) jSpinner18.getValue());
        resourcesSettings.setChance(BLK_DIRT, (Integer) jSpinner17.getValue());
        resourcesSettings.setMinLevel(BLK_DIRT, (Integer) jSpinner32.getValue());
        resourcesSettings.setMaxLevel(BLK_DIRT, (Integer) jSpinner19.getValue());
        resourcesSettings.setChance(BLK_GRAVEL, (Integer) jSpinner20.getValue());
        resourcesSettings.setMinLevel(BLK_GRAVEL, (Integer) jSpinner33.getValue());
        resourcesSettings.setMaxLevel(BLK_GRAVEL, (Integer) jSpinner21.getValue());
        resourcesSettings.setChance(BLK_EMERALD_ORE, (Integer) jSpinner22.getValue());
        resourcesSettings.setMinLevel(BLK_EMERALD_ORE, (Integer) jSpinner29.getValue());
        resourcesSettings.setMaxLevel(BLK_EMERALD_ORE, (Integer) jSpinner23.getValue());
        dimension.setLayerSettings(Resources.INSTANCE, resourcesSettings);
        
        // annotations
        AnnotationsSettings annotationsSettings = (AnnotationsSettings) dimension.getLayerSettings(Annotations.INSTANCE);
        if (annotationsSettings == null) {
            annotationsSettings = new AnnotationsSettings();
        }
        annotationsSettings.setExport(checkBoxExportAnnotations.isSelected());
        dimension.setLayerSettings(Annotations.INSTANCE, annotationsSettings);
        
        return true;
    }
    
    private void loadSettings() {
        int maxHeight = dimension.getMaxHeight() - 1;
        
        // general
        ((SpinnerNumberModel) spinnerMinSurfaceDepth.getModel()).setMaximum(maxHeight);
        spinnerMinSurfaceDepth.setValue(dimension.getTopLayerMinDepth());
        ((SpinnerNumberModel) spinnerMaxSurfaceDepth.getModel()).setMaximum(maxHeight);
        spinnerMaxSurfaceDepth.setValue(dimension.getTopLayerMinDepth() + dimension.getTopLayerVariation());
        if (dimension.getBorder() != null) {
            switch (dimension.getBorder()) {
                case LAVA:
                    radioButtonLavaBorder.setSelected(true);
                    break;
                case WATER:
                    radioButtonWaterBorder.setSelected(true);
                    break;
                case VOID:
                    radioButtonVoidBorder.setSelected(true);
                    break;
                default:
                    throw new InternalError();
            }
        } else {
            radioButtonNoBorder.setSelected(true);
        }
        ((SpinnerNumberModel) spinnerBorderLevel.getModel()).setMaximum(maxHeight);
        spinnerBorderLevel.setValue(dimension.getBorderLevel());
        spinnerBorderSize.setValue(dimension.getBorderSize() * 128);
        checkBoxBedrockWall.setSelected(dimension.isBedrockWall());
        spinnerMinecraftSeed.setValue(dimension.getMinecraftSeed());
        checkBoxBottomless.setSelected(dimension.isBottomless());
        checkBoxCoverSteepTerrain.setSelected(dimension.isCoverSteepTerrain());

        List<Terrain> materialList = new ArrayList<Terrain>(Arrays.asList(Terrain.VALUES));
        for (Iterator<Terrain> i = materialList.iterator(); i.hasNext(); ) {
            Terrain terrain = i.next();
            if ((terrain.isCustom() && (! terrain.isConfigured())) || (terrain == Terrain.GRASS) || (terrain == Terrain.DESERT)) {
                i.remove();
            }
        }
        comboBoxSubsurfaceMaterial.setModel(new DefaultComboBoxModel(materialList.toArray()));
        comboBoxSubsurfaceMaterial.setSelectedItem(dimension.getSubsurfaceMaterial());

        // caverns
        CavernsSettings cavernsSettings = (CavernsSettings) dimension.getLayerSettings(Caverns.INSTANCE);
        if (cavernsSettings == null) {
            cavernsSettings = new CavernsSettings();
        }
        if (cavernsSettings.getCavernsEverywhereLevel() > 0) {
            checkBoxCavernsEverywhere.setSelected(true);
            sliderCavernsEverywhereLevel.setValue(Math.round(cavernsSettings.getCavernsEverywhereLevel() * 6.667f));
        } else {
            checkBoxCavernsEverywhere.setSelected(false);
            sliderCavernsEverywhereLevel.setValue(50);
        }
        ((SpinnerNumberModel) spinnerCavernsFloodLevel.getModel()).setMaximum(maxHeight);
        if (cavernsSettings.getWaterLevel() > 0) {
            checkBoxFloodCaverns.setSelected(true);
            spinnerCavernsFloodLevel.setValue(cavernsSettings.getWaterLevel());
        } else {
            checkBoxFloodCaverns.setSelected(false);
            spinnerCavernsFloodLevel.setValue(8);
        }
        checkBoxCavernsFloodWithLava.setSelected(cavernsSettings.isFloodWithLava());
        checkBoxCavernsBreakSurface.setSelected(cavernsSettings.isSurfaceBreaking());
        checkBoxCavernsRemoveWater.setSelected(! cavernsSettings.isLeaveWater());
        ((SpinnerNumberModel) spinnerCavernsMinLevel.getModel()).setMaximum(maxHeight);
        spinnerCavernsMinLevel.setValue(cavernsSettings.getMinimumLevel());
        ((SpinnerNumberModel) spinnerCavernsMaxLevel.getModel()).setMaximum(maxHeight);
        spinnerCavernsMaxLevel.setValue(Math.min(cavernsSettings.getMaximumLevel(), maxHeight));
        
        // chasms
        ChasmsSettings chasmsSettings = (ChasmsSettings) dimension.getLayerSettings(Chasms.INSTANCE);
        if (chasmsSettings == null) {
            chasmsSettings = new ChasmsSettings();
        }
        if (chasmsSettings.getChasmsEverywhereLevel() > 0) {
            checkBoxChasmsEverywhere.setSelected(true);
            sliderChasmsEverywhereLevel.setValue(Math.round(chasmsSettings.getChasmsEverywhereLevel() * 6.667f));
        } else {
            checkBoxChasmsEverywhere.setSelected(false);
            sliderChasmsEverywhereLevel.setValue(50);
        }
        checkBoxChasmsBreakSurface.setSelected(chasmsSettings.isSurfaceBreaking());
        ((SpinnerNumberModel) spinnerChasmsMinLevel.getModel()).setMaximum(maxHeight);
        spinnerChasmsMinLevel.setValue(chasmsSettings.getMinimumLevel());
        ((SpinnerNumberModel) spinnerChasmsMaxLevel.getModel()).setMaximum(maxHeight);
        spinnerChasmsMaxLevel.setValue(Math.min(chasmsSettings.getMaximumLevel(), maxHeight));
        
        // populate
        checkBoxPopulate.setSelected(dimension.isPopulate());
        
        // deciduous
        TreeLayerSettings<DeciduousForest> deciduousSettings = (TreeLayerSettings<DeciduousForest>) dimension.getLayerSettings(DeciduousForest.INSTANCE);
        if (deciduousSettings == null) {
            deciduousSettings = new TreeLayerSettings<DeciduousForest>(DeciduousForest.INSTANCE);
        }
        if (deciduousSettings.getMinimumLevel() > 0) {
            checkBoxDeciduousEverywhere.setSelected(true);
            sliderDeciduousLevel.setValue(Math.round(deciduousSettings.getMinimumLevel() * 6.667f));
        } else {
            checkBoxDeciduousEverywhere.setSelected(false);
            sliderDeciduousLevel.setValue(50);
        }
        
        // pine
        TreeLayerSettings<PineForest> pineSettings = (TreeLayerSettings<PineForest>) dimension.getLayerSettings(PineForest.INSTANCE);
        if (pineSettings == null) {
            pineSettings = new TreeLayerSettings<PineForest>(PineForest.INSTANCE);
        }
        if (pineSettings.getMinimumLevel() > 0) {
            checkBoxPineEverywhere.setSelected(true);
            sliderPineLevel.setValue(Math.round(pineSettings.getMinimumLevel() * 6.667f));
        } else {
            checkBoxPineEverywhere.setSelected(false);
            sliderPineLevel.setValue(50);
        }
        
        // jungle
        TreeLayerSettings<Jungle> jungleSettings = (TreeLayerSettings<Jungle>) dimension.getLayerSettings(Jungle.INSTANCE);
        if (jungleSettings == null) {
            jungleSettings = new TreeLayerSettings<Jungle>(Jungle.INSTANCE);
        }
        if (jungleSettings.getMinimumLevel() > 0) {
            checkBoxJungleEverywhere.setSelected(true);
            sliderJungleLevel.setValue(Math.round(jungleSettings.getMinimumLevel() * 6.667f));
        } else {
            checkBoxJungleEverywhere.setSelected(false);
            sliderJungleLevel.setValue(50);
        }
        
        // swamp
        TreeLayerSettings<SwampLand> swampLandSettings = (TreeLayerSettings<SwampLand>) dimension.getLayerSettings(SwampLand.INSTANCE);
        if (swampLandSettings == null) {
            swampLandSettings = new TreeLayerSettings<SwampLand>(SwampLand.INSTANCE);
        }
        if (swampLandSettings.getMinimumLevel() > 0) {
            checkBoxSwamplandEverywhere.setSelected(true);
            jSlider6.setValue(Math.round(swampLandSettings.getMinimumLevel() * 6.667f));
        } else {
            checkBoxSwamplandEverywhere.setSelected(false);
            jSlider6.setValue(50);
        }
        
        // frost
        FrostSettings frostSettings = (FrostSettings) dimension.getLayerSettings(Frost.INSTANCE);
        if (frostSettings == null) {
            frostSettings = new FrostSettings();
        }
        jCheckBox3.setSelected(frostSettings.isFrostEverywhere());
        jCheckBox9.setSelected(frostSettings.getMode() == 2);
        checkBoxSnowUnderTrees.setSelected(frostSettings.isSnowUnderTrees());
        
        // resources
        ResourcesExporterSettings resourcesSettings = (ResourcesExporterSettings) dimension.getLayerSettings(Resources.INSTANCE);
        if (resourcesSettings == null) {
            resourcesSettings = new ResourcesExporterSettings(dimension.getMaxHeight());
            resourcesSettings.setMinimumLevel(0);
        }
        jCheckBox8.setSelected(resourcesSettings.isApplyEverywhere());
        if (resourcesSettings.isApplyEverywhere()) {
            jSlider4.setValue(Math.round(resourcesSettings.getMinimumLevel() * 6.667f));
        } else {
            jSlider4.setValue(50);
        }
        jSpinner2.setValue(resourcesSettings.getChance(BLK_GOLD_ORE));
        ((SpinnerNumberModel) jSpinner24.getModel()).setMaximum(maxHeight);
        jSpinner24.setValue(clamp(resourcesSettings.getMinLevel(BLK_GOLD_ORE), maxHeight));
        ((SpinnerNumberModel) jSpinner3.getModel()).setMaximum(maxHeight);
        jSpinner3.setValue(clamp(resourcesSettings.getMaxLevel(BLK_GOLD_ORE), maxHeight));
        jSpinner4.setValue(resourcesSettings.getChance(BLK_IRON_ORE));
        ((SpinnerNumberModel) jSpinner25.getModel()).setMaximum(maxHeight);
        jSpinner25.setValue(clamp(resourcesSettings.getMinLevel(BLK_IRON_ORE), maxHeight));
        ((SpinnerNumberModel) jSpinner5.getModel()).setMaximum(maxHeight);
        jSpinner5.setValue(clamp(resourcesSettings.getMaxLevel(BLK_IRON_ORE), maxHeight));
        jSpinner6.setValue(resourcesSettings.getChance(BLK_COAL));
        ((SpinnerNumberModel) jSpinner26.getModel()).setMaximum(maxHeight);
        jSpinner26.setValue(clamp(resourcesSettings.getMinLevel(BLK_COAL), maxHeight));
        ((SpinnerNumberModel) jSpinner7.getModel()).setMaximum(maxHeight);
        jSpinner7.setValue(clamp(resourcesSettings.getMaxLevel(BLK_COAL), maxHeight));
        jSpinner8.setValue(resourcesSettings.getChance(BLK_LAPIS_LAZULI_ORE));
        ((SpinnerNumberModel) jSpinner27.getModel()).setMaximum(maxHeight);
        jSpinner27.setValue(clamp(resourcesSettings.getMinLevel(BLK_LAPIS_LAZULI_ORE), maxHeight));
        ((SpinnerNumberModel) jSpinner9.getModel()).setMaximum(maxHeight);
        jSpinner9.setValue(clamp(resourcesSettings.getMaxLevel(BLK_LAPIS_LAZULI_ORE), maxHeight));
        jSpinner10.setValue(resourcesSettings.getChance(BLK_DIAMOND_ORE));
        ((SpinnerNumberModel) jSpinner28.getModel()).setMaximum(maxHeight);
        jSpinner28.setValue(clamp(resourcesSettings.getMinLevel(BLK_DIAMOND_ORE), maxHeight));
        ((SpinnerNumberModel) jSpinner11.getModel()).setMaximum(maxHeight);
        jSpinner11.setValue(clamp(resourcesSettings.getMaxLevel(BLK_DIAMOND_ORE), maxHeight));
        jSpinner12.setValue(resourcesSettings.getChance(BLK_REDSTONE_ORE));
        ((SpinnerNumberModel) jSpinner34.getModel()).setMaximum(maxHeight);
        jSpinner34.setValue(clamp(resourcesSettings.getMinLevel(BLK_REDSTONE_ORE), maxHeight));
        ((SpinnerNumberModel) jSpinner13.getModel()).setMaximum(maxHeight);
        jSpinner13.setValue(clamp(resourcesSettings.getMaxLevel(BLK_REDSTONE_ORE), maxHeight));
        jSpinner14.setValue(resourcesSettings.getChance(BLK_WATER));
        ((SpinnerNumberModel) jSpinner30.getModel()).setMaximum(maxHeight);
        jSpinner30.setValue(clamp(resourcesSettings.getMinLevel(BLK_WATER), maxHeight));
        ((SpinnerNumberModel) jSpinner15.getModel()).setMaximum(maxHeight);
        jSpinner15.setValue(clamp(resourcesSettings.getMaxLevel(BLK_WATER), maxHeight));
        jSpinner16.setValue(resourcesSettings.getChance(BLK_LAVA));
        ((SpinnerNumberModel) jSpinner31.getModel()).setMaximum(maxHeight);
        jSpinner31.setValue(clamp(resourcesSettings.getMinLevel(BLK_LAVA), maxHeight));
        ((SpinnerNumberModel) jSpinner18.getModel()).setMaximum(maxHeight);
        jSpinner18.setValue(clamp(resourcesSettings.getMaxLevel(BLK_LAVA), maxHeight));
        jSpinner17.setValue(resourcesSettings.getChance(BLK_DIRT));
        ((SpinnerNumberModel) jSpinner32.getModel()).setMaximum(maxHeight);
        jSpinner32.setValue(clamp(resourcesSettings.getMinLevel(BLK_DIRT), maxHeight));
        ((SpinnerNumberModel) jSpinner19.getModel()).setMaximum(maxHeight);
        jSpinner19.setValue(clamp(resourcesSettings.getMaxLevel(BLK_DIRT), maxHeight));
        jSpinner20.setValue(resourcesSettings.getChance(BLK_GRAVEL));
        ((SpinnerNumberModel) jSpinner33.getModel()).setMaximum(maxHeight);
        jSpinner33.setValue(clamp(resourcesSettings.getMinLevel(BLK_GRAVEL), maxHeight));
        ((SpinnerNumberModel) jSpinner21.getModel()).setMaximum(maxHeight);
        jSpinner21.setValue(clamp(resourcesSettings.getMaxLevel(BLK_GRAVEL), maxHeight));
        jSpinner22.setValue(resourcesSettings.getChance(BLK_EMERALD_ORE));
        ((SpinnerNumberModel) jSpinner29.getModel()).setMaximum(maxHeight);
        jSpinner29.setValue(clamp(resourcesSettings.getMinLevel(BLK_EMERALD_ORE), maxHeight));
        ((SpinnerNumberModel) jSpinner23.getModel()).setMaximum(maxHeight);
        jSpinner23.setValue(clamp(resourcesSettings.getMaxLevel(BLK_EMERALD_ORE), maxHeight));
        
        // terrain ranges
        if (! exportMode) {
            if ((dimension.getTileFactory() instanceof HeightMapTileFactory)
                    && (((HeightMapTileFactory) dimension.getTileFactory()).getTheme() instanceof SimpleTheme)
                    && (((SimpleTheme) ((HeightMapTileFactory) dimension.getTileFactory()).getTheme()).getTerrainRanges() != null)) {
                themeEditor.setTheme((SimpleTheme) ((HeightMapTileFactory) dimension.getTileFactory()).getTheme());
            } else {
                jTabbedPane1.setEnabledAt(1, false);
            }
        }
        
        // annotations
        AnnotationsSettings annotationsSettings = (AnnotationsSettings) dimension.getLayerSettings(Annotations.INSTANCE);
        if (annotationsSettings == null) {
            annotationsSettings = new AnnotationsSettings();
        }
        checkBoxExportAnnotations.setSelected(annotationsSettings.isExport());
        
        setControlStates();
    }
    
    private int clamp(int value, int maxValue) {
        if (value < 0) {
            return 0;
        } else if (value > maxValue) {
            return maxValue;
        } else {
            return value;
        }
    }
    
    private void setControlStates() {
        boolean enabled = isEnabled();
        boolean dim0 = (dimension != null) && (dimension.getDim() == Constants.DIM_NORMAL);
        spinnerBorderLevel.setEnabled(enabled && (radioButtonLavaBorder.isSelected() || radioButtonWaterBorder.isSelected()));
        spinnerBorderSize.setEnabled(enabled && (! radioButtonNoBorder.isSelected()));
        sliderCavernsEverywhereLevel.setEnabled(enabled && checkBoxCavernsEverywhere.isSelected());
        sliderChasmsEverywhereLevel.setEnabled(enabled && checkBoxChasmsEverywhere.isSelected());
        spinnerCavernsFloodLevel.setEnabled(enabled && checkBoxFloodCaverns.isSelected());
        checkBoxCavernsFloodWithLava.setEnabled(enabled && checkBoxFloodCaverns.isSelected());
        sliderDeciduousLevel.setEnabled(enabled && checkBoxDeciduousEverywhere.isSelected());
        sliderPineLevel.setEnabled(enabled && checkBoxPineEverywhere.isSelected());
        sliderJungleLevel.setEnabled(enabled && checkBoxJungleEverywhere.isSelected());
        jSlider6.setEnabled(enabled && checkBoxSwamplandEverywhere.isSelected());
        jSlider4.setEnabled(enabled && jCheckBox8.isSelected());
        spinnerMinecraftSeed.setEnabled((! defaultSettingsMode) && enabled && dim0);
        checkBoxPopulate.setEnabled(enabled && dim0);
        checkBoxCavernsRemoveWater.setEnabled(enabled && (checkBoxCavernsBreakSurface.isSelected() || checkBoxChasmsBreakSurface.isSelected()));
    }

    private void addListeners(final JSpinner minSpinner, final JSpinner maxSpinner) {
        minSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newMinValue = (Integer) minSpinner.getValue();
                int currentMaxValue = (Integer) maxSpinner.getValue();
                if (newMinValue > currentMaxValue) {
                    maxSpinner.setValue(newMinValue);
                }
            }
        });
        maxSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newMaxValue = (Integer) maxSpinner.getValue();
                int currentMinValue = (Integer) minSpinner.getValue();
                if (newMaxValue < currentMinValue) {
                    minSpinner.setValue(newMaxValue);
                }
            }
        });
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        radioButtonWaterBorder = new javax.swing.JRadioButton();
        radioButtonNoBorder = new javax.swing.JRadioButton();
        radioButtonVoidBorder = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        radioButtonLavaBorder = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        spinnerBorderLevel = new javax.swing.JSpinner();
        checkBoxBedrockWall = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        comboBoxSubsurfaceMaterial = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        spinnerMinecraftSeed = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        spinnerBorderSize = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        spinnerMinSurfaceDepth = new javax.swing.JSpinner();
        jLabel66 = new javax.swing.JLabel();
        spinnerMaxSurfaceDepth = new javax.swing.JSpinner();
        checkBoxBottomless = new javax.swing.JCheckBox();
        jLabel67 = new javax.swing.JLabel();
        checkBoxCoverSteepTerrain = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        themeEditor = new org.pepsoft.worldpainter.themes.SimpleThemeEditor();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        checkBoxCavernsEverywhere = new javax.swing.JCheckBox();
        sliderCavernsEverywhereLevel = new javax.swing.JSlider();
        sliderChasmsEverywhereLevel = new javax.swing.JSlider();
        checkBoxChasmsEverywhere = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        checkBoxFloodCaverns = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        spinnerCavernsFloodLevel = new javax.swing.JSpinner();
        checkBoxCavernsFloodWithLava = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        checkBoxCavernsBreakSurface = new javax.swing.JCheckBox();
        checkBoxCavernsRemoveWater = new javax.swing.JCheckBox();
        checkBoxChasmsBreakSurface = new javax.swing.JCheckBox();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        spinnerCavernsMinLevel = new javax.swing.JSpinner();
        spinnerCavernsMaxLevel = new javax.swing.JSpinner();
        jLabel73 = new javax.swing.JLabel();
        spinnerChasmsMinLevel = new javax.swing.JSpinner();
        jLabel72 = new javax.swing.JLabel();
        spinnerChasmsMaxLevel = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox8 = new javax.swing.JCheckBox();
        jSlider4 = new javax.swing.JSlider();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jSpinner3 = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        jSpinner4 = new javax.swing.JSpinner();
        jSpinner5 = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        jSpinner6 = new javax.swing.JSpinner();
        jSpinner7 = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        jSpinner8 = new javax.swing.JSpinner();
        jSpinner9 = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        jSpinner10 = new javax.swing.JSpinner();
        jSpinner11 = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jSpinner12 = new javax.swing.JSpinner();
        jSpinner13 = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        jSpinner14 = new javax.swing.JSpinner();
        jSpinner15 = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        jSpinner16 = new javax.swing.JSpinner();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jSpinner17 = new javax.swing.JSpinner();
        jLabel31 = new javax.swing.JLabel();
        jSpinner18 = new javax.swing.JSpinner();
        jSpinner19 = new javax.swing.JSpinner();
        jLabel32 = new javax.swing.JLabel();
        jSpinner20 = new javax.swing.JSpinner();
        jLabel33 = new javax.swing.JLabel();
        jSpinner21 = new javax.swing.JSpinner();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jSpinner22 = new javax.swing.JSpinner();
        jLabel52 = new javax.swing.JLabel();
        jSpinner23 = new javax.swing.JSpinner();
        jLabel53 = new javax.swing.JLabel();
        jSpinner24 = new javax.swing.JSpinner();
        jSpinner25 = new javax.swing.JSpinner();
        jSpinner26 = new javax.swing.JSpinner();
        jSpinner27 = new javax.swing.JSpinner();
        jSpinner28 = new javax.swing.JSpinner();
        jSpinner29 = new javax.swing.JSpinner();
        jSpinner30 = new javax.swing.JSpinner();
        jSpinner31 = new javax.swing.JSpinner();
        jSpinner32 = new javax.swing.JSpinner();
        jSpinner33 = new javax.swing.JSpinner();
        jSpinner34 = new javax.swing.JSpinner();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        checkBoxPopulate = new javax.swing.JCheckBox();
        checkBoxDeciduousEverywhere = new javax.swing.JCheckBox();
        sliderDeciduousLevel = new javax.swing.JSlider();
        jCheckBox3 = new javax.swing.JCheckBox();
        checkBoxPineEverywhere = new javax.swing.JCheckBox();
        sliderPineLevel = new javax.swing.JSlider();
        jCheckBox9 = new javax.swing.JCheckBox();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        checkBoxJungleEverywhere = new javax.swing.JCheckBox();
        jLabel68 = new javax.swing.JLabel();
        sliderJungleLevel = new javax.swing.JSlider();
        checkBoxSwamplandEverywhere = new javax.swing.JCheckBox();
        jLabel69 = new javax.swing.JLabel();
        jSlider6 = new javax.swing.JSlider();
        checkBoxSnowUnderTrees = new javax.swing.JCheckBox();
        checkBoxExportAnnotations = new javax.swing.JCheckBox();

        buttonGroup1.add(radioButtonWaterBorder);
        radioButtonWaterBorder.setText("Water");
        radioButtonWaterBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonWaterBorderActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonNoBorder);
        radioButtonNoBorder.setText("No border");
        radioButtonNoBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonNoBorderActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonVoidBorder);
        radioButtonVoidBorder.setText("Void");
        radioButtonVoidBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonVoidBorderActionPerformed(evt);
            }
        });

        jLabel4.setText("Border:");

        buttonGroup1.add(radioButtonLavaBorder);
        radioButtonLavaBorder.setText("Lava");
        radioButtonLavaBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonLavaBorderActionPerformed(evt);
            }
        });

        jLabel5.setText("Water or lava level:");

        spinnerBorderLevel.setModel(new javax.swing.SpinnerNumberModel(62, 0, 127, 1));
        spinnerBorderLevel.setEnabled(false);

        checkBoxBedrockWall.setText("Bedrock wall");

        jLabel6.setText("Underground material:");

        comboBoxSubsurfaceMaterial.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoxSubsurfaceMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxSubsurfaceMaterialActionPerformed(evt);
            }
        });

        jLabel7.setText("Minecraft seed:");

        spinnerMinecraftSeed.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(-9223372036854775808L), null, null, Long.valueOf(1L)));
        spinnerMinecraftSeed.setEditor(new javax.swing.JSpinner.NumberEditor(spinnerMinecraftSeed, "0"));

        jLabel8.setLabelFor(spinnerBorderSize);
        jLabel8.setText("Border size:");

        spinnerBorderSize.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(256), Integer.valueOf(128), null, Integer.valueOf(128)));
        spinnerBorderSize.setEnabled(false);
        spinnerBorderSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerBorderSizeStateChanged(evt);
            }
        });

        jLabel9.setText("blocks (in multiples of 128)");

        jLabel44.setText("(Minecraft default: 62)");

        jLabel65.setText("Top layer minimum depth:");

        spinnerMinSurfaceDepth.setModel(new javax.swing.SpinnerNumberModel(3, 1, 255, 1));
        spinnerMinSurfaceDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerMinSurfaceDepthStateChanged(evt);
            }
        });

        jLabel66.setText(", maximum depth:");

        spinnerMaxSurfaceDepth.setModel(new javax.swing.SpinnerNumberModel(7, 1, 255, 1));
        spinnerMaxSurfaceDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerMaxSurfaceDepthStateChanged(evt);
            }
        });

        checkBoxBottomless.setText("Bottomless world");
        checkBoxBottomless.setToolTipText("<html>Generate a bottomless map:\n<ul><li>No bedrock at the bottom of the map\n<li>Caverns and chasms are open to the void</html>");

        jLabel67.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/information.png"))); // NOI18N
        jLabel67.setToolTipText("<html>Generate a bottomless map:\n<ul><li>No bedrock at the bottom of the map\n<li>Caverns and chasms are open to the void</html>");

        checkBoxCoverSteepTerrain.setText("keep steep terrain covered");
        checkBoxCoverSteepTerrain.setToolTipText("<html>Enable this to extend the top layer<br>\ndownwards on steep terrain such as cliffs <br>\nso that the underground material is never exposed.</html>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxSubsurfaceMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4)
                    .addComponent(radioButtonNoBorder)
                    .addComponent(radioButtonVoidBorder)
                    .addComponent(radioButtonLavaBorder)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButtonWaterBorder)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerBorderLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(jLabel44))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(spinnerBorderSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel9))))
                    .addComponent(checkBoxBedrockWall)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerMinecraftSeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel65)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerMinSurfaceDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel66)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerMaxSurfaceDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxCoverSteepTerrain))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(checkBoxBottomless)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel67)))
                .addContainerGap(110, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
                    .addComponent(spinnerMinSurfaceDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel66)
                    .addComponent(spinnerMaxSurfaceDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxCoverSteepTerrain))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(comboBoxSubsurfaceMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel67, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxBottomless))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonNoBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonVoidBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonWaterBorder)
                    .addComponent(jLabel5)
                    .addComponent(spinnerBorderLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel44))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonLavaBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(spinnerBorderSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxBedrockWall)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinnerMinecraftSeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("General", jPanel1);

        jLabel45.setText("These are the default terrain types used by the Mountain tool, and when right-clicking a Terrain tool,");

        jLabel46.setText("with the minimum block level from which each terrain type is used:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(themeEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel46, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel45, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel45)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel46)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(themeEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Default Terrain", jPanel5);

        checkBoxCavernsEverywhere.setText("Caverns everywhere");
        checkBoxCavernsEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCavernsEverywhereActionPerformed(evt);
            }
        });

        sliderCavernsEverywhereLevel.setMajorTickSpacing(7);
        sliderCavernsEverywhereLevel.setMinimum(2);
        sliderCavernsEverywhereLevel.setPaintTicks(true);
        sliderCavernsEverywhereLevel.setSnapToTicks(true);

        sliderChasmsEverywhereLevel.setMajorTickSpacing(7);
        sliderChasmsEverywhereLevel.setMinimum(2);
        sliderChasmsEverywhereLevel.setPaintTicks(true);
        sliderChasmsEverywhereLevel.setSnapToTicks(true);

        checkBoxChasmsEverywhere.setText("Chasms everywhere");
        checkBoxChasmsEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxChasmsEverywhereActionPerformed(evt);
            }
        });

        checkBoxFloodCaverns.setText("Flood the caverns and chasms");
        checkBoxFloodCaverns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFloodCavernsActionPerformed(evt);
            }
        });

        jLabel1.setText("Level:");

        spinnerCavernsFloodLevel.setModel(new javax.swing.SpinnerNumberModel(16, 1, 127, 1));

        checkBoxCavernsFloodWithLava.setText("Lava instead of water:");
        checkBoxCavernsFloodWithLava.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jLabel2.setText("Settings for the Caverns and Chasms layers. These apply also to hand-painted Caverns and Chasms:");

        checkBoxCavernsBreakSurface.setText("Caverns break the surface");
        checkBoxCavernsBreakSurface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCavernsBreakSurfaceActionPerformed(evt);
            }
        });

        checkBoxCavernsRemoveWater.setSelected(true);
        checkBoxCavernsRemoveWater.setText("Remove water and lava above openings");
        checkBoxCavernsRemoveWater.setEnabled(false);

        checkBoxChasmsBreakSurface.setText("Chasms break the surface");
        checkBoxChasmsBreakSurface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxChasmsBreakSurfaceActionPerformed(evt);
            }
        });

        jLabel70.setText("Caverns min. level:");

        jLabel71.setText("Caverns max. level:");

        spinnerCavernsMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        spinnerCavernsMaxLevel.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));

        jLabel73.setText("Chasms min. level:");

        spinnerChasmsMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        jLabel72.setText("Chasms max. level:");

        spinnerChasmsMaxLevel.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxCavernsEverywhere)
                    .addComponent(jLabel2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxCavernsRemoveWater)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(sliderCavernsEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(sliderChasmsEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(checkBoxChasmsEverywhere)))
                            .addComponent(checkBoxCavernsFloodWithLava)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerCavernsFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxFloodCaverns)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel70)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerCavernsMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(checkBoxCavernsBreakSurface)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel71)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerCavernsMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel72)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerChasmsMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(checkBoxChasmsBreakSurface)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel73)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerChasmsMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(checkBoxCavernsEverywhere)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sliderCavernsEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(checkBoxChasmsEverywhere)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sliderChasmsEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel70)
                    .addComponent(spinnerCavernsMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel73)
                    .addComponent(spinnerChasmsMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel71)
                    .addComponent(spinnerCavernsMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel72)
                    .addComponent(spinnerChasmsMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(checkBoxFloodCaverns)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spinnerCavernsFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxCavernsFloodWithLava)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxCavernsBreakSurface)
                    .addComponent(checkBoxChasmsBreakSurface))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxCavernsRemoveWater)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Caverns and Chasms", new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/caverns.png")), jPanel3); // NOI18N

        jCheckBox8.setText("Resources everywhere");
        jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox8ActionPerformed(evt);
            }
        });

        jSlider4.setMajorTickSpacing(7);
        jSlider4.setMinimum(2);
        jSlider4.setPaintTicks(true);
        jSlider4.setSnapToTicks(true);

        jLabel10.setText("Settings for the Resources layer at 50% intensity. These also apply to hand-painted Resources:");

        jLabel12.setText("Gold:");

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jLabel13.setText("Occurrence:");

        jLabel14.setText("Levels:");

        jSpinner3.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel15.setText("Iron:");

        jSpinner4.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jSpinner5.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel16.setText("Coal:");

        jSpinner6.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jSpinner7.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel17.setText("Lapis Lazuli:");

        jSpinner8.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jSpinner9.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel18.setText("Diamond:");

        jSpinner10.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jSpinner11.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel19.setText("Redstone:");

        jSpinner12.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jSpinner13.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel20.setText("Water:");

        jSpinner14.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jSpinner15.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel21.setText("Lava:");

        jSpinner16.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jLabel22.setText("‰");

        jLabel23.setText("‰");

        jLabel24.setText("‰");

        jLabel25.setText("‰");

        jLabel26.setText("‰");

        jLabel27.setText("‰");

        jLabel28.setText("‰");

        jLabel29.setText("‰");

        jLabel30.setText("Dirt:");

        jSpinner17.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jLabel31.setText("‰");

        jSpinner18.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jSpinner19.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel32.setText("Gravel:");

        jSpinner20.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jLabel33.setText("‰");

        jSpinner21.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel34.setText("blocks");

        jLabel35.setText("blocks");

        jLabel36.setText("blocks");

        jLabel37.setText("blocks");

        jLabel38.setText("blocks");

        jLabel39.setText("blocks");

        jLabel40.setText("blocks");

        jLabel41.setText("blocks");

        jLabel42.setText("blocks");

        jLabel43.setText("blocks");

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel3.setText("Occurrence:");

        jLabel11.setText("Levels:");

        jLabel51.setText("Emerald:");

        jSpinner22.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));

        jLabel52.setText("‰");

        jSpinner23.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));

        jLabel53.setText("blocks");

        jSpinner24.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner25.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner26.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner27.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner28.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner29.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner30.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner31.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner32.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner33.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jSpinner34.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));

        jLabel54.setText("-");

        jLabel55.setText("-");

        jLabel56.setText("-");

        jLabel57.setText("-");

        jLabel58.setText("-");

        jLabel59.setText("-");

        jLabel60.setText("-");

        jLabel61.setText("-");

        jLabel62.setText("-");

        jLabel63.setText("-");

        jLabel64.setText("-");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jSlider4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBox8)
                    .addComponent(jLabel10)
                    .addComponent(jSeparator2)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel18))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel22))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel23))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel24))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel25))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel26))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel52))))
                            .addComponent(jLabel51))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel58)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel38))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel57)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel37))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel56)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel36))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel55)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel35))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel54)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel34))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel59)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel53)))
                                .addGap(18, 18, 18)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel20)
                                    .addComponent(jLabel21)
                                    .addComponent(jLabel30)
                                    .addComponent(jLabel32)
                                    .addComponent(jLabel19))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jSpinner12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinner34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel64)
                                .addGap(0, 0, 0)
                                .addComponent(jSpinner13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel39))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel28))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel29))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel31))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel33))
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel63)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel43))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel62)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel42))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel61)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel41))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jSpinner30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel60)
                                        .addGap(0, 0, 0)
                                        .addComponent(jSpinner15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel40)))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(jLabel3)
                            .addComponent(jLabel11))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel22)
                                    .addComponent(jLabel34)
                                    .addComponent(jSpinner24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel54))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel15)
                                    .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel35)
                                    .addComponent(jSpinner25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel55))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel16)
                                    .addComponent(jSpinner6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel24)
                                    .addComponent(jLabel36)
                                    .addComponent(jSpinner26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel56))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel17)
                                    .addComponent(jSpinner8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel25)
                                    .addComponent(jLabel37)
                                    .addComponent(jSpinner27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel57))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel18)
                                    .addComponent(jSpinner10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel26)
                                    .addComponent(jLabel38)
                                    .addComponent(jSpinner28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel58)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel20)
                                    .addComponent(jSpinner14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel28)
                                    .addComponent(jLabel40)
                                    .addComponent(jSpinner30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel60))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel21)
                                    .addComponent(jSpinner16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel29)
                                    .addComponent(jSpinner18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel41)
                                    .addComponent(jSpinner31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel61))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel30)
                                    .addComponent(jSpinner17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel31)
                                    .addComponent(jSpinner19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel42)
                                    .addComponent(jSpinner32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel62))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel32)
                                    .addComponent(jSpinner20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel33)
                                    .addComponent(jSpinner21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel43)
                                    .addComponent(jSpinner33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel63))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel19)
                                    .addComponent(jSpinner13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel27)
                                    .addComponent(jLabel39)
                                    .addComponent(jSpinner12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel64))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel51)
                            .addComponent(jSpinner22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel52)
                            .addComponent(jSpinner23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel53)
                            .addComponent(jSpinner29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel59)))
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Resources", new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/resources.png")), jPanel4); // NOI18N

        checkBoxPopulate.setText("Allow Minecraft to populate the entire terrain");
        checkBoxPopulate.setToolTipText("<html>This will mark the entire terrain as unpopulated, causing Minecraft to generate trees,<br/>\nwater and lava pools, pockets of dirt, gravel, coal, ore, etc. (but not caverns) as the<br/>\nchunks are loaded. This will slow down the initial loading of each chunk, and you have<br/>\nno control over where trees, snow, etc. appear.</html>");
        checkBoxPopulate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPopulateActionPerformed(evt);
            }
        });

        checkBoxDeciduousEverywhere.setText("Deciduous forest everywhere");
        checkBoxDeciduousEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDeciduousEverywhereActionPerformed(evt);
            }
        });

        sliderDeciduousLevel.setMajorTickSpacing(7);
        sliderDeciduousLevel.setMinimum(2);
        sliderDeciduousLevel.setPaintTicks(true);
        sliderDeciduousLevel.setSnapToTicks(true);

        jCheckBox3.setText("Frost everywhere");

        checkBoxPineEverywhere.setText("Pine forest everywhere");
        checkBoxPineEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPineEverywhereActionPerformed(evt);
            }
        });

        sliderPineLevel.setMajorTickSpacing(7);
        sliderPineLevel.setMinimum(2);
        sliderPineLevel.setPaintTicks(true);
        sliderPineLevel.setSnapToTicks(true);

        jCheckBox9.setText("Smooth snow (also applies to hand-painted Frost layer)");

        jLabel47.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/populate.png"))); // NOI18N

        jLabel48.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/deciduousforest.png"))); // NOI18N

        jLabel49.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/pineforest.png"))); // NOI18N

        jLabel50.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/frost.png"))); // NOI18N

        checkBoxJungleEverywhere.setText("Jungle everywhere");
        checkBoxJungleEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxJungleEverywhereActionPerformed(evt);
            }
        });

        jLabel68.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/jungle.png"))); // NOI18N

        sliderJungleLevel.setMajorTickSpacing(7);
        sliderJungleLevel.setMinimum(2);
        sliderJungleLevel.setPaintTicks(true);
        sliderJungleLevel.setSnapToTicks(true);

        checkBoxSwamplandEverywhere.setText("Swampland everywhere");
        checkBoxSwamplandEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxSwamplandEverywhereActionPerformed(evt);
            }
        });

        jLabel69.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/swampland.png"))); // NOI18N

        jSlider6.setMajorTickSpacing(7);
        jSlider6.setMinimum(2);
        jSlider6.setPaintTicks(true);
        jSlider6.setSnapToTicks(true);

        checkBoxSnowUnderTrees.setText("Frost under trees (also applies to hand-painted Frost layer)");

        checkBoxExportAnnotations.setText("Export the annotations (as coloured wool)");
        checkBoxExportAnnotations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxExportAnnotationsActionPerformed(evt);
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
                        .addComponent(checkBoxPopulate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel47))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel50))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox9)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sliderDeciduousLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sliderPineLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(67, 67, 67)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(checkBoxJungleEverywhere)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel68))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(checkBoxSwamplandEverywhere)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel69))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jSlider6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(sliderJungleLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addComponent(checkBoxSnowUnderTrees)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxDeciduousEverywhere)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(checkBoxPineEverywhere)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel49)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel48))
                    .addComponent(checkBoxExportAnnotations))
                .addContainerGap(112, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxPopulate)
                            .addComponent(jLabel47))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxDeciduousEverywhere)
                                    .addComponent(jLabel48))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sliderDeciduousLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxJungleEverywhere)
                                    .addComponent(jLabel68))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sliderJungleLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxPineEverywhere)
                                    .addComponent(jLabel49)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel69))))
                    .addComponent(checkBoxSwamplandEverywhere, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sliderPineLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSlider6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox3)
                    .addComponent(jLabel50))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxSnowUnderTrees)
                .addGap(18, 18, 18)
                .addComponent(checkBoxExportAnnotations)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Other Layers", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void radioButtonWaterBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonWaterBorderActionPerformed
        setControlStates();
        if ((Integer) spinnerBorderLevel.getValue() == (dimension.getMaxHeight() - 1)) {
            spinnerBorderLevel.setValue(dimension.getBorderLevel());
        }
}//GEN-LAST:event_radioButtonWaterBorderActionPerformed

    private void radioButtonNoBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonNoBorderActionPerformed
        setControlStates();
}//GEN-LAST:event_radioButtonNoBorderActionPerformed

    private void checkBoxPopulateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPopulateActionPerformed
        if (checkBoxPopulate.isSelected() && jCheckBox8.isSelected()) {
            jCheckBox8.setSelected(false);
            setControlStates();
            JOptionPane.showMessageDialog(this, "\"Resources everywhere\" disabled on the Resources tab,\nto avoid duplicate resources. You may enable it again manually.", "Resources Everywhere Disabled", JOptionPane.INFORMATION_MESSAGE);
        }
}//GEN-LAST:event_checkBoxPopulateActionPerformed

    private void radioButtonVoidBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonVoidBorderActionPerformed
        setControlStates();
}//GEN-LAST:event_radioButtonVoidBorderActionPerformed

    private void radioButtonLavaBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonLavaBorderActionPerformed
        setControlStates();
        if ((Integer) spinnerBorderLevel.getValue() == (dimension.getMaxHeight() - 1)) {
            spinnerBorderLevel.setValue(dimension.getBorderLevel());
        }
}//GEN-LAST:event_radioButtonLavaBorderActionPerformed

    private void comboBoxSubsurfaceMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxSubsurfaceMaterialActionPerformed
        //        if (comboBoxSubsurfaceMaterial.getSelectedItem() == Terrain.RESOURCES) {
        //            // TODO: do something?
        //        }
}//GEN-LAST:event_comboBoxSubsurfaceMaterialActionPerformed

    private void checkBoxDeciduousEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxDeciduousEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxDeciduousEverywhereActionPerformed

    private void checkBoxPineEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPineEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxPineEverywhereActionPerformed

    private void spinnerBorderSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerBorderSizeStateChanged
        int value = (Integer) spinnerBorderSize.getValue();
        value = Math.round(value / 128f) * 128;
        if (value < 128) {
            value = 128;
        }
        spinnerBorderSize.setValue(value);
    }//GEN-LAST:event_spinnerBorderSizeStateChanged

    private void jCheckBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox8ActionPerformed
        setControlStates();
    }//GEN-LAST:event_jCheckBox8ActionPerformed

    private void spinnerMinSurfaceDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerMinSurfaceDepthStateChanged
        int topLevelMinDepth = (Integer) spinnerMinSurfaceDepth.getValue();
        int topLevelMaxDepth = (Integer) spinnerMaxSurfaceDepth.getValue();
        if (topLevelMinDepth > topLevelMaxDepth) {
            spinnerMaxSurfaceDepth.setValue(topLevelMinDepth);
        }
    }//GEN-LAST:event_spinnerMinSurfaceDepthStateChanged

    private void spinnerMaxSurfaceDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerMaxSurfaceDepthStateChanged
        int topLevelMinDepth = (Integer) spinnerMinSurfaceDepth.getValue();
        int topLevelMaxDepth = (Integer) spinnerMaxSurfaceDepth.getValue();
        if (topLevelMaxDepth < topLevelMinDepth) {
            spinnerMinSurfaceDepth.setValue(topLevelMaxDepth);
        }
    }//GEN-LAST:event_spinnerMaxSurfaceDepthStateChanged

    private void checkBoxCavernsBreakSurfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCavernsBreakSurfaceActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxCavernsBreakSurfaceActionPerformed

    private void checkBoxFloodCavernsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFloodCavernsActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxFloodCavernsActionPerformed

    private void checkBoxCavernsEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCavernsEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxCavernsEverywhereActionPerformed

    private void checkBoxChasmsEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxChasmsEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxChasmsEverywhereActionPerformed

    private void checkBoxChasmsBreakSurfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxChasmsBreakSurfaceActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxChasmsBreakSurfaceActionPerformed

    private void checkBoxJungleEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxJungleEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxJungleEverywhereActionPerformed

    private void checkBoxSwamplandEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxSwamplandEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxSwamplandEverywhereActionPerformed

    private void checkBoxExportAnnotationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxExportAnnotationsActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxExportAnnotationsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox checkBoxBedrockWall;
    private javax.swing.JCheckBox checkBoxBottomless;
    private javax.swing.JCheckBox checkBoxCavernsBreakSurface;
    private javax.swing.JCheckBox checkBoxCavernsEverywhere;
    private javax.swing.JCheckBox checkBoxCavernsFloodWithLava;
    private javax.swing.JCheckBox checkBoxCavernsRemoveWater;
    private javax.swing.JCheckBox checkBoxChasmsBreakSurface;
    private javax.swing.JCheckBox checkBoxChasmsEverywhere;
    private javax.swing.JCheckBox checkBoxCoverSteepTerrain;
    private javax.swing.JCheckBox checkBoxDeciduousEverywhere;
    private javax.swing.JCheckBox checkBoxExportAnnotations;
    private javax.swing.JCheckBox checkBoxFloodCaverns;
    private javax.swing.JCheckBox checkBoxJungleEverywhere;
    private javax.swing.JCheckBox checkBoxPineEverywhere;
    private javax.swing.JCheckBox checkBoxPopulate;
    private javax.swing.JCheckBox checkBoxSnowUnderTrees;
    private javax.swing.JCheckBox checkBoxSwamplandEverywhere;
    private javax.swing.JComboBox comboBoxSubsurfaceMaterial;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSlider jSlider4;
    private javax.swing.JSlider jSlider6;
    private javax.swing.JSpinner jSpinner10;
    private javax.swing.JSpinner jSpinner11;
    private javax.swing.JSpinner jSpinner12;
    private javax.swing.JSpinner jSpinner13;
    private javax.swing.JSpinner jSpinner14;
    private javax.swing.JSpinner jSpinner15;
    private javax.swing.JSpinner jSpinner16;
    private javax.swing.JSpinner jSpinner17;
    private javax.swing.JSpinner jSpinner18;
    private javax.swing.JSpinner jSpinner19;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner20;
    private javax.swing.JSpinner jSpinner21;
    private javax.swing.JSpinner jSpinner22;
    private javax.swing.JSpinner jSpinner23;
    private javax.swing.JSpinner jSpinner24;
    private javax.swing.JSpinner jSpinner25;
    private javax.swing.JSpinner jSpinner26;
    private javax.swing.JSpinner jSpinner27;
    private javax.swing.JSpinner jSpinner28;
    private javax.swing.JSpinner jSpinner29;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner30;
    private javax.swing.JSpinner jSpinner31;
    private javax.swing.JSpinner jSpinner32;
    private javax.swing.JSpinner jSpinner33;
    private javax.swing.JSpinner jSpinner34;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JSpinner jSpinner5;
    private javax.swing.JSpinner jSpinner6;
    private javax.swing.JSpinner jSpinner7;
    private javax.swing.JSpinner jSpinner8;
    private javax.swing.JSpinner jSpinner9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton radioButtonLavaBorder;
    private javax.swing.JRadioButton radioButtonNoBorder;
    private javax.swing.JRadioButton radioButtonVoidBorder;
    private javax.swing.JRadioButton radioButtonWaterBorder;
    private javax.swing.JSlider sliderCavernsEverywhereLevel;
    private javax.swing.JSlider sliderChasmsEverywhereLevel;
    private javax.swing.JSlider sliderDeciduousLevel;
    private javax.swing.JSlider sliderJungleLevel;
    private javax.swing.JSlider sliderPineLevel;
    private javax.swing.JSpinner spinnerBorderLevel;
    private javax.swing.JSpinner spinnerBorderSize;
    private javax.swing.JSpinner spinnerCavernsFloodLevel;
    private javax.swing.JSpinner spinnerCavernsMaxLevel;
    private javax.swing.JSpinner spinnerCavernsMinLevel;
    private javax.swing.JSpinner spinnerChasmsMaxLevel;
    private javax.swing.JSpinner spinnerChasmsMinLevel;
    private javax.swing.JSpinner spinnerMaxSurfaceDepth;
    private javax.swing.JSpinner spinnerMinSurfaceDepth;
    private javax.swing.JSpinner spinnerMinecraftSeed;
    private org.pepsoft.worldpainter.themes.SimpleThemeEditor themeEditor;
    // End of variables declaration//GEN-END:variables

    private Dimension dimension;
    private boolean exportMode, defaultSettingsMode;
    
    private static final long serialVersionUID = 1L;
}