package com.project.heritagelink.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Payload to register a family member or beneficiary as claimant")
public class ClaimantRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Schema(description = "First name", example = "Margaret")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Schema(description = "Last name", example = "Sullivan")
    private String lastName;

    @Email(message = "Must be a valid email address")
    @Schema(description = "Contact email (must be unique)", example = "margaret.sullivan@example.com")
    private String email;

    @Schema(description = "Contact phone number", example = "+1-222-3333")
    private String phone;

    @NotBlank(message = "Relationship to client is required")
    @Size(max = 100)
    @Schema(description = "Relationship to the primary client", example = "Daughter")
    private String relationship;

}
