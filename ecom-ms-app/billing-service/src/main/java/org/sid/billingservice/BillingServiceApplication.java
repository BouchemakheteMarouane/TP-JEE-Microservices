package org.sid.billingservice;

import org.sid.billingservice.entities.Bill;
import org.sid.billingservice.entities.ProductItem;
import org.sid.billingservice.repositories.BillRepository;
import org.sid.billingservice.repositories.ProductItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
@EnableFeignClients
public class BillingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(BillRepository billRepository, ProductItemRepository productItemRepository) {
        return args -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (long customerId = 1; customerId <= 3; customerId++) {
                Bill bill = billRepository.save(Bill.builder()
                        .billingDate(new Date())
                        .customerId(customerId)
                        .build());
                for (long productId = 1; productId <= 3; productId++) {
                    productItemRepository.save(ProductItem.builder()
                            .productId(productId)
                            .quantity(random.nextInt(1, 6))
                            .price(random.nextDouble(100, 10000))
                            .bill(bill)
                            .build());
                }
            }
        };
    }
}
