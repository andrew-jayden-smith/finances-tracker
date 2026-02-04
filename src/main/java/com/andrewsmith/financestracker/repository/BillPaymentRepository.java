package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Bill;
import com.andrewsmith.financestracker.model.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    List<BillPayment> findByBill(Bill bill);
    List<BillPayment> findByBillAndPaidDateBetween(Bill bill, LocalDate start, LocalDate end);
    BillPayment findTopByBillOrderByPaidDateDesc(Bill bill); // last payment
}

