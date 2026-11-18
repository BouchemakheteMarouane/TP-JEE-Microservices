package org.sid.billingservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.sid.billingservice.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryRestClient {
    @GetMapping("/products/{id}")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getDefaultProduct")
    Product findProductById(@PathVariable("id") Long id);

    default Product getDefaultProduct(Long id, Exception e) {
        return new Product(id, "Default Product", 0.0, 0);
    }
}
