package com.carmenio.consensus.domain.repository;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for {@link ElectoralProcess} persistence.
 * <p>
 * This interface is part of the domain layer and defines the contract
 * that infrastructure must implement.
 */
public interface ElectoralProcessRepository {

    /**
     * Persists a new or updated electoral process.
     */
    ElectoralProcess save(ElectoralProcess process);

    /**
     * Finds a process by its unique identifier.
     */
    Optional<ElectoralProcess> findById(UUID id);

    /**
     * Returns a paginated list of all processes.
     */
    Page<ElectoralProcess> findAll(Pageable pageable);

    /**
     * Finds a process by its unique scope string.
     */
    Optional<ElectoralProcess> findByScope(String scope);

    /**
     * Deletes a process from persistence.
     */
    void delete(ElectoralProcess process);

    /**
     * Returns whether a process with the given name exists.
     */
    boolean existsByName(String name);

    /**
     * Returns whether a process with the given scope exists.
     */
    boolean existsByScope(String scope);

    /**
     * Returns a paginated list of processes created by the given user ID.
     */
    Page<ElectoralProcess> findByCreatedBy(String createdBy, Pageable pageable);
}
