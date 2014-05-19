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
public class StopConditions {
    private StopConditions() {
        // Prevent instantiation
    }
    
    public static StopCondition steps(final int steps) {
        return new StopCondition() {
            @Override
            public boolean shouldStop(Crawly aThis) {
                remainingSteps--;
                return remainingSteps <= 0;
            }
            
            private int remainingSteps = steps;
        };
    }
}