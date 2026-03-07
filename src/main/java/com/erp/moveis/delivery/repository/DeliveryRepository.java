package com.erp.moveis.delivery.repository;

import com.erp.moveis.delivery.entity.Delivery;
import com.erp.moveis.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByDeliveryNumber(String deliveryNumber);

    List<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByCompanyIdAndStatus(Long companyId, DeliveryStatus status);

    List<Delivery> findByStatus(DeliveryStatus status);

    @Query("SELECT d FROM Delivery d LEFT JOIN FETCH d.items WHERE d.id = :id")
    Optional<Delivery> findFullDelivery(@Param("id") Long id);

    @Query("SELECT d FROM Delivery d WHERE d.scheduledDate = :date AND d.status = 'PENDING'")
    List<Delivery> findScheduledForDate(@Param("date") LocalDate date);

    @Query("SELECT d FROM Delivery d WHERE d.status = 'IN_TRANSIT' AND d.companyId = :companyId")
    List<Delivery> findInTransit(@Param("companyId") Long companyId);
}
