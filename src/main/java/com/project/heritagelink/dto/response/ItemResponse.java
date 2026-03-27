package com.project.heritagelink.dto.response;

import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Full representation of an inventory item")
public class ItemResponse {

    private Long id;
    private String name;
    private String description;
    private String roomOfOrigin;
    private Double widthCm;
    private Double heightCm;
    private Double depthCm;
    private BigDecimal estimatedValue;
    private Integer sentimentalScore;
    private ItemStatus status;
    private DispositionType dispositionType;
    private Boolean fragile;
    private String specialHandlingNotes;

    @Schema(description = "True when two or more active claims exist on a high-sentiment item and manual resolution is required")
    private Boolean mediationRequired;

    @Schema(description = "Number of active claims currently on this item")
    private Long activeClaimCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
