package edu.ai.haut.model;

/**
 * 教师实体类
 * 对应数据库中的teachers表
 */
public class Teacher extends User {
    
    private String title;      // 职称
    private String college;    // 所属学院
    
    /**
     * 默认构造函数
     */
    public Teacher() {
        super();
    }
    
    /**
     * 带参数的构造函数
     */
    public Teacher(String teacherId, String name, String gender, String title,
                   String college, String password) {
        super(teacherId, name, gender, password);
        this.title = title;
        this.college = college;
    }
    
    // Getter和Setter方法
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCollege() {
        return college;
    }
    
    public void setCollege(String college) {
        this.college = college;
    }
    

    
    /**
     * 获取工号（重写getId方法以提供更明确的语义）
     */
    public String getTeacherId() {
        return getId();
    }
    
    /**
     * 设置工号
     */
    public void setTeacherId(String teacherId) {
        setId(teacherId);
    }
    
    @Override
    public String getUserType() {
        return "教师";
    }
    
    /**
     * 获取完整的教师信息字符串
     */
    public String getFullInfo() {
        return String.format("工号: %s, 姓名: %s, 性别: %s, 职称: %s, 学院: %s",
                           id, name, gender, title, college);
    }

    @Override
    public String toString() {
        return String.format("Teacher{teacherId='%s', name='%s', gender='%s', title='%s', college='%s'}",
                           id, name, gender, title, college);
    }
}
