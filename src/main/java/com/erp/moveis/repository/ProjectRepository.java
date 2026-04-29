package com.erp.moveis.repository;

import com.erp.moveis.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCompanyIdAndClientId(Long companyId, Long clientId);
    List<Project> findByCompanyId(Long companyId);
    Optional<Project> findByIdAndCompanyId(Long id, Long companyId);
}