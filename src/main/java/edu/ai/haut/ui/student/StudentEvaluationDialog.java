package edu.ai.haut.ui.student;

import edu.ai.haut.model.*;
import edu.ai.haut.service.EvaluationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生评教对话框
 * 用于学生对课程进行评教
 */
public class StudentEvaluationDialog extends JDialog {
    
    private CourseOffering courseOffering;
    private EvaluationPeriod evaluationPeriod;
    private Student student;
    private EvaluationService evaluationService;
    
    private List<EvaluationCriteria> criteriaList;
    private Map<String, JSlider> scoreSliders;
    private Map<String, JLabel> scoreLabels;
    private JTextArea commentsArea;
    private JLabel totalScoreLabel;
    
    private JButton submitButton;
    private JButton cancelButton;
    
    private boolean evaluationSubmitted = false;
    
    public StudentEvaluationDialog(Frame parent, CourseOffering offering, 
                                 EvaluationPeriod period, Student student) {
        super(parent, "课程评教", true);
        this.courseOffering = offering;
        this.evaluationPeriod = period;
        this.student = student;
        this.evaluationService = new EvaluationService();
        this.scoreSliders = new HashMap<>();
        this.scoreLabels = new HashMap<>();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setupFrame();
        loadEvaluationCriteria();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        commentsArea = new JTextArea(5, 30);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setBorder(BorderFactory.createLoweredBevelBorder());
        
        totalScoreLabel = new JLabel("总分: 0.0");
        totalScoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        totalScoreLabel.setForeground(new Color(70, 130, 180));
        
        submitButton = new JButton("提交评教");
        cancelButton = new JButton("取消");
        
        submitButton.setBackground(new Color(60, 179, 113));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        
        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部课程信息面板
        JPanel courseInfoPanel = createCourseInfoPanel();
        add(courseInfoPanel, BorderLayout.NORTH);
        
        // 中间评教内容面板
        JPanel evaluationPanel = new JPanel(new BorderLayout());
        evaluationPanel.setBorder(BorderFactory.createTitledBorder("评教内容"));
        
        // 评教指标面板
        JPanel criteriaPanel = new JPanel();
        criteriaPanel.setLayout(new BoxLayout(criteriaPanel, BoxLayout.Y_AXIS));
        
        JScrollPane criteriaScrollPane = new JScrollPane(criteriaPanel);
        criteriaScrollPane.setPreferredSize(new Dimension(600, 300));
        criteriaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        evaluationPanel.add(criteriaScrollPane, BorderLayout.CENTER);
        
        // 评价意见面板
        JPanel commentsPanel = new JPanel(new BorderLayout());
        commentsPanel.setBorder(BorderFactory.createTitledBorder("评价意见（选填）"));
        commentsPanel.add(new JScrollPane(commentsArea), BorderLayout.CENTER);
        
        evaluationPanel.add(commentsPanel, BorderLayout.SOUTH);
        
        add(evaluationPanel, BorderLayout.CENTER);
        
        // 底部按钮面板
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建课程信息面板
     */
    private JPanel createCourseInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("课程信息"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 课程名称
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("课程名称:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(courseOffering.getCourse().getCourseName()), gbc);
        
        // 授课教师
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("授课教师:"), gbc);
        gbc.gridx = 3;
        panel.add(new JLabel(courseOffering.getTeacher().getName()), gbc);
        
        // 学期
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("学期:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(courseOffering.getSemester()), gbc);
        
        // 评教周期
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("评教周期:"), gbc);
        gbc.gridx = 3;
        panel.add(new JLabel(evaluationPeriod.getPeriodName()), gbc);
        
        return panel;
    }
    
    /**
     * 创建底部面板
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 总分显示
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scorePanel.add(totalScoreLabel);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        panel.add(scorePanel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitEvaluation();
            }
        });
        
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
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        // 设置窗口图标
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        } catch (Exception e) {
            // 忽略图标加载错误
        }
    }
    
    /**
     * 加载评教指标
     */
    private void loadEvaluationCriteria() {
        try {
            criteriaList = evaluationService.getAllEvaluationCriteria();
            
            if (criteriaList.isEmpty()) {
                // 如果没有评教指标，创建默认指标
                createDefaultCriteria();
            }
            
            createCriteriaComponents();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载评教指标失败: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 创建默认评教指标
     */
    private void createDefaultCriteria() {
        criteriaList = new ArrayList<>();
        
        criteriaList.add(new EvaluationCriteria("C001", "教学态度", "教师的教学态度和责任心", 20.0, 100));
        criteriaList.add(new EvaluationCriteria("C002", "教学内容", "教学内容的丰富性和实用性", 25.0, 100));
        criteriaList.add(new EvaluationCriteria("C003", "教学方法", "教学方法的有效性和创新性", 20.0, 100));
        criteriaList.add(new EvaluationCriteria("C004", "课堂管理", "课堂秩序和时间管理", 15.0, 100));
        criteriaList.add(new EvaluationCriteria("C005", "师生互动", "与学生的互动和沟通", 20.0, 100));
    }
    
    /**
     * 创建评教指标组件
     */
    private void createCriteriaComponents() {
        // 获取评教内容面板中的滚动面板
        JPanel evaluationPanel = (JPanel) getContentPane().getComponent(1);
        JScrollPane criteriaScrollPane = (JScrollPane) evaluationPanel.getComponent(0);
        JPanel criteriaContainer = (JPanel) criteriaScrollPane.getViewport().getView();

        for (EvaluationCriteria criteria : criteriaList) {
            JPanel criteriaPanel = createCriteriaPanel(criteria);
            criteriaContainer.add(criteriaPanel);
        }

        // 刷新显示
        criteriaContainer.revalidate();
        criteriaContainer.repaint();
    }
    
    /**
     * 创建单个评教指标面板
     */
    private JPanel createCriteriaPanel(EvaluationCriteria criteria) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // 指标信息
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(criteria.getCriteriaName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        JLabel descLabel = new JLabel(criteria.getDescription());
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        
        JLabel weightLabel = new JLabel(String.format("权重: %.1f%%", criteria.getWeight()));
        weightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        weightLabel.setForeground(Color.BLUE);
        
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(descLabel, BorderLayout.CENTER);
        infoPanel.add(weightLabel, BorderLayout.SOUTH);
        
        // 评分滑块
        JPanel scorePanel = new JPanel(new BorderLayout());
        JSlider scoreSlider = new JSlider(0, criteria.getMaxScore(), criteria.getMaxScore());
        scoreSlider.setMajorTickSpacing(20);
        scoreSlider.setMinorTickSpacing(10);
        scoreSlider.setPaintTicks(true);
        scoreSlider.setPaintLabels(true);
        
        JLabel scoreLabel = new JLabel(String.valueOf(criteria.getMaxScore()));
        scoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        scoreLabel.setForeground(new Color(70, 130, 180));
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setPreferredSize(new Dimension(50, 30));
        
        // 滑块变化事件
        scoreSlider.addChangeListener(e -> {
            int value = scoreSlider.getValue();
            scoreLabel.setText(String.valueOf(value));
            updateTotalScore();
        });
        
        scorePanel.add(scoreSlider, BorderLayout.CENTER);
        scorePanel.add(scoreLabel, BorderLayout.EAST);
        
        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(scorePanel, BorderLayout.CENTER);
        
        // 保存组件引用
        scoreSliders.put(criteria.getCriteriaId(), scoreSlider);
        scoreLabels.put(criteria.getCriteriaId(), scoreLabel);
        
        return panel;
    }
    
    /**
     * 更新总分
     */
    private void updateTotalScore() {
        double totalScore = 0.0;
        
        for (EvaluationCriteria criteria : criteriaList) {
            JSlider slider = scoreSliders.get(criteria.getCriteriaId());
            if (slider != null) {
                int score = slider.getValue();
                double weightedScore = (score * criteria.getWeight()) / 100.0;
                totalScore += weightedScore;
            }
        }
        
        totalScoreLabel.setText(String.format("总分: %.1f", totalScore));
        
        // 根据分数设置颜色
        if (totalScore >= 90) {
            totalScoreLabel.setForeground(new Color(0, 128, 0)); // 绿色
        } else if (totalScore >= 80) {
            totalScoreLabel.setForeground(new Color(70, 130, 180)); // 蓝色
        } else if (totalScore >= 70) {
            totalScoreLabel.setForeground(new Color(255, 165, 0)); // 橙色
        } else {
            totalScoreLabel.setForeground(new Color(220, 20, 60)); // 红色
        }
    }
    
    /**
     * 提交评教
     */
    private void submitEvaluation() {
        // 确认提交
        int option = JOptionPane.showConfirmDialog(this, 
            "确定要提交评教吗？提交后将无法修改。", 
            "确认提交", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // 创建评教记录
            Evaluation evaluation = new Evaluation();
            evaluation.setEvaluationId(generateEvaluationId());
            evaluation.setStudentId(student.getStudentId());
            evaluation.setOfferingId(courseOffering.getOfferingId());
            evaluation.setPeriodId(evaluationPeriod.getPeriodId());
            evaluation.setComments(commentsArea.getText().trim());
            
            // 设置各指标分数
            Map<String, Integer> scores = new HashMap<>();
            double totalScore = 0.0;
            
            for (EvaluationCriteria criteria : criteriaList) {
                JSlider slider = scoreSliders.get(criteria.getCriteriaId());
                if (slider != null) {
                    int score = slider.getValue();
                    scores.put(criteria.getCriteriaId(), score);
                    double weightedScore = (score * criteria.getWeight()) / 100.0;
                    totalScore += weightedScore;
                }
            }
            
            evaluation.setScoreMap(scores);
            evaluation.setTotalScore(totalScore);
            
            // 提交评教
            if (evaluationService.submitEvaluation(evaluation)) {
                evaluationSubmitted = true;
                JOptionPane.showMessageDialog(this, 
                    String.format("评教提交成功！\n总分: %.1f分\n等级: %s", 
                        totalScore, evaluation.getGrade()), 
                    "提交成功", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "评教提交失败，请重试", 
                    "提交失败", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "提交评教时发生错误: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 生成评教记录ID
     */
    private String generateEvaluationId() {
        return "E" + System.currentTimeMillis();
    }
    
    /**
     * 是否已提交评教
     */
    public boolean isEvaluationSubmitted() {
        return evaluationSubmitted;
    }
}
