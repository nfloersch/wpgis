/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.importing;

import java.awt.Point;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.vecmath.Point2i;

import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.Tag;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.ChunkImpl;
import org.pepsoft.minecraft.ChunkImpl2;
import org.pepsoft.minecraft.Level;
import org.pepsoft.minecraft.RegionFile;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.SubProgressReceiver;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Generator;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.TileFactory;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.Populate;
import org.pepsoft.worldpainter.layers.ReadOnly;
import org.pepsoft.worldpainter.layers.Resources;
import org.pepsoft.worldpainter.layers.exporters.FrostExporter.FrostSettings;
import org.pepsoft.worldpainter.layers.exporters.ResourcesExporter.ResourcesExporterSettings;
import org.pepsoft.worldpainter.vo.EventVO;

import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.minecraft.Material;
import static org.pepsoft.worldpainter.Constants.*;

/**
 *
 * @author pepijn
 */
public class MapImporter {
    public MapImporter(TileFactory tileFactory, File levelDatFile, boolean populateNewChunks, Set<Point2i> chunksToSkip, ReadOnlyOption readOnlyOption) {
        if ((tileFactory == null) || (levelDatFile == null) || (readOnlyOption == null)) {
            throw new NullPointerException();
        }
        if (! levelDatFile.isFile()) {
            throw new IllegalArgumentException(levelDatFile + " does not exist or is not a regular file");
        }
        this.tileFactory = tileFactory;
        this.levelDatFile = levelDatFile;
        this.populateNewChunks = populateNewChunks;
        this.chunksToSkip = chunksToSkip;
        this.readOnlyOption = readOnlyOption;
    }
    
    public World2 doImport() throws IOException {
        try {
            return doImport(null);
        } catch (ProgressReceiver.OperationCancelled e) {
            throw new InternalError();
        }
    }
    
