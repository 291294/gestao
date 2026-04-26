package com.erp.moveis.core.auth.service;

import com.erp.moveis.core.auth.dto.LoginRequest;
import com.erp.moveis.core.auth.dto.RegisterCompanyRequest;
import com.erp.moveis.core.auth.dto.RegisterRequest;
import com.erp.moveis.core.auth.dto.TokenResponse;
import com.erp.moveis.core.auth.entity.RefreshToken;
import com.erp.moveis.core.company.entity.Company;
import com.erp.moveis.core.company.repository.CompanyRepository;
import com.erp.moveis.core.role.entity.Role;
import com.erp.moveis.core.role.repository.RoleRepository;
import com.erp.moveis.core.security.jwt.JwtService;
import com.erp.moveis.core.user.entity.User;
import com.erp.moveis.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public TokenResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return buildResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        Role defaultRole = roleRepository.findByName("VENDEDOR")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setCompany(company);
        user.setActive(true);
        user.addRole(defaultRole);
        user = userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return buildResponse(user, accessToken, refreshToken.getToken());
    }

    /**
     * Rotaciona o refresh token:
     * - Valida no banco
     * - Revoga o antigo
     * - Gera novo refresh + novo access
     * - Detecta replay attack automaticamente
     */
    @Transactional
    public TokenResponse refreshToken(String tokenValue) {
        RefreshToken newRefreshToken = refreshTokenService.rotate(tokenValue);
        User user = newRefreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        return buildResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    /**
     * Logout: revoga todos os refresh tokens ativos do usuário.
     */
    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> refreshTokenService.revokeAllByUser(user.getId()));
    }

    /**
     * Troca de senha: atualiza o hash e invalida todos os refresh tokens.
     */
    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        refreshTokenService.revokeAllByUser(user.getId());
    }

    @Transactional
    public TokenResponse registerCompany(RegisterCompanyRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Company company = new Company(request.getCompanyName(), request.getCnpj());
        company = companyRepository.save(company);

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setCompany(company);
        user.setActive(true);
        user.addRole(adminRole);
        user = userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return buildResponse(user, accessToken, refreshToken.getToken());
    }

    private TokenResponse buildResponse(User user, String accessToken, String refreshToken) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissionList = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getResource() + "." + p.getAction())
                .distinct()
                .collect(Collectors.toList());

        Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyId(companyId)
                .roles(roleNames)
                .permissions(permissionList)
                .build();
    }
}
