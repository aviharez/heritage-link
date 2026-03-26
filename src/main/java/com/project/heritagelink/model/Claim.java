package com.project.heritagelink.model;

import com.project.heritagelink.model.enums.ClaimStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Records a family member's request to receive a specific item.
 * Multiple active claims on a high-sentiment item trigger the mediation workflow.
 */
@Entity
@Table(
        name = "claims",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_claim_item_claimant",
                columnNames = { "item_id", "claimant_id" }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claimant_id", nullable = false)
    private Claimant claimant;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.ACTIVE;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "claimed_at", updatable = false)
    private LocalDateTime claimedAt;

}
