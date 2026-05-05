package com.carmenio.consensus.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * JPA entity representing a candidate team within an electoral process.
 * <p>
 * Each team belongs to exactly one electoral process and can receive votes.
 * The vote count is calculated at result time, not stored here.
 */
@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column
    private String avatarUrl;

    @Column(nullable = false)
    private UUID electoralProcessId;
}
