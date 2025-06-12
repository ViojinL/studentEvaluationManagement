package edu.ai.haut.model;

/**
 * 学生实体类
 * 对应数据库中的students表
 */
public class Student extends User {
    
    private String grade;      // 年级
    private String major;      // 专业
    private String classId;    // 班级ID
    
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
                   String major, String classId, String password) {
        super(studentId, name, gender, password);
        this.grade = grade;
        this.major = major;
        this.classId = classId;
    }

    /**
     * Builder模式构建器
     */
    public static class Builder {
        private String studentId;
        private String name;
        private String gender;
        private String password;
        private String grade;
        private String major;
        private String classId;

        public Builder studentId(String studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder grade(String grade) {
            this.grade = grade;
            return this;
        }

        public Builder major(String major) {
            this.major = major;
            return this;
        }

        public Builder classId(String classId) {
            this.classId = classId;
            return this;
        }

        public Student build() {
            return new Student(studentId, name, gender, password, grade, major, classId);
        }
    }

    public static Builder builder() {
        return new Builder();
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
        return String.format("学号: %s, 姓名: %s, 性别: %s, 年级: %s, 专业: %s, 班级: %s",
                           id, name, gender, grade, major, classId);
    }
    
    @Override
    public String toString() {
        return String.format("Student{studentId='%s', name='%s', gender='%s', grade='%s', major='%s', classId='%s'}",
                           id, name, gender, grade, major, classId);
    }
}
