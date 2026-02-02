package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.repository.AccountRepository;
import org.springframework.ui.Model;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user") // User path
public class UserController {

    private final UserService userService;
    private final AccountRepository accountRepository;

    // Inject UserService class
    public UserController(UserService userService, AccountRepository accountRepository) {
        this.userService = userService;
        this.accountRepository = accountRepository;
    }

    // Login Page through login.html
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Login form submission
    @PostMapping("/login")
    public String authenticateLogin(@RequestParam String username, @RequestParam String password, Model model) {
        // 1. Get User by username or email by if statement
        User user = userService.getUserByUsername(username).orElse(null);

        if (user == null) {
            model.addAttribute("error", "User Not Found!");
            return "login";
        }
        // 2. Validate password matches by if
        if(!user.getPasswordHash().equals(password)) {
            model.addAttribute("error", "Wrong Password!");
            return "login";
        }
        // 3. Load accounts for users
        model.addAttribute("user", user);
        model.addAttribute("accounts", userService.getUserAccounts(user));
        return "redirect:/account/dashboard?username=" + username;
    }

    // Register user Page through register.html
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Register user form logic
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        // Check if username or email exists in db
        if (userService.userExists(user.getUsername())) {
            model.addAttribute("error", "User Already Exists!");
            return "register";
        }
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email Already Exists!");
            return "register";
        }
        // Save user if not existing
        userService.createUser(user);
        // Redirect to login.html upon user creation
        return "redirect:/user/login";
    }
}
