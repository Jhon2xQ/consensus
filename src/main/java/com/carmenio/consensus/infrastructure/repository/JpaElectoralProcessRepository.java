package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ElectoralProcess}.
 * <p>
 * Provides basic CRUD operations and query derivation.
 * Not exposed outside the infrastructure layer — use
 * {@link JpaElectoralProcessRepositoryAdapter} instead.
 */
@Repository
interface JpaElectoralProcessRepository extends JpaRepository<ElectoralProcess, UUID> {

    Optional<ElectoralProcess> findByScope(String scope);

    boolean existsByName(String name);

    boolean existsByScope(String scope);
}
