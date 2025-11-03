package com.trim.booking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating deposit amounts with proper rounding.
 * <p>
 * Rounds to nearest €5:
 * €25.50 → €25
 */
@Service
public class DepositCalculationService {

    /**
     * Calculate deposit amount from total price and deposit percentage.
     * Rounds to nearest €5.
     *
     * @param totalPrice        Total service price
     * @param depositPercentage Deposit percentage (e.g., 30 for 30%)
     * @return Deposit amount rounded to nearest €5
     */
    public BigDecimal calculateDeposit(BigDecimal totalPrice, Integer depositPercentage) {
        if (totalPrice == null || depositPercentage == null) {
            throw new IllegalArgumentException("Price and percentage cannot be null");
        }

        if (depositPercentage < 1 || depositPercentage > 100) {
            throw new IllegalArgumentException("Deposit percentage must be between 1 and 100");
        }

        // Calculate raw deposit: totalPrice * (depositPercentage / 100)
        BigDecimal depositAmount = totalPrice
                .multiply(BigDecimal.valueOf(depositPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Round to nearest €5
        return roundToNearestFive(depositAmount);
    }

    /**
     * Calculate outstanding balance.
     *
     * @param totalPrice    Total service price
     * @param depositAmount Deposit already calculated
     * @return Outstanding balance (totalPrice - depositAmount)
     */
    public BigDecimal calculateOutstandingBalance(BigDecimal totalPrice, BigDecimal depositAmount) {
        return totalPrice.subtract(depositAmount);
    }

    /**
     * Round amount to nearest €5
     */
    private BigDecimal roundToNearestFive(BigDecimal amount) {
        // Convert to cents (multiply by 100)
        long centsValue = amount.multiply(BigDecimal.valueOf(100)).longValue();

        // Round to nearest €5 (500 cents)
        long roundedCents = Math.round(centsValue / 500.0) * 500;

        // Convert back to euros
        return BigDecimal.valueOf(roundedCents).divide(BigDecimal.valueOf(100));
    }
}