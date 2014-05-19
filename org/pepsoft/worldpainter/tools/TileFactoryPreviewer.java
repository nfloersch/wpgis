/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JFrame;
import org.pepsoft.minecraft.Material;
import org.pepsoft.util.swing.TiledImageViewer;
import org.pepsoft.worldpainter.HeightMap;
//import org.pepsoft.worldpainter.ExperimentalTileFactory;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.WPTileProvider;
import org.pepsoft.worldpainter.colourschemes.DynMapColourScheme;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.HeightMapTileFactory;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.MixedMaterial.Row;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.TileRenderer;
import org.pepsoft.worldpainter.biomeschemes.AutoBiomeScheme;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import org.pepsoft.worldpainter.biomeschemes.Minecraft1_2BiomeScheme;
import org.pepsoft.worldpainter.heightMaps.ConstantHeightMap;
import org.pepsoft.worldpainter.heightMaps.MaximisingHeightMap;
import org.pepsoft.worldpainter.heightMaps.NinePatchHeightMap;
import org.pepsoft.worldpainter.heightMaps.NoiseHeightMap;
import org.pepsoft.worldpainter.heightMaps.ProductHeightMap;
import org.pepsoft.worldpainter.heightMaps.SumHeightMap;
import org.pepsoft.worldpainter.themes.impl.fancy.FancyTheme;

/**
 *
 * @author pepijn
 */
public class TileFactoryPreviewer {
    public static void main(String[] args) {
        final long seed;
        if (args.length > 0) {
            seed = Long.parseLong(args[0]);
        } else {
            seed = new Random().nextLong();
        }
//        final ExperimentalTileFactory tileFactory = new ExperimentalTileFactory(DEFAULT_MAX_HEIGHT_2);
//        final HeightMapTileFactory tileFactory = TileFactoryFactory.createNoiseTileFactory(Terrain.GRASS, World2.DEFAULT_MAX_HEIGHT, 58, 62, false, true, 20.0f, 1.0);
        HeightMap oceanFloor = new ConstantHeightMap(40f);
        HeightMap continent;
//        continent = new NinePatchHeightMap(200, 100, 50, 58f);
        continent = new NinePatchHeightMap(0, 1000, 50, 22f);
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
        HeightMap mountainsLimit = new NinePatchHeightMap(0, 1000, 200, 1f);
        HeightMap mountains = new ProductHeightMap(
            new ProductHeightMap(
                new NoiseHeightMap(1.0f, 10f, 1),
                mountainsLimit),
            new NoiseHeightMap(256f, 5f, 4));
        HeightMap heightMap = new MaximisingHeightMap(continent, mountains);
        final HeightMapTileFactory tileFactory = new HeightMapTileFactory(seed, heightMap, 256, false, new FancyTheme(256, 62, heightMap));
//        SortedMap<Integer, Terrain> terrainRanges = tileFactory.getTerrainRanges();
//        terrainRanges.clear();
//        terrainRanges.put( -1, Terrain.DIRT);
//        terrainRanges.put( 64, Terrain.GRASS);
//        terrainRanges.put(128, Terrain.ROCK);
//        terrainRanges.put(192, Terrain.DEEP_SNOW);
//        tileFactory.setTerrainRanges(terrainRanges);
        final org.pepsoft.worldpainter.TileProvider tileProvider = new org.pepsoft.worldpainter.TileProvider() {
            @Override
            public Tile getTile(int x, int y) {
                Point coords = new Point(x, y);
                synchronized (cache) {
                    Tile tile = cache.get(coords);
                    if (tile == null) {
                        tile = tileFactory.createTile(x, y);
                        cache.put(coords, tile);
                    }
                    return tile;
                }
            }
            
            private final Map<Point, Tile> cache = new HashMap<Point, Tile>();
        };
        Terrain.setCustomMaterial(0, new MixedMaterial("Dirt/Gravel", new Row[] {new Row(Material.DIRT, 750, 1.0f), new Row(Material.GRAVEL, 250, 1.0f)}, Minecraft1_2BiomeScheme.BIOME_PLAINS, false, 1.0f, null));
        Terrain.setCustomMaterial(1, new MixedMaterial("Stone/Gravel", new Row[] {new Row(Material.STONE, 750, 1.0f), new Row(Material.GRAVEL, 250, 1.0f)}, Minecraft1_2BiomeScheme.BIOME_PLAINS, false, 1.0f, null));
        TiledImageViewer viewer = new TiledImageViewer();
        JFrame frame = new JFrame("TileFactory Previewer");
        viewer.setTileProvider(new WPTileProvider(tileProvider, new DynMapColourScheme("default", true), new AutoBiomeScheme(null), null, Collections.singleton((Layer) Biome.INSTANCE), true, TileRenderer.LightOrigin.NORTHWEST));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(viewer, BorderLayout.CENTER);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
