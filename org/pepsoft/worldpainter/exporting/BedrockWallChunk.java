/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.exporting;

import org.pepsoft.worldpainter.biomeschemes.Minecraft1_2BiomeScheme;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.ChunkImpl;
import org.pepsoft.minecraft.ChunkImpl2;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.worldpainter.Dimension;
import static org.pepsoft.minecraft.Constants.*;

/**
 *
 * @author pepijn
 */
public class BedrockWallChunk {
    public static Chunk create(int chunkX, int chunkZ, Dimension dimension) {
        int maxHeight = dimension.getMaxHeight();
        int version = dimension.getWorld().getVersion();
        boolean dark = dimension.isDarkLevel();
        Chunk chunk = (version == Constants.SUPPORTED_VERSION_1) ? new ChunkImpl(chunkX, chunkZ, maxHeight) : new ChunkImpl2(chunkX, chunkZ, maxHeight);
        int maxY = maxHeight - 1;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (version == SUPPORTED_VERSION_2) {
                    chunk.setBiome(x, z, Minecraft1_2BiomeScheme.BIOME_PLAINS);
                }
                for (int y = 0; y <= maxY; y++) {
                    if ((x == 0) || (x == 15) || (z == 0) || (z == 15)) {
                        chunk.setBlockType(x, y, z, BLK_BEDROCK);
                    }
                }
                if (dark) {
                    chunk.setBlockType(x, maxY, z, BLK_BEDROCK);
                    chunk.setHeight(x, z, maxY);
                } else {
                    if ((x == 0) || (x == 15) || (z == 0) || (z == 15)) {
                        chunk.setHeight(x, z, maxY);
                    } else {
                        chunk.setHeight(x, z, 0);
                    }
                }
            }
        }
        chunk.setTerrainPopulated(true);
        return chunk;
    }
}