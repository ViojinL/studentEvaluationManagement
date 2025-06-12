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
            insertInitialData(conn);
            System.out.println("数据库初始化完成，测试数据已添加");
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
     * 插入初始数据（包括管理员和测试数据）
     */
    private static void insertInitialData(Connection conn) throws SQLException {
        // 检查是否已有数据
        if (hasData(conn)) {
            System.out.println("数据库已有数据，跳过初始化");
            return;
        }

        System.out.println("正在插入初始数据...");

        // 1. 插入管理员
        insertAdministrators(conn);

        // 2. 插入班级
        insertClasses(conn);

        // 3. 插入学生
        insertStudents(conn);

        // 4. 插入教师
        insertTeachers(conn);

        // 5. 插入教务人员
        insertStaff(conn);

        // 6. 插入课程
        insertCourses(conn);

        // 7. 插入开课信息
        insertCourseOfferings(conn);

        // 8. 插入评教指标
        insertEvaluationCriteria(conn);

        // 9. 插入评教周期
        insertEvaluationPeriods(conn);

        // 10. 插入评教记录
        insertEvaluations(conn);

        System.out.println("初始数据插入完成");
    }

    /**
     * 检查数据库是否已有数据
     */
    private static boolean hasData(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM administrators";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
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

    // ==================== 数据插入方法 ====================

    /**
     * 插入管理员数据
     */
    private static void insertAdministrators(Connection conn) throws SQLException {
        String sql = "INSERT INTO administrators (admin_id, name, gender, password) VALUES (?, ?, ?, ?)";

        Object[][] adminData = {
            {"ADMIN001", "系统管理员", "男", "123456"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : adminData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + adminData.length + " 个管理员");
    }

    /**
     * 插入班级数据
     */
    private static void insertClasses(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO classes (class_id, class_name, grade, major, college, student_count)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Object[][] classData = {
            {"软件工程2301", "软件工程2301班", "23", "软件工程", "人工智能与大数据学院", 0},
            {"软件工程2302", "软件工程2302班", "23", "软件工程", "人工智能与大数据学院", 0},
            {"计算机2301", "计算机科学与技术2301班", "23", "计算机科学与技术", "信息科学与工程学院", 0},
            {"计算机2302", "计算机科学与技术2302班", "23", "计算机科学与技术", "信息科学与工程学院", 0},
            {"数据科学2301", "数据科学与大数据技术2301班", "23", "数据科学与大数据技术", "人工智能与大数据学院", 0}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : classData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + classData.length + " 个班级");
    }

    /**
     * 插入学生数据
     */
    private static void insertStudents(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO students (student_id, name, gender, grade, major, class_id, password)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        Object[][] studentData = {
            {"231210400111", "张三", "男", "23", "软件工程", "软件工程2301", "123456"},
            {"231210400112", "李四", "女", "23", "软件工程", "软件工程2301", "123456"},
            {"231210400113", "王五", "男", "23", "软件工程", "软件工程2301", "123456"},
            {"231210400121", "赵六", "女", "23", "软件工程", "软件工程2302", "123456"},
            {"231210400122", "钱七", "男", "23", "软件工程", "软件工程2302", "123456"},
            {"231210500111", "孙八", "女", "23", "计算机科学与技术", "计算机2301", "123456"},
            {"231210500112", "周九", "男", "23", "计算机科学与技术", "计算机2301", "123456"},
            {"231210500121", "吴十", "女", "23", "计算机科学与技术", "计算机2302", "123456"},
            {"231210600111", "郑一", "男", "23", "数据科学与大数据技术", "数据科学2301", "123456"},
            {"231210600112", "王二", "女", "23", "数据科学与大数据技术", "数据科学2301", "123456"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : studentData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }

        // 更新班级学生数量
        updateClassStudentCount(conn);

        System.out.println("插入了 " + studentData.length + " 个学生");
    }

    /**
     * 更新班级学生数量
     */
    private static void updateClassStudentCount(Connection conn) throws SQLException {
        String sql = """
            UPDATE classes SET student_count = (
                SELECT COUNT(*) FROM students WHERE students.class_id = classes.class_id
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * 插入教师数据
     */
    private static void insertTeachers(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO teachers (teacher_id, name, gender, title, college, password)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Object[][] teacherData = {
            {"T20240001", "陈教授", "男", "教授", "人工智能与大数据学院", "123456"},
            {"T20240002", "刘副教授", "女", "副教授", "人工智能与大数据学院", "123456"},
            {"T20240003", "张讲师", "男", "讲师", "信息科学与工程学院", "123456"},
            {"T20240004", "李助教", "女", "助教", "信息科学与工程学院", "123456"},
            {"T20240005", "王教授", "男", "教授", "信息科学与工程学院", "123456"},
            {"T20240006", "赵副教授", "女", "副教授", "人工智能与大数据学院", "123456"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : teacherData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + teacherData.length + " 个教师");
    }

    /**
     * 插入教务人员数据
     */
    private static void insertStaff(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO academic_affairs_staff (staff_id, name, gender, department, position, password)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Object[][] staffData = {
            {"S20240001", "教务主任", "男", "教务处", "主任", "123456"},
            {"S20240002", "教务副主任", "女", "教务处", "副主任", "123456"},
            {"S20240003", "教务员", "女", "教务处", "教务员", "123456"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : staffData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + staffData.length + " 个教务人员");
    }

    /**
     * 插入课程数据
     */
    private static void insertCourses(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO courses (course_id, course_name, credits, course_type, college)
            VALUES (?, ?, ?, ?, ?)
        """;

        Object[][] courseData = {
            {"AI0001", "Java程序设计", 3.0, "必修", "人工智能与大数据学院"},
            {"IS0001", "数据结构与算法", 4.0, "必修", "信息科学与工程学院"},
            {"AI0002", "数据库原理", 3.0, "必修", "人工智能与大数据学院"},
            {"AI0003", "软件工程", 3.0, "必修", "人工智能与大数据学院"},
            {"IS0002", "计算机网络", 3.0, "必修", "信息科学与工程学院"},
            {"IS0003", "操作系统", 3.0, "必修", "信息科学与工程学院"},
            {"AI0004", "Web开发技术", 2.0, "选修", "人工智能与大数据学院"},
            {"AI0005", "人工智能导论", 2.0, "选修", "人工智能与大数据学院"},
            {"AI0006", "大数据技术", 2.0, "选修", "人工智能与大数据学院"},
            {"AI0007", "移动应用开发", 2.0, "选修", "人工智能与大数据学院"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : courseData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + courseData.length + " 门课程");
    }

    /**
     * 插入开课信息数据
     */
    private static void insertCourseOfferings(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO course_offerings (offering_id, course_id, teacher_id, class_id, semester, schedule)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Object[][] offeringData = {
            {"OFF001", "AI0001", "T20240001", "软件工程2301", "2025-1", "周一 1-2节"},
            {"OFF002", "AI0001", "T20240001", "软件工程2302", "2025-1", "周一 3-4节"},
            {"OFF003", "IS0001", "T20240002", "软件工程2301", "2025-1", "周二 1-2节"},
            {"OFF004", "IS0001", "T20240002", "计算机2301", "2025-1", "周二 3-4节"},
            {"OFF005", "AI0002", "T20240003", "软件工程2301", "2025-1", "周三 1-2节"},
            {"OFF006", "AI0002", "T20240003", "计算机2301", "2025-1", "周三 3-4节"},
            {"OFF007", "AI0003", "T20240004", "软件工程2301", "2025-1", "周四 1-2节"},
            {"OFF008", "IS0002", "T20240005", "计算机2301", "2025-1", "周四 3-4节"},
            {"OFF009", "AI0004", "T20240006", "软件工程2301", "2025-1", "周五 1-2节"},
            {"OFF010", "AI0005", "T20240001", "数据科学2301", "2025-1", "周五 3-4节"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : offeringData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + offeringData.length + " 个开课信息");
    }

    /**
     * 插入评教指标数据
     */
    private static void insertEvaluationCriteria(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO evaluation_criteria (criteria_id, criteria_name, description, weight, max_score)
            VALUES (?, ?, ?, ?, ?)
        """;

        Object[][] criteriaData = {
            {"C001", "教学态度", "教师的教学态度和责任心", 20.0, 100},
            {"C002", "教学内容", "教学内容的丰富性和实用性", 25.0, 100},
            {"C003", "教学方法", "教学方法的多样性和有效性", 20.0, 100},
            {"C004", "课堂管理", "课堂纪律和时间管理", 15.0, 100},
            {"C005", "师生互动", "与学生的交流和互动情况", 20.0, 100}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : criteriaData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + criteriaData.length + " 个评教指标");
    }

    /**
     * 插入评教周期数据
     */
    private static void insertEvaluationPeriods(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO evaluation_periods (period_id, period_name, semester, start_date, end_date, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Object[][] periodData = {
            {"P001", "2024年春季学期期中评教", "2024-1", "2024-04-01", "2024-04-15", "已结束"},
            {"P002", "2024年春季学期期末评教", "2024-1", "2024-06-01", "2024-06-15", "已结束"},
            {"P003", "2024年秋季学期期中评教", "2024-2", "2024-10-01", "2024-10-15", "已结束"},
            {"P004", "2024年秋季学期期末评教", "2024-2", "2024-12-01", "2024-12-15", "已结束"},
            {"P005", "2025年春季学期期中评教", "2025-1", "2025-04-01", "2025-04-15", "已结束"},
            {"P006", "2025年春季学期期末评教", "2025-1", "2025-06-01", "2025-06-15", "进行中"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : periodData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + periodData.length + " 个评教周期");
    }

    /**
     * 插入评教记录数据
     */
    private static void insertEvaluations(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO evaluations (evaluation_id, student_id, offering_id, period_id, criteria_scores, total_score, comments)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        Object[][] evaluationData = {
            // 2024年春季学期期中评教记录 - 使用2024年的开课信息和评教周期
            {"E001", "231210400111", "OFF001", "P001", "{\"C001\":85,\"C002\":90,\"C003\":88,\"C004\":92,\"C005\":87}", 88.4, "老师讲课很认真，内容丰富"},
            {"E002", "231210400112", "OFF001", "P001", "{\"C001\":90,\"C002\":88,\"C003\":85,\"C004\":90,\"C005\":92}", 88.8, "教学方法很好，互动性强"},
            {"E003", "231210400113", "OFF001", "P001", "{\"C001\":88,\"C002\":92,\"C003\":90,\"C004\":85,\"C005\":88}", 89.0, "课程内容实用，受益匪浅"},

            // 2024年春季学期期末评教记录
            {"E004", "231210400111", "OFF002", "P002", "{\"C001\":92,\"C002\":90,\"C003\":88,\"C004\":90,\"C005\":85}", 89.2, "Java课程很实用"},
            {"E005", "231210400112", "OFF002", "P002", "{\"C001\":85,\"C002\":88,\"C003\":92,\"C004\":88,\"C005\":90}", 88.6, "编程实践很有帮助"},

            // 2025年春季学期期中评教记录
            {"E006", "231210500111", "OFF004", "P005", "{\"C001\":90,\"C002\":85,\"C003\":88,\"C004\":92,\"C005\":88}", 88.4, "老师很负责任"},
            {"E007", "231210500112", "OFF004", "P005", "{\"C001\":88,\"C002\":90,\"C003\":85,\"C004\":88,\"C005\":92}", 88.6, "课堂氛围很好"},

            // 2025年春季学期期末评教记录（当前进行中的周期）
            {"E008", "231210400111", "OFF001", "P006", "{\"C001\":90,\"C002\":92,\"C003\":89,\"C004\":88,\"C005\":91}", 90.0, "期末复习很有帮助"},
            {"E009", "231210400112", "OFF005", "P006", "{\"C001\":87,\"C002\":89,\"C003\":90,\"C004\":92,\"C005\":88}", 89.2, "数据库课程很实用"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] data : evaluationData) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
        }
        System.out.println("插入了 " + evaluationData.length + " 条评教记录");
    }


}
