package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.AccountRepository;
import com.andrewsmith.financestracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    // Inject the UserRepository
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public UserService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    // Check if user exists by username
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // Check if a user exists by email
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Create new user and save it to the UserRepository
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Find all accounts for a user
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findAllByUser(user);
    }

    // Check if a specific account exists for a user
    public boolean accountExists(User user, String accountName) {
        return accountRepository.existsByUserAndName(user, accountName);
    }
}
