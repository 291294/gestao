package com.erp.moveis.delivery.dto;

import com.erp.moveis.delivery.entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private Long id;
    private Long companyId;
    private Long orderId;
    private String deliveryNumber;
    private DeliveryStatus status;
    private LocalDate scheduledDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private String deliveryAddress;
    private String receiverName;
    private String driverName;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeliveryItemResponse> items = new ArrayList<>();
}
