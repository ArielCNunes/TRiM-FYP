package com.trim.booking.repository;

import com.trim.booking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Keep unchanged - Stripe IDs are globally unique
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}