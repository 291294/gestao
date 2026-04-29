package com.erp.moveis.service;

import com.erp.moveis.core.tenant.TenantContext;
import com.erp.moveis.model.Supplier;
import com.erp.moveis.repository.SupplierRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository repository;

    @InjectMocks
    private SupplierService service;

    private MockedStatic<TenantContext> tenantContextMock;
    private static final Long COMPANY_ID = 10L;

    private Supplier buildSupplier(Long id) {
        Supplier s = new Supplier();
        s.setId(id);
        s.setCompanyId(COMPANY_ID);
        s.setName("Fornecedor Teste");
        s.setCnpj("12.345.678/0001-99");
        s.setEmail("fornecedor@teste.com");
        s.setPhone("11999999999");
        s.setActive(true);
        return s;
    }

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::requireTenantId).thenReturn(COMPANY_ID);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    // ── list ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list – deve retornar fornecedores da empresa atual")
    void shouldListSuppliersByCompany() {
        List<Supplier> suppliers = List.of(buildSupplier(1L), buildSupplier(2L));
        when(repository.findByCompanyId(COMPANY_ID)).thenReturn(suppliers);

        List<Supplier> result = service.list();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> COMPANY_ID.equals(s.getCompanyId()));
        verify(repository).findByCompanyId(COMPANY_ID);
    }

    @Test
    @DisplayName("list – deve retornar lista vazia quando não há fornecedores")
    void shouldReturnEmptyListWhenNoSuppliers() {
        when(repository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());

        List<Supplier> result = service.list();

        assertThat(result).isEmpty();
    }

    // ── listActive ────────────────────────────────────────────────────────

    @Test
    @DisplayName("listActive – deve retornar apenas fornecedores ativos")
    void shouldListActiveSuppliers() {
        Supplier active = buildSupplier(1L);
        when(repository.findByCompanyIdAndActiveTrue(COMPANY_ID)).thenReturn(List.of(active));

        List<Supplier> result = service.listActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActive()).isTrue();
        verify(repository).findByCompanyIdAndActiveTrue(COMPANY_ID);
    }

    // ── findById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById – deve retornar fornecedor quando pertence à empresa")
    void shouldFindSupplierByIdWhenSameCompany() {
        Supplier supplier = buildSupplier(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        Optional<Supplier> result = service.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById – deve retornar vazio quando companyId não corresponde (isolamento de tenant)")
    void shouldReturnEmptyWhenDifferentCompany() {
        Supplier supplier = buildSupplier(1L);
        supplier.setCompanyId(99L); // empresa diferente
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        Optional<Supplier> result = service.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById – deve retornar vazio quando fornecedor não existe")
    void shouldReturnEmptyWhenSupplierNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<Supplier> result = service.findById(999L);

        assertThat(result).isEmpty();
    }

    // ── save ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save – deve definir companyId automaticamente pelo tenant")
    void shouldSetCompanyIdOnSave() {
        Supplier toSave = new Supplier();
        toSave.setName("Novo Fornecedor");

        when(repository.save(any(Supplier.class))).thenAnswer(inv -> {
            Supplier s = inv.getArgument(0);
            s.setId(5L);
            return s;
        });

        Supplier saved = service.save(toSave);

        assertThat(saved.getCompanyId()).isEqualTo(COMPANY_ID);
        verify(repository).save(toSave);
    }

    // ── update ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update – deve atualizar campos do fornecedor da mesma empresa")
    void shouldUpdateSupplierFields() {
        Supplier existing = buildSupplier(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        Supplier details = new Supplier();
        details.setName("Nome Atualizado");
        details.setEmail("novo@email.com");
        details.setPhone("11888888888");

        Supplier updated = service.update(1L, details);

        assertThat(updated.getName()).isEqualTo("Nome Atualizado");
        assertThat(updated.getEmail()).isEqualTo("novo@email.com");
        assertThat(updated.getPhone()).isEqualTo("11888888888");
    }

    @Test
    @DisplayName("update – deve lançar exceção quando fornecedor não pertence à empresa")
    void shouldThrowWhenUpdatingSupplierOfDifferentCompany() {
        Supplier existing = buildSupplier(1L);
        existing.setCompanyId(99L); // empresa diferente
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        Supplier details = new Supplier();
        details.setName("Tentativa Maliciosa");

        assertThatThrownBy(() -> service.update(1L, details))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("update – não deve sobrescrever campos nulos")
    void shouldNotOverwriteNullFields() {
        Supplier existing = buildSupplier(1L);
        existing.setCity("São Paulo");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        Supplier details = new Supplier();
        details.setName("Nome Atualizado");
        // city = null — não deve sobrescrever

        Supplier updated = service.update(1L, details);

        assertThat(updated.getCity()).isEqualTo("São Paulo"); // mantém o valor original
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete – deve excluir fornecedor da mesma empresa")
    void shouldDeleteSupplierFromSameCompany() {
        Supplier supplier = buildSupplier(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete – não deve excluir fornecedor de empresa diferente (isolamento de tenant)")
    void shouldNotDeleteSupplierFromDifferentCompany() {
        Supplier supplier = buildSupplier(1L);
        supplier.setCompanyId(99L); // empresa diferente
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        service.delete(1L);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete – não deve lançar exceção quando fornecedor não existe")
    void shouldNotThrowWhenDeletingNonExistent() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatCode(() -> service.delete(999L)).doesNotThrowAnyException();
        verify(repository, never()).deleteById(any());
    }
}
