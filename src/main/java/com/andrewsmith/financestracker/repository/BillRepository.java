package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Bill;
import com.andrewsmith.financestracker.model.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends CrudRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {
    List<Bill> findByUser(User user);
    // Check if bill has been paid
    List<Bill> findByUserAndNextDueDateBetween(User user, LocalDate start, LocalDate end);

    Bill findByUserAndNameIgnoreCase(User user, String name);
 }

