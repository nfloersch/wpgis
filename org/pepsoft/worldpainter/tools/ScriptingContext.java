/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import static org.pepsoft.minecraft.Constants.*;
import org.pepsoft.util.FileUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.HeightMapTileFactory;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.TileFactoryFactory;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.exporting.WorldExporter;
import org.pepsoft.worldpainter.importing.HeightMapImporter;
import org.pepsoft.worldpainter.themes.Theme;

/**
 *
 * @author SchmitzP
 */
public class ScriptingContext {
    /**
     * Load a WorldPainter .world file from the file system.
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public World2 loadWorld(String filename) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));
        try {
            return (World2) in.readObject();
        } finally {
            in.close();
        }
    }
    
    /**
     * Import a height map as a new WorldPainter world.
     * 
     * @param args
     * @return 
     */
    public World2 importHeightMap(HeightMapImportArgs args) throws IOException {
        File file = new File(args.file);
        BufferedImage image = ImageIO.read(file);
        args.importer.setImage(image);
        HeightMapTileFactory tileFactory = TileFactoryFactory.createNoiseTileFactory(new Random().nextLong(), Terrain.GRASS, DEFAULT_MAX_HEIGHT_2, 58, 62, false, true, 20, 1.0);
        Theme defaults = Configuration.getInstance().getHeightMapDefaultTheme();
        if (defaults != null) {
            tileFactory.setTheme(defaults);
        }
        args.importer.setTileFactory(tileFactory);
        String name = file.getName();
        int p = name.lastIndexOf('.');
        if (p != -1) {
            name = name.substring(0, p);
        }
        args.importer.setName(name);
        try {
            return args.importer.doImport(null);
        } catch (ProgressReceiver.OperationCancelled e) {
            // Can never happen since we don't pass a progress receiver in
            throw new InternalError();
        }
    }
    
    /**
     * Export a WorldPainter world as a new Minecraft map. If there is already a
     * map with the same name in the specified directory, it is moved to the
     * backups directory.
     * 
     * @param world
     * @param directory
     * @throws IOException 
     */
    public void exportWorld(World2 world, String directory) throws IOException {
        // Set the file format if it was not set yet (because this world was
        // not exported before)
        if (world.getVersion() == 0) {
            world.setVersion((world.getMaxHeight() == DEFAULT_MAX_HEIGHT_2) ? SUPPORTED_VERSION_2 : SUPPORTED_VERSION_1);
        }

        // Load any custom materials defined in the world
        for (int i = 0; i < Terrain.CUSTOM_TERRAIN_COUNT; i++) {
            MixedMaterial material = world.getMixedMaterial(i);
            Terrain.setCustomMaterial(i, material);
        }
        
        // Select and create (if necessary) the backups directory
        File baseDir = new File(directory);
        if (! baseDir.isDirectory()) {
            throw new IllegalArgumentException("Base directory " + directory + " does not exist or is not a directory");
        }
        File worldDir = new File(baseDir, FileUtils.sanitiseName(world.getName()));
        WorldExporter exporter = new WorldExporter(world);
        File backupDir = exporter.selectBackupDir(worldDir);
        
        // Export the world
        try {
            exporter.export(baseDir, world.getName(), backupDir, null);
        } catch (ProgressReceiver.OperationCancelled e) {
            // Can never happen since we don't pass a progress receiver in
            throw new InternalError();
        }
    }
    
    public void mergeWorld(World2 world, String levelDatFile, MergeType type) {
        // Set the file format if it was not set yet (because this world was
        // not exported before)
        if (world.getVersion() == 0) {
            world.setVersion((world.getMaxHeight() == DEFAULT_MAX_HEIGHT_2) ? SUPPORTED_VERSION_2 : SUPPORTED_VERSION_1);
        }

        // Load any custom materials defined in the world
        for (int i = 0; i < Terrain.CUSTOM_TERRAIN_COUNT; i++) {
            MixedMaterial material = world.getMixedMaterial(i);
            Terrain.setCustomMaterial(i, material);
        }
        
    }
    
    public HeightMapImportArgs getHeightMapImportArgsBuilder() {
        return new HeightMapImportArgs();
    }
    
    public enum MergeType {MERGE_CHUNKS, BIOMES_ONLY, REPLACE_CHUNKS}
    
    public static class HeightMapImportArgs {
        public HeightMapImportArgs file(String file) {
            this.file = file;
            return this;
        }
        
        public HeightMapImportArgs scale(int scale) {
            importer.setScale(scale);
            return this;
        }
        
        public HeightMapImportArgs offset(int x, int y) {
            importer.setOffsetX(x);
            importer.setOffsetY(y);
            return this;
        }
        
        public HeightMapImportArgs mapping(int imageLow, int worldLow, int imageHigh, int worldHigh) {
            importer.setImageLowLevel(imageLow);
            importer.setWorldLowLevel(worldLow);
            importer.setImageHighLevel(imageHigh);
            importer.setWorldHighLevel(worldHigh);
            return this;
        }
        
        String file;
        HeightMapImporter importer = new HeightMapImporter();
    }
}