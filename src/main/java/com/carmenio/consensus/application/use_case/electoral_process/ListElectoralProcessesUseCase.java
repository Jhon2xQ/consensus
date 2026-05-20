package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.PaginatedResponse;
import com.carmenio.consensus.application.util.ProcessStateCalculator;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Use case for listing electoral processes with pagination.
 * <p>
 * Calls {@link ProcessStateCalculator#transitionState} on each entity
 * to ensure all responses contain fresh computed estatus values.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListElectoralProcessesUseCase {

    private final ElectoralProcessRepository repository;
    private final ElectoralProcessMapper mapper;

    /**
     * Returns a paginated list of all electoral processes.
     *
     * @param pageable pagination parameters
     * @return paginated response with process DTOs
     */
    public PaginatedResponse<ElectoralProcessResponse> execute(Pageable pageable) {
        var now = Instant.now();
        var page = repository.findAll(pageable);
        var content = page.getContent().stream()
                .map(entity -> {
                    ProcessStateCalculator.transitionState(entity, now);
                    return mapper.toResponse(entity, entity.getEstatus());
                })
                .toList();

        return PaginatedResponse.<ElectoralProcessResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
