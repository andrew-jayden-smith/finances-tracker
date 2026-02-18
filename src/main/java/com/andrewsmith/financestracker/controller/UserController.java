package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Show login page
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    /**
     * Show signup page
     */
    @GetMapping("/register")
    public String showSignupPage() {
        return "register";
    }

    /**
     * Handle user registration
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam String password,
                               @RequestParam(required = false) String email,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Model model) {

        // Get username from Spring security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();


        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                model.addAttribute("error", "Username is required");
                return "register";
            }

            if (password == null || password.length() < 6) {
                model.addAttribute("error", "Password must be at least 6 characters");
                return "register";
            }

            // Register user (password will be encrypted in service)
            User user = userService.registerUser(username, password, email);

            // Auto-login after registration
            authenticateUser(user, password, request, response);

            // Redirect to dashboard
            return "redirect:/account/dashboard";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
    }

    /**
     * Programmatically authenticate user (for auto-login after registration)
     */
    private void authenticateUser(User user, String password,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        // Create authentication token
        UsernamePasswordAuthenticationToken token =
                UsernamePasswordAuthenticationToken.authenticated(
                        user.getUsername(),
                        password,
                        java.util.Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        // Create security context
        SecurityContextHolderStrategy securityContextHolderStrategy =
                SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(token);
        securityContextHolderStrategy.setContext(context);

        // Save to session
        securityContextRepository.saveContext(context, request, response);
    }

    /**
     * Handle logout (Spring Security handles this automatically via /user/logout)
     * This endpoint is just in case you need manual logout
     */
    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/user/login?logout=true";
    }
}