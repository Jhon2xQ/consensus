package com.carmenio.consensus.domain.entity;

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
 * The state is <strong>calculated in real-time</strong> from these dates.
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
}
