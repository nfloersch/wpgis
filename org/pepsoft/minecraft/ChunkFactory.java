/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.minecraft;

/**
 *
 * @author pepijn
 */
public interface ChunkFactory {
    /**
     * Create the chunk at the specified location. No lighting has to be
     * performed.
     *
     * @param x The X coordinate in the Minecraft coordinate space of the chunk
     *     to generate.
     * @param z The Z coordinate in the Minecraft coordinate space of the chunk
     *     to generate.
     * @return The generated chunk.
     */
    Chunk createChunk(int x, int z);
    
    /**
     * Get the height of the chunks this chunk factory will create.
     * 
     * @return The height of the chunks this factory will create.
     */
    int getMaxHeight();
}