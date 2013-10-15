/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.themes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.pepsoft.worldpainter.Terrain;

/**
 *
 * @author pepijn
 */
public class TerrainRangesTableModel implements TableModel {
    public TerrainRangesTableModel(SortedMap<Integer, Terrain> terrainRanges) {
        levels = new int[terrainRanges.size()];
        terrains = new Terrain[terrainRanges.size()];
        rows = terrainRanges.size();
        int i = 0;
        for (Map.Entry<Integer, Terrain> row: terrainRanges.entrySet()) {
            levels[i] = row.getKey() + 1;
            terrains[i] = row.getValue();
            i++;
        }
    }

    public void addRow(int level, Terrain terrain) {
        SortedMap<Integer, Terrain> sortedMap = new TreeMap<Integer, Terrain>();
        sortedMap.put(level, terrain);
        for (int i = 0; i < rows; i++) {
            sortedMap.put(levels[i], terrains[i]);
        }
        rows++;
        if (rows > levels.length) {
            levels = new int[rows];
            terrains = new Terrain[rows];
        }
        int i = 0;
        for (Map.Entry<Integer, Terrain> row: sortedMap.entrySet()) {
            levels[i] = row.getKey();
            terrains[i] = row.getValue();
            i++;
        }
        TableModelEvent event = new TableModelEvent(this);
        for (TableModelListener listener: listeners) {
            listener.tableChanged(event);
        }
    }

    public void deleteRow(int row) {
        System.arraycopy(levels, row + 1, levels, row, rows - 1 - row);
        System.arraycopy(terrains, row + 1, terrains, row, rows - 1 - row);
        rows--;
        TableModelEvent event = new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        for (TableModelListener listener: listeners) {
            listener.tableChanged(event);
        }
    }

    public SortedMap<Integer, Terrain> getTerrainRanges() {
        SortedMap<Integer, Terrain> terrainRanges = new TreeMap<Integer, Terrain>();
        for (int i = 0; i < rows; i++) {
            terrainRanges.put(levels[i] - 1, terrains[i]);
        }
        return terrainRanges;
    }
    
    public boolean isValid() {
        for (int i = 1; i < rows; i++) {
            if (levels[i] == levels[i - 1]) {
                return false;
            }
        }
        return true;
    }
    
    // TableModel
    
    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Level";
            case 1:
                return "Terrain type";
            case 2:
                return "Actions";
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Integer.class;
            case 1:
                return Terrain.class;
            case 2:
                return JButton.class;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Level or action of first row should not be editable
        return (rowIndex > 0) || (columnIndex == 1);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return levels[rowIndex];
            case 1:
                return terrains[rowIndex];
            case 2:
                if (rowIndex > 0) {
                    return deleteButton;
                } else {
                    return null;
                }
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                levels[rowIndex] = (Integer) aValue;
                sortIfNeeded();
                break;
            case 1:
                terrains[rowIndex] = (Terrain) aValue;
                break;
            case 2:
                // Do nothing
                break;
            default:
                throw new IllegalArgumentException();
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

    private void sortIfNeeded() {
        for (int i = 1; i < rows; i++) {
            if (levels[i] < levels[i - 1]) {
                SortedMap<Integer, Terrain> sortedMap = new TreeMap<Integer, Terrain>();
                for (int j = 0; j < rows; j++) {
                    sortedMap.put(levels[j], terrains[j]);
                }
                int j = 0;
                for (Map.Entry<Integer, Terrain> row: sortedMap.entrySet()) {
                    levels[j] = row.getKey();
                    terrains[j] = row.getValue();
                    j++;
                }
                TableModelEvent event = new TableModelEvent(this, 0, rows - 1);
                for (TableModelListener listener: listeners) {
                    listener.tableChanged(event);
                }
                break;
            }
        }
    }
 
    private final JButton deleteButton = new JButton("Delete");
    private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private int[] levels;
    private Terrain[] terrains;
    private int rows;
}