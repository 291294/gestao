package com.erp.moveis.promob.repository;

import com.erp.moveis.promob.entity.PromobProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromobProjectRepository extends JpaRepository<PromobProject, Long> {

    List<PromobProject> findByCompanyIdOrderByImportedAtDesc(Long companyId);

    List<PromobProject> findByCompanyIdAndStatus(Long companyId, String status);

    boolean existsByCompanyIdAndFileName(Long companyId, String fileName);
}
