package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    // Get Account List for User
    public List<Account> getAllAccounts(User user) {
        return accountRepository.findAllByUser(user);
    }
    // Get Account by id
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    // Check if Account exists
    public boolean accountExists(User user, String name) {
        return accountRepository.existsByUserAndName(user, name);
    }
    // Create a new account and save it
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }
    // Update Account
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    // Delete an account
    public void deleteAccount(Long accountId) {
        accountRepository.deleteById(accountId);
    }

}

