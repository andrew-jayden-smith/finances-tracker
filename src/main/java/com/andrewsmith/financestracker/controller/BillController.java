package com.andrewsmith.financestracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bills")
public class BillController {

    // Call and Inject all services and repos needed

    // Schedule view for calendar
    // Get mapping for route and view method with request parameters and constructors
    // set user null cases
    // set current month for default or no selection, today, month, year
    // List bills for the month
    // Map bills by week and Map bills by stats
    // Map payment status paidStatus, loop through the bills {boolean isPaid} to check the status of all bills
    // Check for overdue bills, if its over the target month
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
