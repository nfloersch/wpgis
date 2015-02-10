/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.importing;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.util.MathUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.Configuration;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Generator;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.TileFactory;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.layers.exporters.FrostExporter;

/**
 *
 * @author SchmitzP
 */
public class HeightMapImporter {
    /**
     * Create a new WorldPainter world from the configured image and import
     * settings. Note that the image property is set to <code>null</code> so
     * that it can be garbage collected and free up more room for the import.
     * 
     * @param progressReceiver The progress receiver to report progress to and
     *     check for cancellation with.
     * @return A new WorldPainter world based on the specified height map.
     * @throws org.pepsoft.util.ProgressReceiver.OperationCancelled If and when
     *     the specified progress received throws it (when the user cancels the
     *     operation).
     */
    public World2 doImport(ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled {
        logger.log(Level.INFO, "Importing world from height map {0} (size: {1}x{2})", new Object[]{name, image.getWidth(), image.getHeight()});

        final int widthInBlocks = image.getWidth() * scale / 100;
        final int heightInBlocks = image.getHeight() * scale / 100;
        final boolean sixteenBit = bitDepth == 16;
        final BufferedImage scaledImage;
        if ((scale == 100) && (image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY)) {
            // No scaling necessary
            scaledImage = image;
        } else {
            scaledImage = new BufferedImage(widthInBlocks, heightInBlocks, sixteenBit ? BufferedImage.TYPE_USHORT_GRAY : BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2 = scaledImage.createGraphics();
            try {
                if (scale != 100) {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                }
                g2.drawImage(image, 0, 0, widthInBlocks, heightInBlocks, null);
            } finally {
                g2.dispose();
            }
        }
        image = null; // The original image is no longer necessary, so allow it to be garbage collected to make more space available for the import
        final boolean oneOnOne = (worldLowLevel == imageLowLevel) && (worldHighLevel == imageHighLevel);
        final boolean highRes = (bitDepth == 16) && (! oneOnOne) && (worldHighLevel < maxHeight);
        final World2 world = new World2(World2.DEFAULT_OCEAN_SEED, tileFactory, maxHeight);
        int p = name.lastIndexOf('.');
        if (p != -1) {
            name = name.substring(0, p);
        }
        world.setName(name);
        final Dimension dimension = world.getDimension(0);

        // Export settings
        final Configuration config = Configuration.getInstance();
        final boolean minecraft11Only = dimension.getMaxHeight() != DEFAULT_MAX_HEIGHT_2;
        world.setCreateGoodiesChest(config.isDefaultCreateGoodiesChest());
        Generator generator = config.getDefaultGenerator();
        if (minecraft11Only && (generator == Generator.LARGE_BIOMES)) {
            generator = Generator.DEFAULT;
        } else if ((! minecraft11Only) && (generator == Generator.DEFAULT)) {
            generator = Generator.LARGE_BIOMES;
        }
        world.setGenerator(generator);
        if (generator == Generator.FLAT) {
            world.setGeneratorOptions(config.getDefaultGeneratorOptions());
        }
        world.setMapFeatures(config.isDefaultMapFeatures());
        world.setGameType(config.getDefaultGameType());
        world.setAllowCheats(config.isDefaultAllowCheats());
        
        // Turn off smooth snow, except for high res imports
        if (! highRes) {
            FrostExporter.FrostSettings frostSettings = new FrostExporter.FrostSettings();
            frostSettings.setMode(FrostExporter.FrostSettings.MODE_FLAT);
            dimension.setLayerSettings(Frost.INSTANCE, frostSettings);
        }

        final boolean useVoidBelow = voidBelowLevel > 0;
        final int tileX1 = offsetX >> TILE_SIZE_BITS;
        final int tileY1 = offsetY >> TILE_SIZE_BITS;
        final int tileX2 = (offsetX + widthInBlocks - 1) >> TILE_SIZE_BITS;
        final int tileY2 = (offsetY + heightInBlocks - 1) >> TILE_SIZE_BITS;
        final int widthInTiles = tileX2 - tileX1 + 1;
        final int heightInTiles = tileY2 - tileY1 + 1;
        final Raster raster = scaledImage.getRaster();
        initLevelMappingIfNecessary();
        final int totalTileCount = widthInTiles * heightInTiles;
        int tileCount = 0;
        for (int tileX = tileX1; tileX <= tileX2; tileX++) {
            for (int tileY = tileY1; tileY <= tileY2; tileY++) {
                final Tile tile = new Tile(tileX, tileY, maxHeight);
                final int xOffset = tileX * TILE_SIZE - offsetX;
                final int yOffset = tileY * TILE_SIZE - offsetY;
                for (int x = 0; x < TILE_SIZE; x++) {
                    for (int y = 0; y < TILE_SIZE; y++) {
                        final int imageX = xOffset + x;
                        final int imageY = yOffset + y;
                        final float level;
                        final boolean void_;
                        if ((imageX >= 0) && (imageX < widthInBlocks) && (imageY >= 0) && (imageY < heightInBlocks)) {
                            final int imageLevel = raster.getSample(imageX, imageY, 0);
                            level = levelMapping[imageLevel];
                            void_ = useVoidBelow && (imageLevel < voidBelowLevel);
                        } else {
                            level = 0.0f;
                            void_ = useVoidBelow;
                        }
                        tile.setHeight(x, y, level);
                        tile.setWaterLevel(x, y, worldWaterLevel);
                        if (void_) {
                            tile.setBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, x, y, true);
                        }
                        tileFactory.applyTheme(tile, x, y);
                    }
                }
                dimension.addTile(tile);
                tileCount++;
                if (progressReceiver != null) {
                    progressReceiver.setProgress((float) tileCount / totalTileCount);
                }
            }
        }

        Dimension defaults = config.getDefaultTerrainAndLayerSettings();
        dimension.setBorder(defaults.getBorder());
        dimension.setBorderSize(defaults.getBorderSize());
        dimension.setBorderLevel(worldWaterLevel);
        dimension.setBedrockWall(defaults.isBedrockWall());
        dimension.setSubsurfaceMaterial(defaults.getSubsurfaceMaterial());
        dimension.setPopulate(defaults.isPopulate());
        dimension.setTopLayerMinDepth(defaults.getTopLayerMinDepth());
        dimension.setTopLayerVariation(defaults.getTopLayerVariation());
        dimension.setBottomless(defaults.isBottomless());
        for (Map.Entry<Layer, ExporterSettings> entry: defaults.getAllLayerSettings().entrySet()) {
            dimension.setLayerSettings(entry.getKey(), entry.getValue().clone());
        }

        dimension.setGridEnabled(config.isDefaultGridEnabled());
        dimension.setGridSize(config.getDefaultGridSize());
        dimension.setContoursEnabled(config.isDefaultContoursEnabled());
        dimension.setContourSeparation(config.getDefaultContourSeparation());
        world.setSpawnPoint(new Point(offsetX + widthInBlocks / 2, offsetY + heightInBlocks / 2));
        dimension.setLastViewPosition(world.getSpawnPoint());
        world.setDirty(false);
        
        return world;
    }
    
    public BufferedImage getPreview() {
        if ((image == null) || (maxHeight != DEFAULT_MAX_HEIGHT_2)) {
            return null;
        }
        initHistogramIfNecessary();
        BufferedImage preview = new BufferedImage(DEFAULT_MAX_HEIGHT_2, DEFAULT_MAX_HEIGHT_2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = preview.createGraphics();
        try {
            for (int x = 0; x < DEFAULT_MAX_HEIGHT_2; x++) {
                
            }
        } finally {
            g2.dispose();
        }
        return preview;
    }
    
    // Properties
    
    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        bitDepth = image.getSampleModel().getSampleSize(0);
        histogram = null;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getWorldLowLevel() {
        return worldLowLevel;
    }

    public void setWorldLowLevel(int worldLowLevel) {
        this.worldLowLevel = worldLowLevel;
    }

    public int getWorldWaterLevel() {
        return worldWaterLevel;
    }

    public void setWorldWaterLevel(int worldWaterLevel) {
        this.worldWaterLevel = worldWaterLevel;
    }

    public int getWorldHighLevel() {
        return worldHighLevel;
    }

    public void setWorldHighLevel(int worldHighLevel) {
        this.worldHighLevel = worldHighLevel;
    }

    public int getImageLowLevel() {
        return imageLowLevel;
    }

    public void setImageLowLevel(int imageLowLevel) {
        this.imageLowLevel = imageLowLevel;
    }

    public int getImageHighLevel() {
        return imageHighLevel;
    }

    public void setImageHighLevel(int imageHighLevel) {
        this.imageHighLevel = imageHighLevel;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getVoidBelowLevel() {
        return voidBelowLevel;
    }

    public void setVoidBelowLevel(int voidBelowLevel) {
        this.voidBelowLevel = voidBelowLevel;
    }

    public TileFactory getTileFactory() {
        return tileFactory;
    }

    public void setTileFactory(TileFactory tileFactory) {
        this.tileFactory = tileFactory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }
    
    private void initHistogramIfNecessary() {
        if (histogram == null) {
            final int imageWidth = image.getWidth(), imageHeight = image.getHeight();
            final Raster raster = image.getRaster();
            final long[] buckets = new long[DEFAULT_MAX_HEIGHT_2];
            initLevelMappingIntIfNecessary();
            for (int imageX = 0; imageX < imageWidth; imageX++) {
                for (int imageY = 0; imageY < imageHeight; imageY++) {
                    buckets[levelMappingInt[raster.getSample(imageX, imageY, 0)]]++;
                }
            }
            histogram = new int[DEFAULT_MAX_HEIGHT_2];
            // TODO
        }
    }
    
    private void initLevelMappingIfNecessary() {
        if (levelMapping == null) {
            final boolean sixteenBit = bitDepth == 16;
            final boolean oneOnOne = (worldLowLevel == imageLowLevel) && (worldHighLevel == imageHighLevel);
            final boolean highRes = (bitDepth == 16) && (! oneOnOne);
            final float levelScale = (float) (worldHighLevel - worldLowLevel) / (imageHighLevel - imageLowLevel);
            final int maxZ = maxHeight - 1;
            final int levelCount = sixteenBit ? 65536 : 256;
            levelMapping = new float[levelCount];
            levelMappingInt = new int[levelCount];
            for (int imageLevel = 0; imageLevel < levelCount; imageLevel++) {
                final int level;
                if (invert) {
                    level = (sixteenBit ? 65535 : 255) - imageLevel;
                } else {
                    level = imageLevel;
                }
                final float mappedLevel;
                if (highRes) {
                    mappedLevel = MathUtils.clamp(0.0f, (level - imageLowLevel) * levelScale + worldLowLevel, maxZ);
                } else {
                    mappedLevel = MathUtils.clamp(0.0f, oneOnOne
                        ? level - 0.4375f
                        : (int) ((level - imageLowLevel) * levelScale + worldLowLevel) - 0.4375f, maxZ);
                }
                levelMapping[imageLevel] = mappedLevel;
                levelMappingInt[imageLevel] = (int) (mappedLevel + 0.5f);
            }
        }
    }
    
    private void initLevelMappingIntIfNecessary() {
        if (levelMappingInt == null) {
            initLevelMappingIfNecessary();
        }
    }
    
    private BufferedImage image;
    private int scale = 100, worldLowLevel, worldWaterLevel = 62, worldHighLevel = DEFAULT_MAX_HEIGHT_2 - 1, imageLowLevel, imageHighLevel = DEFAULT_MAX_HEIGHT_2 - 1, offsetX, offsetY, maxHeight = DEFAULT_MAX_HEIGHT_2, voidBelowLevel, bitDepth;
    private TileFactory tileFactory;
    private String name;
    private boolean invert;
    private float[] levelMapping;
    private int[] histogram, levelMappingInt;
    
    private static final Logger logger = Logger.getLogger(HeightMapImporter.class.getName());
}