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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimService {

    @Value("${heritagelink.mediation.sentimentail-threshold:7}")
    private int mediationThreshold;

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final ClaimantService claimantService;
    private final ItemService itemService;
    private final AuditService auditService;

    /**
     * Submits a new claim on an item.
     * If the item's sentimental score meets or exceeds the threshold and another
     * active claim already exists, the item is flagged for mediation.
     */
    public ClaimResponse submit(ClaimRequest request) {
        Item item = itemService.getItem(request.getItemId());
        Claimant claimant = claimantService.getClaimant(request.getClaimantId());

        // prevent duplicate claims from the same person
        if (claimRepository.existsByItemIdAndClaimantIdAndStatus(item.getId(), claimant.getId(), ClaimStatus.ACTIVE)) {
            throw new BusinessRuleException("DUPLICATE_CLAIM",
                    "Claimant " + claimant.getId() + " already has an active claim on item " + item.getId());
        }

        if (item.getStatus() == ItemStatus.DISPOSED) {
            throw new BusinessRuleException("ITEM_DISPOSED", "Cannot submit a claim on a disposed item.");
        }

        Claim claim = Claim.builder()
                .item(item)
                .claimant(claimant)
                .reason(request.getReason())
                .status(ClaimStatus.ACTIVE)
                .build();

        Claim saved = claimRepository.save(claim);

        // check if mediation should be triggered
        long activeClaimsAfter = claimRepository.countByItemIdAndStatus(item.getId(), ClaimStatus.ACTIVE);
        boolean isHighSentiment = item.getSentimentalScore() >= mediationThreshold;

        if (activeClaimsAfter >= 2 && isHighSentiment && !Boolean.TRUE.equals(item.getMediationRequired())) {
            item.setMediationRequired(true);
            itemRepository.save(item);
            auditService.log(item, "MEDIATION_FLAGGED", null, "MEDIATION_REQUIRED",
                    "Mediation triggered: " + activeClaimsAfter + " active claims on high-sentiment item " +
                            "(score " + item.getSentimentalScore() + " >= threshold " + mediationThreshold + ")");
        }

        auditService.logClaim(item.getId(), item.getName(), "CLAIM_SUBMITTED",
                "Claim submitted by " + claimant.getFirstName() + " " + claimant.getLastName() +
                        " (claimant #" + claimant.getId() + ")");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> findAll(Pageable pageable) {
        return claimRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> findByItemId(Long itemId, Pageable pageable) {
        itemService.getItem(itemId);
        return claimRepository.findByItemId(itemId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> findByClaimantId(Long claimantId, Pageable pageable) {
        claimantService.getClaimant(claimantId);
        return claimRepository.findByClaimantId(claimantId, pageable).map(this::toResponse);
    }

    /**
     * Resolves a claim as APPROVED or DISMISSED
     *
     * <p>If APPROVED: all other active claims on the same item are automatically dismissed
     * and the mediation flag is cleared
     *
     * <p>If DISMISSED: the mediation flag is cleared once fewer than 2 active claims remain.
     */
    public ClaimResponse resolve(Long claimId, ResolveClaimRequest request) {
        if (request.getResolution() == ClaimStatus.ACTIVE) {
            throw new BusinessRuleException("INVALID_RESOLUTION", "Resolution must APPROVED or DISMISSED, not ACTIVE.");
        }

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId));

        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            throw new BusinessRuleException("CLAIM_ALREADY_RESOLVED", "Claim " + claimId + " is already " + claim.getStatus() + " and cannot be resolved again.");
        }

        Item item = claim.getItem();

        claim.setStatus(request.getResolution());
        claim.setResolutionNotes(request.getResolutionNotes());
        claim.setResolvedAt(LocalDateTime.now());
        claimRepository.save(claim);

        if (request.getResolution() == ClaimStatus.APPROVED) {
            // dismiss all other active claims automatically
            List<Claim> otherActive = claimRepository.findByItemIdAndStatus(item.getId(), ClaimStatus.ACTIVE);
            for (Claim other : otherActive) {
                other.setStatus(ClaimStatus.DISMISSED);
                other.setResolutionNotes("Auto-dismissed: another claim was approved.");
                other.setResolvedAt(LocalDateTime.now());
                claimRepository.save(other);
            }
            clearMediationIfResolved(item);

            auditService.log(item, "CLAIM_APPROVED", "MEDIATION_REQUIRED", item.getStatus().name(),
                    "Claim #" + claimId + " approved. All competing claims dismissed.");
        } else {
            // DISMISSED: clear mediation flag if fewer than 2 active claims remain
            clearMediationIfResolved(item);

            auditService.log(item, "CLAIM_DISMISSED", null, item.getStatus().name(),
                    "Claim #" + claimId + " dismissed. Notes: " + request.getResolutionNotes());
        }

        return toResponse(claim);
    }

    // Helpers

    private void clearMediationIfResolved(Item item) {
        long remaining = claimRepository.countByItemIdAndStatus(item.getId(), ClaimStatus.ACTIVE);
        if (remaining < 2 && Boolean.TRUE.equals(item.getMediationRequired())) {
            item.setMediationRequired(false);
            itemRepository.save(item);
        }
    }

    public ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .itemId(claim.getItem().getId())
                .itemName(claim.getItem().getName())
                .claimantId(claim.getClaimant().getId())
                .claimantName(claim.getClaimant().getFirstName() + " " + claim.getClaimant().getLastName())
                .reason(claim.getReason())
                .status(claim.getStatus())
                .resolutionNotes(claim.getResolutionNotes())
                .claimedAt(claim.getClaimedAt())
                .resolvedAt(claim.getResolvedAt())
                .build();
    }

}
