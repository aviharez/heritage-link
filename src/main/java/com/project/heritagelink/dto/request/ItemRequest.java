package com.project.heritagelink.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Payload to create or update an inventory item")
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 200)
    @Schema(description = "Descriptve name of the item", example = "Victorian Writing Desk")
    private String name;

    @Schema(description = "Additional notes or history about the item", example = "Solid walnut, circa 1890. Family acquired it from an estate sale.")
    private String description;

    @NotBlank(message = "Room of origin is required")
    @Size(max = 100)
    @Schema(description = "Room where the item currently resides", example = "Study")
    private String roomOfOrigin;

    @Positive
    @Schema(description = "Width of the item in centimetres", example = "120.0")
    private Double widthCm;

    @Positive
    @Schema(description = "Height of the item in centimetres", example = "75.0")
    private Double heightCm;

    @Positive
    @Schema(description = "Depth of the item in centimetres", example = "60.0")
    private Double depthCm;

    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "Estimated monetary value in USD", example ="2500.00")
    private BigDecimal estimatedValue;

    @NotNull(message = "Sentimental score is required")
    @Min(value = 1, message = "Sentimental score must be between 1 and 10")
    @Max(value = 10, message = "Sentimental score must be between 1 and 10")
    @Schema(description = "Family sentimental importance score (1 = low, 10 = priceless)", example = "8")
    private Integer sentimentalScore;

    @Schema(description = "Whether the item requires careful handling", example = "true")
    private Boolean fragile = false;

    @Size(max = 500)
    @Schema(description = "Special instructions for handlers or movers", example = "Do not stack. Keep upright at all times.")
    private String specialHandlingNotes;

}
