package com.carmenio.consensus.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a voter enrollment in an electoral process.
 * <p>
 * Each enrollment links a voter (by userId and Semaphore commitment)
 * to a specific electoral process. The {@code hasVoted} flag is set
 * externally when a validated vote record is received from the
 * Semaphore Relayer.
 * <p>
 * Constraint pairs enforced at the database level:
 * <ul>
 *   <li>{@code UNIQUE(electoralProcessId, userId)} — one enrollment per voter per process</li>
 *   <li>{@code UNIQUE(electoralProcessId, commitment)} — no duplicate commitments per process</li>
 * </ul>
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"electoralProcessId", "userId"}),
        @UniqueConstraint(columnNames = {"electoralProcessId", "commitment"})
})
@EntityListeners(AuditingEntityListener.class)
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
    private String userId;

    @Column(nullable = false)
    private String commitment;

    @Builder.Default
    private boolean hasVoted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
