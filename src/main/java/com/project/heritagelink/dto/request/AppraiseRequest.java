package com.project.heritagelink.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Payload to record an appraisal value and advance the item to APPRAISED status")
public class AppraiseRequest {

    @NotNull(message = "Estimated value is required for appraisal")
    @DecimalMin(value = "0.00", inclusive = true, message = "Estimated value cannot be negative")
    @Schema(description = "Professionally assessed value of the item in USD", example = "2500.00")
    private BigDecimal estimatedValue;

    @Schema(description = "Optional notes from the appraiser", example = "Verified by Judge's. Authenticated Victorian period, circa 1885.")
    private String appraisalNotes;

}
