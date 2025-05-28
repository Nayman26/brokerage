package com.example.brokerage.controller;

import com.example.brokerage.dto.request.AuthRequest;
import com.example.brokerage.entity.Customer;
import com.example.brokerage.enums.Role;
import com.example.brokerage.repository.CustomerRepository;
import com.example.brokerage.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Authorization", description = "Apis for login")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Operation(summary = "login api", description = "login with username and password")
    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {
        Optional<Customer> optional = customerRepository.findByUsername(request.getUsername());
        if (optional.isEmpty())
            throw new RuntimeException("Kullanıcı bulunamadı");

        Customer customer = optional.get();
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword()))
            throw new RuntimeException("Şifre yanlış");

        Set<String> roles = customer.getRoles().stream().map(Role::name).collect(Collectors.toSet());

        return jwtService.generateToken(customer.getUsername(), roles);
    }
}