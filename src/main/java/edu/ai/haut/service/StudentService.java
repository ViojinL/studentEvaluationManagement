package edu.ai.haut.service;

import edu.ai.haut.model.Student;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生服务类
 * 处理学生相关的业务逻辑
 */
public class StudentService extends BaseService {
    
    /**
     * 注册学生
     */
    public boolean registerStudent(Student student) {
        // 数据验证
        if (!validateStudentData(student)) {
            return false;
        }

        // 检查学号是否已存在
        if (isIdExists("students", "student_id", student.getStudentId())) {
            System.err.println("学号已存在: " + student.getStudentId());
            return false;
        }

        // 检查班级是否存在
        if (!isIdExists("classes", "class_id", student.getClassId())) {
            System.err.println("班级不存在: " + student.getClassId());
            return false;
        }

        // 使用通用插入方法
        String[] columns = {"student_id", "name", "gender", "grade", "major", "class_id", "password"};
        Object[] values = {student.getStudentId(), student.getName(), student.getGender(),
                          student.getGrade(), student.getMajor(), student.getClassId(), student.getPassword()};

        boolean result = insertRecord("students", columns, values);

        // 如果注册成功，更新班级学生人数
        if (result) {
            updateClassStudentCount(student.getClassId(), 1);
        }

        return result;
    }
    
    /**
     * 验证学生数据
     */
    private boolean validateStudentData(Student student) {
        if (student == null) return false;

        return ValidationUtil.isValidStudentId(student.getStudentId()) &&
               validateBasicData(student.getStudentId(), student.getName(),
                               student.getGender(), student.getPassword()) &&
               ValidationUtil.isValidGrade(student.getGrade()) &&
               ValidationUtil.isNotEmpty(student.getMajor()) &&
               ValidationUtil.isNotEmpty(student.getClassId());
    }
    
    /**
     * 更新班级学生人数
     */
    public void updateClassStudentCount(String classId, int increment) {
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
     * 获取班级学生数量
     */
    public int getStudentCountByClass(String classId) {
        try {
            String sql = "SELECT COUNT(*) FROM students WHERE class_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, classId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取班级学生数量时数据库错误: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 更新学生信息
     */
    public boolean updateStudent(Student student) {
        if (!validateStudentData(student)) {
            return false;
        }

        String[] columns = {"name", "gender", "grade", "major", "class_id"};
        Object[] values = {student.getName(), student.getGender(), student.getGrade(),
                          student.getMajor(), student.getClassId()};

        return updateRecord("students", columns, values, "student_id", student.getStudentId());
    }
    
    /**
     * 删除学生
     */
    public boolean deleteStudent(String studentId) {
        // 先获取学生信息以便更新班级人数
        Student student = getStudentById(studentId);
        if (student == null) {
            return false;
        }

        boolean result = deleteRecord("students", "student_id", studentId);

        // 如果删除成功，更新班级学生人数
        if (result) {
            updateClassStudentCount(student.getClassId(), -1);
        }

        return result;
    }
}
