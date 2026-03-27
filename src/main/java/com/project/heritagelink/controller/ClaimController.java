package com.project.heritagelink.controller;

import com.project.heritagelink.dto.request.ClaimRequest;
import com.project.heritagelink.dto.request.ResolveClaimRequest;
import com.project.heritagelink.dto.response.ClaimResponse;
import com.project.heritagelink.service.ClaimService;
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
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Heirloom claim submission and conflict resolution")
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @Operation(summary = "Submit a claim",
            description = "Registers a family member's claim on an item. " +
                    "If the item's sentimental score >= 7 and another active claim already exists, " +
                    "the item is automatically flagged as MEDIATION_REQUIRED.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Claim submitted"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Item or claimant not found"),
            @ApiResponse(responseCode = "422", description = "Duplicate claim or item is disposed")
    })
    public ResponseEntity<ClaimResponse> submit(@Valid @RequestBody ClaimRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.submit(request));
    }

    @GetMapping
    @Operation(summary = "List all claims",
            description = "Returns a paginated list of all claims across all items. " +
                    "Pagination: `?page=0&size=20&sort=claimedAt,desc`")
    public ResponseEntity<Page<ClaimResponse>> findAll(
            @PageableDefault(size = 20, sort = "claimedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(claimService.findAll(pageable));
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "Get all claims for an item",
            description = "Returns a paginated list of every claim (all statuses) submitted for the specified item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claims returned"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Page<ClaimResponse>> findByItem(
            @PathVariable Long itemId,
            @PageableDefault(size = 20, sort = "claimedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(claimService.findByItemId(itemId, pageable));
    }

    @GetMapping("/claimant/{claimantId}")
    @Operation(summary = "Get all claims by a claimant",
            description = "Returns a paginated list of all claims submitted by the specified family member.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claims returned"),
            @ApiResponse(responseCode = "404", description = "Claimant not found")
    })
    public ResponseEntity<Page<ClaimResponse>> findByClaimant(
            @PathVariable Long claimantId,
            @PageableDefault(size = 20, sort = "claimedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(claimService.findByClaimantId(claimantId, pageable));
    }

    @PutMapping("/{id}/resolve")
    @Operation(summary = "Resolve a claim",
            description = "Approves or dismisses a claim. " +
                    "Approving automatically dismisses all other active claims on the same item and clears the mediation flag. " +
                    "Dismissing clears the mediation flag once fewer than 2 active claims remain.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim resolved"),
            @ApiResponse(responseCode = "404", description = "Claim not found"),
            @ApiResponse(responseCode = "422", description = "Claim is already resolved or resolution value is invalid")
    })
    public ResponseEntity<ClaimResponse> resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveClaimRequest request) {
        return ResponseEntity.ok(claimService.resolve(id, request));
    }

}
