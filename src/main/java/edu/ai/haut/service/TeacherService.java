package edu.ai.haut.service;

import edu.ai.haut.model.Teacher;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师服务类
 * 处理教师相关的业务逻辑
 */
public class TeacherService {
    
    /**
     * 注册教师
     */
    public boolean registerTeacher(Teacher teacher) {
        // 数据验证
        if (!validateTeacherData(teacher)) {
            return false;
        }
        
        // 检查工号是否已存在
        if (isTeacherIdExists(teacher.getTeacherId())) {
            return false;
        }
        
        try {
            String sql = """
                INSERT INTO teachers (teacher_id, name, gender, title, college, password)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, teacher.getTeacherId());
                pstmt.setString(2, teacher.getName());
                pstmt.setString(3, teacher.getGender());
                pstmt.setString(4, teacher.getTitle());
                pstmt.setString(5, teacher.getCollege());
                pstmt.setString(6, teacher.getPassword());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("注册教师时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证教师数据
     */
    private boolean validateTeacherData(Teacher teacher) {
        if (teacher == null) return false;
        
        return ValidationUtil.isValidTeacherId(teacher.getTeacherId()) &&
               ValidationUtil.isValidName(teacher.getName()) &&
               ValidationUtil.isValidGender(teacher.getGender()) &&
               ValidationUtil.isNotEmpty(teacher.getTitle()) &&
               ValidationUtil.isNotEmpty(teacher.getCollege()) &&
               ValidationUtil.isValidPassword(teacher.getPassword());
    }
    
    /**
     * 检查工号是否已存在
     */
    private boolean isTeacherIdExists(String teacherId) {
        try {
            String sql = "SELECT COUNT(*) FROM teachers WHERE teacher_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, teacherId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("检查工号时数据库错误: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 根据工号获取教师信息
     */
    public Teacher getTeacherById(String teacherId) {
        try {
            String sql = "SELECT * FROM teachers WHERE teacher_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, teacherId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(rs.getString("teacher_id"));
                    teacher.setName(rs.getString("name"));
                    teacher.setGender(rs.getString("gender"));
                    teacher.setTitle(rs.getString("title"));
                    teacher.setCollege(rs.getString("college"));
                    teacher.setPassword(rs.getString("password"));
                    return teacher;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取教师信息时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取所有教师列表
     */
    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM teachers ORDER BY teacher_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(rs.getString("teacher_id"));
                    teacher.setName(rs.getString("name"));
                    teacher.setGender(rs.getString("gender"));
                    teacher.setTitle(rs.getString("title"));
                    teacher.setCollege(rs.getString("college"));
                    teacher.setPassword(rs.getString("password"));
                    teachers.add(teacher);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取教师列表时数据库错误: " + e.getMessage());
        }
        
        return teachers;
    }
    
    /**
     * 根据学院获取教师列表
     */
    public List<Teacher> getTeachersByCollege(String college) {
        List<Teacher> teachers = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM teachers WHERE college = ? ORDER BY teacher_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, college);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(rs.getString("teacher_id"));
                    teacher.setName(rs.getString("name"));
                    teacher.setGender(rs.getString("gender"));
                    teacher.setTitle(rs.getString("title"));
                    teacher.setCollege(rs.getString("college"));
                    teacher.setPassword(rs.getString("password"));
                    teachers.add(teacher);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取学院教师列表时数据库错误: " + e.getMessage());
        }
        
        return teachers;
    }
    
    /**
     * 更新教师信息
     */
    public boolean updateTeacher(Teacher teacher) {
        if (!validateTeacherData(teacher)) {
            return false;
        }
        
        try {
            String sql = """
                UPDATE teachers SET name = ?, gender = ?, title = ?, college = ?
                WHERE teacher_id = ?
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, teacher.getName());
                pstmt.setString(2, teacher.getGender());
                pstmt.setString(3, teacher.getTitle());
                pstmt.setString(4, teacher.getCollege());
                pstmt.setString(5, teacher.getTeacherId());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("更新教师信息时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除教师
     */
    public boolean deleteTeacher(String teacherId) {
        try {
            String sql = "DELETE FROM teachers WHERE teacher_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, teacherId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("删除教师时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取所有学院列表
     */
    public List<String> getAllColleges() {
        List<String> colleges = new ArrayList<>();
        
        try {
            String sql = "SELECT DISTINCT college FROM teachers ORDER BY college";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    colleges.add(rs.getString("college"));
                }
            }
        } catch (SQLException e) {
            System.err.println("获取学院列表时数据库错误: " + e.getMessage());
        }
        
        return colleges;
    }
    
    /**
     * 获取所有职称列表
     */
    public List<String> getAllTitles() {
        List<String> titles = new ArrayList<>();
        
        try {
            String sql = "SELECT DISTINCT title FROM teachers ORDER BY title";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    String title = rs.getString("title");
                    if (title != null && !title.trim().isEmpty()) {
                        titles.add(title);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("获取职称列表时数据库错误: " + e.getMessage());
        }
        
        return titles;
    }
}
