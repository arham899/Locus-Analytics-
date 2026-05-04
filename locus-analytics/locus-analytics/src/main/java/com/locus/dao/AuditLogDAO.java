package com.locus.dao;

import com.locus.model.AuditLog;
import java.util.List;

public interface AuditLogDAO {
    boolean insert(AuditLog log);
    List<AuditLog> findByAdmin(String adminId);
    List<AuditLog> findAll();
}
