/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;

/**
 *
 * @author SchmitzP
 */
public final class ObjectUtils {
    private ObjectUtils() {
        // Prevent instantiation
    }
    
    public static DataBuffer clone(DataBuffer dataBuffer) {
        if (dataBuffer instanceof DataBufferByte) {
            return clone((DataBufferByte) dataBuffer);
        } else if (dataBuffer instanceof DataBufferDouble) {
            return clone((DataBufferDouble) dataBuffer);
        } else if (dataBuffer instanceof DataBufferFloat) {
            return clone((DataBufferFloat) dataBuffer);
        } else if (dataBuffer instanceof DataBufferInt) {
            return clone((DataBufferInt) dataBuffer);
        } else if (dataBuffer instanceof DataBufferShort) {
            return clone((DataBufferShort) dataBuffer);
        } else if (dataBuffer instanceof DataBufferUShort) {
            return clone((DataBufferUShort) dataBuffer);
        } else {
            throw new UnsupportedOperationException("Don't know how to clone " + dataBuffer.getClass().getName());
        }
    }
    
    public static DataBufferByte clone(DataBufferByte dataBuffer) {
        return new DataBufferByte(clone(dataBuffer.getBankData()), dataBuffer.getSize(), dataBuffer.getOffsets());
    }

    public static DataBufferDouble clone(DataBufferDouble dataBuffer) {
        return new DataBufferDouble(clone(dataBuffer.getBankData()), dataBuffer.getSize(), dataBuffer.getOffsets());
    }

    public static DataBufferFloat clone(DataBufferFloat dataBuffer) {
        return new DataBufferFloat(clone(dataBuffer.getBankData()), dataBuffer.getSize(), dataBuffer.getOffsets());
    }

    public static DataBufferInt clone(DataBufferInt dataBuffer) {
        return new DataBufferInt(clone(dataBuffer.getBankData()), dataBuffer.getSize(), dataBuffer.getOffsets());
    }
    
    public static DataBufferShort clone(DataBufferShort dataBuffer) {
        return new DataBufferShort(clone(dataBuffer.getBankData()), dataBuffer.getSize(), dataBuffer.getOffsets());
    }

    public static DataBufferUShort clone(DataBufferUShort dataBuffer) {
        return new DataBufferUShort(clone(dataBuffer.getBankData()), dataBuffer.getSize(), dataBuffer.getOffsets());
    }
    
    public static byte[][] clone(byte[][] array) {
        byte[][] clone = new byte[array.length][];
        for (int i = 0; i < array.length; i++) {
            clone[i] = array[i].clone();
        }
        return clone;
    }

    public static double[][] clone(double[][] array) {
        double[][] clone = new double[array.length][];
        for (int i = 0; i < array.length; i++) {
            clone[i] = array[i].clone();
        }
        return clone;
    }

    public static float[][] clone(float[][] array) {
        float[][] clone = new float[array.length][];
        for (int i = 0; i < array.length; i++) {
            clone[i] = array[i].clone();
        }
        return clone;
    }

    public static int[][] clone(int[][] array) {
        int[][] clone = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            clone[i] = array[i].clone();
        }
        return clone;
    }

    public static short[][] clone(short[][] array) {
        short[][] clone = new short[array.length][];
        for (int i = 0; i < array.length; i++) {
            clone[i] = array[i].clone();
        }
        return clone;
    }
}