/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.heightMaps.gui;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import org.pepsoft.util.MathUtils;
import org.pepsoft.util.swing.TileListener;
import org.pepsoft.util.swing.TileProvider;
import org.pepsoft.worldpainter.HeightMap;

/**
 *
 * @author pepijn
 */
public class HeightMapTileProvider implements TileProvider {
    public HeightMapTileProvider(HeightMap heightMap) {
        if (heightMap == null) {
            throw new NullPointerException();
        }
        this.heightMap = heightMap;
    }

    public HeightMap getHeightMap() {
        return heightMap;
    }
    
    // TileProvider
    
    @Override
    public int getTileSize() {
        return 128;
    }

    @Override
    public BufferedImage getTile(int x, int y) {
        final BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_BYTE_GRAY);
        final WritableRaster raster = image.getRaster();
        final int xOffset = x << 7, yOffset = y << 7;
        for (int dx = 0; dx < 128; dx++) {
            for (int dy = 0; dy < 128; dy++) {
                raster.setSample(dx, dy, 0, MathUtils.clamp(0, (int) (heightMap.getHeight(xOffset + dx, yOffset + dy) + 0.5f), 255));
            }
        }
        return image;
    }

    @Override
    public int getTilePriority(int x, int y) {
        return 0;
    }

    @Override
    public Rectangle getExtent() {
        return null;
    }

    @Override
    public void addTileListener(TileListener tileListener) {
        // Do nothing
    }

    @Override
    public void removeTileListener(TileListener tileListener) {
        // Do nothing
    }

    @Override
    public boolean isZoomSupported() {
        return false;
    }

    @Override
    public int getZoom() {
        return 0;
    }

    @Override
    public void setZoom(int zoom) {
        if (zoom != 0) {
            throw new UnsupportedOperationException("Not supported");
        }
    }
    
    private final HeightMap heightMap;
}