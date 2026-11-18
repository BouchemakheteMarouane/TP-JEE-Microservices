package org.sid.bankservice.mappers;

import org.sid.bankservice.dto.BankAccountRequestDTO;
import org.sid.bankservice.dto.BankAccountResponseDTO;
import org.sid.bankservice.entities.BankAccount;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public BankAccount fromBankAccountRequestDTO(BankAccountRequestDTO requestDTO) {
        BankAccount bankAccount = new BankAccount();
        BeanUtils.copyProperties(requestDTO, bankAccount);
        return bankAccount;
    }

    public BankAccountResponseDTO fromBankAccount(BankAccount bankAccount) {
        BankAccountResponseDTO responseDTO = new BankAccountResponseDTO();
        BeanUtils.copyProperties(bankAccount, responseDTO);
        return responseDTO;
    }
}
