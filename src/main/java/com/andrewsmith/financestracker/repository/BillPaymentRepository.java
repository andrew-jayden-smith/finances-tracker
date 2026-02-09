package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Bill;
import com.andrewsmith.financestracker.model.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    // Find all payments for a specific bill
    List<BillPayment> findByBillOrderByPaidDateDesc(Bill bill);

    // Find payment for a bill in a specific month/year
    @Query("SELECT bp FROM BillPayment bp WHERE bp.bill = :bill AND " +
            "MONTH(bp.paidDate) = :month AND YEAR(bp.paidDate) = :year")
    Optional<BillPayment> findByBillAndMonthYear(
            @Param("bill") Bill bill,
            @Param("month") int month,
            @Param("year") int year);

    // Find all payments in a date range
    List<BillPayment> findByPaidDateBetweenOrderByPaidDateDesc(
            LocalDate startDate, LocalDate endDate);

    // Check if bill was paid in specific month/year
    @Query("SELECT CASE WHEN COUNT(bp) > 0 THEN true ELSE false END " +
            "FROM BillPayment bp WHERE bp.bill.id = :billId AND " +
            "MONTH(bp.paidDate) = :month AND YEAR(bp.paidDate) = :year")
    boolean isPaidForMonth(
            @Param("billId") Long billId,
            @Param("month") int month,
            @Param("year") int year
    );
}