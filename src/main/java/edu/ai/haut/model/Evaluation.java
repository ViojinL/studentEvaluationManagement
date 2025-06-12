package edu.ai.haut.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 评教记录实体类
 * 对应数据库中的evaluations表
 */
public class Evaluation {
    
    private String evaluationId;   // 评教记录编号
    private String studentId;      // 学生学号
    private String offeringId;     // 开课编号
    private String periodId;       // 评教周期编号
    private String criteriaScores; // 各指标分数（JSON格式）
    private double totalScore;     // 总分
    private String comments;       // 评价意见
    private LocalDateTime evaluationDate; // 评教日期
    
    // 关联对象
    private Student student;       // 学生对象
    private CourseOffering courseOffering; // 开课信息对象
    private EvaluationPeriod period; // 评教周期对象
    private Map<String, Integer> scoreMap; // 分数映射
    
    /**
     * 默认构造函数
     */
    public Evaluation() {
        this.evaluationDate = LocalDateTime.now();
        this.scoreMap = new HashMap<>();
    }
    
    /**
     * 带参数的构造函数
     */
    public Evaluation(String evaluationId, String studentId, String offeringId, 
                     String periodId, String criteriaScores, double totalScore, String comments) {
        this.evaluationId = evaluationId;
        this.studentId = studentId;
        this.offeringId = offeringId;
        this.periodId = periodId;
        this.criteriaScores = criteriaScores;
        this.totalScore = totalScore;
        this.comments = comments;
        this.evaluationDate = LocalDateTime.now();
        this.scoreMap = new HashMap<>();
        parseScores();
    }
    
    // Getter和Setter方法
    public String getEvaluationId() {
        return evaluationId;
    }
    
    public void setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getOfferingId() {
        return offeringId;
    }
    
    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
    }
    
    public String getPeriodId() {
        return periodId;
    }
    
    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }
    
    public String getCriteriaScores() {
        return criteriaScores;
    }
    
    public void setCriteriaScores(String criteriaScores) {
        this.criteriaScores = criteriaScores;
        parseScores();
    }
    
    public double getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public LocalDateTime getEvaluationDate() {
        return evaluationDate;
    }
    
    public void setEvaluationDate(LocalDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public CourseOffering getCourseOffering() {
        return courseOffering;
    }
    
    public void setCourseOffering(CourseOffering courseOffering) {
        this.courseOffering = courseOffering;
    }
    
    public EvaluationPeriod getPeriod() {
        return period;
    }
    
    public void setPeriod(EvaluationPeriod period) {
        this.period = period;
    }
    
    public Map<String, Integer> getScoreMap() {
        return scoreMap;
    }
    
    public void setScoreMap(Map<String, Integer> scoreMap) {
        this.scoreMap = scoreMap;
        buildScoresString();
    }
    
    /**
     * 解析分数字符串为Map
     */
    private void parseScores() {
        scoreMap.clear();
        if (criteriaScores != null && !criteriaScores.trim().isEmpty()) {
            try {
                // 检查是否为JSON格式
                if (criteriaScores.trim().startsWith("{") && criteriaScores.trim().endsWith("}")) {
                    // JSON格式：{"C001":85,"C002":90}
                    parseJsonScores();
                } else {
                    // 简单的键值对格式：C001:85,C002:90
                    parseSimpleScores();
                }
            } catch (Exception e) {
                System.err.println("解析评分数据时出错: " + e.getMessage());
                // 如果解析失败，保持scoreMap为空
            }
        }
    }

    /**
     * 解析JSON格式的分数
     */
    private void parseJsonScores() {
        String json = criteriaScores.trim();
        // 移除大括号
        json = json.substring(1, json.length() - 1);

        // 按逗号分割键值对
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            // 按冒号分割键和值
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                try {
                    // 移除引号和空格
                    String key = parts[0].trim().replaceAll("\"", "");
                    String value = parts[1].trim().replaceAll("\"", "");
                    scoreMap.put(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    System.err.println("解析分数值时出错: " + parts[1] + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * 解析简单格式的分数
     */
    private void parseSimpleScores() {
        String[] pairs = criteriaScores.split(",");
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                try {
                    scoreMap.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException e) {
                    System.err.println("解析分数值时出错: " + parts[1] + " - " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 构建分数字符串
     */
    private void buildScoresString() {
        if (scoreMap.isEmpty()) {
            criteriaScores = "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(entry.getKey()).append(":").append(entry.getValue());
            }
            criteriaScores = sb.toString();
        }
    }
    
    /**
     * 设置某个指标的分数
     */
    public void setScore(String criteriaId, int score) {
        scoreMap.put(criteriaId, score);
        buildScoresString();
    }
    
    /**
     * 获取某个指标的分数
     */
    public int getScore(String criteriaId) {
        return scoreMap.getOrDefault(criteriaId, 0);
    }
    
    /**
     * 获取评价等级
     */
    public String getGrade() {
        if (totalScore >= 90) return "优秀";
        else if (totalScore >= 80) return "良好";
        else if (totalScore >= 70) return "中等";
        else if (totalScore >= 60) return "及格";
        else return "不及格";
    }
    
    /**
     * 获取完整的评教信息字符串
     */
    public String getFullInfo() {
        String studentName = student != null ? student.getName() : "未知学生";
        String courseName = courseOffering != null && courseOffering.getCourse() != null ? 
                           courseOffering.getCourse().getCourseName() : "未知课程";
        String teacherName = courseOffering != null && courseOffering.getTeacher() != null ? 
                           courseOffering.getTeacher().getName() : "未知教师";
        
        return String.format("评教编号: %s, 学生: %s, 课程: %s, 教师: %s, 总分: %.1f, 等级: %s",
                           evaluationId, studentName, courseName, teacherName, totalScore, getGrade());
    }
    
    /**
     * 获取简要信息（用于列表显示）
     */
    public String getBriefInfo() {
        String courseName = courseOffering != null && courseOffering.getCourse() != null ? 
                           courseOffering.getCourse().getCourseName() : "未知课程";
        
        return String.format("%s - %.1f分 (%s)", courseName, totalScore, getGrade());
    }
    
    @Override
    public String toString() {
        return String.format("Evaluation{evaluationId='%s', studentId='%s', offeringId='%s', totalScore=%.1f}", 
                           evaluationId, studentId, offeringId, totalScore);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Evaluation evaluation = (Evaluation) obj;
        return evaluationId != null && evaluationId.equals(evaluation.evaluationId);
    }
    
    @Override
    public int hashCode() {
        return evaluationId != null ? evaluationId.hashCode() : 0;
    }
}
