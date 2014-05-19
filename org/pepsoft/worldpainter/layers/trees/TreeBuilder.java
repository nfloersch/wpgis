/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.vecmath.Point3i;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;

/**
 *
 * @author pepijn
 */
public class TreeBuilder {
    public void plant(MinecraftWorld world, Point3i coords, Seed seed) {
        List<Crawly> crawlies = new LinkedList<Crawly>(seed.spawn(coords));
        List<Crawly> newCrawlies = new LinkedList<Crawly>();
        while (! crawlies.isEmpty()) {
            for (Iterator<Crawly> i = crawlies.iterator(); i.hasNext(); ) {
                Crawly crawly = i.next();
                if (! crawly.crawl(world)) {
                    i.remove();
                }
                List<Crawly> myNewCrawlies = crawly.spawn();
                if (myNewCrawlies != null) {
                    newCrawlies.addAll(myNewCrawlies);
                }
            }
            crawlies.addAll(newCrawlies);
            newCrawlies.clear();
        }
    }
}