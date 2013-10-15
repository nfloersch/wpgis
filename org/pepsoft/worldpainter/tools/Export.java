/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.util.FileUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.exporting.WorldExporter;
import org.pepsoft.worldpainter.util.MinecraftUtil;

/**
 *
 * @author pepijn
 */
public class Export {
    public static void main(String args[]) throws IOException, ClassNotFoundException, OperationCancelled {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.OFF);
        File worldFile = new File(args[0]);
        System.out.println("Loading " + worldFile);
        World2 world;
        ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(worldFile)));
        try {
            world = (World2) in.readObject();
        } finally {
            in.close();
        }

        for (int i = 0; i < Terrain.CUSTOM_TERRAIN_COUNT; i++) {
            MixedMaterial material = world.getMixedMaterial(i);
            Terrain.setCustomMaterial(i, material);
        }
        if (world.getVersion() == 0) {
            if (world.getMaxHeight() == Constants.DEFAULT_MAX_HEIGHT_2) {
                world.setVersion(Constants.SUPPORTED_VERSION_2);
            } else {
                world.setVersion(Constants.SUPPORTED_VERSION_1);
            }
        }
        
        File exportDir;
        if (args.length > 1) {
            exportDir = new File(args[1]);
        } else {
            File minecraftDir = MinecraftUtil.findMinecraftDir();
            exportDir = new File(minecraftDir, "saves");
        }
        System.out.println("Exporting to " + exportDir);
        System.out.println("+---------+---------+---------+---------+---------+");
        WorldExporter exporter = new WorldExporter(world);
        exporter.export(exportDir, world.getName(), exporter.selectBackupDir(new File(exportDir, FileUtils.sanitiseName(world.getName()))), new ProgressReceiver() {
            @Override
            public void setProgress(float progressFraction) throws OperationCancelled {
                int progress = (int) (progressFraction * 50);
                while (progress > previousProgress) {
                    System.out.print('.');
                    previousProgress++;
                }
            }

            @Override
            public void exceptionThrown(Throwable exception) {
                exception.printStackTrace();
                System.exit(1);
            }

            @Override public void done() {}
            @Override public void setMessage(String message) throws OperationCancelled {}
            @Override public void checkForCancellation() throws OperationCancelled {}
            
            private int previousProgress = -1;
        });
        System.out.println();
        System.out.println("World " + world.getName() + " exported successfully");
    }
}