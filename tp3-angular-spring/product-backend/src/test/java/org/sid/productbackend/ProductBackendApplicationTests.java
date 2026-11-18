package org.sid.productbackend;

import org.junit.jupiter.api.Test;
import org.sid.productbackend.entities.Product;
import org.sid.productbackend.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProductBackendApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository repository;

    @Test
    void initializesTheThreeExpectedProducts() {
        assertThat(repository.findAll()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Computer", "Printer", "Smartphone");
        Product computer = repository.findById(1L).orElseThrow();
        assertThat(computer.getPrice()).isEqualTo(1300);
        assertThat(computer.getQuantity()).isEqualTo(10);
        assertThat(computer.isSelected()).isTrue();
        assertThat(repository.findById(2L).orElseThrow().isSelected()).isFalse();
    }

    @Test
    void listsProductsAsJson() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void readsAProductById() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Computer"))
                .andExpect(jsonPath("$.price").value(1300.0))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.selected").value(true));
    }

    @Test
    void deletesAProduct() throws Exception {
        mockMvc.perform(delete("/products/1")).andExpect(status().isOk());
        assertThat(repository.count()).isEqualTo(2);
        mockMvc.perform(get("/products/1")).andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundForUnknownProducts() throws Exception {
        mockMvc.perform(get("/products/9999")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/products/9999")).andExpect(status().isNotFound());
    }

    @Test
    void allowsAngularCorsRequestsIncludingDeletion() throws Exception {
        mockMvc.perform(options("/products/1")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("DELETE")));
    }

    @Test
    void exposesTheOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/products'].get").exists())
                .andExpect(jsonPath("$.paths['/products/{id}'].delete").exists());
    }
}
