package edu.ai.haut.ui.admin;

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
    private ManagementUIHelper managementUIHelper;
    
    private JTabbedPane tabbedPane;
    private JTable userTable;
    private JTable courseTable;
    private JTable classTable;
    private JTable evaluationTable;
    private DefaultTableModel userTableModel;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel classTableModel;
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
        String[] userColumns = {"用户ID", "姓名", "性别", "类型", "详细信息"};
        userTable = TableUtil.createReadOnlyTable(userColumns);
        userTableModel = (DefaultTableModel) userTable.getModel();
        
        // 课程管理表格
        String[] courseColumns = {"课程编号", "课程名称", "学分", "课程类型", "开课学院"};
        courseTable = TableUtil.createReadOnlyTable(courseColumns);
        courseTableModel = (DefaultTableModel) courseTable.getModel();

        // 班级管理表格
        String[] classColumns = {"班级编号", "班级名称", "年级", "专业", "学院", "学生数量"};
        classTable = TableUtil.createReadOnlyTable(classColumns);
        classTableModel = (DefaultTableModel) classTable.getModel();

        // 评教管理表格
        String[] evaluationColumns = {"周期编号", "周期名称", "学期", "开始日期", "结束日期", "状态", "评教数量"};
        evaluationTable = TableUtil.createReadOnlyTable(evaluationColumns);
        evaluationTableModel = (DefaultTableModel) evaluationTable.getModel();
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
        JButton systemStatsButton = LayoutUtil.createButton("系统统计", LayoutUtil.SUCCESS_COLOR);
        JButton logoutButton = LayoutUtil.createButton("退出登录", LayoutUtil.DANGER_COLOR);
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(systemStatsButton);
        buttonPanel.add(logoutButton);
        
        topPanel.add(infoPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // 用户管理面板
        JPanel userPanel = createUserManagementPanel();

        // 课程管理面板
        JPanel coursePanel = createCourseManagementPanel();

        // 班级管理面板
        JPanel classPanel = createClassManagementPanel();

        // 评教管理面板
        JPanel evaluationPanel = createEvaluationManagementPanel();

        // 添加到选项卡
        tabbedPane.addTab("用户管理", userPanel);
        tabbedPane.addTab("课程管理", coursePanel);
        tabbedPane.addTab("班级管理", classPanel);
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
        
        JButton queryUsersButton = LayoutUtil.createButton("查询", LayoutUtil.PRIMARY_COLOR);
        JButton addUserButton = LayoutUtil.createButton("添加用户", LayoutUtil.SUCCESS_COLOR);
        JButton editUserButton = LayoutUtil.createButton("编辑用户", LayoutUtil.WARNING_COLOR);
        JButton deleteUserButton = LayoutUtil.createButton("删除用户", LayoutUtil.DANGER_COLOR);
        
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
     * 创建班级管理面板
     */
    private JPanel createClassManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("班级管理"));

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addClassButton = LayoutUtil.createButton("添加班级", LayoutUtil.SUCCESS_COLOR);
        JButton deleteClassButton = LayoutUtil.createButton("删除班级", LayoutUtil.DANGER_COLOR);
        JButton classStudentsButton = LayoutUtil.createButton("班级学生", LayoutUtil.PRIMARY_COLOR);

        toolPanel.add(addClassButton);
        toolPanel.add(deleteClassButton);
        toolPanel.add(classStudentsButton);

        panel.add(toolPanel, BorderLayout.NORTH);

        JScrollPane classScrollPane = new JScrollPane(classTable);
        classScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(classScrollPane, BorderLayout.CENTER);

        // 设置按钮事件
        addClassButton.addActionListener(e -> showAddClassDialog());
        deleteClassButton.addActionListener(e -> deleteSelectedClass());
        classStudentsButton.addActionListener(e -> showClassStudentsDialog());

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
        
        JScrollPane evaluationScrollPane = new JScrollPane(evaluationTable);
        evaluationScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(evaluationScrollPane, BorderLayout.CENTER);
        
        // 设置按钮事件
        addPeriodButton.addActionListener(e -> managementUIHelper.showAddEvaluationPeriodDialog(this, this::loadEvaluationData));
        editPeriodButton.addActionListener(e -> {
            int selectedRow = evaluationTable.getSelectedRow();
            if (selectedRow < 0) {
                managementUIHelper.showWarningMessage(this, "请选择要编辑的评教周期");
                return;
            }
            String periodId = (String) evaluationTableModel.getValueAt(selectedRow, 0);
            managementUIHelper.showEditEvaluationPeriodDialog(this, periodId, this::loadEvaluationData);
        });
        manageCriteriaButton.addActionListener(e -> managementUIHelper.showEvaluationCriteriaDialog(this));
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
        loadUserData();
        loadCourseData();
        loadClassData();
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
                String.format("年级:%s 专业:%s 班级:%s", student.getGrade(), student.getMajor(), student.getClassId())
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
                String.format("职称:%s 学院:%s", teacher.getTitle(), teacher.getCollege())
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
                    String.format("部门:%s 职位:%s", staff.getDepartment(), staff.getPosition())
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
                    "系统管理员"
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
     * 加载班级数据
     */
    private void loadClassData() {
        classTableModel.setRowCount(0);

        try {
            List<ClassRoom> classes = classService.getAllClasses();
            for (ClassRoom classRoom : classes) {
                // 获取班级学生数量
                int studentCount = studentService.getStudentCountByClass(classRoom.getClassId());

                Object[] row = {
                    classRoom.getClassId(),
                    classRoom.getClassName(),
                    classRoom.getGrade(),
                    classRoom.getMajor(),
                    classRoom.getCollege(),
                    studentCount
                };
                classTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载班级数据失败: " + e.getMessage(),
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



        // 根据用户类型添加特定字段
        int currentRow = 4;
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

                        Student student = new Student(id, name, gender, grade, major, classRoom.getClassId(), password);
                        success = studentService.registerStudent(student);
                        break;

                    case "教师":
                        String title = (String) titleComboBox.getSelectedItem();
                        String college = extraField2.getText().trim();
                        Teacher teacher = new Teacher(id, name, gender, title, college, password);
                        success = teacherService.registerTeacher(teacher);
                        break;

                    case "教务人员":
                        String department = (String) departmentComboBox.getSelectedItem();
                        String position = extraField2.getText().trim();
                        AcademicAffairsStaff staff = new AcademicAffairsStaff(id, name, gender, department, position, password);
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

        // 学生特有字段
        final JTextField gradeField;
        final JTextField majorField;
        final JTextField classField;

        // 教师特有字段
        final JComboBox<String> titleComboBox;
        final JTextField collegeField;

        // 教务人员特有字段
        final JComboBox<String> departmentComboBox;

        int currentRow = 3;

        // 初始化变量
        JTextField tempGradeField = null;
        JTextField tempMajorField = null;
        JTextField tempClassField = null;
        JComboBox<String> tempTitleComboBox = null;
        JTextField tempCollegeField = null;
        JComboBox<String> tempDepartmentComboBox = null;

        // 根据用户类型加载现有数据和添加特有字段
        try {
            switch (userType) {
                case "学生":
                    Student student = studentService.getStudentById(userId);
                    if (student != null) {
                        genderComboBox.setSelectedItem(student.getGender());

                        // 添加学生特有字段
                        tempGradeField = new JTextField(student.getGrade(), 15);
                        tempMajorField = new JTextField(student.getMajor(), 15);

                        // 获取学生当前班级信息并显示班级名称
                        String currentClassName = "";
                        try {
                            ClassRoom currentClass = classService.getClassById(student.getClassId());
                            if (currentClass != null) {
                                currentClassName = currentClass.getClassName();
                            }
                        } catch (Exception ex) {
                            System.err.println("获取班级信息失败: " + ex.getMessage());
                        }
                        tempClassField = new JTextField(currentClassName, 15);

                        // 添加到界面
                        gbc.gridx = 0; gbc.gridy = currentRow; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                        panel.add(new JLabel("年级:"), gbc);
                        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                        panel.add(tempGradeField, gbc);
                        currentRow++;

                        gbc.gridx = 0; gbc.gridy = currentRow; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                        panel.add(new JLabel("专业:"), gbc);
                        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                        panel.add(tempMajorField, gbc);
                        currentRow++;

                        gbc.gridx = 0; gbc.gridy = currentRow; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                        panel.add(new JLabel("班级:"), gbc);
                        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                        panel.add(tempClassField, gbc);
                        currentRow++;
                    }
                    break;
                case "教师":
                    Teacher teacher = teacherService.getTeacherById(userId);
                    if (teacher != null) {
                        genderComboBox.setSelectedItem(teacher.getGender());

                        // 添加教师特有字段
                        tempTitleComboBox = new JComboBox<>(new String[]{"教授", "副教授", "讲师", "助教"});
                        tempTitleComboBox.setSelectedItem(teacher.getTitle());
                        tempCollegeField = new JTextField(teacher.getCollege(), 15);

                        // 添加到界面
                        gbc.gridx = 0; gbc.gridy = currentRow; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                        panel.add(new JLabel("职称:"), gbc);
                        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                        panel.add(tempTitleComboBox, gbc);
                        currentRow++;

                        gbc.gridx = 0; gbc.gridy = currentRow; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                        panel.add(new JLabel("学院:"), gbc);
                        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                        panel.add(tempCollegeField, gbc);
                        currentRow++;
                    }
                    break;
                case "教务人员":
                    AcademicAffairsStaff staff = new AcademicAffairsStaffService().getStaffById(userId);
                    if (staff != null) {
                        genderComboBox.setSelectedItem(staff.getGender());

                        // 添加教务人员特有字段
                        tempDepartmentComboBox = new JComboBox<>(new String[]{"教务处"});
                        tempDepartmentComboBox.setSelectedItem(staff.getDepartment());

                        // 添加到界面
                        gbc.gridx = 0; gbc.gridy = currentRow; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
                        panel.add(new JLabel("部门:"), gbc);
                        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
                        panel.add(tempDepartmentComboBox, gbc);
                        currentRow++;
                    }
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载用户数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        // 将临时变量赋值给final变量
        gradeField = tempGradeField;
        majorField = tempMajorField;
        classField = tempClassField;
        titleComboBox = tempTitleComboBox;
        collegeField = tempCollegeField;
        departmentComboBox = tempDepartmentComboBox;

        // 按钮面板
        JButton saveButton = LayoutUtil.createButton("保存", LayoutUtil.SUCCESS_COLOR);
        JButton cancelButton = LayoutUtil.createButton("取消", LayoutUtil.DANGER_COLOR);
        JPanel buttonPanel = LayoutUtil.createButtonPanel(saveButton, cancelButton);

        gbc.gridx = 0; gbc.gridy = currentRow;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String gender = (String) genderComboBox.getSelectedItem();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "姓名不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = false;
                switch (userType) {
                    case "学生":
                        Student student = studentService.getStudentById(userId);
                        if (student != null) {
                            // 获取原班级ID用于更新班级人数
                            String oldClassId = student.getClassId();

                            student.setName(name);
                            student.setGender(gender);

                            String newGrade = "";
                            String newMajor = "";
                            String newClassName = "";

                            if (gradeField != null) {
                                newGrade = gradeField.getText().trim();
                                student.setGrade(newGrade);
                            }
                            if (majorField != null) {
                                newMajor = majorField.getText().trim();
                                student.setMajor(newMajor);
                            }
                            if (classField != null) {
                                newClassName = classField.getText().trim();
                            }

                            // 处理班级变更逻辑
                            String newClassId = handleClassChange(student, oldClassId, newClassName, newGrade, newMajor);
                            if (newClassId != null) {
                                student.setClassId(newClassId);

                                // 如果班级发生变化，需要更新班级人数
                                if (!oldClassId.equals(newClassId)) {
                                    // 原班级人数-1，新班级人数+1
                                    studentService.updateClassStudentCount(oldClassId, -1);
                                    studentService.updateClassStudentCount(newClassId, 1);
                                }
                            }

                            success = studentService.updateStudent(student);
                        }
                        break;
                    case "教师":
                        Teacher teacher = teacherService.getTeacherById(userId);
                        if (teacher != null) {
                            teacher.setName(name);
                            teacher.setGender(gender);

                            if (titleComboBox != null) {
                                teacher.setTitle((String) titleComboBox.getSelectedItem());
                            }
                            if (collegeField != null) {
                                teacher.setCollege(collegeField.getText().trim());
                            }

                            success = teacherService.updateTeacher(teacher);
                        }
                        break;
                    case "教务人员":
                        AcademicAffairsStaff staff = new AcademicAffairsStaffService().getStaffById(userId);
                        if (staff != null) {
                            staff.setName(name);
                            staff.setGender(gender);

                            if (departmentComboBox != null) {
                                staff.setDepartment((String) departmentComboBox.getSelectedItem());
                            }

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
     * 处理班级变更逻辑
     * 支持创建新班级或查找现有班级
     */
    private String handleClassChange(Student student, String oldClassId, String newClassName, String newGrade, String newMajor) {
        if (newClassName == null || newClassName.trim().isEmpty()) {
            return oldClassId; // 如果班级名为空，保持原班级
        }

        try {
            // 首先尝试根据班级名称查找现有班级
            ClassRoom existingClass = classService.getClassByName(newClassName);
            if (existingClass != null) {
                System.out.println("找到现有班级: " + existingClass.getClassId() + " - " + existingClass.getClassName());
                return existingClass.getClassId();
            }

            // 如果班级不存在，创建新班级
            System.out.println("班级不存在，准备创建新班级: " + newClassName);

            // 生成新的班级ID
            String newClassId = generateClassId(newGrade);

            ClassRoom newClass = new ClassRoom();
            newClass.setClassId(newClassId);
            newClass.setClassName(newClassName);
            newClass.setGrade(newGrade.isEmpty() ? student.getGrade() : newGrade);
            newClass.setMajor(newMajor.isEmpty() ? student.getMajor() : newMajor);
            newClass.setCollege("信息科学与工程学院"); // 默认学院
            newClass.setStudentCount(0); // 初始学生数为0

            if (classService.createClass(newClass)) {
                System.out.println("成功创建新班级: " + newClassId + " - " + newClassName);
                return newClassId;
            } else {
                System.err.println("创建班级失败: " + newClassName);
                JOptionPane.showMessageDialog(this,
                    "创建新班级失败: " + newClassName + "\n将保持学生原班级不变",
                    "警告", JOptionPane.WARNING_MESSAGE);
                return oldClassId;
            }

        } catch (Exception e) {
            System.err.println("处理班级变更时出错: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "处理班级变更时出错: " + e.getMessage() + "\n将保持学生原班级不变",
                "错误", JOptionPane.ERROR_MESSAGE);
            return oldClassId;
        }
    }

    /**
     * 生成班级ID
     */
    private String generateClassId(String grade) {
        // 根据年级生成班级ID，格式如：CS2301, CS2302等
        String prefix = "CS" + (grade.length() >= 2 ? grade : "23"); // 默认23年级

        try {
            // 查找该年级下已有的班级数量
            List<ClassRoom> existingClasses = classService.getClassesByGrade(grade.isEmpty() ? "23" : grade);
            int classNumber = existingClasses.size() + 1;

            // 生成新的班级ID
            return prefix + String.format("%02d", classNumber);

        } catch (Exception e) {
            // 如果查询失败，使用时间戳作为后缀
            return prefix + System.currentTimeMillis() % 100;
        }
    }

    /**
     * 生成唯一的班级编号（使用英文缩写格式，与系统初始化数据保持一致）
     */
    private String generateUniqueClassId(String grade, String major) {
        try {
            // 根据专业生成英文前缀
            String majorPrefix = getMajorPrefix(major);

            // 查找该年级和专业下已有的班级数量
            List<ClassRoom> existingClasses = classService.getClassesByGrade(grade);

            // 过滤出相同专业的班级
            long sameGradeMajorCount = existingClasses.stream()
                .filter(c -> c.getMajor().equals(major))
                .count();

            int classNumber = (int) sameGradeMajorCount + 1;

            // 生成候选ID，格式：专业缩写+年级+班级号，如：SE2401
            String candidateId = majorPrefix + grade + String.format("%02d", classNumber);

            // 确保ID唯一
            while (classService.getClassById(candidateId) != null) {
                classNumber++;
                candidateId = majorPrefix + grade + String.format("%02d", classNumber);
            }

            System.out.println("生成的班级编号: " + candidateId + " (专业: " + major + ", 年级: " + grade + ", 序号: " + classNumber + ")");
            return candidateId;

        } catch (Exception e) {
            System.err.println("生成班级编号时出错: " + e.getMessage());
            // 备用方案：使用专业缩写+时间戳
            String majorPrefix = getMajorPrefix(major);
            return majorPrefix + grade + String.format("%02d", (int)(System.currentTimeMillis() % 100));
        }
    }

    /**
     * 根据专业获取英文前缀
     */
    private String getMajorPrefix(String major) {
        if (major.contains("软件工程")) {
            return "SE";
        } else if (major.contains("计算机科学") || major.contains("计算机")) {
            return "CS";
        } else if (major.contains("人工智能")) {
            return "AI";
        } else if (major.contains("数据科学")) {
            return "DS";
        } else if (major.contains("网络工程")) {
            return "NE";
        } else if (major.contains("信息安全")) {
            return "IS";
        } else {
            return "CS"; // 默认前缀
        }
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
        JButton saveButton = LayoutUtil.createButton("保存", LayoutUtil.SUCCESS_COLOR);
        JButton cancelButton = LayoutUtil.createButton("取消", LayoutUtil.DANGER_COLOR);
        JPanel buttonPanel = LayoutUtil.createButtonPanel(saveButton, cancelButton);

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
            JButton saveButton = LayoutUtil.createButton("保存", LayoutUtil.SUCCESS_COLOR);
            JButton cancelButton = LayoutUtil.createButton("取消", LayoutUtil.DANGER_COLOR);
            JPanel buttonPanel = LayoutUtil.createButtonPanel(saveButton, cancelButton);

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

                    if (courseService.updateCourse(course)) {
                        JOptionPane.showMessageDialog(dialog, "课程更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadCourseData(); // 刷新课程数据
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "课程更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }

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
            if (courseService.deleteCourse(courseId)) {
                JOptionPane.showMessageDialog(this, "课程删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadCourseData(); // 刷新课程列表
            } else {
                JOptionPane.showMessageDialog(this, "课程删除失败，可能存在相关的开课信息", "错误", JOptionPane.ERROR_MESSAGE);
            }

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

        // 加载开课信息
        loadOfferingData(offeringTableModel);

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
    private void loadOfferingData(DefaultTableModel tableModel) {
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

            List<ClassRoom> classes = courseService.getAllClassRooms();
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
     * 显示添加班级对话框
     */
    private void showAddClassDialog() {
        JDialog dialog = new JDialog(this, "添加班级", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField classIdField = new JTextField("系统自动生成", 15);
        classIdField.setEditable(false);
        classIdField.setBackground(Color.LIGHT_GRAY);

        JTextField classNameField = new JTextField(15);
        JTextField gradeField = new JTextField(15);
        JTextField majorField = new JTextField(15);
        JTextField collegeField = new JTextField("信息科学与工程学院", 15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("班级编号:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(classIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("班级名称:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(classNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("年级:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(gradeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("专业:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(majorField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("学院:"), gbc);
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
                String className = classNameField.getText().trim();
                String grade = gradeField.getText().trim();
                String major = majorField.getText().trim();
                String college = collegeField.getText().trim();

                if (className.isEmpty() || grade.isEmpty() || major.isEmpty() || college.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 检查班级名称是否已存在
                ClassRoom existingClass = classService.getClassByName(className);
                if (existingClass != null) {
                    JOptionPane.showMessageDialog(dialog,
                        "班级名称已存在！\n\n已存在的班级：\n" +
                        "班级编号：" + existingClass.getClassId() + "\n" +
                        "班级名称：" + existingClass.getClassName() + "\n" +
                        "年级：" + existingClass.getGrade() + "\n" +
                        "专业：" + existingClass.getMajor(),
                        "班级名称冲突", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 自动生成班级编号
                String classId = generateUniqueClassId(grade, major);
                System.out.println("生成的班级编号: " + classId);

                ClassRoom classRoom = new ClassRoom();
                classRoom.setClassId(classId);
                classRoom.setClassName(className);
                classRoom.setGrade(grade);
                classRoom.setMajor(major);
                classRoom.setCollege(college);
                classRoom.setStudentCount(0); // 初始学生数为0

                System.out.println("准备创建班级: " + classRoom.getClassId() + " - " + classRoom.getClassName());

                if (classService.createClass(classRoom)) {
                    JOptionPane.showMessageDialog(dialog,
                        "班级添加成功！\n\n班级信息：\n" +
                        "班级编号：" + classId + "\n" +
                        "班级名称：" + className + "\n" +
                        "年级：" + grade + "\n" +
                        "专业：" + major,
                        "添加成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadClassData(); // 刷新班级列表
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "班级添加失败！\n\n可能的原因：\n" +
                        "1. 数据验证失败\n" +
                        "2. 数据库连接问题\n" +
                        "3. 班级编号冲突\n\n" +
                        "生成的班级编号：" + classId,
                        "添加失败", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                System.err.println("添加班级时发生错误: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                    "添加班级时发生错误！\n\n错误信息：" + ex.getMessage() + "\n\n请检查：\n" +
                    "1. 数据库连接是否正常\n" +
                    "2. 输入的数据格式是否正确\n" +
                    "3. 系统日志中的详细错误信息",
                    "系统错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }



    /**
     * 删除选中的班级
     */
    private void deleteSelectedClass() {
        int selectedRow = classTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的班级", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String classId = (String) classTableModel.getValueAt(selectedRow, 0);
        String className = (String) classTableModel.getValueAt(selectedRow, 1);

        int option = JOptionPane.showConfirmDialog(this,
            String.format("确定要删除班级：%s (%s) 吗？\n注意：删除班级前请确保该班级下没有学生。", className, classId),
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                if (classService.deleteClass(classId)) {
                    JOptionPane.showMessageDialog(this, "班级删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadClassData(); // 刷新班级列表
                } else {
                    JOptionPane.showMessageDialog(this, "班级删除失败，可能该班级下还有学生", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "删除班级时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 显示班级学生对话框
     */
    private void showClassStudentsDialog() {
        int selectedRow = classTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要查看的班级", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String classId = (String) classTableModel.getValueAt(selectedRow, 0);
        String className = (String) classTableModel.getValueAt(selectedRow, 1);

        JDialog dialog = new JDialog(this, "班级学生 - " + className, true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());

        // 学生列表
        String[] columns = {"学号", "姓名", "性别", "年级", "专业"};
        DefaultTableModel studentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable studentTable = new JTable(studentTableModel);
        studentTable.setRowHeight(25);
        managementUIHelper.setupTableStyle(studentTable);

        // 加载班级学生数据
        try {
            List<Student> students = studentService.getStudentsByClass(classId);
            for (Student student : students) {
                Object[] row = {
                    student.getStudentId(),
                    student.getName(),
                    student.getGender(),
                    student.getGrade(),
                    student.getMajor()
                };
                studentTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "加载学生数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(studentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 底部信息
        JLabel infoLabel = new JLabel("班级：" + className + "，学生总数：" + studentTableModel.getRowCount() + " 人");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(infoLabel, BorderLayout.SOUTH);

        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("关闭");
        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

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
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            panel.add(offeringIdField, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel("课程:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            panel.add(courseComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel("授课教师:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            panel.add(teacherComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel("班级:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            panel.add(classComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel("学期:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            panel.add(semesterField, gbc);

            gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel("上课时间:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
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

            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
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

                // 验证开始日期不能晚于结束日期
                java.time.LocalDate startDateParsed = java.time.LocalDate.parse(newStartDate);
                java.time.LocalDate endDateParsed = java.time.LocalDate.parse(newEndDate);
                if (startDateParsed.isAfter(endDateParsed)) {
                    JOptionPane.showMessageDialog(dialog, "开始日期不能晚于结束日期", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 创建更新的评教周期对象
                EvaluationPeriod updatedPeriod = new EvaluationPeriod(periodId, newPeriodName, newSemester, startDateParsed, endDateParsed, newStatus);

                // 调用服务层更新数据库
                if (evaluationService.updateEvaluationPeriod(updatedPeriod)) {
                    // 更新表格数据
                    evaluationTableModel.setValueAt(newPeriodName, selectedRow, 1);
                    evaluationTableModel.setValueAt(newSemester, selectedRow, 2);
                    evaluationTableModel.setValueAt(newStartDate, selectedRow, 3);
                    evaluationTableModel.setValueAt(newEndDate, selectedRow, 4);
                    evaluationTableModel.setValueAt(newStatus, selectedRow, 5);

                    JOptionPane.showMessageDialog(dialog, "评教周期更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "评教周期更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }

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
                if (evaluationService.deleteEvaluationCriteria(criteriaId)) {
                    criteriaTableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(dialog, "指标删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "指标删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
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

        JButton generateButton = new JButton("生成统计");

        generateButton.setBackground(new Color(70, 130, 180));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);

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
            generateAdminStatisticsReport(periodId, statisticsType, statisticsTableModel, analysisArea);
        });

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 生成管理员统计报告
     */
    private void generateAdminStatisticsReport(String periodId, String statisticsType, DefaultTableModel tableModel, JTextArea analysisArea) {
        tableModel.setRowCount(0);
        analysisArea.setText("");

        try {
            switch (statisticsType) {
                case "总体统计":
                    generateAdminOverallStatistics(periodId, tableModel, analysisArea);
                    break;
                case "教师排名":
                    generateAdminTeacherRanking(periodId, tableModel, analysisArea);
                    break;
                case "课程排名":
                    generateAdminCourseRanking(periodId, tableModel, analysisArea);
                    break;

                case "学院统计":
                    generateAdminCollegeStatistics(periodId, tableModel, analysisArea);
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "生成统计报告失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 生成管理员总体统计
     */
    private void generateAdminOverallStatistics(String periodId, DefaultTableModel tableModel, JTextArea analysisArea) {
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
            tableModel.addRow(new Object[]{"总体平均分", String.format("%.2f", overallAvgScore), "-", getGradeByScore(overallAvgScore), "所有评教的平均分数"});

            // 生成分析报告
            StringBuilder analysis = new StringBuilder();
            analysis.append("=== 评教总体统计分析报告 ===\n\n");
            analysis.append("一、基本情况\n");
            analysis.append(String.format("本次评教周期共有 %d 名教师、%d 门课程参与评教，",
                teacherStats.getOrDefault("totalTeachers", 0), courseStats.getOrDefault("totalCourses", 0)));
            analysis.append(String.format("学生参与率为 %.1f%%。\n\n", (Double) studentStats.getOrDefault("participationRate", 0.0)));

            analysis.append("二、评教结果分析\n");
            analysis.append(String.format("总体平均分为 %.2f 分，等级为 %s。\n\n", overallAvgScore, getGradeByScore(overallAvgScore)));

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
     * 生成管理员教师排名统计
     */
    private void generateAdminTeacherRanking(String periodId, DefaultTableModel tableModel, JTextArea analysisArea) {
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
     * 生成管理员课程排名统计
     */
    private void generateAdminCourseRanking(String periodId, DefaultTableModel tableModel, JTextArea analysisArea) {
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
     * 生成管理员学院统计
     */
    private void generateAdminCollegeStatistics(String periodId, DefaultTableModel tableModel, JTextArea analysisArea) {
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
     * 根据分数获取等级
     */
    private String getGradeByScore(double score) {
        if (score >= 90) return "优秀";
        else if (score >= 80) return "良好";
        else if (score >= 70) return "中等";
        else if (score >= 60) return "及格";
        else return "不及格";
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
