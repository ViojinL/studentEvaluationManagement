package edu.ai.haut.ui.staff;

import edu.ai.haut.model.*;
import edu.ai.haut.service.*;
import edu.ai.haut.ui.LoginFrame;
import edu.ai.haut.ui.common.ManagementUIHelper;
import edu.ai.haut.ui.common.LayoutUtil;
import edu.ai.haut.ui.common.TableUtil;
import edu.ai.haut.util.DatabaseUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教务人员主界面
 * 提供评教结果查询、统计分析等功能
 */
public class StaffMainFrame extends JFrame {
    
    private User currentUser;
    private AcademicAffairsStaff currentStaff;
    
    private EvaluationService evaluationService;
    private StatisticsService statisticsService;
    private TeacherService teacherService;
    private CourseService courseService;
    private UserService userService;
    private ClassService classService;
    private StudentService studentService;
    private ManagementUIHelper managementUIHelper;
    
    private JTabbedPane tabbedPane;
    private JTable evaluationTable;
    private JTable statisticsTable;
    private JTable courseTable;
    private JTable evaluationPeriodTable;
    private JTable classTable;
    private JTable userTable;
    private DefaultTableModel evaluationTableModel;
    private DefaultTableModel statisticsTableModel;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel evaluationPeriodTableModel;
    private DefaultTableModel classTableModel;
    private DefaultTableModel userTableModel;
    
    private JLabel welcomeLabel;
    private JLabel statusLabel;
    private JComboBox<String> periodComboBox;
    private JComboBox<String> statisticsTypeComboBox;
    
