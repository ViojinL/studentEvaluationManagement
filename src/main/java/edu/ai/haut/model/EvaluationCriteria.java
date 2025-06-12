package edu.ai.haut.model;

/**
 * 评教指标实体类
 * 对应数据库中的evaluation_criteria表
 */
public class EvaluationCriteria {
    
    private String criteriaId;     // 指标编号
    private String criteriaName;   // 指标名称
    private String description;    // 指标描述
    private double weight;         // 权重（百分比）
    private int maxScore;          // 最高分数
    
    /**
     * 默认构造函数
     */
    public EvaluationCriteria() {
        this.maxScore = 100;
    }
    
    /**
     * 带参数的构造函数
     */
    public EvaluationCriteria(String criteriaId, String criteriaName, String description, 
                             double weight, int maxScore) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.description = description;
        this.weight = weight;
        this.maxScore = maxScore;
    }
    
    // Getter和Setter方法
    public String getCriteriaId() {
        return criteriaId;
    }
    
    public void setCriteriaId(String criteriaId) {
        this.criteriaId = criteriaId;
    }
    
    public String getCriteriaName() {
        return criteriaName;
    }
    
    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public int getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }
    
    /**
     * 计算加权分数
     */
    public double calculateWeightedScore(int score) {
        return (score * weight) / 100.0;
    }
    
    /**
     * 验证分数是否有效
     */
    public boolean isValidScore(int score) {
        return score >= 0 && score <= maxScore;
    }
    
    /**
     * 获取完整的指标信息字符串
     */
    public String getFullInfo() {
        return String.format("指标编号: %s, 指标名称: %s, 权重: %.1f%%, 最高分: %d分",
                           criteriaId, criteriaName, weight, maxScore);
    }
    
    /**
     * 获取简要信息（用于列表显示）
     */
    public String getBriefInfo() {
        return String.format("%s (权重: %.1f%%)", criteriaName, weight);
    }
    
    @Override
    public String toString() {
        return String.format("EvaluationCriteria{criteriaId='%s', criteriaName='%s', weight=%.1f, maxScore=%d}", 
                           criteriaId, criteriaName, weight, maxScore);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EvaluationCriteria criteria = (EvaluationCriteria) obj;
        return criteriaId != null && criteriaId.equals(criteria.criteriaId);
    }
    
    @Override
    public int hashCode() {
        return criteriaId != null ? criteriaId.hashCode() : 0;
    }
}
