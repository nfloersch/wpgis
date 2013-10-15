/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

import java.awt.Frame;
import javax.swing.JDialog;

/**
 *
 * @author pepijn
 */
public abstract class CustomLayerDialog<T extends CustomLayer> extends JDialog {
    public CustomLayerDialog(Frame owner, boolean modal) {
        super(owner, modal);
    }
    
    public abstract T getSelectedLayer();
    
    public abstract boolean isCancelled();
    
    private static final long serialVersionUID = 1L;
}