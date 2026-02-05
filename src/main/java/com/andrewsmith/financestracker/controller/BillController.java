package com.andrewsmith.financestracker.controller;

import ch.qos.logback.core.model.Model;
import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.Bill;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.MerchantRepository;
import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.BillService;
import com.andrewsmith.financestracker.service.CategoryService;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bills")
public class BillController {

    // Call and Inject all services and repos needed
    private final BillService billService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final MerchantRepository merchantRepository;

    public BillController(BillService billService, UserService userService, CategoryService categoryService, AccountService accountService, MerchantRepository merchantRepository) {
        this.billService = billService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.accountService = accountService;
        this.merchantRepository = merchantRepository;
    }

    // Schedule view for calendar
    // Get mapping for route and view method with request parameters and constructors
    @GetMapping("/schedule")
    public String viewSchedule(@RequestParam String username, @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year, Model model) {
        // Verify user
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/user/login";
        }

        // set current month for default or no selection, today, month, year
        LocalDate today = LocalDate.now();

        int specificMonth;
        if (month != null) {
            specificMonth = month;
        } else {
            specificMonth = today.getMonthValue();
        }
        //int specificMonth = month != null ? month : today.getMonthValue();

        int specificYear;
        if (year != null) {
            specificYear = year;
        } else {
            specificYear = today.getYear();
        }
        //int specificYear = year != null ? year : today.getYear();

        // List bills for the month
        List<Bill> bills = billService.getBillsForMonth(user, specificMonth, specificYear);

        // Map bills due by week (Weeks are key and List of Bills is value)
        Map<Integer, List<Bill>> billsByWeek = billService.groupBillsByWeek(bills); // Week 1, [Electric, Water] and Week 2, [Gas]

        // Map bills by stats with String(name) and Object(int, double, can store any)
        Map<String, Object> stats = billService.getMonthlyStats(user, specificMonth, specificYear);

        // Map payment status(key: billId, value is true/false)
        Map<Long, Boolean> paidStatus = new HashMap<>();
        // Check for overdue bills, if its over the target month
        for (Bill bill : bills) {
            boolean isPaid = billService.isBillPaidForMonth(bill.getId(), specificMonth, specificYear);
            paidStatus.put(bill.getId(), isPaid);
        }

        // Month navigation



        return "bills/schedule";
    }

    // Month navigation
    // model.addAttribute for all variables
    // return bills-schedule view

    // Post mapping for create bill
    // if user is null case
    // Call new bill, set status, set frquency
    // Link category if its provided "should be"
    // return the link of "redirect:/bills/schedule?username=" + username + "&month=" + billingMonth + "&year=" + billingYear;

    // Post mapping for handling update bill
    // Call all constructors
    // case if the bill is null then return to the link "redirect:/bills/schedule?username=" + username;
    // bill.set.. variable names
    // if case if the category is not null and doesnt have a name, call category to find that name, if category is null create a new categoryName for it
    // return same link as creating bill

    // Post map delete a bill
    // Bill bill = find by id
    // if its not null then .deleteBill
    // return "redirect:/bills/schedule?username=" + username + "&month=" + month + "&year=" + year;

    // Get mapping "mark-paid/{id}", response body
    // Map String Boolean for markBillPaid
    // Call the bill by id from service
    // response is a new HashMap
    // if the bill is not null, then the bill is boolean alreadyPaid for isBillPaidForMonth, else its not been paid
    // if its not already paid, set the paidDate, .recordPayment, response.put

    // Generate monthly bills
    // post map "generate-month"
    // getUserByUsername, if its not null then generateMonthlyBills
    // return the same redirect as delete bill
}
