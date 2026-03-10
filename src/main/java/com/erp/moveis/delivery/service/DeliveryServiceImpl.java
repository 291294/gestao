package com.erp.moveis.delivery.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.delivery.dto.DeliveryItemRequest;
import com.erp.moveis.delivery.dto.DeliveryRequest;
import com.erp.moveis.delivery.dto.DeliveryResponse;
import com.erp.moveis.delivery.entity.Delivery;
import com.erp.moveis.delivery.entity.DeliveryItem;
import com.erp.moveis.delivery.entity.DeliveryStatus;
import com.erp.moveis.delivery.mapper.DeliveryMapper;
import com.erp.moveis.delivery.repository.DeliveryRepository;
import com.erp.moveis.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final InventoryService inventoryService;

    // ── CRUD ───────────────────────────────────────────────────

    @Override
    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest request) {
        Delivery delivery = DeliveryMapper.toEntity(request);
        delivery.setDeliveryNumber(generateDeliveryNumber());
        delivery.setStatus(DeliveryStatus.PENDING);
        Delivery saved = deliveryRepository.save(delivery);
        return DeliveryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDelivery(Long id) {
        Delivery delivery = deliveryRepository.findFullDelivery(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
        return DeliveryMapper.toResponse(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByOrder(Long orderId) {
        return deliveryRepository.findByOrderId(orderId).stream()
                .map(DeliveryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByCompanyAndStatus(Long companyId, DeliveryStatus status) {
        return deliveryRepository.findByCompanyIdAndStatus(companyId, status).stream()
                .map(DeliveryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getInTransit(Long companyId) {
        return deliveryRepository.findInTransit(companyId).stream()
                .map(DeliveryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getScheduledForDate(LocalDate date) {
        return deliveryRepository.findScheduledForDate(date).stream()
                .map(DeliveryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Workflow ────────────────────────────────────────────────

    @Override
    @Transactional
    public DeliveryResponse ship(Long id) {
        Delivery delivery = findEntityById(id);
        if (delivery.getStatus() != DeliveryStatus.PENDING && delivery.getStatus() != DeliveryStatus.PREPARING) {
            throw new BusinessException("Only PENDING or PREPARING deliveries can be shipped. Current: " + delivery.getStatus());
        }

        // Baixa de estoque para cada item da entrega (item-based)
        for (DeliveryItem item : delivery.getItems()) {
            inventoryService.removeStockByProduct(
                    delivery.getCompanyId(),
                    item.getProductId(),
                    item.getQuantity(),
                    "DELIVERY",
                    delivery.getId(),
                    "Saída por entrega " + delivery.getDeliveryNumber()
            );

            // Consumo no warehouse stock (RESERVATION → SALE)
            inventoryService.removeWarehouseStock(
                    delivery.getCompanyId(),
                    item.getProductId(),
                    1L,
                    java.math.BigDecimal.valueOf(item.getQuantity())
            );
        }

        delivery.setStatus(DeliveryStatus.IN_TRANSIT);
        delivery.setShippedDate(LocalDateTime.now());
        return DeliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    @Override
    @Transactional
    public DeliveryResponse deliver(Long id) {
        Delivery delivery = findEntityById(id);
        if (delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
            throw new BusinessException("Only IN_TRANSIT deliveries can be marked as delivered. Current: " + delivery.getStatus());
        }
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredDate(LocalDateTime.now());
        return DeliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    @Override
    @Transactional
    public DeliveryResponse cancel(Long id) {
        Delivery delivery = findEntityById(id);
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel a delivered delivery");
        }
        delivery.setStatus(DeliveryStatus.CANCELLED);
        return DeliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    @Override
    @Transactional
    public DeliveryResponse addItem(Long deliveryId, DeliveryItemRequest itemRequest) {
        Delivery delivery = findEntityById(deliveryId);
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new BusinessException("Items can only be added to PENDING deliveries");
        }
        DeliveryItem item = DeliveryMapper.toItemEntity(itemRequest);
        delivery.addItem(item);
        return DeliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    // ── Helpers ────────────────────────────────────────────────

    private Delivery findEntityById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
    }

    private String generateDeliveryNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String uid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ENT-" + year + "-" + uid;
    }
}
