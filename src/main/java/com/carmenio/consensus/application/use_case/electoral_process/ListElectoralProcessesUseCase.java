package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.PaginatedResponse;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Use case for listing electoral processes with pagination.
 */
@Component
@RequiredArgsConstructor
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
        var page = repository.findAll(pageable);
        var content = page.getContent().stream()
                .map(mapper::toResponse)
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
