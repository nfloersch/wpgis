package org.pepsoft.worldpainter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.pepsoft.minecraft.Material;
import org.pepsoft.util.MathUtils;
import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.heightMaps.NoiseHeightMap;

/**
 * @author SchmitzP
 */
public class MixedMaterial implements Serializable {
    /**
     * Create a new "mixed material" which contains only one material.
     * 
     * @param name The name of the mixed material.
     * @param row A single row describing the material.
     * @param biome The default biome associated with this mixed material, or -1
     *     for no default biome.
     * @param colour The colour associated with this mixed material, or
     *     <code>null</code> for no default colour.
     */
    public MixedMaterial(final String name, final Row row, final int biome, final Integer colour) {
        this(name, new Row[] {row}, biome, Mode.SIMPLE, 1.0f, colour, null, 0, 0, false);
    }
    
    /**
     * Create a new noisy mixed material.
     * 
     * @param name The name of the mixed material.
     * @param rows The rows describing the materials to be used together with
     *     their occurrences.
     * @param biome The default biome associated with this mixed material, or -1
     *     for no default biome.
     * @param colour The colour associated with this mixed material, or
     *     <code>null</code> for no default colour.
     */
    public MixedMaterial(final String name, final Row[] rows, final int biome, final Integer colour) {
        this(name, rows, biome, Mode.NOISE, 1.0f, colour, null, 0, 0, false);
    }

    /**
     * Create a new blobby mixed material.
     * 
     * @param name The name of the mixed material.
     * @param rows The rows describing the materials to be used together with
     *     their occurrences.
     * @param biome The default biome associated with this mixed material, or -1
     *     for no default biome.
     * @param colour The colour associated with this mixed material, or
     *     <code>null</code> for no default colour.
     * @param scale The scale of the blobs. <code>1.0f</code> for default size.
     */
    public MixedMaterial(final String name, final Row[] rows, final int biome, final Integer colour, final float scale) {
        this(name, rows, biome, Mode.BLOBS, scale, colour, null, 0, 0, false);
    }

    /**
     * Create a new layered mixed material.
     * 
     * @param name The name of the mixed material.
     * @param rows The rows describing the materials to be used together with
     *     their heights.
     * @param biome The default biome associated with this mixed material, or -1
     *     for no default biome.
     * @param colour The colour associated with this mixed material, or
     *     <code>null</code> for no default colour.
     * @param variation The variation in layer height which should be applied,
     *     or <code>null</code> for no variation.
     * @param layerXSlope The slope of the layer for the x-axis.
     *     Must be zero if <code>repeat</code> is false.
     * @param layerYSlope The slope of the layer for the y-axis.
     *     Must be zero if <code>repeat</code> is false.
     * @param repeat Whether the layers should repeat vertically.
     */
    public MixedMaterial(final String name, final Row[] rows, final int biome, final Integer colour, final NoiseSettings variation, final double layerXSlope, final double layerYSlope, final boolean repeat) {
        this(name, rows, biome, Mode.LAYERED, 1.0f, colour, variation, layerXSlope, layerYSlope, repeat);
    }
    
    private MixedMaterial(final String name, final Row[] rows, final int biome, final Mode mode, final float scale, final Integer colour, final NoiseSettings variation, final double layerXSlope, final double layerYSlope, final boolean repeat) {
        if ((mode != Mode.LAYERED) && (mode != Mode.SIMPLE)) {
            int total = 0;
            for (Row row: rows) {
                total += row.occurrence;
            }
            if (total != 1000) {
                throw new IllegalArgumentException("Total occurrence is not 1000");
            }
        }
        this.name = name;
        this.rows = rows;
        this.biome = biome;
        this.mode = mode;
        this.scale = scale;
        this.colour = colour;
        this.variation = variation;
        this.layerXSlope = layerXSlope;
        this.layerYSlope = layerYSlope;
        this.repeat = repeat;
        init();
    }

    public String getName() {
        return name;
    }

    public int getBiome() {
        return biome;
    }

    public Mode getMode() {
        return mode;
    }

    public float getScale() {
        return scale;
    }

    public Integer getColour() {
        return colour;
    }

