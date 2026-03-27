package com.project.heritagelink.service;

import com.project.heritagelink.dto.request.ClaimantRequest;
import com.project.heritagelink.dto.response.ClaimantResponse;
import com.project.heritagelink.exception.BusinessRuleException;
import com.project.heritagelink.exception.ResourceNotFoundException;
import com.project.heritagelink.model.Claimant;
import com.project.heritagelink.repository.ClaimantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimantService {

    private final ClaimantRepository claimantRepository;

    public ClaimantResponse create(ClaimantRequest request) {
        if (request.getEmail() != null && claimantRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("DUPLICATE_EMAIL", "A claimant with email '" + request.getEmail() + "' is already registered");
        }

        Claimant claimant = Claimant.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .relationship(request.getRelationship())
                .build();

        return toResponse(claimantRepository.save(claimant));
    }

    @Transactional(readOnly = true)
    public ClaimantResponse findById(Long id) {
        return toResponse(getClaimant(id));
    }

    @Transactional(readOnly = true)
    public Page<ClaimantResponse> findAll(Pageable pageable) {
        return claimantRepository.findAll(pageable).map(this::toResponse);
    }

    public ClaimantResponse update(Long id, ClaimantRequest request) {
        Claimant claimant = getClaimant(id);

        if (request.getEmail() != null && !request.getEmail().equals(claimant.getEmail()) && claimantRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("DUPLICATE_EMAIL", "A claimant with email '" + request.getEmail() + "' is already registered");
        }

        claimant.setFirstName(request.getFirstName());
        claimant.setLastName(request.getLastName());
        claimant.setEmail(request.getEmail());
        claimant.setPhone(request.getPhone());
        claimant.setRelationship(request.getRelationship());

        return toResponse(claimantRepository.save(claimant));
    }

    // Helpers

    public Claimant getClaimant(Long id) {
        return claimantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claimant", id));
    }

    public ClaimantResponse toResponse(Claimant c) {
        return ClaimantResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .relationship(c.getRelationship())
                .createdAt(c.getCreatedAt())
                .build();
    }

}
