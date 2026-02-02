package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.service.AccountService;
import com.andrewsmith.financestracker.service.TransactionService;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final UserService userService;

    // Inject all classes
    public TransactionController(TransactionService transactionService, AccountService accountService, UserService userService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.userService = userService;
    }

    //
}
