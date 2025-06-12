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
        if (criteriaScores == null || criteriaScores.trim().isEmpty()) {
            return;
        }

        try {
            String data = criteriaScores.trim();
            // 统一处理JSON和简单格式
            if (data.startsWith("{") && data.endsWith("}")) {
                data = data.substring(1, data.length() - 1); // 移除大括号
            }

            // 解析键值对
            for (String pair : data.split(",")) {
                String[] parts = pair.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim().replaceAll("\"", "");
                    String value = parts[1].trim().replaceAll("\"", "");
                    scoreMap.put(key, Integer.parseInt(value));
                }
            }
        } catch (Exception e) {
            System.err.println("解析评分数据时出错: " + e.getMessage());
        }
    }
    
    /**
     * 构建分数字符串（JSON格式）
     */
    private void buildScoresString() {
        if (scoreMap.isEmpty()) {
            criteriaScores = "";
            return;
        }

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        criteriaScores = sb.toString();
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
