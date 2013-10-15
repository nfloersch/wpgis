/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import javax.swing.SwingUtilities;

/**
 *
 * @author pepijn
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    public void handle(Throwable t) {
//        t.printStackTrace();
        ErrorDialog dialog = new ErrorDialog(App.getInstanceIfExists());
        dialog.setException(t);
        dialog.setVisible(true);
    }

    @Override
    public void uncaughtException(Thread t, final Throwable e) {
//        e.printStackTrace();
        if (SwingUtilities.isEventDispatchThread()) {
            handle(e);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    handle(e);
                }
            });
        }
    }
}