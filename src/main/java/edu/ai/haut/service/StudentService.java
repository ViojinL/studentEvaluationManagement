package edu.ai.haut.service;

import edu.ai.haut.model.Student;
import edu.ai.haut.model.ClassRoom;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生服务类
 * 处理学生相关的业务逻辑
 */
public class StudentService {
    
    /**
     * 注册学生
     */
    public boolean registerStudent(Student student) {
        // 数据验证
        if (!validateStudentData(student)) {
            System.err.println("学生数据验证失败");
            if (!ValidationUtil.isValidStudentId(student.getStudentId())) {
                System.err.println("学号格式错误，应为12位数字，如：231210400111");
            }
            if (!ValidationUtil.isValidName(student.getName())) {
                System.err.println("姓名格式错误，应为2-20个字符");
            }
            if (!ValidationUtil.isValidGender(student.getGender())) {
                System.err.println("性别格式错误，应为'男'或'女'");
            }
            if (!ValidationUtil.isValidGrade(student.getGrade())) {
                System.err.println("年级格式错误，应为2位数字，如：23");
            }
            if (!ValidationUtil.isNotEmpty(student.getMajor())) {
                System.err.println("专业不能为空");
            }
            if (!ValidationUtil.isNotEmpty(student.getClassId())) {
                System.err.println("班级不能为空");
            }
            if (!ValidationUtil.isValidPassword(student.getPassword())) {
                System.err.println("密码格式错误，至少6位字符");
            }
            return false;
        }

        // 检查学号是否已存在
        if (isStudentIdExists(student.getStudentId())) {
            System.err.println("学号已存在: " + student.getStudentId());
            return false;
        }
        
        // 检查班级是否存在
        if (!isClassExists(student.getClassId())) {
            return false;
        }
        
        try {
            String sql = """
                INSERT INTO students (student_id, name, gender, grade, major, class_id, contact, password) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, student.getStudentId());
                pstmt.setString(2, student.getName());
                pstmt.setString(3, student.getGender());
                pstmt.setString(4, student.getGrade());
                pstmt.setString(5, student.getMajor());
                pstmt.setString(6, student.getClassId());
                pstmt.setString(7, student.getContact());
                pstmt.setString(8, student.getPassword());
                
                int result = pstmt.executeUpdate();
                
                // 如果注册成功，更新班级学生人数
                if (result > 0) {
                    updateClassStudentCount(student.getClassId(), 1);
                }
                
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("注册学生时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证学生数据
     */
    private boolean validateStudentData(Student student) {
        if (student == null) return false;
        
        return ValidationUtil.isValidStudentId(student.getStudentId()) &&
               ValidationUtil.isValidName(student.getName()) &&
               ValidationUtil.isValidGender(student.getGender()) &&
               ValidationUtil.isValidGrade(student.getGrade()) &&
               ValidationUtil.isNotEmpty(student.getMajor()) &&
               ValidationUtil.isNotEmpty(student.getClassId()) &&
               ValidationUtil.isValidPassword(student.getPassword());
    }
    
    /**
     * 检查学号是否已存在
     */
    private boolean isStudentIdExists(String studentId) {
        try {
            String sql = "SELECT COUNT(*) FROM students WHERE student_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, studentId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("检查学号时数据库错误: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 检查班级是否存在
     */
    private boolean isClassExists(String classId) {
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
            System.err.println("检查班级时数据库错误: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 更新班级学生人数
     */
    private void updateClassStudentCount(String classId, int increment) {
        try {
            String sql = "UPDATE classes SET student_count = student_count + ? WHERE class_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, increment);
                pstmt.setString(2, classId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("更新班级学生人数时数据库错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据学号获取学生信息
     */
    public Student getStudentById(String studentId) {
        try {
            String sql = "SELECT * FROM students WHERE student_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, studentId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getString("student_id"));
                    student.setName(rs.getString("name"));
                    student.setGender(rs.getString("gender"));
                    student.setGrade(rs.getString("grade"));
                    student.setMajor(rs.getString("major"));
                    student.setClassId(rs.getString("class_id"));
                    student.setContact(rs.getString("contact"));
                    student.setPassword(rs.getString("password"));
                    return student;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取学生信息时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取所有学生列表
     */
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM students ORDER BY student_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getString("student_id"));
                    student.setName(rs.getString("name"));
                    student.setGender(rs.getString("gender"));
                    student.setGrade(rs.getString("grade"));
                    student.setMajor(rs.getString("major"));
                    student.setClassId(rs.getString("class_id"));
                    student.setContact(rs.getString("contact"));
                    student.setPassword(rs.getString("password"));
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取学生列表时数据库错误: " + e.getMessage());
        }
        
        return students;
    }
    
    /**
     * 根据班级获取学生列表
     */
    public List<Student> getStudentsByClass(String classId) {
        List<Student> students = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM students WHERE class_id = ? ORDER BY student_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, classId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getString("student_id"));
                    student.setName(rs.getString("name"));
                    student.setGender(rs.getString("gender"));
                    student.setGrade(rs.getString("grade"));
                    student.setMajor(rs.getString("major"));
                    student.setClassId(rs.getString("class_id"));
                    student.setContact(rs.getString("contact"));
                    student.setPassword(rs.getString("password"));
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取班级学生列表时数据库错误: " + e.getMessage());
        }
        
        return students;
    }
    
    /**
     * 更新学生信息
     */
    public boolean updateStudent(Student student) {
        if (!validateStudentData(student)) {
            return false;
        }
        
        try {
            String sql = """
                UPDATE students SET name = ?, gender = ?, grade = ?, major = ?, 
                class_id = ?, contact = ? WHERE student_id = ?
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, student.getName());
                pstmt.setString(2, student.getGender());
                pstmt.setString(3, student.getGrade());
                pstmt.setString(4, student.getMajor());
                pstmt.setString(5, student.getClassId());
                pstmt.setString(6, student.getContact());
                pstmt.setString(7, student.getStudentId());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("更新学生信息时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除学生
     */
    public boolean deleteStudent(String studentId) {
        try {
            // 先获取学生信息以便更新班级人数
            Student student = getStudentById(studentId);
            if (student == null) {
                return false;
            }
            
            String sql = "DELETE FROM students WHERE student_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, studentId);
                int result = pstmt.executeUpdate();
                
                // 如果删除成功，更新班级学生人数
                if (result > 0) {
                    updateClassStudentCount(student.getClassId(), -1);
                }
                
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("删除学生时数据库错误: " + e.getMessage());
            return false;
        }
    }
}
