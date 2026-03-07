package com.erp.moveis.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    private Long companyId;
    private Long orderId;
    private LocalDate scheduledDate;
    private String deliveryAddress;
    private String receiverName;
    private String driverName;
    private String notes;
    private List<DeliveryItemRequest> items = new ArrayList<>();
}
