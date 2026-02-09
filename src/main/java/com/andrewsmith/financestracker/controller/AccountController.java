package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.*;
import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.BillService;
import com.andrewsmith.financestracker.service.TransactionService;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final BillService billService;

    public AccountController(AccountService accountService, UserService userService, TransactionService transactionService, BillService billService) {
        this.accountService = accountService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.billService = billService;
    }

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

        // Bills
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        // Get bills for current month
        List<Bill> currentMonthBills = billService.getBillsForMonth(user, month, year);

        // Check payment status for each bill
        Map<Long, Boolean> billsPaidStatus = new HashMap<>();
        for (Bill bill : currentMonthBills) {
            // Use the bill ID from the database, not a newly generated object
            boolean isPaid = billService.isBillPaidForMonth(bill.getId(), bill.getBillingMonth(), bill.getBillingYear());
            billsPaidStatus.put(bill.getId(), isPaid);

            // Optional: also update the bill status for display
            bill.setStatus(isPaid ? BillStatus.PAID : BillStatus.DUE);
        }

        // Get overdue bills
        List<Bill> overdueBills = billService.getOverdueBills(user, today);

        // Get stats after checking paid status
        Map<String, Object> billStats = billService.getMonthlyStats(user, month, year);

        // Month name for display
        String currentMonthName = today.getMonth().toString();
        currentMonthName = currentMonthName.substring(0, 1) +
                currentMonthName.substring(1).toLowerCase();

        // Pass values to UI
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("accountBalances", accountBalances);
        model.addAttribute("totalAssets", totalAssets);
        model.addAttribute("totalLiabilities", totalLiabilities);
        model.addAttribute("netWorth", netWorth);

        // Bills attributes
        model.addAttribute("currentMonthBills", currentMonthBills);
        model.addAttribute("billStats", billStats);
        model.addAttribute("billsPaidStatus", billsPaidStatus);
        model.addAttribute("overdueBills", overdueBills);
        model.addAttribute("currentMonthName", currentMonthName);
        model.addAttribute("currentDayOfMonth", today.getDayOfMonth());
        model.addAttribute("totalBills", billService.getAllBillsForUser(user).size());

        return "dashboard";
    }

    @GetMapping("/create")
    public String showCreateAccountForm(@RequestParam String username, Model model) {
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/user/login";
        }
        model.addAttribute("account", new Account());
        model.addAttribute("user", user);
        return "account-create";
    }

    @PostMapping("/create")
    public String createAccount(@ModelAttribute Account account, @RequestParam String username, Model model) {
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/user/login";
        }
        account.setUser(user);
        account.setOpeningDate(LocalDateTime.now());
        accountService.createAccount(account);
        return "redirect:/account/dashboard?username=" + username;
    }

    @PostMapping("/delete/{accountId}")
    public String deleteAccount(@PathVariable Long accountId, @RequestParam String username) {
        accountService.deleteAccount(accountId);
        return "redirect:/account/dashboard?username=" + username;
    }

    // Reorder endpoint for drag-and-drop
    @PostMapping("/reorder")
    @ResponseBody
    public Map<String, Boolean> reorderAccounts(@RequestBody List<Map<String, Object>> orderData) {
        Map<String, Boolean> response = new HashMap<>();

        try {
            for (Map<String, Object> item : orderData) {
                Long accountId = Long.parseLong(item.get("id").toString());
                Integer newOrder = Integer.parseInt(item.get("order").toString());

                Optional<Account> accountOpt = accountService.getAccountById(accountId);
                if (accountOpt.isPresent()) {
                    Account account = accountOpt.get();
                    account.setDisplayOrder(newOrder);
                    accountService.updateAccount(account);
                }
            }
            response.put("success", true);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            return response;
        }
    }
}