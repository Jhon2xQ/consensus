package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaTeamRepositoryAdapterTest {

    @Mock
    private JpaTeamRepository jpaRepository;

    @InjectMocks
    private JpaTeamRepositoryAdapter adapter;

    @Test
    @DisplayName("Should save team")
    void shouldSaveTeam() {
        var team = new Team();
        when(jpaRepository.save(team)).thenReturn(team);

        var result = adapter.save(team);

        assertSame(team, result);
        verify(jpaRepository).save(team);
    }

    @Test
    @DisplayName("Should find team by ID")
    void shouldFindById() {
        var id = UUID.randomUUID();
        var team = new Team();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(team));

        var result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertSame(team, result.get());
        verify(jpaRepository).findById(id);
    }

    @Test
    @DisplayName("Should return empty when team not found")
    void shouldReturnEmptyWhenNotFound() {
        var id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        var result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find teams by electoral process ID")
    void shouldFindByElectoralProcessId() {
        var processId = UUID.randomUUID();
        var teams = List.of(new Team(), new Team());
        when(jpaRepository.findByElectoralProcessId(processId)).thenReturn(teams);

        var result = adapter.findByElectoralProcessId(processId);

        assertEquals(2, result.size());
        verify(jpaRepository).findByElectoralProcessId(processId);
    }

    @Test
    @DisplayName("Should delete team")
    void shouldDeleteTeam() {
        var team = new Team();
        doNothing().when(jpaRepository).delete(team);

        adapter.delete(team);

        verify(jpaRepository).delete(team);
    }

    @Test
    @DisplayName("Should check if team exists by ID")
    void shouldExistsById() {
        var id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(true);

        var result = adapter.existsById(id);

        assertTrue(result);
        verify(jpaRepository).existsById(id);
    }

    @Test
    @DisplayName("Should check if teams exist by process ID")
    void shouldExistsByProcessId() {
        var processId = UUID.randomUUID();
        when(jpaRepository.existsByElectoralProcessId(processId)).thenReturn(true);

        var result = adapter.existsByProcessId(processId);

        assertTrue(result);
        verify(jpaRepository).existsByElectoralProcessId(processId);
    }
}
