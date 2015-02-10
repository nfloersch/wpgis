/*
 * Copyright (C) 2014 pepijn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.pepsoft.worldpainter.layers.trees;

import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.gardenofeden.Garden;
import org.pepsoft.worldpainter.gardenofeden.Seed;
import org.pepsoft.worldpainter.layers.GardenCategory;

/**
 *
 * @author pepijn
 */
public class Branch extends Seed {
    public Branch(Garden garden, long seed, Seed parent, Point3i location, int germinationTime, int distanceFromOrigin, int generation, float crossSection) {
        super(garden, seed, parent, location, germinationTime, GardenCategory.CATEGORY_TREE);
        this.distanceFromOrigin = distanceFromOrigin;
        this.generation = generation;
        this.crossSection = crossSection;
    }

    public int getDistanceFromOrigin() {
        return distanceFromOrigin;
    }

    public int getGeneration() {
        return generation;
    }

    // Seed
    
    @Override
    protected boolean sprout() {
        // TODO
        return false;
    }

    @Override
    public void buildFirstPass(Dimension dimension, Tile tile, MinecraftWorld minecraftWorld) {
        boolean hollow = getTree().isHollow();
    }

    protected Tree getTree() {
        Seed tree = this.parent;
        while ((tree != null) && (! (tree instanceof Tree))) {
            tree = tree.parent;
        }
        return (Tree) tree;
    }
    
    protected final int distanceFromOrigin, generation;
    protected float crossSection;
    protected Vector3f direction;
}