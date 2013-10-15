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
public class Box extends AbstractShape3D {
    public Box(float width, float length, float height) {
        this.width = width;
        this.length = length;
        this.height = height;
    }

    public float getHeight() {
        return height;
    }

    public float getLength() {
        return length;
    }

    public float getWidth() {
        return width;
    }
    
    @Override
    public boolean isInside(Point3f coords) {
        return coords.x >= (-width / 2) && coords.x <= (width / 2)
            && coords.y >= (-length / 2) && coords.y <= (length / 2)
            && coords.z >= 0 && coords.z <= height;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(new Point3f(-width / 2, -length / 2, 0), new Point3f(width / 2, length / 2, height));
    }
    
    private final float width, length, height;
}