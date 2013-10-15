/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Biome extends Layer {
    public Biome() {
        super("Biome", "Displays the biome Minecraft will generate", Layer.DataSize.BYTE, 70);
    }
    
    public static final Biome INSTANCE = new Biome();
    
    private static final long serialVersionUID = -5510962172433402363L;
}