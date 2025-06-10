package edu.ai.haut.model;

import java.time.LocalDateTime;

/**
 * 课程实体类
 * 对应数据库中的courses表
 */
public class Course {
    
    private String courseId;       // 课程编号
    private String courseName;     // 课程名称
    private double credits;        // 学分
    private String courseType;     // 课程类型（必修、选修、实践）
    private String college;        // 开课学院
    private LocalDateTime createdAt; // 创建时间
    
    /**
     * 默认构造函数
     */
    public Course() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 带参数的构造函数
     */
    public Course(String courseId, String courseName, double credits, 
                  String courseType, String college) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.courseType = courseType;
        this.college = college;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public double getCredits() {
        return credits;
    }
    
    public void setCredits(double credits) {
        this.credits = credits;
    }
    
    public String getCourseType() {
        return courseType;
    }
    
    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }
    
    public String getCollege() {
        return college;
    }
    
    public void setCollege(String college) {
        this.college = college;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * 获取完整的课程信息字符串
     */
    public String getFullInfo() {
        return String.format("课程编号: %s, 课程名称: %s, 学分: %.1f, 课程类型: %s, 开课学院: %s",
                           courseId, courseName, credits, courseType, college);
    }
    
    @Override
    public String toString() {
        return String.format("Course{courseId='%s', courseName='%s', credits=%.1f, courseType='%s', college='%s'}", 
                           courseId, courseName, credits, courseType, college);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return courseId != null && courseId.equals(course.courseId);
    }
    
    @Override
    public int hashCode() {
        return courseId != null ? courseId.hashCode() : 0;
    }
}
