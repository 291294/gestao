package com.erp.moveis.notification.service;

import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.notification.entity.Notification;
import com.erp.moveis.notification.repository.NotificationRepository;
import com.erp.moveis.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    @Override
    public Notification create(Long companyId, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setCompanyId(companyId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        return repository.save(notification);
    }

    @Override
    public List<Notification> getUnread(Long companyId) {
        return repository.findByCompanyIdAndReadFalse(companyId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        repository.save(notification);
    }
}
