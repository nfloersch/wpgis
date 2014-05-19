/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.groundcover;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.NoiseSettings;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;

/**
 *
 * @author pepijn
 */
public class GroundCoverLayer extends CustomLayer {
    public GroundCoverLayer(String name, MixedMaterial material, int colour) {
        super(name, "a 1 block layer of " + name + " on top of the terrain", DataSize.BIT, 30, colour);
        mixedMaterial = material;
        exporter = new GroundCoverLayerExporter(this);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        setDescription("a " + thickness + " block layer of " + name + " on top of the terrain");
    }

    public MixedMaterial getMaterial() {
        return mixedMaterial;
    }

    public void setMaterial(MixedMaterial material) {
        mixedMaterial = material;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
        setDescription("a " + thickness + " block layer of " + getName() + " on top of the terrain");
    }

    public int getEdgeWidth() {
        return edgeWidth;
    }

    public void setEdgeWidth(final int edgeWidth) {
        this.edgeWidth = edgeWidth;
    }

    public EdgeShape getEdgeShape() {
        return edgeShape;
    }

    public void setEdgeShape(EdgeShape edgeShape) {
        if (edgeShape == null) {
            throw new NullPointerException();
        }
        this.edgeShape = edgeShape;
    }

    public NoiseSettings getNoiseSettings() {
        return noiseSettings;
    }

    public void setNoiseSettings(NoiseSettings noiseSettings) {
        this.noiseSettings = noiseSettings;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    @Override
    public LayerExporter<GroundCoverLayer> getExporter() {
        return exporter;
    }

    // Comparable
    @Override
    public int compareTo(Layer layer) {
        if (priority < layer.priority) {
            return -1;
        } else if (priority > layer.priority) {
            return 1;
        } else if ((layer instanceof GroundCoverLayer) && (Math.abs(((GroundCoverLayer) layer).thickness) != Math.abs(thickness))) {
            return Math.abs(((GroundCoverLayer) layer).thickness) - Math.abs(thickness);
        } else {
            return getId().compareTo(layer.getId());
        }
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        exporter = new GroundCoverLayerExporter(this);

        // Legacy support
        if (colour != 0) {
            setColour(colour);
            colour = 0;
        }
        if (thickness == 0) {
            thickness = 1;
        }
        if (mixedMaterial == null) {
            mixedMaterial = MixedMaterial.create(material);
            material = null;
        }
        if (edgeShape == null) {
            edgeWidth = 1;
            if (thickness == 1) {
                edgeShape = EdgeShape.SHEER;
            } else {
                edgeShape = EdgeShape.ROUNDED;
            }
        }
    }
    
    @Deprecated
    private Material material;
    @Deprecated
    private int colour;
    private int thickness = 1;
    private int edgeWidth = 1;
    private MixedMaterial mixedMaterial;
    private EdgeShape edgeShape = EdgeShape.SHEER;
    private NoiseSettings noiseSettings;
    private boolean smooth;
    private transient GroundCoverLayerExporter exporter;

    private static final long serialVersionUID = 1L;
    
    public enum EdgeShape {SHEER, LINEAR, SMOOTH, ROUNDED}
}