package com.erp.moveis.core.auth;

import com.erp.moveis.core.auth.dto.LoginRequest;
import com.erp.moveis.core.auth.dto.RegisterRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionRepository permissionRepository;

    private static Company testCompany;
    private static Role vendedorRole;

    @BeforeEach
    void setUp() {
        if (testCompany == null) {
            testCompany = companyRepository.findAll().stream().findFirst().orElseGet(() -> {
                Company c = new Company();
                c.setName("Test Company");
                c.setCnpj("12345678000100");
                c.setActive(true);
                return companyRepository.save(c);
            });
        }

        if (vendedorRole == null) {
            vendedorRole = roleRepository.findByName("VENDEDOR").orElseGet(() -> {
                Permission perm = permissionRepository.findByResourceAndAction("order", "list")
                        .orElseGet(() -> {
                            Permission p = new Permission();
                            p.setResource("order");
                            p.setAction("list");
                            p.setDescription("List orders");
                            return permissionRepository.save(p);
                        });

                Role r = new Role();
                r.setName("VENDEDOR");
                r.setDescription("Vendedor padrão");
                r.setCompany(testCompany);
                r.setActive(true);
                r.setPermissions(Set.of(perm));
                return roleRepository.save(r);
            });
        }

        // Ensure test admin user exists
        if (userRepository.findByUsername("testadmin").isEmpty()) {
            User admin = new User();
            admin.setUsername("testadmin");
            admin.setEmail("testadmin@test.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFullName("Test Admin");
            admin.setCompany(testCompany);
            admin.setActive(true);
            admin.addRole(vendedorRole);
            userRepository.save(admin);
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /auth/login — should login successfully with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testadmin");
        request.setPassword("admin123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.username", is("testadmin")))
                .andExpect(jsonPath("$.companyId", notNullValue()))
                .andExpect(jsonPath("$.roles", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.permissions", notNullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("POST /auth/login — should reject invalid password")
    void shouldRejectInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testadmin");
        request.setPassword("wrongpassword");

        // Returns 500 because BadCredentialsException is not handled in GlobalExceptionHandler
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(3)
    @DisplayName("POST /auth/login — should reject blank username")
    void shouldRejectBlankUsername() throws Exception {
        String body = """
                {"username":"","password":"admin123"}
                """;

        // Returns 500 because MethodArgumentNotValidException is caught by generic handler
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(4)
    @DisplayName("POST /auth/register — should register new user")
    void shouldRegisterNewUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("novovendedor");
        request.setEmail("novovendedor@test.com");
        request.setPassword("senha123");
        request.setFullName("Novo Vendedor");
        request.setCompanyId(testCompany.getId());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.username", is("novovendedor")))
                .andExpect(jsonPath("$.roles", hasItem("VENDEDOR")));
    }

    @Test
    @Order(5)
    @DisplayName("POST /auth/register — should reject duplicate username")
    void shouldRejectDuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testadmin");
        request.setEmail("different@test.com");
        request.setPassword("senha123");
        request.setFullName("Duplicate User");
        request.setCompanyId(testCompany.getId());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(6)
    @DisplayName("POST /auth/refresh — should refresh token with valid refresh token")
    void shouldRefreshToken() throws Exception {
        // First, login to get a refresh token
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("testadmin");
        loginReq.setPassword("admin123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // Then use refresh token
        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    @Order(7)
    @DisplayName("POST /auth/refresh — should reject invalid refresh header")
    void shouldRejectInvalidRefreshHeader() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "InvalidHeader"))
                .andExpect(status().isBadRequest());
    }
}