    public World2 doImport(ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        long start = System.currentTimeMillis();

        logger.info("Importing map from " + levelDatFile.getAbsolutePath());
        Level level = Level.load(levelDatFile);
        int version = level.getVersion();
        if ((version != SUPPORTED_VERSION_1) && (version != SUPPORTED_VERSION_2)) {
            throw new UnsupportedOperationException("Level format version " + version + " not supported");
        }
        String name = level.getName().trim();
        int maxHeight = level.getMaxHeight();
        World2 world = new World2(maxHeight);
        world.setCreateGoodiesChest(false);
        world.setName(name);
        world.setSpawnPoint(new Point(level.getSpawnX(), level.getSpawnZ()));
        world.setImportedFrom(levelDatFile);
        world.setMapFeatures(level.isMapFeatures());
        world.setGameType(level.getGameType());
        world.setGenerator(level.getGenerator());
        world.setGeneratorOptions(level.getGeneratorOptions());
        world.setVersion(version);
        long minecraftSeed = level.getSeed();
        tileFactory.setSeed(minecraftSeed);
        Dimension dimension = new Dimension(minecraftSeed, tileFactory, DIM_NORMAL, maxHeight);
        dimension.setEventsInhibited(true);
        try {
            dimension.setSubsurfaceMaterial(Terrain.STONE);
            dimension.setBorderLevel(62);
            
            // Turn off smooth snow
            FrostSettings frostSettings = new FrostSettings();
            frostSettings.setMode(FrostSettings.MODE_FLAT);
            dimension.setLayerSettings(Frost.INSTANCE, frostSettings);
            
            ResourcesExporterSettings resourcesSettings = (ResourcesExporterSettings) dimension.getLayerSettings(Resources.INSTANCE);
            resourcesSettings.setMinimumLevel(0);
            if (version == SUPPORTED_VERSION_1) {
                resourcesSettings.setChance(BLK_EMERALD_ORE, 0);
            }
            File worldDir = levelDatFile.getParentFile();
            File regionDir = new File(worldDir, "region");
//            File netherDir = new File(worldDir, "DIM-1");
//            File endDir = new File(worldDir, "DIM1");
            int dimCount = 1;
//            if (netherDir.isDirectory()) {
//                dimCount++;
//            }
//            if (endDir.isDirectory()) {
//                dimCount++;
//            }
            Configuration config = Configuration.getInstance();
            dimension.setGridEnabled(config.isDefaultGridEnabled());
            dimension.setGridSize(config.getDefaultGridSize());
            dimension.setContoursEnabled(config.isDefaultContoursEnabled());
            dimension.setContourSeparation(config.getDefaultContourSeparation());
            String dimWarnings = importDimension(regionDir, dimension, version, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, 0.0f, 1.0f / dimCount) : null);
            if (dimWarnings != null) {
                if (warnings == null) {
                    warnings = dimWarnings;
                } else {
                    warnings = warnings + dimWarnings;
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
        world.addDimension(dimension);
//        int dimNo = 1;
//        if (netherDir.isDirectory()) {
//            regionDir = new File(netherDir, "region");
//            FlatTileFactory netherTileFactory = new FlatTileFactory(Terrain.NETHERLIKE, maxHeight, 127, 32, false, false);
//            netherTileFactory.getTerrainRanges().clear();
//            netherTileFactory.getTerrainRanges().put(-1, Terrain.NETHERLIKE);
//            dimension = new Dimension(seed + 1, netherTileFactory, DIM_NETHER, maxHeight);
//            dimension.setEventsInhibited(true);
//            ChasmsSettings cavernsSettings = new ChasmsSettings();
//            cavernsSettings.setChasmsEverywhereLevel(15);
//            cavernsSettings.setFloodWithLava(true);
//            cavernsSettings.setSurfaceBreaking(true);
//            cavernsSettings.setWaterLevel(32);
//            dimension.setLayerSettings(Caverns.INSTANCE, cavernsSettings);
//            importDimension(regionDir, dimension, version, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, (float) dimNo++ / dimCount, 1.0f / dimCount) : null);
//            world.addDimension(dimension);
//        }
//        if (endDir.isDirectory()) {
//            regionDir = new File(endDir, "region");
//            NoiseTileFactory endTileFactory = new NoiseTileFactory(Terrain.END_STONE, maxHeight, 32, 0, false, false);
//            endTileFactory.getTerrainRanges().clear();
//            endTileFactory.getTerrainRanges().put(-1, Terrain.END_STONE);
//            dimension = new Dimension(seed + 2, endTileFactory, DIM_END, maxHeight);
//            dimension.setEventsInhibited(true);
//            importDimension(regionDir, dimension, version, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, (float) dimNo / dimCount, 1.0f / dimCount) : null);
//            world.addDimension(dimension);
//        }
        
        // Log an event
        Configuration config = Configuration.getInstance();
        if (config != null) {
            EventVO event = new EventVO(EVENT_KEY_ACTION_IMPORT_MAP).duration(System.currentTimeMillis() - start);
            event.setAttribute(EventVO.ATTRIBUTE_TIMESTAMP, new Date(start));
            event.setAttribute(ATTRIBUTE_KEY_MAX_HEIGHT, world.getMaxHeight());
            event.setAttribute(ATTRIBUTE_KEY_VERSION, world.getVersion());
            event.setAttribute(ATTRIBUTE_KEY_MAP_FEATURES, world.isMapFeatures());
            event.setAttribute(ATTRIBUTE_KEY_GAME_TYPE, world.getGameType());
            event.setAttribute(ATTRIBUTE_KEY_ALLOW_CHEATS, world.isAllowCheats());
            event.setAttribute(ATTRIBUTE_KEY_GENERATOR, world.getGenerator().name());
            if ((world.getVersion() == SUPPORTED_VERSION_2) && (world.getGenerator() == Generator.FLAT)) {
                event.setAttribute(ATTRIBUTE_KEY_GENERATOR_OPTIONS, world.getGeneratorOptions());
            }
            event.setAttribute(ATTRIBUTE_KEY_TILES, dimension.getTiles().size());
            config.logEvent(event);
        }
        
        return world;
    }

    public String getWarnings() {
        return warnings;
    }
    
    private String importDimension(File regionDir, Dimension dimension, int version, ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        if (progressReceiver != null) {
            progressReceiver.setMessage(dimension.getName() + " dimension");
        }
        final int maxHeight = dimension.getMaxHeight();
        final int maxY = maxHeight - 1;
        final Pattern regionFilePattern = (version == SUPPORTED_VERSION_1)
            ? Pattern.compile("r\\.-?\\d+\\.-?\\d+\\.mcr")
            : Pattern.compile("r\\.-?\\d+\\.-?\\d+\\.mca");
        final File[] regionFiles = regionDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return regionFilePattern.matcher(name).matches();
            }
        });
        if ((regionFiles == null) || (regionFiles.length == 0)) {
            throw new RuntimeException("The " + dimension.getName() + " dimension of this map has no region files!");
        }
        final Set<Point> newChunks = new HashSet<Point>();
//        final SortedSet<Material> manMadeBlockTypes = new TreeSet<Material>();
        final boolean importBiomes = (version == SUPPORTED_VERSION_2) && (dimension.getDim() == DIM_NORMAL);
        final int total = regionFiles.length * 1024;
        int count = 0;
        final StringBuilder reportBuilder = new StringBuilder();
        for (File file: regionFiles) {
            try {
                RegionFile regionFile = new RegionFile(file);
                try {
                    for (int x = 0; x < 32; x++) {
                        for (int z = 0; z < 32; z++) {
                            if (progressReceiver != null) {
                                progressReceiver.setProgress((float) count / total);
                                count++;
                            }
                            final Point2i chunkCoords = new Point2i((regionFile.getX() << 5) | x, (regionFile.getZ() << 5) | z);
                            if ((chunksToSkip != null) && chunksToSkip.contains(chunkCoords)) {
                                continue;
                            }
                            if (regionFile.containsChunk(x, z)) {
                                final Tag tag;
                                try {
                                    final InputStream chunkData = regionFile.getChunkDataInputStream(x, z);
                                    if (chunkData == null) {
                                        // This should never happen, since we checked
                                        // with containsChunk(), but in practice it
                                        // does. Perhaps corrupted data?
                                        reportBuilder.append("Missing chunk data for chunk " + x + ", " + z + " in " + file + "; skipping chunk" + EOL);
                                        logger.warning("Missing chunk data for chunk " + x + ", " + z + " in " + file + "; skipping chunk");
                                        continue;
                                    }
                                    final NBTInputStream in = new NBTInputStream(chunkData);
                                    try {
                                        tag = in.readTag();
                                    } finally {
                                        in.close();
                                    }
                                } catch (IOException e) {
                                    reportBuilder.append("I/O error while reading chunk " + x + ", " + z + " from file " + file + " (message: \"" + e.getMessage() + "\"); skipping chunk" + EOL);
                                    logger.log(java.util.logging.Level.SEVERE, "I/O error while reading chunk " + x + ", " + z + " from file " + file + "; skipping chunk", e);
                                    continue;
                                } catch (IllegalArgumentException e) {
                                    reportBuilder.append("Illegal argument exception while reading chunk " + x + ", " + z + " from file " + file + " (message: \"" + e.getMessage() + "\"); skipping chunk" + EOL);
                                    logger.log(java.util.logging.Level.SEVERE, "Illegal argument exception while reading chunk " + x + ", " + z + " from file " + file + "; skipping chunk", e);
                                    continue;
                                }
                                final Chunk chunk = (version == SUPPORTED_VERSION_1)
                                    ? new ChunkImpl((CompoundTag) tag, maxHeight)
                                    : new ChunkImpl2((CompoundTag) tag, maxHeight);

                                final Point tileCoords = new Point(chunk.getxPos() >> 3, chunk.getzPos() >> 3);
                                Tile tile = dimension.getTile(tileCoords);
                                if (tile == null) {
                                    tile = dimension.getTileFactory().createTile(tileCoords.x, tileCoords.y);
                                    for (int xx = 0; xx < 8; xx++) {
                                        for (int yy = 0; yy < 8; yy++) {
                                            newChunks.add(new Point((tileCoords.x << TILE_SIZE_BITS) | (xx << 4), (tileCoords.y << TILE_SIZE_BITS) | (yy << 4)));
                                        }
                                    }
                                    dimension.addTile(tile);
                                }
                                newChunks.remove(new Point(chunk.getxPos() << 4, chunk.getzPos() << 4));

                                boolean manMadeStructuresBelowGround = false;
                                boolean manMadeStructuresAboveGround = false;
                                try {
                                    for (int xx = 0; xx < 16; xx++) {
                                        for (int zz = 0; zz < 16; zz++) {
                                            float height = -1.0f;
                                            int waterLevel = 0;
                                            boolean floodWithLava = false, frost = false;
                                            Terrain terrain = Terrain.BEDROCK;
                                            for (int y = maxY; y >= 0; y--) {
                                                int blockType = chunk.getBlockType(xx, y, zz);
                                                int data = chunk.getDataValue(xx, y, zz);
                                                if (! NATURAL_BLOCKS.contains(blockType)) {
                                                    if (height == -1.0f) {
                                                        manMadeStructuresAboveGround = true;
                                                    } else {
                                                        manMadeStructuresBelowGround = true;
                                                    }
//                                                    manMadeBlockTypes.add(Material.get(blockType, data));
                                                }
                                                if ((blockType == BLK_SNOW) || (blockType == BLK_ICE)) {
                                                    frost = true;
                                                }
                                                if (((blockType == BLK_ICE) || (((blockType == BLK_STATIONARY_WATER) || (blockType == BLK_WATER) || (blockType == BLK_STATIONARY_LAVA) || (blockType == BLK_LAVA)) && (data == 0))) && (waterLevel == 0)) {
                                                    waterLevel = y;
                                                    if ((blockType == BLK_LAVA) || (blockType == BLK_STATIONARY_LAVA)) {
                                                        floodWithLava = true;
                                                    }
                                                } else if (height == -1.0f) {
                                                    final Material material = Material.get(blockType, data);
                                                    if (SPECIAL_TERRAIN_MAPPING.containsKey(material)) {
                                                        // Special terrain found
                                                        height = y - 0.4375f; // Value that falls in the middle of the lowest one eigthth which will still round to the same integer value and will receive a one layer thick smooth snow block (principle of least surprise)
                                                        terrain = SPECIAL_TERRAIN_MAPPING.get(material);
                                                    } else if (TERRAIN_MAPPING.containsKey(blockType)) {
                                                        // Terrain found
                                                        height = y - 0.4375f; // Value that falls in the middle of the lowest one eigthth which will still round to the same integer value and will receive a one layer thick smooth snow block (principle of least surprise)
                                                        terrain = TERRAIN_MAPPING.get(blockType);
                                                    }
                                                }
                                            }
                                            // Use smooth snow, if present, to better approximate world height, so smooth snow will survive merge
                                            final int intHeight = (int) (height + 0.5f);
                                            if ((height != -1.0f) && (intHeight < maxY) && (chunk.getBlockType(xx, intHeight + 1, zz) == BLK_SNOW)) {
                                                int data = chunk.getDataValue(xx, intHeight + 1, zz);
                                                height += data * 0.125;
                                                
                                            }
                                            if ((waterLevel == 0) && (height >= 61.5f)) {
                                                waterLevel = 62;
                                            }

                                            final int blockX = (chunk.getxPos() << 4) | xx;
                                            final int blockY = (chunk.getzPos() << 4) | zz;
                                            final Point coords = new Point(blockX, blockY);
                                            dimension.setTerrainAt(coords, terrain);
                                            dimension.setHeightAt(coords, Math.max(height, 0.0f));
                                            dimension.setWaterLevelAt(blockX, blockY, waterLevel);
                                            if (frost) {
                                                dimension.setBitLayerValueAt(Frost.INSTANCE, blockX, blockY, true);
                                            }
                                            if (floodWithLava) {
                                                dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, blockX, blockY, true);
                                            }
                                            if (height == -1.0f) {
                                                dimension.setBitLayerValueAt(org.pepsoft.worldpainter.layers.Void.INSTANCE, blockX, blockY, true);
                                            }
                                            if (importBiomes && ((ChunkImpl2) chunk).isBiomesAvailable()) {
                                                final int biome = ((ChunkImpl2) chunk).getBiome(xx, zz);
                                                if (biome != 255) {
                                                    // Around the edges of the map
                                                    // Minecraft sets the biome to 255,
                                                    // presumable as a marker that it
                                                    // has yet to be calculated,
                                                    // although there seems to be no
                                                    // reason why it couldn't be
                                                    // calculated already
                                                    // TODO: is this a replacement for
                                                    // the populate flag?
                                                    dimension.setLayerValueAt(Biome.INSTANCE, blockX, blockY, biome);
                                                }
                                            }
                                        }
                                    }
                                } catch (NullPointerException e) {
                                    reportBuilder.append("Null pointer exception while reading chunk " + x + ", " + z + " from file " + file + "; skipping chunk" + EOL);
                                    logger.log(java.util.logging.Level.SEVERE, "Null pointer exception while reading chunk " + x + ", " + z + " from file " + file + "; skipping chunk", e);
                                    continue;
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    reportBuilder.append("Array index out of bounds while reading chunk " + x + ", " + z + " from file " + file + " (message: \"" + e.getMessage() + "\"); skipping chunk" + EOL);
                                    logger.log(java.util.logging.Level.SEVERE, "Array index out of bounds while reading chunk " + x + ", " + z + " from file " + file + "; skipping chunk", e);
                                    continue;
                                }

                                if (((readOnlyOption == ReadOnlyOption.MAN_MADE) && (manMadeStructuresBelowGround || manMadeStructuresAboveGround))
                                        || ((readOnlyOption == ReadOnlyOption.MAN_MADE_ABOVE_GROUND) && manMadeStructuresAboveGround)
                                        || (readOnlyOption == ReadOnlyOption.ALL)) {
                                    dimension.setBitLayerValueAt(ReadOnly.INSTANCE, chunk.getxPos() << 4, chunk.getzPos() << 4, true);
                                }
                            }
                        }
                    }
                } finally {
                    regionFile.close();
                }
            } catch (IOException e) {
                reportBuilder.append("I/O error while opening region file " + file + " (message: \"" + e.getMessage() + "\"); skipping region" + EOL);
                logger.log(java.util.logging.Level.SEVERE, "I/O error while opening region file " + file + "; skipping region", e);
            }
        }
        
        // Process chunks that were only added to fill out a tile. They should
        // *always* be read-only, regardless of the setting of the read-only
        // option
        for (Point newChunkCoords: newChunks) {
            dimension.setBitLayerValueAt(ReadOnly.INSTANCE, newChunkCoords.x, newChunkCoords.y, true);
            if (populateNewChunks) {
                dimension.setBitLayerValueAt(Populate.INSTANCE, newChunkCoords.x, newChunkCoords.y, true);
            }
        }
        
        if (progressReceiver != null) {
            progressReceiver.setProgress(1.0f);
        }
        
