package edu.ai.haut.util;

import java.sql.*;
import java.util.Properties;

/**
 * 数据库工具类
 * 负责数据库连接管理和初始化
 */
public class DatabaseUtil {
    
    private static final String DB_URL = "jdbc:hsqldb:file:data/studentevaluation;shutdown=true";
    private static final String DB_USER = "SA";
    private static final String DB_PASSWORD = "";
    
    private static Connection connection;
    
    static {
        try {
            // 加载HSQLDB驱动
            Class.forName("org.hsqldb.jdbcDriver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法加载HSQLDB驱动", e);
        }
    }
    
    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("shutdown", "true");
            connection = DriverManager.getConnection(DB_URL, props);
        }
        return connection;
    }
    
    /**
     * 初始化数据库
     */
    private static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createTables(conn);
            createDefaultAdmin(conn);
            System.out.println("数据库初始化完成");
        } catch (SQLException e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
    
    /**
     * 创建数据库表
     */
    private static void createTables(Connection conn) throws SQLException {
        String[] createTableSQLs = {
            // 管理员表
            """
            CREATE TABLE IF NOT EXISTS administrators (
                admin_id VARCHAR(20) PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                gender VARCHAR(10),
                password VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // 班级表
            """
            CREATE TABLE IF NOT EXISTS classes (
                class_id VARCHAR(20) PRIMARY KEY,
                class_name VARCHAR(100) NOT NULL,
                grade VARCHAR(10) NOT NULL,
                major VARCHAR(100) NOT NULL,
                college VARCHAR(100) NOT NULL,
                student_count INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // 学生表
            """
            CREATE TABLE IF NOT EXISTS students (
                student_id VARCHAR(20) PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                gender VARCHAR(10),
                grade VARCHAR(10) NOT NULL,
                major VARCHAR(100) NOT NULL,
                class_id VARCHAR(20) NOT NULL,
                contact VARCHAR(50),
                password VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (class_id) REFERENCES classes(class_id)
            )
            """,
            
            // 教师表
            """
            CREATE TABLE IF NOT EXISTS teachers (
                teacher_id VARCHAR(20) PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                gender VARCHAR(10),
                title VARCHAR(50),
                college VARCHAR(100) NOT NULL,
                contact VARCHAR(50),
                password VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,

            // 教务人员表
            """
            CREATE TABLE IF NOT EXISTS academic_affairs_staff (
                staff_id VARCHAR(20) PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                gender VARCHAR(10),
                department VARCHAR(100) NOT NULL,
                position VARCHAR(50),
                contact VARCHAR(50),
                password VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // 课程表
            """
            CREATE TABLE IF NOT EXISTS courses (
                course_id VARCHAR(20) PRIMARY KEY,
                course_name VARCHAR(100) NOT NULL,
                credits DECIMAL(3,1) NOT NULL,
                course_type VARCHAR(20) NOT NULL,
                college VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // 开课信息表
            """
            CREATE TABLE IF NOT EXISTS course_offerings (
                offering_id VARCHAR(20) PRIMARY KEY,
                course_id VARCHAR(20) NOT NULL,
                teacher_id VARCHAR(20) NOT NULL,
                class_id VARCHAR(20) NOT NULL,
                semester VARCHAR(20) NOT NULL,
                schedule VARCHAR(200),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (course_id) REFERENCES courses(course_id),
                FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
                FOREIGN KEY (class_id) REFERENCES classes(class_id)
            )
            """,
            
            // 评教指标表
            """
            CREATE TABLE IF NOT EXISTS evaluation_criteria (
                criteria_id VARCHAR(20) PRIMARY KEY,
                criteria_name VARCHAR(100) NOT NULL,
                description VARCHAR(500),
                weight DECIMAL(5,2) NOT NULL,
                max_score INTEGER DEFAULT 100,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // 评教模板表
            """
            CREATE TABLE IF NOT EXISTS evaluation_templates (
                template_id VARCHAR(20) PRIMARY KEY,
                template_name VARCHAR(100) NOT NULL,
                course_type VARCHAR(20) NOT NULL,
                criteria_ids VARCHAR(1000) NOT NULL,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // 评教周期表
            """
            CREATE TABLE IF NOT EXISTS evaluation_periods (
                period_id VARCHAR(20) PRIMARY KEY,
                period_name VARCHAR(100) NOT NULL,
                semester VARCHAR(20) NOT NULL,
                start_date DATE NOT NULL,
                end_date DATE NOT NULL,
                status VARCHAR(20) DEFAULT '未开始',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // 评教记录表
            """
            CREATE TABLE IF NOT EXISTS evaluations (
                evaluation_id VARCHAR(20) PRIMARY KEY,
                student_id VARCHAR(20) NOT NULL,
                offering_id VARCHAR(20) NOT NULL,
                period_id VARCHAR(20) NOT NULL,
                criteria_scores CLOB NOT NULL,
                total_score DECIMAL(5,2),
                comments CLOB,
                evaluation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES students(student_id),
                FOREIGN KEY (offering_id) REFERENCES course_offerings(offering_id),
                FOREIGN KEY (period_id) REFERENCES evaluation_periods(period_id),
                UNIQUE(student_id, offering_id, period_id)
            )
            """
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : createTableSQLs) {
                stmt.execute(sql);
            }
        }
    }
    
    /**
     * 创建默认管理员账户
     */
    private static void createDefaultAdmin(Connection conn) throws SQLException {
        String checkAdminSQL = "SELECT COUNT(*) FROM administrators WHERE admin_id = 'ADMIN001'";
        try (PreparedStatement pstmt = conn.prepareStatement(checkAdminSQL)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                String insertAdminSQL = """
                    INSERT INTO administrators (admin_id, name, gender, password) 
                    VALUES ('ADMIN001', '系统管理员', '男', '123456')
                """;
                try (PreparedStatement insertStmt = conn.prepareStatement(insertAdminSQL)) {
                    insertStmt.executeUpdate();
                    System.out.println("默认管理员账户创建成功");
                }
            }
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接时出错: " + e.getMessage());
        }
    }
    
    /**
     * 执行查询
     */
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }
    
    /**
     * 执行更新
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        }
    }
}
