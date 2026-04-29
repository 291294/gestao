package com.erp.moveis.repository;

import com.erp.moveis.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCompanyId(Long companyId);
    Page<Order> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Order> findByIdAndCompanyId(Long id, Long companyId);

    @Query("SELECT o FROM Order o WHERE o.companyId = :companyId AND o.client.id = :clientId")
    List<Order> findByCompanyIdAndClientId(@Param("companyId") Long companyId, @Param("clientId") Long clientId);
}