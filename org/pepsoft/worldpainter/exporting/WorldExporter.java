/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.exporting;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.pepsoft.minecraft.Chest;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.ChunkFactory;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.InventoryItem;
import org.pepsoft.minecraft.Level;
import org.pepsoft.minecraft.Material;
import org.pepsoft.util.Box;
import org.pepsoft.util.FileUtils;
import org.pepsoft.util.ParallelProgressManager;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.SubProgressReceiver;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Generator;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.gardenofeden.GardenExporter;
import org.pepsoft.worldpainter.gardenofeden.Seed;
import org.pepsoft.worldpainter.layers.GardenCategory;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.ReadOnly;
import org.pepsoft.worldpainter.util.FileInUseException;
import org.pepsoft.worldpainter.vo.AttributeKeyVO;
import org.pepsoft.worldpainter.vo.EventVO;

import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.layers.CombinedLayer;

/**
 *
 * @author pepijn
 */
public class WorldExporter {
    public WorldExporter(World2 world) {
        if (world == null) {
            throw new NullPointerException();
        }
        this.world = world;
        this.selectedTiles = world.getTilesToExport();
        this.selectedDimension = (selectedTiles != null) ? world.getDimensionToExport() : -1;
    }

    public World2 getWorld() {
        return world;
    }
    
    public File selectBackupDir(File worldDir) throws IOException {
        File baseDir = worldDir.getParentFile();
        File minecraftDir = baseDir.getParentFile();
        File backupsDir = new File(minecraftDir, "backups");
        if ((! backupsDir.isDirectory()) &&  (! backupsDir.mkdirs())) {
            backupsDir = new File(System.getProperty("user.home"), "WorldPainter Backups");
            if ((! backupsDir.isDirectory()) && (! backupsDir.mkdirs())) {
                throw new IOException("Could not create " + backupsDir);
            }
        }
        return new File(backupsDir, worldDir.getName() + "." + DATE_FORMAT.format(new Date()));
    }

    public Map<Integer, ChunkFactory.Stats> export(File baseDir, String name, File backupDir, ProgressReceiver progressReceiver) throws IOException, ProgressReceiver.OperationCancelled {
        // Sanity checks
        if ((world.getVersion() != SUPPORTED_VERSION_1) && (world.getVersion() != SUPPORTED_VERSION_2)) {
            throw new IllegalArgumentException("Not a supported version: 0x" + Integer.toHexString(world.getVersion()));
        }
        
        // Backup existing level
        File worldDir = new File(baseDir, FileUtils.sanitiseName(name));
        logger.info("Exporting world " + world.getName() + " to map at " + worldDir);
        if (worldDir.isDirectory()) {
            logger.info("Directory already exists; backing up to " + backupDir);
            if (! worldDir.renameTo(backupDir)) {
                throw new FileInUseException("Could not move " + worldDir + " to " + backupDir);
            }
        }
        
        // Record start of export
        long start = System.currentTimeMillis();
        
        // Export dimensions
        Dimension dim0 = world.getDimension(0);
        Level level = new Level(world.getMaxHeight(), world.getVersion());
        level.setSeed(dim0.getMinecraftSeed());
        level.setName(name);
        Point spawnPoint = world.getSpawnPoint();
        level.setSpawnX(spawnPoint.x);
        level.setSpawnY(Math.max(dim0.getIntHeightAt(spawnPoint), dim0.getWaterLevelAt(spawnPoint)));
        level.setSpawnZ(spawnPoint.y);
        level.setMapFeatures(world.isMapFeatures());
        level.setGameType(world.getGameType());
        level.setAllowCommands(world.isAllowCheats());
        level.setGenerator(world.getGenerator());
        if ((world.getVersion() == SUPPORTED_VERSION_2) && (world.getGenerator() == Generator.FLAT) && (world.getGeneratorOptions() != null)) {
            level.setGeneratorOptions(world.getGeneratorOptions());
        }
        level.save(worldDir);
        int totalDimensions = world.getDimensions().length, dim = 0;
        Map<Integer, ChunkFactory.Stats> stats = new HashMap<Integer, ChunkFactory.Stats>();
        if (selectedDimension == -1) {
            for (Dimension dimension: world.getDimensions()) {
                stats.put(dimension.getDim(), exportDimension(worldDir, dimension, world.getVersion(), (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, (float) dim / totalDimensions, 1.0f / totalDimensions) : null));
                dim++;
            }
        } else {
            stats.put(selectedDimension, exportDimension(worldDir, world.getDimension(selectedDimension), world.getVersion(), progressReceiver));
        }
        
        // Log an event
        Configuration config = Configuration.getInstance();
        if (config != null) {
            EventVO event = new EventVO(EVENT_KEY_ACTION_EXPORT_WORLD).duration(System.currentTimeMillis() - start);
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
            Dimension dimension = world.getDimension(0);
            event.setAttribute(ATTRIBUTE_KEY_TILES, dimension.getTiles().size());
            logLayers(dimension, event, "");
            dimension = world.getDimension(1);
            if (dimension != null) {
                event.setAttribute(ATTRIBUTE_KEY_NETHER_TILES, dimension.getTiles().size());
                logLayers(dimension, event, "nether.");
            }
            dimension = world.getDimension(2);
            if (dimension != null) {
                event.setAttribute(ATTRIBUTE_KEY_END_TILES, dimension.getTiles().size());
                logLayers(dimension, event, "end.");
            }
            if (selectedDimension != -1) {
                event.setAttribute(ATTRIBUTE_KEY_EXPORTED_DIMENSION, selectedDimension);
                event.setAttribute(ATTRIBUTE_KEY_EXPORTED_DIMENSION_TILES, selectedTiles.size());
            }
            if (world.getImportedFrom() != null) {
                event.setAttribute(ATTRIBUTE_KEY_IMPORTED_WORLD, true);
            }
            config.logEvent(event);
        }
        
        return stats;
    }
    
