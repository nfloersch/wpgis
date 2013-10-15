package org.pepsoft.worldpainter.util;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ActionMapUIResource;
import java.awt.*;
import java.awt.event.*;

public final class TristateCheckBox extends JCheckBox {
    public TristateCheckBox() {
        this(null, null, TristateState.DESELECTED);
    }
    
    public TristateCheckBox(String text) {
        this(text, null, TristateState.DESELECTED);
    }

    public TristateCheckBox(String text, Icon icon,
            TristateState initial) {
        super(text, icon);

        //Set default single model
        setModel(new TristateButtonModel(initial));

        // override action behaviour
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TristateCheckBox.this.iterateState();
            }
        });
        ActionMap actions = new ActionMapUIResource();
        actions.put("pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TristateCheckBox.this.iterateState();
            }

            private static final long serialVersionUID = 1L;
        });
        actions.put("released", null);
        SwingUtilities.replaceUIActionMap(this, actions);
    }

    // Next two methods implement new API by delegation to model
    public void setIndeterminate() {
        getTristateModel().setIndeterminate();
    }

    public boolean isIndeterminate() {
        return getTristateModel().isIndeterminate();
    }

    public TristateState getState() {
        return getTristateModel().getState();
    }

    public void setTristateMode(boolean tristateMode) {
        getTristateModel().setTristateMode(tristateMode);
    }
    
    public boolean isTristateMode() {
        return getTristateModel().isTristateMode();
    }
    
    //Overrides superclass method
    @Override
    public void setModel(ButtonModel newModel) {
        super.setModel(newModel);

        //Listen for enable changes
        if (model instanceof TristateButtonModel) {
            model.addChangeListener(enableListener);
        }
    }

    //Empty override of superclass method
    @Override
    public void addMouseListener(MouseListener l) {
        // Do nothing
    }

    // Mostly delegates to model
    private void iterateState() {
        //Maybe do nothing at all?
        if (!getModel().isEnabled()) {
            return;
        }

        grabFocus();

        // Iterate state
        getTristateModel().iterateState();

        // Fire ActionEvent
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
            modifiers = ((InputEvent) currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
            modifiers = ((ActionEvent) currentEvent).getModifiers();
        }
        fireActionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, getText(),
                System.currentTimeMillis(), modifiers));
    }

    //Convenience cast
    public TristateButtonModel getTristateModel() {
        return (TristateButtonModel) super.getModel();
    }

    // Listener on model changes to maintain correct focusability
    private final ChangeListener enableListener =
            new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TristateCheckBox.this.setFocusable(
                            getModel().isEnabled());
                }
            };

    private static final long serialVersionUID = 1L;
}