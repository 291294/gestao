package com.erp.moveis.core.user.controller;

import com.erp.moveis.core.role.entity.Role;
import com.erp.moveis.core.role.repository.RoleRepository;
import com.erp.moveis.core.user.entity.User;
import com.erp.moveis.core.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuarios", description = "Gestao de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user.list')")
    @Operation(summary = "Listar todos os usuarios")
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAuthority('user.list')")
    @Operation(summary = "Listar usuarios por empresa")
    public ResponseEntity<List<Map<String, Object>>> listByCompany(@PathVariable Long companyId) {
        List<User> users = userRepository.findByCompanyId(companyId);
        return ResponseEntity.ok(users.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.view')")
    @Operation(summary = "Buscar usuario por ID")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toDto(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user.create')")
    @Operation(summary = "Criar novo usuario")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String email = (String) request.get("email");
        String password = (String) request.get("password");
        String fullName = (String) request.get("fullName");
        List<String> roleNames = request.get("roles") != null ? (List<String>) request.get("roles") : List.of();

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username ja existe"));
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email ja existe"));
        }

        User user = new User(username, email, passwordEncoder.encode(password), fullName);
        user.setActive(true);

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roleRepository.findByName(roleName).ifPresent(roles::add);
        }
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toDto(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.update')")
    @Operation(summary = "Atualizar usuario")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        return userRepository.findById(id).map(user -> {
            if (request.containsKey("email")) user.setEmail((String) request.get("email"));
            if (request.containsKey("fullName")) user.setFullName((String) request.get("fullName"));
            if (request.containsKey("active")) user.setActive((Boolean) request.get("active"));
            if (request.containsKey("password") && request.get("password") != null && !((String) request.get("password")).isBlank()) {
                user.setPasswordHash(passwordEncoder.encode((String) request.get("password")));
            }
            if (request.containsKey("roles")) {
                List<String> roleNames = (List<String>) request.get("roles");
                Set<Role> roles = new HashSet<>();
                for (String roleName : roleNames) {
                    roleRepository.findByName(roleName).ifPresent(roles::add);
                }
                user.setRoles(roles);
            }
            User saved = userRepository.save(user);
            return ResponseEntity.ok(toDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user.delete')")
    @Operation(summary = "Desativar usuario")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('user.list')")
    @Operation(summary = "Listar roles disponiveis")
    public ResponseEntity<List<Map<String, Object>>> listRoles() {
        List<Role> roles = roleRepository.findByActive(true);
        return ResponseEntity.ok(roles.stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("name", r.getName());
            map.put("description", r.getDescription());
            return map;
        }).collect(Collectors.toList()));
    }

    private Map<String, Object> toDto(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        map.put("active", user.getActive());
        map.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        return map;
    }
}
