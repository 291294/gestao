package com.erp.moveis.core.company.repository;

import com.erp.moveis.core.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByCnpj(String cnpj);

    List<Company> findByActive(Boolean active);

    boolean existsByCnpj(String cnpj);
}
