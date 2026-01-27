package com.trim.booking.repository;

import com.trim.booking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Business-filtered methods for multi-tenancy
    Optional<Payment> findByBusinessIdAndBookingId(Long businessId, Long bookingId);

    // Keep unchanged - Stripe IDs are globally unique
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}