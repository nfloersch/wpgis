/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.io.File;
import java.io.IOException;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.pepsoft.minecraft.ChunkImpl2;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Painting;
import org.pepsoft.minecraft.RegionFile;

/**
 *
 * @author pepijn
 */
public class DumpEntities {
    public static void main(String[] args) throws IOException {
        File worldDir = new File(args[0]);
        File[] regionFiles = new File(worldDir, "region").listFiles();
        for (File file: regionFiles) {
            RegionFile regionFile = new RegionFile(file);
            try {
                for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        if (regionFile.containsChunk(x, z)) {
                            CompoundTag tag;
                            NBTInputStream in = new NBTInputStream(regionFile.getChunkDataInputStream(x, z));
                            try {
                                tag = (CompoundTag) in.readTag();
                            } finally {
                                in.close();
                            }
                            ChunkImpl2 chunk = new ChunkImpl2(tag, 256);
                            for (Entity entity: chunk.getEntities()) {
                                if ((entity instanceof Painting) /*&& (((Painting) entity).getTileX() == 40) && (((Painting) entity).getTileZ() == 31)*/) {
                                    System.out.println(entity);
                                }
                            }
                        }
                    }
                }
            } finally {
                regionFile.close();
            }
        }
        
    }
}