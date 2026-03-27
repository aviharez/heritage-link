package com.project.heritagelink.service;

import com.project.heritagelink.dto.request.ClaimRequest;
import com.project.heritagelink.dto.request.ResolveClaimRequest;
import com.project.heritagelink.dto.response.ClaimResponse;
import com.project.heritagelink.exception.BusinessRuleException;
import com.project.heritagelink.exception.ResourceNotFoundException;
import com.project.heritagelink.model.Claim;
import com.project.heritagelink.model.Claimant;
import com.project.heritagelink.model.Item;
import com.project.heritagelink.model.enums.ClaimStatus;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimService")
class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private ClaimantService claimantService;
    @Mock private ItemService itemService;
    @Mock private AuditService auditService;

    @InjectMocks private ClaimService claimService;

    private Item highSentimentItem;
    private Item lowSentimentItem;
    private Claimant margaret;
    private Claimant thomas;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(claimService, "mediationThreshold", 7);

        highSentimentItem = Item.builder()
                .id(1L).name("Victorian Desk").roomOfOrigin("Study")
                .sentimentalScore(9).status(ItemStatus.IDENTIFIED)
                .mediationRequired(false).fragile(false).build();

        lowSentimentItem = Item.builder()
                .id(2L).name("Generic Bookshelf").roomOfOrigin("Study")
                .sentimentalScore(3).status(ItemStatus.IDENTIFIED)
                .mediationRequired(false).fragile(false).build();

        margaret = Claimant.builder().id(1L).firstName("Margaret").lastName("Sullivan")
                .relationship("Daughter").build();

        thomas = Claimant.builder().id(2L).firstName("Thomas").lastName("Sullivan")
                .relationship("Son").build();
    }

    // submit

    @Nested
    @DisplayName("submit()")
    class Submit {

        @Test
        @DisplayName("accepts the first claim without triggering mediation")
        void submit_firstClaim_noMediation() {
            when(itemService.getItem(1L)).thenReturn(highSentimentItem);
            when(claimantService.getClaimant(1L)).thenReturn(margaret);
            when(claimRepository.existsByItemIdAndClaimantIdAndStatus(1L, 1L, ClaimStatus.ACTIVE)).thenReturn(false);
            Claim saved = Claim.builder().id(10L).item(highSentimentItem).claimant(margaret)
                    .reason("My reason").status(ClaimStatus.ACTIVE).build();
            when(claimRepository.save(any())).thenReturn(saved);
            when(claimRepository.countByItemIdAndStatus(1L, ClaimStatus.ACTIVE)).thenReturn(1L);

            ClaimRequest req = buildClaimRequest(1L, 1L, "My reason");
            ClaimResponse response = claimService.submit(req);

            assertThat(response.getStatus()).isEqualTo(ClaimStatus.ACTIVE);
            assertThat(highSentimentItem.getMediationRequired()).isFalse();
            verify(itemRepository, never()).save(any());
        }

        @Test
        @DisplayName("flags mediation when second claim arrives on a high-sentiment item")
        void submit_secondClaim_highSentiment_triggersMediation() {
            when(itemService.getItem(1L)).thenReturn(highSentimentItem);
            when(claimantService.getClaimant(2L)).thenReturn(thomas);
            when(claimRepository.existsByItemIdAndClaimantIdAndStatus(1L, 2L, ClaimStatus.ACTIVE)).thenReturn(false);
            Claim saved = Claim.builder().id(11L).item(highSentimentItem).claimant(thomas)
                    .reason("Thomas reason").status(ClaimStatus.ACTIVE).build();
            when(claimRepository.save(any())).thenReturn(saved);

            when(claimRepository.countByItemIdAndStatus(1L, ClaimStatus.ACTIVE)).thenReturn(2L);
            when(itemRepository.save(any())).thenReturn(highSentimentItem);

            ClaimRequest req = buildClaimRequest(1L, 2L, "Thomas reason");
            claimService.submit(req);

            assertThat(highSentimentItem.getMediationRequired()).isTrue();
            verify(itemRepository).save(highSentimentItem);
            verify(auditService).log(any(), eq("MEDIATION_FLAGGED"), isNull(), eq("MEDIATION_REQUIRED"), anyString());
        }

        @Test
        @DisplayName("does NOT flag mediation for a second claim on a low-sentiment item")
        void submit_secondClaim_lowSentiment_noMediation() {
            when(itemService.getItem(2L)).thenReturn(lowSentimentItem);
            when(claimantService.getClaimant(2L)).thenReturn(thomas);
            when(claimRepository.existsByItemIdAndClaimantIdAndStatus(2L, 2L, ClaimStatus.ACTIVE)).thenReturn(false);
            Claim saved = Claim.builder().id(12L).item(lowSentimentItem).claimant(thomas)
                    .reason("I want it").status(ClaimStatus.ACTIVE).build();
            when(claimRepository.save(any())).thenReturn(saved);

            when(claimRepository.countByItemIdAndStatus(2L, ClaimStatus.ACTIVE)).thenReturn(2L);

            ClaimRequest req = buildClaimRequest(2L, 2L, "I want it");
            claimService.submit(req);

            assertThat(lowSentimentItem.getMediationRequired()).isFalse();
            verify(itemRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when the same claimant already has an active claim on the item")
        void submit_duplicateClaim_throws() {
            when(itemService.getItem(1L)).thenReturn(highSentimentItem);
            when(claimantService.getClaimant(1L)).thenReturn(margaret);
            when(claimRepository.existsByItemIdAndClaimantIdAndStatus(1L, 1L, ClaimStatus.ACTIVE)).thenReturn(true);

            assertThatThrownBy(() -> claimService.submit(buildClaimRequest(1L, 1L, "Again")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("already has an active claim");
        }

        @Test
        @DisplayName("throws when item is already disposed")
        void submit_disposedItem_throws() {
            highSentimentItem.setStatus(ItemStatus.DISPOSED);
            when(itemService.getItem(1L)).thenReturn(highSentimentItem);
            when(claimantService.getClaimant(1L)).thenReturn(margaret);
            when(claimRepository.existsByItemIdAndClaimantIdAndStatus(any(), any(), any())).thenReturn(false);

            assertThatThrownBy(() -> claimService.submit(buildClaimRequest(1L, 1L, "Too late")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("disposed");
        }
    }

    // resolve

    @Nested
    @DisplayName("resolve()")
    class Resolve {

        @Test
        @DisplayName("approving a claim dismisses all other active claims and clears mediation flag")
        void resolve_approve_clearsMediation() {
            highSentimentItem.setMediationRequired(true);

            Claim margaretClaim = Claim.builder().id(10L).item(highSentimentItem)
                    .claimant(margaret).reason("r1").status(ClaimStatus.ACTIVE).build();
            Claim thomasClaim = Claim.builder().id(11L).item(highSentimentItem)
                    .claimant(thomas).reason("r2").status(ClaimStatus.ACTIVE).build();

            when(claimRepository.findById(10L)).thenReturn(Optional.of(margaretClaim));
            when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            when(claimRepository.findByItemIdAndStatus(1L, ClaimStatus.ACTIVE))
                    .thenReturn(List.of(thomasClaim));

            when(claimRepository.countByItemIdAndStatus(1L, ClaimStatus.ACTIVE)).thenReturn(0L);
            when(itemRepository.save(any())).thenReturn(highSentimentItem);

            ResolveClaimRequest req = new ResolveClaimRequest();
            req.setResolution(ClaimStatus.APPROVED);
            req.setResolutionNotes("Margaret's claim accepted.");

            claimService.resolve(10L, req);

            assertThat(margaretClaim.getStatus()).isEqualTo(ClaimStatus.APPROVED);
            assertThat(thomasClaim.getStatus()).isEqualTo(ClaimStatus.DISMISSED);
            assertThat(highSentimentItem.getMediationRequired()).isFalse();
        }

        @Test
        @DisplayName("throws when claim is already resolved")
        void resolve_alreadyResolved_throws() {
            Claim approved = Claim.builder().id(10L).item(highSentimentItem)
                    .claimant(margaret).reason("r").status(ClaimStatus.APPROVED).build();
            when(claimRepository.findById(10L)).thenReturn(Optional.of(approved));

            ResolveClaimRequest req = new ResolveClaimRequest();
            req.setResolution(ClaimStatus.DISMISSED);

            assertThatThrownBy(() -> claimService.resolve(10L, req))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("already");
        }

        @Test
        @DisplayName("throws when resolution value is ACTIVE")
        void resolve_invalidResolutionActive_throws() {
            ResolveClaimRequest req = new ResolveClaimRequest();
            req.setResolution(ClaimStatus.ACTIVE);

            assertThatThrownBy(() -> claimService.resolve(10L, req))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("APPROVED or DISMISSED");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown claim")
        void resolve_notFound_throws() {
            when(claimRepository.findById(99L)).thenReturn(Optional.empty());

            ResolveClaimRequest req = new ResolveClaimRequest();
            req.setResolution(ClaimStatus.DISMISSED);

            assertThatThrownBy(() -> claimService.resolve(99L, req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // Helpers

    private ClaimRequest buildClaimRequest(Long itemId, Long claimantId, String reason) {
        ClaimRequest req = new ClaimRequest();
        req.setItemId(itemId);
        req.setClaimantId(claimantId);
        req.setReason(reason);
        return req;
    }
}
