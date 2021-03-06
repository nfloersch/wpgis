/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.pockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;

/**
 *
 * @author pepijn
 */
public class UndergroundPocketsLayer extends CustomLayer {
    public UndergroundPocketsLayer(String name, Material material, int frequency, int minLevel, int maxLevel, int scale, int colour) {
        super(name, "underground pockets of " + name, DataSize.NIBBLE, 15, colour);
        if ((frequency < 1) || (frequency > 1000)) {
            throw new IllegalArgumentException("frequency");
        }
        if (scale < 0) {
            throw new IllegalArgumentException("scale");
        }
        if (minLevel < 0) {
            throw new IllegalArgumentException("minLevel");
        }
        if (maxLevel < minLevel) {
            throw new IllegalArgumentException("maxLevel");
        }
        this.material = material;
        this.frequency = frequency;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.scale = scale;
        exporter = new UndergroundPocketsLayerExporter(this);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        setDescription("underground pockets of " + name);
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    @Override
    public LayerExporter<? extends Layer> getExporter() {
        return exporter;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        exporter = new UndergroundPocketsLayerExporter(this);
    }
    
    private Material material;
    private int scale, frequency, maxLevel, minLevel;
    private transient UndergroundPocketsLayerExporter exporter;
    
    private static final long serialVersionUID = 1L;
}