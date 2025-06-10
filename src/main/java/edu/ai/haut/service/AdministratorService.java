package edu.ai.haut.service;

import edu.ai.haut.model.Administrator;
import edu.ai.haut.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员服务类
 */
public class AdministratorService {
    
    /**
     * 获取所有管理员
     */
    public List<Administrator> getAllAdministrators() throws SQLException {
        List<Administrator> administrators = new ArrayList<>();
        String sql = "SELECT * FROM administrators ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Administrator admin = new Administrator();
                admin.setAdminId(rs.getString("admin_id"));
                admin.setName(rs.getString("name"));
                admin.setGender(rs.getString("gender"));
                admin.setPassword(rs.getString("password"));
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    admin.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                administrators.add(admin);
            }
        }
        
        return administrators;
    }
    
    /**
     * 根据ID获取管理员
     */
    public Administrator getAdministratorById(String adminId) throws SQLException {
        String sql = "SELECT * FROM administrators WHERE admin_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, adminId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Administrator admin = new Administrator();
                    admin.setAdminId(rs.getString("admin_id"));
                    admin.setName(rs.getString("name"));
                    admin.setGender(rs.getString("gender"));
                    admin.setPassword(rs.getString("password"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        admin.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    
                    return admin;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 创建管理员
     */
    public boolean createAdministrator(Administrator administrator) throws SQLException {
        String sql = "INSERT INTO administrators (admin_id, name, gender, password) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, administrator.getAdminId());
            pstmt.setString(2, administrator.getName());
            pstmt.setString(3, administrator.getGender());
            pstmt.setString(4, administrator.getPassword());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 更新管理员信息
     */
    public boolean updateAdministrator(Administrator administrator) throws SQLException {
        String sql = "UPDATE administrators SET name = ?, gender = ? WHERE admin_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, administrator.getName());
            pstmt.setString(2, administrator.getGender());
            pstmt.setString(3, administrator.getAdminId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 删除管理员
     */
    public boolean deleteAdministrator(String adminId) throws SQLException {
        // 不允许删除默认管理员
        if ("ADMIN001".equals(adminId)) {
            return false;
        }
        
        String sql = "DELETE FROM administrators WHERE admin_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, adminId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 验证管理员登录
     */
    public Administrator validateLogin(String adminId, String password) throws SQLException {
        String sql = "SELECT * FROM administrators WHERE admin_id = ? AND password = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, adminId);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Administrator admin = new Administrator();
                    admin.setAdminId(rs.getString("admin_id"));
                    admin.setName(rs.getString("name"));
                    admin.setGender(rs.getString("gender"));
                    admin.setPassword(rs.getString("password"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        admin.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    
                    return admin;
                }
            }
        }
        
        return null;
    }
}
