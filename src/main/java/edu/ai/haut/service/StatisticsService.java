package edu.ai.haut.service;

import edu.ai.haut.util.DatabaseUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计服务类
 * 处理各种统计分析功能
 */
public class StatisticsService {
    
    /**
     * 按学生统计评教参与情况
     */
    public Map<String, Object> getStudentEvaluationStatistics(String periodId) {
        Map<String, Object> statistics = new HashMap<>();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 总学生数
            String totalStudentsSql = "SELECT COUNT(*) as total FROM students";
            try (PreparedStatement pstmt = conn.prepareStatement(totalStudentsSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    statistics.put("totalStudents", rs.getInt("total"));
                }
            }
            
            // 参与评教的学生数
            String participatedStudentsSql = """
                SELECT COUNT(DISTINCT student_id) as participated 
                FROM evaluations 
                WHERE period_id = ?
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(participatedStudentsSql)) {
                pstmt.setString(1, periodId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    statistics.put("participatedStudents", rs.getInt("participated"));
                }
            }
            
            // 计算参与率
            int total = (Integer) statistics.getOrDefault("totalStudents", 0);
            int participated = (Integer) statistics.getOrDefault("participatedStudents", 0);
            double participationRate = total > 0 ? (double) participated / total * 100 : 0;
            statistics.put("participationRate", participationRate);
            
            // 评价分布统计
            String distributionSql = """
                SELECT 
                    COUNT(CASE WHEN total_score >= 90 THEN 1 END) as excellent,
                    COUNT(CASE WHEN total_score >= 80 AND total_score < 90 THEN 1 END) as good,
                    COUNT(CASE WHEN total_score >= 70 AND total_score < 80 THEN 1 END) as average,
                    COUNT(CASE WHEN total_score >= 60 AND total_score < 70 THEN 1 END) as pass,
                    COUNT(CASE WHEN total_score < 60 THEN 1 END) as fail
                FROM evaluations 
                WHERE period_id = ?
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(distributionSql)) {
                pstmt.setString(1, periodId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    Map<String, Integer> distribution = new HashMap<>();
                    distribution.put("优秀", rs.getInt("excellent"));
                    distribution.put("良好", rs.getInt("good"));
                    distribution.put("中等", rs.getInt("average"));
                    distribution.put("及格", rs.getInt("pass"));
                    distribution.put("不及格", rs.getInt("fail"));
                    statistics.put("gradeDistribution", distribution);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取学生评教统计时数据库错误: " + e.getMessage());
        }
        
        return statistics;
    }
    

    
    /**
     * 按教师统计评教结果
     */
    public Map<String, Object> getTeacherEvaluationStatistics(String periodId) {
        Map<String, Object> statistics = new HashMap<>();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = """
                SELECT 
                    t.teacher_id,
                    t.name as teacher_name,
                    t.title,
                    t.college,
                    COUNT(DISTINCT co.offering_id) as course_count,
                    COUNT(e.evaluation_id) as evaluation_count,
                    AVG(e.total_score) as avg_score,
                    MIN(e.total_score) as min_score,
                    MAX(e.total_score) as max_score
                FROM teachers t
                LEFT JOIN course_offerings co ON t.teacher_id = co.teacher_id
                LEFT JOIN evaluations e ON co.offering_id = e.offering_id AND e.period_id = ?
                GROUP BY t.teacher_id, t.name, t.title, t.college
                HAVING COUNT(e.evaluation_id) > 0
                ORDER BY avg_score DESC, evaluation_count DESC
            """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, periodId);
                ResultSet rs = pstmt.executeQuery();
                
                Map<String, Map<String, Object>> teacherStats = new HashMap<>();
                double totalAvgScore = 0;
                int teacherCount = 0;
                
                while (rs.next()) {
                    Map<String, Object> teacherData = new HashMap<>();
                    teacherData.put("teacherName", rs.getString("teacher_name"));
                    teacherData.put("title", rs.getString("title"));
                    teacherData.put("college", rs.getString("college"));
                    teacherData.put("courseCount", rs.getInt("course_count"));
                    teacherData.put("evaluationCount", rs.getInt("evaluation_count"));
                    teacherData.put("avgScore", rs.getDouble("avg_score"));
                    teacherData.put("minScore", rs.getDouble("min_score"));
                    teacherData.put("maxScore", rs.getDouble("max_score"));
                    
                    // 计算等级
                    double avgScore = rs.getDouble("avg_score");
                    String grade;
                    if (avgScore >= 90) grade = "优秀";
                    else if (avgScore >= 80) grade = "良好";
                    else if (avgScore >= 70) grade = "中等";
                    else if (avgScore >= 60) grade = "及格";
                    else grade = "不及格";
                    teacherData.put("grade", grade);
                    
                    teacherStats.put(rs.getString("teacher_id"), teacherData);
                    
                    totalAvgScore += avgScore;
                    teacherCount++;
                }
                
