package com.trim.booking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating deposit amounts.
 * <p>
 * Balance (in-shop cash payment) is ALWAYS a multiple of €5.
 * Deposit is whatever remains to reach the total.
 */
@Service
public class DepositCalculationService {

    /**
     * Calculate deposit amount.
     * Balance is rounded UP to nearest €5, deposit is the remainder.
     *
     * @param totalPrice Total service price
     * @return Deposit amount (totalPrice - balanceAmount)
     */
    public BigDecimal calculateDeposit(BigDecimal totalPrice) {
        if (totalPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }

        // Calculate target balance (50% of total, ideally)
        BigDecimal targetBalance = totalPrice.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        // Round balance UP to nearest €5
        BigDecimal balanceAmount = roundUpToNearestFive(targetBalance);

        // Deposit is whatever remains
        return totalPrice.subtract(balanceAmount);
    }

    /**
     * Calculate outstanding balance.
     * Balance is rounded up to nearest €5.
     *
     * @param totalPrice Total service price
     * @return Outstanding balance (multiple of €5)
     */
    public BigDecimal calculateOutstandingBalance(BigDecimal totalPrice) {
        // Calculate target balance (50% of total)
        BigDecimal targetBalance = totalPrice.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        // Round balance UP to nearest €5
        return roundUpToNearestFive(targetBalance);
    }

    /**
     * Round amount UP to nearest €5.
     * €12.50 → €15.00
     */
    private BigDecimal roundUpToNearestFive(BigDecimal amount) {
        // Convert to cents (multiply by 100)
        long centsValue = amount.multiply(BigDecimal.valueOf(100)).longValue();

        // Round UP to nearest €5 (500 cents)
        // Using ceiling division: (value + 499) / 500
        long roundedCents = ((centsValue + 499) / 500) * 500;

        // Convert back to euros
        return BigDecimal.valueOf(roundedCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}