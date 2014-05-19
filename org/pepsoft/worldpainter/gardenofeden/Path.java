/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.gardenofeden;

import java.awt.Point;
import javax.vecmath.Point3i;
/**
 *
 * @author pepijn
 */
public abstract class Path extends Seed {
    public Path(Garden garden, long seed, Seed parent, Point3i location, int germinationTime, int category) {
        super(garden, seed, parent, location, germinationTime, category);
    }
}