/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.operations;

/**
 * An operation which changes something which could potentially affect the
 * default biome, and which can set the biome to the default whenever it is
 * applied.
 * 
 * @author pepijn
 */
public interface AutoBiomeOperation extends Operation {
    boolean isAutoBiomesEnabled();
    void setAutoBiomesEnabled(boolean autoBiomesEnabled);
}