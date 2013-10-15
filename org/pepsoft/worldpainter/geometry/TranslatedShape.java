/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.geometry;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author pepijn
 */
public class TranslatedShape extends TransformedShape {
    public TranslatedShape(Shape3D shape, Vector3f translation) {
        super(shape, new Transform3D(IDENTITY_MATRIX, translation, 1.0f));
    }
    
    private static final Matrix3f IDENTITY_MATRIX = new Matrix3f(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
}