package edu.ai.haut.model;

/**
 * 用户基类
 * 所有用户类型的公共属性和方法
 */
public abstract class User {

    protected String id;           // 用户ID（学号/工号/管理员ID）
    protected String name;         // 姓名
    protected String gender;       // 性别
    protected String password;     // 密码
    
    /**
     * 默认构造函数
     */
    public User() {
    }

    /**
     * 带参数的构造函数
     */
    public User(String id, String name, String gender, String password) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.password = password;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * 抽象方法：获取用户类型
     */
    public abstract String getUserType();
    
    /**
     * 验证密码
     */
    public boolean validatePassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }
    
    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', gender='%s'}", 
                           getUserType(), id, name, gender);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
