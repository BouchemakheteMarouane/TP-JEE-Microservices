package org.sid.bankservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.sid.bankservice.dto.BankAccountRequestDTO;
import org.sid.bankservice.dto.BankAccountResponseDTO;
import org.sid.bankservice.entities.BankAccount;
import org.sid.bankservice.enums.AccountType;
import org.sid.bankservice.repositories.BankAccountRepository;
import org.sid.bankservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BankServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BankAccountRepository repository;
    @Autowired
    private AccountService service;

    @Test
    void initializesTenAccounts() {
        List<BankAccount> accounts = repository.findAll();
        assertThat(accounts).hasSize(10);
        assertThat(repository.findByType(AccountType.CURRENT_ACCOUNT)).hasSize(5);
        assertThat(repository.findByType(AccountType.SAVING_ACCOUNT)).hasSize(5);
        accounts.forEach(account -> {
            assertThat(UUID.fromString(account.getId()).toString()).isEqualTo(account.getId());
            assertThat(account.getCreatedAt()).isNotNull();
            assertThat(account.getCurrency()).isIn("MAD", "EUR");
            assertThat(account.getBalance()).isBetween(1000.0, 100000.0);
        });
    }

    @Test
    void listsAndReadsAccountsThroughCustomApi() throws Exception {
        BankAccount account = repository.findAll().get(0);
        mockMvc.perform(get("/api/bankAccounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
        mockMvc.perform(get("/api/bankAccounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.currency").value(account.getCurrency()));
    }

    @Test
    void createsAccountThroughCustomApi() throws Exception {
        String response = mockMvc.perform(post("/api/bankAccounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"balance\":1500.0,\"currency\":\"MAD\",\"type\":\"CURRENT_ACCOUNT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.balance").value(1500.0))
                .andExpect(jsonPath("$.currency").value("MAD"))
                .andExpect(jsonPath("$.type").value("CURRENT_ACCOUNT"))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asText();
        assertThat(UUID.fromString(id)).isNotNull();
        assertThat(repository.existsById(id)).isTrue();
        assertThat(repository.count()).isEqualTo(11);
    }

    @Test
    void partiallyUpdatesWithoutChangingNullFieldsOrIdentity() throws Exception {
        BankAccount account = repository.findAll().get(0);
        String currency = account.getCurrency();
        AccountType type = account.getType();
        long createdAt = account.getCreatedAt().getTime();
        mockMvc.perform(put("/api/bankAccounts/{id}", account.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"balance\":0.0,\"currency\":null,\"type\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.currency").value(currency))
                .andExpect(jsonPath("$.type").value(type.name()));
        BankAccount updated = repository.findById(account.getId()).orElseThrow();
        assertThat(updated.getCreatedAt().getTime()).isEqualTo(createdAt);
        assertThat(updated.getBalance()).isZero();
    }

    @Test
    void updatesAllProvidedFields() {
        BankAccount account = repository.findAll().get(0);
        BankAccountResponseDTO updated = service.updateAccount(account.getId(),
                BankAccountRequestDTO.builder()
                        .balance(2500.0)
                        .currency("EUR")
                        .type(AccountType.SAVING_ACCOUNT)
                        .build());
        assertThat(updated.getId()).isEqualTo(account.getId());
        assertThat(updated.getBalance()).isEqualTo(2500.0);
        assertThat(updated.getCurrency()).isEqualTo("EUR");
        assertThat(updated.getType()).isEqualTo(AccountType.SAVING_ACCOUNT);
    }

    @Test
    void deletesAccountThroughCustomApi() throws Exception {
        String id = repository.findAll().get(0).getId();
        mockMvc.perform(delete("/api/bankAccounts/{id}", id))
                .andExpect(status().isOk());
        assertThat(repository.existsById(id)).isFalse();
        assertThat(repository.count()).isEqualTo(9);
    }

    @Test
    void rejectsMissingAccounts() throws Exception {
        String id = UUID.randomUUID().toString();
        assertThatThrownBy(() -> service.updateAccount(id, new BankAccountRequestDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Compte bancaire introuvable");
        assertThatThrownBy(() -> service.deleteAccount(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Compte bancaire introuvable");
        assertThatThrownBy(() -> mockMvc.perform(get("/api/bankAccounts/{id}", id)))
                .hasRootCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("Compte bancaire introuvable : " + id);
    }

    @Test
    void exposesDataRestSearchAndProjection() throws Exception {
        mockMvc.perform(get("/bankAccounts/search/byType").param("t", "CURRENT_ACCOUNT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bankAccounts", hasSize(5)))
                .andExpect(jsonPath("$._embedded.bankAccounts[0].type").value("CURRENT_ACCOUNT"));
        mockMvc.perform(get("/bankAccounts").param("projection", "p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bankAccounts", hasSize(10)))
                .andExpect(jsonPath("$._embedded.bankAccounts[0].id").isNotEmpty())
                .andExpect(jsonPath("$._embedded.bankAccounts[0].balance").isNumber())
                .andExpect(jsonPath("$._embedded.bankAccounts[0].currency").doesNotExist())
                .andExpect(jsonPath("$._embedded.bankAccounts[0].createdAt").doesNotExist());
    }

    @Test
    void exposesOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/bankAccounts']").exists())
                .andExpect(jsonPath("$.paths['/api/bankAccounts/{id}']").exists());
    }
}
