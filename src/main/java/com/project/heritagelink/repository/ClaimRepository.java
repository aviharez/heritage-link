package com.project.heritagelink.repository;

import com.project.heritagelink.model.Claim;
import com.project.heritagelink.model.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Page<Claim> findByItemId(Long itemId, Pageable pageable);
    Page<Claim> findByClaimantId(Long claimantId, Pageable pageable);

    List<Claim> findByItemIdAndStatus(Long itemId, ClaimStatus status);

    Optional<Claim> findByItemIdAndClaimantId(Long itemId, Long claimantId);
    boolean existsByItemIdAndClaimantIdAndStatus(Long itemId, Long claimantId, ClaimStatus status);
    long countByItemIdAndStatus(Long itemId, ClaimStatus status);

}
