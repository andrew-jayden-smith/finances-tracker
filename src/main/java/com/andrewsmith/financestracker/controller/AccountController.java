package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.Transaction;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.TransactionService;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, UserService userService, TransactionService transactionService) {
        this.accountService = accountService;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    // Dashboard page
    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam String username, Model model) {

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/user/login";
        }

        List<Account> accounts = accountService.getAllAccounts(user);

        // Map of account → current balance
        Map<Long, BigDecimal> accountBalances = new HashMap<>();

        for (Account account : accounts) {

            // Get all transactions for that account
            List<Transaction> transactions = transactionService.getAllTransactionsByAccount(account);

            BigDecimal currentBalance = account.getOpeningBalance();

            for (Transaction t : transactions) {
                currentBalance = currentBalance.add(t.getAmount());
            }

            accountBalances.put(account.getId(), currentBalance);
        }

        // Summaries
        BigDecimal totalAssets = accountBalances.values().stream()
                .filter(b -> b.signum() >= 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLiabilities = accountBalances.values().stream()
                .filter(b -> b.signum() < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netWorth = totalAssets.add(totalLiabilities);

        // Pass values to UI
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("accountBalances", accountBalances);
        model.addAttribute("totalAssets", totalAssets);
        model.addAttribute("totalLiabilities", totalLiabilities);
        model.addAttribute("netWorth", netWorth);

        return "dashboard";
    }


    // Create new accounts button(opens a pop up) and delete
    @GetMapping("/create")
    public String showCreateAccountForm(@RequestParam String username, Model model) {
        User user = userService.getUserByUsername(username).orElseThrow(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/user/login";
        }
        model.addAttribute("account", new Account());
        model.addAttribute("user", user);
        return "account-create";
    }

    // After account creation logic
    @PostMapping("/create")
    public String createAccount(@ModelAttribute Account account, @RequestParam String username, Model model) {
        User user = userService.getUserByUsername(username).orElseThrow(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/user/login";
        }
        account.setUser(user);
        account.setOpeningDate(LocalDateTime.now());
        accountService.createAccount(account);
        return "redirect:/account/dashboard?username=" + username;
    }

    // Delete Account
    @PostMapping("/delete/{accountId}")
    public String deleteAccount(@PathVariable Long accountId, @RequestParam String username) {
        accountService.deleteAccount(accountId);
        return "redirect:/account/dashboard?username=" + username;
    }



    // Select an account takes you into the account information (checkbook style with rows of transactions for the selected(week, month, year)

}