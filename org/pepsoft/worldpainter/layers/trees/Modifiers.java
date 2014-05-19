/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

import javax.vecmath.Vector3d;

/**
 *
 * @author pepijn
 */
public class Modifiers {
    private Modifiers() {
        // Prevent instantiation
    }
    
    public static Modifier rise() {
        return new Modifier() {
            @Override
            public void modify(Crawly crawly) {
                crawly.getLocation().add(new Vector3d(0.0, 0.0, 1.0));
            }
        };
    }

    public static Modifier fall() {
        return new Modifier() {
            @Override
            public void modify(Crawly crawly) {
                crawly.getLocation().add(new Vector3d(0.0, 0.0, -1.0));
            }
        };
    }
}