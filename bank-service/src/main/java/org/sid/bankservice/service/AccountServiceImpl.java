package org.sid.bankservice.service;

import org.sid.bankservice.dto.BankAccountRequestDTO;
import org.sid.bankservice.dto.BankAccountResponseDTO;
import org.sid.bankservice.entities.BankAccount;
import org.sid.bankservice.mappers.AccountMapper;
import org.sid.bankservice.repositories.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {
    private final BankAccountRepository bankAccountRepository;
    private final AccountMapper accountMapper;

    public AccountServiceImpl(BankAccountRepository bankAccountRepository, AccountMapper accountMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public BankAccountResponseDTO addAccount(BankAccountRequestDTO bankAccountDTO) {
        BankAccount bankAccount = accountMapper.fromBankAccountRequestDTO(bankAccountDTO);
        bankAccount.setId(UUID.randomUUID().toString());
        bankAccount.setCreatedAt(new Date());
        return accountMapper.fromBankAccount(bankAccountRepository.save(bankAccount));
    }

    @Override
    public BankAccountResponseDTO updateAccount(String id, BankAccountRequestDTO bankAccountDTO) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire introuvable : " + id));
        if (bankAccountDTO.getBalance() != null) {
            bankAccount.setBalance(bankAccountDTO.getBalance());
        }
        if (bankAccountDTO.getCurrency() != null) {
            bankAccount.setCurrency(bankAccountDTO.getCurrency());
        }
        if (bankAccountDTO.getType() != null) {
            bankAccount.setType(bankAccountDTO.getType());
        }
        return accountMapper.fromBankAccount(bankAccountRepository.save(bankAccount));
    }

    @Override
    public void deleteAccount(String id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire introuvable : " + id));
        bankAccountRepository.delete(bankAccount);
    }
}
