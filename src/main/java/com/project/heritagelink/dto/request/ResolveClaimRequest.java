package com.project.heritagelink.dto.request;

import com.project.heritagelink.model.enums.ClaimStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Payload to resolve (approve or dismiss) a claim")
public class ResolveClaimRequest {

    @NotNull(message = "Resolution is required")
    @Schema(description = "Resolution decision. Must be APPROVED ir DISMISSED.",
            example = "APPROVED",
            allowableValues = {"APPROVED", "DISMISSED"})
    private ClaimStatus resolution;

    @Schema(description = "Notes documenting the rationale for this resolution",
            example = "Family mediation session held on 2026-03-10. Margaret's claim was supported by two other family members.")
    private String resolutionNotes;

}
