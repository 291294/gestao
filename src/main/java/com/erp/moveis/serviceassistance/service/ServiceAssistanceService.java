package com.erp.moveis.serviceassistance.service;

import com.erp.moveis.core.tenant.TenantContext;
import com.erp.moveis.notification.service.NotificationService;
import com.erp.moveis.notification.type.NotificationType;
import com.erp.moveis.serviceassistance.dto.ServiceAssistanceRequest;
import com.erp.moveis.serviceassistance.dto.ServiceAssistanceResponse;
import com.erp.moveis.serviceassistance.entity.ServiceAssistance;
import com.erp.moveis.serviceassistance.entity.ServiceAssistanceStatus;
import com.erp.moveis.serviceassistance.repository.ServiceAssistanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceAssistanceService {

    private final ServiceAssistanceRepository repository;
    private final NotificationService notificationService;

    public List<ServiceAssistanceResponse> list() {
        Long companyId = TenantContext.requireTenantId();
        return repository.findByCompanyIdOrderByScheduledDateAsc(companyId)
                .stream().map(this::toResponse).toList();
    }

    public Optional<ServiceAssistanceResponse> findById(Long id) {
        return repository.findByIdAndCompanyId(id, TenantContext.requireTenantId())
                .map(this::toResponse);
    }

    @Transactional
    public ServiceAssistanceResponse create(ServiceAssistanceRequest request) {
        ServiceAssistance entity = new ServiceAssistance();
        applyRequest(entity, request);
        entity.setCompanyId(TenantContext.requireTenantId());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<ServiceAssistanceResponse> update(Long id, ServiceAssistanceRequest request) {
        return repository.findByIdAndCompanyId(id, TenantContext.requireTenantId())
                .map(existing -> {
                    applyRequest(existing, request);
                    return toResponse(repository.save(existing));
                });
    }

    @Transactional
    public boolean delete(Long id) {
        return repository.findByIdAndCompanyId(id, TenantContext.requireTenantId())
                .map(entity -> {
                    repository.delete(entity);
                    return true;
                }).orElse(false);
    }

    private void applyRequest(ServiceAssistance entity, ServiceAssistanceRequest req) {
        entity.setClientName(req.getClientName());
        entity.setClientAddress(req.getClientAddress());
        entity.setScheduledDate(req.getScheduledDate());
        entity.setServiceDescription(req.getServiceDescription());
        entity.setMaterialRequestedAt(req.getMaterialRequestedAt());
        entity.setPhotoUrl(req.getPhotoUrl());
        entity.setNotes(req.getNotes());
        if (req.getStatus() != null) {
            try {
                entity.setStatus(ServiceAssistanceStatus.valueOf(req.getStatus()));
            } catch (IllegalArgumentException ignored) {
                // mantém status atual
            }
        }
    }

    private ServiceAssistanceResponse toResponse(ServiceAssistance e) {
        ServiceAssistanceResponse r = new ServiceAssistanceResponse();
        r.setId(e.getId());
        r.setCompanyId(e.getCompanyId());
        r.setClientName(e.getClientName());
        r.setClientAddress(e.getClientAddress());
        r.setScheduledDate(e.getScheduledDate());
        r.setServiceDescription(e.getServiceDescription());
        r.setMaterialRequestedAt(e.getMaterialRequestedAt());
        r.setPhotoUrl(e.getPhotoUrl());
        r.setNotes(e.getNotes());
        r.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        r.setNotificationSent(e.getNotificationSent());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    /**
     * Executa todo dia às 07:00 e notifica assistências agendadas para hoje.
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void notifyTodayAssistances() {
        LocalDate today = LocalDate.now();
        List<ServiceAssistance> pending = repository.findAllPendingNotificationsForDate(today);

        for (ServiceAssistance sa : pending) {
            try {
                notificationService.create(
                        sa.getCompanyId(),
                        NotificationType.SERVICE_ASSISTANCE_TODAY,
                        "Assistência de Serviço hoje",
                        "Assistência agendada para hoje com o cliente " + sa.getClientName()
                                + ". Serviço: " + sa.getServiceDescription()
                                + ". Endereço: " + sa.getClientAddress()
                );
                sa.setNotificationSent(true);
                repository.save(sa);
                log.info("[SERVICE_ASSISTANCE] Notificação enviada para assistência ID={} empresa={}",
                        sa.getId(), sa.getCompanyId());
            } catch (Exception ex) {
                log.error("[SERVICE_ASSISTANCE] Erro ao notificar assistência ID={}", sa.getId(), ex);
            }
        }
    }
}