                statistics.put("teacherStatistics", teacherStats);
                statistics.put("overallAvgScore", teacherCount > 0 ? totalAvgScore / teacherCount : 0);
                statistics.put("totalTeachers", teacherCount);
            }
            
        } catch (SQLException e) {
            System.err.println("获取教师评教统计时数据库错误: " + e.getMessage());
        }
        
        return statistics;
    }
    
    /**
     * 按课程统计评教结果
     */
    public Map<String, Object> getCourseEvaluationStatistics(String periodId) {
        Map<String, Object> statistics = new HashMap<>();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = """
                SELECT 
                    c.course_id,
                    c.course_name,
                    c.course_type,
                    c.college,
                    c.credits,
                    COUNT(DISTINCT co.offering_id) as offering_count,
                    COUNT(e.evaluation_id) as evaluation_count,
                    AVG(e.total_score) as avg_score,
                    MIN(e.total_score) as min_score,
                    MAX(e.total_score) as max_score
                FROM courses c
                LEFT JOIN course_offerings co ON c.course_id = co.course_id
                LEFT JOIN evaluations e ON co.offering_id = e.offering_id AND e.period_id = ?
                GROUP BY c.course_id, c.course_name, c.course_type, c.college, c.credits
                HAVING COUNT(e.evaluation_id) > 0
                ORDER BY avg_score DESC, evaluation_count DESC
            """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, periodId);
                ResultSet rs = pstmt.executeQuery();
                
                Map<String, Map<String, Object>> courseStats = new HashMap<>();
                Map<String, Integer> typeDistribution = new HashMap<>();
                double totalAvgScore = 0;
                int courseCount = 0;
                
                while (rs.next()) {
                    Map<String, Object> courseData = new HashMap<>();
                    courseData.put("courseName", rs.getString("course_name"));
                    courseData.put("courseType", rs.getString("course_type"));
                    courseData.put("college", rs.getString("college"));
                    courseData.put("credits", rs.getDouble("credits"));
                    courseData.put("offeringCount", rs.getInt("offering_count"));
                    courseData.put("evaluationCount", rs.getInt("evaluation_count"));
                    courseData.put("avgScore", rs.getDouble("avg_score"));
                    courseData.put("minScore", rs.getDouble("min_score"));
                    courseData.put("maxScore", rs.getDouble("max_score"));
                    
                    courseStats.put(rs.getString("course_id"), courseData);
                    
                    // 统计课程类型分布
                    String courseType = rs.getString("course_type");
                    typeDistribution.put(courseType, typeDistribution.getOrDefault(courseType, 0) + 1);
                    
                    totalAvgScore += rs.getDouble("avg_score");
                    courseCount++;
                }
                
                statistics.put("courseStatistics", courseStats);
                statistics.put("typeDistribution", typeDistribution);
                statistics.put("overallAvgScore", courseCount > 0 ? totalAvgScore / courseCount : 0);
                statistics.put("totalCourses", courseCount);
            }
            
        } catch (SQLException e) {
            System.err.println("获取课程评教统计时数据库错误: " + e.getMessage());
        }
        
        return statistics;
    }
    
    /**
     * 获取系统总体统计信息
     */
    public Map<String, Object> getSystemOverallStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 用户统计
            String[] userTables = {"students", "teachers", "administrators", "academic_affairs_staff"};
            String[] userTypes = {"学生", "教师", "管理员", "教务人员"};
            
            for (int i = 0; i < userTables.length; i++) {
                String sql = "SELECT COUNT(*) as count FROM " + userTables[i];
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        statistics.put(userTypes[i] + "数量", rs.getInt("count"));
                    }
                }
            }
            
            // 课程统计
            String courseSql = "SELECT COUNT(*) as count FROM courses";
            try (PreparedStatement pstmt = conn.prepareStatement(courseSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    statistics.put("课程数量", rs.getInt("count"));
                }
            }
            
            // 班级统计
            String classSql = "SELECT COUNT(*) as count FROM classes";
            try (PreparedStatement pstmt = conn.prepareStatement(classSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    statistics.put("班级数量", rs.getInt("count"));
                }
            }
            
            // 开课统计
            String offeringSql = "SELECT COUNT(*) as count FROM course_offerings";
            try (PreparedStatement pstmt = conn.prepareStatement(offeringSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    statistics.put("开课数量", rs.getInt("count"));
                }
            }
            
            // 评教统计
            String evaluationSql = "SELECT COUNT(*) as count FROM evaluations";
            try (PreparedStatement pstmt = conn.prepareStatement(evaluationSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    statistics.put("评教记录数量", rs.getInt("count"));
                }
            }
            
            // 评教周期统计
            String periodSql = "SELECT COUNT(*) as count FROM evaluation_periods";
            try (PreparedStatement pstmt = conn.prepareStatement(periodSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    statistics.put("评教周期数量", rs.getInt("count"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取系统统计信息时数据库错误: " + e.getMessage());
        }
        
        return statistics;
    }
}
