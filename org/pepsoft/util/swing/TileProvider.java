/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util.swing;

import java.awt.image.BufferedImage;

/**
 *
 * @author pepijn
 */
public interface TileProvider {
    /**
     * Get the width and height of tiles (which must be square) in pixels. The
     * size must be a power of two!
     * 
     * @return The size in pixels of a tile.
     */
    int getTileSize();
    
    /**
     * Get the tile at the specified coordinates. The X coordinate increases to
     * the right and the Y coordinate increases towards the bottom.
     * 
     * @param x The X coordinate (in tiles) of the tile to get.
     * @param y The Y coordinate (in tiles) of the tile to get.
     * @return The specified tile.
     */
    BufferedImage getTile(int x, int y);
    
    /**
     * Register a tile listener which will be notified if the contents of a 
     * tile change.
     * 
     * @param tileListener The tile listener to register.
     */
    void addTileListener(TileListener tileListener);
    
    /**
     * Remove a previously registered tile listener.
     * 
     * @param tileListener The tile listener to remove.
     */
    void removeTileListener(TileListener tileListener);
    
    int getZoom();
    
    void setZoom(int zoom);
}