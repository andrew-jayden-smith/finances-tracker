package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user") // User path
public class UserController {

    private final UserService userService;

    // Inject UserService class
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Login Page through login.html
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Get User by name
    // Check if email exists
    // Check if username exists

    // Register User form

    // Get accounts for user


}
