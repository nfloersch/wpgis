/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

/* im port com.install4j.api.launcher.ApplicationLauncher; */
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.pepsoft.util.MathUtils;
import org.pepsoft.worldpainter.Dimension.Border;
import org.pepsoft.worldpainter.browser.WPTrustManager;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.plugins.WPPluginManager;
import org.pepsoft.worldpainter.util.WPLogManager;
import static org.pepsoft.worldpainter.Constants.*;
import org.pepsoft.worldpainter.operations.MouseOrTabletOperation;
import org.pepsoft.worldpainter.plugins.Plugin;
import org.pepsoft.worldpainter.util.BetterAction;
import org.pepsoft.worldpainter.vo.EventVO;

/**
 *
 * @author pepijn
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (processCommandLine(args)) {
            return;
        }
        
        System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        // Configure logging
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        boolean debugLogging = "true".equalsIgnoreCase(System.getProperty("org.pepsoft.worldpainter.debugLogging"));
        Formatter formatter = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    StringWriter sw = new StringWriter();
                    StringBuffer sb = sw.getBuffer();
                    java.util.Formatter formatter = new java.util.Formatter(sb);
                    String loggerName = record.getLoggerName();
                    if (loggerName.length() > 30) {
                        loggerName = loggerName.substring(loggerName.length() - 30);
                    }
                    long date = record.getMillis();
                    long millis = date % 1000;
                    formatter.format("[%tF %<tT.%03d] {%-6s} (%30s) %s%n", date, millis, record.getLevel().getName(), loggerName, record.getMessage());
                    if (record.getThrown() != null) {
                        record.getThrown().printStackTrace(new PrintWriter(sw, false));
                    }
                    return sb.toString();
                }
            };
        for (Handler handler: rootLogger.getHandlers()) {
            handler.setLevel(debugLogging ? Level.FINER : Level.INFO);
            handler.setFormatter(formatter);
        }
        File configDir = Configuration.getConfigDir();
        if (! configDir.isDirectory()) {
            configDir.mkdirs();
        }
        try {
            FileHandler fileHandler = new FileHandler(configDir.getAbsolutePath() + "/logfile%g.txt", 10 * 1024 * 1024, 2, true);
            fileHandler.setLevel(debugLogging ? Level.FINER : Level.INFO);
            fileHandler.setFormatter(formatter);
            rootLogger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (debugLogging) {
            Logger pepsoftLogger = Logger.getLogger("org.pepsoft");
            pepsoftLogger.setLevel(Level.FINE);
            additionalLoggers.add(pepsoftLogger);
//        Logger villagesLogger = Logger.getLogger("org.pepsoft.worldpainter.villages");
//        villagesLogger.setLevel(Level.FINER);
//        additionalLoggers.add(villagesLogger);
//        villagesLogger = Logger.getLogger("org.pepsoft.worldpainter.gardenofeden");
//        villagesLogger.setLevel(Level.FINER);
//        additionalLoggers.add(villagesLogger);
//            Logger exportersLogger = Logger.getLogger("org.pepsoft.worldpainter.layers.exporters");
//            exportersLogger.setLevel(Level.FINER);
//            additionalLoggers.add(exportersLogger);
//        Logger bo2Logger = Logger.getLogger("org.pepsoft.worldpainter.layers.bo2");
//        bo2Logger.setLevel(Level.FINER);
//        additionalLoggers.add(bo2Logger);
        }
        logger.info("Starting WorldPainter " + Version.VERSION);

        // Load or initialise configuration
        Configuration config = null;
        try {
            config = Configuration.load(); // This will migrate the configuration directory if necessary
        } catch (IOException e) {
            configError(e);
        } catch (ClassNotFoundException e) {
            configError(e);
        }
        if (config == null) {
            config = new Configuration();
        }
        Configuration.setInstance(config);
        logger.info("Installation ID: " + config.getUuid());

        // Load and install trusted WorldPainter root certificate
        X509Certificate trustedCert = null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            trustedCert = (X509Certificate) certificateFactory.generateCertificate(Main.class.getResourceAsStream("/wproot.pem"));
            
            WPTrustManager trustManager = new WPTrustManager(trustedCert);
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] {trustManager}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (CertificateException e) {
            logger.log(Level.SEVERE, "Certificate exception while loading trusted root certificate", e);
        } catch (NoSuchAlgorithmException  e) {
            logger.log(Level.SEVERE, "No such algorithm exception while loading trusted root certificate", e);
        } catch (KeyManagementException e) {
            logger.log(Level.SEVERE, "Key management exception while loading trusted root certificate", e);
        }
        
        // Load the plugins
        if (trustedCert != null) {
            org.pepsoft.util.PluginManager.loadPlugins(new File(configDir, "plugins"), trustedCert.getPublicKey());
        } else {
            logger.severe("Trusted root certificate not available; not loading plugins");
        }
        WPPluginManager.initialise(config.getUuid());
        
        // Use the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            // We tried...
        } catch (InstantiationException e) {
            // We tried...
        } catch (IllegalAccessException e) {
            // We tried...
        } catch (UnsupportedLookAndFeelException e) {
            // We tried...
        }
        
        // Don't paint values above sliders in GTK look and feel
        UIManager.put("Slider.paintValue", Boolean.FALSE);
        
        String httpAgent = "WorldPainter " + Version.VERSION + "; " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + ";";
        //        System.out.println(httpAgent);
        System.setProperty("http.agent", httpAgent);
        
        // This will return immediately if you call it from the EDT,
        // otherwise it will block until the installer application exits
//        if ((! "true".equals(System.getProperty("org.pepsoft.worldpainter.devMode"))) && config.isCheckForUpdates()) {
//            logger.info("Checking for updates");
//            ApplicationLauncher.launchApplicationInProcess("217", null, new ApplicationLauncher.Callback() {
//                    @Override
//                    public void exited(int exitValue) {
//                        // Do nothing
//                    }
//
//                    @Override
//                    public void prepareShutdown() {
//                        // Do nothing
//                    }
//                }, ApplicationLauncher.WindowMode.FRAME, null
//            );
//            // Install4j overrides the default uncaught exception handler, so restore it:
//            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
//        } else {
//            logger.info("Update check disabled in preferences or by system property");
//        }

        final long start = System.currentTimeMillis();
        config.setLaunchCount(config.getLaunchCount() + 1);
        Runtime.getRuntime().addShutdownHook(new Thread("Configuration Saver") {
            @Override
            public void run() {
                try {
                    Configuration config = Configuration.getInstance();
                    MouseOrTabletOperation.flushEvents(config);
                    BetterAction.flushEvents(config);
                    EventVO sessionEvent = new EventVO("worldpainter.session").setAttribute(EventVO.ATTRIBUTE_TIMESTAMP, new Date(start)).duration(System.currentTimeMillis() - start);
                    StringBuilder sb = new StringBuilder();
                    List<Plugin> plugins = WPPluginManager.getInstance().getAllPlugins();
                    for (Plugin plugin: plugins) {
                        if (! plugin.getName().equals("Default")) {
                            if (sb.length() > 0) {
                                sb.append(',');
                            }
                            sb.append("{name=");
                            sb.append(plugin.getName().replaceAll("[ \\t\\n\\x0B\\f\\r\\.]", ""));
                            sb.append(",version=");
                            sb.append(plugin.getVersion());
                            sb.append('}');
                        }
                    }
                    if (sb.length() > 0) {
                        sessionEvent.setAttribute(ATTRIBUTE_KEY_PLUGINS, sb.toString());
                    }
                    config.logEvent(sessionEvent);
                    config.save();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "I/O error saving configuration", e);
                }
                logger.info("Shutting down WorldPainter");
                ((WPLogManager) LogManager.getLogManager()).realReset();
            }
        });
        
        // Make the "action:" URLs used in various places work:
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (protocol.equals("action")) {
                    return new URLStreamHandler() {
                        @Override
                        protected URLConnection openConnection(URL u) throws IOException {
                            throw new UnsupportedOperationException("Not supported");
                        }
                    };
                } else {
                    return null;
                }
            }
        });

        final World2 world;
        final File file;
        if ((args.length > 0) && new File(args[0]).isFile()) {
            file = new File(args[0]);
            world = null;
        } else {
            file = null;
//            final HeightMapTileFactory tileFactory = new ExperimentalTileFactory(config.getDefaultMaxHeight());
            final HeightMapTileFactory tileFactory;
            if (config.isHilly()) {
                tileFactory = TileFactoryFactory.createNoiseTileFactory(config.getSurface(), config.getDefaultMaxHeight(), config.getLevel(), config.getWaterLevel(), config.isLava(), config.isBeaches(), config.getDefaultRange(), config.getDefaultScale());
            } else {
                tileFactory = TileFactoryFactory.createFlatTileFactory(config.getSurface(), config.getDefaultMaxHeight(), config.getLevel(), config.getWaterLevel(), config.isLava(), config.isBeaches());
            }
            Dimension defaults = config.getDefaultTerrainAndLayerSettings();
            if ((defaults.getTileFactory() instanceof HeightMapTileFactory) && (((HeightMapTileFactory) defaults.getTileFactory()).getTerrainRanges() != null)) {
                HeightMapTileFactory defaultTileFactory = (HeightMapTileFactory) defaults.getTileFactory();
                tileFactory.setTerrainRanges(new TreeMap<Integer, Terrain>(defaultTileFactory.getTerrainRanges()));
                tileFactory.setRandomise(defaultTileFactory.isRandomise());
            }
            world = new World2(World2.DEFAULT_OCEAN_SEED, new Random().nextLong(), tileFactory, tileFactory.getMaxHeight());
            ResourceBundle strings = ResourceBundle.getBundle("org.pepsoft.worldpainter.resources.strings");
            world.setName(strings.getString("generated.world"));
            Dimension dim0 = world.getDimension(0);
            dim0.setEventsInhibited(true);
            try {
                if (config.isDefaultCircularWorld()) {
                    int radius = config.getDefaultWidth() * 64;
                    int tileRadius = (radius + 127) / 128;
                    for (int x = -tileRadius; x < tileRadius; x++) {
                        for (int y = -tileRadius; y < tileRadius; y++) {
                            if (org.pepsoft.worldpainter.util.MathUtils.getSmallestDistanceFromOrigin(x, y) < radius) {
                                // At least one corner is inside the circle; include
                                // the tile. Note that this is always correct in
                                // this case only because the centre of the circle
                                // is always at a tile intersection so the circle
                                // can never "bulge" into a tile without any of the
                                // the tile's corners being inside the circle
                                Tile tile = tileFactory.createTile(dim0.getSeed(), x, y);
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
                    dim0.setBorder(Border.VOID);
                    dim0.setBorderSize(2);
                } else {
                    int width = config.getDefaultWidth(), height = config.getDefaultHeight();
                    int startX = -width / 2;
                    int startY = -height / 2;
                    for (int x = startX; x < startX + width; x++) {
                        for (int y = startY; y < startY + height; y++) {
                            Tile tile = tileFactory.createTile(dim0.getSeed(), x, y);
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
            } finally {
                dim0.setEventsInhibited(false);
            }
            if (tileFactory.getMaxHeight() == org.pepsoft.minecraft.Constants.DEFAULT_MAX_HEIGHT_2) {
                world.setBiomeAlgorithm(config.isDefaultAutomaticBiomesEnabled() ? World2.BIOME_ALGORITHM_AUTO_BIOMES : World2.BIOME_ALGORITHM_NONE);
                world.setCustomBiomes(config.isDefaultCustomBiomesEnabled());
            } else {
                world.setBiomeAlgorithm(World2.BIOME_ALGORITHM_NONE);
                world.setCustomBiomes(false);
            }
            world.setDirty(false);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                App app = App.getInstance();
                app.setVisible(true);
                // Swing quirk:
                if (Configuration.getInstance().isMaximised()) {
                    app.setExtendedState(Frame.MAXIMIZED_BOTH);
                }
                if (world != null) {
                    // On a Mac we may be doing this unnecessarily because we
                    // may be opening a .world file, but it has proven difficult
                    // to detect that. TODO
                    app.setWorld(world);
                    if (world.isCustomBiomes() && (app.getBiomeScheme() != null)) {
                        Dimension dimension = world.getDimension(0);
                        
                        // Initialise the custom biomes
                        dimension.recalculateBiomes(app.getBiomeScheme(), null);
                        
                        // Throw away the undo information generated by initialising the biomes
                        dimension.clearUndo();
                        dimension.armSavePoint();
                    }
                } else {
                    app.open(file);
                }
                DonationDialog.maybeShowDonationDialog(app);
            }
        });
    }
    
    private static void configError(Exception e) {
        JOptionPane.showMessageDialog(null, "Could not read configuration file! Resetting configuration.\n\nException type: " + e.getClass().getSimpleName() + "\nMessage: " + e.getMessage(), "Configuration Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private static boolean processCommandLine(String[] args) {
        for (int i = 0; i < args.length; i++) {
        }
        return false;
    }
    
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    /**
     * A list of references to package loggers which had diverging log levels
     * set, so that they don't get garbage collected before an actual logger is
     * created in the package.
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final List<Logger> additionalLoggers = new ArrayList<Logger>();
}