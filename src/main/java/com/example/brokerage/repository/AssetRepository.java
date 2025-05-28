package com.example.brokerage.repository;

import com.example.brokerage.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCustomerId(Long customerId);

    Optional<Asset> findByCustomerIdAndAssetNameIgnoreCase(Long customerId, String assetName);
}