package com.project.heritagelink.service;

import com.project.heritagelink.dto.request.AppraiseRequest;
import com.project.heritagelink.dto.request.AssignRequest;
import com.project.heritagelink.dto.request.ItemRequest;
import com.project.heritagelink.dto.response.ItemResponse;
import com.project.heritagelink.exception.BusinessRuleException;
import com.project.heritagelink.exception.MediationRequiredException;
import com.project.heritagelink.exception.ResourceNotFoundException;
import com.project.heritagelink.model.Item;
import com.project.heritagelink.model.enums.ClaimStatus;
import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import com.project.heritagelink.repository.ClaimRepository;
import com.project.heritagelink.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final AuditService auditService;

    // CRUD

    public ItemResponse create(ItemRequest request) {
        Item item = Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .roomOfOrigin(request.getRoomOfOrigin())
                .widthCm(request.getWidthCm())
                .heightCm(request.getHeightCm())
                .depthCm(request.getDepthCm())
                .estimatedValue(request.getEstimatedValue())
                .sentimentalScore(request.getSentimentalScore())
                .fragile(request.getFragile() != null ? request.getFragile() : false)
                .specialHandlingNotes(request.getSpecialHandlingNotes())
                .status(ItemStatus.IDENTIFIED)
                .mediationRequired(false)
                .build();

        Item saved = itemRepository.save(item);

        auditService.log(saved, "ITEM_CREATED", null, ItemStatus.IDENTIFIED.name(), "Item catalogued in room: " + saved.getRoomOfOrigin());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ItemResponse findById(Long id) {
        return toResponse(getItem(id));
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> findAll(ItemStatus status, DispositionType dispositionType, String room, Pageable pageable) {
        Page<Item> items;

        if (status != null && dispositionType != null) {
            items = itemRepository.findByStatusAndDispositionType(status, dispositionType, pageable);
        } else if (status != null) {
            items = itemRepository.findByStatus(status, pageable);
        } else if (dispositionType != null) {
            items = itemRepository.findByDispositionType(dispositionType, pageable);
        } else if (room != null && !room.isBlank()) {
            items = itemRepository.findByRoomOfOriginIgnoreCase(room, pageable);
        } else {
            items = itemRepository.findAll(pageable);
        }

        return items.map(this::toResponse);
    }

    public ItemResponse update(Long id, ItemRequest request) {
        Item item = getItem(id);

        if (item.getStatus() == ItemStatus.DISPOSED) {
            throw new BusinessRuleException("ITEM_DISPOSED", "Cannot update metadata on a disposed item.");
        }

        String previousState = item.getStatus().name();

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setRoomOfOrigin(request.getRoomOfOrigin());
        item.setWidthCm(request.getWidthCm());
        item.setHeightCm(request.getHeightCm());
        item.setDepthCm(request.getDepthCm());
        item.setSentimentalScore(request.getSentimentalScore());
        item.setFragile(request.getFragile() != null ? request.getFragile() : false);
        item.setSpecialHandlingNotes(request.getSpecialHandlingNotes());

        if (request.getEstimatedValue() != null) {
            item.setEstimatedValue(request.getEstimatedValue());
        }

        Item saved = itemRepository.save(item);
        auditService.log(saved, "ITEM_UPDATED", previousState, saved.getStatus().name(), "Metadata updated");

        return toResponse(saved);
    }

    public void delete(Long id) {
        Item item = getItem(id);

        if (item.getStatus() != ItemStatus.IDENTIFIED) {
            throw new BusinessRuleException("CANNOT_DELETE", "Only items in IDENTIFIED status with no claims may be deleted. Current status: " + item.getStatus());
        }

        long activeClaims = claimRepository.countByItemIdAndStatus(id, ClaimStatus.ACTIVE);
        if (activeClaims > 0) {
            throw new BusinessRuleException("CANNOT_DELETE", "Cannot delete an item that has active claims. Dismiss all claims first.");
        }

        auditService.log(item, "ITEM_DELETED", item.getStatus().name(), null, "Item removed from inventory");
        itemRepository.delete(item);
    }

    // Disposition Workflow

    /**
     * Advances an item from IDENTIFIED to APPRAISED and records its assessed value.
     */
    public ItemResponse appraise(Long id, AppraiseRequest request) {
        Item item = getItem(id);

        checkMediationBlock(item);

        if (!item.getStatus().canTransitionTo(ItemStatus.APPRAISED)) {
            throw new BusinessRuleException("INVALID_TRANSITION", "Cannot appraise an item in status: " + item.getStatus() + ". Item must be in IDENTIFIED status.");
        }

        String previousStatus = item.getStatus().name();
        item.setEstimatedValue(request.getEstimatedValue());
        item.setStatus(ItemStatus.APPRAISED);

        Item saved = itemRepository.save(item);

        String details = "Appraised at $" + request.getEstimatedValue();
        if (request.getAppraisalNotes() != null) {
            details += ". Notes: " + request.getAppraisalNotes();
        }
        auditService.log(saved, "STATUS_CHANGE", previousStatus, ItemStatus.APPRAISED.name(), details);

        return toResponse(saved);
    }

    /**
     * Advances an item from APPRAISED to ASSIGNED and sets its disposition type.
     * Items destined for SALE must have an appraisal value greater than $0.
     */
    public ItemResponse assign(Long id, AssignRequest request) {
        Item item = getItem(id);

        checkMediationBlock(item);

        if (!item.getStatus().canTransitionTo(ItemStatus.ASSIGNED)) {
            throw new BusinessRuleException("INVALID_TRANSITION", "Cannot assign disposition for an item in status: " + item.getStatus() + ". Item must be in APPRAISED status.");
        }

        if (request.getDispositionType() == DispositionType.SALE) {
            validateSaleAppraisal(item);
        }

        String previousStatus = item.getStatus().name();
        item.setDispositionType(request.getDispositionType());
        item.setStatus(ItemStatus.ASSIGNED);

        Item saved = itemRepository.save(item);

        String details = "Disposition assigned: " + request.getDispositionType();
        if (request.getAssignmentNotes() != null) {
            details += ". Notes: " + request.getAssignmentNotes();
        }
        auditService.log(saved, "STATUS_CHANGE", previousStatus, ItemStatus.ASSIGNED.name(), details);

        return toResponse(saved);
    }

    /**
     * Advances an item from ASSIGNED to DISPOSED (final state).
     */
    public ItemResponse dispose(Long id) {
        Item item = getItem(id);

        checkMediationBlock(item);

        if (!item.getStatus().canTransitionTo(ItemStatus.DISPOSED)) {
            throw new BusinessRuleException("INVALID_TRANSITION",
                    "Cannot dispose an item in status: " + item.getStatus() +
                            ". Item must be in ASSIGNED status.");
        }

        String previousStatus = item.getStatus().name();
        item.setStatus(ItemStatus.DISPOSED);

        Item saved = itemRepository.save(item);

        auditService.log(saved, "STATUS_CHANGE", previousStatus, ItemStatus.DISPOSED.name(), "Item marked as disposed via: " + saved.getDispositionType());

        return toResponse(saved);
    }

    // Helpers

    public Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    private void checkMediationBlock(Item item) {
        if (Boolean.TRUE.equals(item.getMediationRequired())) {
            throw new MediationRequiredException(item.getId());
        }
    }

    private void validateSaleAppraisal(Item item) {
        if (item.getEstimatedValue() == null || item.getEstimatedValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("SALE_REQUIRES_APPRAISAL", "Items designated for SALE must have a verified appraisal value greater than $0. " +
                    "Current value: " + item.getEstimatedValue());
        }
    }

    public ItemResponse toResponse(Item item) {
        long activeClaimCount = claimRepository.countByItemIdAndStatus(item.getId(), ClaimStatus.ACTIVE);

        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .roomOfOrigin(item.getRoomOfOrigin())
                .widthCm(item.getWidthCm())
                .heightCm(item.getHeightCm())
                .depthCm(item.getDepthCm())
                .estimatedValue(item.getEstimatedValue())
                .sentimentalScore(item.getSentimentalScore())
                .status(item.getStatus())
                .dispositionType(item.getDispositionType())
                .fragile(item.getFragile())
                .specialHandlingNotes(item.getSpecialHandlingNotes())
                .mediationRequired(item.getMediationRequired())
                .activeClaimCount(activeClaimCount)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

}
