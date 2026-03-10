package com.erp.moveis.core.scheduler;

import com.erp.moveis.sales.entity.SalesTarget;
import com.erp.moveis.sales.repository.SalesTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SalesTargetScheduler {

    private final SalesTargetRepository repository;

    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void closeExpiredTargets() {
        List<SalesTarget> expired = repository.findExpiredTargets(LocalDate.now());

        if (expired.isEmpty()) {
            return;
        }

        for (SalesTarget target : expired) {
            BigDecimal achieved = target.getAchievedAmount() != null ? target.getAchievedAmount() : BigDecimal.ZERO;
            BigDecimal pct = target.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                    ? achieved.multiply(BigDecimal.valueOf(100)).divide(target.getTargetAmount(), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            target.setAchievementPercentage(pct);

            if (pct.compareTo(BigDecimal.valueOf(100)) >= 0) {
                target.setStatus(SalesTarget.TargetStatus.ACHIEVED);
            } else {
                target.setStatus(SalesTarget.TargetStatus.EXPIRED);
            }

            repository.save(target);

            log.info("[SALES_TARGET] Target {} (seller {}) closed — achieved: {}%",
                    target.getId(), target.getSellerId(), pct);
        }

        log.info("Sales target evaluation completed: {} targets processed", expired.size());
    }
}
