/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.trees;

import java.util.List;
import javax.vecmath.Point3d;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;

/**
 *
 * @author pepijn
 */
public class Crawly {
    public Crawly(Point3d location, Brush brush, Modifier modifier, StopCondition stopCondition, Spawner spawner) {
        this.location = location;
        this.brush = brush;
        this.modifier = modifier;
        this.stopCondition = stopCondition;
        this.spawner = spawner;
    }
    
    public boolean crawl(MinecraftWorld world) {
        brush.paint(world, location);
        modifier.modify(this);
        return ! stopCondition.shouldStop(this);
    }
    
    public List<Crawly> spawn() {
        if (spawner != null) {
            return spawner.spawn(this);
        } else {
            return null;
        }
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public StopCondition getStopCondition() {
        return stopCondition;
    }

    public void setStopCondition(StopCondition stopCondition) {
        this.stopCondition = stopCondition;
    }

    public Spawner getSpawner() {
        return spawner;
    }

    public void setSpawner(Spawner spawner) {
        this.spawner = spawner;
    }

    public Point3d getLocation() {
        return location;
    }

    public void setLocation(Point3d location) {
        this.location = location;
    }

    public Brush getBrush() {
        return brush;
    }

    public void setBrush(Brush brush) {
        this.brush = brush;
    }
    
    private Point3d location;
    private Brush brush;
    private Modifier modifier;
    private StopCondition stopCondition;
    private Spawner spawner;
}