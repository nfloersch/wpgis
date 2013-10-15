/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author SchmitzP
 */
public class DumpImageInfo {
    public static void main(String[] args) throws IOException {
        File dir = new File(args[0]);
        File[] files = dir.listFiles();
        for (File file: files) {
            dumpInfo(file);
        }
    }
    
    private static void dumpInfo(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image != null) {
            System.out.println(file.getPath());
            System.out.println("  Width: " + image.getWidth());
            System.out.println("  Height: " + image.getWidth());
            SampleModel sampleModel = image.getSampleModel();
            System.out.print("  Sample model data type: ");
            switch (sampleModel.getDataType()) {
                case DataBuffer.TYPE_BYTE:
                    System.out.println("byte");
                    break;
                case DataBuffer.TYPE_DOUBLE:
                    System.out.println("double");
                    break;
                case DataBuffer.TYPE_FLOAT:
                    System.out.println("float");
                    break;
                case DataBuffer.TYPE_INT:
                    System.out.println("int");
                    break;
                case DataBuffer.TYPE_SHORT:
                    System.out.println("short");
                    break;
                case DataBuffer.TYPE_UNDEFINED:
                    System.out.println("undefined");
                    break;
                case DataBuffer.TYPE_USHORT:
                    System.out.println("unsigned short");
                    break;
                default:
                    System.out.println("unknown");
                    break;
            }
            System.out.println("  Sample model number of bands: " + sampleModel.getNumBands());
            for (int i = 0; i < sampleModel.getNumBands(); i++) {
                System.out.println("    Band " + i + ": " + sampleModel.getSampleSize(i) + " bits");
            }
            ColorModel colorModel = image.getColorModel();
            ColorSpace colorSpace = colorModel.getColorSpace();
            System.out.print("  Color space type: ");
            switch (colorSpace.getType()) {
                case ColorSpace.TYPE_2CLR:
                case ColorSpace.TYPE_3CLR:
                case ColorSpace.TYPE_4CLR:
                case ColorSpace.TYPE_5CLR:
                case ColorSpace.TYPE_6CLR:
                case ColorSpace.TYPE_7CLR:
                case ColorSpace.TYPE_8CLR:
                case ColorSpace.TYPE_9CLR:
                case ColorSpace.TYPE_ACLR:
                case ColorSpace.TYPE_BCLR:
                case ColorSpace.TYPE_CCLR:
                case ColorSpace.TYPE_DCLR:
                case ColorSpace.TYPE_ECLR:
                case ColorSpace.TYPE_FCLR:
                    System.out.println("generic");
                    break;
                case ColorSpace.TYPE_CMY:
                    System.out.println("CMY");
                    break;
                case ColorSpace.TYPE_CMYK:
                    System.out.println("CMYK");
                    break;
                case ColorSpace.TYPE_GRAY:
                    System.out.println("grayscale");
                    break;
                case ColorSpace.TYPE_HLS:
                    System.out.println("HLS");
                    break;
                case ColorSpace.TYPE_HSV:
                    System.out.println("HSV");
                    break;
                case ColorSpace.TYPE_Lab:
                    System.out.println("Lab");
                    break;
                case ColorSpace.TYPE_Luv:
                    System.out.println("Luv");
                    break;
                case ColorSpace.TYPE_RGB:
                    System.out.println("RGB");
                    break;
                case ColorSpace.TYPE_XYZ:
                    System.out.println("XYZ");
                    break;
                case ColorSpace.TYPE_YCbCr:
                    System.out.println("YCbCr");
                    break;
                case ColorSpace.TYPE_Yxy:
                    System.out.println("Yxy");
                    break;
                default:
                    System.out.println("unknown");
                    break;
            }
            System.out.println("  Color space components: " + colorSpace.getNumComponents());
            for (int i = 0; i < colorSpace.getNumComponents(); i++) {
                System.out.println("    Component " + i + ": " + colorSpace.getName(i));
                System.out.println("      Minimum value: " + colorSpace.getMinValue(i));
                System.out.println("      Maximum value: " + colorSpace.getMaxValue(i));
            }
            System.out.println("  Color model components: " + colorModel.getNumComponents());
            for (int i = 0; i < colorModel.getNumComponents(); i++) {
                System.out.println("    Component " + i + ": " + colorModel.getComponentSize(i) + " bits");
            }
            System.out.println("  Color model pixel size: " + colorModel.getPixelSize() + " bits");
            System.out.print("  Color model transparency: ");
            switch (colorModel.getTransparency()) {
                case Transparency.BITMASK:
                    System.out.println("bitmask");
                    break;
                case Transparency.OPAQUE:
                    System.out.println("opaque");
                    break;
                case Transparency.TRANSLUCENT:
                    System.out.println("translucent");
                    break;
                default:
                    System.out.println("unknown");
                    break;
            }
            System.out.println("  Color model has alpha: " + colorModel.hasAlpha());
            if (colorModel.hasAlpha()) {
                System.out.println("    Premultiplied: " + colorModel.isAlphaPremultiplied());
            }
        }
    }
}