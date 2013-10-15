/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.renderers;

import org.pepsoft.util.ColourUtils;

/**
 *
 * @author pepijn
 */
public class TransparentColourRenderer implements NibbleLayerRenderer, BitLayerRenderer {
    public TransparentColourRenderer(int colour) {
        this.colour = colour;
    }

    @Override
    public int getPixelColour(int x, int y, int underlyingColour, int value) {
        if (value > 0) {
            int intensity = value * 255 / 15;
            return ColourUtils.mix(colour, underlyingColour, intensity);
        } else {
            return underlyingColour;
        }
    }

    @Override
    public int getPixelColour(int x, int y, int underlyingColour, boolean value) {
        return value ? colour : underlyingColour;
    }

    private final int colour;
}