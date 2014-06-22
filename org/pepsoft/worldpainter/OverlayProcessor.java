/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import static org.pepsoft.minecraft.Constants.BLK_COAL;
import static org.pepsoft.minecraft.Constants.BLK_DIAMOND_ORE;
import static org.pepsoft.minecraft.Constants.BLK_EMERALD_ORE;
import static org.pepsoft.minecraft.Constants.BLK_GOLD_ORE;
import static org.pepsoft.minecraft.Constants.BLK_IRON_ORE;
import static org.pepsoft.minecraft.Constants.BLK_LAPIS_LAZULI_ORE;
import static org.pepsoft.minecraft.Constants.BLK_REDSTONE_ORE;
import org.pepsoft.minecraft.Material;
import org.pepsoft.util.ProgressReceiver;
import static org.pepsoft.worldpainter.Constants.EVENT_KEY_ACTION_MERGE_WORLD;
import static org.pepsoft.worldpainter.biomeschemes.AbstractMinecraft1_7BiomeScheme.*;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.DeciduousForest;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.PineForest;
import org.pepsoft.worldpainter.layers.SwampLand;
import org.pepsoft.worldpainter.merging.WorldMerger;
import org.pepsoft.worldpainter.vo.EventVO;



/**
 *
 * @author Nick
 */
class OverlayProcessor {
    private WorldPainter view;
    private Set<Layer> tLayers;
    private Layer tLyrPine;
    private Layer tLyrDeciduous;
    private Layer tLyrJungle;
    private Layer tLyrSwamp;
    private Layer tLyrBiome;
    public OverlayProcessor(WorldPainter world) {
        view = world;
    }
    
    public void Roadwork(
        File imageFile, 
        int RaiseLowerAmt,
        int TerrainThickness,
        String landuse,
        ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        logger.info("Roadwork() Starting...");
        // Record start of roadwork
        long start = System.currentTimeMillis();
        
        overlayMask = LoadImage(imageFile);
        tLayers = view.getDimension().getAllLayers(true);
        for (Layer l : tLayers) {
            String tLName = l.getName();
            if (tLName.equals("Pine")) tLyrPine = l;
            if (tLName.equals("Jungle")) tLyrJungle = l;
            if (tLName.equals("Swamp")) tLyrSwamp = l;
            if (tLName.equals("Deciduous")) tLyrDeciduous = l;
            if (tLName.equals("Biome")) tLyrBiome = l;
            }
        EtchRoads(overlayMask,RaiseLowerAmt,TerrainThickness,landuse);
        
        view.getDimension().setOverlayEnabled(false);
        view.repaint();
        view.getDimension().setOverlay(null);
        // Log an event
        Configuration config = Configuration.getInstance();
        if (config != null) {
            EventVO event = new EventVO(EVENT_KEY_ACTION_MERGE_WORLD).duration(System.currentTimeMillis() - start);
            event.setAttribute(EventVO.ATTRIBUTE_TIMESTAMP, new Date(start));
            config.logEvent(event);
        }
    }
    
    public void WaterWorks(
        File imageFile, 
        int RaiseLowerAmt,
        int TerrainThickness,
        ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        logger.info("WaterWorks() Starting...");
        // Record start of roadwork
        long start = System.currentTimeMillis();
        
        overlayMask = LoadImage(imageFile);
        tLayers = view.getDimension().getAllLayers(true);
        for (Layer l : tLayers) {
            String tLName = l.getName();
            if (tLName.equals("Pine")) tLyrPine = l;
            if (tLName.equals("Jungle")) tLyrJungle = l;
            if (tLName.equals("Swamp")) tLyrSwamp = l;
            if (tLName.equals("Deciduous")) tLyrDeciduous = l;
            if (tLName.equals("Biome")) tLyrBiome = l;
            }
        PourRivers(overlayMask,RaiseLowerAmt,TerrainThickness);
        
        view.getDimension().setOverlayEnabled(false);
        view.repaint();
        view.getDimension().setOverlay(null);
        // Log an event
        Configuration config = Configuration.getInstance();
        if (config != null) {
            EventVO event = new EventVO(EVENT_KEY_ACTION_MERGE_WORLD).duration(System.currentTimeMillis() - start);
            event.setAttribute(EventVO.ATTRIBUTE_TIMESTAMP, new Date(start));
            config.logEvent(event);
        }
    }
    
