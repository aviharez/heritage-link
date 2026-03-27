package com.project.heritagelink.service;

import com.project.heritagelink.dto.response.AuditLogResponse;
import com.project.heritagelink.model.AuditLog;
import com.project.heritagelink.model.Item;
import com.project.heritagelink.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(Item item, String action, String previousState, String newState, String details) {
        AuditLog log = AuditLog.builder()
                .itemId(item.getId())
                .itemName(item.getName())
                .action(action)
                .previousState(previousState)
                .newState(newState)
                .details(details)
                .performedBy(getCurrentUser())
                .build();
        auditLogRepository.save(log);
    }

    public void logClaim(Long itemId, String itemName, String action, String details) {
        AuditLog log = AuditLog.builder()
                .itemId(itemId)
                .itemName(itemName)
                .action(action)
                .details(details)
                .performedBy(getCurrentUser())
                .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<AuditLogResponse> findByItemId(Long itemId, Pageable pageable) {
        return auditLogRepository.findByItemId(itemId, pageable).map(this::toResponse);
    }

    // Helpers

    /**
     * Extracts the authenticated username from the Spring Security context
     * Falls back to "SYSTEM" for internal/programmatic calls
     */
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .itemId(log.getItemId())
                .itemName(log.getItemName())
                .action(log.getAction())
                .previousState(log.getPreviousState())
                .newState(log.getNewState())
                .details(log.getDetails())
                .performedBy(log.getPerformedBy())
                .timestamp(log.getTimestamp())
                .build();
    }

}
