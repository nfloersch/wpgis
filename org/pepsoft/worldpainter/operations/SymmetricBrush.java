/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.util.ObjectUtils;

/**
 * An object which calculates brush strengths for simple mathematical brushes,
 * which must be symmetric in both the x and y axes (so that only one quadrant
 * of the brush needs to be calculated).
 *
 * @author pepijn
 */
public abstract class SymmetricBrush extends AbstractBrush {
    public SymmetricBrush(String name, boolean rotationallySymmetric) {
        super(name);
        this.rotationallySymmetric = rotationallySymmetric;
    }

    @Override
    public BrushShape getBrushShape() {
        return brushShape;
    }

    public void setBrushShape(BrushShape brushShape) {
        if (brushShape != this.brushShape) {
            this.brushShape = brushShape;
            cacheStrengths();
        }
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public void setRadius(int radius) {
        if (radius != this.radius) {
            this.radius = radius;
            cacheStrengths();
        }
    }

    @Override
    public float getLevel() {
        return level;
    }

    @Override
    public void setLevel(float level) {
        if (level != this.level) {
            this.level = level;
            cacheStrengths();
        }
    }

    public final boolean isRotationallySymmetric() {
        return rotationallySymmetric;
    }

    @Override
    public final float getStrength(int centerX, int centerY, int x, int y) {
        int dx = Math.abs(x - centerX), dy = Math.abs(y - centerY);
        return strengthCache[dx][dy];
    }

    @Override
    public final float getStrength(int dx, int dy) {
        return strengthCache[Math.abs(dx)][Math.abs(dy)];
    }
    
    @Override
    public final float getFullStrength(int dx, int dy) {
        return fullStrengthCache[Math.abs(dx)][Math.abs(dy)];
    }
    
    @Override
    public String toString() {
        return getName() + " (radius=" + radius + ", brushShape=" + brushShape + ", level=" + level + ')';
    }

    @Override
    public SymmetricBrush clone() {
        SymmetricBrush clone = (SymmetricBrush) super.clone();
        if (strengthCache != null) {
            clone.strengthCache = ObjectUtils.clone(strengthCache);
        }
        if (fullStrengthCache != null) {
            if (fullStrengthCache == strengthCache) {
                clone.fullStrengthCache = clone.strengthCache;
            } else {
                clone.fullStrengthCache = ObjectUtils.clone(fullStrengthCache);
            }
        }
        return clone;
    }
    
    protected abstract float calcStrength(int dx, int dy);
    
    private void cacheStrengths() {
        if ((brushShape != null) && ((radius != cachedRadius) || (brushShape != cachedBrushshape) || (level != cachedLevel))) {
            // No need to allocate a new array if there is one already and the
            // data will fit in it:
            if ((strengthCache == null) || (strengthCache.length < (radius + 1))) {
                strengthCache = new float[radius + 1][radius + 1];
            }
            // Only update the full strength cache if necessary
            if ((radius != cachedRadius) || (brushShape != cachedBrushshape)) {
                // No need to allocate a new array if there is one already and the
                // data will fit in it:
                if ((fullStrengthCache == null) || (fullStrengthCache.length < (radius + 1))) {
                    fullStrengthCache = new float[radius + 1][radius + 1];
                }
                for (int dx = 0; dx <= radius; dx++) {
                    for (int dy = 0; dy <= radius; dy++) {
                        float strength = calcStrength(dx, dy);
                        strengthCache[dx][dy] = strength * level;
                        fullStrengthCache[dx][dy] = strength;
                    }
                }
            } else {
                for (int dx = 0; dx <= radius; dx++) {
                    for (int dy = 0; dy <= radius; dy++) {
                        strengthCache[dx][dy] = calcStrength(dx, dy) * level;
                    }
                }
            }
            cachedRadius = radius;
            cachedBrushshape = brushShape;
            cachedLevel = level;
        }
    }

    private final boolean rotationallySymmetric;
    private int radius, cachedRadius;
    private BrushShape brushShape, cachedBrushshape;
    private float[][] strengthCache, fullStrengthCache;
    private float level = 1.0f, cachedLevel = 1.0f;

    public static final SymmetricBrush CONSTANT_CIRCLE = new RadialBrush("Constant Circle", true) {
        {
            setBrushShape(BrushShape.CIRCLE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return 1.0f;
        }
    };

    public static final SymmetricBrush CONSTANT_SQUARE = new RadialBrush("Constant Square", false) {
        {
            setBrushShape(BrushShape.SQUARE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return 1.0f;
        }
    };
    
    public static final SymmetricBrush LINEAR_CIRCLE = new RadialBrush("Linear Circle", true) {
        {
            setBrushShape(BrushShape.CIRCLE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return 1.0f - dr;
        }
    };

    public static final SymmetricBrush LINEAR_SQUARE = new RadialBrush("Linear Square", false) {
        {
            setBrushShape(BrushShape.SQUARE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return 1.0f - dr;
        }
    };

    public static final SymmetricBrush COSINE_CIRCLE = new RadialBrush("Sine Circle", true) {
        {
            setBrushShape(BrushShape.CIRCLE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return (float) Math.cos(dr * Math.PI) / 2 + 0.5f;
        }
    };
    
    public static final SymmetricBrush COSINE_SQUARE = new RadialBrush("Sine Square", false) {
        {
            setBrushShape(BrushShape.SQUARE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return (float) Math.cos(dr * Math.PI) / 2 + 0.5f;
        }
    };

    public static final SymmetricBrush PLATEAU_CIRCLE = new RadialBrush("Plateau Circle", true) {
        {
            setBrushShape(BrushShape.CIRCLE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            if (dr <= 0.5f) {
                return 1.0f;
            } else {
                return (float) Math.cos((dr - 0.5f) * TWO_PI) / 2 + 0.5f;
            }
        }
    };
    
    public static final SymmetricBrush PLATEAU_SQUARE = new RadialBrush("Plateau Square", false) {
        {
            setBrushShape(BrushShape.SQUARE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            if (dr <= 0.5f) {
                return 1.0f;
            } else {
                return (float) Math.cos((dr - 0.5f) * TWO_PI) / 2 + 0.5f;
            }
        }
    };

    public static final SymmetricBrush SPIKE_CIRCLE = new RadialBrush("Spike Circle", true) {
        {
            setBrushShape(BrushShape.CIRCLE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return (1.0f - dr) * (1.0f - dr);
        }
    };

    public static final SymmetricBrush SPIKE_SQUARE = new RadialBrush("Spike Square", false) {
        {
            setBrushShape(BrushShape.SQUARE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return (1.0f - dr) * (1.0f - dr);
        }
    };

    public static final SymmetricBrush DOME_CIRCLE = new RadialBrush("Dome Circle", true) {
        {
            setBrushShape(BrushShape.CIRCLE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return (float) Math.sqrt(1 - dr * dr);
        }
    };

    public static final SymmetricBrush DOME_SQUARE = new RadialBrush("Dome Square", false) {
        {
            setBrushShape(BrushShape.SQUARE);
        }
        
        @Override
        protected float calcStrength(float dr) {
            return (float) Math.sqrt(1 - dr * dr);
        }
    };
    
    private static final float TWO_PI = (float) (Math.PI * 2);
}