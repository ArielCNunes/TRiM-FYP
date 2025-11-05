package com.trim.booking.dto;

import java.math.BigDecimal;

public class GuestBookingResponse {
    private Long bookingId;
    private Long customerId;
    private BigDecimal depositAmount;
    private BigDecimal outstandingBalance;
    private String customerEmail;

    // Constructors
    public GuestBookingResponse() {
    }

    public GuestBookingResponse(Long bookingId, Long customerId, BigDecimal depositAmount,
                                BigDecimal outstandingBalance, String customerEmail) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.depositAmount = depositAmount;
        this.outstandingBalance = outstandingBalance;
        this.customerEmail = customerEmail;
    }

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}

