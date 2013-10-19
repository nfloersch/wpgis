/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.vecmath.Point3d;
import static org.pepsoft.minecraft.Constants.BLK_COAL;
import static org.pepsoft.minecraft.Constants.BLK_DIAMOND_ORE;
import static org.pepsoft.minecraft.Constants.BLK_EMERALD_ORE;
import static org.pepsoft.minecraft.Constants.BLK_GOLD_ORE;
import static org.pepsoft.minecraft.Constants.BLK_IRON_ORE;
import static org.pepsoft.minecraft.Constants.BLK_LAPIS_LAZULI_ORE;
import static org.pepsoft.minecraft.Constants.BLK_REDSTONE_ORE;
import org.pepsoft.util.MemoryUtils;
import org.pepsoft.util.ProgressReceiver;
import static org.pepsoft.worldpainter.Constants.EVENT_KEY_ACTION_MERGE_WORLD;
import org.pepsoft.worldpainter.merging.WorldMerger;
import org.pepsoft.worldpainter.vo.EventVO;
import org.pepsoft.worldpainter.layers.DeciduousForest;
import org.pepsoft.worldpainter.layers.TreeLayer;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.PineForest;
import org.pepsoft.worldpainter.layers.SwampLand;

/**
 *
 * @author Nick
 */
class OverlayProcessor {
    private WorldPainter view;
    public OverlayProcessor(WorldPainter world) {
        view = world;
    }
    