    public void SetLanduse(File imageFile, String luName,ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        logger.info("SetLanduse() Starting...");
        // Record start of roadwork
        long start = System.currentTimeMillis();

        tLayers = view.getDimension().getAllLayers(true);
        for (Layer l : tLayers) {
            String tLName = l.getName();
            if (tLName.equals("Pine")) tLyrPine = l;
            if (tLName.equals("Jungle")) tLyrJungle = l;
            if (tLName.equals("Swamp")) tLyrSwamp = l;
            if (tLName.equals("Deciduous")) tLyrDeciduous = l;
            if (tLName.equals("Biome")) tLyrBiome = l;
            }

        org.pepsoft.worldpainter.layers.Layer layerType = null;
        org.pepsoft.worldpainter.layers.Layer frostLayer = Frost.INSTANCE;
        boolean doFrost = false;
        doFrost = getLuseHasFrost(luName);
        layerType = getLuseLayer(luName);
        boolean isBiome = false;
        int theBiomeType = 255;
        switch(luName) {
            case "Grassland":
                isBiome = true;
                theBiomeType = BIOME_SUNFLOWER_PLAINS;
                break;
            case "Ice Plains":
                isBiome = true;
                theBiomeType = BIOME_ICE_PLAINS;
                break;
            default:
                break;
        }

        overlayMask = LoadImage(imageFile);
        for (int y = 0; y < overlayMask.getHeight(); y++) {
            for (int x = 0; x < overlayMask.getWidth(); x++) {
                int  clr   = overlayMask.getRGB(x, y);
                if (isNotWhite(clr,overlayMask.getColorModel())) {
                    // Get Location in Map for X/Y
                    Point mapLoc2d = view.imageToWorldCoordinates(x, y);
                    // Get Height At That Location
                    Point3d mapLoc = new Point3d(mapLoc2d.x,mapLoc2d.y,view.getDimension().getHeightAt(mapLoc2d));
                    int tClr = scaleColorToNibble(clr,overlayMask.getColorModel());
                    if (isBiome) {
                        tClr = theBiomeType;
                        view.getDimension().setLayerValueAt(layerType, mapLoc2d.x, mapLoc2d.y, 255); //Reset Biome First?
                    }
                    view.getDimension().setLayerValueAt(layerType, mapLoc2d.x, mapLoc2d.y, tClr);
                    //if (doFrost) view.getDimension().setLayerValueAt(frostLayer, mapLoc2d.x, mapLoc2d.y, 15 );
                    if (doFrost) view.getDimension().setBitLayerValueAt(frostLayer, mapLoc2d.x, mapLoc2d.y, true);
                }
            }
        }

        view.getDimension().setOverlayEnabled(false);
        view.repaint();
        view.getDimension().setOverlay(null);
        // Log an event
        Configuration config = Configuration.getInstance();
        if (config != null) {
            EventVO event = new EventVO(EVENT_KEY_ACTION_MERGE_WORLD).duration(System.currentTimeMillis() - start);
            event.setAttribute(EventVO.ATTRIBUTE_TIMESTAMP, new Date(start));
            config.logEvent(event);
        }
    }
    
    public void RaiseLowerTerrain(File imageFile, int RaiseLowerAmt, ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        logger.info("RaiseLowerTerrain() Starting...");
        long start = System.currentTimeMillis();
        
        overlayMask = LoadImage(imageFile);
        for (int y = 0; y < overlayMask.getHeight(); y++) {
            for (int x = 0; x < overlayMask.getWidth(); x++) {
                int  clr   = overlayMask.getRGB(x, y);
                if (isNotWhite(clr,overlayMask.getColorModel())) {
                    // Get Location in Map for X/Y
                    Point mapLoc2d = view.imageToWorldCoordinates(x, y);
                    // Get Height At That Location
                    Point3d mapLoc = new Point3d(mapLoc2d.x,mapLoc2d.y,view.getDimension().getHeightAt(mapLoc2d));
                    
                    view.getDimension().setHeightAt(mapLoc2d, ((int)mapLoc.z) + RaiseLowerAmt);
                }
            }
        }
        view.getDimension().setOverlayEnabled(false);
        view.repaint();
        view.getDimension().setOverlay(null);
        // Log an event
        Configuration config = Configuration.getInstance();
        if (config != null) {
            EventVO event = new EventVO(EVENT_KEY_ACTION_MERGE_WORLD).duration(System.currentTimeMillis() - start);
            event.setAttribute(EventVO.ATTRIBUTE_TIMESTAMP, new Date(start));
            config.logEvent(event);
        }
    }
 
    private BufferedImage overlayMask;
    private File overlay;
    private float overlayScale = 1.0f, overlayTransparency = 0.5f;
    private int overlayOffsetX, overlayOffsetY, gridSize = 128;
    private String warnings;
    private static final Logger logger = Logger.getLogger(WorldMerger.class.getName());
    private static final String EOL = System.getProperty("line.separator");
    private static final Set<Integer> RESOURCES = new HashSet<Integer>(Arrays.asList(BLK_COAL, BLK_IRON_ORE, BLK_GOLD_ORE, BLK_REDSTONE_ORE, BLK_LAPIS_LAZULI_ORE, BLK_DIAMOND_ORE, BLK_EMERALD_ORE));

