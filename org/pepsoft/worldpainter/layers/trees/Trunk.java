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
import org.pepsoft.worldpainter.gardenofeden.Garden;
import org.pepsoft.worldpainter.gardenofeden.Seed;
import org.pepsoft.worldpainter.util.GeometryUtil;

/**
 *
 * @author pepijn
 */
public class Trunk extends Branch {
    public Trunk(Garden garden, long seed, Seed parent, Point3i location, int germinationTime) {
        super(garden, seed, parent, location, germinationTime, 0, 0, 1.0f);
    }

    @Override
    protected boolean sprout() {
        GeometryUtil.visitFilledCircle((int) crossSection - 1, new GeometryUtil.GeometryVisitor() {
            @Override
            public boolean visit(int dx, int dy, float d) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        throw new UnsupportedOperationException();
    }
}