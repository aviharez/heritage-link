package com.project.heritagelink.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "A single item entry in the mover's relocation manifest")
public class ManifestItemResponse {

    private Long itemId;
    private String name;
    private String roomOfOrigin;

    @Schema(description = "Physical dimensions of the item")
    private DimensionsDto dimensions;

    private BigDecimal estimatedValue;
    private Boolean fragile;
    private String specialHandlingNotes;
    private String status;

    @Data
    @Builder
    @Schema(description = "Physical dimensions in centimetres")
    public static class DimensionsDto {
        private Double widthCm;
        private Double heightCm;
        private Double depthCm;
    }

}
