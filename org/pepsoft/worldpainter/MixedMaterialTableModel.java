/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.MixedMaterial.Row;

/**
 *
 * @author pepijn
 */
public class MixedMaterialTableModel implements TableModel {
    public MixedMaterialTableModel(MixedMaterial material) {
        rows = Arrays.copyOf(material.getRows(), material.getRows().length);
        scaleEnabled = ! material.isNoise();
    }
    
    public MixedMaterialTableModel() {
        rows = new Row[] {new Row(Material.DIRT, 1000, 1.0f)};
        scaleEnabled = true;
    }

    public void addMaterial(Row row) {
        rows = Arrays.copyOf(rows, rows.length + 1);
        rows[rows.length - 1] = row;
        
        int remaining = 1000;
        for (int i = 1; i < rows.length; i++) {
            remaining -= rows[i].occurrence;
        }
        rows[0] = new Row(rows[0].material, remaining, rows[0].scale);

        TableModelEvent event = new TableModelEvent(this, 0, 0, COLUMN_OCCURRENCE);
        fireEvent(event);
        event = new TableModelEvent(this, rows.length - 1, rows.length - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        fireEvent(event);
    }

    public void removeMaterial(int rowIndex) {
        if (rowIndex == 0) {
            throw new IllegalArgumentException("Can't remove row zero");
        }
        Row[] oldRows = rows;
        rows = new Row[rows.length - 1];
        System.arraycopy(oldRows, 0, rows, 0, rowIndex);
        System.arraycopy(oldRows, rowIndex + 1, rows, rowIndex, rows.length - rowIndex);

        int remaining = 1000;
        for (int i = 1; i < rows.length; i++) {
            remaining -= rows[i].occurrence;
        }
        rows[0] = new Row(rows[0].material, remaining, rows[0].scale);

        TableModelEvent event = new TableModelEvent(this);
        fireEvent(event);
    }

    public Row[] getRows() {
        return rows;
    }

    public void setScaleEnabled(boolean scaleEnabled) {
        if (scaleEnabled != this.scaleEnabled) {
            this.scaleEnabled = scaleEnabled;
            TableModelEvent event = new TableModelEvent(this, TableModelEvent.HEADER_ROW);
            fireEvent(event);
        }
    }

    public boolean isScaleEnabled() {
        return scaleEnabled;
    }

    // TableModel
    
    @Override
    public int getRowCount() {
        return rows.length;
    }

    @Override
    public int getColumnCount() {
        return scaleEnabled ? COLUMN_NAMES.length : (COLUMN_NAMES.length - 1);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_TYPES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (rowIndex != 0) || (columnIndex != COLUMN_OCCURRENCE);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Row row = rows[rowIndex];
        switch (columnIndex) {
            case COLUMN_BLOCK_ID:
                return row.material.getBlockType();
            case COLUMN_DATA_VALUE:
                return row.material.getData();
            case COLUMN_OCCURRENCE:
                return row.occurrence;
            case COLUMN_SCALE:
                return (int) (row.scale * 100 + 0.5f);
            default:
                throw new IndexOutOfBoundsException("columnIndex " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if ((rowIndex == 0) && (columnIndex == COLUMN_OCCURRENCE)) {
            throw new IllegalArgumentException("Uneditable cell");
        }
        Row row = rows[rowIndex];
        switch (columnIndex) {
            case COLUMN_BLOCK_ID:
                row = new Row(Material.get((Integer) aValue, row.material.getData()), row.occurrence, row.scale);
                break;
            case COLUMN_DATA_VALUE:
                row = new Row(Material.get(row.material.getBlockType(), (Integer) aValue), row.occurrence, row.scale);
                break;
            case COLUMN_OCCURRENCE:
                row = new Row(row.material, (Integer) aValue, row.scale);
                break;
            case COLUMN_SCALE:
                row = new Row(row.material, row.occurrence, (Integer) aValue / 100f);
                break;
            default:
                throw new IndexOutOfBoundsException("columnIndex " + columnIndex);
        }
        rows[rowIndex] = row;
        
        if (columnIndex == COLUMN_OCCURRENCE) {
            int remaining = 1000;
            for (int i = 1; i < rows.length; i++) {
                remaining -= rows[i].occurrence;
            }
            rows[0] = new Row(rows[0].material, remaining, rows[0].scale);

            TableModelEvent event = new TableModelEvent(this, 0, 0, columnIndex);
            fireEvent(event);
            event = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
            fireEvent(event);
        } else {
            TableModelEvent event = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
            fireEvent(event);
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    private void fireEvent(TableModelEvent event) {
        for (TableModelListener listener: listeners) {
            listener.tableChanged(event);
        }
    }
    
    private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private Row[] rows;
    private boolean scaleEnabled;
    
    public static final int COLUMN_BLOCK_ID   = 0;
    public static final int COLUMN_DATA_VALUE = 1;
    public static final int COLUMN_OCCURRENCE = 2;
    public static final int COLUMN_SCALE      = 3;
    
    private static final String[] COLUMN_NAMES =   {"Block ID",    "Data Value",  "Occurrence (in ‰)", "Scale (in %)"};
    private static final Class<?>[] COLUMN_TYPES = {Integer.class, Integer.class, Integer.class,       Integer.class};
}