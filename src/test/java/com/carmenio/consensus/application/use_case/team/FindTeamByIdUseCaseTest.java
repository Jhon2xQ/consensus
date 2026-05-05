package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindTeamByIdUseCaseTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper mapper;

    @InjectMocks
    private FindTeamByIdUseCase useCase;

    @Test
    @DisplayName("Should return team when it exists")
    void shouldReturnTeamWhenFound() {
        var id = UUID.randomUUID();
        var team = new Team();
        team.setId(id);
        team.setName("Team Alpha");

        var expectedResponse = TeamResponse.builder()
                .id(id)
                .name("Team Alpha")
                .build();

        when(teamRepository.findById(id)).thenReturn(Optional.of(team));
        when(mapper.toResponse(team)).thenReturn(expectedResponse);

        var result = useCase.execute(id);

        assertNotNull(result);
        assertEquals("Team Alpha", result.getName());
        assertEquals(id, result.getId());
        verify(teamRepository).findById(id);
        verify(mapper).toResponse(team);
    }

    @Test
    @DisplayName("Should throw 404 when team not found")
    void shouldThrow404WhenNotFound() {
        var id = UUID.randomUUID();

        when(teamRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(TeamException.class,
                () -> useCase.execute(id));

        assertTrue(exception.getMessage().contains("not found"));
        assertTrue(exception.getMessage().contains(id.toString()));
    }
}
