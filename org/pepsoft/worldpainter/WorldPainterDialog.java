/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

/**
 *
 * @author pepijn
 */
public class WorldPainterDialog extends JDialog {
    public WorldPainterDialog(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);

        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("cancel", new AbstractAction("cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }

            private static final long serialVersionUID = 1L;
        });

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    }

    public final boolean isCancelled() {
        return cancelled;
    }
    
    protected void ok() {
        cancelled = false;
        dispose();
    }
    
    protected void cancel() {
        dispose();
    }
    
    private boolean cancelled = true;
    
    private static final long serialVersionUID = 1L;
}