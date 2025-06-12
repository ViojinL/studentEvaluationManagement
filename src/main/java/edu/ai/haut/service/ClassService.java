package edu.ai.haut.service;

import edu.ai.haut.model.ClassRoom;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 班级服务类
 * 处理班级相关的业务逻辑
 */
public class ClassService extends BaseService {
    
    /**
     * 创建班级
     */
    public boolean createClass(ClassRoom classRoom) {
        System.out.println("ClassService.createClass() 开始创建班级...");
        System.out.println("班级信息: " + classRoom.getClassId() + " - " + classRoom.getClassName());

        // 数据验证
        if (!validateClassData(classRoom)) {
            System.out.println("ClassService.createClass() 数据验证失败");
            return false;
        }
        System.out.println("ClassService.createClass() 数据验证通过");

        // 检查班级编号是否已存在
        if (isIdExists("classes", "class_id", classRoom.getClassId())) {
            System.out.println("ClassService.createClass() 班级编号已存在: " + classRoom.getClassId());
            return false;
        }
        System.out.println("ClassService.createClass() 班级编号检查通过");

        String[] columns = {"class_id", "class_name", "grade", "major", "college", "student_count"};
        Object[] values = {classRoom.getClassId(), classRoom.getClassName(), classRoom.getGrade(),
                          classRoom.getMajor(), classRoom.getCollege(), classRoom.getStudentCount()};

        System.out.println("ClassService.createClass() 准备插入数据库...");
        boolean result = insertRecord("classes", columns, values);
        System.out.println("ClassService.createClass() 插入结果: " + result);

        return result;
    }

    /**
     * 验证班级数据
     */
    private boolean validateClassData(ClassRoom classRoom) {
        if (classRoom == null) {
            System.out.println("validateClassData: classRoom is null");
            return false;
        }

        boolean classIdValid = ValidationUtil.isNotEmpty(classRoom.getClassId());
        boolean classNameValid = ValidationUtil.isValidClassName(classRoom.getClassName());
        boolean gradeValid = ValidationUtil.isValidGrade(classRoom.getGrade());
        boolean majorValid = ValidationUtil.isNotEmpty(classRoom.getMajor());
        boolean collegeValid = ValidationUtil.isNotEmpty(classRoom.getCollege());

        System.out.println("validateClassData: classId='" + classRoom.getClassId() + "' valid=" + classIdValid);
        System.out.println("validateClassData: className='" + classRoom.getClassName() + "' valid=" + classNameValid);
        System.out.println("validateClassData: grade='" + classRoom.getGrade() + "' valid=" + gradeValid);
        System.out.println("validateClassData: major='" + classRoom.getMajor() + "' valid=" + majorValid);
        System.out.println("validateClassData: college='" + classRoom.getCollege() + "' valid=" + collegeValid);

        boolean result = classIdValid && classNameValid && gradeValid && majorValid && collegeValid;
        System.out.println("validateClassData: 总体验证结果=" + result);

        return result;
    }
    
    /**
     * 根据班级编号获取班级信息
     */
    public ClassRoom getClassById(String classId) {
        try {
            String sql = "SELECT * FROM classes WHERE class_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, classId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    return classRoom;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取班级信息时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 根据班级名称获取班级信息
     */
    public ClassRoom getClassByName(String className) {
        try {
            String sql = "SELECT * FROM classes WHERE class_name = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, className);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    return classRoom;
                }
            }
        } catch (SQLException e) {
            System.err.println("根据班级名称获取班级信息时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取所有班级列表
     */
    public List<ClassRoom> getAllClasses() {
        List<ClassRoom> classes = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM classes ORDER BY class_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    classes.add(classRoom);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取班级列表时数据库错误: " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * 根据年级获取班级列表
     */
    public List<ClassRoom> getClassesByGrade(String grade) {
        List<ClassRoom> classes = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM classes WHERE grade = ? ORDER BY class_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, grade);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    classes.add(classRoom);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取年级班级列表时数据库错误: " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * 根据专业获取班级列表
     */
    public List<ClassRoom> getClassesByMajor(String major) {
        List<ClassRoom> classes = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM classes WHERE major = ? ORDER BY class_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, major);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    classRoom.setCollege(rs.getString("college"));
                    classRoom.setStudentCount(rs.getInt("student_count"));
                    classes.add(classRoom);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取专业班级列表时数据库错误: " + e.getMessage());
        }
        
        return classes;
    }
    


    /**
     * 删除班级
     */
    public boolean deleteClass(String classId) {
        // 检查班级是否有学生
        ClassRoom classRoom = getClassById(classId);
        if (classRoom != null && classRoom.getStudentCount() > 0) {
            return false; // 班级有学生，不能删除
        }

        return deleteRecord("classes", "class_id", classId);
    }
    
    /**
     * 获取所有专业列表
     */
    public List<String> getAllMajors() {
        List<String> majors = new ArrayList<>();
        
        try {
            String sql = "SELECT DISTINCT major FROM classes ORDER BY major";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    majors.add(rs.getString("major"));
                }
            }
        } catch (SQLException e) {
            System.err.println("获取专业列表时数据库错误: " + e.getMessage());
        }
        
        return majors;
    }
    
    /**
     * 获取所有年级列表
     */
    public List<String> getAllGrades() {
        List<String> grades = new ArrayList<>();
        
        try {
            String sql = "SELECT DISTINCT grade FROM classes ORDER BY grade DESC";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    grades.add(rs.getString("grade"));
                }
            }
        } catch (SQLException e) {
            System.err.println("获取年级列表时数据库错误: " + e.getMessage());
        }
        
        return grades;
    }
}
