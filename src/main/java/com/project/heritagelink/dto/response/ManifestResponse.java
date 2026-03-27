package com.project.heritagelink.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Structured manifest for professional movers covering all items designed for Relocation")
public class ManifestResponse {

    @Schema(description = "Timestamp when this manifest was generated")
    private LocalDateTime generatedAt;

    @Schema(description = "Total number of relocation items in this manifest")
    private int totalItems;

    @Schema(description = "Number of items requiring special fragile handling")
    private int fragileItemCount;

    @Schema(description = "Sum of estimated values for all items in this manifest")
    private BigDecimal totalEstimatedValue;

    @Schema(description = "Ordered list of items to be relocated")
    private List<ManifestItemResponse> items;

}
