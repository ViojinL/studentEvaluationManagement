package edu.ai.haut.ui.common;

import edu.ai.haut.model.*;
import edu.ai.haut.service.ManagementService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 管理界面通用工具类
 * 提供教务人员和管理员界面的通用对话框和操作方法
 */
public class ManagementUIHelper {
    
    private ManagementService managementService;
    
    public ManagementUIHelper() {
        this.managementService = new ManagementService();
    }
    
    // ==================== 课程管理对话框 ====================
    
    /**
     * 显示添加课程对话框
     */
    public void showAddCourseDialog(JFrame parent, Runnable refreshCallback) {
        JDialog dialog = new JDialog(parent, "添加课程", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = createCourseFormPanel(null);

        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        styleButton(saveButton, new Color(60, 179, 113));
        styleButton(cancelButton, new Color(220, 20, 60));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 获取表单组件
        JTextField courseIdField = (JTextField) findComponentByName(panel, "courseIdField");
        JTextField courseNameField = (JTextField) findComponentByName(panel, "courseNameField");
        JTextField creditsField = (JTextField) findComponentByName(panel, "creditsField");
        JComboBox<String> courseTypeComboBox = (JComboBox<String>) findComponentByName(panel, "courseTypeComboBox");
        JTextField collegeField = (JTextField) findComponentByName(panel, "collegeField");

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
                
                if (managementService.createCourse(course)) {
                    JOptionPane.showMessageDialog(dialog, "课程添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "课程添加失败，可能课程编号已存在", "错误", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加课程时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }
    
    /**
     * 显示编辑课程对话框
     */
    public void showEditCourseDialog(JFrame parent, String courseId, Runnable refreshCallback) {
        Course course = managementService.getCourseById(courseId);
        if (course == null) {
            JOptionPane.showMessageDialog(parent, "课程信息不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(parent, "编辑课程 - " + course.getCourseName(), true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parent);
        
        JPanel panel = createCourseFormPanel(course);
        
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");
        
        styleButton(saveButton, new Color(60, 179, 113));
        styleButton(cancelButton, new Color(220, 20, 60));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // 获取表单组件
        JTextField courseIdField = (JTextField) findComponentByName(panel, "courseIdField");
        JTextField courseNameField = (JTextField) findComponentByName(panel, "courseNameField");
        JTextField creditsField = (JTextField) findComponentByName(panel, "creditsField");
        JComboBox<String> courseTypeComboBox = (JComboBox<String>) findComponentByName(panel, "courseTypeComboBox");
        JTextField collegeField = (JTextField) findComponentByName(panel, "collegeField");
        
        // 课程ID不可编辑
        courseIdField.setEditable(false);
        courseIdField.setBackground(Color.LIGHT_GRAY);
        
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
                
                if (managementService.updateCourse(course)) {
                    JOptionPane.showMessageDialog(dialog, "课程更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "课程更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "更新课程时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }
    
    /**
     * 删除选中的课程
     */
    public void deleteCourse(JFrame parent, String courseId, Runnable refreshCallback) {
        Course course = managementService.getCourseById(courseId);
        if (course == null) {
            JOptionPane.showMessageDialog(parent, "课程信息不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(parent,
            String.format("确定要删除课程：%s (%s) 吗？", course.getCourseName(), course.getCourseId()),
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            if (managementService.deleteCourse(courseId)) {
                JOptionPane.showMessageDialog(parent, "课程删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(parent,
                    "课程删除失败！\n\n该课程存在相关的开课信息，请先删除所有相关开课后再删除课程。",
                    "删除失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // ==================== 私有辅助方法 ====================



    /**
     * 创建课程表单面板
     */
    private JPanel createCourseFormPanel(Course course) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField courseIdField = new JTextField(course != null ? course.getCourseId() : "", 15);
        JTextField courseNameField = new JTextField(course != null ? course.getCourseName() : "", 15);
        JTextField creditsField = new JTextField(course != null ? String.valueOf(course.getCredits()) : "", 15);
        JComboBox<String> courseTypeComboBox = new JComboBox<>(new String[]{"必修", "选修", "实践"});
        if (course != null) {
            courseTypeComboBox.setSelectedItem(course.getCourseType());
        }
        JTextField collegeField = new JTextField(course != null ? course.getCollege() : "", 15);
        
        // 设置组件名称以便查找
        courseIdField.setName("courseIdField");
        courseNameField.setName("courseNameField");
        creditsField.setName("creditsField");
        courseTypeComboBox.setName("courseTypeComboBox");
        collegeField.setName("collegeField");
        
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
        
        return panel;
    }
    
    /**
     * 设置按钮样式
     */
    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }
    
    /**
     * 根据名称查找组件
     */
    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component found = findComponentByName((Container) component, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // ==================== 评教周期管理对话框 ====================

    /**
     * 显示添加评教周期对话框
     */
    public void showAddEvaluationPeriodDialog(JFrame parent, Runnable refreshCallback) {
        JDialog dialog = new JDialog(parent, "创建评教周期", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = createEvaluationPeriodFormPanel(null);

        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        styleButton(saveButton, new Color(60, 179, 113));
        styleButton(cancelButton, new Color(220, 20, 60));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 获取表单组件
        JTextField periodIdField = (JTextField) findComponentByName(panel, "periodIdField");
        JTextField periodNameField = (JTextField) findComponentByName(panel, "periodNameField");
        JTextField semesterField = (JTextField) findComponentByName(panel, "semesterField");
        JTextField startDateField = (JTextField) findComponentByName(panel, "startDateField");
        JTextField endDateField = (JTextField) findComponentByName(panel, "endDateField");
        JComboBox<String> statusComboBox = (JComboBox<String>) findComponentByName(panel, "statusComboBox");

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

                LocalDate startDate, endDate;
                try {
                    startDate = LocalDate.parse(startDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    endDate = LocalDate.parse(endDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "日期格式不正确，请使用 yyyy-MM-dd 格式", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (startDate.isAfter(endDate)) {
                    JOptionPane.showMessageDialog(dialog, "开始日期不能晚于结束日期", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                EvaluationPeriod period = new EvaluationPeriod(periodId, periodName, semester, startDate, endDate, status);

                if (managementService.createEvaluationPeriod(period)) {
                    JOptionPane.showMessageDialog(dialog, "评教周期创建成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "评教周期创建失败，可能周期编号已存在", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "创建评教周期时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 显示编辑评教周期对话框
     */
    public void showEditEvaluationPeriodDialog(JFrame parent, String periodId, Runnable refreshCallback) {
        EvaluationPeriod period = managementService.getEvaluationPeriodById(periodId);
        if (period == null) {
            JOptionPane.showMessageDialog(parent, "评教周期信息不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parent, "编辑评教周期 - " + period.getPeriodName(), true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = createEvaluationPeriodFormPanel(period);

        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        styleButton(saveButton, new Color(60, 179, 113));
        styleButton(cancelButton, new Color(220, 20, 60));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 获取表单组件
        JTextField periodIdField = (JTextField) findComponentByName(panel, "periodIdField");
        JTextField periodNameField = (JTextField) findComponentByName(panel, "periodNameField");
        JTextField semesterField = (JTextField) findComponentByName(panel, "semesterField");
        JTextField startDateField = (JTextField) findComponentByName(panel, "startDateField");
        JTextField endDateField = (JTextField) findComponentByName(panel, "endDateField");
        JComboBox<String> statusComboBox = (JComboBox<String>) findComponentByName(panel, "statusComboBox");

        // 周期ID不可编辑
        periodIdField.setEditable(false);
        periodIdField.setBackground(Color.LIGHT_GRAY);

        saveButton.addActionListener(e -> {
            try {
                String periodName = periodNameField.getText().trim();
                String semester = semesterField.getText().trim();
                String startDateText = startDateField.getText().trim();
                String endDateText = endDateField.getText().trim();
                String status = (String) statusComboBox.getSelectedItem();

                if (periodName.isEmpty() || semester.isEmpty() ||
                    startDateText.isEmpty() || endDateText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalDate startDate, endDate;
                try {
                    startDate = LocalDate.parse(startDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    endDate = LocalDate.parse(endDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "日期格式不正确，请使用 yyyy-MM-dd 格式", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (startDate.isAfter(endDate)) {
                    JOptionPane.showMessageDialog(dialog, "开始日期不能晚于结束日期", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                period.setPeriodName(periodName);
                period.setSemester(semester);
                period.setStartDate(startDate);
                period.setEndDate(endDate);
                period.setStatus(status);

                if (managementService.updateEvaluationPeriod(period)) {
                    JOptionPane.showMessageDialog(dialog, "评教周期更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "评教周期更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "更新评教周期时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 创建评教周期表单面板
     */
    private JPanel createEvaluationPeriodFormPanel(EvaluationPeriod period) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField periodIdField = new JTextField(period != null ? period.getPeriodId() : "", 15);
        JTextField periodNameField = new JTextField(period != null ? period.getPeriodName() : "", 15);
        JTextField semesterField = new JTextField(period != null ? period.getSemester() : "", 15);
        JTextField startDateField = new JTextField(period != null ? period.getStartDate().toString() : "", 15);
        JTextField endDateField = new JTextField(period != null ? period.getEndDate().toString() : "", 15);
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"未开始", "进行中", "已完成", "已关闭"});
        if (period != null) {
            statusComboBox.setSelectedItem(period.getStatus());
        }

        // 设置组件名称以便查找
        periodIdField.setName("periodIdField");
        periodNameField.setName("periodNameField");
        semesterField.setName("semesterField");
        startDateField.setName("startDateField");
        endDateField.setName("endDateField");
        statusComboBox.setName("statusComboBox");

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

        // 添加日期格式提示
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel dateFormatLabel = new JLabel("日期格式: yyyy-MM-dd (例如: 2024-03-01)");
        dateFormatLabel.setFont(new Font("微软雅黑", Font.ITALIC, 10));
        dateFormatLabel.setForeground(Color.GRAY);
        panel.add(dateFormatLabel, gbc);

        return panel;
    }

    // ==================== 评教指标管理对话框 ====================

    /**
     * 显示评教指标管理对话框
     */
    public void showEvaluationCriteriaDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "评教指标管理", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("添加指标");
        JButton editButton = new JButton("编辑指标");
        JButton deleteButton = new JButton("删除指标");
        JButton closeButton = new JButton("关闭");

        styleButton(addButton, new Color(60, 179, 113));
        styleButton(editButton, new Color(255, 165, 0));
        styleButton(deleteButton, new Color(220, 20, 60));
        styleButton(closeButton, new Color(70, 130, 180));

        toolPanel.add(addButton);
        toolPanel.add(editButton);
        toolPanel.add(deleteButton);
        toolPanel.add(closeButton);

        mainPanel.add(toolPanel, BorderLayout.NORTH);

        // 指标列表表格
        String[] columns = {"指标编号", "指标名称", "描述", "权重(%)", "最高分"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable criteriaTable = new JTable(tableModel);
        TableUtil.styleTable(criteriaTable);

        JScrollPane scrollPane = new JScrollPane(criteriaTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 加载指标数据
        loadCriteriaData(tableModel);

        // 按钮事件
        addButton.addActionListener(e -> showAddCriteriaDialog(dialog, tableModel));

        editButton.addActionListener(e -> {
            int selectedRow = criteriaTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "请选择要编辑的指标", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String criteriaId = (String) tableModel.getValueAt(selectedRow, 0);
            showEditCriteriaDialog(dialog, tableModel, criteriaId);
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = criteriaTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "请选择要删除的指标", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String criteriaId = (String) tableModel.getValueAt(selectedRow, 0);
            String criteriaName = (String) tableModel.getValueAt(selectedRow, 1);

            int option = JOptionPane.showConfirmDialog(dialog,
                String.format("确定要删除指标：%s (%s) 吗？", criteriaName, criteriaId),
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                if (managementService.deleteEvaluationCriteria(criteriaId)) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(dialog, "指标删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "指标删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 加载评教指标数据
     */
    private void loadCriteriaData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        try {
            List<EvaluationCriteria> criteriaList = managementService.getAllEvaluationCriteria();
            for (EvaluationCriteria criteria : criteriaList) {
                Object[] row = {
                    criteria.getCriteriaId(),
                    criteria.getCriteriaName(),
                    criteria.getDescription(),
                    criteria.getWeight(),
                    criteria.getMaxScore()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            System.err.println("加载评教指标数据失败: " + e.getMessage());
        }
    }

    /**
     * 显示添加评教指标对话框
     */
    private void showAddCriteriaDialog(JDialog parentDialog, DefaultTableModel tableModel) {
        JDialog dialog = new JDialog(parentDialog, "添加评教指标", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentDialog);

        JPanel panel = createCriteriaFormPanel(null);

        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        styleButton(saveButton, new Color(60, 179, 113));
        styleButton(cancelButton, new Color(220, 20, 60));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 获取表单组件
        JTextField criteriaIdField = (JTextField) findComponentByName(panel, "criteriaIdField");
        JTextField criteriaNameField = (JTextField) findComponentByName(panel, "criteriaNameField");
        JTextArea descriptionArea = (JTextArea) findComponentByName(panel, "descriptionArea");
        JTextField weightField = (JTextField) findComponentByName(panel, "weightField");
        JTextField maxScoreField = (JTextField) findComponentByName(panel, "maxScoreField");

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

                if (managementService.createEvaluationCriteria(criteria)) {
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

        dialog.setVisible(true);
    }

    /**
     * 显示编辑评教指标对话框
     */
    private void showEditCriteriaDialog(JDialog parentDialog, DefaultTableModel tableModel, String criteriaId) {
        EvaluationCriteria criteria = managementService.getEvaluationCriteriaById(criteriaId);
        if (criteria == null) {
            JOptionPane.showMessageDialog(parentDialog, "评教指标信息不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parentDialog, "编辑评教指标 - " + criteria.getCriteriaName(), true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentDialog);

        JPanel panel = createCriteriaFormPanel(criteria);

        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        styleButton(saveButton, new Color(60, 179, 113));
        styleButton(cancelButton, new Color(220, 20, 60));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 获取表单组件
        JTextField criteriaIdField = (JTextField) findComponentByName(panel, "criteriaIdField");
        JTextField criteriaNameField = (JTextField) findComponentByName(panel, "criteriaNameField");
        JTextArea descriptionArea = (JTextArea) findComponentByName(panel, "descriptionArea");
        JTextField weightField = (JTextField) findComponentByName(panel, "weightField");
        JTextField maxScoreField = (JTextField) findComponentByName(panel, "maxScoreField");

        // 指标ID不可编辑
        criteriaIdField.setEditable(false);
        criteriaIdField.setBackground(Color.LIGHT_GRAY);

        saveButton.addActionListener(e -> {
            try {
                String criteriaName = criteriaNameField.getText().trim();
                String description = descriptionArea.getText().trim();
                String weightText = weightField.getText().trim();
                String maxScoreText = maxScoreField.getText().trim();

                if (criteriaName.isEmpty() || weightText.isEmpty() || maxScoreText.isEmpty()) {
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

                criteria.setCriteriaName(criteriaName);
                criteria.setDescription(description);
                criteria.setWeight(weight);
                criteria.setMaxScore(maxScore);

                if (managementService.updateEvaluationCriteria(criteria)) {
                    // 更新表格中的数据
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (criteriaId.equals(tableModel.getValueAt(i, 0))) {
                            tableModel.setValueAt(criteriaName, i, 1);
                            tableModel.setValueAt(description, i, 2);
                            tableModel.setValueAt(weight, i, 3);
                            tableModel.setValueAt(maxScore, i, 4);
                            break;
                        }
                    }
                    JOptionPane.showMessageDialog(dialog, "评教指标更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "评教指标更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "权重和最高分必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "更新评教指标时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 创建评教指标表单面板
     */
    private JPanel createCriteriaFormPanel(EvaluationCriteria criteria) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField criteriaIdField = new JTextField(criteria != null ? criteria.getCriteriaId() : "", 15);
        JTextField criteriaNameField = new JTextField(criteria != null ? criteria.getCriteriaName() : "", 15);
        JTextArea descriptionArea = new JTextArea(criteria != null ? criteria.getDescription() : "", 3, 15);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JTextField weightField = new JTextField(criteria != null ? String.valueOf(criteria.getWeight()) : "", 15);
        JTextField maxScoreField = new JTextField(criteria != null ? String.valueOf(criteria.getMaxScore()) : "100", 15);

        // 设置组件名称以便查找
        criteriaIdField.setName("criteriaIdField");
        criteriaNameField.setName("criteriaNameField");
        descriptionArea.setName("descriptionArea");
        weightField.setName("weightField");
        maxScoreField.setName("maxScoreField");

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

        return panel;
    }

    // ==================== 通用工具方法 ====================

    /**
     * 获取ManagementService实例
     */
    public ManagementService getManagementService() {
        return managementService;
    }

    /**
     * 设置表格样式
     */
    public void setupTableStyle(JTable table) {
        TableUtil.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * 根据分数获取等级
     */
    public String getGradeByScore(double score) {
        if (score >= 90) return "优秀";
        else if (score >= 80) return "良好";
        else if (score >= 70) return "中等";
        else if (score >= 60) return "及格";
        else return "不及格";
    }

    /**
     * 根据等级获取颜色
     */
    public Color getGradeColor(String grade) {
        switch (grade) {
            case "优秀": return new Color(34, 139, 34);
            case "良好": return new Color(0, 128, 255);
            case "中等": return new Color(255, 165, 0);
            case "及格": return new Color(255, 140, 0);
            case "不及格": return new Color(220, 20, 60);
            default: return Color.BLACK;
        }
    }

    /**
     * 显示确认删除对话框
     */
    public boolean showDeleteConfirmDialog(JFrame parent, String itemName, String itemId) {
        int option = JOptionPane.showConfirmDialog(parent,
            String.format("确定要删除：%s (%s) 吗？", itemName, itemId),
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        return option == JOptionPane.YES_OPTION;
    }

    /**
     * 显示成功消息
     */
    public void showSuccessMessage(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示错误消息
     */
    public void showErrorMessage(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 显示警告消息
     */
    public void showWarningMessage(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "提示", JOptionPane.WARNING_MESSAGE);
    }
}
