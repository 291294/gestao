package com.erp.moveis.notification.service;

import com.erp.moveis.notification.entity.Notification;
import com.erp.moveis.notification.type.NotificationType;

import java.util.List;

public interface NotificationService {

    Notification create(Long companyId, NotificationType type, String title, String message);

    List<Notification> getUnread(Long companyId);

    void markAsRead(Long notificationId);
}
