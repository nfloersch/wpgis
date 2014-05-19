/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.biomeschemes;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.pepsoft.util.Checksum;

/**
 *
 * @author pepijn
 */
public final class Minecraft1_7BiomeScheme extends Minecraft1_7JarBiomeScheme {
    public Minecraft1_7BiomeScheme(File minecraftJar, File libDir, Checksum md5Sum) {
        super(minecraftJar, libDir, md5Sum, HASHES_TO_CLASSNAMES, "1.7.2 Default");
    }
    
    public static boolean isCompatible(Checksum md5Sum) {
        return HASHES_TO_CLASSNAMES.containsKey(md5Sum);
    }
    
    private static final Map<Checksum, String[]> HASHES_TO_CLASSNAMES = new HashMap<Checksum, String[]>();

//                                                                                                                                                                                                                                                                       Landscape class
//                                                                                                                                                                                                                                                                              Buffer manager class
    static { //                                                                                                                                                                                                                                                                        World generator class
        HASHES_TO_CLASSNAMES.put(new Checksum(new byte[] {(byte) 122, (byte) 48, (byte) 69, (byte) 84, (byte) -3, (byte) -22, (byte) -121, (byte) -102, (byte) 121, (byte) -98, (byte) -2, (byte) 110, (byte) -82, (byte) -35, (byte) -116, (byte) -107}), new String[] {"avz", "avx", "afy"}); // 1.7.2
    }
}