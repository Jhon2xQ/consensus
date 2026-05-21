package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.util.JwtClaimExtractor;
import com.carmenio.consensus.application.util.ProcessStateCalculator;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Use case for listing electoral processes created by the authenticated user.
 * <p>
 * Extracts the user ID from the JWT {@code sub} claim, filters by
 * {@code createdBy}, and returns a paginated response with fresh computed
 * estatus values.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListProcessesByCreatorUseCase {

    private final ElectoralProcessRepository repository;
    private final ElectoralProcessMapper mapper;
    private final JwtClaimExtractor jwtClaimExtractor;

    /**
     * Returns a paginated list of electoral processes created by the
     * authenticated user.
     *
     * @param jwt      the authenticated JWT from the request
     * @param pageable pagination parameters
     * @return paginated response with process DTOs
     * @throws org.springframework.security.authentication.BadCredentialsException if the sub claim is missing (401)
     */
    public PaginatedResponse<ElectoralProcessResponse> execute(Jwt jwt, Pageable pageable) {
        var createdBy = jwtClaimExtractor.extractUserId(jwt);
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
