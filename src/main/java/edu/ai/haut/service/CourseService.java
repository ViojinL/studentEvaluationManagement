package edu.ai.haut.service;

import edu.ai.haut.model.*;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程服务类
 * 处理课程和开课信息相关的业务逻辑
 */
public class CourseService extends BaseService {
    
    /**
     * 创建课程
     */
    public boolean createCourse(Course course) {
        // 数据验证
        if (!validateCourseData(course)) {
            return false;
        }
        
        // 检查课程编号是否已存在
        if (isCourseIdExists(course.getCourseId())) {
            return false;
        }
        
        try {
            String sql = """
                INSERT INTO courses (course_id, course_name, credits, course_type, college) 
                VALUES (?, ?, ?, ?, ?)
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, course.getCourseId());
                pstmt.setString(2, course.getCourseName());
                pstmt.setDouble(3, course.getCredits());
                pstmt.setString(4, course.getCourseType());
                pstmt.setString(5, course.getCollege());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("创建课程时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证课程数据
     */
    private boolean validateCourseData(Course course) {
        if (course == null) return false;
        
        return ValidationUtil.isValidCourseId(course.getCourseId()) &&
               ValidationUtil.isNotEmpty(course.getCourseName()) &&
               ValidationUtil.isValidCredits(course.getCredits()) &&
               ValidationUtil.isValidCourseType(course.getCourseType()) &&
               ValidationUtil.isNotEmpty(course.getCollege());
    }
    
    /**
     * 检查课程编号是否已存在
     */
    private boolean isCourseIdExists(String courseId) {
        return DatabaseUtil.recordExists("courses", "course_id", courseId);
    }
    
    /**
     * 根据课程编号获取课程信息
     */
    public Course getCourseById(String courseId) {
        try {
            String sql = "SELECT * FROM courses WHERE course_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, courseId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    course.setCredits(rs.getDouble("credits"));
                    course.setCourseType(rs.getString("course_type"));
                    course.setCollege(rs.getString("college"));
                    return course;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取课程信息时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取所有课程列表
     */
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM courses ORDER BY course_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    course.setCredits(rs.getDouble("credits"));
                    course.setCourseType(rs.getString("course_type"));
                    course.setCollege(rs.getString("college"));
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取课程列表时数据库错误: " + e.getMessage());
        }
        
        return courses;
    }

    /**
     * 更新课程信息
     */
    public boolean updateCourse(Course course) {
        if (!validateCourseData(course)) {
            return false;
        }

        try {
            String sql = """
                UPDATE courses SET course_name = ?, credits = ?, course_type = ?, college = ?
                WHERE course_id = ?
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, course.getCourseName());
                pstmt.setDouble(2, course.getCredits());
                pstmt.setString(3, course.getCourseType());
                pstmt.setString(4, course.getCollege());
                pstmt.setString(5, course.getCourseId());

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("更新课程信息时数据库错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除课程
     */
    public boolean deleteCourse(String courseId) {
        try {
            // 检查是否有相关的开课信息
            String checkSql = "SELECT COUNT(*) FROM course_offerings WHERE course_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setString(1, courseId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // 有相关开课信息，不能删除
                }
            }

            String sql = "DELETE FROM courses WHERE course_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, courseId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("删除课程时数据库错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 创建开课信息
     */
    public boolean createCourseOffering(CourseOffering offering) {
        // 数据验证
        if (!validateOfferingData(offering)) {
            return false;
        }

        // 检查开课编号是否已存在
        if (isIdExists("course_offerings", "offering_id", offering.getOfferingId())) {
            return false;
        }

        try {
            String sql = """
                INSERT INTO course_offerings (offering_id, course_id, teacher_id, class_id, semester, schedule)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, offering.getOfferingId());
                pstmt.setString(2, offering.getCourseId());
                pstmt.setString(3, offering.getTeacherId());
                pstmt.setString(4, offering.getClassId());
                pstmt.setString(5, offering.getSemester());
                pstmt.setString(6, offering.getSchedule());

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("创建开课信息时数据库错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新开课信息
     */
    public boolean updateCourseOffering(CourseOffering offering) {
        if (!validateOfferingData(offering)) {
            return false;
        }

        try {
            String sql = """
                UPDATE course_offerings SET course_id = ?, teacher_id = ?, class_id = ?,
                semester = ?, schedule = ? WHERE offering_id = ?
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, offering.getCourseId());
                pstmt.setString(2, offering.getTeacherId());
                pstmt.setString(3, offering.getClassId());
                pstmt.setString(4, offering.getSemester());
                pstmt.setString(5, offering.getSchedule());
                pstmt.setString(6, offering.getOfferingId());

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("更新开课信息时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证开课信息数据
     */
    private boolean validateOfferingData(CourseOffering offering) {
        if (offering == null) return false;
        
        return ValidationUtil.isNotEmpty(offering.getOfferingId()) &&
               ValidationUtil.isNotEmpty(offering.getCourseId()) &&
               ValidationUtil.isNotEmpty(offering.getTeacherId()) &&
               ValidationUtil.isNotEmpty(offering.getClassId()) &&
               ValidationUtil.isNotEmpty(offering.getSemester());
    }
    

    
    /**
     * 获取学生的课程列表（根据班级）
     */
    public List<CourseOffering> getCourseOfferingsByClass(String classId) {
        List<CourseOffering> offerings = new ArrayList<>();
        
        try {
            String sql = """
                SELECT co.*, c.course_name, c.credits, c.course_type, c.college,
                       t.name as teacher_name, t.title, cl.class_name
                FROM course_offerings co
                JOIN courses c ON co.course_id = c.course_id
                JOIN teachers t ON co.teacher_id = t.teacher_id
                JOIN classes cl ON co.class_id = cl.class_id
                WHERE co.class_id = ?
                ORDER BY co.offering_id
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, classId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    CourseOffering offering = new CourseOffering();
                    offering.setOfferingId(rs.getString("offering_id"));
                    offering.setCourseId(rs.getString("course_id"));
                    offering.setTeacherId(rs.getString("teacher_id"));
                    offering.setClassId(rs.getString("class_id"));
                    offering.setSemester(rs.getString("semester"));
                    offering.setSchedule(rs.getString("schedule"));
                    
                    // 设置关联的课程信息
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    course.setCredits(rs.getDouble("credits"));
                    course.setCourseType(rs.getString("course_type"));
                    course.setCollege(rs.getString("college"));
                    offering.setCourse(course);
                    
                    // 设置关联的教师信息
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(rs.getString("teacher_id"));
                    teacher.setName(rs.getString("teacher_name"));
                    teacher.setTitle(rs.getString("title"));
                    offering.setTeacher(teacher);
                    
                    // 设置关联的班级信息
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    offering.setClassRoom(classRoom);
                    
                    offerings.add(offering);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取班级课程列表时数据库错误: " + e.getMessage());
        }
        
        return offerings;
    }
    
    /**
     * 获取教师的授课列表
     */
    public List<CourseOffering> getCourseOfferingsByTeacher(String teacherId) {
        List<CourseOffering> offerings = new ArrayList<>();
        
        try {
            String sql = """
                SELECT co.*, c.course_name, c.credits, c.course_type, c.college,
                       cl.class_name, cl.grade, cl.major
                FROM course_offerings co
                JOIN courses c ON co.course_id = c.course_id
                JOIN classes cl ON co.class_id = cl.class_id
                WHERE co.teacher_id = ?
                ORDER BY co.offering_id
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, teacherId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    CourseOffering offering = new CourseOffering();
                    offering.setOfferingId(rs.getString("offering_id"));
                    offering.setCourseId(rs.getString("course_id"));
                    offering.setTeacherId(rs.getString("teacher_id"));
                    offering.setClassId(rs.getString("class_id"));
                    offering.setSemester(rs.getString("semester"));
                    offering.setSchedule(rs.getString("schedule"));
                    
                    // 设置关联的课程信息
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    course.setCredits(rs.getDouble("credits"));
                    course.setCourseType(rs.getString("course_type"));
                    course.setCollege(rs.getString("college"));
                    offering.setCourse(course);
                    
                    // 设置关联的班级信息
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    classRoom.setGrade(rs.getString("grade"));
                    classRoom.setMajor(rs.getString("major"));
                    offering.setClassRoom(classRoom);
                    
                    offerings.add(offering);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取教师授课列表时数据库错误: " + e.getMessage());
        }
        
        return offerings;
    }
    
    /**
     * 根据开课编号获取开课信息
     */
    public CourseOffering getCourseOfferingById(String offeringId) {
        try {
            String sql = """
                SELECT co.*, c.course_name, c.credits, c.course_type, c.college,
                       t.name as teacher_name, t.title, cl.class_name
                FROM course_offerings co
                JOIN courses c ON co.course_id = c.course_id
                JOIN teachers t ON co.teacher_id = t.teacher_id
                JOIN classes cl ON co.class_id = cl.class_id
                WHERE co.offering_id = ?
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, offeringId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    CourseOffering offering = new CourseOffering();
                    offering.setOfferingId(rs.getString("offering_id"));
                    offering.setCourseId(rs.getString("course_id"));
                    offering.setTeacherId(rs.getString("teacher_id"));
                    offering.setClassId(rs.getString("class_id"));
                    offering.setSemester(rs.getString("semester"));
                    offering.setSchedule(rs.getString("schedule"));
                    
                    // 设置关联的课程信息
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    course.setCredits(rs.getDouble("credits"));
                    course.setCourseType(rs.getString("course_type"));
                    course.setCollege(rs.getString("college"));
                    offering.setCourse(course);
                    
                    // 设置关联的教师信息
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(rs.getString("teacher_id"));
                    teacher.setName(rs.getString("teacher_name"));
                    teacher.setTitle(rs.getString("title"));
                    offering.setTeacher(teacher);
                    
                    // 设置关联的班级信息
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    offering.setClassRoom(classRoom);
                    
                    return offering;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取开课信息时数据库错误: " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取所有开课信息
     */
    public List<CourseOffering> getAllCourseOfferings() {
        List<CourseOffering> offerings = new ArrayList<>();

        try {
            String sql = """
                SELECT co.*, c.course_name, c.credits, c.course_type, c.college,
                       t.name as teacher_name, t.title, cl.class_name
                FROM course_offerings co
                JOIN courses c ON co.course_id = c.course_id
                JOIN teachers t ON co.teacher_id = t.teacher_id
                JOIN classes cl ON co.class_id = cl.class_id
                ORDER BY co.offering_id
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    CourseOffering offering = new CourseOffering();
                    offering.setOfferingId(rs.getString("offering_id"));
                    offering.setCourseId(rs.getString("course_id"));
                    offering.setTeacherId(rs.getString("teacher_id"));
                    offering.setClassId(rs.getString("class_id"));
                    offering.setSemester(rs.getString("semester"));
                    offering.setSchedule(rs.getString("schedule"));

                    // 设置关联的课程信息
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    course.setCredits(rs.getDouble("credits"));
                    course.setCourseType(rs.getString("course_type"));
                    course.setCollege(rs.getString("college"));
                    offering.setCourse(course);

                    // 设置关联的教师信息
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(rs.getString("teacher_id"));
                    teacher.setName(rs.getString("teacher_name"));
                    teacher.setTitle(rs.getString("title"));
                    offering.setTeacher(teacher);

                    // 设置关联的班级信息
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassId(rs.getString("class_id"));
                    classRoom.setClassName(rs.getString("class_name"));
                    offering.setClassRoom(classRoom);

                    offerings.add(offering);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取开课列表时数据库错误: " + e.getMessage());
        }

        return offerings;
    }

    /**
     * 删除开课信息
     */
    public boolean deleteCourseOffering(String offeringId) {
        try {
            String sql = "DELETE FROM course_offerings WHERE offering_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, offeringId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("删除开课信息时数据库错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 根据班级编号获取班级信息
     */
    public ClassRoom getClassRoomById(String classId) {
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
     * 获取所有班级信息
     */
    public List<ClassRoom> getAllClassRooms() {
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

}
