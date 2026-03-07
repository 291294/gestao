package com.erp.moveis.core.permission.repository;

import com.erp.moveis.core.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByResourceAndAction(String resource, String action);

    List<Permission> findByResource(String resource);

    boolean existsByResourceAndAction(String resource, String action);
}
