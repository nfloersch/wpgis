/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;

/**
 *
 * @author pepijn
 */
public class Brush {
    public Brush(MixedMaterial material, Shape shape, Vector3d scale) {
        this.material = material;
        this.shape = shape;
        this.scale = scale;
    }
    
    public void paint(MinecraftWorld world, Point3d coords) {
        for (int x = (int) (coords.x - scale.x / 2 + 0.5); x <= (int) (coords.x + scale.x / 2 + 0.5); x++) {
            for (int y = (int) (coords.y - scale.y / 2 + 0.5); y <= (int) (coords.y + scale.y / 2 + 0.5); y++) {
                for (int z = (int) (coords.z - scale.z / 2 + 0.5); z <= (int) (coords.z + scale.z / 2 + 0.5); z++) {
                    int existingBlockType = world.getBlockTypeAt(x, y, z);
                    Material myMaterial = material.getMaterial(0, x, y, z);
                    if (Constants.VERY_INSUBSTANTIAL_BLOCKS.contains(existingBlockType)) {
                        world.setMaterialAt(x, y, z, myMaterial);
                    }
                }
            }
        }
    }

    public MixedMaterial getMaterial() {
        return material;
    }

    public void setMaterial(MixedMaterial material) {
        this.material = material;
    }

    public Vector3d getScale() {
        return scale;
    }

    public void setScale(Vector3d scale) {
        this.scale = scale;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
    
    private MixedMaterial material;
    private Vector3d scale;
    private Shape shape;
    
    public enum Shape {SPHERE, CUBE}
}