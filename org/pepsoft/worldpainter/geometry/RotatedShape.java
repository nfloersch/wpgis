/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.geometry;

import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author pepijn
 */
public class RotatedShape extends TransformedShape {
    public RotatedShape(Shape3D shape, float x, float y, float z) {
        super(shape, new Transform3D(new Quat4f(x, y, z, 1.0f), NULL_VECTOR, 1.0f));
    }
    
    private static final Vector3f NULL_VECTOR = new Vector3f();
}