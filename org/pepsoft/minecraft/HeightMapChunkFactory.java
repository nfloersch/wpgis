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
public abstract class HeightMapChunkFactory implements ChunkFactory {
    public HeightMapChunkFactory(int maxHeight, int version) {
        this.maxHeight = maxHeight;
        this.version = version;
    }

    public final int getMaxHeight() {
        return maxHeight;
    }
    
    public final Chunk createChunk(int chunkX, int chunkZ) {
        Chunk chunk = (version == SUPPORTED_VERSION_1) ? new ChunkImpl(chunkX, chunkZ, maxHeight) : new ChunkImpl2(chunkX, chunkZ, maxHeight);
        int maxY = maxHeight - 1;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = getHeight(chunkX * 16 + x, chunkZ * 16 + z);
                for (int y = 0; y <= maxY; y++) {
                    if (y == 0) {
                        chunk.setBlockType(x, y, z, BLK_BEDROCK);
                    } else if (y <= (height - 3)) {
                        chunk.setBlockType(x, y, z, BLK_STONE);
                    } else if (y < height) {
                        chunk.setBlockType(x, y, z, BLK_DIRT);
                    } else if (y == height) {
                        chunk.setBlockType(x, y, z, BLK_GRASS);
                    } else {
                        chunk.setSkyLightLevel(x, y, z, 15);
                    }
                }
                chunk.setHeight(x, z, (height < maxY) ? (height + 1): maxY);
            }
        }
        chunk.setTerrainPopulated(true);
        return chunk;
    }

    protected abstract int getHeight(int x, int z);
    
    protected final int maxHeight, version;
}