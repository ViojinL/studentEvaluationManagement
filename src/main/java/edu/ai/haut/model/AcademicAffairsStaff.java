package edu.ai.haut.model;

/**
 * 教务人员实体类
 * 对应数据库中的academic_affairs_staff表
 */
public class AcademicAffairsStaff extends User {
    
    private String department; // 部门
    private String position;   // 职位
    private String contact;    // 联系方式
    
    /**
     * 默认构造函数
     */
    public AcademicAffairsStaff() {
        super();
    }
    
    /**
     * 带参数的构造函数
     */
    public AcademicAffairsStaff(String staffId, String name, String gender, String department,
                               String position, String contact, String password) {
        super(staffId, name, gender, password);
        this.department = department;
        this.position = position;
        this.contact = contact;
    }
    
    // Getter和Setter方法
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    

    
    /**
     * 获取工号（重写getId方法以提供更明确的语义）
     */
    public String getStaffId() {
        return getId();
    }
    
    /**
     * 设置工号
     */
    public void setStaffId(String staffId) {
        setId(staffId);
    }
    
    @Override
    public String getUserType() {
        return "教务人员";
    }
    
    /**
     * 获取完整的教务人员信息字符串
     */
    public String getFullInfo() {
        return String.format("工号: %s, 姓名: %s, 性别: %s, 部门: %s, 职位: %s, 联系方式: %s",
                           id, name, gender, department, position, contact);
    }

    @Override
    public String toString() {
        return String.format("AcademicAffairsStaff{staffId='%s', name='%s', gender='%s', department='%s', position='%s', contact='%s'}",
                           id, name, gender, department, position, contact);
    }
}
