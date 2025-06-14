package edu.ai.haut.ui.student;

import edu.ai.haut.model.*;
import edu.ai.haut.service.*;
import edu.ai.haut.ui.LoginFrame;
import edu.ai.haut.ui.common.TableUtil;
import edu.ai.haut.ui.student.StudentMainFrame.ButtonEditor;
import edu.ai.haut.ui.student.StudentMainFrame.ButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 学生主界面
 * 提供学生评教、查看评教历史等功能
 */
public class StudentMainFrame extends JFrame {
    
    private User currentUser;
    private Student currentStudent;
    
    private CourseService courseService;
    private EvaluationService evaluationService;
    private UserService userService;
    
    private JTabbedPane tabbedPane;
    private JTable courseTable;
    private JTable historyTable;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel historyTableModel;
    
    private JLabel welcomeLabel;
    private JLabel statusLabel;
    
    public StudentMainFrame(User user) {
        this.currentUser = user;
        this.currentStudent = (Student) user;
        
        this.courseService = new CourseService();
        this.evaluationService = new EvaluationService();
        this.userService = new UserService();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setupFrame();
        loadData();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        
        // 欢迎标签
        welcomeLabel = new JLabel("欢迎，" + currentStudent.getName() + " 同学！");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        
        // 状态标签
        statusLabel = new JLabel("学号：" + currentStudent.getStudentId() + " | 班级：" + currentStudent.getClassId());
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        
        // 课程评教表格
        String[] courseColumns = {"开课编号", "课程名称", "授课教师", "学期", "上课时间", "评教状态", "操作"};
        courseTableModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 只有操作列可编辑
            }
        };
        courseTable = new JTable(courseTableModel);

        // 设置操作列的按钮渲染器和编辑器
        courseTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        courseTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));

        // 评教历史表格
        String[] historyColumns = {"评教编号", "课程名称", "授课教师", "评教周期", "总分", "等级", "评教日期"};
        historyTableModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);

        // 设置表格样式
        setupTableStyle(courseTable);
        setupTableStyle(historyTable);
    }
    
    /**
     * 设置表格样式
     */
    private void setupTableStyle(JTable table) {
        TableUtil.styleTable(table);
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 248, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.add(welcomeLabel, BorderLayout.NORTH);
        infoPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        JButton refreshButton = new JButton("刷新");
        JButton logoutButton = new JButton("退出登录");

        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);

        logoutButton.setBackground(new Color(220, 20, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);

        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        
        topPanel.add(infoPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // 课程评教面板
        JPanel coursePanel = new JPanel(new BorderLayout());
        coursePanel.setBorder(BorderFactory.createTitledBorder("课程评教"));
        
        JScrollPane courseScrollPane = new JScrollPane(courseTable);
        courseScrollPane.setPreferredSize(new Dimension(800, 300));
        coursePanel.add(courseScrollPane, BorderLayout.CENTER);
        
        // 评教历史面板
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("评教历史"));
        
        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setPreferredSize(new Dimension(800, 300));
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
        
        // 添加到选项卡
        tabbedPane.addTab("课程评教", coursePanel);
        tabbedPane.addTab("评教历史", historyPanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        refreshButton.addActionListener(e -> loadData());
        logoutButton.addActionListener(e -> logout());
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 课程表格双击事件
        courseTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = courseTable.getSelectedRow();
                    if (row >= 0) {
                        String offeringId = (String) courseTableModel.getValueAt(row, 0);
                        String status = (String) courseTableModel.getValueAt(row, 5);
                        
                        if ("未评教".equals(status)) {
                            openEvaluationDialog(offeringId);
                        } else {
                            JOptionPane.showMessageDialog(StudentMainFrame.this, 
                                "该课程已完成评教", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });
    }
    
    /**
     * 设置窗口属性
     */
    private void setupFrame() {
        setTitle("学生评教管理系统 - 学生端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

// 设置窗口图标
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        } catch (Exception e) {
            // 忽略图标加载错误
        }
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        loadCourseData();
        loadHistoryData();
    }
    
    /**
     * 加载课程数据
     */
    private void loadCourseData() {
        courseTableModel.setRowCount(0);
        
        try {
            List<CourseOffering> offerings = courseService.getCourseOfferingsByClass(currentStudent.getClassId());
            EvaluationPeriod currentPeriod = evaluationService.getCurrentActivePeriod();
            
            for (CourseOffering offering : offerings) {
                String status = "未评教";
                if (currentPeriod != null) {
                    boolean hasEvaluated = evaluationService.hasStudentEvaluated(
                        currentStudent.getStudentId(), 
                        offering.getOfferingId(), 
                        currentPeriod.getPeriodId()
                    );
                    status = hasEvaluated ? "已评教" : "未评教";
                }
                
                Object[] row = {
                    offering.getOfferingId(),
                    offering.getCourse() != null ? offering.getCourse().getCourseName() : "未知课程",
                    offering.getTeacher() != null ? offering.getTeacher().getName() : "未知教师",
                    offering.getSemester(),
                    offering.getSchedule(),
                    status,
                    "评教"
                };
                courseTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载课程数据失败: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载历史数据
     */
    private void loadHistoryData() {
        historyTableModel.setRowCount(0);
        
        try {
            List<Evaluation> evaluations = evaluationService.getStudentEvaluationHistory(currentStudent.getStudentId());
            
            for (Evaluation evaluation : evaluations) {
                Object[] row = {
                    evaluation.getEvaluationId(),
                    evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getCourse() != null ? 
                        evaluation.getCourseOffering().getCourse().getCourseName() : "未知课程",
                    evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getTeacher() != null ? 
                        evaluation.getCourseOffering().getTeacher().getName() : "未知教师",
                    evaluation.getPeriod() != null ? evaluation.getPeriod().getPeriodName() : "未知周期",
                    String.format("%.1f", evaluation.getTotalScore()),
                    evaluation.getGrade(),
                    evaluation.getEvaluationDate().toLocalDate().toString()
                };
                historyTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载历史数据失败: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 打开评教对话框
     */
    private void openEvaluationDialog(String offeringId) {
        EvaluationPeriod currentPeriod = evaluationService.getCurrentActivePeriod();
        if (currentPeriod == null) {
            JOptionPane.showMessageDialog(this, "当前没有进行中的评教周期", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CourseOffering offering = courseService.getCourseOfferingById(offeringId);
        if (offering == null) {
            JOptionPane.showMessageDialog(this, "课程信息不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        StudentEvaluationDialog dialog = new StudentEvaluationDialog(this, offering, currentPeriod, currentStudent);
        dialog.setVisible(true);
        
        // 评教完成后刷新数据
        if (dialog.isEvaluationSubmitted()) {
            loadCourseData();
            loadHistoryData();
        }
    }
    

    

    
    /**
     * 退出登录
     */
    private void logout() {
        int option = JOptionPane.showConfirmDialog(this, "确定要退出登录吗？", "确认",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /**
     * 按钮渲染器类
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (String) table.getValueAt(row, 5); // 评教状态列

            if ("已评教".equals(status)) {
                setText("已完成");
                setEnabled(false);
                setBackground(Color.LIGHT_GRAY);
            } else {
                setText("评教");
                setEnabled(true);
                setBackground(new Color(70, 130, 180));
                setForeground(Color.WHITE);
            }

            return this;
        }
    }

    /**
     * 按钮编辑器类
     */
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            String status = (String) table.getValueAt(row, 5); // 评教状态列

            if ("已评教".equals(status)) {
                label = "已完成";
                button.setText(label);
                button.setEnabled(false);
                button.setBackground(Color.LIGHT_GRAY);
            } else {
                label = "评教";
                button.setText(label);
                button.setEnabled(true);
                button.setBackground(new Color(70, 130, 180));
                button.setForeground(Color.WHITE);
            }

            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && button.isEnabled()) {
                String offeringId = (String) courseTableModel.getValueAt(currentRow, 0);
                String status = (String) courseTableModel.getValueAt(currentRow, 5);

                if ("未评教".equals(status)) {
                    SwingUtilities.invokeLater(() -> openEvaluationDialog(offeringId));
                }
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
}
