package org.sid.bankservice.service;

import org.sid.bankservice.dto.BankAccountRequestDTO;
import org.sid.bankservice.dto.BankAccountResponseDTO;

public interface AccountService {
    BankAccountResponseDTO addAccount(BankAccountRequestDTO bankAccountDTO);

    BankAccountResponseDTO updateAccount(String id, BankAccountRequestDTO bankAccountDTO);

    void deleteAccount(String id);
}
