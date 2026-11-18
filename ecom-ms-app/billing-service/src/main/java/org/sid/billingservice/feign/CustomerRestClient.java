package org.sid.billingservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.sid.billingservice.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerRestClient {
    @GetMapping("/customers/{id}")
    @CircuitBreaker(name = "customerService", fallbackMethod = "getDefaultCustomer")
    Customer findCustomerById(@PathVariable("id") Long id);

    default Customer getDefaultCustomer(Long id, Exception e) {
        return new Customer(id, "Default Customer", "unavailable@example.com");
    }
}