    Material matStoneBricks = Material.STONE_BRICKS;
    
    public String getWarnings() {
        return warnings;
    }
    
    private BufferedImage LoadImage(File imageFile) {
        // Ideally we would get our own, but for now steal whatever is in the overlay.
        return view.getOverlay();
    }

    private void EtchRoads(BufferedImage overlayMask,int RaiseLowerAmt, int TerrainThickness,String landuse) {
        for (int y = 0; y < overlayMask.getHeight(); y++) {
            for (int x = 0; x < overlayMask.getWidth(); x++) {
                int  clr   = overlayMask.getRGB(x, y);
                if (isNotWhite(clr,overlayMask.getColorModel())) {
                    // Get Location in Map for X/Y
                    Point mapLoc2d = view.imageToWorldCoordinates(x, y);
                    // Get Height At That Location
                    Point3d mapLoc = new Point3d(mapLoc2d.x,mapLoc2d.y,view.getDimension().getHeightAt(mapLoc2d));
                    
                    Terrain luse = getLuseTerrain(landuse);
                    
                    view.getDimension().setHeightAt(mapLoc2d, ((int)mapLoc.z) + RaiseLowerAmt);
                    // In theory fills up from ground level to TerrainThickness but
                    // in practice it is not filling up evenly ... may depend on
                    // where the topsoil is vs where the underlying stone is.
                    if (TerrainThickness < 0) {
                        for (int i = 0;i > TerrainThickness;i--) {
                            view.getDimension().setHeightAt(mapLoc2d, ((int)mapLoc.z) + 1);
                            view.getDimension().setTerrainAt(mapLoc2d, luse);
                        }
                    } 
                    // In theory fills _down_ from ground level to TerrainThickness
                    // In practice it is filling a lot farther down...
                    if (TerrainThickness > 0) {
                        for (int i = 0;i < TerrainThickness;i++) {
                            view.getDimension().setHeightAt(mapLoc2d, ((int)mapLoc.z) - 1);
                            view.getDimension().setTerrainAt(mapLoc2d, luse);
                        }
                    }

                    if (TerrainThickness == 0) view.getDimension().setTerrainAt(mapLoc2d, luse);

                    if (tLyrSwamp != null) view.getDimension().setLayerValueAt(tLyrSwamp, mapLoc2d.x, mapLoc2d.y, 0);
                    if (tLyrJungle != null) view.getDimension().setLayerValueAt(tLyrJungle, mapLoc2d.x, mapLoc2d.y, 0);
                    if (tLyrPine != null) view.getDimension().setLayerValueAt(tLyrPine, mapLoc2d.x, mapLoc2d.y, 0);
                    if (tLyrDeciduous != null) view.getDimension().setLayerValueAt(tLyrDeciduous, mapLoc2d.x, mapLoc2d.y, 0);
                    if (tLyrBiome != null) view.getDimension().setLayerValueAt(tLyrBiome, mapLoc2d.x, mapLoc2d.y, 0);
                }
            }
        }
    }

    private Terrain getLuseTerrain(String landuse) {
        Terrain luse = Terrain.DIRT;
        Boolean doFrost = false;
        switch(landuse) {
                        case "Paved":
                            luse = Terrain.COAL_BLOCK ;
                            break;
                        case "Gravel":
                            luse = Terrain.BROWN_WOOL;
                            break;
                        case "Primary":
                            luse = Terrain.CYAN_STAINED_CLAY;
                            break;
                        case "Secondary":
                            luse = Terrain.GRAY_WOOL;
                            break;
                        case "Cobble":
                            luse = Terrain.MOSSY_COBBLESTONE;
                            break;
                        case "White":
                            luse = Terrain.WHITE_WOOL;
                            break;
                        case "Orange":
                            luse = Terrain.ORANGE_WOOL;
                            break;
                        case "Magenta":
                            luse = Terrain.MAGENTA_WOOL;
                            break;
                        case "Light Blue":
                            luse = Terrain.LIGHT_BLUE_WOOL;
                            break;
                        case "Yellow":
                            luse = Terrain.YELLOW_WOOL;
                            break;
                        case "Lime":
                            luse = Terrain.LIME_WOOL;
                            break;
                        case "Pink":
                            luse = Terrain.PINK_WOOL;
                            break;
                        case "Gray":
                            luse = Terrain.GRAY_WOOL;
                            break;
                        case "Light Gray":
                            luse = Terrain.LIGHT_GRAY_WOOL;
                            break;
                        case "Cyan":
                            luse = Terrain.CYAN_WOOL;
                            break;
                        case "Purple":
                            luse = Terrain.PURPLE_WOOL;
                            break;
                        case "Blue":
                            luse = Terrain.BLUE_WOOL;
                            break;
                        case "Brown":
                            luse = Terrain.BROWN_WOOL;
                            break;
                        case "Green":
                            luse = Terrain.GREEN_WOOL;
                            break;
                        case "Red":
                            luse = Terrain.RED_WOOL;
                            break;
                        case "Black":
                            luse = Terrain.BLACK_WOOL;
                            break;
                        default:
                            luse = Terrain.COBBLESTONE;
                            break;
                    }
        return luse;
    }
    
