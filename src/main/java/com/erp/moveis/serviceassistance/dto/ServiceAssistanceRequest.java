package com.erp.moveis.serviceassistance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ServiceAssistanceRequest {

    @NotBlank(message = "Nome do cliente é obrigatório")
    private String clientName;

    @NotBlank(message = "Endereço do cliente é obrigatório")
    private String clientAddress;

    @NotNull(message = "Data de agendamento é obrigatória")
    private LocalDate scheduledDate;

    @NotBlank(message = "Descrição do serviço é obrigatória")
    private String serviceDescription;

    private LocalDate materialRequestedAt;

    private String photoUrl;

    private String notes;

    private String status;
}
