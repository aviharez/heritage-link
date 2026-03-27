package com.project.heritagelink.controller;

import com.project.heritagelink.dto.request.ClaimantRequest;
import com.project.heritagelink.dto.response.ClaimantResponse;
import com.project.heritagelink.service.ClaimantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claimant")
@RequiredArgsConstructor
@Tag(name = "Claimants", description = "Family member and beneficiary management")
public class ClaimantController {

    private final ClaimantService claimantService;

    @PostMapping
    @Operation(summary = "Register a claimant",
            description = "Registers a family member or beneficiary who may submit claims on inventory items.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Claimant registered"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "422", description = "Email already registered")
    })
    public ResponseEntity<ClaimantResponse> create(@Valid @RequestBody ClaimantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimantService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all claimants",
            description = "Returns a paginated list of all registered family members. " +
                    "Pagination: `?page=0&size=20&sort=lastName,asc`")
    public ResponseEntity<Page<ClaimantResponse>> findAll(
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(claimantService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claimant by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claimant found"),
            @ApiResponse(responseCode = "404", description = "Claimant not found")
    })
    public ResponseEntity<ClaimantResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(claimantService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update claimant information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claimant updated"),
            @ApiResponse(responseCode = "404", description = "Claimant not found"),
            @ApiResponse(responseCode = "422", description = "Email conflict")
    })
    public ResponseEntity<ClaimantResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ClaimantRequest request) {
        return ResponseEntity.ok(claimantService.update(id, request));
    }

}