    public void Roadwork(
            File imageFile, 
            int RaiseLowerAmt,
            int TerrainThickness, 
            ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        logger.info("Roadwork() Starting...");
        // Record start of roadwork
        long start = System.currentTimeMillis();
        
        overlayMask = LoadImage(imageFile);
        EtchRoads(overlayMask,RaiseLowerAmt,TerrainThickness);
        
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
    
    public void SetLanduse(
            File imageFile, 
            String luName,
            ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        logger.info("SetLanduse() Starting...");
        // Record start of roadwork
        long start = System.currentTimeMillis();
        
        org.pepsoft.worldpainter.layers.Layer layerType = null;
        switch(luName) {
            case "Deciduous":
                layerType = DeciduousForest.INSTANCE;
                break;
            case "Pine":
                layerType = PineForest.INSTANCE;
                break;
            case "Swamp":
                layerType = SwampLand.INSTANCE;
                break;
            case "Frozen Deciduous":
                break;
            case "Frozen Pine":
                break;
            case "Frozen Swamp":
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
                    view.getDimension().setLayerValueAt(layerType, x, y, clr);
                    
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
    
    private java.awt.Dimension getImageSize(File image) throws IOException {
        String filename = image.getName();
        int p = filename.lastIndexOf('.');
        if (p == -1) {
            return null;
        }
        String suffix = filename.substring(p + 1).toLowerCase();
        for (Iterator<ImageReader> i = ImageIO.getImageReadersBySuffix(suffix); i.hasNext();) {
            ImageReader reader = i.next();
            try {
                ImageInputStream in = new FileImageInputStream(image);
                try {
                    reader.setInput(in);
                    int width = reader.getWidth(reader.getMinIndex());
                    int height = reader.getHeight(reader.getMinIndex());
                    return new java.awt.Dimension(width, height);
                } finally {
                    in.close();
                }
            } finally {
                reader.dispose();
            }
        }
        return null;
    }
    
    int getImageSize() {
        //return (worldImage != null) ? MemoryUtils.getSize(worldImage, Collections.<Class<?>>emptySet()) : 0;
        return 0;
    }

    int getOverlayImageSize() {
        return (overlay != null) ? MemoryUtils.getSize(overlay, Collections.<Class<?>>emptySet()) : 0;
    }  
    
    private void resizeImageIfNecessary() {
//        int newLowestX = dimension.getLowestX();
//        int newHighestX = dimension.getHighestX();
//        int newLowestY = dimension.getLowestY();
//        int newHighestY = dimension.getHighestY();
//        int newWidth = newHighestX - newLowestX + 1;
//        int newHeight = newHighestY - newLowestY + 1;
//        if ((newWidth != imageWidth) || (newHeight != imageHeight)) {
//            if (logger.isLoggable(java.util.logging.Level.FINE)) {
//                logger.fine("Resizing world image from " + imageX + ", " + imageY + ", " + imageWidth + "x" + imageHeight + " to " + newLowestX + ", " + newLowestY + ", " + newWidth + "x" + newHeight);
//                logger.fine("Creating image of size " + (newWidth * TILE_SIZE) + "x" + (newHeight * TILE_SIZE));
//            }
//            BufferedImage newImage = createOptimalBufferedImage(newWidth * TILE_SIZE, newHeight * TILE_SIZE);
//            int dx = imageX - newLowestX;
//            int dy = imageY - newLowestY;
//            Graphics2D g2 = newImage.createGraphics();
//            try {
//                g2.setColor(new Color(Constants.VOID_COLOUR));
//                g2.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
//                if (logger.isLoggable(java.util.logging.Level.FINE)) {
//                    logger.fine("Painting old image at " + (dx * TILE_SIZE) + ", " + (dy * TILE_SIZE));
//                }
//                g2.drawImage(worldImage, dx * TILE_SIZE, dy * TILE_SIZE, null);
//            } finally {
//                g2.dispose();
//            }
//            worldImage = newImage;
//            imageX = newLowestX;
//            imageY = newLowestY;
//            imageWidth = newWidth;
//            imageHeight = newHeight;
//            resize();
//        }
    }

    private void resize() {
//        setPreferredSize(getPreferredScrollableViewportSize());
//        revalidate();
    }
    
    private BufferedImage createOptimalBufferedImage(int width, int height) {
//        if (createOptimalImage) {
//            logger.info("Creating optimal image of size " + width + "x" + height);
//            return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
//        } else {
//            logger.info("Creating non-optimal image of size " + width + "x" + height);
//            return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        }
        return null;
    }
    
    private void drawOverlay(Graphics2D g2) {
//        if (overlayTransparency == 1.0f) {
//            // Fully transparent
//            return;
//        } else  if (overlayTransparency > 0.0f) {
//            // Transparent
//            Composite savedComposite = g2.getComposite();
//            try {
//                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - overlayTransparency));
////                g2.drawImage(scaledOverlay, overlayOffsetX, overlayOffsetY, null);
//                if (overlayScale == 1.0f) {
//                    // 1:1 scale
//                    g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, null);
//                } else {
//                    int width = Math.round(overlay.getWidth() * overlayScale);
//                    int height = Math.round(overlay.getHeight() * overlayScale);
//                    Object savedInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
//                    try {
//                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//                        g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, width, height, null);
//                    } finally {
//                        if (savedInterpolation != null) {
//                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolation);
//                        }
//                    }
//                }
//            } finally {
//                g2.setComposite(savedComposite);
//            }
//        } else {
//            // Fully opaque
////            g2.drawImage(scaledOverlay, overlayOffsetX, overlayOffsetY, null);
//            if (overlayScale == 1.0f) {
//                // 1:1 scale
//                g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, null);
//            } else {
//                int width = Math.round(overlay.getWidth() * overlayScale);
//                int height = Math.round(overlay.getHeight() * overlayScale);
//                Object savedInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
//                try {
//                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//                    g2.drawImage(overlay, overlayOffsetX, overlayOffsetY, width, height, null);
//                } finally {
//                    if (savedInterpolation != null) {
//                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolation);
//                    }
//                }
//            }
//        }
    }
    
    protected void paintComponent(Graphics g) {
//        if (worldImage != null) {
////            long start = System.nanoTime();
//            Graphics2D g2 = (Graphics2D) g;
//            Color savedColour = g2.getColor();
//            Object savedAAValue = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
//            Object savedInterpolationValue = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
//            Stroke savedStroke = g2.getStroke();
//            AffineTransform savedTransform = g2.getTransform();
////            long start2 = 0, end2 = 0, start3 = 0, end3 = 0, start4 = 0, end4 = 0, start5 = 0, end5 = 0;
//            try {
//                if (zoom != 0) {
//                    g2.scale(scale, scale);
//                }
//                Rectangle clipBounds = g.getClipBounds();
//                if (logger.isLoggable(java.util.logging.Level.FINER)) {
//                    logger.finer("Clip: " + clipBounds.width + "x" + clipBounds.height);
//                }
////                start2 = System.nanoTime();
//                updateImage(clipBounds);
////                end2 = System.nanoTime();
//                if (zoom < 0) {
//                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//                }
////                start3 = System.nanoTime();
//                g2.drawImage(worldImage, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height, null);
////                end3 = System.nanoTime();
//                if (drawGrid) {
////                    start4 = System.nanoTime();
//                    drawGrid(g2, clipBounds);
////                    end4 = System.nanoTime();
//                }
//                if (dimension.getDim() == 0) {
//                    g2.setColor(Color.RED);
//                    g2.setStroke(new BasicStroke());
//                    Point spawnPoint = dimension.getWorld().getSpawnPoint();
//                    g2.fillRect(-imageX * TILE_SIZE + spawnPoint.x - 1, -imageY * TILE_SIZE + spawnPoint.y    , 3, 1);
//                    g2.fillRect(-imageX * TILE_SIZE + spawnPoint.x    , -imageY * TILE_SIZE + spawnPoint.y - 1, 1, 3);
//                }
//                if (drawOverlay && (overlay != null)) {
////                    start5 = System.nanoTime();
//                    drawOverlay(g2);
////                    end5 = System.nanoTime();
//                }
//                if (drawRadius || drawViewDistance || drawWalkingDistance || drawLabels) {
//                    g2.setColor(Color.BLACK);
//                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                }
//                if (drawRadius) {
//                    float strokeWidth = 1 / scale;
//                    g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {3 * strokeWidth, 3 * strokeWidth}, 0));
//                    int diameter = radius * 2 + 1;
//                    switch (brushShape) {
//                        case CIRCLE:
//                            g2.drawOval(mouseX - radius, mouseY - radius, diameter, diameter);
//                            break;
//                        case SQUARE:
//                            if (brushRotation % 90 == 0) {
//                                g2.drawRect(mouseX - radius, mouseY - radius, diameter, diameter);
//                            } else {
//                                AffineTransform existingTransform = g2.getTransform();
//                                try {
//                                    g2.rotate(brushRotation / 180.0 * Math.PI, mouseX, mouseY);
//                                    g2.drawRect(mouseX - radius, mouseY - radius, diameter, diameter);
//                                } finally {
//                                    g2.setTransform(existingTransform);
//                                }
//                            }
//                            break;
//                    }
//                }
//                if (drawViewDistance) {
//                    float strokeWidth = 1 / scale;
//                    g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {10 * strokeWidth, 10 * strokeWidth}, 0));
//                    g2.drawOval(mouseX - VIEW_DISTANCE_RADIUS, mouseY - VIEW_DISTANCE_RADIUS, VIEW_DISTANCE_DIAMETER, VIEW_DISTANCE_DIAMETER);
//                }
//                if (drawWalkingDistance) {
//                    float strokeWidth = 1 / scale;
//                    g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {20 * strokeWidth, 20 * strokeWidth}, 0));
//                    g2.drawOval(mouseX - DAY_NIGHT_WALK_DISTANCE_RADIUS, mouseY - DAY_NIGHT_WALK_DISTANCE_RADIUS, DAY_NIGHT_WALK_DISTANCE_DIAMETER, DAY_NIGHT_WALK_DISTANCE_DIAMETER);
//                    g2.drawOval(mouseX - DAY_WALK_DISTANCE_RADIUS, mouseY - DAY_WALK_DISTANCE_RADIUS, DAY_WALK_DISTANCE_DIAMETER, DAY_WALK_DISTANCE_DIAMETER);
//                    g2.drawOval(mouseX - FIVE_MINUTE_WALK_DISTANCE_RADIUS, mouseY - FIVE_MINUTE_WALK_DISTANCE_RADIUS, FIVE_MINUTE_WALK_DISTANCE_DIAMETER, FIVE_MINUTE_WALK_DISTANCE_DIAMETER);
//                }
//                if (drawLabels && ((mouseX > 0) ||(mouseY > 0))) {
//                    Point worldCoords = imageToWorldCoordinates(mouseX, mouseY);
//                    int height = dimension.getIntHeightAt(worldCoords);
//                    int labelY = mouseY + labelAscent + 2;
//                    g2.setFont(labelFont);
//                    g2.setColor(Color.WHITE);
//                    g2.drawString(Integer.toString(height), mouseX + 2, labelY);
//                    labelY += labelLineHeight;
//                    Terrain terrain = dimension.getTerrainAt(worldCoords.x, worldCoords.y);
//                    if (terrain != null) {
//                        g2.drawString(terrain.getName(), mouseX + 2, labelY);
//                        labelY += labelLineHeight;
//                    }
//                }
//            } finally {
//                g2.setColor(savedColour);
//                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedAAValue);
//                if (savedInterpolationValue != null) {
//                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolationValue);
//                }
//                g2.setStroke(savedStroke);
//                g2.setTransform(savedTransform);
//            }
////            long end = System.nanoTime();
////            if (logger.isLoggable(Level.FINER)) {
////                logger.finer("Total drawing time: " + ((end - start) / 1000000f) + "ms");
////                if (start2 != 0) {
////                    logger.fine("Updating image took " + ((end2 - start2) / 1000000f) + "ms");
////                    logger.fine("Drawing image took " + ((end3 - start3) / 1000000f) + "ms");
////                    if (start4 != 0) {
////                        logger.fine("Drawing grid took " + ((end4 - start4) / 1000000f) + "ms");
////                    }
////                }
////                if (start5 != 0) {
////                    logger.fine("Drawing overlay took " + ((end5 - start5) / 1000000f) + "ms");
////                }
////            }
//        }
    }
    
    private BufferedImage overlayMask;
    private File overlay;
    private float overlayScale = 1.0f, overlayTransparency = 0.5f;
    private int overlayOffsetX, overlayOffsetY, gridSize = 128;
    private String warnings;
    private static final Logger logger = Logger.getLogger(WorldMerger.class.getName());
    private static final String EOL = System.getProperty("line.separator");
    private static final Set<Integer> RESOURCES = new HashSet<Integer>(Arrays.asList(BLK_COAL, BLK_IRON_ORE, BLK_GOLD_ORE, BLK_REDSTONE_ORE, BLK_LAPIS_LAZULI_ORE, BLK_DIAMOND_ORE, BLK_EMERALD_ORE));

    public String getWarnings() {
        return warnings;
    }
    
    private BufferedImage LoadImage(File imageFile) {
        // Ideally we would get our own, but for now steal whatever is in the overlay.
        return view.getOverlay();
    }

    private void EtchRoads(BufferedImage overlayMask,int RaiseLowerAmt, int TerrainThickness) {
        for (int y = 0; y < overlayMask.getHeight(); y++) {
            for (int x = 0; x < overlayMask.getWidth(); x++) {
                int  clr   = overlayMask.getRGB(x, y);
                if (isNotWhite(clr,overlayMask.getColorModel())) {
                    // Get Location in Map for X/Y
                    Point mapLoc2d = view.imageToWorldCoordinates(x, y);
                    // Get Height At That Location
                    Point3d mapLoc = new Point3d(mapLoc2d.x,mapLoc2d.y,view.getDimension().getHeightAt(mapLoc2d));
                    
                    view.getDimension().setHeightAt(mapLoc2d, ((int)mapLoc.z) + RaiseLowerAmt);
                    // In theory fills up from ground level to TerrainThickness but
                    // in practice it is not filling up evenly ... may depend on
                    // where the topsoil is vs where the underlying stone is.
                    if (TerrainThickness < 0) {
                        for (int i = 0;i > TerrainThickness;i--) {
                            view.getDimension().setHeightAt(mapLoc2d, ((int)mapLoc.z) + 1);
                            view.getDimension().setTerrainAt(mapLoc2d, Terrain.OBSIDIAN);
                        }
                    } 
                    // In theory fills _down_ from ground level to TerrainThickness
                    // In practice it is filling a lot farther down...
                    if (TerrainThickness > 0) {
                        for (int i = 0;i < TerrainThickness;i++) {
                            view.getDimension().setHeightAt(mapLoc2d, ((int)mapLoc.z) - 1);
                            view.getDimension().setTerrainAt(mapLoc2d, Terrain.OBSIDIAN);
                        }
                    }
                    if (TerrainThickness == 0) view.getDimension().setTerrainAt(mapLoc2d, Terrain.OBSIDIAN);
                }
            }
        }
    }

    private boolean isNotWhite(int clr,ColorModel cm) {
        int  red   = (clr & 0x00ff0000) >> 16;
        int  green = (clr & 0x0000ff00) >> 8;
        int  blue  =  clr & 0x000000ff;
        if (((red + green + blue)/3) != 255) return true;
        return false;
    }
}



class ORGNode {
    public int x;
    public int y;
    public int z;
    private ORGNode upStream;
    private ORGNode downStream;
    private Boolean isIntersection;
    private ORGNode Intersector;
    private WorldPainter view;
    
    public ORGNode() {
        x = 0;
        y = 0;
        z = 0;
    }
    
    public ORGNode(int inX, int inY, WorldPainter inView) {
        x = inX;
        y = inY;
        view = inView;
        z = view.getDimension().getIntHeightAt(x, y);
    }
    
    public ORGNode getUpStream() {
        return upStream;
    }
    public void setUpStream(ORGNode node) {
        upStream = node;
    }
    public ORGNode getDownStream() {
        return downStream;
    }
    public void setDownStream(ORGNode node) {
        downStream = node;
    }
    public int getX() {
        return x;
    }
    public void setX(int newX) {
        x = newX;
    }
    public int getY() {
        return x;
    }
    public void setY(int newY) {
        x = newY;
    }
    public int getZ() {
        return x;
    }
    public void setZ(int newZ) {
        x = newZ;
    }
    public Boolean isIsIntersection() {
        return isIntersection;
    }
    public void setIsIntersection(Boolean isIntersection) {
        this.isIntersection = isIntersection;
    }
    public ORGNode getIntersector() {
        return Intersector;
    }
    public void setIntersector(ORGNode Intersector) {
        this.Intersector = Intersector;
    }
    public WorldPainter getView() {
        return view;
    }
    public void setView(WorldPainter view) {
        this.view = view;
    }
}

class ORGNodeGraph {
    private ORGNode startNode;
    private ORGNode topNodes;
    private ORGNode bottomNodes;
    private int nodeCount;
    private LinkedHashMap<String, LinkedHashMap<String, ORGNode>> nodeMap;
    
    public ORGNodeGraph() {
        
    }
    public ORGNodeGraph(ORGNode node) {
        startNode = node;
    }
    
    public ORGNode getStartNode() {
        return startNode;
    }
    public ORGNode getTopNodes() {
        return topNodes;
    }
    public ORGNode getBottomNodes() {
        return bottomNodes;
    }
    public void setStartNode(ORGNode startNode) {
        this.startNode = startNode;
    }    
    public void setTopNodes(ORGNode topNodes) {
        this.topNodes = topNodes;
    }
    public void setBottomNodes(ORGNode bottomNodes) {
        this.bottomNodes = bottomNodes;
    }
    
    
}