/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util.random;

/**
 * A two dimensional field of scalar values. Both coordinates and scalar values
 * are <code>double</code>s.
 *
 * @author pepijn
 */
public interface Field2DDouble extends Field2D {
    double getValue(double x, double y);
}