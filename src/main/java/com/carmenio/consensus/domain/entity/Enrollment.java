package com.carmenio.consensus.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a voter enrollment in an electoral process.
 * <p>
 * Two-phase enrollment flow:
 * <ol>
 *   <li><b>Creator</b> registers a voter email — creates an Enrollment with
 *       {@code email} populated and {@code userId}/@{@code commitment} null.</li>
 *   <li><b>User</b> claims the enrollment — JWT email matches, sets
 *       {@code userId} (from JWT {@code sub}) and {@code commitment}.</li>
 * </ol>
 * The {@code hasVoted} flag is set externally when a validated vote
 * record is received from the Semaphore Relayer.
 * <p>
 * Constraint pairs enforced at the database level:
 * <ul>
 *   <li>{@code UNIQUE(electoralProcessId, email)} — no duplicate emails per process</li>
 *   <li>{@code UNIQUE(electoralProcessId, commitment)} — no duplicate commitments per process</li>
 * </ul>
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"electoralProcessId", "email"}),
        @UniqueConstraint(columnNames = {"electoralProcessId", "commitment"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID electoralProcessId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String userId;

    @Column(nullable = true)
    private String commitment;

    @Builder.Default
    private boolean hasVoted = false;

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
