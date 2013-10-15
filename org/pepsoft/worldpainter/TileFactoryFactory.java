/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import org.pepsoft.worldpainter.heightMaps.ConstantHeightMap;
import org.pepsoft.worldpainter.heightMaps.NoiseHeightMap;
import org.pepsoft.worldpainter.heightMaps.SumHeightMap;

/**
 *
 * @author pepijn
 */
public final class TileFactoryFactory {
    private TileFactoryFactory() {
        // Prevent instantiation
    }
    
    public static HeightMapTileFactory createNoiseTileFactory(Terrain terrain, int maxHeight, int baseHeight, int waterLevel, boolean floodWithLava, boolean beaches, float range, double scale) {
        return new HeightMapTileFactory(new SumHeightMap(new ConstantHeightMap(baseHeight), new NoiseHeightMap(range, scale)), maxHeight, HeightMapTileFactory.createDefaultTerrainMap(terrain, maxHeight, baseHeight), waterLevel, floodWithLava, true, beaches);
    }
    
    public static HeightMapTileFactory createFlatTileFactory(Terrain terrain, int maxHeight, int height, int waterLevel, boolean floodWithLava, boolean beaches) {
        return new HeightMapTileFactory(new ConstantHeightMap(height), maxHeight, HeightMapTileFactory.createDefaultTerrainMap(terrain, maxHeight, height), waterLevel, floodWithLava, false, beaches);
    }
}