package com.project.heritagelink.controller;

import com.project.heritagelink.dto.request.AppraiseRequest;
import com.project.heritagelink.dto.request.AssignRequest;
import com.project.heritagelink.dto.request.ItemRequest;
import com.project.heritagelink.dto.response.ItemResponse;
import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import com.project.heritagelink.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Inventory item management and lifecycle workflow")
public class ItemController {

    private final ItemService itemService;

    // CRUD

    @PostMapping
    @Operation(summary = "Creates a new inventory item in IDENTIFIED status.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required for write operations")
    })
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all items",
            description = "Returns a paginated list of inventory items. " +
                    "Supports optional filtering by status, disposition type, and room. " +
                    "Pagination: `?page=0&size=20&sort=createdAt,desc`")
    public ResponseEntity<Page<ItemResponse>> findAll(
            @Parameter(description = "Filter by workflow status")
            @RequestParam(required = false) ItemStatus status,
            @Parameter(description = "Filter by disposition type")
            @RequestParam(required = false) DispositionType dispositionType,
            @Parameter(description = "Filter by room of origin (case-insensitive)")
            @RequestParam(required = false) String room,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(itemService.findAll(status, dispositionType, room, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<ItemResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item metadata",
            description = "Updates descriptive field. Cannot update a DISPOSED item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated"),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "422", description = "Item is disposed and cannot be updated")
    })
    public ResponseEntity<ItemResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an item", description = "Delete an item. Only permitted when status is IDENTIFIED and no active claims exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item deleted"),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "422", description = "Item cannot be deleted in its current state")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Disposition Workflow

    @PutMapping("/{id}/appraise")
    @Operation(summary = "Appraise an item",
            description = "Records the assessed monetary value and advances the item from IDENTIFIED to APPRAISED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item appraised"),
            @ApiResponse(responseCode = "409", description = "Item has an active mediation conflict"),
            @ApiResponse(responseCode = "422", description = "Invalid transition. Item is not in IDENTIFIED status")
    })
    public ResponseEntity<ItemResponse> appraise(
            @PathVariable Long id,
            @Valid @RequestBody AppraiseRequest request) {
        return ResponseEntity.ok(itemService.appraise(id, request));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign disposition",
            description = "Sets the final disposition type and advances the item from APPRAISED to ASSIGNED. " +
                    "SALE disposition requires a verified appraisal value > $0.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disposition assigned"),
            @ApiResponse(responseCode = "409", description = "Item has an active mediation conflict"),
            @ApiResponse(responseCode = "422", description = "Invalid transition or SALE without appraisal value")
    })
    public ResponseEntity<ItemResponse> assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignRequest request) {
        return ResponseEntity.ok(itemService.assign(id, request));
    }

    @PutMapping("/{id}/dispose")
    @Operation(summary = "Mark item as disposed", description = "Advances an ASSIGNED item to the final DISPOSED state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item disposed"),
            @ApiResponse(responseCode = "409", description = "Item has an active mediation conflict"),
            @ApiResponse(responseCode = "422", description = "Invalid transition. Item is not in ASSIGNED status")
    })
    public ResponseEntity<ItemResponse> dispose(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.dispose(id));
    }

}
