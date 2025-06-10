package edu.ai.haut.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 评教模板实体类
 * 对应数据库中的evaluation_templates表
 */
public class EvaluationTemplate {
    
    private String templateId;     // 模板编号
    private String templateName;   // 模板名称
    private String courseType;     // 适用课程类型
    private String criteriaIds;    // 指标ID列表（逗号分隔）
    private boolean isActive;      // 是否激活
    private LocalDateTime createdAt; // 创建时间
    
    // 关联对象
    private List<EvaluationCriteria> criteriaList; // 指标列表
    
    /**
     * 默认构造函数
     */
    public EvaluationTemplate() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.criteriaList = new ArrayList<>();
    }
    
    /**
     * 带参数的构造函数
     */
    public EvaluationTemplate(String templateId, String templateName, String courseType, 
                             String criteriaIds, boolean isActive) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.courseType = courseType;
        this.criteriaIds = criteriaIds;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
        this.criteriaList = new ArrayList<>();
    }
    
    // Getter和Setter方法
    public String getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public String getCourseType() {
        return courseType;
    }
    
    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }
    
    public String getCriteriaIds() {
        return criteriaIds;
    }
    
    public void setCriteriaIds(String criteriaIds) {
        this.criteriaIds = criteriaIds;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<EvaluationCriteria> getCriteriaList() {
        return criteriaList;
    }
    
    public void setCriteriaList(List<EvaluationCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }
    
    /**
     * 获取指标ID列表
     */
    public List<String> getCriteriaIdList() {
        if (criteriaIds == null || criteriaIds.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(criteriaIds.split(","));
    }
    
    /**
     * 设置指标ID列表
     */
    public void setCriteriaIdList(List<String> criteriaIdList) {
        if (criteriaIdList == null || criteriaIdList.isEmpty()) {
            this.criteriaIds = "";
        } else {
            this.criteriaIds = String.join(",", criteriaIdList);
        }
    }
    
    /**
     * 添加指标ID
     */
    public void addCriteriaId(String criteriaId) {
        List<String> idList = getCriteriaIdList();
        if (!idList.contains(criteriaId)) {
            idList.add(criteriaId);
            setCriteriaIdList(idList);
        }
    }
    
    /**
     * 移除指标ID
     */
    public void removeCriteriaId(String criteriaId) {
        List<String> idList = getCriteriaIdList();
        idList.remove(criteriaId);
        setCriteriaIdList(idList);
    }
    
    /**
     * 计算总权重
     */
    public double getTotalWeight() {
        return criteriaList.stream()
                          .mapToDouble(EvaluationCriteria::getWeight)
                          .sum();
    }
    
    /**
     * 验证权重是否合法（总和为100%）
     */
    public boolean isValidWeight() {
        double totalWeight = getTotalWeight();
        return Math.abs(totalWeight - 100.0) < 0.01; // 允许0.01的误差
    }
    
    /**
     * 获取完整的模板信息字符串
     */
    public String getFullInfo() {
        return String.format("模板编号: %s, 模板名称: %s, 课程类型: %s, 指标数量: %d, 状态: %s",
                           templateId, templateName, courseType, criteriaList.size(), 
                           isActive ? "激活" : "停用");
    }
    
    /**
     * 获取简要信息（用于列表显示）
     */
    public String getBriefInfo() {
        return String.format("%s (%s)", templateName, courseType);
    }
    
    @Override
    public String toString() {
        return String.format("EvaluationTemplate{templateId='%s', templateName='%s', courseType='%s', isActive=%s}", 
                           templateId, templateName, courseType, isActive);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EvaluationTemplate template = (EvaluationTemplate) obj;
        return templateId != null && templateId.equals(template.templateId);
    }
    
    @Override
    public int hashCode() {
        return templateId != null ? templateId.hashCode() : 0;
    }
}
