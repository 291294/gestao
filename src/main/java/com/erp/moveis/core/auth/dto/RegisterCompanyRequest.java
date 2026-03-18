package com.erp.moveis.core.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class RegisterCompanyRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @Size(min = 14, max = 18, message = "CNPJ must be between 14 and 18 characters")
    private String cnpj;

    @NotBlank(message = "Admin username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Admin email is required")
    @Email
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6)
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    public RegisterCompanyRequest() {
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