    protected ExportResults firstPass(MinecraftWorld minecraftWorld, Dimension dimension, Point regionCoords, Map<Point, Tile> tiles, boolean tileSelection, Map<Layer, LayerExporter<Layer>> exporters, ChunkFactory chunkFactory, ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled, IOException {
        int lowestChunkX = (regionCoords.x << 5) - 1;
        int highestChunkX = (regionCoords.x << 5) + 32;
        int lowestChunkY = (regionCoords.y << 5) - 1;
        int highestChunkY = (regionCoords.y << 5) + 32;
        int lowestRegionChunkX = lowestChunkX + 1;
        int highestRegionChunkX = highestChunkX - 1;
        int lowestRegionChunkY = lowestChunkY + 1;
        int highestRegionChunkY = highestChunkY - 1;
        ExportResults exportResults = new ExportResults();
        int chunkNo = 0;
        for (int chunkX = lowestChunkX; chunkX <= highestChunkX; chunkX++) {
            for (int chunkY = lowestChunkY; chunkY <= highestChunkY; chunkY++) {
                ChunkFactory.ChunkCreationResult chunkCreationResult = getChunk(dimension, chunkFactory, tiles, chunkX, chunkY, tileSelection, exporters);
                if (chunkCreationResult != null) {
                    if ((chunkX >= lowestRegionChunkX) && (chunkX <= highestRegionChunkX) && (chunkY >= lowestRegionChunkY) && (chunkY <= highestRegionChunkY)) {
                        exportResults.chunksGenerated = true;
                        exportResults.stats.landArea += chunkCreationResult.stats.landArea;
                        exportResults.stats.surfaceArea += chunkCreationResult.stats.surfaceArea;
                        exportResults.stats.waterArea += chunkCreationResult.stats.waterArea;
                    }
                    minecraftWorld.addChunk(chunkCreationResult.chunk);
                }
                chunkNo++;
                if (progressReceiver != null) {
                    progressReceiver.setProgress((float) chunkNo / 1156);
                }
            }
        }
        return exportResults;
    }
    
    protected List<Fixup> secondPass(List<Layer> secondaryPassLayers, Set<Layer> mandatoryLayers, Dimension dimension, MinecraftWorld minecraftWorld, Map<Layer, LayerExporter<Layer>> exporters, Collection<Tile> tiles, Point regionCoords, ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled {
        // Apply other secondary pass layers, except frost
        int layerCount = secondaryPassLayers.size(), counter = 0;
        Rectangle area = new Rectangle((regionCoords.x << 9) - 16, (regionCoords.y << 9) - 16, 544, 544);
        Rectangle exportedArea = new Rectangle((regionCoords.x << 9), (regionCoords.y << 9), 512, 512);
        List<Fixup> fixups = new ArrayList<Fixup>();
//        boolean frost = false;
        for (Layer layer: secondaryPassLayers) {
//            if (layer instanceof Frost) {
//                frost = true;
//                continue;
//            }
            @SuppressWarnings("unchecked")
            SecondPassLayerExporter<Layer> exporter = (SecondPassLayerExporter<Layer>) exporters.get(layer);
            List<Fixup> layerFixups = exporter.render(dimension, area, exportedArea, minecraftWorld);
            if (layerFixups != null) {
                fixups.addAll(layerFixups);
            }
            if (progressReceiver != null) {
                counter++;
                progressReceiver.setProgress((float) counter / layerCount);
            }
        }

        // Garden / seeds first and second pass
        GardenExporter gardenExporter = new GardenExporter();
        Set<Seed> firstPassProcessedSeeds = new HashSet<Seed>();
        Set<Seed> secondPassProcessedSeeds = new HashSet<Seed>();
        for (Tile tile: tiles) {
            if (tile.getLayers().contains(GardenCategory.INSTANCE)) {
                gardenExporter.firstPass(dimension, tile, minecraftWorld, firstPassProcessedSeeds);
                gardenExporter.secondPass(dimension, tile, minecraftWorld, secondPassProcessedSeeds);
            }
        }
        
        // Apply frost layer
//        if (frost) {
//            @SuppressWarnings("unchecked")
//            SecondPassLayerExporter<Layer> exporter = (SecondPassLayerExporter<Layer>) exporters.get(Frost.INSTANCE);
//            exporter.render(dimension, area, exportedArea, minecraftWorld);
//            if (progressReceiver != null) {
//                counter++;
//                progressReceiver.setProgress((float) counter / layerCount);
//            }
//        }
        
        // TODO: trying to do this for every region should work but is not very
        // elegant
        if ((dimension.getDim() == 0) && world.isCreateGoodiesChest()) {
            Point goodiesPoint = (Point) world.getSpawnPoint().clone();
            goodiesPoint.translate(3, 3);
            int height = Math.min(dimension.getIntHeightAt(goodiesPoint) + 1, dimension.getMaxHeight() - 1);
            minecraftWorld.setBlockTypeAt(goodiesPoint.x, goodiesPoint.y, height, BLK_CHEST);
            Chunk chunk = minecraftWorld.getChunk(goodiesPoint.x >> 4, goodiesPoint.y >> 4);
            if (chunk != null) {
                Chest goodiesChest = createGoodiesChest();
                goodiesChest.setX(goodiesPoint.x);
                goodiesChest.setY(height);
                goodiesChest.setZ(goodiesPoint.y);
                chunk.getTileEntities().add(goodiesChest);
            }
        }

        return fixups;
    }

    protected void postProcess(MinecraftWorld minecraftWorld, Point regionCoords, SubProgressReceiver progressReceiver) throws OperationCancelled {
        final int x1 = regionCoords.x << 9;
        final int y1 = regionCoords.y << 9;
        final int x2 = x1 + 511, y2 = y1 + 511;
        final int maxZ = minecraftWorld.getMaxHeight() - 1;
        final boolean dry = false;
        for (int x = x1; x <= x2; x ++) {
            for (int y = y1; y <= y2; y++) {
                int blockTypeBelow = minecraftWorld.getBlockTypeAt(x, y, 0);
                if (supportSand && (blockTypeBelow == BLK_SAND)) {
                    
                    minecraftWorld.setMaterialAt(x, y, 0, Material.SANDSTONE);
                    blockTypeBelow = BLK_SANDSTONE;
                }
                for (int z = 1; z <= maxZ; z++) {
                    int blockType = minecraftWorld.getBlockTypeAt(x, y, z);
                    if (((blockTypeBelow == BLK_GRASS) || (blockTypeBelow == BLK_MYCELIUM) || (blockTypeBelow == BLK_TILLED_DIRT)) && ((blockType == BLK_WATER) || (blockType == BLK_STATIONARY_WATER) || (blockType == BLK_ICE) || (! BLOCK_TRANSPARENCY.containsKey(blockType)))) {
                        // Covered grass, mycelium or tilled earth block, should be dirt
                        minecraftWorld.setMaterialAt(x, y, z - 1, Material.DIRT);
                        blockTypeBelow = BLK_DIRT;
                    }
                    if (supportSand && (blockType == BLK_SAND) && Constants.VERY_INSUBSTANTIAL_BLOCKS.contains(blockTypeBelow)) {
                        // All unsupported sand should be supported by sandstone
                        minecraftWorld.setMaterialAt(x, y, z, Material.SANDSTONE);
                        blockType = BLK_SANDSTONE;
                    }
                    if ((blockType == BLK_DEAD_SHRUBS) && (blockTypeBelow != BLK_SAND) && (blockTypeBelow != BLK_DIRT) && (blockTypeBelow != BLK_STAINED_CLAY) && (blockTypeBelow != BLK_HARDENED_CLAY)) {
                        // Dead shrubs can only exist on Sand
                        minecraftWorld.setMaterialAt(x, y, z, Material.AIR);
                        blockType = BLK_AIR;
                    } else if (((blockType == BLK_TALL_GRASS) || (blockType == BLK_ROSE) || (blockType == BLK_DANDELION)) && (blockTypeBelow != BLK_GRASS) && (blockTypeBelow != BLK_DIRT)) {
                        // Tall grass and flowers can only exist on Grass or Dirt blocks
                        minecraftWorld.setMaterialAt(x, y, z, Material.AIR);
                        blockType = BLK_AIR;
                    } else if (((blockType == BLK_RED_MUSHROOM) || (blockType == BLK_BROWN_MUSHROOM)) && (blockTypeBelow != BLK_GRASS) && (blockTypeBelow != BLK_DIRT) && (blockTypeBelow != BLK_MYCELIUM) && (blockTypeBelow != BLK_STONE)) {
                        // Mushrooms can only exist on Grass, Dirt, Mycelium or Stone (in caves) blocks
                        minecraftWorld.setMaterialAt(x, y, z, Material.AIR);
                        blockType = BLK_AIR;
                    } else if (dry && ((blockType == BLK_WATER) || (blockType == BLK_STATIONARY_WATER) || (blockType == BLK_SNOW) || (blockType == BLK_SNOW_BLOCK) || (blockType == BLK_LAVA) || (blockType == BLK_STATIONARY_LAVA) || (blockType == BLK_ICE))) {
                        minecraftWorld.setMaterialAt(x, y, z, Material.AIR);
                        blockType = BLK_AIR;
                    } else if ((blockType == BLK_SNOW) && ((blockTypeBelow == BLK_ICE) || (blockTypeBelow == BLK_SNOW) || (blockTypeBelow == BLK_AIR) || (blockTypeBelow == BLK_PACKED_ICE))) {
                        // Snow can't be on ice, or another snow block, or air
                        // (well it could be, but it makes no sense, would
                        // disappear when touched, and it makes this algorithm
                        // remove stacks of snow blocks correctly)
                        minecraftWorld.setMaterialAt(x, y, z, Material.AIR);
                        blockType = BLK_AIR;
                    } else if ((blockType == BLK_WHEAT) && (blockTypeBelow != BLK_TILLED_DIRT)) {
                        // Wheat can only exist on Tilled Dirt blocks
                        minecraftWorld.setMaterialAt(x, y, z, Material.AIR);
                        blockType = BLK_AIR;
                    }
                    blockTypeBelow = blockType;
                }
            }
            if (progressReceiver != null) {
                progressReceiver.setProgress((x - x1 + 1) / 512f);
            }
        }
    }
    
    protected void lightingPass(MinecraftWorld minecraftWorld, Point regionCoords, ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled {
        LightingCalculator lightingVolume = new LightingCalculator(minecraftWorld);
        
        // Calculate primary light
        int lightingLowMark = Integer.MAX_VALUE, lightingHighMark = Integer.MIN_VALUE;
        int lowestChunkX = (regionCoords.x << 5) - 1;
        int highestChunkX = (regionCoords.x << 5) + 32;
        int lowestChunkY = (regionCoords.y << 5) - 1;
        int highestChunkY = (regionCoords.y << 5) + 32;
        for (int chunkX = lowestChunkX; chunkX <= highestChunkX; chunkX++) {
            for (int chunkY = lowestChunkY; chunkY <= highestChunkY; chunkY++) {
                Chunk chunk = minecraftWorld.getChunk(chunkX, chunkY);
                if (chunk != null) {
                    int[] levels = lightingVolume.calculatePrimaryLight(chunk);
                    if (levels[0] < lightingLowMark) {
                        lightingLowMark = levels[0];
                    }
                    if (levels[1] > lightingHighMark) {
                        lightingHighMark = levels[1];
                    }
                }
            }
        }

        if (lightingLowMark != Integer.MAX_VALUE) {
            if (progressReceiver != null) {
                progressReceiver.setProgress(0.2f);
            }

            // Calculate secondary light
            lightingVolume.setDirtyArea(new Box((regionCoords.x << 9) - 16, ((regionCoords.x + 1) << 9) + 15, lightingLowMark, lightingHighMark, (regionCoords.y << 9) - 16, ((regionCoords.y + 1) << 9) + 15));
            while (lightingVolume.calculateSecondaryLight());
        }
        
        if (progressReceiver != null) {
            progressReceiver.setProgress(1.0f);
        }
    }
    
    protected final ExportResults exportRegion(MinecraftWorld minecraftWorld, Dimension dimension, Point regionCoords, boolean tileSelection, Map<Layer, LayerExporter<Layer>> exporters, ChunkFactory chunkFactory, ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled, IOException {
        int lowestTileX = (regionCoords.x << 2) - 1;
        int highestTileX = lowestTileX + 5;
        int lowestTileY = (regionCoords.y << 2) - 1;
        int highestTileY = lowestTileY + 5;
        Map<Point, Tile> tiles = new HashMap<Point, Tile>();
        for (int tileX = lowestTileX; tileX <= highestTileX; tileX++) {
            for (int tileY = lowestTileY; tileY <= highestTileY; tileY++) {
                Point tileCoords = new Point(tileX, tileY);
                Tile tile = dimension.getTile(tileCoords);
                if ((tile != null) && ((! tileSelection) || dimension.getWorld().getTilesToExport().contains(tileCoords))) {
                    tiles.put(tileCoords, tile);
                }
            }
        }
        
        Set<Layer> allLayers = new HashSet<Layer>();
        for (Tile tile: tiles.values()) {
            allLayers.addAll(tile.getLayers());
        }
        
        // Add layers that have been configured to be applied everywhere
        Set<Layer> minimumLayers = dimension.getMinimumLayers();
        allLayers.addAll(minimumLayers);
        
        List<Layer> secondaryPassLayers = new ArrayList<Layer>();
        for (Layer layer: allLayers) {
            LayerExporter exporter = layer.getExporter();
            if (exporter instanceof SecondPassLayerExporter) {
                secondaryPassLayers.add(layer);
            }
        }
        Collections.sort(secondaryPassLayers);

        long t1 = System.currentTimeMillis();
        // First pass. Create terrain and apply layers which don't need access
        // to neighbouring chunks
        ExportResults exportResults = firstPass(minecraftWorld, dimension, regionCoords, tiles, tileSelection, exporters, chunkFactory, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, 0.0f, 0.45f) : null);

        if (exportResults.chunksGenerated) {
            // Second pass. Apply layers which need information from or apply
            // changes to neighbouring chunks
            long t2 = System.currentTimeMillis();
            List<Fixup> myFixups = secondPass(secondaryPassLayers, minimumLayers, dimension, minecraftWorld, exporters, tiles.values(), regionCoords, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, 0.45f, 0.1f) : null);
            if ((myFixups != null) && (! myFixups.isEmpty())) {
                exportResults.fixups = myFixups;
            }

            // Post processing. Fix covered grass blocks, things like that
            long t3 = System.currentTimeMillis();
            postProcess(minecraftWorld, regionCoords, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, 0.55f, 0.1f) : null);

            // Third pass. Calculate lighting
            long t4 = System.currentTimeMillis();
            lightingPass(minecraftWorld, regionCoords, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, 0.65f, 0.35f) : null);
            long t5 = System.currentTimeMillis();
            if ("true".equalsIgnoreCase(System.getProperty("org.pepsoft.worldpainter.devMode"))) {
                String timingMessage = (t2 - t1) + ", " + (t3 - t2) + ", " + (t4 - t3) + ", " + (t5 - t4) + ", " + (t5 - t1);
//                System.out.println("Export timing: " + timingMessage);
                synchronized (TIMING_FILE_LOCK) {
                    PrintWriter out = new PrintWriter(new FileOutputStream("exporttimings.csv", true));
                    try {
                        out.println(timingMessage);
                    } finally {
                        out.close();
                    }
                }
            }
        }
        
        return exportResults;
    }
    
