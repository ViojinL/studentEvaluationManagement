package edu.ai.haut.ui.common;

import javax.swing.*;
import java.awt.*;

/**
 * 布局工具类
 * 提供通用的布局创建和组件样式设置方法
 */
public class LayoutUtil {

    // 标准颜色常量
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    public static final Color SUCCESS_COLOR = new Color(60, 179, 113);
    public static final Color DANGER_COLOR = new Color(220, 20, 60);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color INFO_COLOR = new Color(23, 162, 184);
    public static final Color LIGHT_GRAY = new Color(248, 249, 250);
    public static final Color BORDER_COLOR = new Color(200, 200, 200);

    // 标准字体
    public static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 16);
    public static final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font("微软雅黑", Font.BOLD, 12);
    
    /**
     * 创建标准表单面板
     */
    public static JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    /**
     * 创建标题面板
     */
    public static JPanel createTitlePanel(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel);
        
        return panel;
    }
    
    /**
     * 创建按钮面板
     */
    public static JPanel createButtonPanel(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.WHITE);
        
        for (JButton button : buttons) {
            panel.add(button);
        }
        
        return panel;
    }
    
    /**
     * 创建标准按钮
     */
    public static JButton createButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        styleButton(button, backgroundColor);
        return button;
    }

    /**
     * 创建简单按钮（无悬停效果）
     */
    public static JButton createSimpleButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
    
    /**
     * 设置按钮样式
     */
    public static void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = backgroundColor;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
    }
    
    /**
     * 创建标准输入字段
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }
    
    /**
     * 创建标准标签
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }
    
    /**
     * 创建必填标签（带红色星号）
     */
    public static JLabel createRequiredLabel(String text) {
        JLabel label = new JLabel(text + " *");
        label.setFont(LABEL_FONT);
        // 设置HTML格式，让星号显示为红色
        label.setText("<html>" + text + " <font color='red'>*</font></html>");
        return label;
    }
    
    /**
     * 创建标准下拉框
     */
    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(LABEL_FONT);
        comboBox.setBackground(Color.WHITE);
        return comboBox;
    }
    
    /**
     * 创建标准文本区域
     */
    public static JTextArea createTextArea(int rows, int columns) {
        JTextArea textArea = new JTextArea(rows, columns);
        textArea.setFont(LABEL_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return textArea;
    }
    
    /**
     * 添加表单行（标签 + 组件）
     */
    public static void addFormRow(JPanel panel, int row, String labelText, JComponent component) {
        addFormRow(panel, row, labelText, component, false);
    }
    
    /**
     * 添加表单行（标签 + 组件，可选必填）
     */
    public static void addFormRow(JPanel panel, int row, String labelText, JComponent component, boolean required) {
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 添加标签
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 10);
        
        JLabel label = required ? createRequiredLabel(labelText) : createLabel(labelText);
        panel.add(label, gbc);
        
        // 添加组件
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 5);
        
        panel.add(component, gbc);
    }
    
    /**
     * 创建分隔线
     */
    public static JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        return separator;
    }
    
    /**
     * 创建带边框的面板
     */
    public static JPanel createBorderedPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            title,
            0,
            0,
            LABEL_FONT,
            Color.DARK_GRAY
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    /**
     * 创建卡片式面板
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }
    
    /**
     * 设置窗口居中显示
     */
    public static void centerWindow(Window window) {
        window.setLocationRelativeTo(null);
    }
    
    /**
     * 设置窗口相对于父窗口居中
     */
    public static void centerWindow(Window window, Window parent) {
        window.setLocationRelativeTo(parent);
    }
}
