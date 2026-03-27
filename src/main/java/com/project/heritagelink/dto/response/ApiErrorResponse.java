package com.project.heritagelink.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Structured error envelope returned for all 4xx and 5xx responses")
public class ApiErrorResponse {

    @Schema(description = "HTTP status code", example = "409")
    private int status;

    @Schema(description = "Short error classification", example = "MEDIATION_REQUIRED")
    private String error;

    @Schema(description = "Human-readable explanation", example = "Item has an active mediation conflict and cannot be updated.")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/items/5/assign")
    private String path;

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;

    @Schema(description = "Field-level validation errors (only present on 400 responses)")
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    @Schema(description = "A single field-level validation failure")
    public static class FieldError {
        private String field;
        private String rejectedValue;
        private String message;
    }

}
