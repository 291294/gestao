package com.erp.moveis.core.audit;

import com.erp.moveis.core.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    // ObjectMapper próprio para serialização de audit — sem Hibernate proxies, sem ciclos
    private static final ObjectMapper AUDIT_MAPPER = buildAuditMapper();

    // Campos sensíveis nunca serializados no audit log
    private static final List<String> SENSITIVE_FIELDS = List.of(
            "password", "passwordHash", "secret", "token", "pin", "cvv"
    );

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // Capturar estado dos args ANTES da execução
        String requestData = serializeArgs(joinPoint.getArgs());

        Object result = joinPoint.proceed();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = resolveUsername(auth);
            Long userId = resolveUserId(auth);
            String ip = resolveIp();

            String entityType = auditable.entity().isEmpty()
                    ? joinPoint.getTarget().getClass().getSimpleName()
                            .replace("ServiceImpl", "").replace("Service", "")
                    : auditable.entity();

            Long entityId = extractEntityId(joinPoint.getArgs(), result);

            String responseData = (result != null) ? safeSerialize(result) : null;

            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .userId(userId)
                    .action(auditable.action())
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(buildDetails(auditable.action(), entityType, entityId))
                    .requestData(requestData)
                    .responseData(responseData)
                    .ipAddress(ip)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("[AUDIT] Falha ao registrar audit log: {}", e.getMessage());
        }

        return result;
    }

    // -------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------

    private String resolveUsername(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) return auth.getName();
        return "system";
    }

    private Long resolveUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) return user.getId();
        return null;
    }

    private String resolveIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                // X-Forwarded-For pode conter lista; usar o primeiro (IP real do cliente)
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) return null;
        // Filtrar apenas objetos complexos (DTOs) — ignorar Long/String/primitivos simples
        List<Object> dtos = Arrays.stream(args)
                .filter(a -> a != null
                        && !(a instanceof Long)
                        && !(a instanceof Integer)
                        && !(a instanceof String)
                        && !a.getClass().isPrimitive())
                .collect(Collectors.toList());
        if (dtos.isEmpty()) return null;
        return safeSerialize(dtos.size() == 1 ? dtos.get(0) : dtos);
    }

    private String safeSerialize(Object obj) {
        try {
            return AUDIT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"_error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String buildDetails(String action, String entityType, Long entityId) {
        return action + " on " + entityType + (entityId != null ? " #" + entityId : "");
    }

    private Long extractEntityId(Object[] args, Object result) {
        // Primeiro arg Long = ID passado como parâmetro (ex: updateOrder(Long id, ...))
        if (args != null && args.length > 0 && args[0] instanceof Long id) {
            return id;
        }
        // Tentar extrair do resultado via getId() reflection
        if (result != null) {
            try {
                var method = result.getClass().getMethod("getId");
                Object id = method.invoke(result);
                if (id instanceof Long longId) return longId;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static ObjectMapper buildAuditMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // Filtro de campos sensíveis registrado dinamicamente
        SimpleFilterProvider filters = new SimpleFilterProvider().setFailOnUnknownId(false);
        filters.addFilter("sensitiveFilter",
                SimpleBeanPropertyFilter.serializeAllExcept(
                        "password", "passwordHash", "secret", "token", "pin", "cvv"
                ));
        mapper.setFilterProvider(filters);
        return mapper;
    }
}
