package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.AccountRepository;
import com.andrewsmith.financestracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }

    // Register a new user with encrypted password
    public User registerUser(String username, String password, String email) {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (email != null && !email.isEmpty() && userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user with encrypted password
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // ✅ ENCRYPT PASSWORD
        user.setEmail(email);

        return userRepository.save(user);
    }

    // Authenticate user (for manual login - Spring Security handles this automatically)
    public boolean authenticateUser(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        // Check if password matches using BCrypt
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    // Update user password (with encryption)
    public void updatePassword(User user, String newPassword) {
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encryptedPassword);
        userRepository.save(user);
    }

    // Check if username is available
    public boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

    // Check if email is available
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    // Find all accounts for a user
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findAllByUser(user);
    }
}