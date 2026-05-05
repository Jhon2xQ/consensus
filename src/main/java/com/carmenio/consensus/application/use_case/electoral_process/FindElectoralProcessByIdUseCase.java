package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case for finding an electoral process by its unique identifier.
 */
@Component
@RequiredArgsConstructor
public class FindElectoralProcessByIdUseCase {

    private final ElectoralProcessRepository repository;
    private final ElectoralProcessMapper mapper;

    /**
     * Finds an electoral process by ID.
     *
     * @param id the process UUID
     * @return the process response DTO
     * @throws ElectoralProcessException if no process exists with the given ID
     */
    public ElectoralProcessResponse execute(UUID id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> ElectoralProcessException.notFound(id));
        return mapper.toResponse(entity);
    }
}
