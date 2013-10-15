/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.renderers;

import org.pepsoft.worldpainter.Constants;

/**
 *
 * @author pepijn
 */
public class VoidRenderer implements BitLayerRenderer {
    public int getPixelColour(int x, int y, int underlyingColour, boolean value) {
        return value ? Constants.VOID_COLOUR : underlyingColour;
    }
}