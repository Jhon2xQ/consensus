package com.carmenio.consensus.domain.entity;

import com.carmenio.consensus.common.constant.ProcessStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing an electoral process.
 * <p>
 * Each process defines a timeline with distinct phases:
 * <ol>
 *   <li><b>Commitment</b> — enrollment window ({@link #commitmentStart} → {@link #commitmentEnd})</li>
 *   <li><b>Voting</b> — voting window ({@link #votingStart} → {@link #votingEnd})</li>
 *   <li><b>Results</b> — results publication ({@link #results})</li>
 * </ol>
 * The {@code estatus} is always non-null, managed by {@link
 * com.carmenio.consensus.application.util.ProcessStateCalculator#transitionState}
 * on write operations. All states are date-driven with no manual overrides.
 */
@Entity
@Table(name = "electoral_processes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectoralProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String scope;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProcessStatus estatus = ProcessStatus.NONE;

    @Column(nullable = false)
    private Instant commitmentStart;

    @Column(nullable = false)
    private Instant commitmentEnd;

    @Column(nullable = false)
    private Instant votingStart;

    @Column(nullable = false)
    private Instant votingEnd;

    @Column(nullable = false)
    private Instant results;

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
