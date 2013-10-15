package org.pepsoft.worldpainter.util;

import javax.swing.JToggleButton.ToggleButtonModel;
import java.awt.event.ItemEvent;

public class TristateButtonModel extends ToggleButtonModel {
    public TristateButtonModel(TristateState state) {
        setState(state);
    }

    public TristateButtonModel() {
        this(TristateState.DESELECTED);
    }

    public void setIndeterminate() {
        setState(TristateState.INDETERMINATE);
    }

    public boolean isIndeterminate() {
        return state == TristateState.INDETERMINATE;
    }

    // Overrides of superclass methods
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // Restore state display
        displayState();
    }

    @Override
    public void setSelected(boolean selected) {
        setState(selected
                ? TristateState.SELECTED : TristateState.DESELECTED);
    }

    // Empty overrides of superclass methods
    @Override
    public void setArmed(boolean b) {
        // Do nothing
    }

    @Override
    public void setPressed(boolean b) {
        // Do nothing
    }

    public boolean isTristateMode() {
        return tristateMode;
    }

    public void setTristateMode(boolean tristateMode) {
        if (tristateMode != this.tristateMode) {
            this.tristateMode = tristateMode;
            if ((! tristateMode) && (state == TristateState.INDETERMINATE)) {
                setState(TristateState.DESELECTED);
            }
        }
    }

    void iterateState() {
        setState(((! tristateMode) && (state == TristateState.SELECTED)) ? TristateState.DESELECTED : state.next());
    }

    private void setState(TristateState state) {
        //Set internal state
        this.state = state;
        displayState();
        if (state == TristateState.INDETERMINATE && isEnabled()) {
            // force the events to fire

            // Send ChangeEvent
            fireStateChanged();

            // Send ItemEvent
            int indeterminate = 3;
            fireItemStateChanged(new ItemEvent(
                    this, ItemEvent.ITEM_STATE_CHANGED, this,
                    indeterminate));
        }
    }

    private void displayState() {
        super.setSelected(state != TristateState.DESELECTED);
        super.setArmed(state == TristateState.INDETERMINATE);
        super.setPressed(state == TristateState.INDETERMINATE);

    }

    public TristateState getState() {
        return state;
    }

    private TristateState state = TristateState.DESELECTED;
    private boolean tristateMode  = true;

    private static final long serialVersionUID = 1L;
}