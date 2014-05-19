/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import java.util.HashSet;
import java.util.Set;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.pepsoft.util.ColourUtils;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.renderers.BitLayerRenderer;
import org.pepsoft.worldpainter.layers.renderers.ByteLayerRenderer;
import org.pepsoft.worldpainter.layers.renderers.ColourSchemeRenderer;
import org.pepsoft.worldpainter.layers.renderers.BiomeRenderer;
import org.pepsoft.worldpainter.layers.renderers.LayerRenderer;
import org.pepsoft.worldpainter.layers.renderers.NibbleLayerRenderer;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.layers.renderers.DimensionAwareRenderer;

/**
 * This class is <strong>not</strong> thread-safe!
 * 
 * @author pepijn
 */
public final class TileRenderer {
    public TileRenderer(TileProvider tileProvider, ColourScheme colourScheme, BiomeScheme biomeScheme, CustomBiomeManager customBiomeManager) {
        this(tileProvider, colourScheme, biomeScheme, customBiomeManager, false);
    }

    public TileRenderer(TileProvider tileProvider, ColourScheme colourScheme, BiomeScheme biomeScheme, CustomBiomeManager customBiomeManager, boolean dry) {
        biomeRenderer = new BiomeRenderer(biomeScheme, customBiomeManager);
        setTileProvider(tileProvider);
        setColourScheme(colourScheme);
        this.dry = dry;
    }

    public final TileProvider getTileProvider() {
        return tileProvider;
    }

    public final void setTileProvider(TileProvider tileProvider) {
        this.tileProvider = tileProvider;
        if (tileProvider instanceof Dimension) {
            seed = ((Dimension) tileProvider).getSeed();
            bottomless = ((Dimension) tileProvider).isBottomless();
        }
    }

    public final ColourScheme getColourScheme() {
        return colourScheme;
    }

    public final void setColourScheme(ColourScheme colourScheme) {
        this.colourScheme = colourScheme;
        waterColour = colourScheme.getColour(BLK_WATER);
        lavaColour = colourScheme.getColour(BLK_LAVA);
        bedrockColour = colourScheme.getColour(BLK_BEDROCK);
    }

    public void addHiddenLayers(Collection<Layer> hiddenLayers) {
        this.hiddenLayers.addAll(hiddenLayers);
    }
    
    public void addHiddenLayer(Layer hiddenLayer) {
        hiddenLayers.add(hiddenLayer);
    }

    public void removeHiddenLayer(Layer hiddenLayer) {
        // The FloodWithLava layer should *always* remain hidden
        if (! hiddenLayer.equals(FloodWithLava.INSTANCE)) {
            hiddenLayers.remove(hiddenLayer);
        }
    }

