/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Tunnels extends Layer {
    private Tunnels() {
        super("org.pepsoft.Tunnels", "Tunnels", "Generate round winding underground tunnels", DataSize.NIBBLE, 22);
    }

    public static final Tunnels INSTANCE = new Tunnels();
    
    private static final long serialVersionUID = 1L;
}