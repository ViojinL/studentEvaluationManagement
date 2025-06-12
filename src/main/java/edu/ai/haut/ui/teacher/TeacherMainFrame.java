package edu.ai.haut.ui.teacher;

import edu.ai.haut.model.*;
import edu.ai.haut.service.*;
import edu.ai.haut.ui.LoginFrame;
import edu.ai.haut.ui.common.TableUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 教师主界面
 * 提供教师查看评教结果、课程管理等功能
 */
public class TeacherMainFrame extends JFrame {
    
    private User currentUser;
    private Teacher currentTeacher;
    
    private CourseService courseService;
    private EvaluationService evaluationService;

    private UserService userService;
    
    private JTabbedPane tabbedPane;
    private JTable courseTable;
    private JTable evaluationTable;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel evaluationTableModel;
    
    private JLabel welcomeLabel;
    private JLabel statusLabel;
    private JComboBox<String> periodComboBox;
    
    public TeacherMainFrame(User user) {
        this.currentUser = user;
        this.currentTeacher = (Teacher) user;
        
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
        welcomeLabel = new JLabel("欢迎，" + currentTeacher.getName() + " 老师！");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        
        // 状态标签
        statusLabel = new JLabel("工号：" + currentTeacher.getTeacherId() + " | 学院：" + currentTeacher.getCollege());
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        
        // 评教周期选择
        periodComboBox = new JComboBox<>();
        periodComboBox.setPreferredSize(new Dimension(200, 25));
        
        // 授课课程表格
        String[] courseColumns = {"开课编号", "课程名称", "班级", "学期", "上课时间", "评教人数", "平均分"};
        courseTableModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(courseTableModel);

        // 评教结果表格
        String[] evaluationColumns = {"评教编号", "课程名称", "班级", "学生", "总分", "等级", "评教日期"};
        evaluationTableModel = new DefaultTableModel(evaluationColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        evaluationTable = new JTable(evaluationTableModel);

        // 设置表格样式
        setupTableStyle(courseTable);
        setupTableStyle(evaluationTable);
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
        
        // 授课课程面板
        JPanel coursePanel = new JPanel(new BorderLayout());
        coursePanel.setBorder(BorderFactory.createTitledBorder("授课课程"));
        
        // 周期选择面板
        JPanel periodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        periodPanel.add(new JLabel("评教周期:"));
        periodPanel.add(periodComboBox);
        
        JButton queryButton = new JButton("查询");
        queryButton.setBackground(new Color(70, 130, 180));
        queryButton.setForeground(Color.WHITE);
        queryButton.setFocusPainted(false);
        periodPanel.add(queryButton);
        
        coursePanel.add(periodPanel, BorderLayout.NORTH);
        
        JScrollPane courseScrollPane = new JScrollPane(courseTable);
        courseScrollPane.setPreferredSize(new Dimension(800, 250));
        coursePanel.add(courseScrollPane, BorderLayout.CENTER);
        
        // 评教结果面板
        JPanel evaluationPanel = new JPanel(new BorderLayout());
        evaluationPanel.setBorder(BorderFactory.createTitledBorder("评教结果详情"));
        
        JScrollPane evaluationScrollPane = new JScrollPane(evaluationTable);
        evaluationScrollPane.setPreferredSize(new Dimension(800, 250));
        evaluationPanel.add(evaluationScrollPane, BorderLayout.CENTER);
        
        // 添加到选项卡
        tabbedPane.addTab("授课课程", coursePanel);
        tabbedPane.addTab("评教结果", evaluationPanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        refreshButton.addActionListener(e -> loadData());
        queryButton.addActionListener(e -> queryByPeriod());
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
                        showCourseEvaluationDetails(offeringId);
                    }
                }
            }
        });
        
        // 评教结果表格双击事件
        evaluationTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = evaluationTable.getSelectedRow();
                    if (row >= 0) {
                        String evaluationId = (String) evaluationTableModel.getValueAt(row, 0);
                        showEvaluationDetails(evaluationId);
                    }
                }
            }
        });
    }
    
    /**
     * 设置窗口属性
     */
    private void setupFrame() {
        setTitle("学生评教管理系统 - 教师端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
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
        loadPeriods();
        loadCourseData();
        loadEvaluationData();
    }

    /**
     * 根据选择的评教周期查询数据
     */
    private void queryByPeriod() {
        EvaluationPeriod selectedPeriod = getSelectedPeriod();
        if (selectedPeriod == null) {
            JOptionPane.showMessageDialog(this, "请选择评教周期", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 重新加载课程数据和评教数据
        loadCourseDataByPeriod(selectedPeriod);
        loadEvaluationData();
    }
    
    /**
     * 加载评教周期
     */
    private void loadPeriods() {
        periodComboBox.removeAllItems();
        
        try {
            List<EvaluationPeriod> periods = evaluationService.getAllEvaluationPeriods();
            for (EvaluationPeriod period : periods) {
                periodComboBox.addItem(period.getPeriodName() + " (" + period.getSemester() + ")");
            }
            
            // 默认选择当前活跃周期
            EvaluationPeriod currentPeriod = evaluationService.getCurrentActivePeriod();
            if (currentPeriod != null) {
                String currentItem = currentPeriod.getPeriodName() + " (" + currentPeriod.getSemester() + ")";
                periodComboBox.setSelectedItem(currentItem);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载评教周期失败: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载课程数据
     */
    private void loadCourseData() {
        EvaluationPeriod currentPeriod = evaluationService.getCurrentActivePeriod();
        if (currentPeriod != null) {
            loadCourseDataByPeriod(currentPeriod);
        }
    }

    /**
     * 根据指定评教周期加载课程数据
     */
    private void loadCourseDataByPeriod(EvaluationPeriod period) {
        courseTableModel.setRowCount(0);

        try {
            List<CourseOffering> offerings = courseService.getCourseOfferingsByTeacher(currentTeacher.getTeacherId());

            for (CourseOffering offering : offerings) {
                // 计算指定周期的评教统计信息
                int evaluationCount = 0;
                double averageScore = 0.0;

                List<Evaluation> evaluations = evaluationService.getTeacherEvaluationResults(
                    currentTeacher.getTeacherId(), period.getPeriodId()).stream()
                    .filter(e -> offering.getOfferingId().equals(e.getOfferingId()))
                    .toList();

                evaluationCount = evaluations.size();
                if (evaluationCount > 0) {
                    double totalScore = evaluations.stream()
                        .mapToDouble(Evaluation::getTotalScore)
                        .sum();
                    averageScore = totalScore / evaluationCount;
                }

                Object[] row = {
                    offering.getOfferingId(),
                    offering.getCourse() != null ? offering.getCourse().getCourseName() : "未知课程",
                    offering.getClassRoom() != null ? offering.getClassRoom().getClassName() : "未知班级",
                    offering.getSemester(),
                    offering.getSchedule(),
                    evaluationCount,
                    String.format("%.1f", averageScore)
                };
                courseTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载课程数据失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载评教数据
     */
    private void loadEvaluationData() {
        evaluationTableModel.setRowCount(0);
        
        try {
            EvaluationPeriod selectedPeriod = getSelectedPeriod();
            if (selectedPeriod == null) {
                return;
            }
            
            List<Evaluation> evaluations = evaluationService.getTeacherEvaluationResults(
                currentTeacher.getTeacherId(), selectedPeriod.getPeriodId());
            
            for (Evaluation evaluation : evaluations) {
                Object[] row = {
                    evaluation.getEvaluationId(),
                    evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getCourse() != null ? 
                        evaluation.getCourseOffering().getCourse().getCourseName() : "未知课程",
                    evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getClassRoom() != null ? 
                        evaluation.getCourseOffering().getClassRoom().getClassName() : "未知班级",
                    "***", // 不显示学生姓名，保护隐私
                    String.format("%.1f", evaluation.getTotalScore()),
                    evaluation.getGrade(),
                    evaluation.getEvaluationDate().toLocalDate().toString()
                };
                evaluationTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载评教数据失败: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 获取选中的评教周期
     */
    private EvaluationPeriod getSelectedPeriod() {
        String selectedItem = (String) periodComboBox.getSelectedItem();
        if (selectedItem == null) {
            return null;
        }
        
        try {
            List<EvaluationPeriod> periods = evaluationService.getAllEvaluationPeriods();
            for (EvaluationPeriod period : periods) {
                String itemText = period.getPeriodName() + " (" + period.getSemester() + ")";
                if (itemText.equals(selectedItem)) {
                    return period;
                }
            }
        } catch (Exception e) {
            System.err.println("获取选中周期时出错: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 显示课程评教详情
     */
    private void showCourseEvaluationDetails(String offeringId) {
        // 切换到评教结果选项卡
        tabbedPane.setSelectedIndex(1);

        // 这里可以进一步过滤显示特定课程的评教结果
        JOptionPane.showMessageDialog(this, "已切换到评教结果选项卡查看详情", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示评教详情
     */
    private void showEvaluationDetails(String evaluationId) {
        try {
            // 根据评教ID获取详细信息
            Evaluation evaluation = evaluationService.getEvaluationById(evaluationId);
            if (evaluation == null) {
                JOptionPane.showMessageDialog(this, "未找到评教记录", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 构建详情信息
            StringBuilder details = new StringBuilder();
            details.append("评教详情\n");
            details.append("评教编号: ").append(evaluation.getEvaluationId()).append("\n");
            details.append("课程名称: ").append(
                evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getCourse() != null ?
                evaluation.getCourseOffering().getCourse().getCourseName() : "未知课程"
            ).append("\n");
            details.append("班级: ").append(
                evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getClassRoom() != null ?
                evaluation.getCourseOffering().getClassRoom().getClassName() : "未知班级"
            ).append("\n");
            details.append("总分: ").append(String.format("%.1f", evaluation.getTotalScore())).append("\n");
            details.append("等级: ").append(evaluation.getGrade()).append("\n");
            details.append("评教时间: ").append(evaluation.getEvaluationDate().toLocalDate().toString()).append("\n");

            if (evaluation.getComments() != null && !evaluation.getComments().trim().isEmpty()) {
                details.append("评价意见: ").append(evaluation.getComments()).append("\n");
            }

            JOptionPane.showMessageDialog(this, details.toString(), "评教详情", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "获取评教详情失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
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
}
