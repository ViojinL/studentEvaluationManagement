package edu.ai.haut;

import edu.ai.haut.ui.LoginFrame;
import edu.ai.haut.util.DatabaseUtil;

import javax.swing.*;

/**
 * 学生评教管理系统主程序
 * 系统入口点
 */
public class StudentEvaluationManagementSystem {
    
    public static void main(String[] args) {
        // 使用默认外观
        
        // 设置程序在事件调度线程中运行
        SwingUtilities.invokeLater(() -> {
            try {
                // 初始化数据库（在DatabaseUtil的静态块中已经完成）
                System.out.println("学生评教管理系统启动中...");
                
                // 创建并显示登录界面
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                
                System.out.println("系统启动完成");
                
            } catch (Exception e) {
                System.err.println("系统启动失败: " + e.getMessage());
                e.printStackTrace();
                
                // 显示错误对话框
                JOptionPane.showMessageDialog(null, 
                    "系统启动失败: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                
                // 关闭数据库连接并退出
                DatabaseUtil.closeConnection();
                System.exit(1);
            }
        });
        
        // 添加关闭钩子，确保程序退出时正确关闭数据库连接
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭系统...");
            DatabaseUtil.closeConnection();
            System.out.println("系统已关闭");
        }));
    }
}
