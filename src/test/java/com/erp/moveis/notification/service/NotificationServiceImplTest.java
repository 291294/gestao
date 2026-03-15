package com.erp.moveis.notification.service;

import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.notification.entity.Notification;
import com.erp.moveis.notification.repository.NotificationRepository;
import com.erp.moveis.notification.type.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository repository;
    @InjectMocks private NotificationServiceImpl service;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setCompanyId(1L);
        notification.setType(NotificationType.ORDER_CREATED);
        notification.setTitle("New Order");
        notification.setMessage("Order #100 created");
        notification.setRead(false);
    }

    @Test @DisplayName("create — should persist notification")
    void shouldCreate() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });

        Notification result = service.create(1L, NotificationType.ORDER_CREATED, "New Order", "Order #100 created");
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Order");
        verify(repository).save(any(Notification.class));
    }

    @Test @DisplayName("create — should accept all notification types")
    void shouldCreateWithDifferentTypes() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = service.create(1L, NotificationType.LOW_STOCK, "Low Stock", "Product X below minimum");
        assertThat(result.getType()).isEqualTo(NotificationType.LOW_STOCK);
    }

    @Test @DisplayName("getUnread — should return unread notifications")
    void shouldGetUnread() {
        when(repository.findByCompanyIdAndReadFalse(1L)).thenReturn(List.of(notification));
        List<Notification> result = service.getUnread(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRead()).isFalse();
    }

    @Test @DisplayName("markAsRead — should set read=true")
    void shouldMarkAsRead() {
        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markAsRead(1L);
        assertThat(notification.getRead()).isTrue();
        verify(repository).save(notification);
    }

    @Test @DisplayName("markAsRead — should throw when not found")
    void shouldThrowWhenNotFoundOnMarkAsRead() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.markAsRead(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
