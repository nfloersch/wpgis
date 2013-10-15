/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.geometry;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;

/**
 *
 * @author pepijn
 */
public class TransformedShape extends AbstractShape3D {
    public TransformedShape(Shape3D shape, Transform3D transform) {
        this.shape = shape;
        this.transform = transform;
        inverseTransform = new Transform3D(transform);
        inverseTransform.invert();
    }

    @Override
    public boolean isInside(Point3f coords) {
        inverseTransform.transform(coords);
        return shape.isInside(coords);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return transformBoundingBox(shape.getBoundingBox(), transform);
    }

    public Transform3D getTransform() {
        return transform;
    }
    
    private final Shape3D shape;
    private final Transform3D transform, inverseTransform;
}