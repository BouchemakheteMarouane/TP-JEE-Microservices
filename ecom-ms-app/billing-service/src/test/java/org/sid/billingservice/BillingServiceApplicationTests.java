package org.sid.billingservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sid.billingservice.repositories.BillRepository;
import org.sid.billingservice.repositories.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BillingServiceApplicationTests {
    private static final AtomicBoolean SERVICES_AVAILABLE = new AtomicBoolean(true);
    private static final HttpServer REMOTE_SERVICES = startRemoteServices();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private ProductItemRepository productItemRepository;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @DynamicPropertySource
    static void configureRemoteServices(DynamicPropertyRegistry registry) {
        String url = "http://127.0.0.1:" + REMOTE_SERVICES.getAddress().getPort();
        registry.add("spring.cloud.openfeign.client.config.CUSTOMER-SERVICE.url", () -> url);
        registry.add("spring.cloud.openfeign.client.config.INVENTORY-SERVICE.url", () -> url);
    }

    private static HttpServer startRemoteServices() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/customers/", exchange -> {
                long id = Long.parseLong(exchange.getRequestURI().getPath().substring("/customers/".length()));
                sendResponse(exchange, "{\"id\":" + id
                        + ",\"name\":\"Mohamed\",\"email\":\"mohamed@example.com\"}");
            });
            server.createContext("/products/", exchange -> {
                int id = Integer.parseInt(exchange.getRequestURI().getPath().substring("/products/".length()));
                String[] names = {"Computer", "Printer", "Smartphone"};
                sendResponse(exchange, "{\"id\":" + id + ",\"name\":\"" + names[id - 1]
                        + "\",\"price\":9000.0,\"quantity\":15}");
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de demarrer les services HTTP de test", e);
        }
    }

    private static void sendResponse(HttpExchange exchange, String successBody) throws IOException {
        boolean available = SERVICES_AVAILABLE.get();
        byte[] body = (available ? successBody : "{\"error\":\"Service unavailable\"}")
                .getBytes(StandardCharsets.UTF_8);
        try {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(available ? 200 : 503, body.length);
            exchange.getResponseBody().write(body);
        } finally {
            exchange.close();
        }
    }

    @BeforeEach
    void resetCircuitBreakers() {
        SERVICES_AVAILABLE.set(true);
        circuitBreakerRegistry.circuitBreaker("customerService").reset();
        circuitBreakerRegistry.circuitBreaker("inventoryService").reset();
    }

    @AfterAll
    static void stopRemoteServices() {
        REMOTE_SERVICES.stop(0);
    }

    @Test
    void initializesThreeBillsAndNineProductItems() {
        assertThat(billRepository.count()).isEqualTo(3);
        assertThat(productItemRepository.count()).isEqualTo(9);
        for (long id = 1; id <= 3; id++) {
            assertThat(productItemRepository.findByBillId(id)).hasSize(3);
            var bill = billRepository.findById(id).orElseThrow();
            assertThat(bill.getCustomerId()).isEqualTo(id);
            assertThat(bill.getProductItems()).extracting(item -> item.getProductId())
                    .containsExactlyInAnyOrder(1L, 2L, 3L);
        }
    }

    @Test
    void enrichesBillThroughRealFeignHttpCallsWithoutOpenInView() throws Exception {
        mockMvc.perform(get("/api/bills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customer.id").value(1))
                .andExpect(jsonPath("$.customer.name").value("Mohamed"))
                .andExpect(jsonPath("$.productItems", hasSize(3)))
                .andExpect(jsonPath("$.productItems[*].product.name",
                        containsInAnyOrder("Computer", "Printer", "Smartphone")))
                .andExpect(jsonPath("$.productItems[0].bill").doesNotExist());
        assertThat(circuitBreakerRegistry.circuitBreaker("customerService")
                .getMetrics().getNumberOfSuccessfulCalls()).isPositive();
    }

    @Test
    void appliesBothCircuitBreakerFallbacksWhenRemoteServicesFail() throws Exception {
        SERVICES_AVAILABLE.set(false);
        mockMvc.perform(get("/api/bills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.id").value(1))
                .andExpect(jsonPath("$.customer.name").value("Default Customer"))
                .andExpect(jsonPath("$.productItems", hasSize(3)))
                .andExpect(jsonPath("$.productItems[0].product.name").value("Default Product"));
        assertThat(circuitBreakerRegistry.circuitBreaker("customerService")
                .getMetrics().getNumberOfFailedCalls()).isPositive();
        assertThat(circuitBreakerRegistry.circuitBreaker("inventoryService")
                .getMetrics().getNumberOfFailedCalls()).isPositive();
    }

    @Test
    void returnsNotFoundForUnknownBill() throws Exception {
        mockMvc.perform(get("/api/bills/9999"))
                .andExpect(status().isNotFound());
    }
}
