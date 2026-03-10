package com.erp.moveis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String name;
    private String phone;
    @Email(message = "Email inválido")
    private String email;
    private String profession;
    private String preferences;
}
