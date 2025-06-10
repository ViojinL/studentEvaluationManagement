package edu.ai.haut.service;

import edu.ai.haut.model.AcademicAffairsStaff;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 教务人员服务类
 * 处理教务人员相关的业务逻辑
 */
public class AcademicAffairsStaffService {
    
    /**
     * 注册教务人员
     */
    public boolean registerStaff(AcademicAffairsStaff staff) {
        // 数据验证
        if (!validateStaffData(staff)) {
            return false;
        }
        
        // 检查工号是否已存在
        if (isStaffIdExists(staff.getStaffId())) {
            return false;
        }
        
        try {
            String sql = """
                INSERT INTO academic_affairs_staff (staff_id, name, gender, department, position, password)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, staff.getStaffId());
                pstmt.setString(2, staff.getName());
                pstmt.setString(3, staff.getGender());
                pstmt.setString(4, staff.getDepartment());
                pstmt.setString(5, staff.getPosition());
                pstmt.setString(6, staff.getPassword());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("注册教务人员时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证教务人员数据
     */
    private boolean validateStaffData(AcademicAffairsStaff staff) {
        if (staff == null) return false;
        
        return ValidationUtil.isValidStaffId(staff.getStaffId()) &&
               ValidationUtil.isValidName(staff.getName()) &&
               ValidationUtil.isValidGender(staff.getGender()) &&
               ValidationUtil.isNotEmpty(staff.getDepartment()) &&
               ValidationUtil.isNotEmpty(staff.getPosition()) &&
               ValidationUtil.isValidPassword(staff.getPassword());
    }
    
    /**
     * 检查工号是否已存在
     */
    private boolean isStaffIdExists(String staffId) {
        try {
            String sql = "SELECT COUNT(*) FROM academic_affairs_staff WHERE staff_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, staffId);
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
     * 根据工号获取教务人员信息
     */
    public AcademicAffairsStaff getStaffById(String staffId) {
        try {
            String sql = "SELECT * FROM academic_affairs_staff WHERE staff_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, staffId);
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
        } catch (SQLException e) {
            System.err.println("获取教务人员信息时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取所有教务人员列表
     */
    public List<AcademicAffairsStaff> getAllStaff() {
        List<AcademicAffairsStaff> staffList = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM academic_affairs_staff ORDER BY staff_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    AcademicAffairsStaff staff = new AcademicAffairsStaff();
                    staff.setStaffId(rs.getString("staff_id"));
                    staff.setName(rs.getString("name"));
                    staff.setGender(rs.getString("gender"));
                    staff.setDepartment(rs.getString("department"));
                    staff.setPosition(rs.getString("position"));
                    staff.setPassword(rs.getString("password"));

                    // 设置创建时间
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        staff.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    staffList.add(staff);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取教务人员列表时数据库错误: " + e.getMessage());
        }
        
        return staffList;
    }
    
    /**
     * 根据部门获取教务人员列表
     */
    public List<AcademicAffairsStaff> getStaffByDepartment(String department) {
        List<AcademicAffairsStaff> staffList = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM academic_affairs_staff WHERE department = ? ORDER BY staff_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, department);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    AcademicAffairsStaff staff = new AcademicAffairsStaff();
                    staff.setStaffId(rs.getString("staff_id"));
                    staff.setName(rs.getString("name"));
                    staff.setGender(rs.getString("gender"));
                    staff.setDepartment(rs.getString("department"));
                    staff.setPosition(rs.getString("position"));
                    staff.setPassword(rs.getString("password"));
                    staffList.add(staff);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取部门教务人员列表时数据库错误: " + e.getMessage());
        }
        
        return staffList;
    }
    
    /**
     * 更新教务人员信息
     */
    public boolean updateStaff(AcademicAffairsStaff staff) {
        if (!validateStaffData(staff)) {
            return false;
        }
        
        try {
            String sql = """
                UPDATE academic_affairs_staff SET name = ?, gender = ?, department = ?,
                position = ? WHERE staff_id = ?
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, staff.getName());
                pstmt.setString(2, staff.getGender());
                pstmt.setString(3, staff.getDepartment());
                pstmt.setString(4, staff.getPosition());
                pstmt.setString(5, staff.getStaffId());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("更新教务人员信息时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除教务人员
     */
    public boolean deleteStaff(String staffId) {
        try {
            String sql = "DELETE FROM academic_affairs_staff WHERE staff_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, staffId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("删除教务人员时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取所有部门列表
     */
    public List<String> getAllDepartments() {
        List<String> departments = new ArrayList<>();
        
        try {
            String sql = "SELECT DISTINCT department FROM academic_affairs_staff ORDER BY department";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    departments.add(rs.getString("department"));
                }
            }
        } catch (SQLException e) {
            System.err.println("获取部门列表时数据库错误: " + e.getMessage());
        }
        
        return departments;
    }
    
    /**
     * 获取所有职位列表
     */
    public List<String> getAllPositions() {
        List<String> positions = new ArrayList<>();
        
        try {
            String sql = "SELECT DISTINCT position FROM academic_affairs_staff ORDER BY position";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    String position = rs.getString("position");
                    if (position != null && !position.trim().isEmpty()) {
                        positions.add(position);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("获取职位列表时数据库错误: " + e.getMessage());
        }
        
        return positions;
    }
}
