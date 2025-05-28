package com.example.brokerage.service.Impl;

import com.example.brokerage.entity.Asset;
import com.example.brokerage.repository.AssetRepository;
import com.example.brokerage.service.AssetService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public List<Asset> getAssetsByCustomerId(Long customerId) {
        return assetRepository.findByCustomerId(customerId);
    }

    @Override
    public Asset getCashAsset(Long customerId) {
        return assetRepository.findByCustomerIdAndAssetNameIgnoreCase(customerId, "TRY")
                .orElseThrow(() -> new IllegalArgumentException("Müşteriye ait TRY bakiyesi bulunamadı"));
    }

    @Override
    public Asset getStockAsset(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetNameIgnoreCase(customerId, assetName)
                .orElseThrow(() -> new IllegalArgumentException("Müşteriye ait hisse senedi bulunamadı: " + assetName));
    }

    @Override
    public boolean hasEnoughCash(Long customerId, BigDecimal requiredAmount) {
        return getCashAsset(customerId).getUsableSize().compareTo(requiredAmount) >= 0;
    }

    @Override
    public boolean hasEnoughStock(Long customerId, String assetName, BigDecimal requiredSize) {
        return getStockAsset(customerId, assetName).getUsableSize().compareTo(requiredSize) >= 0;
    }

    @Override
    public void reserveCash(Long customerId, BigDecimal amount) {
        updateCash(customerId, amount.negate());
    }

    @Override
    public void reserveStock(Long customerId, String assetName, BigDecimal amount) {
        updateStock(customerId, assetName, amount.negate());
    }

    @Override
    public void releaseCash(Long customerId, BigDecimal amount) {
        updateCash(customerId, amount);
    }

    @Override
    public void releaseStock(Long customerId, String assetName, BigDecimal amount) {
        updateStock(customerId, assetName, amount);
    }

    private void updateCash(Long customerId, BigDecimal delta) {
        Asset cash = getCashAsset(customerId);
        cash.setUsableSize(cash.getUsableSize().add(delta));
        assetRepository.save(cash);
    }

    private void updateStock(Long customerId, String assetName, BigDecimal delta) {
        Asset stock = getStockAsset(customerId, assetName);
        stock.setUsableSize(stock.getUsableSize().add(delta));
        assetRepository.save(stock);
    }
}