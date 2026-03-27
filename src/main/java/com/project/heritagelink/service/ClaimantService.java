package com.project.heritagelink.service;

import com.project.heritagelink.dto.request.ClaimantRequest;
import com.project.heritagelink.dto.response.ClaimantResponse;
import com.project.heritagelink.exception.BusinessRuleException;
import com.project.heritagelink.model.Claimant;
import com.project.heritagelink.repository.ClaimantRepository;
import lombok.RequiredArgsConstructor;
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

    // Helpers

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
