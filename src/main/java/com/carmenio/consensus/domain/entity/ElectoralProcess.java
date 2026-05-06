package com.carmenio.consensus.domain.entity;

import com.carmenio.consensus.common.constant.ProcessStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
 * on write operations. PAUSED and CANCELLED are manual lock states that
 * block auto-transition.
 */
@Entity
@Table(name = "electoral_processes")
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
