/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import org.pepsoft.worldpainter.heightMaps.ConstantHeightMap;
import org.pepsoft.worldpainter.heightMaps.MaximisingHeightMap;
import org.pepsoft.worldpainter.heightMaps.NinePatchHeightMap;
import org.pepsoft.worldpainter.heightMaps.NoiseHeightMap;
import org.pepsoft.worldpainter.heightMaps.ProductHeightMap;
import org.pepsoft.worldpainter.heightMaps.SumHeightMap;
import org.pepsoft.worldpainter.themes.impl.fancy.FancyTheme;
import org.pepsoft.worldpainter.themes.SimpleTheme;

/**
 *
 * @author pepijn
 */
public final class TileFactoryFactory {
    private TileFactoryFactory() {
        // Prevent instantiation
    }
    
    public static HeightMapTileFactory createNoiseTileFactory(long seed, Terrain terrain, int maxHeight, int baseHeight, int waterLevel, boolean floodWithLava, boolean beaches, float range, double scale) {
        return new HeightMapTileFactory(seed, new SumHeightMap(new ConstantHeightMap(baseHeight), new NoiseHeightMap(range, scale, 1)), maxHeight, floodWithLava, SimpleTheme.createDefault(terrain, maxHeight, waterLevel, true, beaches));
    }
    
    public static HeightMapTileFactory createFlatTileFactory(long seed, Terrain terrain, int maxHeight, int height, int waterLevel, boolean floodWithLava, boolean beaches) {
        return new HeightMapTileFactory(seed, new ConstantHeightMap(height), maxHeight, floodWithLava, SimpleTheme.createDefault(terrain, maxHeight, waterLevel, false, beaches));
    }
    
    public static HeightMapTileFactory createFancyTileFactory(long seed) {
//        final HeightMapTileFactory tileFactory = TileFactoryFactory.createNoiseTileFactory(Terrain.GRASS, World2.DEFAULT_MAX_HEIGHT, 58, 62, false, true, 20.0f, 1.0);
        HeightMap oceanFloor = new ConstantHeightMap(40f);
        HeightMap continent;
//        continent = new NinePatchHeightMap(200, 100, 50, 58f);
        continent = new NinePatchHeightMap(0, 500, 50, 22f);
        HeightMap hills = new ProductHeightMap(
                new NoiseHeightMap(1.0f, 10f, 1),
                new SumHeightMap(
                    new NoiseHeightMap(20.0f, 1.0f, 2),
                    new ConstantHeightMap(-5f)));
        continent = new SumHeightMap(
                new SumHeightMap(
                    oceanFloor,
                    continent),
                hills);
        HeightMap mountainsLimit = new NinePatchHeightMap(0, 500, 200, 1f);
        HeightMap mountains = new ProductHeightMap(
            new ProductHeightMap(
                new NoiseHeightMap(1.0f, 10f, 1),
                mountainsLimit),
            new NoiseHeightMap(256f, 5f, 4));
        HeightMap heightMap = new MaximisingHeightMap(continent, mountains);
        return new HeightMapTileFactory(seed, heightMap, 256, false, new FancyTheme(256, 62, heightMap));
    }
}