    public NoiseSettings getVariation() {
        return variation;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public double getLayerXSlope() {
        return layerXSlope;
    }

    public double getLayerYSlope() {
        return layerYSlope;
    }

    public BufferedImage getIcon(ColourScheme colourScheme) {
        final BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        // Draw the border
        for (int i = 0; i < 15; i++) {
            icon.setRGB(     i,      0, 0);
            icon.setRGB(    15,      i, 0);
            icon.setRGB(15 - i,     15, 0);
            icon.setRGB(     0, 15 - i, 0);
        }
        // Draw the terrain
        if (colour != null) {
            for (int x = 1; x < 15; x++) {
                for (int y = 1; y < 15; y++) {
                    icon.setRGB(x, y, colour);
                }
            }
        } else {
            for (int x = 1; x < 15; x++) {
                for (int y = 1; y < 15; y++) {
                    icon.setRGB(x, y, colourScheme.getColour(getMaterial(0, x, 0, y)));
                }
            }
        }
        return icon;
    }
    
    public Material getMaterial(long seed, int x, int y, float z) {
        switch (mode) {
            case SIMPLE:
                return simpleMaterial;
            case NOISE:
                return materials[random.nextInt(1000)];
            case BLOBS:
                double xx = x / Constants.TINY_BLOBS, yy = y / Constants.TINY_BLOBS, zz = z / Constants.TINY_BLOBS;
                if (seed + 1 != noiseGenerators[0].getSeed()) {
                    for (int i = 0; i < noiseGenerators.length; i++) {
                        noiseGenerators[i].setSeed(seed + i + 1);
                    }
                }
                Material material = sortedRows[sortedRows.length - 1].material;
                for (int i = noiseGenerators.length - 1; i >= 0; i--) {
                    final float rowScale = sortedRows[i].scale * this.scale;
                    if (noiseGenerators[i].getPerlinNoise(xx / rowScale, yy / rowScale, zz / rowScale) >= sortedRows[i].chance) {
                        material = sortedRows[i].material;
                    }
                }
                return material;
            case LAYERED:
                if (layerNoiseheightMap != null) {
                    if (layerNoiseheightMap.getSeed() != seed) {
                        layerNoiseheightMap.setSeed(seed);
                    }
                    z += layerNoiseheightMap.getValue(x, y, z) - layerNoiseOffset;
                }
                if (repeat) {
                    if (layerXSlope != 0.0) {
                        z += layerXSlope * x;
                    }
                    if (layerYSlope != 0.0) {
                        z += layerYSlope * y;
                    }
                    return materials[MathUtils.mod((int) (z + 0.5f), materials.length)];
                } else {
                    final int iZ = (int) (z + 0.5f);
                    if (iZ < 0) {
                        return materials[0];
                    } else if (iZ >= materials.length) {
                        return materials[materials.length - 1];
                    } else {
                        return materials[iZ];
                    }
                }
            default:
                throw new InternalError();
        }
    }
    
    public Material getMaterial(long seed, int x, int y, int z) {
        switch (mode) {
            case SIMPLE:
                return simpleMaterial;
            case NOISE:
                return materials[random.nextInt(1000)];
            case BLOBS:
                double xx = x / Constants.TINY_BLOBS, yy = y / Constants.TINY_BLOBS, zz = z / Constants.TINY_BLOBS;
                if (seed + 1 != noiseGenerators[0].getSeed()) {
                    for (int i = 0; i < noiseGenerators.length; i++) {
                        noiseGenerators[i].setSeed(seed + i + 1);
                    }
                }
                Material material = sortedRows[sortedRows.length - 1].material;
                for (int i = noiseGenerators.length - 1; i >= 0; i--) {
                    final float rowScale = sortedRows[i].scale * this.scale;
                    if (noiseGenerators[i].getPerlinNoise(xx / rowScale, yy / rowScale, zz / rowScale) >= sortedRows[i].chance) {
                        material = sortedRows[i].material;
                    }
                }
                return material;
            case LAYERED:
                float fZ = z;
                if (layerNoiseheightMap != null) {
                    if (layerNoiseheightMap.getSeed() != seed) {
                        layerNoiseheightMap.setSeed(seed);
                    }
                    fZ += layerNoiseheightMap.getValue(x, y, z) - layerNoiseOffset;
                }
                if (repeat) {
                    if (layerXSlope != 0.0) {
                        fZ += layerXSlope * x;
                    }
                    if (layerYSlope != 0.0) {
                        fZ += layerYSlope * y;
                    }
                    return materials[MathUtils.mod((int) (fZ + 0.5f), materials.length)];
                } else {
                    final int iZ = (int) (fZ + 0.5f);
                    if (iZ < 0) {
                        return materials[0];
                    } else if (iZ >= materials.length) {
                        return materials[materials.length - 1];
                    } else {
                        return materials[iZ];
                    }
                }
            default:
                throw new InternalError();
        }
    }

    public Row[] getRows() {
        return Arrays.copyOf(rows, rows.length);
    }
    
    // java.lang.Object
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.biome;
        hash = 19 * hash + Arrays.deepHashCode(this.rows);
        hash = 19 * hash + mode.hashCode();
        hash = 19 * hash + Float.floatToIntBits(this.scale);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MixedMaterial other = (MixedMaterial) obj;
        if (this.biome != other.biome) {
            return false;
        }
        if (!Arrays.deepEquals(this.rows, other.rows)) {
            return false;
        }
        if (this.mode != other.mode) {
            return false;
        }
        if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(other.scale)) {
            return false;
        }
        return true;
    }

    /**
     * Utility method for creating a simple mixed material, consisting of one
     * block type with data value 0.
     * 
     * @param blockType The block type the mixed material should consist of
     * @return A new mixed material with the specified block type, and the
     *     block type's name
     */
    public static MixedMaterial create(final int blockType) {
        return create(Material.get(blockType));
    }

    /**
     * Utility method for creating a simple mixed material, consisting of one
     * material.
     * 
     * @param material The simple material the mixed material should consist of
     * @return A new mixed material with the specified material and an
     *     appropriate name
     */
    public static MixedMaterial create(final Material material) {
        return new MixedMaterial(material.toString(), new Row(material, 1000, 1.0f), -1, null);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Legacy
        if (mode == null) {
            if (rows.length == 1) {
                mode = Mode.SIMPLE;
            } else if (noise) {
                mode = Mode.NOISE;
            } else {
                mode = Mode.BLOBS;
            }
        }
        
        init();
    }
    
    private void init() {
        switch (mode) {
            case SIMPLE:
                if (rows.length != 1) {
                    throw new IllegalArgumentException("Only one row allowed for SIMPLE mode");
                }
                simpleMaterial = rows[0].material;
                break;
            case NOISE:
                if (rows.length < 2) {
                    throw new IllegalArgumentException("Multiple rows required for NOISE mode");
                }
                materials = new Material[1000];
                int index = 0;
                for (Row row: rows) {
                    for (int i = 0; i < row.occurrence; i++) {
                        materials[index++] = row.material;
                    }
                }
                random = new Random();
                break;
            case BLOBS:
                if (rows.length < 2) {
                    throw new IllegalArgumentException("Multiple rows required for BLOBS mode");
                }
                sortedRows = Arrays.copyOf(rows, rows.length);
                Arrays.sort(sortedRows, new Comparator<Row>() {
                    @Override
                    public int compare(final Row r1, final Row r2) {
                        return r1.occurrence - r2.occurrence;
                    }
                });
                noiseGenerators = new PerlinNoise[rows.length - 1];
                int cumulativePermillage = 0;
                for (int i = 0; i < noiseGenerators.length; i++) {
                    noiseGenerators[i] = new PerlinNoise(0);
                    cumulativePermillage += sortedRows[i].occurrence * (1000 - cumulativePermillage) / 1000;
                    sortedRows[i].chance = PerlinNoise.getLevelForPromillage(cumulativePermillage);
                }
                break;
            case LAYERED:
                if (rows.length < 2) {
                    throw new IllegalArgumentException("Multiple rows required for LAYERED mode");
                }
                if ((! repeat) && ((layerXSlope != 0) || (layerYSlope != 0))) {
                    throw new IllegalArgumentException("Angle may not be non-zero if repeat is false");
                }
                List<Material> tmpMaterials = new ArrayList<Material>(org.pepsoft.minecraft.Constants.DEFAULT_MAX_HEIGHT_2);
                for (int i = rows.length - 1; i >= 0; i--) {
                    for (int j = 0; j < rows[i].occurrence; j++) {
                        tmpMaterials.add(rows[i].material);
                    }
                }
                materials = tmpMaterials.toArray(new Material[tmpMaterials.size()]);
                if (variation != null) {
                    layerNoiseheightMap = new NoiseHeightMap(variation.getRange() * 2, variation.getScale() / 5, variation.getRoughness() + 1, NOISE_SEED_OFFSET);
                    layerNoiseOffset = variation.getRange();
                } else {
                    layerNoiseheightMap = null;
                    layerNoiseOffset = 0;
                }
                break;
        }
    }

    private final String name;
    private final int biome;
    private final Row[] rows;
    @Deprecated
    private final boolean noise = false;
    private final float scale;
    private final Integer colour;
    private Mode mode = Mode.BLOBS;
    private final NoiseSettings variation;
    private final boolean repeat;
    private final double layerXSlope, layerYSlope;
    private transient Row[] sortedRows;
    private transient PerlinNoise[] noiseGenerators;
    private transient Material[] materials;
    private transient Random random;
    private transient Material simpleMaterial;
    private transient NoiseHeightMap layerNoiseheightMap;
    private transient int layerNoiseOffset;

    public static class Row implements Serializable {
        public Row(Material material, int occurrence, float scale) {
            this.material = material;
            this.occurrence = occurrence;
            this.scale = scale;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + (this.material != null ? this.material.hashCode() : 0);
            hash = 23 * hash + this.occurrence;
            hash = 23 * hash + Float.floatToIntBits(this.scale);
            hash = 23 * hash + Float.floatToIntBits(this.chance);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Row other = (Row) obj;
            if (this.material != other.material && (this.material == null || !this.material.equals(other.material))) {
                return false;
            }
            if (this.occurrence != other.occurrence) {
                return false;
            }
            if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(other.scale)) {
                return false;
            }
            if (Float.floatToIntBits(this.chance) != Float.floatToIntBits(other.chance)) {
                return false;
            }
            return true;
        }
        
        final Material material;
        final int occurrence;
        final float scale;
        float chance;

        private static final long serialVersionUID = 1L;
    }
    
    public enum Mode {SIMPLE, BLOBS, NOISE, LAYERED}
    
    private static final long NOISE_SEED_OFFSET = 55904327L;
    private static final long serialVersionUID = 1L;
}