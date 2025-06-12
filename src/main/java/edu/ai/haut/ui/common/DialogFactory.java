package edu.ai.haut.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 对话框工厂类
 * 统一创建各种对话框，减少重复代码
 */
public class DialogFactory {
    
    /**
     * 创建标准表单对话框
     */
    public static JDialog createFormDialog(JFrame parent, String title, JPanel formPanel, 
                                         ActionListener saveAction, ActionListener cancelAction) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parent);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel(saveAction, cancelAction);
        
        // 设置布局
        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        return dialog;
    }
    
    /**
     * 创建确认对话框
     */
    public static int showConfirmDialog(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, 
                                           JOptionPane.YES_NO_OPTION, 
                                           JOptionPane.QUESTION_MESSAGE);
    }
    
    /**
     * 创建信息对话框
     */
    public static void showInfoDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 创建错误对话框
     */
    public static void showErrorDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * 创建警告对话框
     */
    public static void showWarningDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * 创建输入对话框
     */
    public static String showInputDialog(Component parent, String message, String title, String defaultValue) {
        return (String) JOptionPane.showInputDialog(parent, message, title, 
                                                   JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);
    }
    
    /**
     * 创建选择对话框
     */
    public static Object showSelectionDialog(Component parent, String message, String title, 
                                           Object[] options, Object defaultOption) {
        return JOptionPane.showInputDialog(parent, message, title, 
                                         JOptionPane.QUESTION_MESSAGE, null, options, defaultOption);
    }
    
    /**
     * 创建按钮面板
     */
    private static JPanel createButtonPanel(ActionListener saveAction, ActionListener cancelAction) {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");
        
        // 设置按钮样式
        styleButton(saveButton, new Color(60, 179, 113));
        styleButton(cancelButton, new Color(220, 20, 60));
        
        // 添加事件监听
        if (saveAction != null) {
            saveButton.addActionListener(saveAction);
        }
        if (cancelAction != null) {
            cancelButton.addActionListener(cancelAction);
        }
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        return buttonPanel;
    }
    
    /**
     * 设置按钮样式
     */
    private static void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(80, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    /**
     * 创建标准表格对话框
     */
    public static JDialog createTableDialog(JFrame parent, String title, JTable table, 
                                          JPanel buttonPanel) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(parent);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        if (buttonPanel != null) {
            dialog.add(buttonPanel, BorderLayout.SOUTH);
        }
        
        return dialog;
    }
    
    /**
     * 创建详情查看对话框
     */
    public static JDialog createDetailDialog(JFrame parent, String title, JPanel detailPanel) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(parent);
        
        JScrollPane scrollPane = new JScrollPane(detailPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton closeButton = new JButton("关闭");
        styleButton(closeButton, new Color(108, 117, 125));
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        return dialog;
    }
}
