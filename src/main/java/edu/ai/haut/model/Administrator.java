package edu.ai.haut.model;

/**
 * 管理员实体类
 * 对应数据库中的administrators表
 */
public class Administrator extends User {
    
    /**
     * 默认构造函数
     */
    public Administrator() {
        super();
    }
    
    /**
     * 带参数的构造函数
     */
    public Administrator(String adminId, String name, String gender, String password) {
        super(adminId, name, gender, password);
    }
    
    /**
     * 获取管理员ID（重写getId方法以提供更明确的语义）
     */
    public String getAdminId() {
        return getId();
    }
    
    /**
     * 设置管理员ID
     */
    public void setAdminId(String adminId) {
        setId(adminId);
    }
    
    @Override
    public String getUserType() {
        return "管理员";
    }
    
    /**
     * 获取完整的管理员信息字符串
     */
    public String getFullInfo() {
        return String.format("管理员ID: %s, 姓名: %s, 性别: %s",
                           id, name, gender);
    }
    
    @Override
    public String toString() {
        return String.format("Administrator{adminId='%s', name='%s', gender='%s'}", 
                           id, name, gender);
    }
}
