package org.pepsoft.worldpainter;

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
            throw new IllegalArgumentException("Total permillage is not 1000");
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

    public Material getMaterial(long seed, int x, int y, int z) {
        if (noise) {
//            random.setSeed(seed + x * 65537 + y * 257 + z);
            return materials[random.nextInt(1000)];
        } else {
            if (rows.length == 1) {
                return rows[0].material;
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
//                for (int i = 0; i < noiseGenerators.length; i++) {
//                    final float rowScale = sortedRows[i].scale * this.scale;
//                    if (noiseGenerators[i].getPerlinNoise(xx / rowScale, yy / rowScale, zz / rowScale) >= sortedRows[i].chance) {
//                        return sortedRows[i].material;
//                    }
//                }
//                return sortedRows[sortedRows.length - 1].material;
            }
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
        hash = 19 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 19 * hash + this.biome;
        hash = 19 * hash + Arrays.deepHashCode(this.rows);
        hash = 19 * hash + (this.noise ? 1 : 0);
        hash = 19 * hash + Float.floatToIntBits(this.scale);
        hash = 19 * hash + (this.colour != null ? this.colour : 0);
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
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
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
        if ((this.colour == null) ? (other.colour != null) : !this.colour.equals(other.colour)) {
            return false;
        }
        return true;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }
    
    private void init() {
        if (noise) {
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

    public static class Row implements Serializable {
        public Row(Material material, int occurrence, float scale) {
            this.material = material;
            this.occurrence = occurrence;
            this.scale = scale;
        }
        
        final Material material;
        final int occurrence;
        final float scale;
        float chance;

        private static final long serialVersionUID = 1L;
    }
    
    private static final long serialVersionUID = 1L;
}