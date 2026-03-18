package com.erp.moveis.repository;

import com.erp.moveis.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCompanyId(Long companyId);
    Page<Product> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Product> findByIdAndCompanyId(Long id, Long companyId);
}