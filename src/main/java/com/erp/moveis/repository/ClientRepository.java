package com.erp.moveis.repository;

import com.erp.moveis.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByCompanyId(Long companyId);
    Page<Client> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Client> findByIdAndCompanyId(Long id, Long companyId);
}