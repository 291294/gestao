package com.erp.moveis.service;

import com.erp.moveis.model.Project;
import com.erp.moveis.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository repository;
    @InjectMocks private ProjectService service;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Kitchen Renovation");
        project.setDescription("Full kitchen design");
        project.setBudget(15000.0);
    }

    @Test @DisplayName("list — should return all projects")
    void shouldListAll() {
        when(repository.findAll()).thenReturn(List.of(project));
        List<Project> result = service.list();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Kitchen Renovation");
    }

    @Test @DisplayName("findById — should return project when found")
    void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(project));
        Optional<Project> result = service.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Kitchen Renovation");
    }

    @Test @DisplayName("findById — should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.findById(999L)).isEmpty();
    }

    @Test @DisplayName("findByClientId — should return client projects")
    void shouldFindByClientId() {
        when(repository.findByClientId(5L)).thenReturn(List.of(project));
        List<Project> result = service.findByClientId(5L);
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("save — should persist and return project")
    void shouldSave() {
        when(repository.save(project)).thenReturn(project);
        Project result = service.save(project);
        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).save(project);
    }

    @Test @DisplayName("delete — should call deleteById")
    void shouldDelete() {
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test @DisplayName("update — should update existing project fields")
    void shouldUpdate() {
        when(repository.findById(1L)).thenReturn(Optional.of(project));
        when(repository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project updates = new Project();
        updates.setName("Updated Name");
        updates.setBudget(20000.0);

        Project result = service.update(1L, updates);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getBudget()).isEqualTo(20000.0);
    }

    @Test @DisplayName("update — should return null when not found")
    void shouldReturnNullOnUpdateNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        Project result = service.update(999L, new Project());
        assertThat(result).isNull();
    }
}
