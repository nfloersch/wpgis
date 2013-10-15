/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.biomeschemes;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.pepsoft.util.Checksum;
import org.pepsoft.util.FileUtils;
import org.pepsoft.worldpainter.BiomeScheme;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.Dimension;
import static org.pepsoft.worldpainter.World2.*;
import org.pepsoft.worldpainter.util.MinecraftUtil;

/**
 *
 * @author pepijn
 */
public class BiomeSchemeManager {
    public static BiomeScheme getBiomeScheme(int biomeAlgorithm, Component parent) {
        return getBiomeScheme(null, biomeAlgorithm, parent, true);
    }
    
    public static BiomeScheme getBiomeScheme(int biomeAlgorithm, Component parent, boolean askUser) {
        return getBiomeScheme(null, biomeAlgorithm, parent, askUser);
    }
    
    public static BiomeScheme getBiomeScheme(Dimension dimension, int biomeAlgorithm, Component parent) {
        return getBiomeScheme(dimension, biomeAlgorithm, parent, true);
    }
    
    public static BiomeScheme getBiomeScheme(Dimension dimension, int biomeAlgorithm, Component parent, boolean askUser) {
        if (biomeAlgorithm == BIOME_ALGORITHM_1_7_3) {
            logger.info("Creating biome scheme 1.7.3");
            return new Minecraft1_7_3BiomeScheme();
        } else if (biomeAlgorithm == BIOME_ALGORITHM_AUTO_BIOMES) {
            logger.info("Creating automatic biome scheme");
            return new AutoBiomeScheme(dimension);
        }
        
        Class<? extends BiomeScheme> biomeSchemeType;
        String version;
        switch (biomeAlgorithm) {
            case BIOME_ALGORITHM_1_8_1:
                biomeSchemeType = Minecraft1_8_1BiomeScheme.class;
                version = "Beta 1.8.1";
                break;
            case BIOME_ALGORITHM_1_9:
                biomeSchemeType = Minecraft1_9BiomeScheme.class;
                version = "Beta 1.9 prerelease 3 to 6 or RC2";
                break;
            case BIOME_ALGORITHM_1_0_0:
                biomeSchemeType = Minecraft1_0BiomeScheme.class;
                version = "1.0.0";
                break;
            case BIOME_ALGORITHM_1_1:
                biomeSchemeType = Minecraft1_1BiomeScheme.class;
                version = "1.1";
                break;
            case BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT:
                biomeSchemeType = Minecraft1_2BiomeScheme.class;
                version = "1.5.2 or 1.2.3 - 1.5.1";
                break;
            case BIOME_ALGORITHM_1_3_LARGE:
                biomeSchemeType = Minecraft1_3LargeBiomeScheme.class;
                version = "1.5.2 or 1.3.1 - 1.5.1";
                break;
            default:
                throw new IllegalArgumentException();
        }
        Method isCompatibleMethod;
        Constructor<? extends BiomeScheme> constructor;
        try {
            isCompatibleMethod = biomeSchemeType.getMethod("isCompatible", Checksum.class);
            constructor = biomeSchemeType.getConstructor(File.class, Checksum.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        Configuration config = Configuration.getInstance();
        File minecraftJar = config.getMinecraftJar(biomeAlgorithm);

        // If a file is configured, check that it still exists
        if ((minecraftJar != null) && (! minecraftJar.isFile())) {
            minecraftJar = null;
            config.setMinecraftJar(biomeAlgorithm, null);
        }

        try {
            // If it still exists, check that it is still a valid Minecraft
            // jar file of the correct version
            if (minecraftJar != null) {
                Checksum hash = FileUtils.getMD5(minecraftJar);
                if ((Boolean) isCompatibleMethod.invoke(null, hash) == true) {
                    config.setMinecraftJar(biomeAlgorithm, minecraftJar);
                    logger.info("Creating biome scheme " + version + " from " + minecraftJar.getAbsolutePath() + " stored in configuration");
                    return constructor.newInstance(minecraftJar, hash);
                } else {
                    config.setMinecraftJar(biomeAlgorithm, null);
                }
            }

            // Either no file was configured, it did not exist anymore, or
            // it was not a valid file. Try to automatically find the
            // currently installed minecraft.jar file
            File minecraftDir = MinecraftUtil.findMinecraftDir();
            if (minecraftDir != null) {
                File candidate = new File(minecraftDir, "bin/minecraft.jar");
                // Check whether this is the same file as we already checked
                // above, no use in checking again
                if (candidate.isFile() && (! candidate.equals(minecraftJar))) {
                    minecraftJar = candidate;
                    Checksum hash = FileUtils.getMD5(minecraftJar);
                    if ((Boolean) isCompatibleMethod.invoke(null, hash) == true) {
                        config.setMinecraftJar(biomeAlgorithm, minecraftJar);
                        logger.info("Creating biome scheme " + version + " from " + minecraftJar.getAbsolutePath() + " found in .minecraft/bin");
                        return constructor.newInstance(minecraftJar, hash);
                    }
                }
            }

            // We could not automatically find a compatible file, so let the
            // user select it, if allowed
            if (! askUser) {
                logger.info("Could not find compatible jar for biome scheme " + version + " and not allowed to ask user");
                return null;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
                }

                @Override
                public String getDescription() {
                    return "Java archives (*.jar)";
                }
            });
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Select Minecraft " + version + " minecraft.jar");
            if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                minecraftJar = fileChooser.getSelectedFile();
                if (! minecraftJar.isFile()) {
                    logger.info("Could not find compatible jar for biome scheme " + version + " and user selected a directory or a non existant file");
                    JOptionPane.showMessageDialog(parent, "The selected file is a directory, or is non existant", "No File Selected", JOptionPane.ERROR_MESSAGE);
                } else {
                    Checksum hash = FileUtils.getMD5(minecraftJar);
                    if ((Boolean) isCompatibleMethod.invoke(null, hash) == true) {
                        config.setMinecraftJar(biomeAlgorithm, minecraftJar);
                        logger.info("Creating biome scheme " + version + " from " + minecraftJar.getAbsolutePath() + " provided by user");
                        return constructor.newInstance(minecraftJar, hash);
                    } else {
                        logger.info("Could not find compatible jar for biome scheme " + version + " and user provided incompatible file " + minecraftJar.getAbsolutePath());
                        JOptionPane.showMessageDialog(parent, "The selected file is not an original Minecraft " + version + " minecraft.jar!", "Invalid File", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                logger.info("Could not find compatible jar for biome scheme " + version + " and user cancelled file selection dialog");
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("I/O error while reading Minecraft jar", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException while constructing biome scheme", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("InvocationTargetException while constructing biome scheme", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("InstantiationException while constructing biome scheme", e);
        }
    }
    
    public static BufferedImage createImage(BiomeScheme biomeScheme, int biome, ColourScheme colourScheme) {
        int backgroundColour = biomeScheme.getColour(biome, colourScheme);
        boolean[][] pattern = biomeScheme.getPattern(biome);
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if ((pattern != null) && pattern[x][y]) {
                    image.setRGB(x, y, 0);
                } else {
                    image.setRGB(x, y, backgroundColour);
                }
            }
        }
        return image;
    }
    
    private static final Logger logger = Logger.getLogger(BiomeSchemeManager.class.getName());
}