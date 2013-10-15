/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.PluginManager;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.renderers.LayerRenderer;

/**
 *
 * @author pepijn
 */
public abstract class Layer implements Serializable, Comparable<Layer> {
    @Deprecated
    protected Layer(String name, String description, DataSize dataSize, int priority) {
        this(name, name, description, dataSize, priority, '\0');
    }

    @Deprecated
    protected Layer(String name, String description, DataSize dataSize, int priority, char mnemonic) {
        this(name, name, description, dataSize, priority, mnemonic);
    }

    protected Layer(String id, String name, String description, DataSize dataSize, int priority) {
        this(id, name, description, dataSize, priority, '\0');
    }
    
    protected Layer(String id, String name, String description, DataSize dataSize, int priority, char mnemonic) {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.dataSize = dataSize;
        this.priority = priority;
        this.mnemonic = mnemonic;
        init();
    }
    
    public final DataSize getDataSize() {
        return dataSize;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }
    
    public LayerExporter<? extends Layer> getExporter() {
        return exporter;
    }
    
    public LayerRenderer getRenderer() {
        return renderer;
    }
    
    public BufferedImage getIcon() {
        return icon;
    }
    
    public char getMnemonic() {
        return mnemonic;
    }

    public int getPriority() {
        return priority;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Layer)
            && id.equals(((Layer) obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    // Comparable

    @Override
    public int compareTo(Layer layer) {
        if (priority < layer.priority) {
            return -1;
        } else if (priority > layer.priority) {
            return 1;
        } else {
            return id.compareTo(layer.id);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void init() {
        Class<? extends Layer> clazz = getClass();
        ClassLoader pluginClassLoader = PluginManager.getPluginClassLoader();
        try {
            LayerRenderer myRenderer;
            try {
                myRenderer = (LayerRenderer) pluginClassLoader.loadClass(clazz.getPackage().getName() + ".renderers." + clazz.getSimpleName() + "Renderer").newInstance();
            } catch (ClassNotFoundException e) {
                // This most likely means the class does not exist
                myRenderer = null;
            } catch (InstantiationException e) {
                // This most likely means that the class has no default
                // constructor
                myRenderer = null;
            }
            renderer = myRenderer;
            LayerExporter<? extends Layer> myExporter;
            try {
                myExporter = (LayerExporter<? extends Layer>) pluginClassLoader.loadClass(clazz.getPackage().getName() + ".exporters." + clazz.getSimpleName() + "Exporter").newInstance();
            } catch (ClassNotFoundException e) {
                myExporter = null;
            }
            exporter = myExporter;
        } catch (InstantiationException e) {
            throw new RuntimeException("Exception thrown while instantiating renderer", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Access denied while instantiating renderer", e);
        }
        icon = IconUtils.loadImage(clazz.getClassLoader(), "org/pepsoft/worldpainter/icons/" + getClass().getSimpleName().toLowerCase() + ".png");
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Legacy
        if (id == null) {
            id = name;
        }
        
        init();
    }

    private String name, description;
    public final DataSize dataSize;
    public final int priority;
    private String id;
    private transient LayerRenderer renderer;
    private transient LayerExporter<? extends Layer> exporter;
    private transient BufferedImage icon;
    private transient char mnemonic;

    private static final long serialVersionUID = 2011032901L;

    public enum DataSize {BIT, NIBBLE, BYTE, BIT_PER_CHUNK, NONE}
}
