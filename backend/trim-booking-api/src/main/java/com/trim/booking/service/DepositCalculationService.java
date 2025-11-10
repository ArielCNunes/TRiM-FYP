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

    private static final BigDecimal MIN_DEPOSIT_AMOUNT = BigDecimal.valueOf(0.50);

    /**
     * Calculate deposit amount.
     * Balance is rounded UP to nearest €5, deposit is the remainder.
     *
     * @param totalPrice Total service price
     * @param depositPercentage Deposit percentage (e.g., 50 for 50%)
     * @return Deposit amount (totalPrice - balanceAmount)
     */
    public BigDecimal calculateDeposit(BigDecimal totalPrice, Integer depositPercentage) {
        if (totalPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (depositPercentage == null) {
            throw new IllegalArgumentException("Deposit percentage cannot be null");
        }

        // Calculate deposit amount using the percentage parameter
        BigDecimal depositAmount = totalPrice.multiply(BigDecimal.valueOf(depositPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Calculate target balance (remaining amount)
        BigDecimal targetBalance = totalPrice.subtract(depositAmount);

        // Round balance UP to nearest €5
        BigDecimal balanceAmount = roundUpToNearestFive(targetBalance);

        // Calculate final deposit
        BigDecimal finalDeposit = totalPrice.subtract(balanceAmount);

        // Ensure deposit meets Stripe minimum of €0.50
        if (finalDeposit.compareTo(MIN_DEPOSIT_AMOUNT) < 0) {
            return MIN_DEPOSIT_AMOUNT;
        }

        return finalDeposit;
    }

    /**
     * Calculate outstanding balance.
     * Balance is rounded up to nearest €5.
     *
     * @param totalPrice Total service price
     * @param depositPercentage Deposit percentage (e.g., 50 for 50%)
     * @return Outstanding balance (multiple of €5)
     */
    public BigDecimal calculateOutstandingBalance(BigDecimal totalPrice, Integer depositPercentage) {
        if (totalPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (depositPercentage == null) {
            throw new IllegalArgumentException("Deposit percentage cannot be null");
        }

        // Calculate deposit amount using the percentage parameter
        BigDecimal depositAmount = totalPrice.multiply(BigDecimal.valueOf(depositPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Calculate target balance (remaining amount)
        BigDecimal targetBalance = totalPrice.subtract(depositAmount);

        // Round balance UP to nearest €5
        BigDecimal roundedBalance = roundUpToNearestFive(targetBalance);

        // Check if the resulting deposit would be below minimum
        BigDecimal resultingDeposit = totalPrice.subtract(roundedBalance);
        if (resultingDeposit.compareTo(MIN_DEPOSIT_AMOUNT) < 0) {
            // Adjust balance so deposit equals minimum
            return totalPrice.subtract(MIN_DEPOSIT_AMOUNT);
        }

        return roundedBalance;
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