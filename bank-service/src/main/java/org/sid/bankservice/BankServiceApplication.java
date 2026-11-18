package org.sid.bankservice;

import org.sid.bankservice.entities.BankAccount;
import org.sid.bankservice.enums.AccountType;
import org.sid.bankservice.repositories.BankAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
public class BankServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(BankAccountRepository bankAccountRepository) {
        return args -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < 10; i++) {
                bankAccountRepository.save(BankAccount.builder()
                        .id(UUID.randomUUID().toString())
                        .createdAt(new Date())
                        .balance(random.nextDouble(1000, 100000))
                        .currency(random.nextBoolean() ? "MAD" : "EUR")
                        .type(i % 2 == 0 ? AccountType.CURRENT_ACCOUNT : AccountType.SAVING_ACCOUNT)
                        .build());
            }
        };
    }
}
