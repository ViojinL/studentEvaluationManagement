package edu.ai.haut.service;

import edu.ai.haut.model.ClassRoom;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 班级服务类
 * 处理班级相关的业务逻辑
 */
public class ClassService {
    
    /**
     * 创建班级
     */
    public boolean createClass(ClassRoom classRoom) {
        // 数据验证
        if (!validateClassData(classRoom)) {
            return false;
        }
        
        // 检查班级编号是否已存在
        if (isClassIdExists(classRoom.getClassId())) {
            return false;
        }
        
        try {
            String sql = """
                INSERT INTO classes (class_id, class_name, grade, major, college, student_count) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, classRoom.getClassId());
                pstmt.setString(2, classRoom.getClassName());
                pstmt.setString(3, classRoom.getGrade());
                pstmt.setString(4, classRoom.getMajor());
                pstmt.setString(5, classRoom.getCollege());
                pstmt.setInt(6, classRoom.getStudentCount());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("创建班级时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证班级数据
     */
    private boolean validateClassData(ClassRoom classRoom) {
        if (classRoom == null) return false;
        
        return ValidationUtil.isNotEmpty(classRoom.getClassId()) &&
               ValidationUtil.isValidClassName(classRoom.getClassName()) &&
               ValidationUtil.isValidGrade(classRoom.getGrade()) &&
               ValidationUtil.isNotEmpty(classRoom.getMajor()) &&
               ValidationUtil.isNotEmpty(classRoom.getCollege());
    }
    
    /**
     * 检查班级编号是否已存在
     */
    private boolean isClassIdExists(String classId) {
        try {
            String sql = "SELECT COUNT(*) FROM classes WHERE class_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, classId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("检查班级编号时数据库错误: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 根据班级编号获取班级信息
     */
    public ClassRoom getClassById(String classId) {
        try {
            String sql = "SELECT * FROM classes WHERE class_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, classId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    return classRoom;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取班级信息时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 根据班级名称获取班级信息
     */
    public ClassRoom getClassByName(String className) {
        try {
            String sql = "SELECT * FROM classes WHERE class_name = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, className);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    return classRoom;
                }
            }
        } catch (SQLException e) {
            System.err.println("根据班级名称获取班级信息时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取所有班级列表
     */
    public List<ClassRoom> getAllClasses() {
        List<ClassRoom> classes = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM classes ORDER BY class_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    classes.add(classRoom);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取班级列表时数据库错误: " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * 根据年级获取班级列表
     */
    public List<ClassRoom> getClassesByGrade(String grade) {
        List<ClassRoom> classes = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM classes WHERE grade = ? ORDER BY class_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, grade);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    classes.add(classRoom);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取年级班级列表时数据库错误: " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * 根据专业获取班级列表
     */
    public List<ClassRoom> getClassesByMajor(String major) {
        List<ClassRoom> classes = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM classes WHERE major = ? ORDER BY class_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, major);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    classes.add(classRoom);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取专业班级列表时数据库错误: " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * 更新班级信息
     */
    public boolean updateClass(ClassRoom classRoom) {
        if (!validateClassData(classRoom)) {
            return false;
        }
        
        try {
            String sql = """
                UPDATE classes SET class_name = ?, grade = ?, major = ?, college = ? 
                WHERE class_id = ?
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, classRoom.getClassName());
                pstmt.setString(2, classRoom.getGrade());
                pstmt.setString(3, classRoom.getMajor());
                pstmt.setString(4, classRoom.getCollege());
                pstmt.setString(5, classRoom.getClassId());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("更新班级信息时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除班级
     */
    public boolean deleteClass(String classId) {
        try {
            // 检查班级是否有学生
            ClassRoom classRoom = getClassById(classId);
            if (classRoom != null && classRoom.getStudentCount() > 0) {
                return false; // 班级有学生，不能删除
            }
            
            String sql = "DELETE FROM classes WHERE class_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, classId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("删除班级时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取所有专业列表
     */
    public List<String> getAllMajors() {
        List<String> majors = new ArrayList<>();
        
        try {
            String sql = "SELECT DISTINCT major FROM classes ORDER BY major";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    majors.add(rs.getString("major"));
                }
            }
        } catch (SQLException e) {
            System.err.println("获取专业列表时数据库错误: " + e.getMessage());
        }
        
        return majors;
    }
    
    /**
     * 获取所有年级列表
     */
    public List<String> getAllGrades() {
        List<String> grades = new ArrayList<>();
        
        try {
            String sql = "SELECT DISTINCT grade FROM classes ORDER BY grade DESC";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    grades.add(rs.getString("grade"));
                }
            }
        } catch (SQLException e) {
            System.err.println("获取年级列表时数据库错误: " + e.getMessage());
        }
        
        return grades;
    }
}