    public Set<Layer> getHiddenLayers() {
        return Collections.unmodifiableSet(hiddenLayers);
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
        for (int x = 0; x < TILE_SIZE; x++) {
            for (int y = 0; y < TILE_SIZE; y++) {
                final float height = tile.getHeight(x, y);
                floatHeightCache[x + y * TILE_SIZE] = height;
                intHeightCache[x + y * TILE_SIZE] = (int) (height + 0.5f);
            }
        }
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public boolean isContourLines() {
        return contourLines;
    }

    public void setContourLines(boolean contourLines) {
        this.contourLines = contourLines;
    }

    public int getContourSeparation() {
        return contourSeparation;
    }

    public void setContourSeparation(int contourSeparation) {
        this.contourSeparation = contourSeparation;
    }

    public void renderTile(BufferedImage image, int dx, int dy) {
        // TODO this deadlocks background painting. Find out why:
//        synchronized (tile) {
        final List<Layer> layerList = new ArrayList<Layer>(tile.getLayers());
        if (! layerList.contains(Biome.INSTANCE)) {
            layerList.add(Biome.INSTANCE);
        }
        layerList.removeAll(hiddenLayers);
        final boolean _void = layerList.contains(org.pepsoft.worldpainter.layers.Void.INSTANCE);
        final Layer[] layers = layerList.toArray(new Layer[layerList.size()]);
        final LayerRenderer[] renderers = new LayerRenderer[layers.length];
        boolean renderBiomes = false;
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] instanceof Biome) {
                renderers[i] = biomeRenderer;
                renderBiomes = true;
            } else {
                renderers[i] = layers[i].getRenderer();
            }
            if (renderers[i] instanceof ColourSchemeRenderer) {
                ((ColourSchemeRenderer) renderers[i]).setColourScheme(colourScheme);
            }
            if ((renderers[i] instanceof DimensionAwareRenderer) && (tileProvider instanceof Dimension)) {
                ((DimensionAwareRenderer) renderers[i]).setDimension((Dimension) tileProvider);
            }
        }
        LayerRenderer[] voidRenderers = null;
        Layer[] voidLayers = null;
        if (_void) {
            if (renderBiomes) {
                voidLayers = new Layer[] {org.pepsoft.worldpainter.layers.Void.INSTANCE, Biome.INSTANCE};
                voidRenderers = new LayerRenderer[] {org.pepsoft.worldpainter.layers.Void.INSTANCE.getRenderer(), biomeRenderer};
            } else {
                voidLayers = new Layer[] {org.pepsoft.worldpainter.layers.Void.INSTANCE};
                voidRenderers = new LayerRenderer[] {org.pepsoft.worldpainter.layers.Void.INSTANCE.getRenderer()};
            }
        }

        final int tileX = tile.getX() * TILE_SIZE, tileY = tile.getY() * TILE_SIZE;
        if (zoom == 1) {
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int y = 0; y < TILE_SIZE; y++) {
                    if (_void && tile.getBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, x, y)) {
                        renderBuffer[x + y * TILE_SIZE] = getPixelColour(tileX, tileY, x, y, voidLayers, voidRenderers, false);
                    } else {
                        int colour = getPixelColour(tileX, tileY, x, y, layers, renderers, contourLines);
                        colour = ColourUtils.multiply(colour, getBrightenAmount());
                        renderBuffer[x + y * TILE_SIZE] = colour;
                    }
                }
            }
        } else {
            final int zoomSquared = zoom * zoom;
            for (int x = 0; x < TILE_SIZE; x += zoom) {
                for (int y = 0; y < TILE_SIZE; y += zoom) {
                    if (_void && tile.getBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, x, y)) {
                        renderBuffer[x / zoom + y * TILE_SIZE / zoomSquared] = getPixelColour(tileX, tileY, x, y, voidLayers, voidRenderers, false);
                    } else {
                        int colour = getPixelColour(tileX, tileY, x, y, layers, renderers, contourLines);
                        colour = ColourUtils.multiply(colour, getBrightenAmount());
                        renderBuffer[x / zoom + y * TILE_SIZE / zoomSquared] = colour;
                    }
                }
            }
        }
