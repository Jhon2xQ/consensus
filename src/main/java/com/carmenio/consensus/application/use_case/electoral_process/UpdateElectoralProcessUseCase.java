package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case for updating an existing electoral process.
 * <p>
 * Validates uniqueness if name is changed.
 */
@Component
@RequiredArgsConstructor
public class UpdateElectoralProcessUseCase {

    private final ElectoralProcessRepository repository;
    private final ElectoralProcessMapper mapper;

    /**
     * Updates an electoral process.
     *
     * @param id      the process UUID
     * @param request the update payload with optional fields
     * @return the updated process response DTO
     * @throws ElectoralProcessException if no process exists or the new name conflicts
     */
    public ElectoralProcessResponse execute(UUID id, UpdateElectoralProcessRequest request) {
        var entity = repository.findById(id)
                .orElseThrow(() -> ElectoralProcessException.notFound(id));

        if (request.getName() != null && !request.getName().equals(entity.getName())) {
            if (repository.existsByName(request.getName())) {
                throw ElectoralProcessException.alreadyExists("name \"" + request.getName() + "\"");
            }
        }

        mapper.updateEntity(entity, request);
        var saved = repository.save(entity);
        return mapper.toResponse(saved);
    }
}
