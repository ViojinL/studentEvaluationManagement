package edu.ai.haut.service;

import edu.ai.haut.model.*;
import edu.ai.haut.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * 通用管理服务类
 * 抽象教务人员和管理员的共同操作，减少代码重复
 */
public class ManagementService {
    
    private CourseService courseService;
    private EvaluationService evaluationService;
    private TeacherService teacherService;
    private StudentService studentService;
    private ClassService classService;
    private StatisticsService statisticsService;
    
    public ManagementService() {
        this.courseService = new CourseService();
        this.evaluationService = new EvaluationService();
        this.teacherService = new TeacherService();
        this.studentService = new StudentService();
        this.classService = new ClassService();
        this.statisticsService = new StatisticsService();
    }
    
    // ==================== 课程管理相关方法 ====================
    
    /**
     * 创建课程
     */
    public boolean createCourse(Course course) {
        return courseService.createCourse(course);
    }
    
    /**
     * 更新课程
     */
    public boolean updateCourse(Course course) {
        return courseService.updateCourse(course);
    }
    
    /**
     * 删除课程
     */
    public boolean deleteCourse(String courseId) {
        return courseService.deleteCourse(courseId);
    }
    
    /**
     * 获取所有课程
     */
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }
    
    /**
     * 根据ID获取课程
     */
    public Course getCourseById(String courseId) {
        return courseService.getCourseById(courseId);
    }
    
    // ==================== 开课管理相关方法 ====================
    
    /**
     * 创建开课
     */
    public boolean createCourseOffering(CourseOffering offering) {
        return courseService.createCourseOffering(offering);
    }
    
    /**
     * 更新开课
     */
    public boolean updateCourseOffering(CourseOffering offering) {
        return courseService.updateCourseOffering(offering);
    }
    
    /**
     * 删除开课
     */
    public boolean deleteCourseOffering(String offeringId) {
        return courseService.deleteCourseOffering(offeringId);
    }
    
    /**
     * 获取所有开课
     */
    public List<CourseOffering> getAllCourseOfferings() {
        return courseService.getAllCourseOfferings();
    }
    
    /**
     * 根据课程ID获取开课列表
     */
    public List<CourseOffering> getCourseOfferingsByCourseId(String courseId) {
        return courseService.getAllCourseOfferings().stream()
            .filter(offering -> courseId.equals(offering.getCourseId()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    // ==================== 评教周期管理相关方法 ====================
    
    /**
     * 创建评教周期
     */
    public boolean createEvaluationPeriod(EvaluationPeriod period) {
        return evaluationService.createEvaluationPeriod(period);
    }
    
    /**
     * 更新评教周期
     */
    public boolean updateEvaluationPeriod(EvaluationPeriod period) {
        return evaluationService.updateEvaluationPeriod(period);
    }
    
    /**
     * 获取所有评教周期
     */
    public List<EvaluationPeriod> getAllEvaluationPeriods() {
        return evaluationService.getAllEvaluationPeriods();
    }
    
    /**
     * 根据ID获取评教周期
     */
    public EvaluationPeriod getEvaluationPeriodById(String periodId) {
        return evaluationService.getAllEvaluationPeriods().stream()
            .filter(period -> periodId.equals(period.getPeriodId()))
            .findFirst()
            .orElse(null);
    }
    
    // ==================== 评教指标管理相关方法 ====================
    
    /**
     * 创建评教指标
     */
    public boolean createEvaluationCriteria(EvaluationCriteria criteria) {
        return evaluationService.createEvaluationCriteria(criteria);
    }
    
    /**
     * 更新评教指标
     */
    public boolean updateEvaluationCriteria(EvaluationCriteria criteria) {
        return evaluationService.updateEvaluationCriteria(criteria);
    }
    
    /**
     * 删除评教指标
     */
    public boolean deleteEvaluationCriteria(String criteriaId) {
        return evaluationService.deleteEvaluationCriteria(criteriaId);
    }
    
    /**
     * 获取所有评教指标
     */
    public List<EvaluationCriteria> getAllEvaluationCriteria() {
        return evaluationService.getAllEvaluationCriteria();
    }
    
    /**
     * 根据ID获取评教指标
     */
    public EvaluationCriteria getEvaluationCriteriaById(String criteriaId) {
        return evaluationService.getEvaluationCriteriaById(criteriaId);
    }
    
    // ==================== 评教数据查询相关方法 ====================
    
    /**
     * 根据教师ID和周期ID获取评教列表
     */
    public List<Evaluation> getEvaluationsByTeacherAndPeriod(String teacherId, String periodId) {
        // 这个方法需要在EvaluationService中实现，暂时返回空列表
        return new java.util.ArrayList<>();
    }

    /**
     * 根据学生ID获取评教历史
     */
    public List<Evaluation> getEvaluationsByStudentId(String studentId) {
        // 这个方法需要在EvaluationService中实现，暂时返回空列表
        return new java.util.ArrayList<>();
    }
    
    /**
     * 根据评教ID获取详细信息
     */
    public Evaluation getEvaluationById(String evaluationId) {
        return evaluationService.getEvaluationById(evaluationId);
    }
    
    // ==================== 统计分析相关方法 ====================
    
    /**
     * 获取教师评教统计
     */
    public Map<String, Object> getTeacherEvaluationStatistics(String periodId) {
        return statisticsService.getTeacherEvaluationStatistics(periodId);
    }
    
    /**
     * 获取课程评教统计
     */
    public Map<String, Object> getCourseEvaluationStatistics(String periodId) {
        return statisticsService.getCourseEvaluationStatistics(periodId);
    }
    
    /**
     * 获取学生评教统计
     */
    public Map<String, Object> getStudentEvaluationStatistics(String periodId) {
        return statisticsService.getStudentEvaluationStatistics(periodId);
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取所有教师
     */
    public List<Teacher> getAllTeachers() {
        return teacherService.getAllTeachers();
    }
    
    /**
     * 获取所有学生
     */
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }
    
    /**
     * 获取所有班级
     */
    public List<ClassRoom> getAllClasses() {
        return classService.getAllClasses();
    }
    
    /**
     * 根据ID获取教师
     */
    public Teacher getTeacherById(String teacherId) {
        return teacherService.getTeacherById(teacherId);
    }
    
    /**
     * 根据ID获取班级
     */
    public ClassRoom getClassRoomById(String classId) {
        return classService.getAllClasses().stream()
            .filter(classRoom -> classId.equals(classRoom.getClassId()))
            .findFirst()
            .orElse(null);
    }
    
    // ==================== 数据验证方法 ====================
    
    /**
     * 验证课程数据
     */
    public boolean validateCourseData(Course course) {
        if (course == null) return false;
        if (course.getCourseId() == null || course.getCourseId().trim().isEmpty()) return false;
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) return false;
        if (course.getCredits() <= 0) return false;
        return true;
    }
    
    /**
     * 验证评教周期数据
     */
    public boolean validateEvaluationPeriodData(EvaluationPeriod period) {
        if (period == null) return false;
        if (period.getPeriodId() == null || period.getPeriodId().trim().isEmpty()) return false;
        if (period.getPeriodName() == null || period.getPeriodName().trim().isEmpty()) return false;
        if (period.getStartDate() == null || period.getEndDate() == null) return false;
        if (period.getStartDate().isAfter(period.getEndDate())) return false;
        return true;
    }
    
    /**
     * 验证评教指标数据
     */
    public boolean validateEvaluationCriteriaData(EvaluationCriteria criteria) {
        if (criteria == null) return false;
        if (criteria.getCriteriaId() == null || criteria.getCriteriaId().trim().isEmpty()) return false;
        if (criteria.getCriteriaName() == null || criteria.getCriteriaName().trim().isEmpty()) return false;
        if (criteria.getWeight() <= 0 || criteria.getWeight() > 100) return false;
        if (criteria.getMaxScore() <= 0) return false;
        return true;
    }
}
