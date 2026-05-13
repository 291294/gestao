package com.erp.moveis.core.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Métricas de negócio customizadas para o ERP.
 *
 * Todos os contadores são prefixados com "erp." para fácil filtro no Grafana.
 * Uso: injete ErpMetrics onde precisar incrementar.
 *
 * Exemplo de query PromQL no Grafana:
 *   rate(erp_orders_created_total[5m])   → novos pedidos/s
 *   rate(erp_logins_total[1h])           → logins/hora
 */
@Configuration
public class MetricsConfig {

    /**
     * Registra todos os contadores de negócio no MeterRegistry do Spring.
     * Retorna um bean ErpMetrics disponível para injeção em qualquer serviço.
     */
    @Bean
    public ErpMetrics erpMetrics(MeterRegistry registry) {
        return new ErpMetrics(registry);
    }

    /**
     * Container de todas as métricas de negócio do ERP.
     * Bean único (singleton) — thread-safe por design do Micrometer.
     */
    public static class ErpMetrics {

        // ---------------------------------------------------------------
        // Autenticação
        // ---------------------------------------------------------------
        public final Counter loginsSuccess;
        public final Counter loginsFailure;

        // ---------------------------------------------------------------
        // Pedidos
        // ---------------------------------------------------------------
        public final Counter ordersCreated;
        public final Counter ordersCancelled;

        // ---------------------------------------------------------------
        // Orçamentos
        // ---------------------------------------------------------------
        public final Counter quotesCreated;
        public final Counter quotesApproved;
        public final Counter quotesRejected;
        public final Counter quotesConvertedToOrder;

        // ---------------------------------------------------------------
        // Clientes
        // ---------------------------------------------------------------
        public final Counter clientsCreated;

        // ---------------------------------------------------------------
        // Estoque
        // ---------------------------------------------------------------
        public final Counter inventoryAdjustments;

        // ---------------------------------------------------------------
        // Faturas / Pagamentos
        // ---------------------------------------------------------------
        public final Counter invoicesIssued;
        public final Counter paymentsConfirmed;

        // ---------------------------------------------------------------
        // Segurança / Sessão
        // ---------------------------------------------------------------
        /** JWT inválido, expirado ou malformado detectado no filtro. */
        public final Counter jwtInvalid;

        public ErpMetrics(MeterRegistry registry) {

            jwtInvalid = Counter.builder("erp.security")
                    .tag("event", "jwt_invalid")
                    .description("Total de tokens JWT inválidos, expirados ou malformados")
                    .register(registry);

            loginsSuccess = Counter.builder("erp.logins")
                    .tag("result", "success")
                    .description("Total de logins bem-sucedidos")
                    .register(registry);

            loginsFailure = Counter.builder("erp.logins")
                    .tag("result", "failure")
                    .description("Total de tentativas de login com falha")
                    .register(registry);

            ordersCreated = Counter.builder("erp.orders")
                    .tag("action", "created")
                    .description("Total de pedidos criados")
                    .register(registry);

            ordersCancelled = Counter.builder("erp.orders")
                    .tag("action", "cancelled")
                    .description("Total de pedidos cancelados")
                    .register(registry);

            quotesCreated = Counter.builder("erp.quotes")
                    .tag("action", "created")
                    .description("Total de orçamentos criados")
                    .register(registry);

            quotesApproved = Counter.builder("erp.quotes")
                    .tag("action", "approved")
                    .description("Total de orçamentos aprovados")
                    .register(registry);

            quotesRejected = Counter.builder("erp.quotes")
                    .tag("action", "rejected")
                    .description("Total de orçamentos rejeitados")
                    .register(registry);

            quotesConvertedToOrder = Counter.builder("erp.quotes")
                    .tag("action", "converted_to_order")
                    .description("Total de orçamentos convertidos em pedido")
                    .register(registry);

            clientsCreated = Counter.builder("erp.clients")
                    .tag("action", "created")
                    .description("Total de clientes cadastrados")
                    .register(registry);

            inventoryAdjustments = Counter.builder("erp.inventory")
                    .tag("action", "adjustment")
                    .description("Total de ajustes de estoque")
                    .register(registry);

            invoicesIssued = Counter.builder("erp.invoices")
                    .tag("action", "issued")
                    .description("Total de faturas emitidas")
                    .register(registry);

            paymentsConfirmed = Counter.builder("erp.payments")
                    .tag("action", "confirmed")
                    .description("Total de pagamentos confirmados")
                    .register(registry);
        }
    }
}
