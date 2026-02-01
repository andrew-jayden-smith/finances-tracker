package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    // Dashboard page
    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam String username, Model model) {
        User user = userService.getUserByUsername(username).orElseThrow(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/user/login";
        }
        // Load the users accounts
        List<Account> account = accountService.getAllAccounts(user);
        model.addAttribute("user", user);
        model.addAttribute("accounts", account);

        // Calculating Accounts and Summary
        BigDecimal totalAssets = account.stream().filter(a -> a.getOpeningBalance().compareTo(BigDecimal.ZERO) >= 0).map(Account::getOpeningBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLiabilities = account.stream().filter(a -> a.getOpeningBalance().compareTo(BigDecimal.ZERO) < 0).map(Account::getOpeningBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netWorth = totalAssets.add(totalLiabilities);

        model.addAttribute("totalAssets", totalAssets);
        model.addAttribute("totalLiabilities", totalLiabilities);
        model.addAttribute("netWorth", netWorth);
        return "dashboard";
    }

    // Create new accounts button(opens a pop up) and delete current
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
        account.setOpeningBalance(BigDecimal.ZERO);
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