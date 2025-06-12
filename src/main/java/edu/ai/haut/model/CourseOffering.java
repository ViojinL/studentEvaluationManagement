package edu.ai.haut.model;

/**
 * 开课信息实体类
 * 对应数据库中的course_offerings表
 */
public class CourseOffering {
    
    private String offeringId;     // 开课编号
    private String courseId;       // 课程编号
    private String teacherId;      // 授课教师工号
    private String classId;        // 上课班级编号
    private String semester;       // 学期
    private String schedule;       // 上课时间
    
    // 关联对象（用于显示详细信息）
    private Course course;         // 课程对象
    private Teacher teacher;       // 教师对象
    private ClassRoom classRoom;   // 班级对象
    
    /**
     * 默认构造函数
     */
    public CourseOffering() {
    }
    
    /**
     * 带参数的构造函数
     */
    public CourseOffering(String offeringId, String courseId, String teacherId,
                         String classId, String semester, String schedule) {
        this.offeringId = offeringId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.classId = classId;
        this.semester = semester;
        this.schedule = schedule;
    }
    
    // Getter和Setter方法
    public String getOfferingId() {
        return offeringId;
    }
    
    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
    }
    
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    public String getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getClassId() {
        return classId;
    }
    
    public void setClassId(String classId) {
        this.classId = classId;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public String getSchedule() {
        return schedule;
    }
    
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
    }
    
    public Teacher getTeacher() {
        return teacher;
    }
    
    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
    
    public ClassRoom getClassRoom() {
        return classRoom;
    }
    
    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
    }
    
    /**
     * 获取完整的开课信息字符串
     */
    public String getFullInfo() {
        String courseName = course != null ? course.getCourseName() : "未知课程";
        String teacherName = teacher != null ? teacher.getName() : "未知教师";
        String className = classRoom != null ? classRoom.getClassName() : "未知班级";
        
        return String.format("开课编号: %s, 课程: %s, 教师: %s, 班级: %s, 学期: %s, 时间: %s",
                           offeringId, courseName, teacherName, className, semester, schedule);
    }
    
    /**
     * 获取简要信息（用于列表显示）
     */
    public String getBriefInfo() {
        String courseName = course != null ? course.getCourseName() : courseId;
        String teacherName = teacher != null ? teacher.getName() : teacherId;
        
        return String.format("%s - %s (%s)", courseName, teacherName, semester);
    }
    
    @Override
    public String toString() {
        return String.format("CourseOffering{offeringId='%s', courseId='%s', teacherId='%s', classId='%s', semester='%s', schedule='%s'}", 
                           offeringId, courseId, teacherId, classId, semester, schedule);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CourseOffering offering = (CourseOffering) obj;
        return offeringId != null && offeringId.equals(offering.offeringId);
    }
    
    @Override
    public int hashCode() {
        return offeringId != null ? offeringId.hashCode() : 0;
    }
}
