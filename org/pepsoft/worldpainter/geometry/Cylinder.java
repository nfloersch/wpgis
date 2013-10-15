/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.geometry;

import javax.vecmath.Point3f;

/**
 *
 * @author pepijn
 */
public class Cylinder extends AbstractShape3D {
    public Cylinder(float radius, float height) {
        this.radius = radius;
        this.height = height;
    }

    public float getHeight() {
        return height;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public boolean isInside(Point3f coords) {
        return (coords.z >= 0 && coords.z <= height)
            && (coords.x * coords.x + coords.y * coords.y < radius * radius);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(new Point3f(-radius, -radius, 0), new Point3f(radius, radius, height));
    }
    
    private final float radius, height;
}
