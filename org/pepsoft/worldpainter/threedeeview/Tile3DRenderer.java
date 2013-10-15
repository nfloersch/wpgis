/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.threedeeview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.worldpainter.BiomeScheme;
import org.pepsoft.worldpainter.ColourScheme;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.TileRenderer;
import org.pepsoft.worldpainter.layers.*;

/**
 *
 * @author pepijn
 */
// TODO: adapt for new dynamic maximum level height
public class Tile3DRenderer {
    public Tile3DRenderer(Dimension dimension, ColourScheme colourScheme, BiomeScheme biomeScheme, int rotation) {
        this.dimension = dimension;
        maxHeight = dimension.getMaxHeight();
        this.colourScheme = colourScheme;
        this.rotation = rotation;
        tileRenderer = new TileRenderer(dimension, colourScheme, biomeScheme, true);
        tileRenderer.addHiddenLayers(DEFAULT_HIDDEN_LAYERS);
        tileRenderer.setContourLines(false);
    }
    
    public BufferedImage render(Tile tile) {
//        System.out.println("Rendering tile " + tile);
        tileRenderer.setTile(tile);
        tileRenderer.renderTile(tileImgBuffer, 0, 0);
//        Terrain subSurfaceMaterial = dimension.getSubsurfaceMaterial();
        long seed = dimension.getSeed();
        int tileOffsetX = tile.getX() * TILE_SIZE, tileOffsetY = tile.getY() * TILE_SIZE;
        int currentColour = -1;
        int imgWidth = TILE_SIZE * 2;
        int imgHeight = TILE_SIZE + maxHeight - 1;
        BufferedImage img = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int y = 0; y < TILE_SIZE; y++) {
                    // Coordinates of the block in the world
                    int xInTile, yInTile;
                    switch (rotation) {
                        case 0:
                            xInTile = x;
                            yInTile = y;
                            break;
                        case 1:
                            xInTile = y;
                            yInTile = TILE_SIZE - 1 - x;
                            break;
                        case 2:
                            xInTile = TILE_SIZE - 1 - x;
                            yInTile = TILE_SIZE - 1 - y;
                            break;
                        case 3:
                            xInTile = TILE_SIZE - 1 - y;
                            yInTile = x;
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                    if (tile.getBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, xInTile, yInTile)) {
                        continue;
                    }
                    int blockX = tileOffsetX + xInTile, blockY = tileOffsetY + yInTile;
                    int terrainHeight = tile.getIntHeight(xInTile, yInTile);
                    int fluidLevel = tile.getWaterLevel(xInTile, yInTile);
                    boolean floodWithLava;
                    if (fluidLevel > terrainHeight) {
                        floodWithLava = tile.getBitLayerValue(FloodWithLava.INSTANCE, xInTile, yInTile);
                    } else {
                        floodWithLava = false;
                    }
                    // Image coordinates
                    float imgX = TILE_SIZE + x - y - 0.5f, imgY = (x + y) / 2f + maxHeight - 0.5f;
    //                                System.out.println(blockX + ", " + blockY + " -> " + blockXTranslated + ", " + blockYTranslated + " -> " + imgX + ", " + imgY);
                    int subsurfaceHeight = Math.max(terrainHeight - dimension.getTopLayerDepth(blockX, blockY, terrainHeight), 0);
                    int colour = colourScheme.getColour(BLK_STONE);
                    if (colour != currentColour) {
                        g2.setColor(new Color(colour));
                        currentColour = colour;
                    }
                    if (subsurfaceHeight > 0) {
                        g2.fill(new Rectangle2D.Float(imgX, imgY - subsurfaceHeight, 2, subsurfaceHeight));
                    }
//                    for (int z = 0; z <= subsurfaceHeight; z++) {
//                        colour = colourScheme.getColour(subSurfaceMaterial.getMaterial(seed, blockX, blockY, z, terrainHeight));
//                        if (colour != currentColour) {
//    //                                        g2.setColor(new Color(ColourUtils.multiply(colour, brightenAmount)));
//                            g2.setColor(new Color(colour));
//                            currentColour = colour;
//                        }
//                        g2.draw(new Line2D.Float(imgX, imgY - z, imgX + 1, imgY - z));
//                    }
                    // Do this per block because they might have different
                    // colours
                    for (int z = subsurfaceHeight + 1; z <= terrainHeight - 1; z++) {
                        colour = colourScheme.getColour(tile.getTerrain(xInTile, yInTile).getMaterial(seed, blockX, blockY, z, terrainHeight));
                        if (colour != currentColour) {
    //                                        g2.setColor(new Color(ColourUtils.multiply(colour, brightenAmount)));
                            g2.setColor(new Color(colour));
                            currentColour = colour;
                        }
                        g2.draw(new Line2D.Float(imgX, imgY - z, imgX + 1, imgY - z));
                    }
                    colour = tileImgBuffer.getRGB(xInTile, yInTile);
                    g2.setColor(new Color(colour));
                    currentColour = colour;
                    g2.draw(new Line2D.Float(imgX, imgY - terrainHeight, imgX + 1, imgY - terrainHeight));
                    if (fluidLevel > terrainHeight) {
                        colour = colourScheme.getColour(floodWithLava ? BLK_LAVA : BLK_WATER);
    //                                    currentColour = 0x80000000 | ColourUtils.multiply(colour, brightenAmount);
                        currentColour = 0x80000000 | colour;
                        g2.setColor(new Color(currentColour, true));
                        boolean ice = (! floodWithLava) && tile.getBitLayerValue(Frost.INSTANCE, xInTile, yInTile);
                        for (int z = terrainHeight + 1; z <= fluidLevel; z++) {
                            if ((z == fluidLevel) && ice) {
                                colour = colourScheme.getColour(BLK_ICE);
                                g2.setColor(new Color(colour));
                                currentColour = colour;
                            }
                            g2.draw(new Line2D.Float(imgX, imgY - z, imgX + 1, imgY - z));
                        }
                    }
                }
            }
        } finally {
            g2.dispose();
        }
        return img;
    }
 
    private final Dimension dimension;
    private final ColourScheme colourScheme;
    private final TileRenderer tileRenderer;
    private final int maxHeight, rotation;
    
    private final BufferedImage tileImgBuffer = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);

    static final Set<Layer> DEFAULT_HIDDEN_LAYERS = new HashSet<Layer>(Arrays.asList(Biome.INSTANCE, Caverns.INSTANCE, ReadOnly.INSTANCE, Resources.INSTANCE));
}