package org.sid.productbackend;

import org.sid.productbackend.entities.Product;
import org.sid.productbackend.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class ProductBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner start(ProductRepository productRepository) {
        return args -> productRepository.saveAll(List.of(
                Product.builder().name("Computer").price(1300).quantity(10).selected(true).build(),
                Product.builder().name("Printer").price(450).quantity(20).selected(false).build(),
                Product.builder().name("Smartphone").price(900).quantity(15).selected(true).build()
        ));
    }
}
