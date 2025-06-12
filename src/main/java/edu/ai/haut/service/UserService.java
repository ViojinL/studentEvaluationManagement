package edu.ai.haut.service;

import edu.ai.haut.model.*;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务类
 * 处理用户登录、注册等通用功能
 */
public class UserService {
    
    /**
     * 用户登录
     * @param userId 用户ID
     * @param password 密码
     * @param userType 用户类型
     * @return 登录成功返回用户对象，失败返回null
     */
    public User login(String userId, String password, String userType) {
        if (ValidationUtil.isEmpty(userId) || ValidationUtil.isEmpty(password)) {
            return null;
        }
        
        try {
            switch (userType) {
                case "学生":
                    return loginStudent(userId, password);
                case "教师":
                    return loginTeacher(userId, password);
                case "管理员":
                    return loginAdministrator(userId, password);
                case "教务人员":
                    return loginAcademicAffairsStaff(userId, password);
                default:
                    return null;
            }
        } catch (SQLException e) {
            System.err.println("登录时数据库错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 学生登录
     */
    private Student loginStudent(String studentId, String password) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            pstmt.setString(2, password);
            
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
        return null;
    }
    
    /**
     * 教师登录
     */
    private Teacher loginTeacher(String teacherId, String password) throws SQLException {
        String sql = "SELECT * FROM teachers WHERE teacher_id = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, teacherId);
            pstmt.setString(2, password);
            
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
        return null;
    }
    
    /**
     * 管理员登录
     */
    private Administrator loginAdministrator(String adminId, String password) throws SQLException {
        String sql = "SELECT * FROM administrators WHERE admin_id = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, adminId);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Administrator admin = new Administrator();
                admin.setAdminId(rs.getString("admin_id"));
                admin.setName(rs.getString("name"));
                admin.setGender(rs.getString("gender"));
                admin.setPassword(rs.getString("password"));
                return admin;
            }
        }
        return null;
    }
    
    /**
     * 教务人员登录
     */
    private AcademicAffairsStaff loginAcademicAffairsStaff(String staffId, String password) throws SQLException {
        String sql = "SELECT * FROM academic_affairs_staff WHERE staff_id = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, staffId);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                AcademicAffairsStaff staff = new AcademicAffairsStaff();
                staff.setStaffId(rs.getString("staff_id"));
                staff.setName(rs.getString("name"));
                staff.setGender(rs.getString("gender"));
                staff.setDepartment(rs.getString("department"));
                staff.setPosition(rs.getString("position"));
                staff.setPassword(rs.getString("password"));
                return staff;
            }
        }
        return null;
    }
    
    /**
     * 检查用户ID是否已存在
     */
    public boolean isUserIdExists(String userId, String userType) {
        String tableName = getTableNameByUserType(userType);
        String columnName = getIdColumnNameByUserType(userType);

        if (tableName == null || columnName == null) {
            return false;
        }

        return DatabaseUtil.recordExists(tableName, columnName, userId);
    }
    
    /**
     * 根据用户类型获取表名
     */
    private String getTableNameByUserType(String userType) {
        return switch (userType) {
            case "学生" -> "students";
            case "教师" -> "teachers";
            case "管理员" -> "administrators";
            case "教务人员" -> "academic_affairs_staff";
            default -> null;
        };
    }
    
    /**
     * 根据用户类型获取ID列名
     */
    private String getIdColumnNameByUserType(String userType) {
        return switch (userType) {
            case "学生" -> "student_id";
            case "教师" -> "teacher_id";
            case "管理员" -> "admin_id";
            case "教务人员" -> "staff_id";
            default -> null;
        };
    }
    

}
