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
import org.pepsoft.util.swing.TiledImageViewer;
//import org.pepsoft.worldpainter.ExperimentalTileFactory;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.WPTileProvider;
import org.pepsoft.worldpainter.colourschemes.DynMapColourScheme;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.HeightMapTileFactory;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.TileFactoryFactory;
import org.pepsoft.worldpainter.TileRenderer;
import org.pepsoft.worldpainter.World2;

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
        final HeightMapTileFactory tileFactory = TileFactoryFactory.createNoiseTileFactory(Terrain.GRASS, World2.DEFAULT_MAX_HEIGHT, 58, 62, false, true, 20.0f, 1.0);
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
                        tile = tileFactory.createTile(seed, x, y);
                        cache.put(coords, tile);
                    }
                    return tile;
                }
            }
            
            private final Map<Point, Tile> cache = new HashMap<Point, Tile>();
        };
        TiledImageViewer viewer = new TiledImageViewer();
        viewer.setTileProvider(new WPTileProvider(tileProvider, new DynMapColourScheme("default"), null, Collections.singleton((Layer) Biome.INSTANCE), true, TileRenderer.LightOrigin.NORTHWEST));
        JFrame frame = new JFrame("TileFactory Previewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(viewer, BorderLayout.CENTER);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
