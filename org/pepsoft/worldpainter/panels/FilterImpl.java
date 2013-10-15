/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.panels;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.Layer.DataSize;
import org.pepsoft.worldpainter.operations.Filter;

/**
 *
 * @author pepijn
 */
public final class FilterImpl implements Filter {
    public FilterImpl(Dimension dimension, int aboveLevel, int belowLevel, boolean feather, Object replace, boolean replaceIsExcept) {
        this.dimension = dimension;
        this.aboveLevel = aboveLevel;
        this.belowLevel = belowLevel;
        if (aboveLevel >= 0) {
            checkLevel = true;
            if (belowLevel >= 0) {
                // Above and below are checked
                if (belowLevel >= aboveLevel) {
                    levelType = LevelType.BETWEEN;
                } else {
                    levelType = LevelType.OUTSIDE;
                }
            } else {
                // Only above checked
                levelType = LevelType.ABOVE;
            }
        } else if (belowLevel >= 0) {
            // Only below checked
            checkLevel = true;
            levelType = LevelType.BELOW;
        } else {
            // Neither checked
            checkLevel = false;
            levelType = null;
        }
        this.feather = feather;
        if (replace instanceof Terrain) {
            this.replace = true;
            objectType = ObjectType.TERRAIN;
            terrain = (Terrain) replace;
            layer = null;
            biome = -1;
        } else if (replace instanceof Layer) {
            this.replace = true;
            if ((((Layer) replace).getDataSize() == DataSize.BIT) || (((Layer) replace).getDataSize() == DataSize.BIT_PER_CHUNK)) {
                objectType = ObjectType.BIT_LAYER;
            } else {
                objectType = ObjectType.INT_LAYER;
            }
            terrain = null;
            layer = (Layer) replace;
            biome = -1;
        } else if (replace instanceof Integer) {
            this.replace = true;
            objectType = ObjectType.BIOME;
            terrain = null;
            layer = null;
            biome = (Integer) replace;
        } else {
            this.replace = false;
            objectType = null;
            terrain = null;
            layer = null;
            biome = -1;
        }
        this.replaceIsExcept = replaceIsExcept;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public boolean isCheckLevel() {
        return checkLevel;
    }

    public boolean isReplace() {
        return replace;
    }

    public boolean isFeather() {
        return feather;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public ObjectType getReplaceType() {
        return objectType;
    }

    public int getAboveLevel() {
        return aboveLevel;
    }

    public int getBelowLevel() {
        return belowLevel;
    }

    public int getBiome() {
        return biome;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public Layer getLayer() {
        return layer;
    }

    // Filter
    
    @Override
    public float modifyStrength(int x, int y, float strength) {
        if (strength > 0.0f) {
            if (replace) {
                switch (objectType) {
                    case BIOME:
                        if ((dimension.getLayerValueAt(Biome.INSTANCE, x, y) != biome) ^ replaceIsExcept) {
                            return 0.0f;
                        }
                        break;
                    case BIT_LAYER:
                        if ((!dimension.getBitLayerValueAt(layer, x, y)) ^ replaceIsExcept) {
                            return 0.0f;
                        }
                        break;
                    case INT_LAYER:
                        if ((dimension.getLayerValueAt(layer, x, y) == 0) ^ replaceIsExcept) {
                            return 0.0f;
                        }
                        break;
                    case TERRAIN:
                        if ((dimension.getTerrainAt(x, y) != terrain) ^ replaceIsExcept) {
                            return 0.0f;
                        }
                        break;
                }
            }
            if (checkLevel) {
                int terrainLevel = dimension.getIntHeightAt(x, y);
                switch (levelType) {
                    case ABOVE:
                        if (terrainLevel < aboveLevel) {
                            return feather ? Math.max((1 - (aboveLevel - terrainLevel) / 4.0f) * strength, 0.0f) : 0.0f;
                        }
                        break;
                    case BELOW:
                        if (terrainLevel > belowLevel) {
                            return feather ? Math.max((1 - (terrainLevel - belowLevel) / 4.0f) * strength, 0.0f) : 0.0f;
                        }
                        break;
                    case BETWEEN:
                        if ((terrainLevel < aboveLevel) || (terrainLevel > belowLevel)) {
                            return feather ? Math.max(Math.min((1 - (aboveLevel - terrainLevel) / 4.0f), (1 - (terrainLevel - belowLevel) / 4.0f)) * strength, 0.0f) : 0.0f;
                        }
                        break;
                    case OUTSIDE:
                        if ((terrainLevel > belowLevel) && (terrainLevel < aboveLevel)) {
                            return feather ? Math.max(Math.max((1 - (terrainLevel - belowLevel) / 4.0f), (1 - (aboveLevel - terrainLevel) / 4.0f)) * strength, 0.0f) : 0.0f;
                        }
                        break;
                }
            }
            return strength;
        } else {
            return 0.0f;
        }
    }
    final Dimension dimension;
    final boolean checkLevel;
    final boolean replace;
    final boolean feather;
    final LevelType levelType;
    final ObjectType objectType;
    final boolean replaceIsExcept;
    final int aboveLevel;
    final int belowLevel;
    final int biome;
    final Terrain terrain;
    final Layer layer;

    public enum LevelType {
        BETWEEN, OUTSIDE, ABOVE, BELOW
    }

    public enum ObjectType {
        TERRAIN, BIT_LAYER, INT_LAYER, BIOME
    }
}