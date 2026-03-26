package com.project.heritagelink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A family member or beneficiary who may submit claims on inventory items.
 */
@Entity
@Table(name = "claimants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claimant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email
    @Column(unique = true)
    private String email;

    private String phone;

    @NotBlank
    @Column(nullable = false)
    private String relationship;

    @OneToMany(mappedBy = "claimant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Claim> claims = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
