package com.project.heritagelink.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Payload to submit a claim on an inventory item")
public class ClaimRequest {

    @NotNull(message = "Item ID is required")
    @Schema(description = "ID of the item being claimed", example = "5")
    private Long itemId;

    @NotNull(message = "Claimant ID is required")
    @Schema(description = "ID of the claimant submitting the claim", example = "2")
    private Long claimantId;

    @NotBlank(message = "Reason for the claim is required")
    @Size(max = 1000)
    @Schema(description = "The claimant's stated reason for wanting the item",
            example = "This writingg desk belonged to my grandmother. I have childhood memories of sitting next to her while she srote letters")
    private String reason;

}
