package com.erp.moveis.delivery.mapper;

import com.erp.moveis.delivery.dto.*;
import com.erp.moveis.delivery.entity.Delivery;
import com.erp.moveis.delivery.entity.DeliveryItem;

import java.util.Collections;
import java.util.stream.Collectors;

public class DeliveryMapper {

    private DeliveryMapper() {}

    public static DeliveryResponse toResponse(Delivery delivery) {
        DeliveryResponse dto = new DeliveryResponse();
        dto.setId(delivery.getId());
        dto.setCompanyId(delivery.getCompanyId());
        dto.setOrderId(delivery.getOrderId());
        dto.setDeliveryNumber(delivery.getDeliveryNumber());
        dto.setStatus(delivery.getStatus());
        dto.setScheduledDate(delivery.getScheduledDate());
        dto.setShippedDate(delivery.getShippedDate());
        dto.setDeliveredDate(delivery.getDeliveredDate());
        dto.setDeliveryAddress(delivery.getDeliveryAddress());
        dto.setReceiverName(delivery.getReceiverName());
        dto.setDriverName(delivery.getDriverName());
        dto.setNotes(delivery.getNotes());
        dto.setCreatedAt(delivery.getCreatedAt());
        dto.setUpdatedAt(delivery.getUpdatedAt());

        if (delivery.getItems() != null) {
            dto.setItems(delivery.getItems().stream()
                    .map(DeliveryMapper::toItemResponse)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(Collections.emptyList());
        }
        return dto;
    }

    public static DeliveryItemResponse toItemResponse(DeliveryItem item) {
        DeliveryItemResponse dto = new DeliveryItemResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setNotes(item.getNotes());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    public static Delivery toEntity(DeliveryRequest request) {
        Delivery delivery = new Delivery();
        delivery.setCompanyId(request.getCompanyId());
        delivery.setOrderId(request.getOrderId());
        delivery.setScheduledDate(request.getScheduledDate());
        delivery.setDeliveryAddress(request.getDeliveryAddress());
        delivery.setReceiverName(request.getReceiverName());
        delivery.setDriverName(request.getDriverName());
        delivery.setNotes(request.getNotes());

        if (request.getItems() != null) {
            for (DeliveryItemRequest itemReq : request.getItems()) {
                DeliveryItem item = toItemEntity(itemReq);
                delivery.addItem(item);
            }
        }
        return delivery;
    }

    public static DeliveryItem toItemEntity(DeliveryItemRequest request) {
        DeliveryItem item = new DeliveryItem();
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setNotes(request.getNotes());
        return item;
    }
}
