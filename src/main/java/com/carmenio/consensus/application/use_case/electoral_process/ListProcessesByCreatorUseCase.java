package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.util.ProcessStateCalculator;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Use case for listing electoral processes created by a specific user.
 * <p>
 * Filters by {@code createdBy} field matching the JWT {@code sub} claim,
 * returns a paginated response with fresh computed estatus values.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListProcessesByCreatorUseCase {

    private final ElectoralProcessRepository repository;
    private final ElectoralProcessMapper mapper;

    /**
     * Returns a paginated list of electoral processes created by the given user.
     *
     * @param createdBy the user ID (JWT sub) to filter by
     * @param pageable  pagination parameters
     * @return paginated response with process DTOs
     */
    public PaginatedResponse<ElectoralProcessResponse> execute(String createdBy, Pageable pageable) {
        var now = Instant.now();
        var page = repository.findByCreatedBy(createdBy, pageable);
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
