package com.erp.moveis.serviceassistance.repository;

import com.erp.moveis.serviceassistance.entity.ServiceAssistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceAssistanceRepository extends JpaRepository<ServiceAssistance, Long> {

    List<ServiceAssistance> findByCompanyIdOrderByScheduledDateAsc(Long companyId);

    Optional<ServiceAssistance> findByIdAndCompanyId(Long id, Long companyId);

    @Query("SELECT s FROM ServiceAssistance s WHERE s.companyId = :companyId AND s.scheduledDate = :date AND s.notificationSent = false AND s.status = 'SCHEDULED'")
    List<ServiceAssistance> findPendingNotificationsForDate(@Param("companyId") Long companyId, @Param("date") LocalDate date);

    @Query("SELECT s FROM ServiceAssistance s WHERE s.scheduledDate = :date AND s.notificationSent = false AND s.status = 'SCHEDULED'")
    List<ServiceAssistance> findAllPendingNotificationsForDate(@Param("date") LocalDate date);
}