//        }
        
        final WritableRaster raster = image.getRaster();
        int transferType = raster.getTransferType();
        if (transferType == DataBuffer.TYPE_INT) {
            // TODO: this is a bit dodgy. The pixel format might be different,
            // etc. In practice it seems to work so far
            raster.setDataElements(dx, dy, TILE_SIZE / zoom, TILE_SIZE / zoom, renderBuffer);
        } else {
            image.setRGB(dx, dy, TILE_SIZE / zoom, TILE_SIZE / zoom, renderBuffer, 0, TILE_SIZE);
        }
    }

    /**
     * Determine the brighten amount. This method assumes that the
     * {@link #deltas} array has been filled by a previous call to
     * {@link #getPixelColour(int, int, int, int, Layer[], LayerRenderer[], boolean)}.
     * 
     * @return The amount by which to brighten the pixel for the specified block. May be negative.
     */
    private int getBrightenAmount() {
        switch (lightOrigin) {
            case NORTHWEST:
                return Math.max(0, ((deltas[2][1] - deltas[0][1] + deltas[1][2] - deltas[1][0]) << 5) + 256);
            case NORTHEAST:
                return Math.max(0, ((deltas[0][1] - deltas[2][1] + deltas[1][2] - deltas[1][0]) << 5) + 256);
            case SOUTHEAST:
                return Math.max(0, ((deltas[0][1] - deltas[2][1] + deltas[1][0] - deltas[1][2]) << 5) + 256);
            case SOUTHWEST:
                return Math.max(0, ((deltas[2][1] - deltas[0][1] + deltas[1][0] - deltas[1][2]) << 5) + 256);
            default:
                throw new InternalError();
        }
    }
    
    public LightOrigin getLightOrigin() {
        return lightOrigin;
    }
    
    public void setLightOrigin(LightOrigin lightOrigin) {
        if (lightOrigin == null) {
            throw new NullPointerException();
        }
        this.lightOrigin = lightOrigin;
    }

    private int getPixelColour(int tileX, int tileY, int x, int y, Layer[] layers, LayerRenderer[] renderers, boolean contourLines) {
        final int offset = x + y * TILE_SIZE;
        final int intHeight = intHeightCache[offset];
//        heights[0][0] = getNeighbourHeight(x, y, -1, -1);
//        deltas [0][0] = heights[0][0] - height;
        heights[1][0] = getNeighbourHeight(x, y,  0, -1);
        deltas [1][0] = heights[1][0] - intHeight;
//        heights[2][0] = getNeighbourHeight(x, y,  1, -1);
//        deltas [2][0] = heights[2][0] - height;
        heights[0][1] = getNeighbourHeight(x, y, -1,  0);
        deltas [0][1] = heights[0][1] - intHeight;
        heights[2][1] = getNeighbourHeight(x, y,  1,  0);
        deltas [2][1] = heights[2][1] - intHeight;
//        heights[0][2] = getNeighbourHeight(x, y, -1,  1);
//        deltas [0][2] = heights[0][2] - height;
        heights[1][2] = getNeighbourHeight(x, y,  0,  1);
        deltas [1][2] = heights[1][2] - intHeight;
//        heights[2][2] = getNeighbourHeight(x, y,  1,  1);
//        deltas [2][2] = heights[2][2] - height;
//        if (       (deltas[0][0] >= 0) || (deltas[0][0] < -1)
//                && (deltas[1][0] >= 0) || (deltas[1][0] < -1)
//                && (deltas[2][0] >= 0) || (deltas[2][0] < -1)
//                && (deltas[0][1] >= 0) || (deltas[0][1] < -1)
//                && (deltas[2][1] >= 0) || (deltas[2][1] < -1)
//                && (deltas[0][2] >= 0) || (deltas[0][2] < -1)
//                && (deltas[1][2] >= 0) || (deltas[1][2] < -1)
//                && (deltas[2][2] >= 0) || (deltas[2][2] < -1)) {
//            if (Math.random() < 0.01) {
//                System.out.println("[" + deltas[0][0] + "][" + deltas[1][0] + "][" + deltas[2][0] + "]");
//                System.out.println("[" + deltas[0][1] + "][" + deltas[1][1] + "][" + deltas[2][1] + "]");
//                System.out.println("[" + deltas[0][2] + "][" + deltas[1][2] + "][" + deltas[2][2] + "]");
//                System.out.println();
//            }
//            return RED;
//        }
        if (contourLines && ((intHeight % contourSeparation) == 0)
                && ((deltas[0][1] < 0)
                    || (deltas[2][1] < 0)
                    || (deltas[1][0] < 0)
                    || (deltas[1][2] < 0))) {
            return BLACK;
        }
        final int waterLevel = tile.getWaterLevel(x, y);
        int colour;
        final int worldX = tileX | x, worldY = tileY | y;
        if ((! dry) && (waterLevel > intHeight)) {
            if (tile.getBitLayerValue(FloodWithLava.INSTANCE, x, y)) {
                colour = lavaColour;
            } else {
                colour = waterColour;
            }
        } else {
            final float height = floatHeightCache[offset];
            colour = ((! bottomless) && (intHeight == 0)) ? bedrockColour : tile.getTerrain(x, y).getColour(seed, worldX, worldY, height, intHeight, colourScheme);
        }
        for (int i = 0; i < layers.length; i++) {
            final Layer layer = layers[i];
            switch (layer.getDataSize()) {
                case BIT:
                case BIT_PER_CHUNK:
                    if (dry && (layer instanceof Frost) && (waterLevel > intHeightCache[offset])) {
                        continue;
                    }
                    boolean bitLayerValue = tile.getBitLayerValue(layer, x, y);
                    if (bitLayerValue) {
                        final BitLayerRenderer renderer = (BitLayerRenderer) renderers[i];
                        if (renderer == null) {
                            logger.severe("Missing renderer for layer " + layer + " (type: " + layer.getClass().getSimpleName() + ")");
                            if (! missingRendererReportedFor.contains(layer)) {
                                missingRendererReportedFor.add(layer);
                                throw new IllegalStateException("Missing renderer for layer " + layer + " (type: " + layer.getClass().getSimpleName() + ")");
                            } else {
                                continue;
                            }
                        }
                        colour = renderer.getPixelColour(worldX, worldY, colour, bitLayerValue);
                    }
                    break;
                case NIBBLE:
                    int layerValue = tile.getLayerValue(layer, x, y);
                    if (layerValue > 0) {
                        final NibbleLayerRenderer renderer = (NibbleLayerRenderer) renderers[i];
                        if (renderer == null) {
                            logger.severe("Missing renderer for layer " + layer + " (type: " + layer.getClass().getSimpleName() + ")");
                            if (! missingRendererReportedFor.contains(layer)) {
                                missingRendererReportedFor.add(layer);
                                throw new IllegalStateException("Missing renderer for layer " + layer + " (type: " + layer.getClass().getSimpleName() + ")");
                            } else {
                                continue;
                            }
                        }
                        colour = renderer.getPixelColour(worldX, worldY, colour, layerValue);
                    }
                    break;
                case BYTE:
                    final ByteLayerRenderer renderer = (ByteLayerRenderer) renderers[i];
                    if (renderer == null) {
                        logger.severe("Missing renderer for layer " + layer + " (type: " + layer.getClass().getSimpleName() + ")");
                        if (! missingRendererReportedFor.contains(layer)) {
                            missingRendererReportedFor.add(layer);
                            throw new IllegalStateException("Missing renderer for layer " + layer + " (type: " + layer.getClass().getSimpleName() + ")");
                        } else {
                            continue;
                        }
                    }
                    colour = renderer.getPixelColour(worldX, worldY, colour, tile.getLayerValue(layer, x, y));
                    break;
                default:
                    throw new UnsupportedOperationException("Don't know how to render " + layer.getClass().getSimpleName());
            }
        }
        return colour;
    }

    private int getNeighbourHeight(int x, int y, int dx, int dy) {
//        System.out.println(x + ", " + y + ", " + dx + ", " + dy);
        x = x + dx;
        y = y + dy;
        if ((x >= 0) && (x < TILE_SIZE) && (y >= 0) && (y < TILE_SIZE)) {
            return intHeightCache[x + y * TILE_SIZE];
        } else {
            int tileDX = 0, tileDY = 0;
            if (x < 0) {
                tileDX = -1;
                x += TILE_SIZE;
            } else if (x >= TILE_SIZE) {
                tileDX = 1;
                x -= TILE_SIZE;
            }
            if (y < 0) {
                tileDY = -1;
                y += TILE_SIZE;
            } else if (y >= TILE_SIZE) {
                tileDY = 1;
                y -= TILE_SIZE;
            }
            Tile neighborTile = tileProvider.getTile(tile.getX() + tileDX, tile.getY() + tileDY);
            return (neighborTile != null) ? neighborTile.getIntHeight(x, y) : 62;
        }
    }

    private final BiomeRenderer biomeRenderer;
    private final Set<Layer> hiddenLayers = new HashSet<Layer>(Arrays.asList(FloodWithLava.INSTANCE));
    private final int[] intHeightCache = new int[TILE_SIZE * TILE_SIZE];
    private final float[] floatHeightCache = new float[TILE_SIZE * TILE_SIZE];
    private final int[] renderBuffer = new int[TILE_SIZE * TILE_SIZE];
    private final boolean dry;
    private final int[][] heights = new int[3][3], deltas = new int[3][3];
    private final Set<Layer> missingRendererReportedFor = new HashSet<Layer>(); // TODO remove when no longer necessary!
    private TileProvider tileProvider;
    private long seed;
    private Tile tile;
    private ColourScheme colourScheme;
    private int zoom = 1;
    private boolean contourLines = true, bottomless;
    private int contourSeparation = 10, waterColour, lavaColour, bedrockColour;
    private LightOrigin lightOrigin = LightOrigin.NORTHWEST;
    
    private static final int BLACK = 0x000000, RED = 0xFF0000;
    private static final Logger logger = Logger.getLogger(TileRenderer.class.getName());

    public enum LightOrigin {
        NORTHWEST {
            @Override
            public LightOrigin left() {
                return SOUTHWEST;
            }

            @Override
            public LightOrigin right() {
                return NORTHEAST;
            }
        },
        NORTHEAST {
            @Override
            public LightOrigin left() {
                return NORTHWEST;
            }

            @Override
            public LightOrigin right() {
                return SOUTHEAST;
            }
        },
        SOUTHEAST{
            @Override
            public LightOrigin left() {
                return NORTHEAST;
            }

            @Override
            public LightOrigin right() {
                return SOUTHWEST;
            }
        },
        SOUTHWEST{
            @Override
            public LightOrigin left() {
                return SOUTHEAST;
            }

            @Override
            public LightOrigin right() {
                return NORTHWEST;
            }
        };
        
        public abstract LightOrigin left();
        
        public abstract LightOrigin right();
    }
}