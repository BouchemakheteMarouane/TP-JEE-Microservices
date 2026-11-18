package org.sid.customerservice;

import org.junit.jupiter.api.Test;
import org.sid.customerservice.entities.Customer;
import org.sid.customerservice.repositories.CustomerRepository;
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
class CustomerServiceApplicationTests {
    @Autowired
    private CustomerRepository repository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void initializesThreeCustomers() {
        assertThat(repository.findAll()).extracting(Customer::getName)
                .containsExactlyInAnyOrder("Mohamed", "Imane", "Yassine");
        assertThat(repository.findAll()).extracting(Customer::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void exposesCustomersAndIdsThroughDataRest() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers", hasSize(3)));
        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mohamed"))
                .andExpect(jsonPath("$.email").value("mohamed@example.com"));
    }
}
