package org.sid.bankservice.mappers;

import org.junit.jupiter.api.Test;
import org.sid.bankservice.dto.BankAccountRequestDTO;
import org.sid.bankservice.dto.BankAccountResponseDTO;
import org.sid.bankservice.entities.BankAccount;
import org.sid.bankservice.enums.AccountType;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTests {
    private final AccountMapper accountMapper = new AccountMapper();

    @Test
    void copiesRequestToEntityWithoutGeneratingIdentityOrCreationDate() {
        BankAccountRequestDTO requestDTO = BankAccountRequestDTO.builder()
                .balance(1500.0)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build();

        BankAccount bankAccount = accountMapper.fromBankAccountRequestDTO(requestDTO);

        assertThat(bankAccount.getBalance()).isEqualTo(requestDTO.getBalance());
        assertThat(bankAccount.getCurrency()).isEqualTo(requestDTO.getCurrency());
        assertThat(bankAccount.getType()).isEqualTo(requestDTO.getType());
        assertThat(bankAccount.getId()).isNull();
        assertThat(bankAccount.getCreatedAt()).isNull();
    }

    @Test
    void copiesEveryEntityPropertyToResponse() {
        BankAccount bankAccount = BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .balance(2500.0)
                .currency("EUR")
                .type(AccountType.SAVING_ACCOUNT)
                .build();

        BankAccountResponseDTO responseDTO = accountMapper.fromBankAccount(bankAccount);

        assertThat(responseDTO).usingRecursiveComparison().isEqualTo(bankAccount);
    }
}
