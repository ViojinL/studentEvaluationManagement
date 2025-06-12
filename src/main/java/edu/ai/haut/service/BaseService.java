package edu.ai.haut.service;

import edu.ai.haut.util.DatabaseUtil;
import edu.ai.haut.util.ValidationUtil;

/**
 * 基础服务类
 * 提供通用的CRUD操作和验证方法，减少重复代码
 */
public abstract class BaseService {
    
    /**
     * 检查ID是否已存在
     */
    protected boolean isIdExists(String tableName, String columnName, String id) {
        return DatabaseUtil.recordExists(tableName, columnName, id);
    }
    
    /**
     * 通用的数据验证方法
     */
    protected boolean validateBasicData(String id, String name, String gender, String password) {
        return ValidationUtil.isNotEmpty(id) &&
               ValidationUtil.isValidName(name) &&
               ValidationUtil.isValidGender(gender) &&
               ValidationUtil.isValidPassword(password);
    }
    
    /**
     * 通用的插入操作
     */
    protected boolean insertRecord(String tableName, String[] columns, Object[] values) {
        return DatabaseUtil.insertRecord(tableName, columns, values);
    }
    
    /**
     * 通用的更新操作
     */
    protected boolean updateRecord(String tableName, String[] setColumns, Object[] setValues, 
                                 String whereColumn, Object whereValue) {
        return DatabaseUtil.updateRecord(tableName, setColumns, setValues, whereColumn, whereValue);
    }
    
    /**
     * 通用的删除操作
     */
    protected boolean deleteRecord(String tableName, String whereColumn, Object whereValue) {
        return DatabaseUtil.deleteRecord(tableName, whereColumn, whereValue);
    }
    
    /**
     * 打印数据库错误信息
     */
    protected void logError(String operation, Exception e) {
        System.err.println(operation + "时发生错误: " + e.getMessage());
    }
}
