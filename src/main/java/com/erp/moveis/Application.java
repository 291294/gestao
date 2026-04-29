package com.erp.moveis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableCaching
@EnableScheduling
// order menor que MAX_VALUE permite que TenantFilterAspect (MAX_VALUE) rode DENTRO da transação
@EnableTransactionManagement(order = Integer.MAX_VALUE - 100)
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}