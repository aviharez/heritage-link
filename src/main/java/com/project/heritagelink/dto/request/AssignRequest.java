package com.project.heritagelink.dto.request;

import com.project.heritagelink.model.enums.DispositionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Payload to assign a final disposition type to an appraised item")
public class AssignRequest {

    @NotNull(message = "Disposition type is required")
    @Schema(description = "How the item will be handled at the end of the transition",
            example = "RELOCATION",
            allowableValues = {"GIFTING", "DONATION", "SALE", "RELOCATION"})
    private DispositionType dispositionType;

    @Schema(description = "Optional notes supporting the assignment decision", example = "Client's daughter specifically requested this item be moved with her")
    private String assignmentNotes;

}
