/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class NotPresent extends Layer {
    public NotPresent() {
        super("Not Present", "Marks chunks that were not present when the map was imported", Layer.DataSize.BIT_PER_CHUNK, 90);
    }
    
    public static final NotPresent INSTANCE = new NotPresent();

    private static final long serialVersionUID = 1L;
}