package com.erp.moveis.notification.repository;

import com.erp.moveis.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCompanyIdAndReadFalse(Long companyId);
}
