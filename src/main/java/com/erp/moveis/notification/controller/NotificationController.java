package com.erp.moveis.notification.controller;

import com.erp.moveis.notification.entity.Notification;
import com.erp.moveis.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/unread")
    public List<Notification> unread(@RequestParam Long companyId) {
        return service.getUnread(companyId);
    }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        service.markAsRead(id);
    }
}
