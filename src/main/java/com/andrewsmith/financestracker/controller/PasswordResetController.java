package com.andrewsmith.financestracker.controller;

import com.andrewsmith.financestracker.model.PasswordResetToken;
import com.andrewsmith.financestracker.model.User;
import org.springframework.ui.Model;
import com.andrewsmith.financestracker.service.PasswordResetService;
import com.andrewsmith.financestracker.service.RateLimitService;
import com.andrewsmith.financestracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.swing.text.html.Option;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class PasswordResetController {
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final RateLimitService rateLimitService;

    public PasswordResetController(UserService userService, PasswordResetService passwordResetService, RateLimitService rateLimitService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.rateLimitService = rateLimitService;
    }

    // Get client IP
    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    // Show forgot password form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // Process password reset request
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model, HttpServletRequest request) {

        // Normalize email
        String normalizeEmail = email.trim().toLowerCase();
        String clientIP = getClientIP(request);

        // Rate Limiting Security
        if (!rateLimitService.canRequestReset(normalizeEmail, clientIP)) {
            int minutesLeft = rateLimitService.getMinutesUntilRetry(normalizeEmail);
            model.addAttribute("error", "Too many password reset attempts. Please try again in " + minutesLeft + " minutes.");
            return "forgot-password";
        }

        // record attempt before
        rateLimitService.recordAttempt(normalizeEmail, clientIP);

        // Look up user
        Optional<User> userOptional = userService.getUserByEmail(normalizeEmail);

        // Always show successful, so it does not reveal if email exists or not
        if (userOptional.isEmpty()) {
            model.addAttribute("Success", "If an account exists with that email, a password reset link has been sent.");
            return "forgot-password";
        }

        User user = userOptional.get();

        try {
            // Create reset token
            PasswordResetToken token = passwordResetService.createPasswordResetToken(user);

            // Send Email
            passwordResetService.sendPasswordResetEmail(user, token);

            model.addAttribute("Success", "If an account exists with that email, a password reset link has been sent.");
            return "forgot-password";
        } catch (Exception e) {
            // Dont expose internal errors, log error for debug, show generic message
            System.err.println("Password reset error: " + e.getMessage());
            model.addAttribute("error", "An error occurred. Please try again later.");
            return "forgot-password";
        }
    }

    // Show reset password form
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {

        // Validate token format, prevent useless queries
        if (token == null || token.trim().isEmpty() || token.length() < 20) {
            model.addAttribute("error", "Invalid Reset Link.");
            return "reset-password-error";
        }

        Optional<PasswordResetToken> resetToken = passwordResetService.validateToken(token);

        if (resetToken.isEmpty()) {
            model.addAttribute("error", "Invalid or expired reset link.");
            return "reset-password-error";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    // Process password reset
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, @RequestParam String password, @RequestParam String confirmPassword, Model model) {
        // Validate token format
        if (token == null || token.trim().isEmpty()) {
            model.addAttribute("error", "Invalid Reset Link.");
            return "reset-password-error";
        }

        // Validate Token
        Optional<PasswordResetToken> resetTokenOpt = passwordResetService.validateToken(token);

        if (resetTokenOpt.isEmpty()) {
            model.addAttribute("error", "Invalid or expired reset link.");
            return "reset-password-error";
        }

        // Validate matching passwords
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password-error";
        }

        // Validate password strength
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            model.addAttribute("token", token);
            return "reset-password-error";
        }

        // Additional password validation
        if (!isPasswordStrong(password)) {
            model.addAttribute("error", "Password must contain at least one uppercase letter, one lowercase letter, and one number.");
            model.addAttribute("token", token);
            return "reset-password-error";
        }

        try {
            PasswordResetToken resetToken = resetTokenOpt.get();
            User user = resetToken.getUser();

            // Update password
            userService.updatePassword(user, password);

            // Delete token right after use
            passwordResetService.deleteToken(resetToken);

            model.addAttribute("success", "Password has been reset successfully. You can now login.");
            return "reset-password-success";

        } catch (Exception e) {
            // Dont expose internal errors
            System.err.println("Password reset error: " + e.getMessage());
            model.addAttribute("error", "An error occurred. Please try again later.");
            model.addAttribute("token", token);
            return "reset-password";
        }
    }

    // Validate password strength
    private boolean isPasswordStrong(String password) {
        // One uppercase, One lowercase, One digit
        return password.matches(".*[A-Z].*") && password.matches(".*[a-z].*") &&  password.matches(".*\\d.*");

    }
}
