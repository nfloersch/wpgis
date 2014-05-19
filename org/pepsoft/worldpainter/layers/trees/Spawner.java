/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

import java.util.List;

/**
 *
 * @author pepijn
 */
public abstract class Spawner {
    public abstract List<Crawly> spawn(Crawly crawly);
}