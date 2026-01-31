package com.andrewsmith.financestracker;

import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.UserRepository;
import com.andrewsmith.financestracker.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.SQLOutput;
import java.util.Optional;

@SpringBootApplication
public class FinancesTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancesTrackerApplication.class, args);
    }

    @Bean
    CommandLineRunner testUserRepo(UserRepository userRepository, UserService userService) {
        return args -> {
            System.out.println("Testing UserService Class: ");

            Optional<User> user = userService.getUserByUsername("smithdr3w");
            Optional<User> userEmail = userService.getUserByEmail("smithdrew867@gmail.com");

            System.out.println("Found User? " + user.isPresent());
            System.out.println("Found User Email? " + userEmail.isPresent());

        };
    }
}
