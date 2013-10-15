/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.terrainRanges;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.pepsoft.worldpainter.Terrain;

/**
 *
 * @author pepijn
 */
public class TerrainTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        helper.configure(this, (Terrain) value);
        return this;
    }
    
    private TerrainCellRendererHelper helper = new TerrainCellRendererHelper();
    
    private static final long serialVersionUID = 1L;
}