//        System.err.println("Man-made block types encountered:");
//        for (Material blockType: manMadeBlockTypes) {
//            System.err.println(blockType);
//        }
        
        return reportBuilder.length() != 0 ? reportBuilder.toString() : null;
    }
    
    private final TileFactory tileFactory;
    private final File levelDatFile;
    private final boolean populateNewChunks;
    private final Set<Point2i> chunksToSkip;
    private final ReadOnlyOption readOnlyOption;
    private String warnings;
    
    public static final Map<Integer, Terrain> TERRAIN_MAPPING = new HashMap<Integer, Terrain>();
    public static final Map<Material, Terrain> SPECIAL_TERRAIN_MAPPING = new HashMap<Material, Terrain>();
    
    private static final Set<Integer> NATURAL_BLOCKS = new HashSet<Integer>();
    private static final Logger logger = Logger.getLogger(MapImporter.class.getName());
    private static final String EOL = System.getProperty("line.separator");
    
    static {
        TERRAIN_MAPPING.put(BLK_STONE, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_GRASS, Terrain.BARE_GRASS);
        TERRAIN_MAPPING.put(BLK_DIRT, Terrain.DIRT);
        TERRAIN_MAPPING.put(BLK_BEDROCK, Terrain.BEDROCK);
        TERRAIN_MAPPING.put(BLK_SAND, Terrain.SAND);
        TERRAIN_MAPPING.put(BLK_GRAVEL, Terrain.GRAVEL);
        TERRAIN_MAPPING.put(BLK_GOLD_ORE, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_IRON_ORE, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_COAL, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_LAPIS_LAZULI_ORE, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_DIAMOND_ORE, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_REDSTONE_ORE, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_GLOWING_REDSTONE_ORE, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_HIDDEN_SILVERFISH, Terrain.STONE);
        TERRAIN_MAPPING.put(BLK_SANDSTONE, Terrain.SANDSTONE);
        TERRAIN_MAPPING.put(BLK_OBSIDIAN, Terrain.OBSIDIAN);
        TERRAIN_MAPPING.put(BLK_TILLED_DIRT, Terrain.DIRT);
        TERRAIN_MAPPING.put(BLK_SNOW_BLOCK, Terrain.DEEP_SNOW);
        TERRAIN_MAPPING.put(BLK_CLAY, Terrain.CLAY);
        TERRAIN_MAPPING.put(BLK_NETHERRACK, Terrain.NETHERRACK);
        TERRAIN_MAPPING.put(BLK_QUARTZ_ORE, Terrain.NETHERRACK);
        TERRAIN_MAPPING.put(BLK_SOUL_SAND, Terrain.SOUL_SAND);
        TERRAIN_MAPPING.put(BLK_MYCELIUM, Terrain.MYCELIUM);
        TERRAIN_MAPPING.put(BLK_END_STONE, Terrain.END_STONE);
        TERRAIN_MAPPING.put(BLK_HARDENED_CLAY, Terrain.HARDENED_CLAY);
        
        SPECIAL_TERRAIN_MAPPING.put(Material.RED_SAND, Terrain.RED_SAND);
        SPECIAL_TERRAIN_MAPPING.put(Material.PERMADIRT, Terrain.PERMADIRT);
        SPECIAL_TERRAIN_MAPPING.put(Material.PODZOL, Terrain.PODZOL);
        SPECIAL_TERRAIN_MAPPING.put(Material.WHITE_CLAY, Terrain.WHITE_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.ORANGE_CLAY, Terrain.ORANGE_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.MAGENTA_CLAY, Terrain.MAGENTA_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.LIGHT_BLUE_CLAY, Terrain.LIGHT_BLUE_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.YELLOW_CLAY, Terrain.YELLOW_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.LIME_CLAY, Terrain.LIME_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.PINK_CLAY, Terrain.PINK_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.GREY_CLAY, Terrain.GREY_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.LIGHT_GREY_CLAY, Terrain.LIGHT_GREY_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.CYAN_CLAY, Terrain.CYAN_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.PURPLE_CLAY, Terrain.PURPLE_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.BLUE_CLAY, Terrain.BLUE_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.BROWN_CLAY, Terrain.BROWN_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.GREEN_CLAY, Terrain.GREEN_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.RED_CLAY, Terrain.RED_STAINED_CLAY);
        SPECIAL_TERRAIN_MAPPING.put(Material.BLACK_CLAY, Terrain.BLACK_STAINED_CLAY);
        
        NATURAL_BLOCKS.addAll(TERRAIN_MAPPING.keySet());
        for (Material material: SPECIAL_TERRAIN_MAPPING.keySet()) {
            NATURAL_BLOCKS.add(material.getBlockType());
        }
        NATURAL_BLOCKS.remove(BLK_TILLED_DIRT);
        NATURAL_BLOCKS.add(BLK_AIR);
        NATURAL_BLOCKS.add(BLK_WOOD);
        NATURAL_BLOCKS.add(BLK_WOOD2);
        NATURAL_BLOCKS.add(BLK_LEAVES);
        NATURAL_BLOCKS.add(BLK_LEAVES2);
        NATURAL_BLOCKS.add(BLK_DANDELION);
        NATURAL_BLOCKS.add(BLK_ROSE);
        NATURAL_BLOCKS.add(BLK_BROWN_MUSHROOM);
        NATURAL_BLOCKS.add(BLK_RED_MUSHROOM);
        NATURAL_BLOCKS.add(BLK_FIRE);
        NATURAL_BLOCKS.add(BLK_SNOW);
        NATURAL_BLOCKS.add(BLK_ICE);
        NATURAL_BLOCKS.add(BLK_CACTUS);
        NATURAL_BLOCKS.add(BLK_SUGAR_CANE);
        NATURAL_BLOCKS.add(BLK_PUMPKIN);
        NATURAL_BLOCKS.add(BLK_WATER);
        NATURAL_BLOCKS.add(BLK_STATIONARY_WATER);
        NATURAL_BLOCKS.add(BLK_LAVA);
        NATURAL_BLOCKS.add(BLK_STATIONARY_LAVA);
        NATURAL_BLOCKS.add(BLK_TALL_GRASS);
        NATURAL_BLOCKS.add(BLK_DEAD_SHRUBS);
        NATURAL_BLOCKS.add(BLK_VINES);
        NATURAL_BLOCKS.add(BLK_HUGE_BROWN_MUSHROOM);
        NATURAL_BLOCKS.add(BLK_HUGE_RED_MUSHROOM);
        NATURAL_BLOCKS.add(BLK_LILY_PAD);
        NATURAL_BLOCKS.add(BLK_GLOWSTONE);
        NATURAL_BLOCKS.add(BLK_COCOA_PLANT);
        NATURAL_BLOCKS.add(BLK_EMERALD_ORE);
        NATURAL_BLOCKS.add(BLK_PACKED_ICE);
        NATURAL_BLOCKS.add(BLK_LARGE_FLOWERS);
        NATURAL_BLOCKS.add(BLK_NETHER_WART);
        
        // Dungeons:
        NATURAL_BLOCKS.add(BLK_MONSTER_SPAWNER);
        NATURAL_BLOCKS.add(BLK_CHEST);
        NATURAL_BLOCKS.add(BLK_COBBLESTONE);
        NATURAL_BLOCKS.add(BLK_MOSSY_COBBLESTONE);
        
        // Don't add blocks occurring in abandoned mines and strongholds, as we
        // want to protect those too, and it would make it too hard to detect
        // actually man-made areas. The downside is that the read-only layer
        // reveals where they are when importing a map.
    }
    
    public enum ReadOnlyOption {NONE, MAN_MADE, MAN_MADE_ABOVE_GROUND, ALL}
}
