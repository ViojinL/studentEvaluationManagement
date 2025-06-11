package edu.ai.haut.ui;

import edu.ai.haut.model.User;
import edu.ai.haut.service.UserService;
import edu.ai.haut.ui.student.StudentMainFrame;
import edu.ai.haut.ui.teacher.TeacherMainFrame;
import edu.ai.haut.ui.admin.AdminMainFrame;
import edu.ai.haut.ui.staff.StaffMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 登录界面
 * 系统的主要入口界面
 */
public class LoginFrame extends JFrame {
    
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;
    private JButton loginButton;
    private JButton registerButton;
    private JButton exitButton;
    
    private UserService userService;
    
    public LoginFrame() {
        this.userService = new UserService();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setupFrame();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        userIdField = new JTextField(20);
        passwordField = new JPasswordField(20);
        
        userTypeComboBox = new JComboBox<>(new String[]{"学生", "教师", "管理员", "教务人员"});
        userTypeComboBox.setSelectedIndex(0);
        
        loginButton = new JButton("登录");
        registerButton = new JButton("注册");
        exitButton = new JButton("退出");
        
        // 设置按钮样式
        loginButton.setPreferredSize(new Dimension(100, 35));
        registerButton.setPreferredSize(new Dimension(100, 35));
        exitButton.setPreferredSize(new Dimension(100, 35));
        
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        
        registerButton.setBackground(new Color(60, 179, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        
        exitButton.setBackground(new Color(220, 20, 60));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(240, 248, 255));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("学生评教管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(25, 25, 112));
        titlePanel.add(titleLabel);
        
        // 登录面板
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "用户登录",
                0, 0,
                new Font("微软雅黑", Font.BOLD, 16),
                new Color(70, 130, 180)
            ),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 用户类型
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(new JLabel("用户类型:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(userTypeComboBox, gbc);
        
        // 用户ID
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        loginPanel.add(new JLabel("用户ID:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(userIdField, gbc);
        
        // 密码
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        loginPanel.add(new JLabel("密码:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(passwordField, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(exitButton);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(buttonPanel, gbc);
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        
        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        // 底部信息
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 248, 255));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel footerLabel = new JLabel("© 2025 河南工业大学 - 学生评教管理系统 v1.0", JLabel.CENTER);
        footerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        footerLabel.setForeground(Color.GRAY);
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 登录按钮事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // 注册按钮事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegisterFrame();
            }
        });
        
        // 退出按钮事件
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(
                    LoginFrame.this,
                    "确定要退出系统吗？",
                    "确认退出",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        
        // 回车键登录
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
    }
    
    /**
     * 设置窗口属性
     */
    private void setupFrame() {
        setTitle("学生评教管理系统 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 设置窗口图标
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        } catch (Exception e) {
            // 忽略图标加载错误
        }
    }
    
    /**
     * 执行登录
     */
    private void performLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String userType = (String) userTypeComboBox.getSelectedItem();
        
        if (userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户ID", "提示", JOptionPane.WARNING_MESSAGE);
            userIdField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码", "提示", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }
        
        // 显示登录进度
        loginButton.setText("登录中...");
        loginButton.setEnabled(false);
        
        SwingUtilities.invokeLater(() -> {
            try {
                User user = userService.login(userId, password, userType);
                
                if (user != null) {
                    // 登录成功，打开对应的主界面
                    openMainFrame(user);
                    dispose(); // 关闭登录窗口
                } else {
                    JOptionPane.showMessageDialog(this, "用户名或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "登录时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } finally {
                loginButton.setText("登录");
                loginButton.setEnabled(true);
                passwordField.setText("");
            }
        });
    }
    
    /**
     * 打开对应用户类型的主界面
     */
    private void openMainFrame(User user) {
        switch (user.getUserType()) {
            case "学生":
                new StudentMainFrame(user).setVisible(true);
                break;
            case "教师":
                new TeacherMainFrame(user).setVisible(true);
                break;
            case "管理员":
                new AdminMainFrame(user).setVisible(true);
                break;
            case "教务人员":
                new StaffMainFrame(user).setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "未知的用户类型", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 打开注册窗口
     */
    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame(this);
        registerFrame.setVisible(true);
    }
}
