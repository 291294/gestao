package com.erp.moveis.controller;

import com.erp.moveis.core.auth.dto.LoginRequest;
import com.erp.moveis.core.company.entity.Company;
import com.erp.moveis.core.company.repository.CompanyRepository;
import com.erp.moveis.core.permission.entity.Permission;
import com.erp.moveis.core.permission.repository.PermissionRepository;
import com.erp.moveis.core.role.entity.Role;
import com.erp.moveis.core.role.repository.RoleRepository;
import com.erp.moveis.core.user.entity.User;
import com.erp.moveis.core.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String accessToken;
    private static Company testCompany;

    @BeforeEach
    void setUp() throws Exception {
        if (testCompany == null) {
            testCompany = companyRepository.findAll().stream().findFirst().orElseGet(() -> {
                Company c = new Company();
                c.setName("Test Company");
                c.setCnpj("12345678000100");
                c.setActive(true);
                return companyRepository.save(c);
            });
        }

        Role role = roleRepository.findByName("PRODUCT_TESTER").orElseGet(() -> {
            Permission productList = findOrCreatePermission("product", "list");
            Permission productView = findOrCreatePermission("product", "view");
            Permission productCreate = findOrCreatePermission("product", "create");
            Permission productUpdate = findOrCreatePermission("product", "update");
            Permission productDelete = findOrCreatePermission("product", "delete");

            Role r = new Role();
            r.setName("PRODUCT_TESTER");
            r.setDescription("Product Tester Role");
            r.setCompany(testCompany);
            r.setActive(true);
            r.setPermissions(Set.of(productList, productView, productCreate, productUpdate, productDelete));
            return roleRepository.save(r);
        });

        if (userRepository.findByUsername("producttest").isEmpty()) {
            User u = new User();
            u.setUsername("producttest");
            u.setEmail("producttest@test.com");
            u.setPasswordHash(passwordEncoder.encode("test123"));
            u.setFullName("Product Tester");
            u.setCompany(testCompany);
            u.setActive(true);
            u.addRole(role);
            userRepository.save(u);
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("producttest");
        loginReq.setPassword("test123");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        accessToken = objectMapper.readTree(response).get("accessToken").asText();
    }

    private Permission findOrCreatePermission(String resource, String action) {
        return permissionRepository.findByResourceAndAction(resource, action)
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setResource(resource);
                    p.setAction(action);
                    p.setDescription(resource + " " + action);
                    return permissionRepository.save(p);
                });
    }

    // ── LIST ────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("GET /products — should list all products")
    void shouldListProducts() throws Exception {
        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /products/page — should return paginated products")
    void shouldListPagedProducts() throws Exception {
        mockMvc.perform(get("/products/page")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(5)));
    }

    // ── CREATE ──────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("POST /products — should create product with valid data")
    void shouldCreateProduct() throws Exception {
        String body = """
                {
                    "name": "Mesa Escritório Premium",
                    "material": "MDF",
                    "color": "Carvalho",
                    "basePrice": 1299.90
                }
                """;

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Mesa Escritório Premium")))
                .andExpect(jsonPath("$.basePrice", is(1299.90)));
    }

    @Test
    @Order(4)
    @DisplayName("POST /products — should reject blank name (400)")
    void shouldRejectBlankName() throws Exception {
        String body = """
                {
                    "name": "",
                    "material": "MDF",
                    "basePrice": 100.0
                }
                """;

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("POST /products — should reject negative price (400)")
    void shouldRejectNegativePrice() throws Exception {
        String body = """
                {
                    "name": "Cadeira Teste",
                    "basePrice": -50.0
                }
                """;

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── GET BY ID ───────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("GET /products/{id} — should return 404 for non-existent product")
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/products/99999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    // ── UPDATE ──────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("PUT /products/{id} — should update existing product")
    void shouldUpdateProduct() throws Exception {
        String createBody = """
                {"name": "Produto Update", "material": "Madeira", "basePrice": 500.0}
                """;
        String createResp = mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResp).get("id").asLong();

        String updateBody = """
                {"name": "Produto Atualizado", "material": "MDF Premium", "color": "Branco", "basePrice": 750.0}
                """;
        mockMvc.perform(put("/products/" + id)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Produto Atualizado")))
                .andExpect(jsonPath("$.basePrice", is(750.0)));
    }

    // ── DELETE ──────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("DELETE /products/{id} — should delete product")
    void shouldDeleteProduct() throws Exception {
        String body = """
                {"name": "Delete Me", "material": "Test", "basePrice": 10.0}
                """;
        String resp = mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(resp).get("id").asLong();

        mockMvc.perform(delete("/products/" + id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/" + id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    // ── SECURITY ────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("GET /products — should reject unauthenticated request (401)")
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(10)
    @DisplayName("POST /products — should reject without create permission (403)")
    void shouldRejectWithoutPermission() throws Exception {
        Role readOnlyRole = roleRepository.findByName("PRODUCT_READONLY").orElseGet(() -> {
            Permission productList = findOrCreatePermission("product", "list");
            Role r = new Role();
            r.setName("PRODUCT_READONLY");
            r.setDescription("Read-only product access");
            r.setCompany(testCompany);
            r.setActive(true);
            r.setPermissions(Set.of(productList));
            return roleRepository.save(r);
        });

        if (userRepository.findByUsername("product_readonly").isEmpty()) {
            User u = new User();
            u.setUsername("product_readonly");
            u.setEmail("prodreadonly@test.com");
            u.setPasswordHash(passwordEncoder.encode("test123"));
            u.setFullName("Read Only Product User");
            u.setCompany(testCompany);
            u.setActive(true);
            u.addRole(readOnlyRole);
            userRepository.save(u);
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("product_readonly");
        loginReq.setPassword("test123");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String readOnlyToken = objectMapper.readTree(response).get("accessToken").asText();

        String body = """
                {"name": "Should Fail", "basePrice": 10.0}
                """;
        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + readOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}
