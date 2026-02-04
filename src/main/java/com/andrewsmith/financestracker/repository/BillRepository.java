package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Account;
import com.andrewsmith.financestracker.model.Bill;
import com.andrewsmith.financestracker.model.BillStatus;
import com.andrewsmith.financestracker.model.User;
import org.hibernate.boot.model.internal.ListBinder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    // Get all bills for user
    List<Bill> findByUserOrderByDueDayAsc(User user);
    // Find bills with specified month/year
    List<Bill> findByUserAndBillingMonthAndBillingYearOrderByDueDayAsc(User user, int billingMonth, int billingYear);
    // Check if bill has been paid
    List<Bill> findByUserAndStatusOrderByDueDayAsc(User user, BillStatus status);
    // Get repeated monthly bills, year does not matter
    @Query("SELECT b FROM Bill b WHERE b.user = :user AND " + "(b.billingMonth = :month OR b.frequency = 'MONTHLY') " +
    "ORDER BY b.dueDay ASC")
    List<Bill> findBillsDueInMonth(@Param("user") User user, @Param("month") int month);

    List<Bill> findByUserAndBillingYearOrderByBillingMonthAscDueDayAsc(User user, int billingYear);
}

