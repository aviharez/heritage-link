package com.project.heritagelink.repository;

import com.project.heritagelink.model.Claimant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimantRepository extends JpaRepository<Claimant, Long> {

    Optional<Claimant> findByEmail(String email);

    boolean existsByEmail(String email);

}
