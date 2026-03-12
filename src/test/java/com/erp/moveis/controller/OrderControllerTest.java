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
import com.erp.moveis.model.Client;
import com.erp.moveis.repository.ClientRepository;
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
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private static Company testCompany;
    private static Client testClient;

    @BeforeEach
    void setUp() throws Exception {
        // Setup company
        if (testCompany == null) {
            testCompany = companyRepository.findAll().stream().findFirst().orElseGet(() -> {
                Company c = new Company();
                c.setName("Test Company");
                c.setCnpj("12345678000100");
                c.setActive(true);
                return companyRepository.save(c);
            });
        }

        // Setup role with order permissions
        Role role = roleRepository.findByName("GERENTE").orElseGet(() -> {
            // Create order permissions
            Permission orderList = findOrCreatePermission("order", "list");
            Permission orderView = findOrCreatePermission("order", "view");
            Permission orderCreate = findOrCreatePermission("order", "create");
            Permission orderUpdate = findOrCreatePermission("order", "update");
            Permission orderDelete = findOrCreatePermission("order", "delete");

            Role r = new Role();
            r.setName("GERENTE");
            r.setDescription("Gerente");
            r.setCompany(testCompany);
            r.setActive(true);
            r.setPermissions(Set.of(orderList, orderView, orderCreate, orderUpdate, orderDelete));
            return roleRepository.save(r);
        });

        // Setup test user
        if (userRepository.findByUsername("ordertest").isEmpty()) {
            User u = new User();
            u.setUsername("ordertest");
            u.setEmail("ordertest@test.com");
            u.setPasswordHash(passwordEncoder.encode("test123"));
            u.setFullName("Order Tester");
            u.setCompany(testCompany);
            u.setActive(true);
            u.addRole(role);
            userRepository.save(u);
        }

        // Setup client
        if (testClient == null) {
            testClient = clientRepository.findAll().stream().findFirst().orElseGet(() -> {
                Client cl = new Client("Cliente Teste", "11999999999", "cliente@test.com", "Empresário", "");
                return clientRepository.save(cl);
            });
        }

        // Authenticate
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("ordertest");
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

    @Test
    @Order(1)
    @DisplayName("GET /orders — should list all orders")
    void shouldListOrders() throws Exception {
        mockMvc.perform(get("/orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /orders/page — should return paginated orders")
    void shouldListPagedOrders() throws Exception {
        mockMvc.perform(get("/orders/page")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    @Order(3)
    @DisplayName("POST /orders — should create order without items")
    void shouldCreateOrder() throws Exception {
        String body = String.format("""
                {
                    "companyId": %d,
                    "clientId": %d,
                    "status": "PENDING",
                    "items": []
                }
                """, testCompany.getId(), testClient.getId());

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.companyId", is(testCompany.getId().intValue())))
                .andExpect(jsonPath("$.clientId", is(testClient.getId().intValue())));
    }

    @Test
    @Order(4)
    @DisplayName("GET /orders/{id} — should return 404 for non-existent order")
    void shouldReturn404ForNonExistentOrder() throws Exception {
        mockMvc.perform(get("/orders/99999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("GET /orders/client/{clientId} — should return orders by client")
    void shouldGetOrdersByClient() throws Exception {
        mockMvc.perform(get("/orders/client/" + testClient.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Order(6)
    @DisplayName("GET /orders — should reject unauthenticated request")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        // Returns 403 because no AuthenticationEntryPoint is configured in SecurityConfig
        mockMvc.perform(get("/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /orders/{id} — should delete existing order")
    void shouldDeleteOrder() throws Exception {
        // Create an order first
        String body = String.format("""
                {
                    "companyId": %d,
                    "clientId": %d,
                    "status": "PENDING",
                    "items": []
                }
                """, testCompany.getId(), testClient.getId());

        String createResponse = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        // Delete it
        mockMvc.perform(delete("/orders/" + orderId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Verify it's gone
        mockMvc.perform(get("/orders/" + orderId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
