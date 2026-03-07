package com.erp.moveis.core.config;

import com.erp.moveis.core.company.entity.Company;
import com.erp.moveis.core.company.repository.CompanyRepository;
import com.erp.moveis.core.permission.entity.Permission;
import com.erp.moveis.core.permission.repository.PermissionRepository;
import com.erp.moveis.core.role.entity.Role;
import com.erp.moveis.core.role.repository.RoleRepository;
import com.erp.moveis.core.user.entity.User;
import com.erp.moveis.core.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("dev")
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Criar empresa demo se não existir
            Company company = companyRepository.findByCnpj("00.000.000/0001-00")
                    .orElseGet(() -> {
                        Company newCompany = new Company();
                        newCompany.setName("Empresa Demo");
                        newCompany.setCnpj("00.000.000/0001-00");
                        newCompany.setActive(true);
                        return companyRepository.save(newCompany);
                    });

            // Criar permissões se não existirem
            if (permissionRepository.count() == 0) {
                createPermissions(permissionRepository);
            }

            // Criar roles se não existirem
            if (roleRepository.count() == 0) {
                createRoles(roleRepository, permissionRepository);
            }

            // Criar usuário admin se não existir
            if (!userRepository.existsByUsername("admin")) {
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@erp-moveis.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setFullName("Administrador");
                admin.setCompany(company);
                admin.setActive(true);
                admin.addRole(adminRole);

                userRepository.save(admin);
                System.out.println("✅ Usuário admin criado: admin/admin123");
            }
        };
    }

    private void createPermissions(PermissionRepository permissionRepository) {
        String[][] resources = {
                {"client", "create,view,update,delete,list"},
                {"product", "create,view,update,delete,list"},
                {"project", "create,view,update,delete,list"},
                {"order", "create,view,update,delete,list,approve,cancel"},
                {"payment", "create,view,update,delete,list,approve"},
                {"invoice", "create,view,update,delete,list,generate"},
                {"delivery", "create,view,update,delete,list,schedule,complete"},
                {"user", "create,view,update,delete,list,assign_role"},
                {"report", "sales,financial,inventory,production"}
        };

        for (String[] resource : resources) {
            String resourceName = resource[0];
            String[] actions = resource[1].split(",");

            for (String action : actions) {
                Permission permission = new Permission();
                permission.setResource(resourceName);
                permission.setAction(action);
                permission.setDescription(resourceName + "." + action);
                permissionRepository.save(permission);
            }
        }
        System.out.println("✅ " + permissionRepository.count() + " permissões criadas");
    }

    private void createRoles(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        // ADMIN - todas as permissões
        Role admin = new Role();
        admin.setName("ADMIN");
        admin.setDescription("Administrador do sistema - acesso total");
        admin.setActive(true);
        permissionRepository.findAll().forEach(admin::addPermission);
        roleRepository.save(admin);

        // GERENTE
        Role gerente = new Role();
        gerente.setName("GERENTE");
        gerente.setDescription("Gerente - aprovações e relatórios");
        gerente.setActive(true);
        addPermissionsByPattern(gerente, permissionRepository,
                "client.*", "product.*", "project.*", "order.*", "report.*");
        roleRepository.save(gerente);

        // VENDEDOR
        Role vendedor = new Role();
        vendedor.setName("VENDEDOR");
        vendedor.setDescription("Vendedor - clientes e pedidos");
        vendedor.setActive(true);
        addPermissionsByPattern(vendedor, permissionRepository,
                "client.*", "order.create", "order.view", "order.update", "order.list");
        roleRepository.save(vendedor);

        // FINANCEIRO
        Role financeiro = new Role();
        financeiro.setName("FINANCEIRO");
        financeiro.setDescription("Financeiro - pagamentos e notas fiscais");
        financeiro.setActive(true);
        addPermissionsByPattern(financeiro, permissionRepository,
                "payment.*", "invoice.*", "report.financial");
        roleRepository.save(financeiro);

        // LOGISTICA
        Role logistica = new Role();
        logistica.setName("LOGISTICA");
        logistica.setDescription("Logística - entregas");
        logistica.setActive(true);
        addPermissionsByPattern(logistica, permissionRepository,
                "delivery.*", "order.view", "order.list");
        roleRepository.save(logistica);

        // PRODUCAO
        Role producao = new Role();
        producao.setName("PRODUCAO");
        producao.setDescription("Produção - relatórios de produção");
        producao.setActive(true);
        addPermissionsByPattern(producao, permissionRepository,
                "product.view", "product.list", "report.production");
        roleRepository.save(producao);

        System.out.println("✅ 6 roles criadas");
    }

    private void addPermissionsByPattern(Role role, PermissionRepository permissionRepository, String... patterns) {
        for (String pattern : patterns) {
            if (pattern.endsWith(".*")) {
                String resource = pattern.substring(0, pattern.length() - 2);
                permissionRepository.findByResource(resource).forEach(role::addPermission);
            } else {
                String[] parts = pattern.split("\\.");
                if (parts.length == 2) {
                    permissionRepository.findByResourceAndAction(parts[0], parts[1])
                            .ifPresent(role::addPermission);
                }
            }
        }
    }
}
