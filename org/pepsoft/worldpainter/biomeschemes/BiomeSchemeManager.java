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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    
    public static BiomeScheme getBiomeScheme(final Dimension dimension, final int biomeAlgorithm, final Component parent, final boolean askUser) {
        if (biomeAlgorithm == BIOME_ALGORITHM_1_7_3) {
            logger.info("Creating biome scheme 1.7.3");
            return new Minecraft1_7_3BiomeScheme();
        } else if (biomeAlgorithm == BIOME_ALGORITHM_AUTO_BIOMES) {
            logger.info("Creating automatic biome scheme");
            return new AutoBiomeScheme(dimension);
        }
        
        synchronized (initialisationLock) {
            if (! initialised) {
                initialise();
            }
        }
        
        if (BIOME_SCHEMES.containsKey(biomeAlgorithm)) {
            // We already previously found and initialised a biome scheme for
            // this algorithm, so reuse it. Note that this could be a problem
            // if two threads try to use it for different Minecraft seeds, but
            // that should never happen the way WorldPainter is currently set up
            return BIOME_SCHEMES.get(biomeAlgorithm);
        } else {
            final String version;
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
                    version = "1.6.4 or 1.2.3 - 1.6.2";
                    break;
                case BIOME_ALGORITHM_1_3_LARGE:
                    version = "1.6.4 or 1.3.1 - 1.6.2";
                    break;
                case BIOME_ALGORITHM_1_7_DEFAULT:
                    version = "1.8.1 or 1.7.2 - 1.8";
                    break;
                case BIOME_ALGORITHM_1_7_LARGE:
                    version = "1.8.1 or 1.7.2 - 1.8";
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            SortedMap<Version, BiomeJar> biomeJars = BIOME_JARS.get(biomeAlgorithm);
            if ((biomeJars != null) && (! biomeJars.isEmpty())) {
                // We have a jar for this biome scheme
                final BiomeJar biomeJar = biomeJars.get(biomeJars.lastKey());
                logger.info("Creating biome scheme " + version + " from " + biomeJar.file.getAbsolutePath());
                BiomeScheme biomeScheme = biomeJar.descriptor.instantiate(biomeJar.file, minecraftDir, biomeJar.checksum);
                BIOME_SCHEMES.put(biomeAlgorithm, biomeScheme);
                return biomeScheme;
            } else if (askUser) {
                // We don't have a jar for this biome scheme, but we're allowed to
                // ask the user for it, so do so
                final JFileChooser fileChooser = new JFileChooser();
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
                    final File minecraftJar = fileChooser.getSelectedFile();
                    if (! minecraftJar.isFile()) {
                        logger.info("Could not find compatible jar for biome scheme " + version + " and user selected a directory or a non existant file");
                        JOptionPane.showMessageDialog(parent, "The selected file is a directory, or is non existant", "No File Selected", JOptionPane.ERROR_MESSAGE);
                    } else {
                        try {
                            final Checksum hash = FileUtils.getMD5(minecraftJar);
                            final BiomeSchemeDescriptor descriptor = identify(hash, biomeAlgorithm);
                            if (descriptor != null) {
                                biomeJars = new TreeMap<Version, BiomeJar>();
                                biomeJars.put(descriptor.minecraftVersion, new BiomeJar(minecraftJar, hash, descriptor));
                                BIOME_JARS.put(biomeAlgorithm, biomeJars);
                                final Configuration config = Configuration.getInstance();
                                config.setMinecraftJar(biomeAlgorithm, minecraftJar);
                                logger.info("Creating biome scheme " + version + " from " + minecraftJar.getAbsolutePath());
                                BiomeScheme biomeScheme = descriptor.instantiate(minecraftJar, minecraftDir, hash);
                                BIOME_SCHEMES.put(biomeAlgorithm, biomeScheme);
                                return biomeScheme;
                            } else {
                                logger.info("Could not find compatible jar for biome scheme " + version + " and user provided incompatible file " + minecraftJar.getAbsolutePath());
                                JOptionPane.showMessageDialog(parent, "The selected file is not an original Minecraft " + version + " minecraft.jar!", "Invalid File", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("I/O error reading Minecraft jar " + minecraftJar.getAbsolutePath(), e);
                        }
                    }
                } else {
                    logger.info("Could not find compatible jar for biome scheme " + version + " and user cancelled file selection dialog");
                }
                return null;
            } else {
                // We don't have a jar for this biome scheme, and we're not allowed
                // to ask the user for it, so give up
                logger.info("Could not find compatible jar for biome scheme " + version + " and not allowed to ask user");
                return null;
            }
        }
    }
    
    /**
     * Get all found Minecraft jars which are supported by WorldPainter for
     * calculating biomes.
     * 
     * @return All found Minecraft jars which are supported by WorldPainter for
     *     calculating biomes.
     */
    public static SortedMap<Version, File> getSupportedMinecraftJars() {
        synchronized (initialisationLock) {
            if (! initialised) {
                initialise();
            }
        }

        SortedMap<Version, File> files = new TreeMap<Version, File>();
        for (Map.Entry<Integer, SortedMap<Version, BiomeJar>> entry: BIOME_JARS.entrySet()) {
            for (BiomeJar jar: entry.getValue().values()) {
                files.put(jar.descriptor.minecraftVersion, jar.file);
            }
        }
        return files;
    }
    
    /**
     * Get all found Minecraft jars.
     * 
     * @return All found Minecraft jars.
     */
    public static SortedMap<Version, File> getAllMinecraftJars() {
        synchronized (initialisationLock) {
            if (! initialised) {
                initialise();
            }
        }

        return Collections.unmodifiableSortedMap(ALL_JARS);
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
    
    /**
     * Starts background initialisation of the biome scheme manager, so that
     * subsequent invocations of the <code>getBiomeScheme</code> methods won't
     * have to wait for initialisation.
     */
    public static void initialiseInBackground() {
        synchronized (initialisationLock) {
            if (initialised || initialising) {
                return;
            }
            initialising = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doInitialisation();
                }
            }, "Biome Scheme Manager Initialiser").start();
        }
    }

    private static void scanDir(File dir) throws IOException {
        File[] files = dir.listFiles(new java.io.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".jar");
            }
        });
        // files can only be null if dir does not exist or is not a directory,
        // which really should not be possible, but we've had reports from the
        // wild that it does, so check for it
        if (files != null) {
            for (File file: files) {
                if (file.isDirectory()) {
                    scanDir(file);
                } else {
                    Checksum hash = FileUtils.getMD5(file);
                    if (DESCRIPTORS.containsKey(hash)) {
                        for (BiomeSchemeDescriptor descriptor: DESCRIPTORS.get(hash)) {
                            SortedMap<Version, BiomeJar> jars = BIOME_JARS.get(descriptor.biomeScheme);
                            if (jars == null) {
                                jars = new TreeMap<Version, BiomeJar>();
                                BIOME_JARS.put(descriptor.biomeScheme, jars);
                            }
                            jars.put(descriptor.minecraftVersion, new BiomeJar(file, hash, descriptor));
                            // Also store it as a resources jar
                            ALL_JARS.put(descriptor.minecraftVersion, file);
                        }
                    } else {
                        // It's not a supported jar, but see if the filename is
                        // just a version number so that we can assume it's a
                        // Minecraft jar we can at least use for loading
                        // resources
                        try {
                            Version version = Version.parse(file.getName().substring(0, file.getName().length() - 4));
                            ALL_JARS.put(version, file);
                        } catch (NumberFormatException e) {
                            // Skip silently
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
                if (descriptor.biomeScheme == desiredBiomeScheme) {
                    matchingDescriptors.put(descriptor.minecraftVersion, descriptor);
                }
            }
            return (! matchingDescriptors.isEmpty()) ? matchingDescriptors.get(matchingDescriptors.lastKey()) : null;
        } else {
            return null;
        }
    }

    private static void initialise() {
        synchronized (initialisationLock) {
            if (initialised) {
                return;
            } else if (initialising) {
                // Another thread is initialising us; wait for it to finish
                while (initialising) {
                    try {
                        initialisationLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Thread interrupted while waiting for biome scheme manager initialisation", e);
                    }
                }
                return;
            } else {
                doInitialisation();
            }
        }
    }
    
    private static void doInitialisation() {
        synchronized (initialisationLock) {
            try {
                // Scan the Minecraft directory for supported jars
                minecraftDir = MinecraftUtil.findMinecraftDir();
                if (minecraftDir != null) {
                    scanDir(minecraftDir);
                }

                // Collect the names of the files we alread looked at so we can skip
                // them below
                Set<File> processedFiles = new HashSet<File>();
                for (Map.Entry<Integer, SortedMap<Version, BiomeJar>> entry: BIOME_JARS.entrySet()) {
                    for (BiomeJar biomeJar: entry.getValue().values()) {
                        processedFiles.add(biomeJar.file);
                    }
                }

                // Check the jars stored in the configuration (if we haven't
                // encountered them above)
                Configuration config = Configuration.getInstance();
                Map<Integer, File> minecraftJars = config.getMinecraftJars();
                for (Iterator<Map.Entry<Integer, File>> i = minecraftJars.entrySet().iterator(); i.hasNext(); ) {
                    Map.Entry<Integer, File> entry = i.next();
                    File file = entry.getValue();
                    if (processedFiles.contains(file)) {
                        continue;
                    } else if ((! file.isFile()) || (! file.canRead())) {
                        // The file is no longer there, or it's not accessible;
                        // remove it from the configuration
                        i.remove();
                        continue;
                    }
                    Checksum checksum = FileUtils.getMD5(file);
                    if (DESCRIPTORS.containsKey(checksum)) {
                        for (BiomeSchemeDescriptor descriptor: DESCRIPTORS.get(checksum)) {
                            SortedMap<Version, BiomeJar> jars = BIOME_JARS.get(descriptor.biomeScheme);
                            if (jars == null) {
                                jars = new TreeMap<Version, BiomeJar>();
                                BIOME_JARS.put(descriptor.biomeScheme, jars);
                            }
                            jars.put(descriptor.minecraftVersion, new BiomeJar(file, checksum, descriptor));
                            // Also store it as a resources jar
                            ALL_JARS.put(descriptor.minecraftVersion, file);
                        }
                    } else {
                        // It's not a supported jar, but see if the filename is
                        // just a version number so that we can assume it's a
                        // Minecraft jar we can at least use for loading
                        // resources
                        try {
                            Version version = Version.parse(file.getName().substring(0, file.getName().length() - 4));
                            ALL_JARS.put(version, file);
                        } catch (NumberFormatException e) {
                            // We don't recognize this jar. Perhaps it has been
                            // replaced with an unsupported version. In any
                            // case, remove it from the configuration
                            i.remove();
                        }
                    }
                }

                // Done
                initialised = true;
                initialising = false;
                initialisationLock.notifyAll();
            } catch (IOException e) {
                throw new RuntimeException("I/O error while scanning for Minecraft jars", e);
            }
        }
    }
    
    private static final Map<Checksum, Set<BiomeSchemeDescriptor>> DESCRIPTORS = new HashMap<Checksum, Set<BiomeSchemeDescriptor>>();
    private static final Map<Integer, BiomeScheme> BIOME_SCHEMES = new HashMap<Integer, BiomeScheme>();
    private static final Map<Integer, SortedMap<Version, BiomeJar>> BIOME_JARS = new HashMap<Integer, SortedMap<Version, BiomeJar>>();
    private static final SortedMap<Version, File> ALL_JARS = new TreeMap<Version, File>();
    private static final Object initialisationLock = new Object();
    private static File minecraftDir;
    private static boolean initialised, initialising;

    public static final int BIOME_ALGORITHM_1_7_3               =  0;
    public static final int BIOME_ALGORITHM_1_9                 =  1;
    public static final int BIOME_ALGORITHM_1_8_1               =  2;
    public static final int BIOME_ALGORITHM_1_0_0               =  3;
    public static final int BIOME_ALGORITHM_1_1                 =  4;
    public static final int BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT =  5;
    public static final int BIOME_ALGORITHM_AUTO_BIOMES         =  7; 
    public static final int BIOME_ALGORITHM_1_3_LARGE           =  8;
    public static final int BIOME_ALGORITHM_1_7_DEFAULT         =  9;
    public static final int BIOME_ALGORITHM_1_7_LARGE           = 10;
    
    public static final String[] BIOME_ALGORITHM_NAMES = {"Beta 1.7.3", "Beta 1.9", "Beta 1.8.1", "1.0.0", "1.1", "1.2-1.6 Default", "Custom", "Auto", "1.3-1.6 Large", "1.7 Default", "1.7 Large"};
    
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
        addDescriptor(new Checksum(new byte[] {(byte) 46, (byte) 80, (byte) 68, (byte) -11, (byte) 53, (byte) -98, (byte) -126, (byte) 36, (byte) 85, (byte) 81, (byte) 22, (byte) 122, (byte) 35, (byte) 127, (byte) 49, (byte) 103}),         new BiomeSchemeDescriptor(new Version(1, 6, 4), BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, Minecraft1_6BiomeScheme.class, true));

        addDescriptor(new Checksum(new byte[] {(byte) 29, (byte) 67, (byte) -51, (byte) -70, (byte) -117, (byte) -105, (byte) 82, (byte) -41, (byte) -11, (byte) 87, (byte) -85, (byte) 125, (byte) 62, (byte) 54, (byte) 89, (byte) 100}),     new BiomeSchemeDescriptor(new Version(1, 6, 2), BIOME_ALGORITHM_1_3_LARGE, Minecraft1_6LargeBiomeScheme.class, true));
        addDescriptor(new Checksum(new byte[] {(byte) 46, (byte) 80, (byte) 68, (byte) -11, (byte) 53, (byte) -98, (byte) -126, (byte) 36, (byte) 85, (byte) 81, (byte) 22, (byte) 122, (byte) 35, (byte) 127, (byte) 49, (byte) 103}),         new BiomeSchemeDescriptor(new Version(1, 6, 4), BIOME_ALGORITHM_1_3_LARGE, Minecraft1_6LargeBiomeScheme.class, true));

        addDescriptor(new Checksum(new byte[] {(byte) 122, (byte) 48, (byte) 69, (byte) 84, (byte) -3, (byte) -22, (byte) -121, (byte) -102, (byte) 121, (byte) -98, (byte) -2, (byte) 110, (byte) -82, (byte) -35, (byte) -116, (byte) -107}), new BiomeSchemeDescriptor(new Version(1, 7, 2), BIOME_ALGORITHM_1_7_DEFAULT, Minecraft1_7BiomeScheme.class, true));
        addDescriptor(new Checksum(new byte[] {(byte) 95, (byte) 124, (byte) -57, (byte) -21, (byte) 1, (byte) -53, (byte) -39, (byte) 53, (byte) -87, (byte) -105, (byte) 60, (byte) -74, (byte) -23, (byte) -60, (byte) -63, (byte) 14}),     new BiomeSchemeDescriptor(new Version(1, 7, 9), BIOME_ALGORITHM_1_7_DEFAULT, Minecraft1_7BiomeScheme.class, true));
        addDescriptor(new Checksum(new byte[] {(byte) -122, (byte) 99, (byte) -95, (byte) 12, (byte) -20, (byte) -63, (byte) 14, (byte) -86, (byte) 104, (byte) 58, (byte) -110, (byte) 126, (byte) -11, (byte) 55, (byte) 24, (byte) 82}),     new BiomeSchemeDescriptor(new Version(1, 8),    BIOME_ALGORITHM_1_7_DEFAULT, Minecraft1_8BiomeScheme.class, true));
        addDescriptor(new Checksum(new byte[] {(byte) 92, (byte) -102, (byte) -81, (byte) 49, (byte) 25, (byte) -97, (byte) 118, (byte) 62, (byte) -7, (byte) 8, (byte) 92, (byte) -55, (byte) -74, (byte) -112, (byte) 43, (byte) 29}),        new BiomeSchemeDescriptor(new Version(1, 8, 1), BIOME_ALGORITHM_1_7_DEFAULT, Minecraft1_8BiomeScheme.class, true));

        addDescriptor(new Checksum(new byte[] {(byte) 122, (byte) 48, (byte) 69, (byte) 84, (byte) -3, (byte) -22, (byte) -121, (byte) -102, (byte) 121, (byte) -98, (byte) -2, (byte) 110, (byte) -82, (byte) -35, (byte) -116, (byte) -107}), new BiomeSchemeDescriptor(new Version(1, 7, 2), BIOME_ALGORITHM_1_7_LARGE, Minecraft1_7LargeBiomeScheme.class, true));
        addDescriptor(new Checksum(new byte[] {(byte) 95, (byte) 124, (byte) -57, (byte) -21, (byte) 1, (byte) -53, (byte) -39, (byte) 53, (byte) -87, (byte) -105, (byte) 60, (byte) -74, (byte) -23, (byte) -60, (byte) -63, (byte) 14}),     new BiomeSchemeDescriptor(new Version(1, 7, 9), BIOME_ALGORITHM_1_7_LARGE, Minecraft1_7LargeBiomeScheme.class, true));
        addDescriptor(new Checksum(new byte[] {(byte) -122, (byte) 99, (byte) -95, (byte) 12, (byte) -20, (byte) -63, (byte) 14, (byte) -86, (byte) 104, (byte) 58, (byte) -110, (byte) 126, (byte) -11, (byte) 55, (byte) 24, (byte) 82}),     new BiomeSchemeDescriptor(new Version(1, 8),    BIOME_ALGORITHM_1_7_LARGE, Minecraft1_8LargeBiomeScheme.class, true));
        addDescriptor(new Checksum(new byte[] {(byte) 92, (byte) -102, (byte) -81, (byte) 49, (byte) 25, (byte) -97, (byte) 118, (byte) 62, (byte) -7, (byte) 8, (byte) 92, (byte) -55, (byte) -74, (byte) -112, (byte) 43, (byte) 29}),        new BiomeSchemeDescriptor(new Version(1, 8, 1), BIOME_ALGORITHM_1_7_LARGE, Minecraft1_8LargeBiomeScheme.class, true));
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
    
    static class BiomeSchemeDescriptor {
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
        
        final Version minecraftVersion;
        final int biomeScheme;
        final Class<? extends BiomeScheme> _class;
        final Constructor<? extends BiomeScheme> constructor;
        private final boolean addLibraries;
    }
    
    static class BiomeJar {
        BiomeJar(File file, Checksum checksum, BiomeSchemeDescriptor descriptor) {
            this.file = file;
            this.checksum = checksum;
            this.descriptor = descriptor;
        }

        final File file;
        final Checksum checksum;
        final BiomeSchemeDescriptor descriptor;
    }
}