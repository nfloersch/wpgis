/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.minecraft.mapexplorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.pepsoft.minecraft.RegionFile;

/**
 *
 * @author pepijn
 */
public class RegionFileNode implements Node {
    RegionFileNode(File file) {
        this.file = file;
        StringTokenizer tokenizer = new StringTokenizer(file.getName(), ".");
        tokenizer.nextToken();
        x = Integer.parseInt(tokenizer.nextToken());
        z = Integer.parseInt(tokenizer.nextToken());
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isLeaf() {
        return false;
    }

    public Node[] getChildren() {
        if (children == null) {
            loadChildren();
        }
        return children;
    }

    private void loadChildren() {
        try {
            List<Node> chunks = new ArrayList<Node>();
            RegionFile regionFile = new RegionFile(file);
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    if (regionFile.containsChunk(x, z)) {
                        chunks.add(new ChunkNode(regionFile, x, z));
                    }
                }
            }
            children = chunks.toArray(new Node[chunks.size()]);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while reading region file", e);
        }
    }

    private final File file;
    private final int x, z;
    private Node[] children;
}