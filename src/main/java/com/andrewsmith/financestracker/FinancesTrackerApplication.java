package com.andrewsmith.financestracker;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.UserRepository;
import com.andrewsmith.financestracker.service.BillService;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class FinancesTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancesTrackerApplication.class, args);
    }

    @Bean
    CommandLineRunner testUserRepo(UserRepository userRepository, UserService userService, BillService billService) {
        return args -> {
            System.out.println("=== Testing UserService & SQL Connection ===");

            // Fetch user by username
            Optional<User> userOpt = userService.getUserByUsername("smithdrew");
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("Found User: " + user.getUsername());
                System.out.println("User Email: " + user.getEmail());

                // Fetch accounts for that user
                List<Account> accounts = userService.getUserAccounts(user);
                System.out.println("Accounts count: " + accounts.size());

                // Print each account and balance
                for (Account acct : accounts) {
                    BigDecimal bal = acct.getOpeningBalance() != null ? acct.getOpeningBalance() : BigDecimal.ZERO;
                    System.out.printf(" - %s | Balance: %s | Opened: %s%n",
                            acct.getName(), bal.toString(), acct.getOpeningDate());
                }

                // Optional: Calculate totals
                BigDecimal totalAssets = accounts.stream()
                        .map(Account::getOpeningBalance)
                        .filter(b -> b != null && b.signum() > 0)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalDebts = accounts.stream()
                        .map(Account::getOpeningBalance)
                        .filter(b -> b != null && b.signum() < 0)
                        .map(BigDecimal::abs)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal netWorth = totalAssets.subtract(totalDebts);

                System.out.println("Total Assets: " + totalAssets);
                System.out.println("Total Debts: " + totalDebts);
                System.out.println("Net Worth: " + netWorth);

                System.out.println("Total Bills: " + billService.getAllBillsForUser(user).size());

            } else {
                System.out.println("User not found!");
            }
        };
    }
}
