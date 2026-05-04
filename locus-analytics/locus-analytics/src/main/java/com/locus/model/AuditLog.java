package com.locus.model;

import java.time.LocalDateTime;

public class AuditLog {
    private String auditId;
    private String adminId;
    private String tableName;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;

    public AuditLog() {}

    public AuditLog(String adminId, String tableName, String fieldName, String oldValue, String newValue) {
        this.adminId = adminId;
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
