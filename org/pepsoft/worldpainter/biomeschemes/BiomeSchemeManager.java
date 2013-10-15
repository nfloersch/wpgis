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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.pepsoft.util.Checksum;
import org.pepsoft.util.FileUtils;
import org.pepsoft.util.Version;
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
        
        String version;
        switch (biomeAlgorithm) {
            case BIOME_ALGORITHM_1_8_1:
                version = "Beta 1.8.1";
                break;
            case BIOME_ALGORITHM_1_9:
                version = "Beta 1.9 prerelease 3 to 6 or RC2";
                break;
            case BIOME_ALGORITHM_1_0_0:
                version = "1.0.0";
                break;
            case BIOME_ALGORITHM_1_1:
                version = "1.1";
                break;
            case BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT:
                version = "1.6.2 or 1.2.3 - 1.5.2";
                break;
            case BIOME_ALGORITHM_1_3_LARGE:
                version = "1.6.2 or 1.3.1 - 1.5.2";
                break;
            default:
                throw new IllegalArgumentException();
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
            File minecraftDir = MinecraftUtil.findMinecraftDir();
            if (minecraftJar != null) {
                Checksum hash = FileUtils.getMD5(minecraftJar);
                BiomeSchemeDescriptor descriptor = identify(hash, biomeAlgorithm);
                if (descriptor != null) {
                    config.setMinecraftJar(biomeAlgorithm, minecraftJar);
                    logger.info("Creating biome scheme " + version + " from " + minecraftJar.getAbsolutePath() + " stored in configuration");
                    return descriptor.instantiate(minecraftJar, minecraftDir, hash);
                } else {
                    config.setMinecraftJar(biomeAlgorithm, null);
                }
            }

            // Either no file was configured, it did not exist anymore, or
            // it was not a valid file. Try to automatically find the
            // most recent supported currently installed minecraft.jar file
            if (minecraftDir != null) {
                SortedMap<Version, BiomeJar> foundBiomeJars = new TreeMap<Version, BiomeJar>();
                scanDir(config, minecraftDir, foundBiomeJars, biomeAlgorithm);
                if (! foundBiomeJars.isEmpty()) {
                    BiomeJar biomeJar = foundBiomeJars.get(foundBiomeJars.lastKey());
                    minecraftJar = biomeJar.getFile();
                    config.setMinecraftJar(biomeAlgorithm, minecraftJar);
                    logger.info("Creating biome scheme " + version + " from " + minecraftJar.getAbsolutePath());
                    return biomeJar.getDescriptor().instantiate(minecraftJar, minecraftDir, biomeJar.getChecksum());
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
                    BiomeSchemeDescriptor descriptor = identify(hash, biomeAlgorithm);
                    if (descriptor != null) {
                        config.setMinecraftJar(biomeAlgorithm, minecraftJar);
                        logger.info("Creating biome scheme " + version + " from " + minecraftJar.getAbsolutePath() + " provided by user");
                        return descriptor.instantiate(minecraftJar, minecraftDir, hash);
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

    private static void scanDir(Configuration config, File dir, SortedMap<Version, BiomeJar> foundBiomeSchemes, int biomeScheme) throws IOException {
        File[] files = dir.listFiles(new java.io.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".jar");
            }
        });
        for (File file: files) {
            if (file.isDirectory()) {
                scanDir(config, file, foundBiomeSchemes, biomeScheme);
            } else {
                Checksum hash = FileUtils.getMD5(file);
                if (DESCRIPTORS.containsKey(hash)) {
                    for (BiomeSchemeDescriptor descriptor: DESCRIPTORS.get(hash)) {
                        if (descriptor.getBiomeScheme() == biomeScheme) {
                            foundBiomeSchemes.put(descriptor.getMinecraftVersion(), new BiomeJar(file, hash, descriptor));
                        }
                    }
                }
            }
        }
    }
    
    private static BiomeSchemeDescriptor identify(Checksum checksum, int desiredBiomeScheme) {
        if (DESCRIPTORS.containsKey(checksum)) {
            SortedMap<Version, BiomeSchemeDescriptor> matchingDescriptors = new TreeMap<Version, BiomeSchemeDescriptor>();
            for (BiomeSchemeDescriptor descriptor: DESCRIPTORS.get(checksum)) {
                if (descriptor.getBiomeScheme() == desiredBiomeScheme) {
                    matchingDescriptors.put(descriptor.getMinecraftVersion(), descriptor);
                }
            }
            return (! matchingDescriptors.isEmpty()) ? matchingDescriptors.get(matchingDescriptors.lastKey()) : null;
        } else {
            return null;
        }
    }
    
    private static final Map<Checksum, Set<BiomeSchemeDescriptor>> DESCRIPTORS = new HashMap<Checksum, Set<BiomeSchemeDescriptor>>();

    static {
        addDescriptor(new Checksum(new byte[] {(byte) -8, (byte) -59, (byte) -94, (byte) -52, (byte) -45, (byte) -68, (byte) -103, (byte) 103, (byte) -110, (byte) -69, (byte) -28, (byte) 54, (byte) -40, (byte) -52, (byte) 8, (byte) -68}), new BiomeSchemeDescriptor(new Version(0, 1, 8, 1), BIOME_ALGORITHM_1_8_1, Minecraft1_8_1BiomeScheme.class, false));

        addDescriptor(new Checksum(new byte[] {(byte) 51, (byte) 72, (byte) 39, (byte) -37, (byte) -23, (byte) 24, (byte) 58, (byte) -10, (byte) -42, (byte) 80, (byte) -77, (byte) -109, (byte) 33, (byte) -87, (byte) -98, (byte) 33}),    new BiomeSchemeDescriptor(new Version(0, 1, 9, 0, 3), BIOME_ALGORITHM_1_9, Minecraft1_9BiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) -54, (byte) -28, (byte) 31, (byte) 55, (byte) 70, (byte) -45, (byte) -60, (byte) -60, (byte) 64, (byte) -78, (byte) -42, (byte) 58, (byte) 64, (byte) 55, (byte) 112, (byte) -25}),    new BiomeSchemeDescriptor(new Version(0, 1, 9, 0, 4), BIOME_ALGORITHM_1_9, Minecraft1_9BiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) 98, (byte) 88, (byte) -60, (byte) -14, (byte) -109, (byte) -71, (byte) 57, (byte) 17, (byte) 126, (byte) -2, (byte) 100, (byte) 14, (byte) -38, (byte) 118, (byte) -36, (byte) -92}),  new BiomeSchemeDescriptor(new Version(0, 1, 9, 0, 5), BIOME_ALGORITHM_1_9, Minecraft1_9BiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) 36, (byte) 104, (byte) 32, (byte) 81, (byte) 84, (byte) 55, (byte) 74, (byte) -2, (byte) 95, (byte) -100, (byte) -86, (byte) -70, (byte) 47, (byte) -5, (byte) -11, (byte) -8}),       new BiomeSchemeDescriptor(new Version(0, 1, 9, 0, 6), BIOME_ALGORITHM_1_9, Minecraft1_9BiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) -67, (byte) 86, (byte) -99, (byte) 32, (byte) -35, (byte) 61, (byte) -40, (byte) -104, (byte) -1, (byte) 67, (byte) 113, (byte) -81, (byte) -101, (byte) -66, (byte) 20, (byte) -31}), new BiomeSchemeDescriptor(new Version(0, 1, 9, 1, 2), BIOME_ALGORITHM_1_9, Minecraft1_9BiomeScheme.class, false));
    
        addDescriptor(new Checksum(new byte[] {(byte) 56, (byte) 32, (byte) -46, (byte) 34, (byte) -71, (byte) 93, (byte) 11, (byte) -116, (byte) 82, (byte) 13, (byte) -107, (byte) -106, (byte) -89, (byte) 86, (byte) -90, (byte) -26}), new BiomeSchemeDescriptor(new Version(1, 0, 0), BIOME_ALGORITHM_1_0_0, Minecraft1_0BiomeScheme.class, false));
        
        addDescriptor(new Checksum(new byte[] {(byte) -23, (byte) 35, (byte) 2, (byte) -46, (byte) -84, (byte) -37, (byte) -89, (byte) -55, (byte) 126, (byte) 13, (byte) -115, (byte) -15, (byte) -31, (byte) 13, (byte) 32, (byte) 6}), new BiomeSchemeDescriptor(new Version(1, 1), BIOME_ALGORITHM_1_1, Minecraft1_1BiomeScheme.class, false));
        
        addDescriptor(new Checksum(new byte[] {(byte) 18, (byte) -10, (byte) -60, (byte) -79, (byte) -67, (byte) -52, (byte) 99, (byte) -16, (byte) 41, (byte) -29, (byte) -64, (byte) -120, (byte) -93, (byte) 100, (byte) -72, (byte) -28}),  new BiomeSchemeDescriptor(new Version(1, 2, 3), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false)); // guess
        addDescriptor(new Checksum(new byte[] {(byte) 37, (byte) 66, (byte) 62, (byte) -85, (byte) 109, (byte) -121, (byte) 7, (byte) -7, (byte) 108, (byte) -58, (byte) -83, (byte) -118, (byte) 33, (byte) -89, (byte) 37, (byte) 10}),       new BiomeSchemeDescriptor(new Version(1, 2, 4), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false)); // guess
        addDescriptor(new Checksum(new byte[] {(byte) -114, (byte) -121, (byte) 120, (byte) 7, (byte) -118, (byte) 23, (byte) 90, (byte) 51, (byte) 96, (byte) 58, (byte) 88, (byte) 82, (byte) 87, (byte) -14, (byte) -123, (byte) 99}),       new BiomeSchemeDescriptor(new Version(1, 2, 5), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false)); // guess
        addDescriptor(new Checksum(new byte[] {(byte) 38, (byte) 108, (byte) -53, (byte) -55, (byte) 121, (byte) -118, (byte) -3, (byte) 46, (byte) -83, (byte) -13, (byte) -42, (byte) -64, (byte) 27, (byte) 76, (byte) 86, (byte) 42}),      new BiomeSchemeDescriptor(new Version(1, 3, 1), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false)); // guess
        addDescriptor(new Checksum(new byte[] {(byte) -106, (byte) -106, (byte) -103, (byte) -15, (byte) 62, (byte) 91, (byte) -66, (byte) 127, (byte) 18, (byte) -28, (byte) 10, (byte) -60, (byte) -13, (byte) 43, (byte) 125, (byte) -102}), new BiomeSchemeDescriptor(new Version(1, 3, 2), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) 119, (byte) 17, (byte) 117, (byte) -64, (byte) 23, (byte) 120, (byte) -22, (byte) 103, (byte) 57, (byte) 91, (byte) -58, (byte) -111, (byte) -102, (byte) 90, (byte) -99, (byte) -59}),   new BiomeSchemeDescriptor(new Version(1, 4, 2), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) -114, (byte) -128, (byte) -5, (byte) 1, (byte) -77, (byte) 33, (byte) -58, (byte) -77, (byte) -57, (byte) -17, (byte) -54, (byte) 57, (byte) 122, (byte) 62, (byte) -22, (byte) 53}),     new BiomeSchemeDescriptor(new Version(1, 4, 7), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) 92, (byte) 18, (byte) 25, (byte) -40, (byte) 105, (byte) -72, (byte) 125, (byte) 35, (byte) 61, (byte) -29, (byte) 3, (byte) 54, (byte) -120, (byte) -20, (byte) 117, (byte) 103}),       new BiomeSchemeDescriptor(new Version(1, 5, 1), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) 104, (byte) -105, (byte) -61, (byte) 40, (byte) 127, (byte) -71, (byte) 113, (byte) -55, (byte) -13, (byte) 98, (byte) -21, (byte) 58, (byte) -78, (byte) 15, (byte) 93, (byte) -35}),    new BiomeSchemeDescriptor(new Version(1, 5, 2), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_2BiomeScheme.class, false));

        addDescriptor(new Checksum(new byte[] {(byte) -106, (byte) -106, (byte) -103, (byte) -15, (byte) 62, (byte) 91, (byte) -66, (byte) 127, (byte) 18, (byte) -28, (byte) 10, (byte) -60, (byte) -13, (byte) 43, (byte) 125, (byte) -102}), new BiomeSchemeDescriptor(new Version(1, 3, 2), BIOME_ALGORITHM_1_3_LARGE, Minecraft1_3LargeBiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) 119, (byte) 17, (byte) 117, (byte) -64, (byte) 23, (byte) 120, (byte) -22, (byte) 103, (byte) 57, (byte) 91, (byte) -58, (byte) -111, (byte) -102, (byte) 90, (byte) -99, (byte) -59}),   new BiomeSchemeDescriptor(new Version(1, 4, 2), BIOME_ALGORITHM_1_3_LARGE, Minecraft1_3LargeBiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) -114, (byte) -128, (byte) -5, (byte) 1, (byte) -77, (byte) 33, (byte) -58, (byte) -77, (byte) -57, (byte) -17, (byte) -54, (byte) 57, (byte) 122, (byte) 62, (byte) -22, (byte) 53}),     new BiomeSchemeDescriptor(new Version(1, 4, 7), BIOME_ALGORITHM_1_3_LARGE, Minecraft1_3LargeBiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) 92, (byte) 18, (byte) 25, (byte) -40, (byte) 105, (byte) -72, (byte) 125, (byte) 35, (byte) 61, (byte) -29, (byte) 3, (byte) 54, (byte) -120, (byte) -20, (byte) 117, (byte) 103}),       new BiomeSchemeDescriptor(new Version(1, 5, 1), BIOME_ALGORITHM_1_3_LARGE, Minecraft1_3LargeBiomeScheme.class, false));
        addDescriptor(new Checksum(new byte[] {(byte) 104, (byte) -105, (byte) -61, (byte) 40, (byte) 127, (byte) -71, (byte) 113, (byte) -55, (byte) -13, (byte) 98, (byte) -21, (byte) 58, (byte) -78, (byte) 15, (byte) 93, (byte) -35}),    new BiomeSchemeDescriptor(new Version(1, 5, 2), BIOME_ALGORITHM_1_3_LARGE, Minecraft1_3LargeBiomeScheme.class, false));

        addDescriptor(new Checksum(new byte[] {(byte) 29, (byte) 67, (byte) -51, (byte) -70, (byte) -117, (byte) -105, (byte) 82, (byte) -41, (byte) -11, (byte) 87, (byte) -85, (byte) 125, (byte) 62, (byte) 54, (byte) 89, (byte) 100}),     new BiomeSchemeDescriptor(new Version(1, 6, 2), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_6BiomeScheme.class, true));

        addDescriptor(new Checksum(new byte[] {(byte) 29, (byte) 67, (byte) -51, (byte) -70, (byte) -117, (byte) -105, (byte) 82, (byte) -41, (byte) -11, (byte) 87, (byte) -85, (byte) 125, (byte) 62, (byte) 54, (byte) 89, (byte) 100}),     new BiomeSchemeDescriptor(new Version(1, 6, 2), BIOME_ALGORITHM_1_3_LARGE, Minecraft1_6LargeBiomeScheme.class, true));
    }
    
    private static void addDescriptor(Checksum checksum, BiomeSchemeDescriptor descriptor) {
        Set<BiomeSchemeDescriptor> descriptors = DESCRIPTORS.get(checksum);
        if (descriptors == null) {
            descriptors = new HashSet<BiomeSchemeDescriptor>();
            DESCRIPTORS.put(checksum, descriptors);
        }
        descriptors.add(descriptor);
    }
    
    private static final Logger logger = Logger.getLogger(BiomeSchemeManager.class.getName());
    
    public static class BiomeSchemeDescriptor {
        BiomeSchemeDescriptor(Version minecraftVersion, int biomeScheme, Class<? extends BiomeScheme> _class, boolean addLibraries) {
            this.minecraftVersion = minecraftVersion;
            this.biomeScheme = biomeScheme;
            this._class = _class;
            this.addLibraries = addLibraries;
            try {
                constructor = addLibraries
                    ? _class.getConstructor(File.class, File.class, Checksum.class)
                    : _class.getConstructor(File.class, Checksum.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public Version getMinecraftVersion() {
            return minecraftVersion;
        }


        public int getBiomeScheme() {
            return biomeScheme;
        }

        public Class<? extends BiomeScheme> getClass_() {
            return _class;
        }

        public boolean isAddLibraries() {
            return addLibraries;
        }

        public BiomeScheme instantiate(File jarFile, File minecraftDir, Checksum checksum) {
            try {
                if (addLibraries) {
                    File libDir = new File(minecraftDir, "libraries");
                    return constructor.newInstance(jarFile, libDir, checksum);
                } else {
                    return constructor.newInstance(jarFile, checksum);
                }
            } catch (InstantiationException e) {
                throw new RuntimeException("Could not instantiate biome scheme", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Access denied while instantiating biome scheme", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Exception thrown while instantiating biome scheme", e);
            }
        }
        
        private final Version minecraftVersion;
        private final int biomeScheme;
        private final Class<? extends BiomeScheme> _class;
        private final Constructor<? extends BiomeScheme> constructor;
        private final boolean addLibraries;
    }
    
    public static class BiomeJar {
        public BiomeJar(File file, Checksum checksum, BiomeSchemeDescriptor descriptor) {
            this.file = file;
            this.checksum = checksum;
            this.descriptor = descriptor;
        }

        public File getFile() {
            return file;
        }

        public Checksum getChecksum() {
            return checksum;
        }

        public BiomeSchemeDescriptor getDescriptor() {
            return descriptor;
        }
        
        private final File file;
        private final Checksum checksum;
        private final BiomeSchemeDescriptor descriptor;
    }
}