    protected final void logLayers(Dimension dimension, EventVO event, String prefix) {
        StringBuilder sb = new StringBuilder();
        for (Layer layer: dimension.getAllLayers(false)) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(layer.getName());
        }
        if (sb.length() > 0) {
            event.setAttribute(new AttributeKeyVO<String>(prefix + "layers"), sb.toString());
        }
    }
    
    private ChunkFactory.ChunkCreationResult getChunk(Dimension dimension, ChunkFactory chunkFactory, Map<Point, Tile> tiles, int chunkX, int chunkY, boolean tileSelection, Map<Layer, LayerExporter<Layer>> exporters) {
        final int tileX = chunkX >> 3;
        final int tileY = chunkY >> 3;
        final Point tileCoords = new Point(tileX, tileY);
        final boolean border = (dimension.getBorder() != null) && (dimension.getBorderSize() > 0);
        if (tileSelection) {
            // Tile selection. Don't export bedrock wall or border tiles
            if (tiles.containsKey(tileCoords) && (! dimension.getBitLayerValueAt(ReadOnly.INSTANCE, chunkX << 4, chunkY << 4))) {
                return chunkFactory.createChunk(chunkX, chunkY);
            } else {
                return null;
            }
        } else {
            if (dimension.getTile(tileCoords) != null) {
                if (! dimension.getBitLayerValueAt(ReadOnly.INSTANCE, chunkX << 4, chunkY << 4)) {
                    return chunkFactory.createChunk(chunkX, chunkY);
                } else {
                    return null;
                }
            } else {
                // Might be a border or bedrock wall chunk
                if (border && isBorderChunk(dimension, chunkX, chunkY)) {
                    return BorderChunkFactory.create(chunkX, chunkY, dimension, exporters);
                } else if (dimension.isBedrockWall()
                        && (border
                            ? (isBorderChunk(dimension, chunkX - 1, chunkY) || isBorderChunk(dimension, chunkX, chunkY - 1) || isBorderChunk(dimension, chunkX + 1, chunkY) || isBorderChunk(dimension, chunkX, chunkY + 1))
                            : (isWorldChunk(dimension, chunkX - 1, chunkY) || isWorldChunk(dimension, chunkX, chunkY - 1) || isWorldChunk(dimension, chunkX + 1, chunkY) || isWorldChunk(dimension, chunkX, chunkY + 1)))) {
                    // Bedrock wall is turned on and a neighbouring chunk is a
                    // border chunk (if there is a border), or a world chunk (if
                    // there is no border)
                    return BedrockWallChunk.create(chunkX, chunkY, dimension);
                } else {
                    // Outside known space
                    return null;
                }
            }
        }
    }

    private boolean isWorldChunk(Dimension dimension, int x, int y) {
        return dimension.getTile(x >> 3, y >> 3) != null;
    }
    
    private boolean isBorderChunk(Dimension dimension, int x, int y) {
        final int tileX = x >> 3, tileY = y >> 3;
        final int borderSize = dimension.getBorderSize();
        if ((dimension.getBorder() == null) || (borderSize == 0)) {
            // There is no border configured, so definitely no border chunk
            return false;
        } else if (dimension.getTile(tileX, tileY) != null) {
            // There is a tile here, so definitely no border chunk
            return false;
        } else {
            // Check whether there is a tile within a radius of *borderSize*,
            // in which case we are on a border tile
            for (int dx = -borderSize; dx <= borderSize; dx++) {
                for (int dy = -borderSize; dy <= borderSize; dy++) {
                    if (dimension.getTile(tileX + dx, tileY + dy) != null) {
                        // Tile found, we are a border chunk!
                        return true;
                    }
                }
            }
            // No tiles found within a radius of *borderSize*, we are no border
            // chunk
            return false;
        }
    }

    private ChunkFactory.Stats exportDimension(File worldDir, final Dimension dimension, final int version, ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled, IOException {
        if (progressReceiver != null) {
            progressReceiver.setMessage("exporting " + dimension.getName() + " dimension");
        }
        
        long start = System.currentTimeMillis();
        
        final File dimensionDir;
        switch (dimension.getDim()) {
            case org.pepsoft.worldpainter.Constants.DIM_NORMAL:
                dimensionDir = worldDir;
                break;
            case org.pepsoft.worldpainter.Constants.DIM_NETHER:
                dimensionDir = new File(worldDir, "DIM-1");
                break;
            case org.pepsoft.worldpainter.Constants.DIM_END:
                dimensionDir = new File(worldDir, "DIM1");
                break;
            default:
                throw new IllegalArgumentException("Dimension " + dimension.getDim() + " not supported");
        }
        File regionDir = new File(dimensionDir, "region");
        if (! regionDir.exists()) {
            if (! regionDir.mkdirs()) {
                throw new RuntimeException("Could not create directory " + regionDir);
            }
        }

        final ChunkFactory.Stats collectedStats = new ChunkFactory.Stats();
        boolean wasDirty = dimension.isDirty();
        dimension.rememberChanges();
        try {
        
            // Gather all layers used on the map
            final Map<Layer, LayerExporter<Layer>> exporters = new HashMap<Layer, LayerExporter<Layer>>();
            Set<Layer> allLayers = dimension.getAllLayers(false);
            allLayers.addAll(dimension.getMinimumLayers());
            // If there are combined layers, apply them and gather any newly
            // added layers, recursively
            boolean done;
            do {
                done = true;
                for (Layer layer: new HashSet<Layer>(allLayers)) {
                    if (layer instanceof CombinedLayer) {
                        // Apply the combined layer
                        Set<Layer> addedLayers = ((CombinedLayer) layer).apply(dimension);
                        // Remove the combined layer from the list
                        allLayers.remove(layer);
                        // Add any layers it might have added
                        allLayers.addAll(addedLayers);
                        // Signal that we have to go around at least once more,
                        // in case any of the newly added layers are themselves
                        // combined layers
                        done = false;
                    }
                }
            } while (! done);

            // Load all layer settings into the exporters
            for (Layer layer: allLayers) {
                @SuppressWarnings("unchecked")
                LayerExporter<Layer> exporter = (LayerExporter<Layer>) layer.getExporter();
                if (exporter != null) {
                    exporter.setSettings(dimension.getLayerSettings(layer));
                    exporters.put(layer, exporter);
                }
            }

            // Determine regions to export
            Set<Point> regions = new HashSet<Point>();
            final boolean tileSelection = selectedTiles != null;
            if (tileSelection) {
                // Sanity check
                assert selectedDimension == dimension.getDim();
                for (Point tile: selectedTiles) {
                    regions.add(new Point(tile.x >> 2, tile.y >> 2));
                }
            } else {
                for (Tile tile: dimension.getTiles()) {
                    // Also add regions for any bedrock wall and/or border
                    // tiles, if present
                    int r = ((dimension.getBorder() != null) ? dimension.getBorderSize() : 0) + (dimension.isBedrockWall() ? 1 : 0);
                    for (int dx = -r; dx <= r; dx++) {
                        for (int dy = -r; dy <= r; dy++) {
                            int regionX = (tile.getX() + dx) >> 2;
                            int regionZ = (tile.getY() + dy) >> 2;
                            regions.add(new Point(regionX, regionZ));
                        }
                    }
                }
            }

            final WorldPainterChunkFactory chunkFactory = new WorldPainterChunkFactory(dimension, exporters, world.getVersion(), world.getMaxHeight());

            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long memoryInUse = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            long maxMemoryAvailable = maxMemory - memoryInUse;
            int maxThreadsByMem = (int) (maxMemoryAvailable / 250000000L);
            int threads;
            if (System.getProperty("org.pepsoft.worldpainter.threads") != null) {
                threads = Math.max(Math.min(Integer.parseInt(System.getProperty("org.pepsoft.worldpainter.threads")), regions.size()), 1);
            } else {
                threads = Math.max(Math.min(Math.min(maxThreadsByMem, runtime.availableProcessors()), regions.size()), 1);
            }
            logger.info("Using " + threads + " thread(s) for export (cores: " + runtime.availableProcessors() + ", available memory: " + (maxMemoryAvailable / 1048576L) + " MB)");

            final List<List<Fixup>> fixups = new ArrayList<List<Fixup>>();
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            final ParallelProgressManager parallelProgressManager = (progressReceiver != null) ? new ParallelProgressManager(new SubProgressReceiver(progressReceiver, 0.0f, 0.9f), regions.size()) : null;
            try {
                // Export each individual region
                for (Point region: regions) {
                    final Point regionCoords = region;
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            ProgressReceiver progressReceiver = (parallelProgressManager != null) ? parallelProgressManager.createProgressReceiver() : null;
                            if (progressReceiver != null) {
                                try {
                                    progressReceiver.checkForCancellation();
                                } catch (ProgressReceiver.OperationCancelled e) {
                                    return;
                                }
                            }
                            try {
                                WorldRegion minecraftWorld = new WorldRegion(regionCoords.x, regionCoords.y, dimension.getMaxHeight(), version);
                                ExportResults exportResults = null;
                                try {
                                    exportResults = exportRegion(minecraftWorld, dimension, regionCoords, tileSelection, exporters, chunkFactory, progressReceiver);
                                    if (exportResults.chunksGenerated) {
                                        synchronized (collectedStats) {
                                            collectedStats.landArea += exportResults.stats.landArea;
                                            collectedStats.surfaceArea += exportResults.stats.surfaceArea;
                                            collectedStats.waterArea += exportResults.stats.waterArea;
                                        }
                                    }
                                    if ((exportResults.fixups != null) && (! exportResults.fixups.isEmpty())) {
                                        synchronized (fixups) {
                                            fixups.add(exportResults.fixups);
                                        }
                                    }
                                } finally {
                                    if ((exportResults != null) && exportResults.chunksGenerated) {
                                        minecraftWorld.save(dimensionDir);
                                    }
                                }
                            } catch (Throwable t) {
                                if (progressReceiver != null) {
                                    progressReceiver.exceptionThrown(t);
                                } else {
                                    logger.log(java.util.logging.Level.SEVERE, "Exception while exporting region", t);
                                }
                            }
                        }
                    });
                }
            } finally {
                executor.shutdown();
                try {
                    executor.awaitTermination(366, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread interrupted while waiting for all tasks to finish", e);
                }
            }

            if ((parallelProgressManager == null) || (! parallelProgressManager.isExceptionThrown())) {
                performFixups(worldDir, dimension, version, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, 0.9f, 0.1f) : null, fixups);
            }

            // Calculate total size of dimension
            for (Point region: regions) {
                File file = new File(dimensionDir, "region/r." + region.x + "." + region.y + ((version == SUPPORTED_VERSION_2) ? ".mca" : ".mcr"));
                collectedStats.size += file.length();
            }
            collectedStats.time = System.currentTimeMillis() - start;
        } finally {

            // Undo any changes we made (such as applying any combined layers)
            if (dimension.undoChanges()) {
                // TODO: some kind of cleverer undo mechanism (undo history
                // cloning?) so we don't mess up the user's redo history
                dimension.clearRedo();
                dimension.armSavePoint();
            }
            
            // If the dimension wasn't dirty make sure it still isn't
            dimension.setDirty(wasDirty);
        }

        return collectedStats;
    }

    protected void performFixups(final File worldDir, final Dimension dimension, final int version, final ProgressReceiver progressReceiver, final List<List<Fixup>> fixups) throws OperationCancelled {
        synchronized (fixups) {
            if (! fixups.isEmpty()) {
                if (progressReceiver != null) {
                    progressReceiver.setMessage("doing fixups for " + dimension.getName() + " dimension");
                }
                long start = System.currentTimeMillis();
                if (logger.isLoggable(java.util.logging.Level.FINE)) {
                    logger.fine(fixups.size() + " regions have fixups");
                }
                // Make sure to honour the read-only layer:
                MinecraftWorldImpl minecraftWorld = new MinecraftWorldImpl(worldDir, dimension, version, false, true, 256);
                try {
                    int count = 0, total = fixups.size();
                    for (List<Fixup> regionFixups: fixups) {
                        if (logger.isLoggable(java.util.logging.Level.FINE)) {
                            logger.fine("Performing " + regionFixups.size() + " fixups");
                        }
                        for (Fixup fixup: regionFixups) {
                            fixup.fixup(minecraftWorld, dimension);
                        }
                        count++;
                        if ((count % 96) == 0) {
                            if (logger.isLoggable(java.util.logging.Level.FINE)) {
                                logger.fine("Flushing region files (chunks in cache: " + minecraftWorld.getCacheSize() + ")");
                            }
                            minecraftWorld.flush();
                        }
                        if (progressReceiver != null) {
                            progressReceiver.setProgress((float) count / total);
                        }
                    }
                } finally {
                    minecraftWorld.flush();
                }
                if (logger.isLoggable(java.util.logging.Level.FINE)) {
                    logger.fine("Fixups took " + (System.currentTimeMillis() - start) + " ms");
                }
            } else if (progressReceiver != null) {
                progressReceiver.setProgress(1.0f);
            }
        }
    }

    private Chest createGoodiesChest() {
        List<InventoryItem> list = new ArrayList<InventoryItem>();
        list.add(new InventoryItem(ITM_DIAMOND_SWORD,    0,  1,  0));
        list.add(new InventoryItem(ITM_DIAMOND_SHOVEL,   0,  1,  1));
        list.add(new InventoryItem(ITM_DIAMOND_PICKAXE,  0,  1,  2));
        list.add(new InventoryItem(ITM_DIAMOND_AXE,      0,  1,  3));
        list.add(new InventoryItem(BLK_SAPLING,          0, 64,  4));
        list.add(new InventoryItem(BLK_SAPLING,          1, 64,  5));
        list.add(new InventoryItem(BLK_SAPLING,          2, 64,  6));
        list.add(new InventoryItem(BLK_BROWN_MUSHROOM,   0, 64,  7));
        list.add(new InventoryItem(BLK_RED_MUSHROOM,     0, 64,  8));
        list.add(new InventoryItem(ITM_BONE,             0, 64,  9));
        list.add(new InventoryItem(ITM_WATER_BUCKET,     0,  1, 10));
        list.add(new InventoryItem(ITM_WATER_BUCKET,     0,  1, 11));
        list.add(new InventoryItem(ITM_COAL,             0, 64, 12));
        list.add(new InventoryItem(ITM_IRON_INGOT,       0, 64, 13));
        list.add(new InventoryItem(BLK_CACTUS,           0, 64, 14));
        list.add(new InventoryItem(ITM_SUGAR_CANE,       0, 64, 15));
        list.add(new InventoryItem(BLK_TORCH,            0, 64, 16));
        list.add(new InventoryItem(ITM_BED,              0,  1, 17));
        list.add(new InventoryItem(BLK_OBSIDIAN,         0, 64, 18));
        list.add(new InventoryItem(ITM_FLINT_AND_STEEL,  0,  1, 19));
        list.add(new InventoryItem(BLK_WOOD,             0, 64, 20));
        list.add(new InventoryItem(BLK_CRAFTING_TABLE,   0,  1, 21));
        list.add(new InventoryItem(BLK_END_PORTAL_FRAME, 0, 12, 22));
        list.add(new InventoryItem(ITM_EYE_OF_ENDER,     0, 12, 23));
        Chest chest = new Chest();
        chest.setItems(list);
        return chest;
    }
    
    protected final World2 world;
    protected final int selectedDimension;
    protected final Set<Point> selectedTiles;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final boolean supportSand = ! "false".equalsIgnoreCase(System.getProperty("org.pepsoft.worldpainter.supportSand"));
    private static final Logger logger = Logger.getLogger(WorldExporter.class.getName());
    private static final Object TIMING_FILE_LOCK = new Object();
    
    public static class ExportResults {
        /**
         * Whether any chunks were actually generated for this region.
         */
        public boolean chunksGenerated;
        
        /**
         * Statistics for the generated chunks, if any
         */
        public final ChunkFactory.Stats stats = new ChunkFactory.Stats();
        
        /**
         * Fixups which have to be performed synchronously after all regions
         * have been generated
         */
        public List<Fixup> fixups;
    }
}
