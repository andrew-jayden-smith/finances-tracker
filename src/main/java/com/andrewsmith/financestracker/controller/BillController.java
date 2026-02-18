package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.*;
import com.andrewsmith.financestracker.repository.CategoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.andrewsmith.financestracker.repository.MerchantRepository;
import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.BillService;
import com.andrewsmith.financestracker.repository.CategoryRepository;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public String viewSchedule(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year, Model model) {
        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

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

        return "bill-schedule";
    }//1'1
    // Post mapping for create bill
    @PostMapping("/create")
    public String createBill(@RequestParam String name,
                             @RequestParam BigDecimal amount,
                             @RequestParam int dueDay,
                             @RequestParam int billingMonth,
                             @RequestParam int billingYear,
                             @RequestParam String frequency,
                             @RequestParam(required = false) String categoryName, Model model) {

        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/user/login";
        }
        // Call new bill, set status, set frequency
        Bill bill = new Bill(user, name, amount, dueDay, billingMonth, billingYear);
        bill.setFrequency(BillFrequency.valueOf(frequency));
        bill.setStatus(BillStatus.DUE);

        // Link category if its provided "should be" get it from the repo
        if (categoryName != null && !categoryName.isEmpty()) {
            Category category = categoryRepository.findCategoriesByName(categoryName);
            // If there is no category already create one
            if (category == null) {
                category= new Category(categoryName);
                categoryRepository.save(category);
            }
            bill.setCategory(category);
        }
        billService.createBill(bill);

        return "redirect:/bills/schedule?month=" + billingMonth + "&year=" + billingYear;
    }

    // Post mapping for handling update bill
    @PostMapping("/update/{id}")
    public String updateBill(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam BigDecimal amount,
                             @RequestParam int dueDay,
                             @RequestParam int billingMonth,
                             @RequestParam int billingYear,
                             @RequestParam String frequency,
                             @RequestParam(required = false) String categoryName) {

        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Find the bill by its id
        Bill bill = billService.getBillById(id).orElse(null);
        if (bill == null) {
            // If the bill is null redirect to the users dashboard
            return "redirect:/bills/schedule";
        }
        bill.setName(name);
        bill.setAmount(amount);
        bill.setDueDay(dueDay);
        bill.setFrequency(BillFrequency.valueOf(frequency));
        bill.setBillingMonth(billingMonth);
        bill.setBillingYear(billingYear);

        // If there is no category already create one
        if (categoryName != null && !categoryName.isEmpty()) {
            Category category = categoryRepository.findCategoriesByName(categoryName);
            if (category == null) {
                category= new Category(categoryName);
                categoryRepository.save(category);
            }
            bill.setCategory(category);
        }
        billService.updateBill(bill);

        return "redirect:/bills/schedule?month=" + billingMonth + "&year=" + billingYear;
    }

    // Post map delete a bill
    @PostMapping("/delete/{id}")
    public String deleteBill(@PathVariable Long id, @RequestParam Integer month, @RequestParam Integer year) {
        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Find the bill
        Bill bill = billService.getBillById(id).orElse(null);
        // If the bill is not null (it exists)
        if (bill != null) {
            billService.deleteBill(bill);
        }
        return "redirect:/bills/schedule?month=" + month + "&year=" + year;
    }

    @PostMapping("/mark-paid/{id}")
    @ResponseBody
    public Map<String, Boolean> markBillPaid(@PathVariable Long id,
                                             @RequestParam Integer month,
                                             @RequestParam Integer year) {
        Bill bill = billService.getBillById(id).orElse(null);
        Map<String, Boolean> response = new HashMap<>();

        if (bill != null) {
            boolean currentlyPaid = billService.isBillPaidForMonth(id, month, year);

            if (!currentlyPaid) {
                // ✅ Mark as PAID
                LocalDate paidDate = LocalDate.of(year, month, LocalDate.now().getDayOfMonth());
                billService.recordPayment(bill, paidDate, bill.getAmount().doubleValue(), null);
                bill.setStatus(BillStatus.PAID);
                billService.updateBill(bill);

                response.put("success", true);
                response.put("paid", true);

            } else {
                // ✅ Mark as DUE (unpaid)
                bill.setStatus(BillStatus.DUE);
                billService.updateBill(bill);

                // Optional: Delete the payment record
                billService.deletePaymentForMonth(bill, month, year);

                response.put("success", true);
                response.put("paid", false);
            }
        } else {
            response.put("success", false);
            response.put("paid", false);
        }

        return response;
    }

    // Generate monthly bills
    @PostMapping("/generate-month")
    public String generateMonthlyBills(@RequestParam Integer month, @RequestParam Integer year) {
        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUsername(username).orElse(null);
        if (user != null) {
            billService.generateMonthlyBills(user, month, year);
        }
        return "redirect:/bills/schedule?month=" + month + "&year=" + year;
    }
}