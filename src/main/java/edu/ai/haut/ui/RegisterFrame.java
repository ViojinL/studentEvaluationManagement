package edu.ai.haut.ui;

import edu.ai.haut.model.*;
import edu.ai.haut.service.*;
import edu.ai.haut.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 注册界面
 * 支持学生、教师、教务人员注册
 */
public class RegisterFrame extends JDialog {
    
    private JComboBox<String> userTypeComboBox;
    private JTextField userIdField;
    private JTextField nameField;
    private JComboBox<String> genderComboBox;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    
    // 学生专用字段
    private JTextField gradeField;
    private JTextField majorField;
    private JTextField classNameField;
    
    // 教师专用字段
    private JComboBox<String> titleComboBox;
    private JTextField collegeField;
    
    // 教务人员专用字段
    private JComboBox<String> departmentComboBox;
    private JTextField positionField;
    
    private JPanel dynamicPanel;
    private JButton registerButton;
    private JButton cancelButton;
    
    private StudentService studentService;
    private TeacherService teacherService;
    private AcademicAffairsStaffService staffService;
    private ClassService classService;
    private LoginFrame parentFrame;
    
    public RegisterFrame(LoginFrame parent) {
        super(parent, "用户注册", true);
        this.parentFrame = parent;
        this.studentService = new StudentService();
        this.teacherService = new TeacherService();
        this.staffService = new AcademicAffairsStaffService();
        this.classService = new ClassService();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setupFrame();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        userTypeComboBox = new JComboBox<>(new String[]{"学生", "教师", "教务人员"});
        userIdField = new JTextField(20);
        nameField = new JTextField(20);
        genderComboBox = new JComboBox<>(new String[]{"男", "女"});
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        // 学生字段
        gradeField = new JTextField(20);
        majorField = new JTextField(20);
        classNameField = new JTextField(20);
        
        // 教师字段
        titleComboBox = new JComboBox<>(new String[]{"教授", "副教授", "讲师", "助教", "实验师"});
        titleComboBox.setPreferredSize(new Dimension(200, 25));
        collegeField = new JTextField(20);
        
        // 教务人员字段
        departmentComboBox = new JComboBox<>(new String[]{"教务处"});
        departmentComboBox.setPreferredSize(new Dimension(200, 25));
        positionField = new JTextField(20);
        
        registerButton = new JButton("注册");
        cancelButton = new JButton("取消");
        
        // 设置按钮样式
        registerButton.setBackground(new Color(60, 179, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        
        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        
        dynamicPanel = new JPanel(new GridBagLayout());
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // 用户类型
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("用户类型:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(userTypeComboBox, gbc);
        
        // 用户ID
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("用户ID:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(userIdField, gbc);
        
        // 姓名
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("姓名:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(nameField, gbc);
        
        // 性别
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("性别:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(genderComboBox, gbc);
        
        // 密码
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("密码:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(passwordField, gbc);
        
        // 确认密码
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("确认密码:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(confirmPasswordField, gbc);
        
        // 动态字段面板
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(dynamicPanel, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 初始显示学生字段
        updateDynamicFields();
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 用户类型改变事件
        userTypeComboBox.addActionListener(e -> updateDynamicFields());
        
        // 注册按钮事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegister();
            }
        });
        
        // 取消按钮事件
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * 设置窗口属性
     */
    private void setupFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    /**
     * 更新动态字段
     */
    private void updateDynamicFields() {
        dynamicPanel.removeAll();
        
        String userType = (String) userTypeComboBox.getSelectedItem();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        switch (userType) {
            case "学生":
                setupStudentFields(gbc);
                break;
            case "教师":
                setupTeacherFields(gbc);
                break;
            case "教务人员":
                setupStaffFields(gbc);
                break;
        }
        
        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }
    
    /**
     * 设置学生字段
     */
    private void setupStudentFields(GridBagConstraints gbc) {
        // 年级
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        dynamicPanel.add(new JLabel("年级:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dynamicPanel.add(gradeField, gbc);
        
        // 专业
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        dynamicPanel.add(new JLabel("专业:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dynamicPanel.add(majorField, gbc);
        
        // 班级
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        dynamicPanel.add(new JLabel("班级:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dynamicPanel.add(classNameField, gbc);

        // 添加提示信息
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel tipLabel = new JLabel("<html><font color='gray'>提示：学号格式如 231210400111，班级格式如 软件工程2301</font></html>");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        dynamicPanel.add(tipLabel, gbc);
    }
    
    /**
     * 设置教师字段
     */
    private void setupTeacherFields(GridBagConstraints gbc) {
        // 职称
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        dynamicPanel.add(new JLabel("职称:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dynamicPanel.add(titleComboBox, gbc);
        
        // 学院
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        dynamicPanel.add(new JLabel("学院:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dynamicPanel.add(collegeField, gbc);

        // 添加提示信息
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel tipLabel = new JLabel("<html><font color='gray'>提示：工号格式如 T20240001</font></html>");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        dynamicPanel.add(tipLabel, gbc);
    }
    
    /**
     * 设置教务人员字段
     */
    private void setupStaffFields(GridBagConstraints gbc) {
        // 部门
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        dynamicPanel.add(new JLabel("部门:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dynamicPanel.add(departmentComboBox, gbc);
        
        // 职位
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        dynamicPanel.add(new JLabel("职位:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dynamicPanel.add(positionField, gbc);

        // 添加提示信息
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel tipLabel = new JLabel("<html><font color='gray'>提示：工号格式如 S20240001</font></html>");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        dynamicPanel.add(tipLabel, gbc);
    }
    
    /**
     * 执行注册
     */
    private void performRegister() {
        if (!validateInput()) {
            return;
        }
        
        String userType = (String) userTypeComboBox.getSelectedItem();
        
        registerButton.setText("注册中...");
        registerButton.setEnabled(false);
        
        SwingUtilities.invokeLater(() -> {
            try {
                boolean success = false;
                
                switch (userType) {
                    case "学生":
                        success = registerStudent();
                        break;
                    case "教师":
                        success = registerTeacher();
                        break;
                    case "教务人员":
                        success = registerStaff();
                        break;
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "注册成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "注册失败，请检查输入信息", "失败", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "注册时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } finally {
                registerButton.setText("注册");
                registerButton.setEnabled(true);
            }
        });
    }
    
    /**
     * 验证输入
     */
    private boolean validateInput() {
        String userId = userIdField.getText().trim();
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (userId.isEmpty() || name.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有必填字段", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (!ValidationUtil.isValidPassword(password)) {
            JOptionPane.showMessageDialog(this, "密码长度至少6位", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * 注册学生
     */
    private boolean registerStudent() {
        String userId = userIdField.getText().trim();
        String name = nameField.getText().trim();
        String gender = (String) genderComboBox.getSelectedItem();
        String grade = gradeField.getText().trim();
        String major = majorField.getText().trim();
        String className = classNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // 验证学号格式
        if (!ValidationUtil.isValidStudentId(userId)) {
            JOptionPane.showMessageDialog(this, "学号格式不正确，应为12位数字", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // 验证班级格式
        if (!ValidationUtil.isValidClassName(className)) {
            JOptionPane.showMessageDialog(this, "班级格式不正确，如：软件工程2301", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // 检查或创建班级
        ClassRoom classRoom = classService.getClassByName(className);
        if (classRoom == null) {
            // 如果按班级名称找不到，尝试按生成的班级ID查找
            String generatedClassId = generateClassId(className);
            classRoom = classService.getClassById(generatedClassId);

            if (classRoom == null) {
                // 班级确实不存在，创建新班级
                classRoom = new ClassRoom();
                classRoom.setClassId(generatedClassId);
                classRoom.setClassName(className);
                classRoom.setGrade(grade);
                classRoom.setMajor(major);
                classRoom.setCollege("信息科学与工程学院"); // 默认学院

                System.out.println("RegisterFrame: 创建新班级 " + generatedClassId + " - " + className);

                if (!classService.createClass(classRoom)) {
                    JOptionPane.showMessageDialog(this, "创建班级失败", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                System.out.println("RegisterFrame: 找到已存在的班级 " + classRoom.getClassId() + " - " + classRoom.getClassName());
            }
        } else {
            System.out.println("RegisterFrame: 通过班级名称找到班级 " + classRoom.getClassId() + " - " + classRoom.getClassName());
        }
        
        Student student = new Student(userId, name, gender, grade, major, classRoom.getClassId(), password);
        return studentService.registerStudent(student);
    }
    
    /**
     * 注册教师
     */
    private boolean registerTeacher() {
        String userId = userIdField.getText().trim();
        String name = nameField.getText().trim();
        String gender = (String) genderComboBox.getSelectedItem();
        String title = (String) titleComboBox.getSelectedItem();
        String college = collegeField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!ValidationUtil.isValidTeacherId(userId)) {
            JOptionPane.showMessageDialog(this, "工号格式不正确，应为T开头的9位字符", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Teacher teacher = new Teacher(userId, name, gender, title, college, password);
        return teacherService.registerTeacher(teacher);
    }
    
    /**
     * 注册教务人员
     */
    private boolean registerStaff() {
        String userId = userIdField.getText().trim();
        String name = nameField.getText().trim();
        String gender = (String) genderComboBox.getSelectedItem();
        String department = (String) departmentComboBox.getSelectedItem();
        String position = positionField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!ValidationUtil.isValidStaffId(userId)) {
            JOptionPane.showMessageDialog(this, "工号格式不正确，应为S开头的9位字符", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        AcademicAffairsStaff staff = new AcademicAffairsStaff(userId, name, gender, department, position, password);
        return staffService.registerStaff(staff);
    }
    
    /**
     * 生成班级ID（使用英文缩写格式）
     */
    private String generateClassId(String className) {
        // 从班级名称中提取专业、年级和序号
        // 例如：软件工程2301 -> SE2301
        String yearAndClass = className.replaceAll("[^\\d]", "");
        if (yearAndClass.length() >= 4) {
            String grade = yearAndClass.substring(0, 2);
            String classNum = yearAndClass.substring(2);

            // 根据班级名称确定专业前缀
            String majorPrefix = "CS"; // 默认前缀
            if (className.contains("软件工程")) {
                majorPrefix = "SE";
            } else if (className.contains("计算机")) {
                majorPrefix = "CS";
            } else if (className.contains("数据科学")) {
                majorPrefix = "DS";
            } else if (className.contains("人工智能")) {
                majorPrefix = "AI";
            } else if (className.contains("网络工程")) {
                majorPrefix = "NE";
            } else if (className.contains("信息安全")) {
                majorPrefix = "IS";
            }

            return majorPrefix + grade + classNum;
        }
        return "CS2301"; // 默认值
    }
}
