package com.project.heritagelink.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "An immutable audit record for a state change event")
public class AuditLogResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private String action;
    private String previousState;
    private String newState;
    private String details;
    private String performedBy;
    private LocalDateTime timestamp;

}
