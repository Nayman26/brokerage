package com.example.brokerage.controller;

import com.example.brokerage.entity.Asset;
import com.example.brokerage.entity.Customer;
import com.example.brokerage.repository.CustomerRepository;
import com.example.brokerage.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
@Tag(name = "Asset APIs", description = "Apis for managing assets")
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;
    private final CustomerRepository customerRepository;

    public AssetController(AssetService assetService, CustomerRepository customerRepository) {
        this.assetService = assetService;
        this.customerRepository = customerRepository;
    }

    @Operation(summary = "Get my assets", description = "List all assets that I have")
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<Asset> getMyAssets(Principal principal) {
        String username = principal.getName();
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return assetService.getAssetsByCustomerId(customer.getId());
    }

    @Operation(summary = "Get all assets", description = "List all assets that I have")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Asset> getAssetsByCustomerId(@RequestParam Long customerId) {
        return assetService.getAssetsByCustomerId(customerId);
    }
}