    private org.pepsoft.worldpainter.layers.Layer getLuseLayer(String landuse) {
        org.pepsoft.worldpainter.layers.Layer luse;
        switch(landuse) {
                        case "Deciduous":
                            luse = DeciduousForest.INSTANCE;
                            break;
                        case "Pine":
                            luse = PineForest.INSTANCE;
                            break;
                        case "Swamp":
                            luse = SwampLand.INSTANCE;
                            break;
                        case "Frozen Deciduous":
                            luse = DeciduousForest.INSTANCE;
                            break;
                        case "Frozen Pine":
                            luse = PineForest.INSTANCE;
                            break;
                        case "Frozen Swamp":
                            luse = SwampLand.INSTANCE;
                            break;
                        case "Grassland":
                            luse = Biome.INSTANCE;
                            break;
                        case "Ice Plains":
                            luse = Biome.INSTANCE;
                            break;
                         default:
                            luse = DeciduousForest.INSTANCE;
                            break;
                    }
        return luse;
    }
    
    private boolean getLuseHasFrost(String landuse) {
        boolean hasFrost = false;
        switch(landuse) {
                        case "Deciduous":
                            break;
                        case "Pine":
                            break;
                        case "Swamp":
                            break;
                        case "Frozen Deciduous":
                            hasFrost = true;
                            break;
                        case "Frozen Pine":
                            hasFrost = true;
                            break;
                        case "Frozen Swamp":
                            hasFrost = true;
                            break;
                        case "Grassland":
                            break;
                        case "Ice Plains":
                            hasFrost = true;
                            break;
                         default:
                            break;
                    }
        return hasFrost;
    }
    
    private boolean isNotWhite(int clr,ColorModel cm) {
        int  red   = (clr & 0x00ff0000) >> 16;
        int  green = (clr & 0x0000ff00) >> 8;
        int  blue  =  clr & 0x000000ff;
        if (((red + green + blue)/3) != 255) return true;
        return false;
    }

    private int scaleColorToNibble(int clr,ColorModel cm) {
        int  red   = (clr & 0x00ff0000) >> 16;
        int  green = (clr & 0x0000ff00) >> 8;
        int  blue  =  clr & 0x000000ff;
        int intensity = ((red + green + blue)/3);
        intensity = abs(intensity - 256); // mask uses black as highest intensity value, so this inverts the clr value's intensity
        int nibble = intensity / 16;
        // If tClr < 0 it is 0 and if it is more than 15 it is 15.
        nibble = nibble > 0 ? (nibble < 16 ? nibble : 15) : 0;
        return nibble;
    }
    
    void PourRivers(BufferedImage overlayMask,int RaiseLowerAmt, int TerrainThickness) {
        logger.info("PourRivers() Starting...");
        for (int y = 0; y < overlayMask.getHeight(); y++) {
            for (int x = 0; x < overlayMask.getWidth(); x++) {
                int  clr   = overlayMask.getRGB(x, y);
                if (isNotWhite(clr,overlayMask.getColorModel())) {
                    // Get Location in Map for X/Y
                    Point mapLoc2d = view.imageToWorldCoordinates(x, y);
                    
                    view.getDimension().setHeightAt(mapLoc2d, (view.getDimension().getHeightAt(mapLoc2d)) + RaiseLowerAmt);
                    
                    // Get Height At That Location
                    Point3d mapLoc = new Point3d(mapLoc2d.x,mapLoc2d.y,view.getDimension().getHeightAt(mapLoc2d));
                    
                    view.getDimension().setWaterLevelAt(mapLoc2d.x, mapLoc2d.y, ((int)mapLoc.z) + TerrainThickness);

                    if (tLyrSwamp != null) view.getDimension().setLayerValueAt(tLyrSwamp, mapLoc2d.x, mapLoc2d.y, 0);
                    if (tLyrJungle != null) view.getDimension().setLayerValueAt(tLyrJungle, mapLoc2d.x, mapLoc2d.y, 0);
                    if (tLyrPine != null) view.getDimension().setLayerValueAt(tLyrPine, mapLoc2d.x, mapLoc2d.y, 0);
                    if (tLyrDeciduous != null) view.getDimension().setLayerValueAt(tLyrDeciduous, mapLoc2d.x, mapLoc2d.y, 0);
                    if (tLyrBiome != null) view.getDimension().setLayerValueAt(tLyrBiome, mapLoc2d.x, mapLoc2d.y, 0);
                }
            }
        }
    }
}
