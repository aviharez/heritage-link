package com.project.heritagelink.dto.response;

import com.project.heritagelink.model.enums.ClaimStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "A family member's claim on an inventory item")
public class ClaimResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private Long claimantId;
    private String claimantName;
    private String reason;
    private ClaimStatus status;
    private String resolutionNotes;
    private LocalDateTime claimedAt;
    private LocalDateTime resolvedAt;

}
