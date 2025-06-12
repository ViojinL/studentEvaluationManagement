package edu.ai.haut.model;

/**
 * 班级实体类
 * 对应数据库中的classes表
 */
public class ClassRoom {
    
    private String classId;        // 班级编号
    private String className;      // 班级名称
    private String grade;          // 年级
    private String major;          // 专业
    private String college;        // 所属学院
    private int studentCount;      // 学生人数
    
    /**
     * 默认构造函数
     */
    public ClassRoom() {
        this.studentCount = 0;
    }
    
    /**
     * 带参数的构造函数
     */
    public ClassRoom(String classId, String className, String grade, 
                     String major, String college) {
        this.classId = classId;
        this.className = className;
        this.grade = grade;
        this.major = major;
        this.college = college;
        this.studentCount = 0;
    }
    
    // Getter和Setter方法
    public String getClassId() {
        return classId;
    }
    
    public void setClassId(String classId) {
        this.classId = classId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
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
    
    public String getCollege() {
        return college;
    }
    
    public void setCollege(String college) {
        this.college = college;
    }
    
    public int getStudentCount() {
        return studentCount;
    }
    
    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
    
    /**
     * 增加学生人数
     */
    public void incrementStudentCount() {
        this.studentCount++;
    }
    
    /**
     * 减少学生人数
     */
    public void decrementStudentCount() {
        if (this.studentCount > 0) {
            this.studentCount--;
        }
    }
    
    /**
     * 获取完整的班级信息字符串
     */
    public String getFullInfo() {
        return String.format("班级编号: %s, 班级名称: %s, 年级: %s, 专业: %s, 学院: %s, 学生人数: %d",
                           classId, className, grade, major, college, studentCount);
    }
    
    @Override
    public String toString() {
        return String.format("ClassRoom{classId='%s', className='%s', grade='%s', major='%s', college='%s', studentCount=%d}", 
                           classId, className, grade, major, college, studentCount);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClassRoom classRoom = (ClassRoom) obj;
        return classId != null && classId.equals(classRoom.classId);
    }
    
    @Override
    public int hashCode() {
        return classId != null ? classId.hashCode() : 0;
    }
}
