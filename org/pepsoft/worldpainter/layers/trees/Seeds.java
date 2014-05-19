/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

import java.util.Collections;
import java.util.List;
import javax.vecmath.Point3i;

/**
 *
 * @author pepijn
 */
public class Seeds {
    private Seeds() {
        // Prevent instantiation
    }
    
    public static final Seed oakTree() {
        return new Seed() {
            @Override
            public List<Crawly> spawn(Point3i coords) {
                return Collections.singletonList(Crawlies.trunk(coords));
            }
        };
    }
}