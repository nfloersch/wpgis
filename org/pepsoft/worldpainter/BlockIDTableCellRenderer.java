/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import static org.pepsoft.minecraft.Constants.*;

/**
 *
 * @author pepijn
 */
class BlockIDTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Integer) {
            int blockID = (Integer) value;
            if ((blockID < BLOCK_TYPE_NAMES.length) && (BLOCK_TYPE_NAMES[blockID] != null)) {
                setText(blockID + " " + BLOCK_TYPE_NAMES[blockID]);
            } else {
                setText(Integer.toString(blockID));
            }
        }
        return this;
    }

    private static final long serialVersionUID = 1L;
}