    public StaffMainFrame(User user) {
        this.currentUser = user;
        this.currentStaff = (AcademicAffairsStaff) user;
        
        this.evaluationService = new EvaluationService();
        this.statisticsService = new StatisticsService();
        this.teacherService = new TeacherService();
        this.courseService = new CourseService();
        this.userService = new UserService();
        this.classService = new ClassService();
        this.studentService = new StudentService();
        this.managementUIHelper = new ManagementUIHelper();
        
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
        welcomeLabel = new JLabel("欢迎，" + currentStaff.getName() + " 老师！");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        
        // 状态标签
        statusLabel = new JLabel("工号：" + currentStaff.getStaffId() + " | 部门：" + currentStaff.getDepartment());
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        
        // 评教周期选择
        periodComboBox = new JComboBox<>();
        periodComboBox.setPreferredSize(new Dimension(200, 25));
        
        // 统计类型选择
        statisticsTypeComboBox = new JComboBox<>(new String[]{"按教师统计", "按课程统计", "按学生统计"});
        statisticsTypeComboBox.setPreferredSize(new Dimension(150, 25));
        
        // 评教结果表格
        String[] evaluationColumns = {"评教编号", "课程名称", "授课教师", "班级", "总分", "等级", "评教日期"};
        evaluationTable = TableUtil.createReadOnlyTable(evaluationColumns);
        evaluationTableModel = (DefaultTableModel) evaluationTable.getModel();

        // 统计分析表格
        String[] statisticsColumns = {"项目", "数量/分数", "百分比/等级", "备注"};
        statisticsTable = TableUtil.createReadOnlyTable(statisticsColumns);
        statisticsTableModel = (DefaultTableModel) statisticsTable.getModel();

        // 课程管理表格
        String[] courseColumns = {"课程编号", "课程名称", "学分", "课程类型", "开课学院"};
        courseTable = TableUtil.createReadOnlyTable(courseColumns);
        courseTableModel = (DefaultTableModel) courseTable.getModel();

        // 评教管理表格
        String[] evaluationPeriodColumns = {"周期编号", "周期名称", "学期", "开始日期", "结束日期", "状态", "评教数量"};
        evaluationPeriodTable = TableUtil.createReadOnlyTable(evaluationPeriodColumns);
        evaluationPeriodTableModel = (DefaultTableModel) evaluationPeriodTable.getModel();
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

        JButton refreshButton = LayoutUtil.createButton("刷新", LayoutUtil.PRIMARY_COLOR);
        JButton logoutButton = LayoutUtil.createButton("退出登录", LayoutUtil.DANGER_COLOR);

        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        
        topPanel.add(infoPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // 评教结果查询面板
        JPanel evaluationPanel = createEvaluationQueryPanel();

        // 统计分析面板
        JPanel statisticsPanel = createStatisticsPanel();

        // 课程管理面板
        JPanel coursePanel = createCourseManagementPanel();

        // 评教管理面板
        JPanel evaluationManagementPanel = createEvaluationManagementPanel();

        // 添加到选项卡
        tabbedPane.addTab("评教结果查询", evaluationPanel);
        tabbedPane.addTab("统计分析", statisticsPanel);
        tabbedPane.addTab("课程管理", coursePanel);
        tabbedPane.addTab("评教管理", evaluationManagementPanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        refreshButton.addActionListener(e -> loadData());
        logoutButton.addActionListener(e -> logout());
    }
    
    /**
     * 创建评教结果查询面板
     */
    private JPanel createEvaluationQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("评教结果查询"));
        
        // 直接显示评教结果表格，不需要查询条件
        JScrollPane evaluationScrollPane = new JScrollPane(evaluationTable);
        evaluationScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(evaluationScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建统计分析面板
     */
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("统计分析"));
        
        // 统计条件面板
        JPanel statsQueryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsQueryPanel.add(new JLabel("评教周期:"));
        statsQueryPanel.add(periodComboBox);
        statsQueryPanel.add(new JLabel("统计类型:"));
        statsQueryPanel.add(statisticsTypeComboBox);

        JButton statsQueryButton = LayoutUtil.createButton("生成统计", LayoutUtil.PRIMARY_COLOR);

        statsQueryPanel.add(statsQueryButton);
        
        panel.add(statsQueryPanel, BorderLayout.NORTH);
        
        JScrollPane statisticsScrollPane = new JScrollPane(statisticsTable);
        statisticsScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(statisticsScrollPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        statsQueryButton.addActionListener(e -> loadStatisticsData());
        
        return panel;
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 评教结果查询面板现在是只读的，不需要事件监听器
    }
    
    /**
     * 设置窗口属性
     */
    private void setupFrame() {
        setTitle("学生评教管理系统 - 教务人员端");
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
        loadEvaluationData();
        loadStatisticsData();
        loadCourseData();
        loadEvaluationPeriodData();
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
     * 加载评教数据
     */
    private void loadEvaluationData() {
        evaluationTableModel.setRowCount(0);

        try {
            // 获取所有评教周期的评教记录
            List<EvaluationPeriod> periods = evaluationService.getAllEvaluationPeriods();

            for (EvaluationPeriod period : periods) {
                // 获取所有教师在该周期的评教结果
                List<Teacher> teachers = teacherService.getAllTeachers();

                for (Teacher teacher : teachers) {
                    List<Evaluation> evaluations = evaluationService.getTeacherEvaluationResults(
                        teacher.getTeacherId(), period.getPeriodId());

                    for (Evaluation evaluation : evaluations) {
                        Object[] row = {
                            evaluation.getEvaluationId(),
                            evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getCourse() != null ?
                                evaluation.getCourseOffering().getCourse().getCourseName() : "未知课程",
                            teacher.getName(),
                            evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getClassRoom() != null ?
                                evaluation.getCourseOffering().getClassRoom().getClassName() : "未知班级",
                            String.format("%.1f", evaluation.getTotalScore()),
                            evaluation.getGrade(),
                            evaluation.getEvaluationDate().toLocalDate().toString()
                        };
                        evaluationTableModel.addRow(row);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载评教数据失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载统计数据
     */
    private void loadStatisticsData() {
        statisticsTableModel.setRowCount(0);
        
        try {
            EvaluationPeriod selectedPeriod = getSelectedPeriod();
            if (selectedPeriod == null) {
                return;
            }
            
            String statisticsType = (String) statisticsTypeComboBox.getSelectedItem();
            
            switch (statisticsType) {
                case "按教师统计":
                    loadTeacherStatistics(selectedPeriod.getPeriodId());
                    break;
                case "按课程统计":
                    loadCourseStatistics(selectedPeriod.getPeriodId());
                    break;
                case "按学生统计":
                    loadStudentStatistics(selectedPeriod.getPeriodId());
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载统计数据失败: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载教师统计
     */
    private void loadTeacherStatistics(String periodId) {
        Map<String, Object> stats = statisticsService.getTeacherEvaluationStatistics(periodId);
        
        Object[] totalRow = {
            "参与教师总数",
            stats.getOrDefault("totalTeachers", 0),
            "",
            "已完成评教的教师数量"
        };
        statisticsTableModel.addRow(totalRow);
        
        Object[] avgRow = {
            "总体平均分",
            String.format("%.1f", (Double) stats.getOrDefault("overallAvgScore", 0.0)),
            managementUIHelper.getGradeByScore((Double) stats.getOrDefault("overallAvgScore", 0.0)),
            "所有教师的平均评教分数"
        };
        statisticsTableModel.addRow(avgRow);
    }
    
    /**
     * 加载课程统计
     */
    private void loadCourseStatistics(String periodId) {
        Map<String, Object> stats = statisticsService.getCourseEvaluationStatistics(periodId);
        
        Object[] totalRow = {
            "参与课程总数",
            stats.getOrDefault("totalCourses", 0),
            "",
            "已完成评教的课程数量"
        };
        statisticsTableModel.addRow(totalRow);
        
        Object[] avgRow = {
            "总体平均分",
            String.format("%.1f", (Double) stats.getOrDefault("overallAvgScore", 0.0)),
            managementUIHelper.getGradeByScore((Double) stats.getOrDefault("overallAvgScore", 0.0)),
            "所有课程的平均评教分数"
        };
        statisticsTableModel.addRow(avgRow);
    }
    

    
    /**
     * 加载学生统计
     */
    private void loadStudentStatistics(String periodId) {
        Map<String, Object> stats = statisticsService.getStudentEvaluationStatistics(periodId);
        
        Object[] totalRow = {
            "学生总数",
            stats.getOrDefault("totalStudents", 0),
            "",
            "系统中的学生总数"
        };
        statisticsTableModel.addRow(totalRow);
        
        Object[] participatedRow = {
            "参与评教学生数",
            stats.getOrDefault("participatedStudents", 0),
            String.format("%.1f%%", (Double) stats.getOrDefault("participationRate", 0.0)),
            "已参与评教的学生数量"
        };
        statisticsTableModel.addRow(participatedRow);
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
     * 显示评教详情
     */
    private void showEvaluationDetail() {
        int selectedRow = evaluationTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择一条评教记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String evaluationId = (String) evaluationTableModel.getValueAt(selectedRow, 0);

        try {
            Evaluation evaluation = evaluationService.getEvaluationById(evaluationId);
            if (evaluation == null) {
                JOptionPane.showMessageDialog(this, "评教记录不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            showEvaluationDetailDialog(evaluation);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载评教详情失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 显示评教详情对话框
     */
    private void showEvaluationDetailDialog(Evaluation evaluation) {
        JDialog dialog = new JDialog(this, "评教详情 - " + evaluation.getEvaluationId(), true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // 基本信息面板
        JPanel infoPanel = createEvaluationInfoPanel(evaluation);

        // 评分详情面板
        JPanel scoresPanel = createEvaluationScoresPanel(evaluation);

        // 评价意见面板
        JPanel commentsPanel = createEvaluationCommentsPanel(evaluation);

        // 使用选项卡组织内容
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("基本信息", infoPanel);
        tabbedPane.addTab("评分详情", scoresPanel);
        tabbedPane.addTab("评价意见", commentsPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 底部按钮
        JButton closeButton = LayoutUtil.createButton("关闭", LayoutUtil.PRIMARY_COLOR);
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = LayoutUtil.createButtonPanel(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 创建评教基本信息面板
     */
    private JPanel createEvaluationInfoPanel(Evaluation evaluation) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 评教编号
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("评教编号:"), gbc);
        gbc.gridx = 1;
        JLabel evaluationIdLabel = new JLabel(evaluation.getEvaluationId());
        evaluationIdLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(evaluationIdLabel, gbc);

        // 学生信息
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("评教学生:"), gbc);
        gbc.gridx = 1;
        String studentInfo = evaluation.getStudent() != null ?
            evaluation.getStudent().getName() + " (" + evaluation.getStudent().getStudentId() + ")" : "未知学生";
        panel.add(new JLabel(studentInfo), gbc);

        // 课程信息
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("课程名称:"), gbc);
        gbc.gridx = 1;
        String courseName = evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getCourse() != null ?
            evaluation.getCourseOffering().getCourse().getCourseName() : "未知课程";
        panel.add(new JLabel(courseName), gbc);

        // 授课教师
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("授课教师:"), gbc);
        gbc.gridx = 1;
        String teacherName = evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getTeacher() != null ?
            evaluation.getCourseOffering().getTeacher().getName() : "未知教师";
        panel.add(new JLabel(teacherName), gbc);

        // 班级信息
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("班级:"), gbc);
        gbc.gridx = 1;
        String className = evaluation.getCourseOffering() != null && evaluation.getCourseOffering().getClassRoom() != null ?
            evaluation.getCourseOffering().getClassRoom().getClassName() : "未知班级";
        panel.add(new JLabel(className), gbc);

        // 评教周期
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("评教周期:"), gbc);
        gbc.gridx = 1;
        String periodInfo = evaluation.getPeriod() != null ?
            evaluation.getPeriod().getPeriodName() + " (" + evaluation.getPeriod().getSemester() + ")" : "未知周期";
        panel.add(new JLabel(periodInfo), gbc);

        // 总分
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("总分:"), gbc);
        gbc.gridx = 1;
        JLabel totalScoreLabel = new JLabel(String.format("%.1f", evaluation.getTotalScore()));
        totalScoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        totalScoreLabel.setForeground(new Color(220, 20, 60));
        panel.add(totalScoreLabel, gbc);

        // 等级
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("等级:"), gbc);
        gbc.gridx = 1;
        String grade = managementUIHelper.getGradeByScore(evaluation.getTotalScore());
        JLabel gradeLabel = new JLabel(grade);
        gradeLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        gradeLabel.setForeground(managementUIHelper.getGradeColor(grade));
        panel.add(gradeLabel, gbc);

        // 评教时间
        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(new JLabel("评教时间:"), gbc);
        gbc.gridx = 1;
        String evaluationTime = evaluation.getEvaluationDate() != null ?
            evaluation.getEvaluationDate().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "未知时间";
        panel.add(new JLabel(evaluationTime), gbc);

        return panel;
    }

    /**
     * 创建评分详情面板
     */
    private JPanel createEvaluationScoresPanel(Evaluation evaluation) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建评分表格
        String[] columns = {"评教指标", "指标描述", "满分", "得分", "权重"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable scoresTable = new JTable(tableModel);
        scoresTable.setRowHeight(30);
        scoresTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        scoresTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        managementUIHelper.setupTableStyle(scoresTable);

        // 解析评分数据
        try {
            List<EvaluationCriteria> criteriaList = evaluationService.getAllEvaluationCriteria();

            for (EvaluationCriteria criteria : criteriaList) {
                Integer score = evaluation.getScore(criteria.getCriteriaId());
                Object[] row = {
                    criteria.getCriteriaName(),
                    criteria.getDescription(),
                    criteria.getMaxScore(),
                    score,
                    String.format("%.1f%%", criteria.getWeight())
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            System.err.println("解析评分数据时出错: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(scoresTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 总分信息
        JPanel summaryPanel = new JPanel(new FlowLayout());
        summaryPanel.add(new JLabel("总分: "));
        JLabel totalScoreLabel = new JLabel(String.format("%.1f", evaluation.getTotalScore()));
        totalScoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        totalScoreLabel.setForeground(new Color(220, 20, 60));
        summaryPanel.add(totalScoreLabel);

        summaryPanel.add(new JLabel("  等级: "));
        String grade = managementUIHelper.getGradeByScore(evaluation.getTotalScore());
        JLabel gradeLabel = new JLabel(grade);
        gradeLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        gradeLabel.setForeground(managementUIHelper.getGradeColor(grade));
        summaryPanel.add(gradeLabel);

        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建评价意见面板
     */
    private JPanel createEvaluationCommentsPanel(Evaluation evaluation) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("学生评价意见:");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(titleLabel, BorderLayout.NORTH);

        JTextArea commentsArea = new JTextArea();
        commentsArea.setText(evaluation.getComments() != null ? evaluation.getComments() : "该学生未填写评价意见");
        commentsArea.setEditable(false);
        commentsArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setBackground(new Color(248, 248, 248));
        commentsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(commentsArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }



    /**
     * 创建课程管理面板
     */
    private JPanel createCourseManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("课程管理"));

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addCourseButton = LayoutUtil.createButton("添加课程", LayoutUtil.SUCCESS_COLOR);
        JButton editCourseButton = LayoutUtil.createButton("编辑课程", LayoutUtil.WARNING_COLOR);
        JButton deleteCourseButton = LayoutUtil.createButton("删除课程", LayoutUtil.DANGER_COLOR);
        JButton manageOfferingsButton = LayoutUtil.createButton("开课管理", LayoutUtil.PRIMARY_COLOR);

        toolPanel.add(addCourseButton);
        toolPanel.add(editCourseButton);
        toolPanel.add(deleteCourseButton);
        toolPanel.add(manageOfferingsButton);

        panel.add(toolPanel, BorderLayout.NORTH);

        JScrollPane courseScrollPane = new JScrollPane(courseTable);
        courseScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(courseScrollPane, BorderLayout.CENTER);

        // 设置按钮事件
        addCourseButton.addActionListener(e -> managementUIHelper.showAddCourseDialog(this, this::loadCourseData));
        editCourseButton.addActionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow < 0) {
                managementUIHelper.showWarningMessage(this, "请选择要编辑的课程");
                return;
            }
            String courseId = (String) courseTableModel.getValueAt(selectedRow, 0);
            managementUIHelper.showEditCourseDialog(this, courseId, this::loadCourseData);
        });
        deleteCourseButton.addActionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow < 0) {
                managementUIHelper.showWarningMessage(this, "请选择要删除的课程");
                return;
            }
            String courseId = (String) courseTableModel.getValueAt(selectedRow, 0);
            managementUIHelper.deleteCourse(this, courseId, this::loadCourseData);
        });
        manageOfferingsButton.addActionListener(e -> showCourseOfferingsDialog());

        return panel;
    }

    /**
     * 创建评教管理面板
     */
    private JPanel createEvaluationManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("评教管理"));

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addPeriodButton = LayoutUtil.createButton("创建评教周期", LayoutUtil.SUCCESS_COLOR);
        JButton editPeriodButton = LayoutUtil.createButton("编辑周期", LayoutUtil.WARNING_COLOR);
        JButton manageCriteriaButton = LayoutUtil.createButton("评教指标管理", LayoutUtil.PRIMARY_COLOR);
        JButton evaluationStatsButton = LayoutUtil.createButton("评教统计", new Color(138, 43, 226));

        toolPanel.add(addPeriodButton);
        toolPanel.add(editPeriodButton);
        toolPanel.add(manageCriteriaButton);
        toolPanel.add(evaluationStatsButton);

        panel.add(toolPanel, BorderLayout.NORTH);

        JScrollPane evaluationScrollPane = new JScrollPane(evaluationPeriodTable);
        evaluationScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(evaluationScrollPane, BorderLayout.CENTER);

        // 设置按钮事件
        addPeriodButton.addActionListener(e -> managementUIHelper.showAddEvaluationPeriodDialog(this, this::loadEvaluationPeriodData));
        editPeriodButton.addActionListener(e -> {
            int selectedRow = evaluationPeriodTable.getSelectedRow();
            if (selectedRow < 0) {
                managementUIHelper.showWarningMessage(this, "请选择要编辑的评教周期");
                return;
            }
            String periodId = (String) evaluationPeriodTableModel.getValueAt(selectedRow, 0);
            managementUIHelper.showEditEvaluationPeriodDialog(this, periodId, this::loadEvaluationPeriodData);
        });
        manageCriteriaButton.addActionListener(e -> managementUIHelper.showEvaluationCriteriaDialog(this));
        evaluationStatsButton.addActionListener(e -> showEvaluationStatistics());

        return panel;
    }

    /**
     * 加载课程数据
     */
    private void loadCourseData() {
        courseTableModel.setRowCount(0);

        try {
            List<Course> courses = courseService.getAllCourses();
            for (Course course : courses) {
                Object[] row = {
                    course.getCourseId(),
                    course.getCourseName(),
                    course.getCredits(),
                    course.getCourseType(),
                    course.getCollege()
                };
                courseTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载课程数据失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载评教周期数据
     */
    private void loadEvaluationPeriodData() {
        evaluationPeriodTableModel.setRowCount(0);

        try {
            List<EvaluationPeriod> periods = evaluationService.getAllEvaluationPeriods();
            for (EvaluationPeriod period : periods) {
                Object[] row = {
                    period.getPeriodId(),
                    period.getPeriodName(),
                    period.getSemester(),
                    period.getStartDate().toString(),
                    period.getEndDate().toString(),
                    period.getStatus(),
                    0 // 评教数量，需要单独查询
                };
                evaluationPeriodTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载评教数据失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCourseOfferingsDialog() {
        JDialog dialog = new JDialog(this, "开课管理", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());

        // 开课列表
        String[] columns = {"开课编号", "课程名称", "授课教师", "班级", "学期", "上课时间"};
        DefaultTableModel offeringTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable offeringTable = new JTable(offeringTableModel);
        offeringTable.setRowHeight(30);
        offeringTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        offeringTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        managementUIHelper.setupTableStyle(offeringTable);

        // 加载开课数据
        loadCourseOfferingsData(offeringTableModel);

        JScrollPane scrollPane = new JScrollPane(offeringTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = LayoutUtil.createButton("添加开课", LayoutUtil.SUCCESS_COLOR);
        JButton editButton = LayoutUtil.createButton("编辑开课", LayoutUtil.WARNING_COLOR);
        JButton deleteButton = LayoutUtil.createButton("删除开课", LayoutUtil.DANGER_COLOR);
        JButton closeButton = LayoutUtil.createButton("关闭", LayoutUtil.PRIMARY_COLOR);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 按钮事件
        addButton.addActionListener(e -> showAddCourseOfferingDialog(dialog, offeringTableModel));
        editButton.addActionListener(e -> showEditCourseOfferingDialog(dialog, offeringTable, offeringTableModel));
        deleteButton.addActionListener(e -> {
            int selectedRow = offeringTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "请选择要删除的开课", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int option = JOptionPane.showConfirmDialog(dialog, "确定要删除选中的开课吗？", "确认删除",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                String offeringId = (String) offeringTableModel.getValueAt(selectedRow, 0);
                if (courseService.deleteCourseOffering(offeringId)) {
                    offeringTableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(dialog, "开课删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "开课删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 加载开课数据
     */
    private void loadCourseOfferingsData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        try {
            List<CourseOffering> offerings = courseService.getAllCourseOfferings();
            for (CourseOffering offering : offerings) {
                // 获取课程名称
                Course course = courseService.getCourseById(offering.getCourseId());
                String courseName = course != null ? course.getCourseName() : "未知课程";

                // 获取教师名称
                Teacher teacher = teacherService.getTeacherById(offering.getTeacherId());
                String teacherName = teacher != null ? teacher.getName() : "未知教师";

                // 获取班级名称
                ClassRoom classRoom = courseService.getClassRoomById(offering.getClassId());
                String className = classRoom != null ? classRoom.getClassName() : "未知班级";

                Object[] row = {
                    offering.getOfferingId(),
                    courseName,
                    teacherName,
                    className,
                    offering.getSemester(),
                    offering.getSchedule()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载开课数据失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 显示添加开课对话框
     */
    private void showAddCourseOfferingDialog(JDialog parentDialog, DefaultTableModel tableModel) {
        JDialog dialog = new JDialog(parentDialog, "添加开课", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(parentDialog);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField offeringIdField = new JTextField(15);
        JComboBox<String> courseComboBox = new JComboBox<>();
        JComboBox<String> teacherComboBox = new JComboBox<>();
        JComboBox<String> classComboBox = new JComboBox<>();
        JTextField semesterField = new JTextField(15);
        JTextField scheduleField = new JTextField(15);

        // 加载课程数据
        try {
            List<Course> courses = courseService.getAllCourses();
            for (Course course : courses) {
                courseComboBox.addItem(course.getCourseId() + " - " + course.getCourseName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载课程数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        // 加载教师数据
        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            for (Teacher teacher : teachers) {
                teacherComboBox.addItem(teacher.getTeacherId() + " - " + teacher.getName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载教师数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        // 加载班级数据
        try {
            List<ClassRoom> classes = courseService.getAllClassRooms();
            for (ClassRoom classRoom : classes) {
                classComboBox.addItem(classRoom.getClassId() + " - " + classRoom.getClassName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载班级数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("开课编号:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(offeringIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("课程:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(courseComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("授课教师:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(teacherComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("班级:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(classComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("学期:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(semesterField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("上课时间:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(scheduleField, gbc);

        // 按钮面板
        JButton saveButton = LayoutUtil.createButton("保存", LayoutUtil.SUCCESS_COLOR);
        JButton cancelButton = LayoutUtil.createButton("取消", LayoutUtil.DANGER_COLOR);
        JPanel buttonPanel = LayoutUtil.createButtonPanel(saveButton, cancelButton);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            try {
                String offeringId = offeringIdField.getText().trim();
                String courseSelection = (String) courseComboBox.getSelectedItem();
                String teacherSelection = (String) teacherComboBox.getSelectedItem();
                String classSelection = (String) classComboBox.getSelectedItem();
                String semester = semesterField.getText().trim();
                String schedule = scheduleField.getText().trim();

                if (offeringId.isEmpty() || courseSelection == null || teacherSelection == null ||
                    classSelection == null || semester.isEmpty() || schedule.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 解析选择的ID
                String courseId = courseSelection.split(" - ")[0];
                String teacherId = teacherSelection.split(" - ")[0];
                String classId = classSelection.split(" - ")[0];

                CourseOffering offering = new CourseOffering(offeringId, courseId, teacherId, classId, semester, schedule);

                if (courseService.createCourseOffering(offering)) {
                    // 添加到表格
                    Object[] row = {offeringId, courseSelection.split(" - ")[1], teacherSelection.split(" - ")[1],
                                   classSelection.split(" - ")[1], semester, schedule};
                    tableModel.addRow(row);

                    JOptionPane.showMessageDialog(dialog, "开课添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "开课添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加开课时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 显示编辑开课对话框
     */
    private void showEditCourseOfferingDialog(JDialog parentDialog, JTable offeringTable, DefaultTableModel tableModel) {
        int selectedRow = offeringTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parentDialog, "请选择要编辑的开课", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String offeringId = (String) tableModel.getValueAt(selectedRow, 0);

        try {
            CourseOffering offering = courseService.getCourseOfferingById(offeringId);
            if (offering == null) {
                JOptionPane.showMessageDialog(parentDialog, "开课信息不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(parentDialog, "编辑开课 - " + offeringId, true);
            dialog.setSize(450, 400);
            dialog.setLocationRelativeTo(parentDialog);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);

            JTextField offeringIdField = new JTextField(offering.getOfferingId(), 15);
            offeringIdField.setEditable(false);
            offeringIdField.setBackground(Color.LIGHT_GRAY);

            JComboBox<String> courseComboBox = new JComboBox<>();
            JComboBox<String> teacherComboBox = new JComboBox<>();
            JComboBox<String> classComboBox = new JComboBox<>();
            JTextField semesterField = new JTextField(offering.getSemester(), 15);
            JTextField scheduleField = new JTextField(offering.getSchedule(), 15);

            // 加载数据到下拉框
            try {
                List<Course> courses = courseService.getAllCourses();
                for (Course course : courses) {
                    String item = course.getCourseId() + " - " + course.getCourseName();
                    courseComboBox.addItem(item);
                    if (course.getCourseId().equals(offering.getCourseId())) {
                        courseComboBox.setSelectedItem(item);
                    }
                }

                List<Teacher> teachers = teacherService.getAllTeachers();
                for (Teacher teacher : teachers) {
                    String item = teacher.getTeacherId() + " - " + teacher.getName();
                    teacherComboBox.addItem(item);
                    if (teacher.getTeacherId().equals(offering.getTeacherId())) {
                        teacherComboBox.setSelectedItem(item);
                    }
                }

                List<ClassRoom> classes = courseService.getAllClassRooms();
                for (ClassRoom classRoom : classes) {
                    String item = classRoom.getClassId() + " - " + classRoom.getClassName();
                    classComboBox.addItem(item);
                    if (classRoom.getClassId().equals(offering.getClassId())) {
                        classComboBox.setSelectedItem(item);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog, "加载数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 布局组件
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel("开课编号:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(offeringIdField, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("课程:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(courseComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("授课教师:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(teacherComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("班级:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(classComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("学期:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(semesterField, gbc);

            gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("上课时间:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(scheduleField, gbc);

            // 按钮面板
            JButton saveButton = LayoutUtil.createButton("保存", LayoutUtil.SUCCESS_COLOR);
            JButton cancelButton = LayoutUtil.createButton("取消", LayoutUtil.DANGER_COLOR);
            JPanel buttonPanel = LayoutUtil.createButtonPanel(saveButton, cancelButton);

            gbc.gridx = 0; gbc.gridy = 6;
            gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(buttonPanel, gbc);

            // 保存按钮事件
            saveButton.addActionListener(e -> {
                try {
                    String courseSelection = (String) courseComboBox.getSelectedItem();
                    String teacherSelection = (String) teacherComboBox.getSelectedItem();
                    String classSelection = (String) classComboBox.getSelectedItem();
                    String semester = semesterField.getText().trim();
                    String schedule = scheduleField.getText().trim();

                    if (courseSelection == null || teacherSelection == null || classSelection == null ||
                        semester.isEmpty() || schedule.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "请填写所有必填字段", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 解析选择的ID
                    String courseId = courseSelection.split(" - ")[0];
                    String teacherId = teacherSelection.split(" - ")[0];
                    String classId = classSelection.split(" - ")[0];

                    offering.setCourseId(courseId);
                    offering.setTeacherId(teacherId);
                    offering.setClassId(classId);
                    offering.setSemester(semester);
                    offering.setSchedule(schedule);

                    if (courseService.updateCourseOffering(offering)) {
                        // 更新表格
                        tableModel.setValueAt(courseSelection.split(" - ")[1], selectedRow, 1);
                        tableModel.setValueAt(teacherSelection.split(" - ")[1], selectedRow, 2);
                        tableModel.setValueAt(classSelection.split(" - ")[1], selectedRow, 3);
                        tableModel.setValueAt(semester, selectedRow, 4);
                        tableModel.setValueAt(schedule, selectedRow, 5);

                        JOptionPane.showMessageDialog(dialog, "开课更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "开课更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "更新开课时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(e -> dialog.dispose());

            dialog.add(panel);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentDialog, "加载开课信息失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }






    private void showEvaluationStatistics() {
        JDialog dialog = new JDialog(this, "评教统计分析", true);
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // 顶部控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("统计条件"));

        JComboBox<String> periodComboBox = new JComboBox<>();
        JComboBox<String> statisticsTypeComboBox = new JComboBox<>(new String[]{
            "总体统计", "教师排名", "课程排名", "学院统计"
        });

        // 加载评教周期
        try {
            List<EvaluationPeriod> periods = evaluationService.getAllEvaluationPeriods();
            for (EvaluationPeriod period : periods) {
                periodComboBox.addItem(period.getPeriodId() + " - " + period.getPeriodName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载评教周期失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        JButton generateButton = LayoutUtil.createButton("生成统计", LayoutUtil.PRIMARY_COLOR);

        controlPanel.add(new JLabel("评教周期:"));
        controlPanel.add(periodComboBox);
        controlPanel.add(new JLabel("统计类型:"));
        controlPanel.add(statisticsTypeComboBox);
        controlPanel.add(generateButton);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // 统计结果显示区域
        JTabbedPane resultTabbedPane = new JTabbedPane();

        // 数据统计表格
        String[] columns = {"项目", "数值", "百分比", "排名", "备注"};
        DefaultTableModel statisticsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable statisticsTable = new JTable(statisticsTableModel);
        statisticsTable.setRowHeight(30);
        statisticsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        statisticsTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        managementUIHelper.setupTableStyle(statisticsTable);

        JScrollPane tableScrollPane = new JScrollPane(statisticsTable);
        resultTabbedPane.addTab("数据统计", tableScrollPane);

        // 详细分析文本区域
        JTextArea analysisArea = new JTextArea();
        analysisArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);

        JScrollPane analysisScrollPane = new JScrollPane(analysisArea);
        resultTabbedPane.addTab("分析报告", analysisScrollPane);

        mainPanel.add(resultTabbedPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("关闭");
        closeButton.setBackground(new Color(220, 20, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        generateButton.addActionListener(e -> {
            String selectedPeriod = (String) periodComboBox.getSelectedItem();
            String statisticsType = (String) statisticsTypeComboBox.getSelectedItem();

            if (selectedPeriod == null) {
                JOptionPane.showMessageDialog(dialog, "请选择评教周期", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String periodId = selectedPeriod.split(" - ")[0];
            generateStatisticsReport(periodId, statisticsType, statisticsTableModel, analysisArea);
        });



        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 生成统计报告
     */
    private void generateStatisticsReport(String periodId, String statisticsType, DefaultTableModel tableModel, JTextArea analysisArea) {
        tableModel.setRowCount(0);
        analysisArea.setText("");

        try {
            switch (statisticsType) {
                case "总体统计":
                    generateOverallStatistics(periodId, tableModel, analysisArea);
                    break;
                case "教师排名":
                    generateTeacherRanking(periodId, tableModel, analysisArea);
                    break;
                case "课程排名":
                    generateCourseRanking(periodId, tableModel, analysisArea);
                    break;

                case "学院统计":
                    generateCollegeStatistics(periodId, tableModel, analysisArea);
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "生成统计报告失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 生成总体统计
     */
    private void generateOverallStatistics(String periodId, DefaultTableModel tableModel, JTextArea analysisArea) {
        try {
            // 获取基本统计数据
            Map<String, Object> teacherStats = statisticsService.getTeacherEvaluationStatistics(periodId);
            Map<String, Object> courseStats = statisticsService.getCourseEvaluationStatistics(periodId);
            Map<String, Object> studentStats = statisticsService.getStudentEvaluationStatistics(periodId);

            // 添加统计数据到表格
            tableModel.addRow(new Object[]{"参与教师总数", teacherStats.getOrDefault("totalTeachers", 0), "100%", "-", "已完成评教的教师数量"});
            tableModel.addRow(new Object[]{"参与课程总数", courseStats.getOrDefault("totalCourses", 0), "100%", "-", "已完成评教的课程数量"});
            tableModel.addRow(new Object[]{"参与学生总数", studentStats.getOrDefault("participatedStudents", 0),
                String.format("%.1f%%", (Double) studentStats.getOrDefault("participationRate", 0.0)), "-", "参与评教的学生数量"});

            double overallAvgScore = (Double) teacherStats.getOrDefault("overallAvgScore", 0.0);
            tableModel.addRow(new Object[]{"总体平均分", String.format("%.2f", overallAvgScore), "-", managementUIHelper.getGradeByScore(overallAvgScore), "所有评教的平均分数"});

            // 分数段统计
            int excellentCount = getScoreRangeCount(periodId, 90, 100);
            int goodCount = getScoreRangeCount(periodId, 80, 89);
            int averageCount = getScoreRangeCount(periodId, 70, 79);
            int passCount = getScoreRangeCount(periodId, 60, 69);
            int failCount = getScoreRangeCount(periodId, 0, 59);
            int totalEvaluations = excellentCount + goodCount + averageCount + passCount + failCount;

            if (totalEvaluations > 0) {
                tableModel.addRow(new Object[]{"优秀(90-100分)", excellentCount, String.format("%.1f%%", excellentCount * 100.0 / totalEvaluations), "-", "评分在90分以上"});
                tableModel.addRow(new Object[]{"良好(80-89分)", goodCount, String.format("%.1f%%", goodCount * 100.0 / totalEvaluations), "-", "评分在80-89分"});
                tableModel.addRow(new Object[]{"中等(70-79分)", averageCount, String.format("%.1f%%", averageCount * 100.0 / totalEvaluations), "-", "评分在70-79分"});
                tableModel.addRow(new Object[]{"及格(60-69分)", passCount, String.format("%.1f%%", passCount * 100.0 / totalEvaluations), "-", "评分在60-69分"});
                tableModel.addRow(new Object[]{"不及格(60分以下)", failCount, String.format("%.1f%%", failCount * 100.0 / totalEvaluations), "-", "评分在60分以下"});
            }

            // 生成分析报告
            StringBuilder analysis = new StringBuilder();
            analysis.append("=== 评教总体统计分析报告 ===\n\n");
            analysis.append("一、基本情况\n");
            analysis.append(String.format("本次评教周期共有 %d 名教师、%d 门课程参与评教，",
                teacherStats.getOrDefault("totalTeachers", 0), courseStats.getOrDefault("totalCourses", 0)));
            analysis.append(String.format("学生参与率为 %.1f%%。\n\n", (Double) studentStats.getOrDefault("participationRate", 0.0)));

            analysis.append("二、评教结果分析\n");
            analysis.append(String.format("总体平均分为 %.2f 分，等级为 %s。\n", overallAvgScore, managementUIHelper.getGradeByScore(overallAvgScore)));

            if (totalEvaluations > 0) {
                analysis.append(String.format("其中优秀率为 %.1f%%，良好率为 %.1f%%，",
                    excellentCount * 100.0 / totalEvaluations, goodCount * 100.0 / totalEvaluations));
                analysis.append(String.format("中等率为 %.1f%%，及格率为 %.1f%%。\n\n",
                    averageCount * 100.0 / totalEvaluations, passCount * 100.0 / totalEvaluations));
            }

            analysis.append("三、建议\n");
            if (overallAvgScore >= 85) {
                analysis.append("整体评教结果良好，教学质量较高，建议继续保持并进一步提升。\n");
            } else if (overallAvgScore >= 75) {
                analysis.append("整体评教结果一般，建议加强教学质量监控，提升教学水平。\n");
            } else {
                analysis.append("整体评教结果偏低，建议重点关注教学质量，采取针对性改进措施。\n");
            }

            analysisArea.setText(analysis.toString());

        } catch (Exception e) {
            throw new RuntimeException("生成总体统计失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取分数段统计
     */
    private int getScoreRangeCount(String periodId, int minScore, int maxScore) {
        try {
            String sql = """
                SELECT COUNT(*) FROM evaluations e
                JOIN evaluation_periods ep ON e.period_id = ep.period_id
                WHERE e.period_id = ? AND e.total_score >= ? AND e.total_score <= ?
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, periodId);
                pstmt.setInt(2, minScore);
                pstmt.setInt(3, maxScore);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("获取分数段统计失败: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 生成教师排名统计
     */
    private void generateTeacherRanking(String periodId, DefaultTableModel tableModel, JTextArea analysisArea) {
        try {
            String sql = """
                SELECT t.teacher_id, t.name, t.title, t.college,
                       AVG(e.total_score) as avg_score, COUNT(e.evaluation_id) as eval_count
                FROM teachers t
                JOIN course_offerings co ON t.teacher_id = co.teacher_id
                JOIN evaluations e ON co.offering_id = e.offering_id
                WHERE e.period_id = ?
                GROUP BY t.teacher_id, t.name, t.title, t.college
                HAVING COUNT(e.evaluation_id) >= 3
                ORDER BY avg_score DESC
                LIMIT 20
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, periodId);
                ResultSet rs = pstmt.executeQuery();

                int rank = 1;
                StringBuilder analysis = new StringBuilder();
                analysis.append("=== 教师评教排名分析 ===\n\n");
                analysis.append("排名规则：至少3次评教记录，按平均分排序\n\n");

                while (rs.next()) {
                    String teacherId = rs.getString("teacher_id");
                    String name = rs.getString("name");
                    String title = rs.getString("title");
                    String college = rs.getString("college");
                    double avgScore = rs.getDouble("avg_score");
                    int evalCount = rs.getInt("eval_count");

                    tableModel.addRow(new Object[]{
                        name + "(" + teacherId + ")",
                        String.format("%.2f", avgScore),
                        title,
                        String.valueOf(rank),
                        college + " | " + evalCount + "次评教"
                    });

                    if (rank <= 10) {
                        analysis.append(String.format("第%d名：%s（%s），平均分%.2f，评教%d次\n",
                            rank, name, title, avgScore, evalCount));
                    }

                    rank++;
                }

                analysis.append("\n分析建议：\n");
                analysis.append("1. 排名前10的教师教学效果突出，可作为教学示范推广经验\n");
                analysis.append("2. 建议对排名靠后的教师进行教学指导和培训\n");
                analysis.append("3. 鼓励教师间相互学习，提升整体教学水平\n");

                analysisArea.setText(analysis.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("生成教师排名统计失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成课程排名统计
     */
    private void generateCourseRanking(String periodId, DefaultTableModel tableModel, JTextArea analysisArea) {
        try {
            String sql = """
                SELECT c.course_id, c.course_name, c.course_type, c.college,
                       AVG(e.total_score) as avg_score, COUNT(e.evaluation_id) as eval_count
                FROM courses c
                JOIN course_offerings co ON c.course_id = co.course_id
                JOIN evaluations e ON co.offering_id = e.offering_id
                WHERE e.period_id = ?
                GROUP BY c.course_id, c.course_name, c.course_type, c.college
                HAVING COUNT(e.evaluation_id) >= 5
                ORDER BY avg_score DESC
                LIMIT 20
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, periodId);
                ResultSet rs = pstmt.executeQuery();

                int rank = 1;
                StringBuilder analysis = new StringBuilder();
                analysis.append("=== 课程评教排名分析 ===\n\n");
                analysis.append("排名规则：至少5次评教记录，按平均分排序\n\n");

                while (rs.next()) {
                    String courseId = rs.getString("course_id");
                    String courseName = rs.getString("course_name");
                    String courseType = rs.getString("course_type");
                    String college = rs.getString("college");
                    double avgScore = rs.getDouble("avg_score");
                    int evalCount = rs.getInt("eval_count");

                    tableModel.addRow(new Object[]{
                        courseName + "(" + courseId + ")",
                        String.format("%.2f", avgScore),
                        courseType,
                        String.valueOf(rank),
                        college + " | " + evalCount + "次评教"
                    });

                    if (rank <= 10) {
                        analysis.append(String.format("第%d名：%s（%s），平均分%.2f，评教%d次\n",
                            rank, courseName, courseType, avgScore, evalCount));
                    }

                    rank++;
                }

                analysis.append("\n分析建议：\n");
                analysis.append("1. 排名前列的课程教学质量优秀，可总结教学经验\n");
                analysis.append("2. 关注不同课程类型的评教差异，针对性改进\n");
                analysis.append("3. 建议对评分较低的课程进行教学改革\n");

                analysisArea.setText(analysis.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("生成课程排名统计失败: " + e.getMessage(), e);
        }
    }



    /**
     * 生成学院统计
     */
    private void generateCollegeStatistics(String periodId, DefaultTableModel tableModel, JTextArea analysisArea) {
        try {
            String sql = """
                SELECT t.college,
                       AVG(e.total_score) as avg_score,
                       COUNT(e.evaluation_id) as eval_count,
                       COUNT(DISTINCT t.teacher_id) as teacher_count,
                       COUNT(DISTINCT c.course_id) as course_count
                FROM teachers t
                JOIN course_offerings co ON t.teacher_id = co.teacher_id
                JOIN courses c ON co.course_id = c.course_id
                JOIN evaluations e ON co.offering_id = e.offering_id
                WHERE e.period_id = ?
                GROUP BY t.college
                ORDER BY avg_score DESC
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, periodId);
                ResultSet rs = pstmt.executeQuery();

                int rank = 1;
                StringBuilder analysis = new StringBuilder();
                analysis.append("=== 学院评教统计分析 ===\n\n");

                while (rs.next()) {
                    String college = rs.getString("college");
                    double avgScore = rs.getDouble("avg_score");
                    int evalCount = rs.getInt("eval_count");
                    int teacherCount = rs.getInt("teacher_count");
                    int courseCount = rs.getInt("course_count");

                    tableModel.addRow(new Object[]{
                        college,
                        String.format("%.2f", avgScore),
                        String.format("%.1f", evalCount * 1.0 / teacherCount),
                        String.valueOf(rank),
                        String.format("教师%d人，课程%d门", teacherCount, courseCount)
                    });

                    analysis.append(String.format("第%d名：%s，平均分%.2f，教师%d人，课程%d门\n",
                        rank, college, avgScore, teacherCount, courseCount));

                    rank++;
                }

                analysis.append("\n分析建议：\n");
                analysis.append("1. 各学院应加强教学质量管理，提升整体水平\n");
                analysis.append("2. 学院间可相互学习优秀教学管理经验\n");
                analysis.append("3. 建议制定学院教学质量提升计划\n");

                analysisArea.setText(analysis.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("生成学院统计失败: " + e.getMessage(), e);
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
