/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;

/**
 *
 * @author pepijn
 */
public class BiomesViewerMain {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        Logger rootLogger = Logger.getLogger("");
//        for (Handler handler: rootLogger.getHandlers()) {
//            handler.setLevel(Level.FINE);
//            handler.setFormatter(new Formatter() {
//                @Override
//                public String format(LogRecord record) {
//                    java.util.Formatter formatter = new java.util.Formatter();
//                    String loggerName = record.getLoggerName();
//                    if (loggerName.length() > 30) {
//                        loggerName = loggerName.substring(loggerName.length() - 30);
//                    }
//                    formatter.format("[%-6s] (%30s) [%d] %s%n", record.getLevel().getName(), loggerName, record.getThreadID(), record.getMessage());
//                    return formatter.toString();
//                }
//            });
//        }
//        Logger logger = Logger.getLogger("org.pepsoft");
//        logger.setLevel(Level.FINE);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            // Oh well
        } catch (IllegalAccessException e) {
            // Oh well
        } catch (InstantiationException e) {
            // Oh well
        } catch (UnsupportedLookAndFeelException e) {
            // Oh well
        }
        
        Configuration config = Configuration.load();
        if (config == null) {
            config = new Configuration();
        }
        Configuration.setInstance(config);
        
        BiomesViewerFrame frame = new BiomesViewerFrame(World2.DEFAULT_OCEAN_SEED, BiomeSchemeManager.getBiomeScheme(World2.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT, null), null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}