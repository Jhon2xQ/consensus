package com.carmenio.consensus.application.use_case.record;

import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
import com.carmenio.consensus.infrastructure.mapper.VoteRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for listing vote records.
 * <p>
 * Supports two query paths:
 * <ul>
 *   <li>Paginated listing of all records (no scope filter)</li>
 *   <li>Complete listing filtered by scope (no pagination)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListVoteRecordsUseCase {

    private final VoteRecordRepository repository;
    private final VoteRecordMapper mapper;

    /**
     * Returns a paginated view of all vote records.
     *
     * @param pageable pagination information
     * @return paginated response with vote record DTOs
     */
    public PaginatedResponse<VoteRecordResponse> executePaginated(Pageable pageable) {
        var page = repository.findAll(pageable);
        var content = page.getContent().stream()
                .map(mapper::toResponse)
                .toList();

        return PaginatedResponse.<VoteRecordResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Returns all vote records matching the given scope.
     *
     * @param scope the electoral process scope
     * @return complete list of vote record DTOs for that scope
     */
    public List<VoteRecordResponse> executeByScope(String scope) {
        return repository.findByScope(scope).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
