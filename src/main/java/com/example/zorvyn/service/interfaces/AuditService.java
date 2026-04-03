package com.example.zorvyn.service.interfaces;

public interface AuditService {
    void log(String action, String entityType, Long entityId, String details);
}

