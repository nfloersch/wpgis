/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author pepijn
 */
public class Version implements Comparable<Version>, Serializable {
    public Version(int... parts) {
        this.parts = parts;
        for (int part: parts) {
            if (part < 0) {
                throw new IllegalArgumentException("Negative numbers not allowed");
            }
        }
    }

    public int[] getParts() {
        return Arrays.copyOf(parts, parts.length);
    }
    
    @Override
    public int compareTo(Version o) {
        for (int i = 0; i < Math.max(parts.length, o.parts.length); i++) {
            if (i < parts.length) {
                if (i < o.parts.length) {
                    // Part present in both
                    if (parts[i] < o.parts[i]) {
                        return -1;
                    } else if (parts[i] > o.parts[i]) {
                        return 1;
                    }
                    // Parts are the same, continue to next part (if any)
                } else {
                    // Part only present in us; assume other part is 0
                    if (parts[i] > 0) {
                        return 1;
                    }
                    // Parts are the same, continue to next part (if any)
                }
            } else {
                // Part only present in other; assume our part is 0
                if (o.parts[i] > 0) {
                    return -1;
                }
                // Parts are the same, continue to next part (if any)
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Arrays.hashCode(this.parts);
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
        final Version other = (Version) obj;
        if (!Arrays.equals(this.parts, other.parts)) {
            return false;
        }
        return true;
    }
    
    private final int[] parts;

    private static final long serialVersionUID = 1L;
}