/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.heightMaps;

/**
 *
 * @author pepijn
 */
public final class NinePatchHeightMap extends AbstractHeightMap {
    public NinePatchHeightMap(int innerSize, int borderSize, int coastSize, float height) {
        if ((innerSize < 0) || (borderSize < 0) || (coastSize < 0) || (height <= 0.0f)) {
            throw new IllegalArgumentException();
        }
        if ((innerSize == 0) && (borderSize == 0) && (coastSize == 0)) {
            throw new IllegalArgumentException();
        }
        this.innerSize = innerSize;
        this.borderSize = borderSize;
        this.coastSize = coastSize;
        this.height = height;
        borderTotal = innerSize + borderSize;
        coastTotal = borderTotal + coastSize;
    }

    @Override
    public float getHeight(int x, int y) {
        x = Math.abs(x);
        y = Math.abs(y);
        if (x < innerSize) {
            if (y < innerSize) {
                // On the continent
                return height;
            } else if (y < borderTotal) {
                // Border
                return height;
            } else if (y < coastTotal) {
                // Coast
                return (float) (Math.cos((y - borderTotal) / coastSize * Math.PI) * height);
            } else {
                // Outside the continent
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public float getBaseHeight() {
        return 0.0f;
    }
    
    private final int innerSize, borderSize, coastSize;
    private final int borderTotal, coastTotal;
    private final float height;
    
    private static final long serialVersionUID = 1L;
}