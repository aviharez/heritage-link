package com.project.heritagelink.service;

import com.project.heritagelink.dto.request.AppraiseRequest;
import com.project.heritagelink.dto.request.AssignRequest;
import com.project.heritagelink.dto.request.ItemRequest;
import com.project.heritagelink.dto.response.ItemResponse;
import com.project.heritagelink.exception.BusinessRuleException;
import com.project.heritagelink.model.Item;
import com.project.heritagelink.model.enums.ClaimStatus;
import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import com.project.heritagelink.repository.ClaimRepository;
import com.project.heritagelink.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService")
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private ClaimRepository claimRepository;
    @Mock private AuditService auditService;

    @InjectMocks private ItemService itemService;

    private Item identifiedItem;
    private Item appraisedItem;
    private Item assignedItem;

    @BeforeEach
    void setUp() {
        identifiedItem = Item.builder()
                .id(1L).name("Victorian Desk")
                .roomOfOrigin("Study").sentimentalScore(5)
                .status(ItemStatus.IDENTIFIED).mediationRequired(false)
                .fragile(false).build();

        appraisedItem = Item.builder()
                .id(2L).name("Grandfather Clock")
                .roomOfOrigin("Living Room").sentimentalScore(7)
                .estimatedValue(new BigDecimal("4200.00"))
                .status(ItemStatus.APPRAISED).mediationRequired(false)
                .fragile(true).build();

        assignedItem = Item.builder()
                .id(3L).name("Persian Rug")
                .roomOfOrigin("Dining Room").sentimentalScore(4)
                .estimatedValue(new BigDecimal("3800.00"))
                .status(ItemStatus.ASSIGNED).dispositionType(DispositionType.RELOCATION)
                .mediationRequired(false).fragile(false).build();
    }

    // create

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("persists item with IDENTIFIED status")
        void create_success() {
            ItemRequest req = buildItemRequest("Victorian Desk", "Study", 5, false, null);
            when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
                Item saved = inv.getArgument(0);
                saved = Item.builder()
                        .id(10L).name(saved.getName()).roomOfOrigin(saved.getRoomOfOrigin())
                        .sentimentalScore(saved.getSentimentalScore()).status(ItemStatus.IDENTIFIED)
                        .mediationRequired(false).fragile(false).build();
                return saved;
            });
            when(claimRepository.countByItemIdAndStatus(anyLong(), eq(ClaimStatus.ACTIVE))).thenReturn(0L);

            ItemResponse response = itemService.create(req);

            assertThat(response.getStatus()).isEqualTo(ItemStatus.IDENTIFIED);
            assertThat(response.getName()).isEqualTo("Victorian Desk");
            verify(auditService).log(any(), eq("ITEM_CREATED"), isNull(), eq("IDENTIFIED"), anyString());
        }

    }

    // appraise

    @Nested
    @DisplayName("appraise()")
    class Appraise {

        @Test
        @DisplayName("advances IDENTIFIED item to APPRAISED and records value")
        void appraise_success() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(identifiedItem));
            when(itemRepository.save(any(Item.class))).thenReturn(identifiedItem);
            when(claimRepository.countByItemIdAndStatus(anyLong(), any())).thenReturn(0L);

            AppraiseRequest req = new AppraiseRequest();
            req.setEstimatedValue(new BigDecimal("2500.00"));

            ItemResponse response = itemService.appraise(1L, req);

            assertThat(identifiedItem.getStatus()).isEqualTo(ItemStatus.APPRAISED);
            assertThat(identifiedItem.getEstimatedValue()).isEqualByComparingTo("2500.00");
            verify(auditService).log(any(), eq("STATUS_CHANGE"), eq("IDENTIFIED"), eq("APPRAISED"), anyString());
        }

        @Test
        @DisplayName("throws when items is not in IDENTIFIED status")
        void appraise_wrongStatus() {
            when(itemRepository.findById(2L)).thenReturn(Optional.of(appraisedItem));

            AppraiseRequest req = new AppraiseRequest();
            req.setEstimatedValue(new BigDecimal("100.00"));

            assertThatThrownBy(() -> itemService.appraise(2L, req))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("APPRAISED");
        }

    }

    // assign

    @Nested
    @DisplayName("assign()")
    class Assign {

        @Test
        @DisplayName("advances APPRAISED item to ASSIGNED with valid RELOCATION disposition")
        void assign_relocation_success() {
            when(itemRepository.findById(2L)).thenReturn(Optional.of(appraisedItem));
            when(itemRepository.save(any(Item.class))).thenReturn(appraisedItem);
            when(claimRepository.countByItemIdAndStatus(anyLong(), any())).thenReturn(0L);

            AssignRequest req = new AssignRequest();
            req.setDispositionType(DispositionType.RELOCATION);

            ItemResponse response = itemService.assign(2L, req);

            assertThat(appraisedItem.getStatus()).isEqualTo(ItemStatus.ASSIGNED);
            assertThat(appraisedItem.getDispositionType()).isEqualTo(DispositionType.RELOCATION);
        }

        @Test
        @DisplayName("advances APPRAISED item to ASSIGNED with SALE when appraisal value > 0")
        void assign_sale_withValue_success() {
            when(itemRepository.findById(2L)).thenReturn(Optional.of(appraisedItem));
            when(itemRepository.save(any(Item.class))).thenReturn(appraisedItem);
            when(claimRepository.countByItemIdAndStatus(anyLong(), any())).thenReturn(0L);

            AssignRequest req = new AssignRequest();
            req.setDispositionType(DispositionType.SALE);

            assertThatCode(() -> itemService.assign(2L, req)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when SALE disposition is requested but estimated value is zero")
        void assign_sale_zeroValue_throws() {
            appraisedItem.setEstimatedValue(BigDecimal.ZERO);
            when(itemRepository.findById(2L)).thenReturn(Optional.of(appraisedItem));

            AssignRequest req = new AssignRequest();
            req.setDispositionType(DispositionType.SALE);

            assertThatThrownBy(() -> itemService.assign(2L, req))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("SALE")
                    .hasMessageContaining("$0");
        }

    }

    // dispose

    @Nested
    @DisplayName("dispose()")
    class Dispose {

        @Test
        @DisplayName("advances ASSIGNED item to DISPOSED")
        void dispose_success() {
            when(itemRepository.findById(3L)).thenReturn(Optional.of(assignedItem));
            when(itemRepository.save(any(Item.class))).thenReturn(assignedItem);
            when(claimRepository.countByItemIdAndStatus(anyLong(), any())).thenReturn(0L);

            itemService.dispose(3L);

            assertThat(assignedItem.getStatus()).isEqualTo(ItemStatus.DISPOSED);
            verify(auditService).log(any(), eq("STATUS_CHANGE"), eq("ASSIGNED"), eq("DISPOSED"), anyString());
        }

        @Test
        @DisplayName("throws when item is not in ASSIGNED status")
        void dispose_wrongStatus() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(identifiedItem));

            assertThatThrownBy(() -> itemService.dispose(1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("IDENTIFIED");
        }

    }

    // delete

    @Nested
    @DisplayName("delete()")
    class Delete {
        @Test
        @DisplayName("deletes item in IDENTIFIED status with no active claims")
        void delete_success() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(identifiedItem));
            when(claimRepository.countByItemIdAndStatus(1L, ClaimStatus.ACTIVE)).thenReturn(0L);

            assertThatCode(() -> itemService.delete(1L)).doesNotThrowAnyException();
            verify(itemRepository).delete(identifiedItem);
        }

        @Test
        @DisplayName("throws when items is not in IDENTIFIED status")
        void delete_wrongStatus() {
            when(itemRepository.findById(2L)).thenReturn(Optional.of(appraisedItem));

            assertThatThrownBy(() -> itemService.delete(2L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("APPRAISED");
        }
    }

    // Helpers

    private ItemRequest buildItemRequest(String name, String room, int score, boolean fragile, BigDecimal value) {
        ItemRequest req = new ItemRequest();
        req.setName(name);
        req.setRoomOfOrigin(room);
        req.setSentimentalScore(score);
        req.setFragile(fragile);
        req.setEstimatedValue(value);
        return req;
    }

}
