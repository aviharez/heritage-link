package com.project.heritagelink.controller;

import com.project.heritagelink.dto.response.ManifestResponse;
import com.project.heritagelink.service.ManifestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manifest")
@RequiredArgsConstructor
@Tag(name = "Manifest", description = "Logistics manifest generation for professional movers")
public class ManifestController {

    private final ManifestService manifestService;

    @GetMapping
    @Operation(
            summary = "Generate mover's manifest",
            description = "Returns a structured JSON manifest containing all items designated for RELOCATION " +
                    "that are in ASSIGNED or DISPOSED status. " +
                    "Fragile items are automatically annotated with special handling instructions."
    )
    @ApiResponse(responseCode = "200", description = "Manifest generated successfully")
    public ResponseEntity<ManifestResponse> generateManifest() {
        return ResponseEntity.ok(manifestService.generateManifest());
    }

    @GetMapping("/preview")
    @Operation(
            summary = "Preview manifest (planning mode)",
            description = "Returns all items with RELOCATION disposition regardless of workflow status. " +
                    "Use this for early-stage logistics planning before all items have been assigned."
    )
    @ApiResponse(responseCode = "200", description = "Preview manifest generated successfully")
    public ResponseEntity<ManifestResponse> previewManifest() {
        return ResponseEntity.ok(manifestService.previewManifest());
    }

}
