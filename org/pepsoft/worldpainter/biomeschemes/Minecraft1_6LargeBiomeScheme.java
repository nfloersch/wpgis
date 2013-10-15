/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.biomeschemes;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.pepsoft.util.Checksum;

/**
 *
 * @author pepijn
 */
public final class Minecraft1_6LargeBiomeScheme extends Minecraft1_2JarBiomeScheme {
    public Minecraft1_6LargeBiomeScheme(File minecraftJar, File libDir, Checksum md5Sum) {
        super(minecraftJar, libDir, md5Sum, HASHES_TO_CLASSNAMES, "1.6.2 Large Biomes");
        try {
            Field field = worldGeneratorClass.getField("d");
            largeBiomesGenerator = field.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Not a valid 1.6.2 minecraft.jar", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Not a valid 1.6.2 minecraft.jar", e);
        }
    }
    
    public static boolean isCompatible(Checksum md5Sum) {
        return HASHES_TO_CLASSNAMES.containsKey(md5Sum);
    }
    
    @Override
    public final void setSeed(long seed) {
        if ((seed != this.seed) || (landscape == null)) {
            try {
                landscape = ((Object[]) getLandscapesMethod.invoke(null, seed, largeBiomesGenerator))[1];
                this.seed = seed;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Access denied while trying to set the seed", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Exception thrown while trying to set the seed", e);
            }
        }
    }
    
    private static Object largeBiomesGenerator;
    
    private static final Map<Checksum, String[]> HASHES_TO_CLASSNAMES = new HashMap<Checksum, String[]>();

//                                                                                                                                                                                                                                                                   Landscape class
//                                                                                                                                                                                                                                                                          Buffer manager class
    static { //                                                                                                                                                                                                                                                                    World generator class
        HASHES_TO_CLASSNAMES.put(new Checksum(new byte[] {(byte) 29, (byte) 67, (byte) -51, (byte) -70, (byte) -117, (byte) -105, (byte) 82, (byte) -41, (byte) -11, (byte) 87, (byte) -85, (byte) 125, (byte) 62, (byte) 54, (byte) 89, (byte) 100}), new String[] {"akn", "akl", "acf"}); // 1.6.2
    }
}