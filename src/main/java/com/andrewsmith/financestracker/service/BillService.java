package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.*;
import com.andrewsmith.financestracker.repository.BillPaymentRepository;
import com.andrewsmith.financestracker.repository.BillRepository;
import com.andrewsmith.financestracker.repository.BillPaymentRepository;
import io.micrometer.observation.annotation.ObservationKeyValue;
import org.springframework.stereotype.Service;
import org.thymeleaf.postprocessor.IPostProcessor;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final BillPaymentRepository billPaymentRepository;

    // Inject the repos
    public BillService(BillRepository billRepository, BillPaymentRepository billPaymentRepository) {
        this.billRepository = billRepository;
        this.billPaymentRepository = billPaymentRepository;
    }

    // Get Bills for the Account
    public List<Bill> getAllBillsForUser(User user) {
        return billRepository.findByUserOrderByDueDayAsc(user);
    }
    // Get Bills by id
    public Optional<Bill> getBillById(Long id) {
        return billRepository.findById(id);
    }
    // Create New bill
    public Bill createBill(Bill bill) {
        bill.setStatus(BillStatus.DUE);
        return billRepository.save(bill);
    }
    // Update Bill
    public Bill updateBill(Bill bill) {
        return billRepository.save(bill);
    }
    // Delete Bill
    public void deleteBill(Bill bill) {
        billRepository.delete(bill);
    }

    // Filtering by Month/Year bills
    public List<Bill> getBillsForMonth(User user, int month, int year) {
        List<Bill> specificBills = billRepository.findByUserAndBillingMonthAndBillingYearOrderByDueDayAsc(user, month, year);

        List<Bill> monthlyBills = getAllBillsForUser(user).stream().filter(b -> b.getFrequency() == BillFrequency.MONTHLY).toList();

        // Combine to remove duplicate bills
        Set<Bill> allBills = new HashSet<>(specificBills);
        allBills.addAll(monthlyBills);

        return allBills.stream().sorted(Comparator.comparing(Bill::getDueDay)).collect(Collectors.toList());
    }

    // Get yearly bills
    public List<Bill> getBillsForYear(User user, int year) {
        return billRepository.findByUserAndBillingYearOrderByBillingMonthAscDueDayAsc(user,  year);
    }

    // Payment tracking
    public BillPayment recordPayment(Bill bill, LocalDate paidDate, Double amountPaid, Transaction transaction) {
        BillPayment Payment = new BillPayment(bill, paidDate, amountPaid, transaction);
        bill.setStatus(BillStatus.PAID);
        billRepository.save(bill);
        return billPaymentRepository.save(Payment);
    }

    public boolean isBillPaidForMonth(Long billId, int month, int year) {
        return billPaymentRepository.isPaidForMonth(billId, month, year);
    }

    public Optional<BillPayment> getPaymentForBillInMonth(Bill bill, int month, int year) {
        return billPaymentRepository.findByBillAndMonthYear(bill, month, year);
    }

    // Payment history
    public List<BillPayment> getPaymentHistory(Bill bill) {
        return billPaymentRepository.findByBillOrderByPaidDateDesc(bill);
    }

    // Stats on Dashboard
    public Map<String, Object> getMonthlyStats(User user, int month, int year) {
        List<Bill> bills = getBillsForMonth(user, month, year);

        BigDecimal totalDue = bills.stream().map(Bill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = bills.stream().filter(b -> isBillPaidForMonth(b.getId(), month, year)).map(Bill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = totalDue.subtract(totalPaid);

        long countDue = bills.stream().filter(b -> isBillPaidForMonth(b.getId(), month, year)).count();

        // Map to put the monthly stats in for the dashboard
        Map<String, Object> monthlyStats = new HashMap<>();
        monthlyStats.put("totalDue", totalDue);
        monthlyStats.put("totalPaid", totalPaid);
        monthlyStats.put("remaining", remaining);
        monthlyStats.put("countDue", countDue);
        monthlyStats.put("totalBills", bills.size());
        // return stats in map
        return monthlyStats;
    }

    // Weekly Grouped
    public Map<Integer, List<Bill>> groupBillsByWeek(List<Bill> bills) {
        return bills.stream().collect(Collectors.groupingBy(bill -> {int day = bill.getDueDay(); return (day - 1) / 7 + 1;}));
    }

    // Bill Status Check
    public List<Bill> getOverdueBills(User user, LocalDate currentDate) {
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();
        int currentDay = currentDate.getDayOfMonth();

        return getBillsForMonth(user, currentMonth, currentYear).stream().filter(b -> b.getDueDay() < currentDay).filter(b -> !isBillPaidForMonth(b.getId(), currentMonth, currentYear)).collect(Collectors.toList());
    }

    public List<Bill> getUpcomingBills(User user, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);

        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        int currentDay = today.getDayOfMonth();
        int futureDay = futureDate.getMonthValue();

        return getBillsForMonth(user, currentMonth, currentYear).stream().filter(b -> b.getDueDay() >= currentDay && b.getDueDay() <= futureDay).filter(b -> !isBillPaidForMonth(b.getId(), currentMonth, currentYear)).collect(Collectors.toList());
    }

    // Auto-generate bills for each recurring
    public void generateMonthlyBills(User user, int month, int year) {
        List<Bill> monthlyBills = getAllBillsForUser(user).stream().filter(b -> b.getFrequency() == BillFrequency.MONTHLY).collect(Collectors.toList());

        for (Bill template : monthlyBills) {
            List<Bill> existing = billRepository.findByUserAndBillingMonthAndBillingYearOrderByDueDayAsc(user, month, year);

        boolean alreadyExists = existing.stream().anyMatch(b -> b.getName().equals(template.getName()) && b.getDueDay() == template.getDueDay());

        // if the bill does not already exist create a new bill and save it
        if (!alreadyExists) {
            Bill newBill = new Bill(user, template.getName(), template.getAmount(), template.getDueDay(), month, year);

            newBill.setStatus(BillStatus.DUE);
            newBill.setFrequency(BillFrequency.MONTHLY);
            billRepository.save(newBill);

            // createBill(newBill); creates and sets it due but does not set frequency
            }
        }
    }
}
