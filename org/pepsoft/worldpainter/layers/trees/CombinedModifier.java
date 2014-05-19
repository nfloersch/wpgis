/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pepijn
 */
public class CombinedModifier extends Modifier {
    public CombinedModifier() {
        // Do nothing
    }
    
    public CombinedModifier(Modifier modifier1, Modifier modifier2) {
        modifiers.add(modifier1);
        modifiers.add(modifier2);
    }

    @Override
    public void modify(Crawly crawly) {
        for (Modifier modifier: modifiers) {
            modifier.modify(crawly);
        }
    }

    @Override
    public CombinedModifier and(Modifier modifier) {
        if (modifier instanceof CombinedModifier) {
            modifiers.addAll(((CombinedModifier) modifiers).modifiers);
        } else {
            modifiers.add(modifier);
        }
        return this;
    }
    
    private final List<Modifier> modifiers = new ArrayList<Modifier>();
}