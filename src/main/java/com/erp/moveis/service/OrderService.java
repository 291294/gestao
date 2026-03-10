package com.erp.moveis.service;

import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.inventory.service.InventoryService;
import com.erp.moveis.model.Order;
import com.erp.moveis.model.OrderItem;
import com.erp.moveis.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository repository;
    private final InventoryService inventoryService;

    private static final Long DEFAULT_WAREHOUSE_ID = 1L;

    public List<Order> list() {
        return repository.findAll();
    }

    public Page<Order> listPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Order> findById(Long id) {
        return repository.findById(id);
    }

    public List<Order> findByClientId(Long clientId) {
        return repository.findByClientId(clientId);
    }

    @Transactional
    public Order save(Order order) {
        // Calcular subtotais dos itens
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
                item.calculateSubtotal();
            }
            order.recalculateTotal();
        }

        Order saved = repository.save(order);

        // Reservar estoque para cada item do pedido
        if (saved.getItems() != null && !saved.getItems().isEmpty()) {
            for (OrderItem item : saved.getItems()) {
                inventoryService.reserveWarehouseStock(
                        saved.getCompanyId(),
                        item.getProductId(),
                        DEFAULT_WAREHOUSE_ID,
                        item.getQuantity(),
                        saved.getId()
                );
            }
            log.info("[ORDER_STOCK_RESERVED] Order {} - {} item(s) reserved",
                    saved.getId(), saved.getItems().size());
        }

        return saved;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public Order update(Long id, Order orderDetails) {
        Order existingOrder = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (orderDetails.getTotalValue() != null) {
            existingOrder.setTotalValue(orderDetails.getTotalValue());
        }
        if (orderDetails.getStatus() != null) {
            existingOrder.setStatus(orderDetails.getStatus());
        }
        return repository.save(existingOrder);
    }

    @Transactional
    public Order cancel(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        // Liberar reservas de estoque
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                inventoryService.releaseWarehouseReservation(
                        order.getCompanyId(),
                        item.getProductId(),
                        DEFAULT_WAREHOUSE_ID,
                        item.getQuantity(),
                        order.getId()
                );
            }
            log.info("[ORDER_STOCK_RELEASED] Order {} - {} item(s) released",
                    order.getId(), order.getItems().size());
        }

        order.setStatus("CANCELLED");
        return repository.save(order);
    }
}