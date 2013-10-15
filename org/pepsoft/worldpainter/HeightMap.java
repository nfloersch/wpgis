/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.io.Serializable;

/**
 *
 * @author pepijn
 */
public interface HeightMap extends Serializable {
    long getSeed();
    
    void setSeed(long seed);
    
    float getHeight(int x, int y);
    
    float getBaseHeight();
}