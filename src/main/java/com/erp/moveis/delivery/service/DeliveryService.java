package com.erp.moveis.delivery.service;

import com.erp.moveis.delivery.dto.DeliveryItemRequest;
import com.erp.moveis.delivery.dto.DeliveryRequest;
import com.erp.moveis.delivery.dto.DeliveryResponse;
import com.erp.moveis.delivery.entity.DeliveryStatus;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryService {

    DeliveryResponse createDelivery(DeliveryRequest request);

    DeliveryResponse getDelivery(Long id);

    List<DeliveryResponse> getByOrder(Long orderId);

    List<DeliveryResponse> getByCompanyAndStatus(Long companyId, DeliveryStatus status);

    List<DeliveryResponse> getInTransit(Long companyId);

    List<DeliveryResponse> getScheduledForDate(LocalDate date);

    DeliveryResponse ship(Long id);

    DeliveryResponse deliver(Long id);

    DeliveryResponse cancel(Long id);

    DeliveryResponse addItem(Long deliveryId, DeliveryItemRequest itemRequest);
}
