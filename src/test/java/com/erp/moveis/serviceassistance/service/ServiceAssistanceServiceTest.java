package com.erp.moveis.serviceassistance.service;

import com.erp.moveis.core.tenant.TenantContext;
import com.erp.moveis.notification.service.NotificationService;
import com.erp.moveis.serviceassistance.dto.ServiceAssistanceRequest;
import com.erp.moveis.serviceassistance.dto.ServiceAssistanceResponse;
import com.erp.moveis.serviceassistance.entity.ServiceAssistance;
import com.erp.moveis.serviceassistance.entity.ServiceAssistanceStatus;
import com.erp.moveis.serviceassistance.repository.ServiceAssistanceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceAssistanceServiceTest {

    @Mock
    private ServiceAssistanceRepository repository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ServiceAssistanceService service;

    private MockedStatic<TenantContext> tenantContextMock;
    private static final Long COMPANY_ID = 10L;

    private ServiceAssistance buildEntity(Long id, ServiceAssistanceStatus status) {
        ServiceAssistance sa = new ServiceAssistance();
        sa.setId(id);
        sa.setCompanyId(COMPANY_ID);
        sa.setClientName("Cliente Teste");
        sa.setClientAddress("Rua das Flores, 100");
        sa.setScheduledDate(LocalDate.now().plusDays(1));
        sa.setServiceDescription("Ajuste de porta deslizante");
        sa.setStatus(status);
        sa.setNotificationSent(false);
        // Simular PrePersist
        sa.setCreatedAt(LocalDateTime.now());
        sa.setUpdatedAt(LocalDateTime.now());
        return sa;
    }

    private ServiceAssistanceRequest buildRequest() {
        ServiceAssistanceRequest req = new ServiceAssistanceRequest();
        req.setClientName("Cliente Novo");
        req.setClientAddress("Av. Brasil, 200");
        req.setScheduledDate(LocalDate.now().plusDays(3));
        req.setServiceDescription("Instalação de armário");
        req.setNotes("Levar ferramentas extras");
        return req;
    }

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::requireTenantId).thenReturn(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    // ── list ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list – deve retornar assistências da empresa atual, ordenadas por data")
    void shouldListAssistancesByCompany() {
        List<ServiceAssistance> entities = List.of(
                buildEntity(1L, ServiceAssistanceStatus.SCHEDULED),
                buildEntity(2L, ServiceAssistanceStatus.IN_PROGRESS)
        );
        when(repository.findByCompanyIdOrderByScheduledDateAsc(COMPANY_ID)).thenReturn(entities);

        List<ServiceAssistanceResponse> result = service.list();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> COMPANY_ID.equals(r.getCompanyId()));
        verify(repository).findByCompanyIdOrderByScheduledDateAsc(COMPANY_ID);
    }

    @Test
    @DisplayName("list – deve retornar lista vazia quando não há assistências")
    void shouldReturnEmptyListWhenNoAssistances() {
        when(repository.findByCompanyIdOrderByScheduledDateAsc(COMPANY_ID)).thenReturn(List.of());

        List<ServiceAssistanceResponse> result = service.list();

        assertThat(result).isEmpty();
    }

    // ── findById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById – deve retornar assistência quando pertence à empresa")
    void shouldFindAssistanceById() {
        ServiceAssistance entity = buildEntity(1L, ServiceAssistanceStatus.SCHEDULED);
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(entity));

        Optional<ServiceAssistanceResponse> result = service.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getClientName()).isEqualTo("Cliente Teste");
    }

    @Test
    @DisplayName("findById – deve retornar vazio quando assistência não existe ou é de outra empresa")
    void shouldReturnEmptyWhenNotFoundOrDifferentCompany() {
        when(repository.findByIdAndCompanyId(999L, COMPANY_ID)).thenReturn(Optional.empty());

        Optional<ServiceAssistanceResponse> result = service.findById(999L);

        assertThat(result).isEmpty();
    }

    // ── create ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create – deve criar assistência com companyId do tenant atual")
    void shouldCreateAssistanceWithTenantCompanyId() {
        ServiceAssistanceRequest request = buildRequest();
        when(repository.save(any(ServiceAssistance.class))).thenAnswer(inv -> {
            ServiceAssistance sa = inv.getArgument(0);
            sa.setId(1L);
            sa.setCreatedAt(LocalDateTime.now());
            sa.setUpdatedAt(LocalDateTime.now());
            return sa;
        });

        ServiceAssistanceResponse response = service.create(request);

        assertThat(response.getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(response.getClientName()).isEqualTo("Cliente Novo");
        assertThat(response.getStatus()).isEqualTo(ServiceAssistanceStatus.SCHEDULED.name());
        verify(repository).save(any(ServiceAssistance.class));
    }

    @Test
    @DisplayName("create – deve usar status padrão SCHEDULED quando não especificado")
    void shouldDefaultToScheduledStatus() {
        ServiceAssistanceRequest request = buildRequest();
        request.setStatus(null);

        when(repository.save(any(ServiceAssistance.class))).thenAnswer(inv -> {
            ServiceAssistance sa = inv.getArgument(0);
            sa.setId(2L);
            sa.setCreatedAt(LocalDateTime.now());
            sa.setUpdatedAt(LocalDateTime.now());
            return sa;
        });

        ServiceAssistanceResponse response = service.create(request);

        assertThat(response.getStatus()).isEqualTo(ServiceAssistanceStatus.SCHEDULED.name());
    }

    @Test
    @DisplayName("create – deve aceitar status INVALID sem lançar exceção (mantém SCHEDULED)")
    void shouldIgnoreInvalidStatus() {
        ServiceAssistanceRequest request = buildRequest();
        request.setStatus("STATUS_INVALIDO");

        when(repository.save(any(ServiceAssistance.class))).thenAnswer(inv -> {
            ServiceAssistance sa = inv.getArgument(0);
            sa.setId(3L);
            sa.setCreatedAt(LocalDateTime.now());
            sa.setUpdatedAt(LocalDateTime.now());
            return sa;
        });

        assertThatCode(() -> service.create(request)).doesNotThrowAnyException();
    }

    // ── update ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update – deve atualizar todos os campos da assistência")
    void shouldUpdateAssistanceFields() {
        ServiceAssistance existing = buildEntity(1L, ServiceAssistanceStatus.SCHEDULED);
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(existing));
        when(repository.save(any(ServiceAssistance.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceAssistanceRequest request = buildRequest();
        request.setStatus("IN_PROGRESS");

        Optional<ServiceAssistanceResponse> result = service.update(1L, request);

        assertThat(result).isPresent();
        assertThat(result.get().getClientName()).isEqualTo("Cliente Novo");
        assertThat(result.get().getStatus()).isEqualTo("IN_PROGRESS");
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("update – deve retornar vazio quando assistência não pertence à empresa (isolamento de tenant)")
    void shouldReturnEmptyWhenUpdatingAssistanceOfDifferentCompany() {
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.empty());

        Optional<ServiceAssistanceResponse> result = service.update(1L, buildRequest());

        assertThat(result).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update – deve manter status atual quando status inválido é enviado")
    void shouldKeepCurrentStatusWhenInvalidStatusSent() {
        ServiceAssistance existing = buildEntity(1L, ServiceAssistanceStatus.COMPLETED);
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(existing));
        when(repository.save(any(ServiceAssistance.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceAssistanceRequest request = buildRequest();
        request.setStatus("STATUS_INVALIDO");

        Optional<ServiceAssistanceResponse> result = service.update(1L, request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo("COMPLETED");
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete – deve excluir assistência e retornar true")
    void shouldDeleteAssistanceAndReturnTrue() {
        ServiceAssistance entity = buildEntity(1L, ServiceAssistanceStatus.SCHEDULED);
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(entity));

        boolean deleted = service.delete(1L);

        assertThat(deleted).isTrue();
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("delete – deve retornar false quando assistência não pertence à empresa")
    void shouldReturnFalseWhenDeletingFromDifferentCompany() {
        when(repository.findByIdAndCompanyId(999L, COMPANY_ID)).thenReturn(Optional.empty());

        boolean deleted = service.delete(999L);

        assertThat(deleted).isFalse();
        verify(repository, never()).delete(any());
    }

    // ── toResponse (mapping) ──────────────────────────────────────────────

    @Test
    @DisplayName("create – deve mapear todos os campos na resposta corretamente")
    void shouldMapAllFieldsInResponse() {
        ServiceAssistanceRequest request = buildRequest();
        request.setPhotoUrl("/api/service-assistances/photos/test.jpg");
        request.setMaterialRequestedAt(LocalDate.now());

        when(repository.save(any(ServiceAssistance.class))).thenAnswer(inv -> {
            ServiceAssistance sa = inv.getArgument(0);
            sa.setId(10L);
            sa.setCreatedAt(LocalDateTime.now());
            sa.setUpdatedAt(LocalDateTime.now());
            return sa;
        });

        ServiceAssistanceResponse response = service.create(request);

        assertThat(response.getPhotoUrl()).isEqualTo("/api/service-assistances/photos/test.jpg");
        assertThat(response.getMaterialRequestedAt()).isEqualTo(LocalDate.now());
        assertThat(response.getNotes()).isEqualTo("Levar ferramentas extras");
        assertThat(response.getNotificationSent()).isFalse();
    }
}
