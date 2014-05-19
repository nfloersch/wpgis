/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

/**
 *
 * @author pepijn
 */
public abstract class Modifier {
    public CombinedModifier and(Modifier modifier) {
        if (modifier instanceof CombinedModifier) {
            return modifier.and(this);
        } else {
            return new CombinedModifier(this, modifier);
        }
    }

    public abstract void modify(Crawly aThis);
}