/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import static org.pepsoft.minecraft.Constants.DEFAULT_MAX_HEIGHT_2;
import org.pepsoft.util.MathUtils;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.themes.SimpleTheme;

/**
 *
 * @author pepijn
 */
public final class WorldFactory {
    private WorldFactory() {
        // Prevent instantiation
    }
    
    public static World2 createDefaultWorld(final Configuration config, final long seed) {
//            final HeightMapTileFactory tileFactory = new ExperimentalTileFactory(config.getDefaultMaxHeight());
        final HeightMapTileFactory tileFactory;
        if (config.isHilly()) {
            tileFactory = TileFactoryFactory.createNoiseTileFactory(seed, config.getSurface(), config.getDefaultMaxHeight(), config.getLevel(), config.getWaterLevel(), config.isLava(), config.isBeaches(), config.getDefaultRange(), config.getDefaultScale());
        } else {
            tileFactory = TileFactoryFactory.createFlatTileFactory(seed, config.getSurface(), config.getDefaultMaxHeight(), config.getLevel(), config.getWaterLevel(), config.isLava(), config.isBeaches());
        }
//            tileFactory = TileFactoryFactory.createFancyTileFactory(seed);
        final Dimension defaults = config.getDefaultTerrainAndLayerSettings();
        if ((defaults.getTileFactory() instanceof HeightMapTileFactory)
                && ((((HeightMapTileFactory) defaults.getTileFactory()).getTheme() instanceof SimpleTheme)
                && ((SimpleTheme) ((HeightMapTileFactory) defaults.getTileFactory()).getTheme()).getTerrainRanges() != null)) {
            HeightMapTileFactory defaultTileFactory = (HeightMapTileFactory) defaults.getTileFactory();
            SimpleTheme defaultTheme = (SimpleTheme) defaultTileFactory.getTheme();
            SimpleTheme theme = (SimpleTheme) tileFactory.getTheme();
            theme.setTerrainRanges(new TreeMap<Integer, Terrain>(defaultTheme.getTerrainRanges()));
            theme.setRandomise(defaultTheme.isRandomise());
        }
        final World2 world = new World2(World2.DEFAULT_OCEAN_SEED, tileFactory, tileFactory.getMaxHeight());
        if (config.getDefaultMaxHeight() == DEFAULT_MAX_HEIGHT_2) {
            world.setGenerator(Generator.LARGE_BIOMES);
        }
//            world.setMixedMaterial(0, new MixedMaterial("Dirt/Gravel", new Row[] {new Row(Material.DIRT, 750, 1.0f), new Row(Material.GRAVEL, 250, 1.0f)}, Minecraft1_2BiomeScheme.BIOME_PLAINS, false, 1.0f, null));
//            world.setMixedMaterial(1, new MixedMaterial("Stone/Gravel", new Row[] {new Row(Material.STONE, 750, 1.0f), new Row(Material.GRAVEL, 250, 1.0f)}, Minecraft1_2BiomeScheme.BIOME_PLAINS, false, 1.0f, null));
        final ResourceBundle strings = ResourceBundle.getBundle("org.pepsoft.worldpainter.resources.strings");
        world.setName(strings.getString("generated.world"));
        final Dimension dim0 = world.getDimension(0);
        final boolean circularWorld = config.isDefaultCircularWorld();
        final int radius = config.getDefaultWidth() * 64;
//            final boolean circularWorld = true;
//            final int radius = 750;
        dim0.setEventsInhibited(true);
        try {
            if (circularWorld) {
                final int tileRadius = (radius + 127) / 128;
                for (int x = -tileRadius; x < tileRadius; x++) {
                    for (int y = -tileRadius; y < tileRadius; y++) {
                        if (org.pepsoft.worldpainter.util.MathUtils.getSmallestDistanceFromOrigin(x, y) < radius) {
                            // At least one corner is inside the circle; include
                            // the tile. Note that this is always correct in
                            // this case only because the centre of the circle
                            // is always at a tile intersection so the circle
                            // can never "bulge" into a tile without any of the
                            // the tile's corners being inside the circle
                            final Tile tile = tileFactory.createTile(x, y);
                            dim0.addTile(tile);
                            if (org.pepsoft.worldpainter.util.MathUtils.getLargestDistanceFromOrigin(x, y) >= radius) {
                                // The tile is not completely inside the circle,
                                // so use the Void layer to create the shape of
                                // the edge
                                for (int xx = 0; xx < TILE_SIZE; xx++) {
                                    for (int yy = 0; yy < TILE_SIZE; yy++) {
                                        float distance = MathUtils.getDistance(x * TILE_SIZE + xx + 0.5f, y * TILE_SIZE + yy + 0.5f);
                                        if (distance > radius) {
                                            tile.setBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, xx, yy, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Assume the user will want a void border by default; override
                // the preferences
                dim0.setBorder(Dimension.Border.VOID);
                dim0.setBorderSize(2);
            } else {
                final int width = config.getDefaultWidth(), height = config.getDefaultHeight();
                final int startX = -width / 2;
                final int startY = -height / 2;
                for (int x = startX; x < startX + width; x++) {
                    for (int y = startY; y < startY + height; y++) {
                        final Tile tile = tileFactory.createTile(x, y);
                        dim0.addTile(tile);
                    }
                }
                
                dim0.setBorder(defaults.getBorder());
                dim0.setBorderSize(defaults.getBorderSize());
                dim0.setBedrockWall(defaults.isBedrockWall());
            }
            dim0.setBorderLevel(defaults.getBorderLevel());
            dim0.setSubsurfaceMaterial(defaults.getSubsurfaceMaterial());
            dim0.setPopulate(defaults.isPopulate());
            for (Map.Entry<Layer, ExporterSettings> entry: defaults.getAllLayerSettings().entrySet()) {
                dim0.setLayerSettings(entry.getKey(), entry.getValue().clone());
            }
            dim0.setGridEnabled(config.isDefaultGridEnabled());
            dim0.setGridSize(config.getDefaultGridSize());
            dim0.setContoursEnabled(config.isDefaultContoursEnabled());
            dim0.setContourSeparation(config.getDefaultContourSeparation());
            dim0.setTopLayerMinDepth(defaults.getTopLayerMinDepth());
            dim0.setTopLayerVariation(defaults.getTopLayerVariation());
            dim0.setBottomless(defaults.isBottomless());
        } finally {
            dim0.setEventsInhibited(false);
        }
//            final RiverGenerator riverGenerator = new RiverGenerator(dim0);
//            riverGenerator.generateRivers();
        if (tileFactory.getMaxHeight() == DEFAULT_MAX_HEIGHT_2) {
            world.setBiomeAlgorithm(config.isDefaultAutomaticBiomesEnabled() ? World2.BIOME_ALGORITHM_AUTO_BIOMES : World2.BIOME_ALGORITHM_NONE);
            world.setCustomBiomes(config.isDefaultCustomBiomesEnabled());
        } else {
            world.setBiomeAlgorithm(World2.BIOME_ALGORITHM_NONE);
            world.setCustomBiomes(false);
        }
        world.setDirty(false);
        return world;
    }
}