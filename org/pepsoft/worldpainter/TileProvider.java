/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

/**
 *
 * @author pepijn
 */
public interface TileProvider {
    Tile getTile(int x, int y);
}