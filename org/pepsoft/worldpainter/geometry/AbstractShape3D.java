/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.geometry;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 *
 * @author pepijn
 */
public abstract class AbstractShape3D extends Node implements Shape3D {
    @Override
    public void visitVolume(VolumeVisitor visitor) {
        // Transform the shape with the transforms of its parents
        Shape3D shape = this;
        Node parent = getParent();
        while (parent != null) {
            if (parent instanceof TransformedShape) {
                Transform3D transform = ((TransformedShape) parent).getTransform();
                shape = new TransformedShape(shape, transform);
            }
            parent = parent.getParent();
        }
        
        // Get the bounding box of the transformed shape and round it to integer
        // coordinates
        BoundingBox boundingBox = shape.getBoundingBox();
        int x1 = (int) Math.floor(boundingBox.getCorner1().x);
        int y1 = (int) Math.floor(boundingBox.getCorner1().y);
        int z1 = (int) Math.floor(boundingBox.getCorner1().z);
        int x2 = (int) Math.floor(boundingBox.getCorner2().x);
        int y2 = (int) Math.floor(boundingBox.getCorner2().y);
        int z2 = (int) Math.floor(boundingBox.getCorner2().z);
        
        
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    if (shape.isInside(new Point3f(x + 0.5f, y + 0.5f, z + 0.5f))) {
                        visitor.visit(this, new Point3i(x, y, z));
                    }
                }
            }
        }
    }
    
    protected BoundingBox transformBoundingBox(BoundingBox boundingBox, Transform3D transform) {
        // Get the two opposite corners of the existing bounding box
        Point3f corner1 = boundingBox.getCorner1();
        Point3f corner2 = boundingBox.getCorner2();
        
        // Transform the two opposite corners to all eight corners of the box
        Point3f newCorner1 = new Point3f(corner1.x, corner1.y, corner1.z);
        Point3f newCorner2 = new Point3f(corner1.x, corner1.y, corner2.z);
        Point3f newCorner3 = new Point3f(corner1.x, corner2.y, corner1.z);
        Point3f newCorner4 = new Point3f(corner1.x, corner2.y, corner2.z);
        Point3f newCorner5 = new Point3f(corner2.x, corner1.y, corner1.z);
        Point3f newCorner6 = new Point3f(corner2.x, corner1.y, corner2.z);
        Point3f newCorner7 = new Point3f(corner2.x, corner2.y, corner1.z);
        Point3f newCorner8 = new Point3f(corner2.x, corner2.y, corner2.z);
        
        // Transform each of the eight corners
        transform.transform(newCorner1);
        transform.transform(newCorner2);
        transform.transform(newCorner3);
        transform.transform(newCorner4);
        transform.transform(newCorner5);
        transform.transform(newCorner6);
        transform.transform(newCorner7);
        transform.transform(newCorner8);
        
        // Create a new bounding box with one corner having the lowest of all
        // the coordinates of the transformed corners, and the other corner
        // having the highest of all the coordinates of the transformed corners
        Point3f resultCorner1 = new Point3f(Math.min(Math.min(Math.min(newCorner1.x, newCorner2.x), Math.min(newCorner3.x, newCorner4.x)), Math.min(Math.min(newCorner5.x, newCorner6.x), Math.min(newCorner7.x, newCorner8.x))),
            Math.min(Math.min(Math.min(newCorner1.y, newCorner2.y), Math.min(newCorner3.y, newCorner4.y)), Math.min(Math.min(newCorner5.y, newCorner6.y), Math.min(newCorner7.y, newCorner8.y))),
            Math.min(Math.min(Math.min(newCorner1.z, newCorner2.z), Math.min(newCorner3.z, newCorner4.z)), Math.min(Math.min(newCorner5.z, newCorner6.z), Math.min(newCorner7.z, newCorner8.z))));
        Point3f resultCorner2 = new Point3f(Math.max(Math.max(Math.max(newCorner1.x, newCorner2.x), Math.max(newCorner3.x, newCorner4.x)), Math.max(Math.max(newCorner5.x, newCorner6.x), Math.max(newCorner7.x, newCorner8.x))),
            Math.max(Math.max(Math.max(newCorner1.y, newCorner2.y), Math.max(newCorner3.y, newCorner4.y)), Math.max(Math.max(newCorner5.y, newCorner6.y), Math.max(newCorner7.y, newCorner8.y))),
            Math.max(Math.max(Math.max(newCorner1.z, newCorner2.z), Math.max(newCorner3.z, newCorner4.z)), Math.max(Math.max(newCorner5.z, newCorner6.z), Math.max(newCorner7.z, newCorner8.z))));
        return new BoundingBox(resultCorner1, resultCorner2);
    }
}