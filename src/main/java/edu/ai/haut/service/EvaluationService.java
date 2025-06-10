package edu.ai.haut.service;

import edu.ai.haut.model.*;
import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 评教服务类
 * 处理评教相关的业务逻辑
 */
public class EvaluationService {
    
    /**
     * 创建评教指标
     */
    public boolean createEvaluationCriteria(EvaluationCriteria criteria) {
        if (!validateCriteriaData(criteria)) {
            return false;
        }
        
        try {
            String sql = """
                INSERT INTO evaluation_criteria (criteria_id, criteria_name, description, weight, max_score) 
                VALUES (?, ?, ?, ?, ?)
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, criteria.getCriteriaId());
                pstmt.setString(2, criteria.getCriteriaName());
                pstmt.setString(3, criteria.getDescription());
                pstmt.setDouble(4, criteria.getWeight());
                pstmt.setInt(5, criteria.getMaxScore());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("创建评教指标时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证评教指标数据
     */
    private boolean validateCriteriaData(EvaluationCriteria criteria) {
        if (criteria == null) return false;
        
        return ValidationUtil.isNotEmpty(criteria.getCriteriaId()) &&
               ValidationUtil.isNotEmpty(criteria.getCriteriaName()) &&
               ValidationUtil.isValidWeight(criteria.getWeight()) &&
               criteria.getMaxScore() > 0;
    }
    
    /**
     * 获取所有评教指标
     */
    public List<EvaluationCriteria> getAllEvaluationCriteria() {
        List<EvaluationCriteria> criteriaList = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM evaluation_criteria ORDER BY criteria_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    EvaluationCriteria criteria = new EvaluationCriteria();
                    criteria.setCriteriaId(rs.getString("criteria_id"));
                    criteria.setCriteriaName(rs.getString("criteria_name"));
                    criteria.setDescription(rs.getString("description"));
                    criteria.setWeight(rs.getDouble("weight"));
                    criteria.setMaxScore(rs.getInt("max_score"));
                    criteriaList.add(criteria);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取评教指标列表时数据库错误: " + e.getMessage());
        }
        
        return criteriaList;
    }
    
    /**
     * 创建评教周期
     */
    public boolean createEvaluationPeriod(EvaluationPeriod period) {
        if (!validatePeriodData(period)) {
            return false;
        }
        
        try {
            String sql = """
                INSERT INTO evaluation_periods (period_id, period_name, semester, start_date, end_date, status) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, period.getPeriodId());
                pstmt.setString(2, period.getPeriodName());
                pstmt.setString(3, period.getSemester());
                pstmt.setDate(4, Date.valueOf(period.getStartDate()));
                pstmt.setDate(5, Date.valueOf(period.getEndDate()));
                pstmt.setString(6, period.getStatus());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("创建评教周期时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证评教周期数据
     */
    private boolean validatePeriodData(EvaluationPeriod period) {
        if (period == null) return false;
        
        return ValidationUtil.isNotEmpty(period.getPeriodId()) &&
               ValidationUtil.isNotEmpty(period.getPeriodName()) &&
               ValidationUtil.isNotEmpty(period.getSemester()) &&
               period.getStartDate() != null &&
               period.getEndDate() != null &&
               !period.getStartDate().isAfter(period.getEndDate()) &&
               ValidationUtil.isValidEvaluationStatus(period.getStatus());
    }
    
    /**
     * 获取当前活跃的评教周期
     */
    public EvaluationPeriod getCurrentActivePeriod() {
        try {
            String sql = "SELECT * FROM evaluation_periods WHERE status = '进行中' ORDER BY start_date DESC LIMIT 1";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    EvaluationPeriod period = new EvaluationPeriod();
                    period.setPeriodId(rs.getString("period_id"));
                    period.setPeriodName(rs.getString("period_name"));
                    period.setSemester(rs.getString("semester"));
                    period.setStartDate(rs.getDate("start_date").toLocalDate());
                    period.setEndDate(rs.getDate("end_date").toLocalDate());
                    period.setStatus(rs.getString("status"));
                    return period;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取当前评教周期时数据库错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 提交学生评教
     */
    public boolean submitEvaluation(Evaluation evaluation) {
        if (!validateEvaluationData(evaluation)) {
            return false;
        }
        
        // 检查是否已经评教过
        if (hasStudentEvaluated(evaluation.getStudentId(), evaluation.getOfferingId(), evaluation.getPeriodId())) {
            return false;
        }
        
        try {
            String sql = """
                INSERT INTO evaluations (evaluation_id, student_id, offering_id, period_id, 
                                       criteria_scores, total_score, comments) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, evaluation.getEvaluationId());
                pstmt.setString(2, evaluation.getStudentId());
                pstmt.setString(3, evaluation.getOfferingId());
                pstmt.setString(4, evaluation.getPeriodId());
                pstmt.setString(5, evaluation.getCriteriaScores());
                pstmt.setDouble(6, evaluation.getTotalScore());
                pstmt.setString(7, evaluation.getComments());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("提交评教时数据库错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证评教数据
     */
    private boolean validateEvaluationData(Evaluation evaluation) {
        if (evaluation == null) return false;
        
        return ValidationUtil.isNotEmpty(evaluation.getEvaluationId()) &&
               ValidationUtil.isNotEmpty(evaluation.getStudentId()) &&
               ValidationUtil.isNotEmpty(evaluation.getOfferingId()) &&
               ValidationUtil.isNotEmpty(evaluation.getPeriodId()) &&
               evaluation.getTotalScore() >= 0 && evaluation.getTotalScore() <= 100;
    }
    
    /**
     * 检查学生是否已经评教
     */
    public boolean hasStudentEvaluated(String studentId, String offeringId, String periodId) {
        try {
            String sql = "SELECT COUNT(*) FROM evaluations WHERE student_id = ? AND offering_id = ? AND period_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, studentId);
                pstmt.setString(2, offeringId);
                pstmt.setString(3, periodId);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("检查评教状态时数据库错误: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 获取学生的评教历史
     */
    public List<Evaluation> getStudentEvaluationHistory(String studentId) {
        List<Evaluation> evaluations = new ArrayList<>();
        
        try {
            String sql = """
                SELECT e.*, co.course_id, c.course_name, t.name as teacher_name,
                       ep.period_name, ep.semester
                FROM evaluations e
                JOIN course_offerings co ON e.offering_id = co.offering_id
                JOIN courses c ON co.course_id = c.course_id
                JOIN teachers t ON co.teacher_id = t.teacher_id
                JOIN evaluation_periods ep ON e.period_id = ep.period_id
                WHERE e.student_id = ?
                ORDER BY e.evaluation_date DESC
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, studentId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setEvaluationId(rs.getString("evaluation_id"));
                    evaluation.setStudentId(rs.getString("student_id"));
                    evaluation.setOfferingId(rs.getString("offering_id"));
                    evaluation.setPeriodId(rs.getString("period_id"));
                    evaluation.setCriteriaScores(rs.getString("criteria_scores"));
                    evaluation.setTotalScore(rs.getDouble("total_score"));
                    evaluation.setComments(rs.getString("comments"));
                    
                    // 设置关联的课程信息
                    CourseOffering offering = new CourseOffering();
                    offering.setOfferingId(rs.getString("offering_id"));
                    offering.setCourseId(rs.getString("course_id"));
                    
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    offering.setCourse(course);
                    
                    Teacher teacher = new Teacher();
                    teacher.setName(rs.getString("teacher_name"));
                    offering.setTeacher(teacher);
                    
                    evaluation.setCourseOffering(offering);
                    
                    // 设置评教周期信息
                    EvaluationPeriod period = new EvaluationPeriod();
                    period.setPeriodId(rs.getString("period_id"));
                    period.setPeriodName(rs.getString("period_name"));
                    period.setSemester(rs.getString("semester"));
                    evaluation.setPeriod(period);
                    
                    evaluations.add(evaluation);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取学生评教历史时数据库错误: " + e.getMessage());
        }
        
        return evaluations;
    }
    
    /**
     * 获取教师的评教结果
     */
    public List<Evaluation> getTeacherEvaluationResults(String teacherId, String periodId) {
        List<Evaluation> evaluations = new ArrayList<>();
        
        try {
            String sql = """
                SELECT e.*, co.course_id, c.course_name, cl.class_name,
                       ep.period_name, ep.semester
                FROM evaluations e
                JOIN course_offerings co ON e.offering_id = co.offering_id
                JOIN courses c ON co.course_id = c.course_id
                JOIN classes cl ON co.class_id = cl.class_id
                JOIN evaluation_periods ep ON e.period_id = ep.period_id
                WHERE co.teacher_id = ? AND e.period_id = ?
                ORDER BY e.evaluation_date DESC
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, teacherId);
                pstmt.setString(2, periodId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setEvaluationId(rs.getString("evaluation_id"));
                    evaluation.setStudentId(rs.getString("student_id"));
                    evaluation.setOfferingId(rs.getString("offering_id"));
                    evaluation.setPeriodId(rs.getString("period_id"));
                    evaluation.setCriteriaScores(rs.getString("criteria_scores"));
                    evaluation.setTotalScore(rs.getDouble("total_score"));
                    evaluation.setComments(rs.getString("comments"));
                    
                    // 设置关联的课程信息
                    CourseOffering offering = new CourseOffering();
                    offering.setOfferingId(rs.getString("offering_id"));
                    offering.setCourseId(rs.getString("course_id"));
                    
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    offering.setCourse(course);
                    
                    ClassRoom classRoom = new ClassRoom();
                    classRoom.setClassName(rs.getString("class_name"));
                    offering.setClassRoom(classRoom);
                    
                    evaluation.setCourseOffering(offering);
                    
                    // 设置评教周期信息
                    EvaluationPeriod period = new EvaluationPeriod();
                    period.setPeriodId(rs.getString("period_id"));
                    period.setPeriodName(rs.getString("period_name"));
                    period.setSemester(rs.getString("semester"));
                    evaluation.setPeriod(period);
                    
                    evaluations.add(evaluation);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取教师评教结果时数据库错误: " + e.getMessage());
        }
        
        return evaluations;
    }
    
    /**
     * 计算教师课程的平均分
     */
    public double calculateTeacherCourseAverageScore(String teacherId, String courseId, String periodId) {
        try {
            String sql = """
                SELECT AVG(e.total_score) as avg_score
                FROM evaluations e
                JOIN course_offerings co ON e.offering_id = co.offering_id
                WHERE co.teacher_id = ? AND co.course_id = ? AND e.period_id = ?
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, teacherId);
                pstmt.setString(2, courseId);
                pstmt.setString(3, periodId);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("avg_score");
                }
            }
        } catch (SQLException e) {
            System.err.println("计算平均分时数据库错误: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * 获取所有评教周期
     */
    public List<EvaluationPeriod> getAllEvaluationPeriods() {
        List<EvaluationPeriod> periods = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM evaluation_periods ORDER BY start_date DESC";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    EvaluationPeriod period = new EvaluationPeriod();
                    period.setPeriodId(rs.getString("period_id"));
                    period.setPeriodName(rs.getString("period_name"));
                    period.setSemester(rs.getString("semester"));
                    period.setStartDate(rs.getDate("start_date").toLocalDate());
                    period.setEndDate(rs.getDate("end_date").toLocalDate());
                    period.setStatus(rs.getString("status"));
                    periods.add(period);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取评教周期列表时数据库错误: " + e.getMessage());
        }
        
        return periods;
    }

    /**
     * 根据编号获取评教指标
     */
    public EvaluationCriteria getEvaluationCriteriaById(String criteriaId) {
        try {
            String sql = "SELECT * FROM evaluation_criteria WHERE criteria_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, criteriaId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    EvaluationCriteria criteria = new EvaluationCriteria();
                    criteria.setCriteriaId(rs.getString("criteria_id"));
                    criteria.setCriteriaName(rs.getString("criteria_name"));
                    criteria.setDescription(rs.getString("description"));
                    criteria.setWeight(rs.getDouble("weight"));
                    criteria.setMaxScore(rs.getInt("max_score"));
                    return criteria;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取评教指标时数据库错误: " + e.getMessage());
        }
        return null;
    }

    /**
     * 更新评教指标
     */
    public boolean updateEvaluationCriteria(EvaluationCriteria criteria) {
        if (!validateCriteriaData(criteria)) {
            return false;
        }

        try {
            String sql = """
                UPDATE evaluation_criteria SET criteria_name = ?, description = ?,
                weight = ?, max_score = ? WHERE criteria_id = ?
            """;

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, criteria.getCriteriaName());
                pstmt.setString(2, criteria.getDescription());
                pstmt.setDouble(3, criteria.getWeight());
                pstmt.setInt(4, criteria.getMaxScore());
                pstmt.setString(5, criteria.getCriteriaId());

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("更新评教指标时数据库错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除评教指标
     */
    public boolean deleteEvaluationCriteria(String criteriaId) {
        try {
            String sql = "DELETE FROM evaluation_criteria WHERE criteria_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, criteriaId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("删除评教指标时数据库错误: " + e.getMessage());
            return false;
        }
    }
}
