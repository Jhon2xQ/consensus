package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.util.JwtClaimExtractor;
import com.carmenio.consensus.application.util.ProcessStateCalculator;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Use case for creating a new electoral process.
 * <p>
 * Validates uniqueness of name and scope before persisting,
 * then auto-transitions the estatus via {@link ProcessStateCalculator#transitionState}.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CreateElectoralProcessUseCase {

    private final ElectoralProcessRepository repository;
    private final ElectoralProcessMapper mapper;
    private final JwtClaimExtractor jwtClaimExtractor;

    /**
     * Creates a new electoral process.
     * <p>
     * Extracts the creator's user ID from the JWT {@code sub} claim,
     * validates uniqueness of name and scope, then persists the entity.
     *
     * @param request the creation payload with name, scope, and dates
     * @param jwt     the authenticated JWT from the request
     * @return the created process as a response DTO
     * @throws ElectoralProcessException                           if name or scope already exist
     * @throws org.springframework.security.authentication.BadCredentialsException if the sub claim is missing (401)
     */
    public ElectoralProcessResponse execute(CreateElectoralProcessRequest request, Jwt jwt) {
        if (repository.existsByName(request.getName())) {
            throw ElectoralProcessException.alreadyExists("name \"" + request.getName() + "\"");
        }
        if (repository.existsByScope(request.getScope())) {
            throw ElectoralProcessException.alreadyExists("scope \"" + request.getScope() + "\"");
        }

        var createdBy = jwtClaimExtractor.extractUserId(jwt);

        var entity = mapper.toEntity(request);
        entity.setCreatedBy(createdBy);
        var saved = repository.save(entity);

        // Auto-transition estatus based on current dates
        // Dirty checking persists the change on transaction commit
        ProcessStateCalculator.transitionState(saved, Instant.now());

        return mapper.toResponse(saved, saved.getEstatus());
    }
}
