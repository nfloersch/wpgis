/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.geometry;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 *
 * @author pepijn
 */
public interface Shape3D {
    boolean isInside(Point3f coords);
    BoundingBox getBoundingBox();
    void visitVolume(VolumeVisitor visitor);
    
    public interface VolumeVisitor {
        void visit(Shape3D shape, Point3i coords);
    }
}