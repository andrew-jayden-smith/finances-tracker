package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.Bill;
import com.andrewsmith.financestracker.model.BillStatus;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.BillPaymentRepository;
import com.andrewsmith.financestracker.repository.BillRepository;
import com.andrewsmith.financestracker.repository.BillPaymentRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.postprocessor.IPostProcessor;

import java.util.List;
import java.util.Optional;

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


    // Payment tracking


    // Stats on Dashbaord


    // Weekly Grouped


    // Status Check


    // Auto-generate bills for each recurring
}
