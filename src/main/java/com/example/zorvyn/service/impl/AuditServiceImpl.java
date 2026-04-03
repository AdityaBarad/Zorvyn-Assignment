package com.example.zorvyn.service.impl;

import com.example.zorvyn.model.entity.AuditLog;
import com.example.zorvyn.model.entity.User;
import com.example.zorvyn.repository.AuditLogRepository;
import com.example.zorvyn.repository.UserRepository;
import com.example.zorvyn.service.interfaces.AuditService;
import com.example.zorvyn.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Async
    @Override
    public void log(String action, String entityType, Long entityId, String details) {
        try {
            String email = SecurityUtils.getCurrentUserEmail();
            User actor = "anonymous".equals(email) ? null :
                    userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);

            AuditLog auditLog = AuditLog.builder()
                    .actor(actor)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit logged: action={}, entity={}, id={}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }
}
