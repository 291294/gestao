package com.erp.moveis.inventory.service;

import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.inventory.entity.Warehouse;
import com.erp.moveis.inventory.repository.WarehouseRepository;
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
class WarehouseServiceImplTest {

    @Mock private WarehouseRepository repository;
    @InjectMocks private WarehouseServiceImpl service;

    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCompanyId(1L);
        warehouse.setName("Main Warehouse");
    }

    @Test @DisplayName("create — should set active=true and createdAt")
    void shouldCreate() {
        when(repository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));
        Warehouse result = service.create(warehouse);
        assertThat(result.getActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        verify(repository).save(warehouse);
    }

    @Test @DisplayName("findByCompany — should return company warehouses")
    void shouldFindByCompany() {
        when(repository.findByCompanyId(1L)).thenReturn(List.of(warehouse));
        List<Warehouse> result = service.findByCompany(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Main Warehouse");
    }

    @Test @DisplayName("findById — should return warehouse when found")
    void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(warehouse));
        Warehouse result = service.findById(1L);
        assertThat(result.getName()).isEqualTo("Main Warehouse");
    }

    @Test @DisplayName("findById — should throw when not found")
    void shouldThrowWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("deactivate — should set active=false")
    void shouldDeactivate() {
        warehouse.setActive(true);
        when(repository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(repository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deactivate(1L);
        assertThat(warehouse.getActive()).isFalse();
        verify(repository).save(warehouse);
    }
}
