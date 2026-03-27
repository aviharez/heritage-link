package com.project.heritagelink.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Registered family member or beneficiary")
public class ClaimantResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String relationship;
    private LocalDateTime createdAt;

}
