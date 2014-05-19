package org.pepsoft.worldpainter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.pepsoft.minecraft.Material;
import org.pepsoft.util.PerlinNoise;

/**
 * @author SchmitzP
 */
public class MixedMaterial implements Serializable {
    public MixedMaterial(final String name, final Row[] rows, final int biome, final boolean noise, final float scale, final Integer colour) {
        int total = 0;
        for (Row row: rows) {
            total += row.occurrence;
        }
        if (total != 1000) {
            throw new IllegalArgumentException("Total occurrence is not 1000");
        }
        this.name = name;
        this.rows = rows;
        this.biome = biome;
        this.noise = noise;
        this.scale = scale;
        this.colour = colour;
        init();
    }

    public String getName() {
        return name;
    }

    public int getBiome() {
        return biome;
    }

    public boolean isNoise() {
        return noise;
    }

    public float getScale() {
        return scale;
    }

    public Integer getColour() {
        return colour;
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
                    icon.setRGB(x, y, colourScheme.getColour(getMaterial(0, x, y, 0)));
                }
            }
        }
        return icon;
    }
    
    public Material getMaterial(long seed, int x, int y, float z) {
        if (simple) {
            return simpleMaterial;
        } else if (noise) {
            return materials[random.nextInt(1000)];
        } else {
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
        }
    }
    
    public Material getMaterial(long seed, int x, int y, int z) {
        if (simple) {
            return simpleMaterial;
        } else if (noise) {
            return materials[random.nextInt(1000)];
        } else {
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
        hash = 19 * hash + (this.noise ? 1 : 0);
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
        if (this.noise != other.noise) {
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
        return new MixedMaterial(material.toString(), new Row[] {new Row(material, 1000, 1.0f)}, -1, true, 1.0f, null);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }
    
    private void init() {
        if (rows.length == 1) {
            simple = true;
            simpleMaterial = rows[0].material;
        } else if (noise) {
            materials = new Material[1000];
            int index = 0;
            for (Row row: rows) {
                for (int i = 0; i < row.occurrence; i++) {
                    materials[index++] = row.material;
                }
            }
            random = new Random();
        } else {
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
        }
    }

    private final String name;
    private final int biome;
    private final Row[] rows;
    private final boolean noise;
    private final float scale;
    private final Integer colour;
    private transient Row[] sortedRows;
    private transient PerlinNoise[] noiseGenerators;
    private transient Material[] materials;
    private transient Random random;
    private transient boolean simple;
    private transient Material simpleMaterial;

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
    
    private static final long serialVersionUID = 1L;
}