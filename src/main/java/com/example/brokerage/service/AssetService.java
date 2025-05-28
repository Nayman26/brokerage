package com.example.brokerage.service;

import com.example.brokerage.entity.Asset;

import java.math.BigDecimal;
import java.util.List;

public interface AssetService {

    List<Asset> getAssetsByCustomerId(Long customerId);

    Asset getCashAsset(Long customerId);

    Asset getStockAsset(Long customerId, String assetName);

    boolean hasEnoughCash(Long customerId, BigDecimal requiredAmount);

    boolean hasEnoughStock(Long customerId, String assetName, BigDecimal requiredSize);

    void reserveCash(Long customerId, BigDecimal amount);

    void reserveStock(Long customerId, String assetName, BigDecimal amount);

    void releaseCash(Long customerId, BigDecimal amount);

    void releaseStock(Long customerId, String assetName, BigDecimal amount);
}
