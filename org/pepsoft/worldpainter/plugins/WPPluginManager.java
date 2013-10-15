/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.plugins;

import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author pepijn
 */
public class WPPluginManager {
    private WPPluginManager(UUID uuid) {
        allPlugins = org.pepsoft.util.PluginManager.findPlugins(Plugin.class, FILENAME);
        Set<String> namesEncountered = new HashSet<String>();
        for (Iterator<Plugin> i = allPlugins.iterator(); i.hasNext(); ) {
            Plugin plugin = i.next();
            if ((plugin.getUUIDs() != null) && (uuid != null) && (! plugin.getUUIDs().contains(uuid))) {
                logger.severe(plugin.getName() + " plugin is not authorised for this installation; not loading it");
                i.remove();
                continue;
            }
            String name = plugin.getName();
            if (namesEncountered.contains(name)) {
                throw new RuntimeException("Multiple plugins with the same name (" + name + ") detected!");
            } else {
                namesEncountered.add(name);
            }
            logger.info("Loaded plugin: " + name + " (version " + plugin.getVersion() + ")");
        }
    }
    
    public List<Plugin> getAllPlugins() {
        return Collections.unmodifiableList(allPlugins);
    }
    
    @SuppressWarnings("unchecked") // Guaranteed by Java
    public <T extends Plugin> List<T> getPlugins(Class<T> type) {
        List<T> plugins = new ArrayList<T>(allPlugins.size());
        for (Plugin plugin: allPlugins) {
            if (type.isAssignableFrom(plugin.getClass())) {
                plugins.add((T) plugin);
            }
        }
        return plugins;
    }
    
    public static synchronized void initialise(UUID uuid) {
        if (instance != null) {
            throw new IllegalStateException("Already initialised");
        }
        instance = new WPPluginManager(uuid);
    }
    
    public static synchronized WPPluginManager getInstance() {
        return instance;
    }
    
    private final List<Plugin> allPlugins;
    
    private static WPPluginManager instance;
    private static final String FILENAME = "org.pepsoft.worldpainter.plugins";
    private static final Logger logger = Logger.getLogger(WPPluginManager.class.getName());
}