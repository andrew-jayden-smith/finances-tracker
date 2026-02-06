package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.repository.CategoryRepository;
import org.springframework.ui.Model;
import com.andrewsmith.financestracker.model.Bill;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.MerchantRepository;
import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.BillService;
import com.andrewsmith.financestracker.repository.CategoryRepository;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequestMapping("/bills")
public class BillController {

    // Call and Inject all services and repos needed
    private final BillService billService;
    private final UserService userService;
    private final AccountService accountService;
    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;

    public BillController(BillService billService, UserService userService, CategoryRepository categoryRepository, AccountService accountService, MerchantRepository merchantRepository) {
        this.billService = billService;
        this.userService = userService;
        this.accountService = accountService;
        this.categoryRepository = categoryRepository;
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
        // Set paid status
        for (Bill bill : bills) {
            boolean isPaid = billService.isBillPaidForMonth(bill.getId(), specificMonth, specificYear);
            paidStatus.put(bill.getId(), isPaid);
        }

        //Check for overdue bills
        Set<Long> overdueBills = new HashSet<>();
        if(specificMonth == today.getMonthValue() && specificYear == today.getYear()) {
            List<Bill> overdues = billService.getOverdueBills(user, today);
            overdues.forEach(b -> overdueBills.add(b.getId()));
        }

        // Month navigation using Java built in
        YearMonth currenYearMonth = YearMonth.of(specificYear, specificMonth);
        YearMonth previousMonth = currenYearMonth.minusMonths(1);
        YearMonth nextMonth = currenYearMonth.plusMonths(1);

        String monthName = currenYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        model.addAttribute("user", user);
        model.addAttribute("bills", bills);
        model.addAttribute("stats", stats);
        model.addAttribute("billsByWeek", billsByWeek);
        model.addAttribute("paidStatus", paidStatus);
        model.addAttribute("overdueBills", overdueBills);
        model.addAttribute("currentMonth", monthName);
        model.addAttribute("currentYear", specificYear);
        model.addAttribute("monthValue", specificMonth);
        model.addAttribute("yearValue", specificYear);
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("today", today);
        model.addAttribute("categories", categoryRepository.findAll());

        return "bills-schedule";
    }//1'1

    // Post mapping for create bill
    // if user is null case
    // Call new bill, set status, set frequency
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
