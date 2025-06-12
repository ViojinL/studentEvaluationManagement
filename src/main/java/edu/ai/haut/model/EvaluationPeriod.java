package edu.ai.haut.model;

import java.time.LocalDate;

/**
 * 评教周期实体类
 * 对应数据库中的evaluation_periods表
 */
public class EvaluationPeriod {
    
    private String periodId;       // 周期编号
    private String periodName;     // 周期名称
    private String semester;       // 学期
    private LocalDate startDate;   // 开始日期
    private LocalDate endDate;     // 结束日期
    private String status;         // 状态（未开始、进行中、已完成、已关闭）
    
    /**
     * 默认构造函数
     */
    public EvaluationPeriod() {
        this.status = "未开始";
    }
    
    /**
     * 带参数的构造函数
     */
    public EvaluationPeriod(String periodId, String periodName, String semester, 
                           LocalDate startDate, LocalDate endDate, String status) {
        this.periodId = periodId;
        this.periodName = periodName;
        this.semester = semester;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
    
    // Getter和Setter方法
    public String getPeriodId() {
        return periodId;
    }
    
    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }
    
    public String getPeriodName() {
        return periodName;
    }
    
    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * 检查当前日期是否在评教周期内
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return "进行中".equals(status) && 
               !today.isBefore(startDate) && 
               !today.isAfter(endDate);
    }
    
    /**
     * 检查评教周期是否已开始
     */
    public boolean hasStarted() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate);
    }
    
    /**
     * 检查评教周期是否已结束
     */
    public boolean hasEnded() {
        LocalDate today = LocalDate.now();
        return today.isAfter(endDate);
    }
    
    /**
     * 获取周期持续天数
     */
    public long getDurationDays() {
        if (startDate != null && endDate != null) {
            return endDate.toEpochDay() - startDate.toEpochDay() + 1;
        }
        return 0;
    }
    
    /**
     * 更新状态（根据当前日期自动判断）
     */
    public void updateStatus() {
        LocalDate today = LocalDate.now();
        
        if (today.isBefore(startDate)) {
            this.status = "未开始";
        } else if (today.isAfter(endDate)) {
            if (!"已关闭".equals(this.status)) {
                this.status = "已完成";
            }
        } else {
            this.status = "进行中";
        }
    }
    
    /**
     * 获取完整的周期信息字符串
     */
    public String getFullInfo() {
        return String.format("周期编号: %s, 周期名称: %s, 学期: %s, 开始日期: %s, 结束日期: %s, 状态: %s",
                           periodId, periodName, semester, startDate, endDate, status);
    }
    
    /**
     * 获取简要信息（用于列表显示）
     */
    public String getBriefInfo() {
        return String.format("%s (%s) - %s", periodName, semester, status);
    }
    
    @Override
    public String toString() {
        return String.format("EvaluationPeriod{periodId='%s', periodName='%s', semester='%s', startDate=%s, endDate=%s, status='%s'}", 
                           periodId, periodName, semester, startDate, endDate, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EvaluationPeriod period = (EvaluationPeriod) obj;
        return periodId != null && periodId.equals(period.periodId);
    }
    
    @Override
    public int hashCode() {
        return periodId != null ? periodId.hashCode() : 0;
    }
}
