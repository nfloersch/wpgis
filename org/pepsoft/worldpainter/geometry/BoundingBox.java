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
public class BoundingBox {
    public BoundingBox(Point3f corner1, Point3f corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public Point3f getCorner1() {
        return corner1;
    }

    public Point3f getCorner2() {
        return corner2;
    }
    
    private final Point3f corner1, corner2;
}