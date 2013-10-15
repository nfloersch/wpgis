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
public interface TileFactory extends Serializable {
    int getMaxHeight();
    Tile createTile(long seed, int x, int y);
    void applyTheme(long seed, Tile tile, int x, int y);
}