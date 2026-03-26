package com.project.heritagelink.model;

import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical item in a client's household inventory.
 * Tracks the full lifecycle from initial cataloging to final disposition.
 */
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(name = "room_of_origin", nullable = false)
    private String roomOfOrigin;

    @Column(name = "width_cm")
    private Double widthCm;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "depth_cm")
    private Double depthCm;

    @Column(name = "estimated_value", precision = 12, scale = 2)
    private BigDecimal estimatedValue;

    @NotNull
    @Min(1) @Max(10)
    @Column(name = "sentimental_score", nullable = false)
    private Integer sentimentalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ItemStatus status = ItemStatus.IDENTIFIED;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition_type")
    private DispositionType dispositionType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean fragile = false;

    @Column(name = "special_handling_notes", columnDefinition = "TEXT")
    private String specialHandlingNotes;

    /**
     * True when two or more family members have active claims on a high-sentiment item.
     * Status transitions are blocked until this flag is cleared via claim resolution.
     */
    @Column(name = "mediation_required", nullable = false)
    @Builder.Default
    private Boolean mediationRequired = false;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Claim> claims = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
