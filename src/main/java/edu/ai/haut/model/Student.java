package edu.ai.haut.model;

/**
 * 学生实体类
 * 对应数据库中的students表
 */
public class Student extends User {
    
    private String grade;      // 年级
    private String major;      // 专业
    private String classId;    // 班级ID
    private String contact;    // 联系方式
    
    /**
     * 默认构造函数
     */
    public Student() {
        super();
    }
    
    /**
     * 带参数的构造函数
     */
    public Student(String studentId, String name, String gender, String grade, 
                   String major, String classId, String contact, String password) {
        super(studentId, name, gender, password);
        this.grade = grade;
        this.major = major;
        this.classId = classId;
        this.contact = contact;
    }
    
    // Getter和Setter方法
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    public String getClassId() {
        return classId;
    }
    
    public void setClassId(String classId) {
        this.classId = classId;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    /**
     * 获取学号（重写getId方法以提供更明确的语义）
     */
    public String getStudentId() {
        return getId();
    }
    
    /**
     * 设置学号
     */
    public void setStudentId(String studentId) {
        setId(studentId);
    }
    
    @Override
    public String getUserType() {
        return "学生";
    }
    
    /**
     * 获取完整的学生信息字符串
     */
    public String getFullInfo() {
        return String.format("学号: %s, 姓名: %s, 性别: %s, 年级: %s, 专业: %s, 班级: %s, 联系方式: %s",
                           id, name, gender, grade, major, classId, contact);
    }
    
    @Override
    public String toString() {
        return String.format("Student{studentId='%s', name='%s', gender='%s', grade='%s', major='%s', classId='%s', contact='%s'}", 
                           id, name, gender, grade, major, classId, contact);
    }
}
