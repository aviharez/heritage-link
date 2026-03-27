package com.project.heritagelink.controller;

import com.project.heritagelink.dto.response.AuditLogResponse;
import com.project.heritagelink.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Immutable audit trail for all item state changes")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    @Operation(
            summary = "List all audit logs",
            description = "Returns a paginated audit trail across the entire system in reverse chronological order. " +
                    "Each record includes the authenticated user who performed the action. " +
                    "Pagination: `?page=0&size=20&sort=timestamp,desc`"
    )
    @ApiResponse(responseCode = "200", description = "Audit logs returned")
    public ResponseEntity<Page<AuditLogResponse>> findAll(
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(auditService.findAll(pageable));
    }

    @GetMapping("/item/{itemId}")
    @Operation(
            summary = "Get audit trail for a specific item",
            description = "Returns all audit records for the given item in reverse chronological order, " +
                    "showing the complete history of who changed what and when."
    )
    @ApiResponse(responseCode = "200", description = "Audit trail returned")
    public ResponseEntity<Page<AuditLogResponse>> findByItem(
            @PathVariable Long itemId,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(auditService.findByItemId(itemId, pageable));
    }

}
