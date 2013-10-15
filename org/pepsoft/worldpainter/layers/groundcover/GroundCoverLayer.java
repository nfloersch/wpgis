/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.groundcover;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.CustomLayer;

/**
 *
 * @author pepijn
 */
public class GroundCoverLayer extends CustomLayer {
    public GroundCoverLayer(String name, Material material, int colour) {
        super(name, "a 1 block layer of " + name + " on top of the terrain", DataSize.BIT, 30, colour);
        this.material = material;
        exporter = new GroundCoverLayerExporter(this);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        setDescription("a " + thickness + " block layer of " + name + " on top of the terrain");
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
        setDescription("a " + thickness + " block layer of " + getName() + " on top of the terrain");
    }

//    public boolean isTaperedEdge() {
//        return taperedEdge;
//    }
//
//    public void setTaperedEdge(final boolean taperedEdge) {
//        this.taperedEdge = taperedEdge;
//    }
//
//    public boolean isVariedEdge() {
//        return variedEdge;
//    }
//
//    public void setVariedEdge(final boolean variedEdge) {
//        this.variedEdge = variedEdge;
//    }
//
//    public int getEdgeWidth() {
//        return edgeWidth;
//    }
//
//    public void setEdgeWidth(final int edgeWidth) {
//        this.edgeWidth = edgeWidth;
//    }

    @Override
    public LayerExporter<GroundCoverLayer> getExporter() {
        return exporter;
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
    }
    
    private Material material;
    @Deprecated
    private int colour;
    private int thickness = 5;
//    private boolean taperedEdge = true, variedEdge;
//    private int edgeWidth = 1;
    private transient GroundCoverLayerExporter exporter;

    private static final long serialVersionUID = 1L;
}