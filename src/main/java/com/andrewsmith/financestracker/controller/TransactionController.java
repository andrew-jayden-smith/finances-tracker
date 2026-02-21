package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.*;
import com.andrewsmith.financestracker.repository.CategoryRepository;
import com.andrewsmith.financestracker.repository.MerchantRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.TransactionService;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public String viewTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String filterValue,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {

        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 1️⃣ Verify user
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) return "redirect:/user/login";

        // 2️⃣ Verify account
        Account account = accountService.getAccountById(accountId).orElse(null);
        if (account == null || !account.getUser().equals(user))
            return "redirect:/account/dashboard";

        // 1️⃣ Determine default year/month
        LocalDateTime now = LocalDateTime.now();
        int filterYearValue = (year != null) ? year : now.getYear();
        Integer filterMonthValueSafe = (month != null) ? month : now.getMonthValue(); // default to current month

        List<Transaction> transactions;

        if ("date".equals(filter) && filterValue != null) {
            if ("all".equals(filterValue)) {
                // "All" selected → show all transactions for account
                transactions = transactionService.getAllTransactionsByAccount(account);
            } else if (month != null) {
                LocalDateTime start = LocalDateTime.of(filterYearValue, month, 1, 0, 0);
                LocalDateTime end = start.plusMonths(1).minusNanos(1);
                transactions = transactionService.getTransactionByAccountAndDateDesc(account, start, end);
            } else {
                // Default: current month
                LocalDateTime start = LocalDateTime.of(filterYearValue, filterMonthValueSafe, 1, 0, 0);
                LocalDateTime end = start.plusMonths(1).minusNanos(1);
                transactions = transactionService.getTransactionByAccountAndDateDesc(account, start, end);
            }
        }
        else if ("category".equals(filter) && filterValue != null) {
            Category category = categoryRepository.findCategoriesByName(filterValue);
            if (category != null) {
                if (month != null) {
                    LocalDateTime start = LocalDateTime.of(filterYearValue, month, 1, 0, 0);
                    LocalDateTime end = start.plusMonths(1).minusNanos(1);
                    transactions = transactionService.getTransactionsByAccountAndCategoryAndDateDesc(account, category, start, end);
                } else {
                    // Default: current month
                    LocalDateTime start = LocalDateTime.of(filterYearValue, filterMonthValueSafe, 1, 0, 0);
                    LocalDateTime end = start.plusMonths(1).minusNanos(1);
                    transactions = transactionService.getTransactionsByAccountAndCategoryAndDateDesc(account, category, start, end);
                }
            } else {
                transactions = Collections.emptyList();
            }
        }
        else if ("merchant".equals(filter) && filterValue != null) {
            Merchant merchant = merchantRepository.findByName(filterValue);
            if (merchant != null) {
                if (month != null) {
                    LocalDateTime start = LocalDateTime.of(filterYearValue, month, 1, 0, 0);
                    LocalDateTime end = start.plusMonths(1).minusNanos(1);
                    transactions = transactionService.getTransactionsByAccountAndMerchantAndDateDesc(account, merchant, start, end);
                } else {
                    // Default: current month
                    LocalDateTime start = LocalDateTime.of(filterYearValue, filterMonthValueSafe, 1, 0, 0);
                    LocalDateTime end = start.plusMonths(1).minusNanos(1);
                    transactions = transactionService.getTransactionsByAccountAndMerchantAndDateDesc(account, merchant, start, end);
                }
            } else {
                transactions = Collections.emptyList();
            }
        }
        else {
            // No filter → default to current month
            LocalDateTime start = LocalDateTime.of(filterYearValue, filterMonthValueSafe, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1).minusNanos(1);
            transactions = transactionService.getTransactionByAccountAndDateDesc(account, start, end);
        }


        // 5️⃣ Calculate current balance
        BigDecimal currentBalance = account.getOpeningBalance();
        for (Transaction t : transactionService.getAllTransactionsByAccount(account)) {
            currentBalance = currentBalance.add(t.getAmount());
        }


        LocalDateTime summaryStartDate;
        LocalDateTime summaryEndDate;

        if ("date".equals(filter) && month != null) {
            // Use selected month
            summaryStartDate = LocalDateTime.of(filterYearValue, month, 1, 0, 0);
            summaryEndDate = summaryStartDate.plusMonths(1).minusNanos(1);
        } else if ("date".equals(filter) && "all".equals(filterValue)) {
            // All time
            summaryStartDate = null;
            summaryEndDate = null;
        } else {
            // Default to current month
            summaryStartDate = LocalDateTime.of(filterYearValue, filterMonthValueSafe, 1, 0, 0);
            summaryEndDate = summaryStartDate.plusMonths(1).minusNanos(1);
        }

//  Get comprehensive summary with income and expenses separated
        Map<String, Object> spendingSummary = transactionService.getComprehensiveSpendingSummary(
                account, summaryStartDate, summaryEndDate
        );

// Extract data from summary
        List<Map.Entry<String, BigDecimal>> categorySummary =
                (List<Map.Entry<String, BigDecimal>>) spendingSummary.get("expensesSorted");
        BigDecimal totalExpenses = (BigDecimal) spendingSummary.get("totalExpenses");
        BigDecimal totalIncome = (BigDecimal) spendingSummary.get("totalIncome");

// Add to model
        model.addAttribute("categorySummary", categorySummary);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalIncome", totalIncome);


        // 6️⃣ Model attributes
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("activeFilter", filter);
        model.addAttribute("filterValue", filterValue);
        model.addAttribute("months", List.of(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));
        model.addAttribute("years", List.of(2024, 2025, 2026)); // can generate dynamically
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("merchants", merchantRepository.findAll());

        return "account-transaction";
    }



    @PostMapping("/create")
    public String createTransaction(@RequestParam Long accountId, @RequestParam BigDecimal amount, @RequestParam(defaultValue = "") String description, @RequestParam String categoryName, @RequestParam String merchantName, Model model) {
        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

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
            return "redirect:/account/dashboard";
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
        Transaction transaction = new Transaction(user, account, amount, type, description, category, merchant);
        transaction.setAccount(account);
        transactionService.createTransaction(transaction);

        return "redirect:/transaction/account/" + accountId;
    }

    // Delete transaction
    @PostMapping("/delete/{transactionId}")
    public String deleteTransaction(@PathVariable Long transactionId, @RequestParam Long accountId) {
        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Transaction transaction = transactionService.getTransactionById(transactionId).orElse(null);
        if (transaction != null) {
            transactionService.deleteTransaction(transaction);
        }
        return "redirect:/transaction/account/" + accountId;
    }
}
