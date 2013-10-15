/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.commands;

import org.pepsoft.worldpainter.World2;

/**
 *
 * @author pepijn
 */
public interface CommandContext {
    World2 getWorld();
    
    void setWorld(World2 world);
}