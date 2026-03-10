package com.erp.moveis.dto;

import lombok.Data;

@Data
public class ClientResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String profession;
    private String preferences;
    private Long createdAt;
}
