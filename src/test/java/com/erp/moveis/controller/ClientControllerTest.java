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
class ClientControllerTest {

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

        Role role = roleRepository.findByName("CLIENT_TESTER").orElseGet(() -> {
            Permission clientList = findOrCreatePermission("client", "list");
            Permission clientView = findOrCreatePermission("client", "view");
            Permission clientCreate = findOrCreatePermission("client", "create");
            Permission clientUpdate = findOrCreatePermission("client", "update");
            Permission clientDelete = findOrCreatePermission("client", "delete");

            Role r = new Role();
            r.setName("CLIENT_TESTER");
            r.setDescription("Client Tester Role");
            r.setCompany(testCompany);
            r.setActive(true);
            r.setPermissions(Set.of(clientList, clientView, clientCreate, clientUpdate, clientDelete));
            return roleRepository.save(r);
        });

        if (userRepository.findByUsername("clienttest").isEmpty()) {
            User u = new User();
            u.setUsername("clienttest");
            u.setEmail("clienttest@test.com");
            u.setPasswordHash(passwordEncoder.encode("test123"));
            u.setFullName("Client Tester");
            u.setCompany(testCompany);
            u.setActive(true);
            u.addRole(role);
            userRepository.save(u);
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("clienttest");
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
    @DisplayName("GET /clients — should list all clients")
    void shouldListClients() throws Exception {
        mockMvc.perform(get("/clients")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /clients/page — should return paginated clients")
    void shouldListPagedClients() throws Exception {
        mockMvc.perform(get("/clients/page")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    // ── CREATE ──────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("POST /clients — should create client with valid data")
    void shouldCreateClient() throws Exception {
        String body = """
                {
                    "name": "João Silva",
                    "phone": "11999887766",
                    "email": "joao@test.com",
                    "profession": "Arquiteto",
                    "preferences": "Madeira maciça"
                }
                """;

        mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("João Silva")))
                .andExpect(jsonPath("$.email", is("joao@test.com")));
    }

    @Test
    @Order(4)
    @DisplayName("POST /clients — should reject blank name (400)")
    void shouldRejectBlankName() throws Exception {
        String body = """
                {
                    "name": "",
                    "phone": "11999887766",
                    "email": "test@test.com"
                }
                """;

        mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("POST /clients — should reject invalid email (400)")
    void shouldRejectInvalidEmail() throws Exception {
        String body = """
                {
                    "name": "Maria",
                    "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── GET BY ID ───────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("GET /clients/{id} — should return 404 for non-existent client")
    void shouldReturn404ForNonExistentClient() throws Exception {
        mockMvc.perform(get("/clients/99999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    // ── UPDATE ──────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("PUT /clients/{id} — should update existing client")
    void shouldUpdateClient() throws Exception {
        // Create first
        String createBody = """
                {"name": "Update Test", "phone": "11000000000", "email": "update@test.com"}
                """;
        String createResp = mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResp).get("id").asLong();

        // Update
        String updateBody = """
                {"name": "Updated Name", "phone": "11111111111", "email": "updated@test.com"}
                """;
        mockMvc.perform(put("/clients/" + id)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.email", is("updated@test.com")));
    }

    // ── DELETE ──────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("DELETE /clients/{id} — should delete client")
    void shouldDeleteClient() throws Exception {
        // Create first
        String body = """
                {"name": "Delete Me", "phone": "11000000000", "email": "delete@test.com"}
                """;
        String resp = mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(resp).get("id").asLong();

        mockMvc.perform(delete("/clients/" + id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Verify deleted
        mockMvc.perform(get("/clients/" + id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    // ── SECURITY ────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("GET /clients — should reject unauthenticated request (401)")
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(10)
    @DisplayName("POST /clients — should reject request without create permission (403)")
    void shouldRejectWithoutPermission() throws Exception {
        // Create user with only list permission (no create)
        Role readOnlyRole = roleRepository.findByName("CLIENT_READONLY").orElseGet(() -> {
            Permission clientList = findOrCreatePermission("client", "list");
            Role r = new Role();
            r.setName("CLIENT_READONLY");
            r.setDescription("Read-only client access");
            r.setCompany(testCompany);
            r.setActive(true);
            r.setPermissions(Set.of(clientList));
            return roleRepository.save(r);
        });

        if (userRepository.findByUsername("client_readonly").isEmpty()) {
            User u = new User();
            u.setUsername("client_readonly");
            u.setEmail("readonly@test.com");
            u.setPasswordHash(passwordEncoder.encode("test123"));
            u.setFullName("Read Only User");
            u.setCompany(testCompany);
            u.setActive(true);
            u.addRole(readOnlyRole);
            userRepository.save(u);
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("client_readonly");
        loginReq.setPassword("test123");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String readOnlyToken = objectMapper.readTree(response).get("accessToken").asText();

        // Try to create — should be 403
        String body = """
                {"name": "Should Fail", "email": "fail@test.com"}
                """;
        mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + readOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}
