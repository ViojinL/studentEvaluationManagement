package edu.ai.haut.ui.admin;

import edu.ai.haut.model.*;
import edu.ai.haut.service.*;
import edu.ai.haut.ui.LoginFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * 管理员主界面
 * 提供系统管理、用户管理、评教管理等功能
 */
public class AdminMainFrame extends JFrame {
    
    private User currentUser;
    private Administrator currentAdmin;
    
    private StudentService studentService;
    private TeacherService teacherService;
    private ClassService classService;
    private CourseService courseService;
    private EvaluationService evaluationService;
    private StatisticsService statisticsService;
    private UserService userService;
    
    private JTabbedPane tabbedPane;
    private JTable userTable;
    private JTable courseTable;
    private JTable evaluationTable;
    private DefaultTableModel userTableModel;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel evaluationTableModel;
    
    private JLabel welcomeLabel;
    private JLabel statusLabel;
    private JComboBox<String> userTypeComboBox;
    
    public AdminMainFrame(User user) {
        this.currentUser = user;
        this.currentAdmin = (Administrator) user;
        
        this.studentService = new StudentService();
        this.teacherService = new TeacherService();
        this.classService = new ClassService();
        this.courseService = new CourseService();
        this.evaluationService = new EvaluationService();
        this.statisticsService = new StatisticsService();
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
        welcomeLabel = new JLabel("欢迎，" + currentAdmin.getName() + " 管理员！");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        
        // 状态标签
        statusLabel = new JLabel("管理员ID：" + currentAdmin.getAdminId());
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        
        // 用户类型选择
        userTypeComboBox = new JComboBox<>(new String[]{"学生", "教师", "教务人员", "管理员"});
        userTypeComboBox.setPreferredSize(new Dimension(120, 25));
        
        // 用户管理表格
        String[] userColumns = {"用户ID", "姓名", "性别", "类型", "详细信息", "创建时间"};
        userTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setRowHeight(30);
        userTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        userTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 课程管理表格
        String[] courseColumns = {"课程编号", "课程名称", "学分", "课程类型", "开课学院", "开课数量"};
        courseTableModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(courseTableModel);
        courseTable.setRowHeight(30);
        courseTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        courseTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 评教管理表格
        String[] evaluationColumns = {"周期编号", "周期名称", "学期", "开始日期", "结束日期", "状态", "评教数量"};
        evaluationTableModel = new DefaultTableModel(evaluationColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        evaluationTable = new JTable(evaluationTableModel);
        evaluationTable.setRowHeight(30);
        evaluationTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        evaluationTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 设置表格样式
        setupTableStyle(userTable);
        setupTableStyle(courseTable);
        setupTableStyle(evaluationTable);
    }
    
    /**
     * 设置表格样式
     */
    private void setupTableStyle(JTable table) {
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 230, 230));
        table.getTableHeader().setBackground(new Color(240, 248, 255));
        table.getTableHeader().setForeground(Color.BLACK);
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
        JButton systemStatsButton = new JButton("系统统计");
        JButton logoutButton = new JButton("退出登录");

        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);

        systemStatsButton.setBackground(new Color(60, 179, 113));
        systemStatsButton.setForeground(Color.WHITE);
        systemStatsButton.setFocusPainted(false);
        
        logoutButton.setBackground(new Color(220, 20, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(systemStatsButton);
        buttonPanel.add(logoutButton);
        
        topPanel.add(infoPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // 用户管理面板
        JPanel userPanel = createUserManagementPanel();
        
        // 课程管理面板
        JPanel coursePanel = createCourseManagementPanel();
        
        // 评教管理面板
        JPanel evaluationPanel = createEvaluationManagementPanel();
        
        // 添加到选项卡
        tabbedPane.addTab("用户管理", userPanel);
        tabbedPane.addTab("课程管理", coursePanel);
        tabbedPane.addTab("评教管理", evaluationPanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        refreshButton.addActionListener(e -> loadData());
        systemStatsButton.addActionListener(e -> showSystemStatistics());
        logoutButton.addActionListener(e -> logout());
    }
    
    /**
     * 创建用户管理面板
     */
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("用户管理"));
        
        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolPanel.add(new JLabel("用户类型:"));
        toolPanel.add(userTypeComboBox);
        
        JButton queryUsersButton = new JButton("查询");
        JButton addUserButton = new JButton("添加用户");
        JButton editUserButton = new JButton("编辑用户");
        JButton deleteUserButton = new JButton("删除用户");
        
        queryUsersButton.setBackground(new Color(70, 130, 180));
        queryUsersButton.setForeground(Color.WHITE);
        queryUsersButton.setFocusPainted(false);
        
        addUserButton.setBackground(new Color(60, 179, 113));
        addUserButton.setForeground(Color.WHITE);
        addUserButton.setFocusPainted(false);
        
        editUserButton.setBackground(new Color(255, 165, 0));
        editUserButton.setForeground(Color.WHITE);
        editUserButton.setFocusPainted(false);
        
        deleteUserButton.setBackground(new Color(220, 20, 60));
        deleteUserButton.setForeground(Color.WHITE);
        deleteUserButton.setFocusPainted(false);
        
        toolPanel.add(queryUsersButton);
        toolPanel.add(addUserButton);
        toolPanel.add(editUserButton);
        toolPanel.add(deleteUserButton);
        
        panel.add(toolPanel, BorderLayout.NORTH);
        
        JScrollPane userScrollPane = new JScrollPane(userTable);
        userScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(userScrollPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        queryUsersButton.addActionListener(e -> loadUserData());
        addUserButton.addActionListener(e -> showAddUserDialog());
        editUserButton.addActionListener(e -> showEditUserDialog());
        deleteUserButton.addActionListener(e -> deleteSelectedUser());
        
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
        
        JButton addCourseButton = new JButton("添加课程");
        JButton editCourseButton = new JButton("编辑课程");
        JButton deleteCourseButton = new JButton("删除课程");
        JButton manageOfferingsButton = new JButton("开课管理");
        
        addCourseButton.setBackground(new Color(60, 179, 113));
        addCourseButton.setForeground(Color.WHITE);
        addCourseButton.setFocusPainted(false);
        
        editCourseButton.setBackground(new Color(255, 165, 0));
        editCourseButton.setForeground(Color.WHITE);
        editCourseButton.setFocusPainted(false);
        
        deleteCourseButton.setBackground(new Color(220, 20, 60));
        deleteCourseButton.setForeground(Color.WHITE);
        deleteCourseButton.setFocusPainted(false);
        
        manageOfferingsButton.setBackground(new Color(70, 130, 180));
        manageOfferingsButton.setForeground(Color.WHITE);
        manageOfferingsButton.setFocusPainted(false);
        
        toolPanel.add(addCourseButton);
        toolPanel.add(editCourseButton);
        toolPanel.add(deleteCourseButton);
        toolPanel.add(manageOfferingsButton);
        
        panel.add(toolPanel, BorderLayout.NORTH);
        
        JScrollPane courseScrollPane = new JScrollPane(courseTable);
        courseScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(courseScrollPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        addCourseButton.addActionListener(e -> showAddCourseDialog());
        editCourseButton.addActionListener(e -> showEditCourseDialog());
        deleteCourseButton.addActionListener(e -> deleteSelectedCourse());
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
        
        JButton addPeriodButton = new JButton("创建评教周期");
        JButton editPeriodButton = new JButton("编辑周期");
        JButton manageCriteriaButton = new JButton("评教指标管理");
        JButton evaluationStatsButton = new JButton("评教统计");
        
        addPeriodButton.setBackground(new Color(60, 179, 113));
        addPeriodButton.setForeground(Color.WHITE);
        addPeriodButton.setFocusPainted(false);
        
        editPeriodButton.setBackground(new Color(255, 165, 0));
        editPeriodButton.setForeground(Color.WHITE);
        editPeriodButton.setFocusPainted(false);
        
        manageCriteriaButton.setBackground(new Color(70, 130, 180));
        manageCriteriaButton.setForeground(Color.WHITE);
        manageCriteriaButton.setFocusPainted(false);
        
        evaluationStatsButton.setBackground(new Color(138, 43, 226));
        evaluationStatsButton.setForeground(Color.WHITE);
        evaluationStatsButton.setFocusPainted(false);
        
        toolPanel.add(addPeriodButton);
        toolPanel.add(editPeriodButton);
        toolPanel.add(manageCriteriaButton);
        toolPanel.add(evaluationStatsButton);
        
        panel.add(toolPanel, BorderLayout.NORTH);
        
        JScrollPane evaluationScrollPane = new JScrollPane(evaluationTable);
        evaluationScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(evaluationScrollPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        addPeriodButton.addActionListener(e -> showAddEvaluationPeriodDialog());
        editPeriodButton.addActionListener(e -> showEditEvaluationPeriodDialog());
        manageCriteriaButton.addActionListener(e -> showEvaluationCriteriaDialog());
        evaluationStatsButton.addActionListener(e -> showEvaluationStatistics());
        
        return panel;
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 用户类型选择变化事件
        userTypeComboBox.addActionListener(e -> loadUserData());
    }
    
    /**
     * 设置窗口属性
     */
    private void setupFrame() {
        setTitle("学生评教管理系统 - 管理员端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        loadUserData();
        loadCourseData();
        loadEvaluationData();
    }
    
    /**
     * 加载用户数据
     */
    private void loadUserData() {
        userTableModel.setRowCount(0);
        
        try {
            String userType = (String) userTypeComboBox.getSelectedItem();
            
            switch (userType) {
                case "学生":
                    loadStudentData();
                    break;
                case "教师":
                    loadTeacherData();
                    break;
                case "教务人员":
                    loadStaffData();
                    break;
                case "管理员":
                    loadAdminData();
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载用户数据失败: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载学生数据
     */
    private void loadStudentData() {
        List<Student> students = studentService.getAllStudents();
        for (Student student : students) {
            Object[] row = {
                student.getStudentId(),
                student.getName(),
                student.getGender(),
                "学生",
                String.format("年级:%s 专业:%s 班级:%s", student.getGrade(), student.getMajor(), student.getClassId()),
                student.getCreatedAt() != null ? student.getCreatedAt().toLocalDate().toString() : ""
            };
            userTableModel.addRow(row);
        }
    }
    
    /**
     * 加载教师数据
     */
    private void loadTeacherData() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        for (Teacher teacher : teachers) {
            Object[] row = {
                teacher.getTeacherId(),
                teacher.getName(),
                teacher.getGender(),
                "教师",
                String.format("职称:%s 学院:%s", teacher.getTitle(), teacher.getCollege()),
                teacher.getCreatedAt() != null ? teacher.getCreatedAt().toLocalDate().toString() : ""
            };
            userTableModel.addRow(row);
        }
    }
    
    /**
     * 加载教务人员数据
     */
    private void loadStaffData() {
        try {
            AcademicAffairsStaffService staffService = new AcademicAffairsStaffService();
            List<AcademicAffairsStaff> staffList = staffService.getAllStaff();
            for (AcademicAffairsStaff staff : staffList) {
                Object[] row = {
                    staff.getStaffId(),
                    staff.getName(),
                    staff.getGender(),
                    "教务人员",
                    String.format("部门:%s 职位:%s", staff.getDepartment(), staff.getPosition()),
                    staff.getCreatedAt() != null ? staff.getCreatedAt().toLocalDate().toString() : ""
                };
                userTableModel.addRow(row);
            }
        } catch (Exception e) {
            System.err.println("加载教务人员数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 加载管理员数据
     */
    private void loadAdminData() {
        try {
            AdministratorService adminService = new AdministratorService();
            List<Administrator> adminList = adminService.getAllAdministrators();
            for (Administrator admin : adminList) {
                Object[] row = {
                    admin.getAdminId(),
                    admin.getName(),
                    admin.getGender(),
                    "管理员",
                    "系统管理员",
                    admin.getCreatedAt() != null ? admin.getCreatedAt().toLocalDate().toString() : ""
                };
                userTableModel.addRow(row);
            }
        } catch (Exception e) {
            System.err.println("加载管理员数据失败: " + e.getMessage());
        }
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
                    course.getCollege(),
                    0 // 开课数量，需要单独查询
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
                evaluationTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载评教数据失败: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 显示添加用户对话框
     */
    private void showAddUserDialog() {
        String userType = (String) userTypeComboBox.getSelectedItem();

        JDialog dialog = new JDialog(this, "添加" + userType, true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // 通用字段
        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"男", "女"});
        JPasswordField passwordField = new JPasswordField(15);
        JTextField contactField = new JTextField(15);

        // 添加通用字段
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(userType + "ID:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("姓名:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("性别:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(genderComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("联系方式:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(contactField, gbc);

        // 根据用户类型添加特定字段
        int currentRow = 5;
        JTextField extraField1 = new JTextField(15);
        JTextField extraField2 = new JTextField(15);
        JTextField extraField3 = new JTextField(15);
        JComboBox<String> titleComboBox = new JComboBox<>(new String[]{"教授", "副教授", "讲师", "助教", "实验师"});
        JComboBox<String> departmentComboBox = new JComboBox<>(new String[]{"教务处"});

        switch (userType) {
            case "学生":
                gbc.gridx = 0; gbc.gridy = currentRow++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                panel.add(new JLabel("年级:"), gbc);
                gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                panel.add(extraField1, gbc);

                gbc.gridx = 0; gbc.gridy = currentRow++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                panel.add(new JLabel("专业:"), gbc);
                gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                panel.add(extraField2, gbc);

                gbc.gridx = 0; gbc.gridy = currentRow++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                panel.add(new JLabel("班级:"), gbc);
                gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                panel.add(extraField3, gbc);
                break;

            case "教师":
                gbc.gridx = 0; gbc.gridy = currentRow++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                panel.add(new JLabel("职称:"), gbc);
                gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                panel.add(titleComboBox, gbc);

                gbc.gridx = 0; gbc.gridy = currentRow++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                panel.add(new JLabel("学院:"), gbc);
                gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                panel.add(extraField2, gbc);
                break;

            case "教务人员":
                gbc.gridx = 0; gbc.gridy = currentRow++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                panel.add(new JLabel("部门:"), gbc);
                gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                panel.add(departmentComboBox, gbc);

                gbc.gridx = 0; gbc.gridy = currentRow++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                panel.add(new JLabel("职位:"), gbc);
                gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                panel.add(extraField2, gbc);
                break;
        }

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = currentRow;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String gender = (String) genderComboBox.getSelectedItem();
                String password = new String(passwordField.getPassword());
                String contact = contactField.getText().trim();

                if (id.isEmpty() || name.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有必填字段", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = false;
                switch (userType) {
                    case "学生":
                        String grade = extraField1.getText().trim();
                        String major = extraField2.getText().trim();
                        String className = extraField3.getText().trim();

                        // 检查或创建班级
                        ClassRoom classRoom = classService.getClassByName(className);
                        if (classRoom == null) {
                            classRoom = new ClassRoom();
                            classRoom.setClassId("C" + System.currentTimeMillis());
                            classRoom.setClassName(className);
                            classRoom.setGrade(grade);
                            classRoom.setMajor(major);
                            classRoom.setCollege("信息科学与工程学院");
                            classService.createClass(classRoom);
                        }

                        Student student = new Student(id, name, gender, grade, major, classRoom.getClassId(), contact, password);
                        success = studentService.registerStudent(student);
                        break;

                    case "教师":
                        String title = (String) titleComboBox.getSelectedItem();
                        String college = extraField2.getText().trim();
                        Teacher teacher = new Teacher(id, name, gender, title, college, contact, password);
                        success = teacherService.registerTeacher(teacher);
                        break;

                    case "教务人员":
                        String department = (String) departmentComboBox.getSelectedItem();
                        String position = extraField2.getText().trim();
                        AcademicAffairsStaff staff = new AcademicAffairsStaff(id, name, gender, department, position, contact, password);
                        success = new AcademicAffairsStaffService().registerStaff(staff);
                        break;
                }

                if (success) {
                    JOptionPane.showMessageDialog(dialog, userType + "添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUserData(); // 刷新用户列表
                } else {
                    JOptionPane.showMessageDialog(dialog, userType + "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加用户时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示编辑用户对话框
     */
    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的用户", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        String userName = (String) userTableModel.getValueAt(selectedRow, 1);
        String userType = (String) userTableModel.getValueAt(selectedRow, 3);

        JDialog dialog = new JDialog(this, "编辑" + userType + " - " + userName, true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // 显示用户ID（不可编辑）
        JTextField idField = new JTextField(userId, 15);
        idField.setEditable(false);
        idField.setBackground(Color.LIGHT_GRAY);

        JTextField nameField = new JTextField(userName, 15);
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"男", "女"});
        JTextField contactField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(userType + "ID:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("姓名:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("性别:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(genderComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("联系方式:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(contactField, gbc);

        // 根据用户类型加载现有数据
        try {
            switch (userType) {
                case "学生":
                    Student student = studentService.getStudentById(userId);
                    if (student != null) {
                        genderComboBox.setSelectedItem(student.getGender());
                        contactField.setText(student.getContact());
                    }
                    break;
                case "教师":
                    Teacher teacher = teacherService.getTeacherById(userId);
                    if (teacher != null) {
                        genderComboBox.setSelectedItem(teacher.getGender());
                        contactField.setText(teacher.getContact());
                    }
                    break;
                case "教务人员":
                    AcademicAffairsStaff staff = new AcademicAffairsStaffService().getStaffById(userId);
                    if (staff != null) {
                        genderComboBox.setSelectedItem(staff.getGender());
                        contactField.setText(staff.getContact());
                    }
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载用户数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String gender = (String) genderComboBox.getSelectedItem();
                String contact = contactField.getText().trim();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "姓名不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = false;
                switch (userType) {
                    case "学生":
                        Student student = studentService.getStudentById(userId);
                        if (student != null) {
                            student.setName(name);
                            student.setGender(gender);
                            student.setContact(contact);
                            success = studentService.updateStudent(student);
                        }
                        break;
                    case "教师":
                        Teacher teacher = teacherService.getTeacherById(userId);
                        if (teacher != null) {
                            teacher.setName(name);
                            teacher.setGender(gender);
                            teacher.setContact(contact);
                            success = teacherService.updateTeacher(teacher);
                        }
                        break;
                    case "教务人员":
                        AcademicAffairsStaff staff = new AcademicAffairsStaffService().getStaffById(userId);
                        if (staff != null) {
                            staff.setName(name);
                            staff.setGender(gender);
                            staff.setContact(contact);
                            success = new AcademicAffairsStaffService().updateStaff(staff);
                        }
                        break;
                }

                if (success) {
                    JOptionPane.showMessageDialog(dialog, userType + "信息更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUserData(); // 刷新用户列表
                } else {
                    JOptionPane.showMessageDialog(dialog, userType + "信息更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "更新用户信息时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * 删除选中的用户
     */
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的用户", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        String userName = (String) userTableModel.getValueAt(selectedRow, 1);
        String userType = (String) userTableModel.getValueAt(selectedRow, 3);

        // 确认删除
        int option = JOptionPane.showConfirmDialog(this,
            String.format("确定要删除%s：%s (%s) 吗？\n此操作不可撤销！", userType, userName, userId),
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success = false;
            switch (userType) {
                case "学生":
                    success = studentService.deleteStudent(userId);
                    break;
                case "教师":
                    success = teacherService.deleteTeacher(userId);
                    break;
                case "教务人员":
                    success = new AcademicAffairsStaffService().deleteStaff(userId);
                    break;
                case "管理员":
                    // 管理员删除需要特殊处理，暂不实现
                    JOptionPane.showMessageDialog(this, "管理员账户不能删除", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
            }

            if (success) {
                JOptionPane.showMessageDialog(this, userType + "删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadUserData(); // 刷新用户列表
            } else {
                JOptionPane.showMessageDialog(this, userType + "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除用户时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 显示添加课程对话框
     */
    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "添加课程", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField courseIdField = new JTextField(15);
        JTextField courseNameField = new JTextField(15);
        JTextField creditsField = new JTextField(15);
        JComboBox<String> courseTypeComboBox = new JComboBox<>(new String[]{"必修", "选修", "实践"});
        JTextField collegeField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("课程编号:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(courseIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("课程名称:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(courseNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("学分:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(creditsField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("课程类型:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(courseTypeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("开课学院:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(collegeField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            try {
                String courseId = courseIdField.getText().trim();
                String courseName = courseNameField.getText().trim();
                String creditsText = creditsField.getText().trim();
                String courseType = (String) courseTypeComboBox.getSelectedItem();
                String college = collegeField.getText().trim();

                if (courseId.isEmpty() || courseName.isEmpty() || creditsText.isEmpty() || college.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double credits;
                try {
                    credits = Double.parseDouble(creditsText);
                    if (credits <= 0) {
                        JOptionPane.showMessageDialog(dialog, "学分必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "学分格式不正确", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Course course = new Course(courseId, courseName, credits, courseType, college);

                if (courseService.createCourse(course)) {
                    JOptionPane.showMessageDialog(dialog, "课程添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadCourseData(); // 刷新课程列表
                } else {
                    JOptionPane.showMessageDialog(dialog, "课程添加失败，可能课程编号已存在", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加课程时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示编辑课程对话框
     */
    private void showEditCourseDialog() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的课程", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = (String) courseTableModel.getValueAt(selectedRow, 0);

        try {
            Course course = courseService.getCourseById(courseId);
            if (course == null) {
                JOptionPane.showMessageDialog(this, "课程信息不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(this, "编辑课程 - " + course.getCourseName(), true);
            dialog.setSize(400, 350);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);

            JTextField courseIdField = new JTextField(course.getCourseId(), 15);
            courseIdField.setEditable(false);
            courseIdField.setBackground(Color.LIGHT_GRAY);

            JTextField courseNameField = new JTextField(course.getCourseName(), 15);
            JTextField creditsField = new JTextField(String.valueOf(course.getCredits()), 15);
            JComboBox<String> courseTypeComboBox = new JComboBox<>(new String[]{"必修", "选修", "实践"});
            courseTypeComboBox.setSelectedItem(course.getCourseType());
            JTextField collegeField = new JTextField(course.getCollege(), 15);

            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel("课程编号:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(courseIdField, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("课程名称:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(courseNameField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("学分:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(creditsField, gbc);

            gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("课程类型:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(courseTypeComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("开课学院:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(collegeField, gbc);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");

            saveButton.setBackground(new Color(60, 179, 113));
            saveButton.setForeground(Color.WHITE);
            saveButton.setFocusPainted(false);

            cancelButton.setBackground(new Color(220, 20, 60));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setFocusPainted(false);

            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            gbc.gridx = 0; gbc.gridy = 5;
            gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(buttonPanel, gbc);

            // 保存按钮事件
            saveButton.addActionListener(e -> {
                try {
                    String courseName = courseNameField.getText().trim();
                    String creditsText = creditsField.getText().trim();
                    String courseType = (String) courseTypeComboBox.getSelectedItem();
                    String college = collegeField.getText().trim();

                    if (courseName.isEmpty() || creditsText.isEmpty() || college.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double credits;
                    try {
                        credits = Double.parseDouble(creditsText);
                        if (credits <= 0) {
                            JOptionPane.showMessageDialog(dialog, "学分必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "学分格式不正确", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    course.setCourseName(courseName);
                    course.setCredits(credits);
                    course.setCourseType(courseType);
                    course.setCollege(college);

                    // 这里需要在CourseService中添加updateCourse方法
                    JOptionPane.showMessageDialog(dialog, "课程更新功能需要在CourseService中实现updateCourse方法", "提示", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "更新课程时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(e -> dialog.dispose());

            dialog.add(panel);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载课程信息失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 删除选中的课程
     */
    private void deleteSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的课程", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = (String) courseTableModel.getValueAt(selectedRow, 0);
        String courseName = (String) courseTableModel.getValueAt(selectedRow, 1);

        // 确认删除
        int option = JOptionPane.showConfirmDialog(this,
            String.format("确定要删除课程：%s (%s) 吗？\n此操作不可撤销！", courseName, courseId),
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // 这里需要在CourseService中添加deleteCourse方法
            JOptionPane.showMessageDialog(this, "删除课程功能需要在CourseService中实现deleteCourse方法", "提示", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除课程时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 显示开课管理对话框
     */
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
        offeringTable.setRowHeight(25);

        // 加载开课信息（这里需要实现获取所有开课信息的方法）
        JScrollPane scrollPane = new JScrollPane(offeringTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("添加开课");
        JButton editButton = new JButton("编辑开课");
        JButton deleteButton = new JButton("删除开课");
        JButton closeButton = new JButton("关闭");

        addButton.setBackground(new Color(60, 179, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);

        editButton.setBackground(new Color(255, 165, 0));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);

        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);

        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 按钮事件
        addButton.addActionListener(e -> showAddCourseOfferingDialog(dialog, offeringTableModel));
        editButton.addActionListener(e -> JOptionPane.showMessageDialog(dialog, "编辑开课功能待实现", "提示", JOptionPane.INFORMATION_MESSAGE));
        deleteButton.addActionListener(e -> JOptionPane.showMessageDialog(dialog, "删除开课功能待实现", "提示", JOptionPane.INFORMATION_MESSAGE));
        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
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

        // 加载课程、教师、班级数据
        try {
            List<Course> courses = courseService.getAllCourses();
            for (Course course : courses) {
                courseComboBox.addItem(course.getCourseId() + " - " + course.getCourseName());
            }

            List<Teacher> teachers = teacherService.getAllTeachers();
            for (Teacher teacher : teachers) {
                teacherComboBox.addItem(teacher.getTeacherId() + " - " + teacher.getName());
            }

            List<ClassRoom> classes = classService.getAllClasses();
            for (ClassRoom classRoom : classes) {
                classComboBox.addItem(classRoom.getClassId() + " - " + classRoom.getClassName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
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
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

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
     * 显示创建评教周期对话框
     */
    private void showAddEvaluationPeriodDialog() {
        JDialog dialog = new JDialog(this, "创建评教周期", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField periodIdField = new JTextField(15);
        JTextField periodNameField = new JTextField(15);
        JTextField semesterField = new JTextField(15);

        // 日期选择组件（简化版）
        JTextField startDateField = new JTextField("2024-01-01", 15);
        JTextField endDateField = new JTextField("2024-01-31", 15);

        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"未开始", "进行中", "已完成", "已关闭"});

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("周期编号:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(periodIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("周期名称:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(periodNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("学期:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(semesterField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("开始日期:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(startDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("结束日期:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(endDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("状态:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(statusComboBox, gbc);

        // 提示信息
        JLabel tipLabel = new JLabel("<html><font color='gray'>日期格式：YYYY-MM-DD</font></html>");
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tipLabel, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            try {
                String periodId = periodIdField.getText().trim();
                String periodName = periodNameField.getText().trim();
                String semester = semesterField.getText().trim();
                String startDateText = startDateField.getText().trim();
                String endDateText = endDateField.getText().trim();
                String status = (String) statusComboBox.getSelectedItem();

                if (periodId.isEmpty() || periodName.isEmpty() || semester.isEmpty() ||
                    startDateText.isEmpty() || endDateText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 解析日期
                java.time.LocalDate startDate;
                java.time.LocalDate endDate;

                try {
                    startDate = java.time.LocalDate.parse(startDateText);
                } catch (java.time.format.DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "开始日期格式不正确：" + startDateText + "\n请使用YYYY-MM-DD格式，例如：2025-01-01", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    endDate = java.time.LocalDate.parse(endDateText);
                } catch (java.time.format.DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "结束日期格式不正确：" + endDateText + "\n请使用YYYY-MM-DD格式，例如：2025-06-30\n注意：6月只有30天，请检查日期是否有效", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (startDate.isAfter(endDate)) {
                    JOptionPane.showMessageDialog(dialog, "开始日期不能晚于结束日期", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                EvaluationPeriod period = new EvaluationPeriod(periodId, periodName, semester, startDate, endDate, status);

                if (evaluationService.createEvaluationPeriod(period)) {
                    JOptionPane.showMessageDialog(dialog, "评教周期创建成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadEvaluationData(); // 刷新评教周期列表
                } else {
                    JOptionPane.showMessageDialog(dialog, "评教周期创建失败，可能周期编号已存在", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "日期解析错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "创建评教周期时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示编辑评教周期对话框
     */
    private void showEditEvaluationPeriodDialog() {
        int selectedRow = evaluationTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的评教周期", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String periodId = (String) evaluationTableModel.getValueAt(selectedRow, 0);
        String periodName = (String) evaluationTableModel.getValueAt(selectedRow, 1);
        String semester = (String) evaluationTableModel.getValueAt(selectedRow, 2);
        String startDate = (String) evaluationTableModel.getValueAt(selectedRow, 3);
        String endDate = (String) evaluationTableModel.getValueAt(selectedRow, 4);
        String status = (String) evaluationTableModel.getValueAt(selectedRow, 5);

        JDialog dialog = new JDialog(this, "编辑评教周期 - " + periodName, true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField periodIdField = new JTextField(periodId, 15);
        periodIdField.setEditable(false);
        periodIdField.setBackground(Color.LIGHT_GRAY);

        JTextField periodNameField = new JTextField(periodName, 15);
        JTextField semesterField = new JTextField(semester, 15);
        JTextField startDateField = new JTextField(startDate, 15);
        JTextField endDateField = new JTextField(endDate, 15);
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"未开始", "进行中", "已完成", "已关闭"});
        statusComboBox.setSelectedItem(status);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("周期编号:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(periodIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("周期名称:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(periodNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("学期:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(semesterField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("开始日期:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(startDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("结束日期:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(endDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("状态:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(statusComboBox, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            try {
                String newPeriodName = periodNameField.getText().trim();
                String newSemester = semesterField.getText().trim();
                String newStartDate = startDateField.getText().trim();
                String newEndDate = endDateField.getText().trim();
                String newStatus = (String) statusComboBox.getSelectedItem();

                if (newPeriodName.isEmpty() || newSemester.isEmpty() || newStartDate.isEmpty() || newEndDate.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 验证日期格式
                try {
                    java.time.LocalDate.parse(newStartDate);
                } catch (java.time.format.DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "开始日期格式不正确：" + newStartDate + "\n请使用YYYY-MM-DD格式，例如：2025-01-01", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    java.time.LocalDate.parse(newEndDate);
                } catch (java.time.format.DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "结束日期格式不正确：" + newEndDate + "\n请使用YYYY-MM-DD格式，例如：2025-06-30\n注意：请检查日期是否有效（如6月只有30天）", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 更新表格数据
                evaluationTableModel.setValueAt(newPeriodName, selectedRow, 1);
                evaluationTableModel.setValueAt(newSemester, selectedRow, 2);
                evaluationTableModel.setValueAt(newStartDate, selectedRow, 3);
                evaluationTableModel.setValueAt(newEndDate, selectedRow, 4);
                evaluationTableModel.setValueAt(newStatus, selectedRow, 5);

                JOptionPane.showMessageDialog(dialog, "评教周期更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "日期解析错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "更新评教周期时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示评教指标管理对话框
     */
    private void showEvaluationCriteriaDialog() {
        JDialog dialog = new JDialog(this, "评教指标管理", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());

        // 指标列表
        String[] columns = {"指标编号", "指标名称", "描述", "权重(%)", "最高分"};
        DefaultTableModel criteriaTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable criteriaTable = new JTable(criteriaTableModel);
        criteriaTable.setRowHeight(25);

        // 加载现有指标
        try {
            List<EvaluationCriteria> criteriaList = evaluationService.getAllEvaluationCriteria();
            for (EvaluationCriteria criteria : criteriaList) {
                Object[] row = {
                    criteria.getCriteriaId(),
                    criteria.getCriteriaName(),
                    criteria.getDescription(),
                    criteria.getWeight(),
                    criteria.getMaxScore()
                };
                criteriaTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载评教指标失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(criteriaTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("添加指标");
        JButton editButton = new JButton("编辑指标");
        JButton deleteButton = new JButton("删除指标");
        JButton closeButton = new JButton("关闭");

        addButton.setBackground(new Color(60, 179, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);

        editButton.setBackground(new Color(255, 165, 0));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);

        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);

        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 添加指标按钮事件
        addButton.addActionListener(e -> {
            showAddCriteriaDialog(dialog, criteriaTableModel);
        });

        // 编辑指标按钮事件
        editButton.addActionListener(e -> {
            int selectedRow = criteriaTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "请选择要编辑的指标", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String criteriaId = (String) criteriaTableModel.getValueAt(selectedRow, 0);
            showEditCriteriaDialog(dialog, criteriaTableModel, criteriaId);
        });

        // 删除指标按钮事件
        deleteButton.addActionListener(e -> {
            int selectedRow = criteriaTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "请选择要删除的指标", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String criteriaId = (String) criteriaTableModel.getValueAt(selectedRow, 0);
            String criteriaName = (String) criteriaTableModel.getValueAt(selectedRow, 1);

            int option = JOptionPane.showConfirmDialog(dialog,
                String.format("确定要删除指标：%s (%s) 吗？", criteriaName, criteriaId),
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                // 这里需要在EvaluationService中添加删除指标的方法
                criteriaTableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(dialog, "指标删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 显示添加评教指标对话框
     */
    private void showAddCriteriaDialog(JDialog parentDialog, DefaultTableModel tableModel) {
        JDialog dialog = new JDialog(parentDialog, "添加评教指标", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentDialog);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField criteriaIdField = new JTextField(15);
        JTextField criteriaNameField = new JTextField(15);
        JTextArea descriptionArea = new JTextArea(3, 15);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JTextField weightField = new JTextField(15);
        JTextField maxScoreField = new JTextField("100", 15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("指标编号:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(criteriaIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("指标名称:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(criteriaNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHEAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("描述:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("权重(%):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(weightField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("最高分:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(maxScoreField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            try {
                String criteriaId = criteriaIdField.getText().trim();
                String criteriaName = criteriaNameField.getText().trim();
                String description = descriptionArea.getText().trim();
                String weightText = weightField.getText().trim();
                String maxScoreText = maxScoreField.getText().trim();

                if (criteriaId.isEmpty() || criteriaName.isEmpty() || weightText.isEmpty() || maxScoreText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double weight = Double.parseDouble(weightText);
                int maxScore = Integer.parseInt(maxScoreText);

                if (weight <= 0 || weight > 100) {
                    JOptionPane.showMessageDialog(dialog, "权重必须在0-100之间", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (maxScore <= 0) {
                    JOptionPane.showMessageDialog(dialog, "最高分必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                EvaluationCriteria criteria = new EvaluationCriteria(criteriaId, criteriaName, description, weight, maxScore);

                if (evaluationService.createEvaluationCriteria(criteria)) {
                    Object[] row = {criteriaId, criteriaName, description, weight, maxScore};
                    tableModel.addRow(row);
                    JOptionPane.showMessageDialog(dialog, "评教指标添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "评教指标添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "权重和最高分必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加评教指标时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 显示编辑评教指标对话框
     */
    private void showEditCriteriaDialog(JDialog parentDialog, DefaultTableModel tableModel, String criteriaId) {
        JOptionPane.showMessageDialog(parentDialog, "编辑评教指标功能待完善", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示评教统计
     */
    private void showEvaluationStatistics() {
        JDialog dialog = new JDialog(this, "评教统计分析", true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());

        // 选择面板
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> periodComboBox = new JComboBox<>();
        JComboBox<String> statsTypeComboBox = new JComboBox<>(new String[]{"学生参与统计", "教师评教统计", "课程评教统计", "班级评教统计"});

        // 加载评教周期
        try {
            List<EvaluationPeriod> periods = evaluationService.getAllEvaluationPeriods();
            for (EvaluationPeriod period : periods) {
                periodComboBox.addItem(period.getPeriodName() + " (" + period.getSemester() + ")");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载评教周期失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        selectPanel.add(new JLabel("评教周期:"));
        selectPanel.add(periodComboBox);
        selectPanel.add(new JLabel("统计类型:"));
        selectPanel.add(statsTypeComboBox);

        JButton queryButton = new JButton("查询统计");
        queryButton.setBackground(new Color(70, 130, 180));
        queryButton.setForeground(Color.WHITE);
        queryButton.setFocusPainted(false);
        selectPanel.add(queryButton);

        panel.add(selectPanel, BorderLayout.NORTH);

        // 统计结果显示区域
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        resultArea.setBackground(new Color(248, 248, 248));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setPreferredSize(new Dimension(650, 450));
        panel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton exportButton = new JButton("导出报告");
        JButton closeButton = new JButton("关闭");

        exportButton.setBackground(new Color(60, 179, 113));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);

        closeButton.setBackground(new Color(220, 20, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);

        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 查询按钮事件
        queryButton.addActionListener(e -> {
            try {
                String selectedPeriod = (String) periodComboBox.getSelectedItem();
                String statsType = (String) statsTypeComboBox.getSelectedItem();

                if (selectedPeriod == null) {
                    JOptionPane.showMessageDialog(dialog, "请选择评教周期", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 获取周期ID（简化处理）
                String periodId = "P001"; // 这里应该根据选择的周期获取实际ID

                StringBuilder result = new StringBuilder();
                result.append("=== ").append(statsType).append(" ===\n");
                result.append("评教周期: ").append(selectedPeriod).append("\n");
                result.append("统计时间: ").append(java.time.LocalDateTime.now().toString()).append("\n\n");

                switch (statsType) {
                    case "学生参与统计":
                        Map<String, Object> studentStats = statisticsService.getStudentEvaluationStatistics(periodId);
                        result.append("学生总数: ").append(studentStats.getOrDefault("totalStudents", 0)).append("\n");
                        result.append("参与评教学生数: ").append(studentStats.getOrDefault("participatedStudents", 0)).append("\n");
                        result.append("参与率: ").append(String.format("%.1f%%", (Double) studentStats.getOrDefault("participationRate", 0.0))).append("\n\n");

                        @SuppressWarnings("unchecked")
                        Map<String, Integer> distribution = (Map<String, Integer>) studentStats.get("gradeDistribution");
                        if (distribution != null) {
                            result.append("评价分布:\n");
                            for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                                result.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("人\n");
                            }
                        }
                        break;

                    case "教师评教统计":
                        Map<String, Object> teacherStats = statisticsService.getTeacherEvaluationStatistics(periodId);
                        result.append("参与教师总数: ").append(teacherStats.getOrDefault("totalTeachers", 0)).append("\n");
                        result.append("总体平均分: ").append(String.format("%.1f", (Double) teacherStats.getOrDefault("overallAvgScore", 0.0))).append("\n\n");
                        break;

                    case "课程评教统计":
                        Map<String, Object> courseStats = statisticsService.getCourseEvaluationStatistics(periodId);
                        result.append("参与课程总数: ").append(courseStats.getOrDefault("totalCourses", 0)).append("\n");
                        result.append("总体平均分: ").append(String.format("%.1f", (Double) courseStats.getOrDefault("overallAvgScore", 0.0))).append("\n\n");
                        break;

                    case "班级评教统计":
                        Map<String, Object> classStats = statisticsService.getClassEvaluationStatistics(periodId);
                        result.append("总体平均分: ").append(String.format("%.1f", (Double) classStats.getOrDefault("overallAvgScore", 0.0))).append("\n\n");
                        break;
                }

                result.append("=== 统计完成 ===");
                resultArea.setText(result.toString());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "生成统计时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 导出按钮事件
        exportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "导出功能待实现", "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示系统统计
     */
    private void showSystemStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getSystemOverallStatistics();
            
            StringBuilder message = new StringBuilder();
            message.append("系统总体统计\n\n");
            
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                message.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            
            JOptionPane.showMessageDialog(this, message.toString(), "系统统计", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "获取系统统计失败: " + e.getMessage(), 
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
