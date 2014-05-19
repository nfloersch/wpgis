/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

import java.util.Random;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.worldpainter.MixedMaterial;

/**
 *
 * @author pepijn
 */
public class Crawlies {
    private Crawlies() {
        // Prevent instantiation
    }
    
    public static synchronized Crawly trunk(Point3i coords) {
        return new Crawly(new Point3d(coords.x, coords.y, coords.z), new Brush(MixedMaterial.create(Constants.BLK_WOOD), Brush.Shape.SPHERE, new Vector3d(1.0, 1.0, 1.0)), Modifiers.rise(), StopConditions.steps(random.nextInt(5) + 5), null);
    }
    
    private static final Random random = new Random();
}