package com.project.heritagelink.service;

import com.project.heritagelink.dto.response.ManifestItemResponse;
import com.project.heritagelink.dto.response.ManifestResponse;
import com.project.heritagelink.model.Item;
import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import com.project.heritagelink.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManifestService {

    private final ItemRepository itemRepository;

    /**
     * Generates the full mover's manifest for all items designated for RELOCATION
     * that are in ASSIGNED or DISPOSED status.
     */
    @Transactional(readOnly = true)
    public ManifestResponse generateManifest() {
        List<Item> relocationItems = itemRepository.findAll().stream()
                .filter(item -> item.getDispositionType() == DispositionType.RELOCATION)
                .filter(item -> item.getStatus() == ItemStatus.ASSIGNED || item.getStatus() == ItemStatus.DISPOSED)
                .toList();

        return buildManifest(relocationItems);
    }

    /**
     * Preview manifest includes ALL items with RELOCATION disposition regardless of workflow status.
     * Useful for early planning before items have been fully appraised and assigned.
     */
    @Transactional(readOnly = true)
    public ManifestResponse previewManifest() {
        List<Item> relocationItems = itemRepository.findAll().stream()
                .filter(item -> item.getDispositionType() == DispositionType.RELOCATION)
                .toList();

        return buildManifest(relocationItems);
    }

    // Helpers

    private ManifestResponse buildManifest(List<Item> items) {
        List<ManifestItemResponse> manifestItems = items.stream()
                .map(this::toManifestItem)
                .toList();

        int fragileCount = (int) items.stream().filter(i -> Boolean.TRUE.equals(i.getFragile())).count();

        BigDecimal totalValue = items.stream()
                .filter(i -> i.getEstimatedValue() != null)
                .map(Item::getEstimatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ManifestResponse.builder()
                .generatedAt(LocalDateTime.now())
                .totalItems(items.size())
                .fragileItemCount(fragileCount)
                .totalEstimatedValue(totalValue)
                .items(manifestItems)
                .build();
    }

    private ManifestItemResponse toManifestItem(Item item) {
        ManifestItemResponse.DimensionsDto dimensions = null;
        if (item.getWidthCm() != null || item.getHeightCm() != null || item.getDepthCm() != null) {
            dimensions = ManifestItemResponse.DimensionsDto.builder()
                    .widthCm(item.getWidthCm())
                    .heightCm(item.getHeightCm())
                    .depthCm(item.getDepthCm())
                    .build();
        }

        String handlingNotes = buildHandlingNotes(item);

        return ManifestItemResponse.builder()
                .itemId(item.getId())
                .name(item.getName())
                .roomOfOrigin(item.getRoomOfOrigin())
                .dimensions(dimensions)
                .estimatedValue(item.getEstimatedValue())
                .fragile(item.getFragile())
                .specialHandlingNotes(handlingNotes)
                .status(item.getStatus().name())
                .build();
    }

    /**
     * Builds final handling instructions, prepending a FRAGILE warning for delicate items.
     */
    private String buildHandlingNotes(Item item) {
        StringBuilder sb = new StringBuilder();

        if (Boolean.TRUE.equals(item.getFragile())) {
            sb.append("[FRAGILE - HANDLE WITH EXTREME CARE] ");
        }

        if (item.getSpecialHandlingNotes() != null && !item.getSpecialHandlingNotes().isBlank()) {
            sb.append(item.getSpecialHandlingNotes());
        }

        return sb.isEmpty() ? null : sb.toString().trim();
    }

}
