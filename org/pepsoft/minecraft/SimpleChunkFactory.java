/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.minecraft;

import static org.pepsoft.minecraft.Constants.*;

/**
 *
 * @author pepijn
 */
public class SimpleChunkFactory implements ChunkFactory {
    public SimpleChunkFactory(int maxHeight, int version) {
        this.maxHeight = maxHeight;
        this.version = version;
    }

    public int getMaxHeight() {
        return maxHeight;
    }
    
    public Chunk createChunk(int chunkX, int chunkZ) {
        Chunk chunk = (version == SUPPORTED_VERSION_1)
            ? new ChunkImpl(chunkX, chunkZ, maxHeight)
            : new ChunkImpl2(chunkX, chunkZ, maxHeight);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < maxHeight; y++) {
                    if (y == 0) {
                        chunk.setBlockType(x, y, z, BLK_BEDROCK);
                    } else if (y <= 3) {
                        chunk.setBlockType(x, y, z, BLK_STONE);
                    } else if (y <= 5) {
                        chunk.setBlockType(x, y, z, BLK_DIRT);
                    } else if (y == 6) {
                        chunk.setBlockType(x, y, z, BLK_GRASS);
                    } else {
                        chunk.setSkyLightLevel(x, y, z, 15);
                    }
                }
                chunk.setHeight(x, z, 7);
            }
        }
        chunk.setTerrainPopulated(true);
        return chunk;
    }
    
    private final int maxHeight, version;
}