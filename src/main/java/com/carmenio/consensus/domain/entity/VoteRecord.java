package com.carmenio.consensus.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a validated vote record from the Semaphore Relayer.
 * <p>
 * Each record corresponds to a vote cast in an electoral process, validated
 * on-chain via Semaphore ZK proofs. The Semaphore Relayer sends these records
 * after a {@code ProofValidated} event is emitted.
 * <p>
 * The {@code nullifier} is unique to prevent double-counting votes (the
 * Relayer may retry, and idempotency is handled at the application layer).
 * <p>
 * Constraint:
 * <ul>
 *   <li>{@code UNIQUE(nullifier)} — ensures vote idempotency</li>
 * </ul>
 */
@Entity
@Table(name = "vote_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nullifier"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String groupId;

    @Column(nullable = false, unique = true)
    private String nullifier;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String scope;

    @Column
    private String transactionHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
