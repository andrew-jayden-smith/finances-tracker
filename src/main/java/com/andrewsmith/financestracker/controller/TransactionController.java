package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.*;
import com.andrewsmith.financestracker.repository.CategoryRepository;
import com.andrewsmith.financestracker.repository.MerchantRepository;
import com.andrewsmith.financestracker.service.CategoryService;
import org.springframework.transaction.annotation.TransactionManagementConfigurationSelector;
import org.springframework.ui.Model;
import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.TransactionService;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final MerchantRepository merchantRepository;

    // Inject all classes
    public TransactionController(TransactionService transactionService, AccountService accountService, UserService userService, CategoryRepository categoryRepository, MerchantRepository merchantRepository) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.merchantRepository = merchantRepository;
    }

    // View transactions for a specific amount
    @GetMapping("/account/{accountId}")
    public String viewTransactions(@PathVariable Long accountId, @RequestParam String username, @RequestParam(required = false) String filter, @RequestParam(required = false) String filterValue, Model model) {

        // Verify user
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/user/login";
        }

        // Get account and verify it belongs to user
        Account account = accountService.getAccountById(accountId).orElse(null);
        if (account == null || !account.getUser().equals(user)) {
            model.addAttribute("error", "Account not found");
            return "redirect:/account/dashboard?username=" + username;
        }

        // Get transaction based on filter or none
        List<Transaction> transactions;
        if ("type".equals(filter) && filterValue != null) {
            // filter values by TransactionType Enum
            try {
                TransactionType type = TransactionType.valueOf(filterValue);
                transactions = transactionService.getTransactionByAccountAndType(account, type);
            } catch (IllegalArgumentException e) {
                // If invalid filter, list all transactions
                transactions = transactionService.getAllTransactionsByAccount(account);
            }
        } else if ("date".equals(filter) && filterValue != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start;

            switch (filterValue.toLowerCase()) {
                case "day":
                    start = now.minusDays(1);
                    break;
                case "week":
                    start = now.minusWeeks(1);
                    break;
                case "month":
                    start = now.minusMonths(1);
                    break;
                case "year":
                    start = now.minusYears(1);
                    break;
                default:
                    start = now.minusMonths(1); // default to month
            }
            transactions = transactionService.getTransactionByAccountAndDate(account, start, now);

        } else {
            // No filter, show all
            transactions = transactionService.getAllTransactionsByAccount(account);
        }

        List<Transaction> allTransactions = transactionService.getAllTransactionsByAccount(account);
        BigDecimal currentBalance = account.getOpeningBalance();
        // Loop through all transactions to get the current balance of the account
        for (Transaction transaction : allTransactions) {
            currentBalance = currentBalance.add(transaction.getAmount());
        }

        // Transaction template from model
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("activeFilter", filter);
        model.addAttribute("filterValue", filterValue);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("merchants", merchantRepository.findAll());

        return "account-transaction";
    }

    @PostMapping("/create")
    public String createTransaction(@RequestParam Long accountId, @RequestParam String username, @RequestParam BigDecimal amount, @RequestParam String description, @RequestParam String categoryName, @RequestParam String merchantName, Model model) {
        // If user is not there or null
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/user/login";
        }
        // If account is not there or null
        Account account = accountService.getAccountById(accountId).orElse(null);
        if (accountId == null) {
            model.addAttribute("error", "Account not found");
            return "redirect:/account/dashboard?username=" + username;
        }

        // Find/Create Merchant and Category
        Merchant merchant = merchantRepository.findByName(merchantName);
        if (merchant == null) {
            merchant = new Merchant(merchantName);
            merchantRepository.save(merchant);
        }
        Category category = categoryRepository.findCategoriesByName(categoryName);
        if (category == null) {
            category = new Category(categoryName);
            categoryRepository.save(category);
        }

        // Determine if Transaction Type is Income or Expense
        TransactionType type;
        if (amount.signum() >= 0) {
            type = TransactionType.INCOME;
        } else {
            type = TransactionType.EXPENSE;
        }

        // Build and Save new Transaction
        Transaction transaction = new Transaction(amount, type, description, category, merchant);

        transactionService.createTransaction(transaction);

        return "redirect:/transaction/account/" + accountId + "?username=" + username;
    }

    // Delete transaction
    @PostMapping("/delete/{transactionId}")
    public String deleteTransaction(@PathVariable Long transactionId, @RequestParam Long accountId, @RequestParam String username) {
        Transaction transaction = transactionService.getTransactionById(transactionId).orElse(null);
        if (transaction != null) {
            transactionService.deleteTransaction(transaction);
        }
        return "redirect:/transaction/account/" + accountId + "?username=" + username;
    }
}
