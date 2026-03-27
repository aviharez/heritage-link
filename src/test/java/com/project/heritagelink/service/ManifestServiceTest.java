package com.project.heritagelink.service;

import com.project.heritagelink.dto.response.ManifestResponse;
import com.project.heritagelink.model.Item;
import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import com.project.heritagelink.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManifestService")
class ManifestServiceTest {

    @Mock private ItemRepository itemRepository;

    @InjectMocks private ManifestService manifestService;

    // generateManifest

    @Nested
    @DisplayName("generateManifest()")
    class GenerateManifest {

        @Test
        @DisplayName("returns only RELOCATION items in ASSIGNED or DISPOSED status")
        void manifest_onlyRelocationAssignedOrDisposed() {
            List<Item> allItems = List.of(
                    makeItem(1L, "Clock", DispositionType.RELOCATION, ItemStatus.ASSIGNED, false, "2000.00"),
                    makeItem(2L, "Portrait", DispositionType.RELOCATION, ItemStatus.DISPOSED, false, "900.00"),
                    makeItem(3L, "Rug", DispositionType.SALE, ItemStatus.ASSIGNED, false, "3800.00"),
                    makeItem(4L, "Bible", DispositionType.GIFTING, ItemStatus.ASSIGNED, false, "150.00"),
                    makeItem(5L, "Desk", DispositionType.RELOCATION, ItemStatus.IDENTIFIED, false, null)
            );
            when(itemRepository.findAll()).thenReturn(allItems);

            ManifestResponse manifest = manifestService.generateManifest();

            assertThat(manifest.getTotalItems()).isEqualTo(2);
            assertThat(manifest.getItems())
                    .extracting("itemId")
                    .containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("returns empty manifest when no relocation items are assigned")
        void manifest_empty_whenNoRelocationItems() {
            List<Item> allItems = List.of(
                    makeItem(1L, "Chair", DispositionType.DONATION, ItemStatus.ASSIGNED, false, "200.00")
            );
            when(itemRepository.findAll()).thenReturn(allItems);

            ManifestResponse manifest = manifestService.generateManifest();

            assertThat(manifest.getTotalItems()).isEqualTo(0);
            assertThat(manifest.getItems()).isEmpty();
        }

        @Test
        @DisplayName("counts fragile items correctly")
        void manifest_fragileItemCount() {
            List<Item> allItems = List.of(
                    makeItem(1L, "Fragile Lamp", DispositionType.RELOCATION, ItemStatus.ASSIGNED, true, "500.00"),
                    makeItem(2L, "Sturdy Table", DispositionType.RELOCATION, ItemStatus.ASSIGNED, false, "800.00"),
                    makeItem(3L, "Fragile Mirror", DispositionType.RELOCATION, ItemStatus.DISPOSED, true, "300.00")
            );
            when(itemRepository.findAll()).thenReturn(allItems);

            ManifestResponse manifest = manifestService.generateManifest();

            assertThat(manifest.getFragileItemCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("prepends FRAGILE warning to special handling notes for fragile items")
        void manifest_fragileItemHasHandlingWarning() {
            List<Item> allItems = List.of(
                    makeItem(1L, "Porcelain Vase", DispositionType.RELOCATION, ItemStatus.ASSIGNED, true, "600.00")
            );
            allItems.get(0).setSpecialHandlingNotes("Keep upright.");
            when(itemRepository.findAll()).thenReturn(allItems);

            ManifestResponse manifest = manifestService.generateManifest();

            String notes = manifest.getItems().get(0).getSpecialHandlingNotes();
            assertThat(notes).startsWith("[FRAGILE - HANDLE WITH EXTREME CARE]");
            assertThat(notes).contains("Keep upright.");
        }

        @Test
        @DisplayName("sums estimated values correctly, ignoring nulls")
        void manifest_totalEstimatedValue() {
            List<Item> allItems = List.of(
                    makeItem(1L, "A", DispositionType.RELOCATION, ItemStatus.ASSIGNED, false, "1000.00"),
                    makeItem(2L, "B", DispositionType.RELOCATION, ItemStatus.ASSIGNED, false, "2500.00"),
                    makeItem(3L, "C", DispositionType.RELOCATION, ItemStatus.ASSIGNED, false, null)
            );
            when(itemRepository.findAll()).thenReturn(allItems);

            ManifestResponse manifest = manifestService.generateManifest();

            assertThat(manifest.getTotalEstimatedValue()).isEqualByComparingTo("3500.00");
        }
    }

    // previewManifest

    @Nested
    @DisplayName("previewManifest()")
    class PreviewManifest {

        @Test
        @DisplayName("includes ALL relocation items regardless of status")
        void preview_includesAllRelocationStatuses() {
            List<Item> allItems = List.of(
                    makeItem(1L, "Bed", DispositionType.RELOCATION, ItemStatus.IDENTIFIED, false, null),
                    makeItem(2L, "Clock", DispositionType.RELOCATION, ItemStatus.APPRAISED, false, "4000.00"),
                    makeItem(3L, "Rug", DispositionType.RELOCATION, ItemStatus.ASSIGNED, false, "3800.00"),
                    makeItem(4L, "Trunk", DispositionType.SALE, ItemStatus.ASSIGNED, false, "18000.00")
            );
            when(itemRepository.findAll()).thenReturn(allItems);

            ManifestResponse preview = manifestService.previewManifest();

            assertThat(preview.getTotalItems()).isEqualTo(3);
            assertThat(preview.getItems()).extracting("itemId").containsExactlyInAnyOrder(1L, 2L, 3L);
        }
    }

    // Helpers

    private Item makeItem(Long id, String name, DispositionType dtype,
                          ItemStatus status, boolean fragile, String value) {
        return Item.builder()
                .id(id).name(name).roomOfOrigin("Test Room")
                .sentimentalScore(5).fragile(fragile)
                .dispositionType(dtype).status(status).mediationRequired(false)
                .estimatedValue(value != null ? new BigDecimal(value) : null)
                .build();
    }
}

