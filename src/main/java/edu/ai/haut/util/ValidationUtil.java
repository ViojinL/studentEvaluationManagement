package edu.ai.haut.util;

import java.util.regex.Pattern;

/**
 * 数据验证工具类
 * 提供各种数据格式验证方法
 */
public class ValidationUtil {
    
    // 学号格式：231210400111 (年份2位 + 专业代码4位 + 班级代码2位 + 序号4位)
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^\\d{2}\\d{4}\\d{2}\\d{4}$");

    // 教师工号格式：T + 年份(4位) + 序号(4位)
    private static final Pattern TEACHER_ID_PATTERN = Pattern.compile("^T\\d{4}\\d{4}$");

    // 教务人员工号格式：S + 年份(4位) + 序号(4位)
    private static final Pattern STAFF_ID_PATTERN = Pattern.compile("^S\\d{4}\\d{4}$");

    // 管理员ID格式：ADMIN + 序号(3位)
    private static final Pattern ADMIN_ID_PATTERN = Pattern.compile("^ADMIN\\d{3}$");

    // 班级名称格式：软件工程2301 (专业名称 + 年份2位 + 班级序号2位)
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5]+\\d{4}$");

    // 课程编号格式：学院代码(2位) + 课程序号(4位)
    private static final Pattern COURSE_ID_PATTERN = Pattern.compile("^[A-Z]{2}\\d{4}$");
    
    /**
     * 验证字符串是否为空或null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 验证字符串是否非空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 验证学号格式
     */
    public static boolean isValidStudentId(String studentId) {
        return isNotEmpty(studentId) && STUDENT_ID_PATTERN.matcher(studentId).matches();
    }
    
    /**
     * 验证教师工号格式
     */
    public static boolean isValidTeacherId(String teacherId) {
        return isNotEmpty(teacherId) && TEACHER_ID_PATTERN.matcher(teacherId).matches();
    }
    
    /**
     * 验证教务人员工号格式
     */
    public static boolean isValidStaffId(String staffId) {
        return isNotEmpty(staffId) && STAFF_ID_PATTERN.matcher(staffId).matches();
    }
    
    /**
     * 验证管理员ID格式
     */
    public static boolean isValidAdminId(String adminId) {
        return isNotEmpty(adminId) && ADMIN_ID_PATTERN.matcher(adminId).matches();
    }
    
    /**
     * 验证班级名称格式（如：软件工程2301）
     */
    public static boolean isValidClassName(String className) {
        return isNotEmpty(className) && CLASS_NAME_PATTERN.matcher(className).matches();
    }

    /**
     * 验证课程编号格式
     */
    public static boolean isValidCourseId(String courseId) {
        return isNotEmpty(courseId) && COURSE_ID_PATTERN.matcher(courseId).matches();
    }


    
    /**
     * 验证密码强度（至少6位）
     */
    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && password.length() >= 6;
    }
    
    /**
     * 验证姓名格式（2-20个字符，中文或英文）
     */
    public static boolean isValidName(String name) {
        return isNotEmpty(name) && name.length() >= 2 && name.length() <= 20;
    }
    
    /**
     * 验证性别
     */
    public static boolean isValidGender(String gender) {
        return "男".equals(gender) || "女".equals(gender);
    }
    
    /**
     * 验证年级格式（2位数字）
     */
    public static boolean isValidGrade(String grade) {
        return isNotEmpty(grade) && grade.matches("^\\d{2}$");
    }
    
    /**
     * 验证学分（0.5-10.0之间的数值）
     */
    public static boolean isValidCredits(double credits) {
        return credits >= 0.5 && credits <= 10.0;
    }
    
    /**
     * 验证课程类型
     */
    public static boolean isValidCourseType(String courseType) {
        return "必修".equals(courseType) || "选修".equals(courseType) || "实践".equals(courseType);
    }
    
    /**
     * 验证评分（0-100之间的整数）
     */
    public static boolean isValidScore(int score) {
        return score >= 0 && score <= 100;
    }
    
    /**
     * 验证权重（0-100之间的数值）
     */
    public static boolean isValidWeight(double weight) {
        return weight >= 0 && weight <= 100;
    }
    
    /**
     * 验证评教状态
     */
    public static boolean isValidEvaluationStatus(String status) {
        return "未开始".equals(status) || "进行中".equals(status) || 
               "已完成".equals(status) || "已关闭".equals(status);
    }
    

}
