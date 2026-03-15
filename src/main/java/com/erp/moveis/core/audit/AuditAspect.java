package com.erp.moveis.core.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            String username = "system";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                username = auth.getName();
            }

            String entityType = auditable.entity().isEmpty()
                    ? joinPoint.getTarget().getClass().getSimpleName().replace("ServiceImpl", "").replace("Service", "")
                    : auditable.entity();

            Long entityId = extractEntityId(joinPoint.getArgs(), result);

            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action(auditable.action())
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(auditable.action() + " on " + entityType + (entityId != null ? " #" + entityId : ""))
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to create audit log: {}", e.getMessage());
        }

        return result;
    }

    private Long extractEntityId(Object[] args, Object result) {
        if (args.length > 0 && args[0] instanceof Long id) {
            return id;
        }
        if (result != null) {
            try {
                var method = result.getClass().getMethod("getId");
                Object id = method.invoke(result);
                if (id instanceof Long longId) return longId;
            } catch (Exception ignored) {}
        }
        return null;
    }
}
