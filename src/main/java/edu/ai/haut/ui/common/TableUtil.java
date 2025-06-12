package edu.ai.haut.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 表格工具类
 * 提供通用的表格创建和操作方法
 */
public class TableUtil {
    
    /**
     * 创建标准表格
     */
    public static JTable createStandardTable(String[] columns, boolean[] editableColumns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editableColumns != null && column < editableColumns.length && editableColumns[column];
            }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }
    
    /**
     * 创建只读表格
     */
    public static JTable createReadOnlyTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }
    
    /**
     * 创建带操作按钮的表格
     */
    public static JTable createTableWithActions(String[] columns, String actionColumnName, 
                                               ActionListener actionListener) {
        // 添加操作列
        String[] newColumns = new String[columns.length + 1];
        System.arraycopy(columns, 0, newColumns, 0, columns.length);
        newColumns[columns.length] = actionColumnName;
        
        DefaultTableModel model = new DefaultTableModel(newColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == newColumns.length - 1; // 只有操作列可编辑
            }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        // 设置操作列的渲染器和编辑器
        TableColumn actionColumn = table.getColumn(actionColumnName);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox(), actionListener));
        actionColumn.setPreferredWidth(100);
        actionColumn.setMaxWidth(120);
        
        return table;
    }
    
    /**
     * 设置表格样式
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // 设置表头样式
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setReorderingAllowed(false);
    }
    
    /**
     * 清空表格数据
     */
    public static void clearTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
    }
    
    /**
     * 添加行数据
     */
    public static void addRow(JTable table, Object[] rowData) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(rowData);
    }
    
    /**
     * 删除选中行
     */
    public static void removeSelectedRows(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int[] selectedRows = table.getSelectedRows();
        
        // 从后往前删除，避免索引变化
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            model.removeRow(selectedRows[i]);
        }
    }
    
    /**
     * 获取选中行数据
     */
    public static Object[] getSelectedRowData(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int columnCount = model.getColumnCount();
        Object[] rowData = new Object[columnCount];
        
        for (int i = 0; i < columnCount; i++) {
            rowData[i] = model.getValueAt(selectedRow, i);
        }
        
        return rowData;
    }
    
    /**
     * 按钮渲染器
     */
    public static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            setText(value != null ? value.toString() : "操作");
            setBackground(new Color(70, 130, 180));
            setForeground(Color.WHITE);
            setFont(new Font("微软雅黑", Font.BOLD, 11));
            setBorderPainted(false);
            setFocusPainted(false);
            return this;
        }
    }
    
    /**
     * 按钮编辑器
     */
    public static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private ActionListener actionListener;
        private JTable table;
        private int row;
        
        public ButtonEditor(JCheckBox checkBox, ActionListener actionListener) {
            super(checkBox);
            this.actionListener = actionListener;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                   boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            label = value != null ? value.toString() : "操作";
            button.setText(label);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("微软雅黑", Font.BOLD, 11));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed && actionListener != null) {
                // 创建包含行信息的ActionEvent
                actionListener.actionPerformed(new java.awt.event.ActionEvent(table, row, "button_clicked"));
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
    
    /**
     * 自动调整列宽
     */
    public static void autoResizeColumns(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();
            
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
                
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }
            
            tableColumn.setPreferredWidth(preferredWidth);
        }
    }
}
