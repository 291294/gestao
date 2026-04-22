package com.erp.moveis.serviceassistance.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ServiceAssistanceResponse {

    private Long id;
    private Long companyId;
    private String clientName;
    private String clientAddress;
    private LocalDate scheduledDate;
    private String serviceDescription;
    private LocalDate materialRequestedAt;
    private String photoUrl;
    private String notes;
    private String status;
    private Boolean notificationSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
