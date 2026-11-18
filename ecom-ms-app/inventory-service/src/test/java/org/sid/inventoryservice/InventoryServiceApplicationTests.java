package org.sid.inventoryservice;

import org.junit.jupiter.api.Test;
import org.sid.inventoryservice.entities.Product;
import org.sid.inventoryservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InventoryServiceApplicationTests {
    @Autowired
    private ProductRepository repository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void initializesThreeProducts() {
        assertThat(repository.findAll()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Computer", "Printer", "Smartphone");
        assertThat(repository.findAll()).extracting(Product::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void exposesProductsAndIdsThroughDataRest() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.products", hasSize(3)));
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Computer"))
                .andExpect(jsonPath("$.price").value(9000.0))
                .andExpect(jsonPath("$.quantity").value(15));
    }
}
