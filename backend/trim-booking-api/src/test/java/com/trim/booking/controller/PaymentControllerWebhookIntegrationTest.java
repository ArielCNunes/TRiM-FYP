package com.trim.booking.controller;

import com.trim.booking.entity.*;
import com.trim.booking.repository.*;
import com.trim.booking.service.notification.EmailService;
import com.trim.booking.service.notification.SmsService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentController.handleStripeWebhook()
 *
 * Tests cover:
 * - Successful payment webhook handling (booking confirmation)
 * - Payment and booking status transitions
 * - Invalid payload handling
 * - Unknown payment intent handling
 * - Different webhook event types
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentControllerWebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceCategoryRepository categoryRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Mock external services to avoid actual email/SMS sending
    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private SmsService smsService;

    private User customer;
    private Barber barber;
    private ServiceOffered service;
    private Booking booking;
    private Payment payment;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        barberRepository.deleteAll();
        serviceRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create a customer
        customer = new User();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("customer@test.com");
        customer.setPasswordHash("hashedpassword");
        customer.setPhone("+353871234567");
        customer.setRole(User.Role.CUSTOMER);
        customer = userRepository.save(customer);

        // Create a barber user
        User barberUser = new User();
        barberUser.setFirstName("Jane");
        barberUser.setLastName("Barber");
        barberUser.setEmail("barber@test.com");
        barberUser.setPasswordHash("hashedpassword");
        barberUser.setPhone("+353877654321");
        barberUser.setRole(User.Role.BARBER);
        barberUser = userRepository.save(barberUser);

        // Create barber entity
        barber = new Barber();
        barber.setUser(barberUser);
        barber.setBio("Expert barber");
        barber.setActive(true);
        barber = barberRepository.save(barber);

        // Create service category
        ServiceCategory category = new ServiceCategory("Haircuts");
        category.setActive(true);
        category = categoryRepository.save(category);

        // Create a service
        service = new ServiceOffered();
        service.setName("Standard Haircut");
        service.setDescription("Basic haircut service");
        service.setDurationMinutes(30);
        service.setPrice(new BigDecimal("25.00"));
        service.setDepositPercentage(20);
        service.setActive(true);
        service.setCategory(category);
        service = serviceRepository.save(service);

        // Create a pending booking
        booking = new Booking();
        booking.setCustomer(customer);
        booking.setBarber(barber);
        booking.setService(service);
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(10, 30));
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PENDING);
        booking.setDepositAmount(new BigDecimal("5.00")); // 20% of 25
        booking.setOutstandingBalance(new BigDecimal("20.00"));
        booking.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));
        booking = bookingRepository.save(booking);

        // Create a pending payment record
        payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(new BigDecimal("5.00"));
        payment.setStripePaymentIntentId("pi_test_123456");
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        // Reset mocks
        reset(emailService, smsService);
    }

    // ==================== SUCCESSFUL WEBHOOK TESTS ====================

    @Test
    @DisplayName("Should confirm booking when payment_intent.succeeded webhook received")
    void handleWebhook_PaymentSucceeded_ConfirmsBooking() throws Exception {
        String webhookPayload = createPaymentSucceededPayload("pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        // Verify booking status changed to CONFIRMED
        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(Booking.BookingStatus.CONFIRMED, updatedBooking.getStatus());
    }

    @Test
    @DisplayName("Should update payment status to DEPOSIT_PAID on successful payment")
    void handleWebhook_PaymentSucceeded_UpdatesPaymentStatus() throws Exception {
        String webhookPayload = createPaymentSucceededPayload("pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk());

        // Verify booking payment status
        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(Booking.PaymentStatus.DEPOSIT_PAID, updatedBooking.getPaymentStatus());
    }

    @Test
    @DisplayName("Should update payment record status to SUCCEEDED")
    void handleWebhook_PaymentSucceeded_UpdatesPaymentRecord() throws Exception {
        String webhookPayload = createPaymentSucceededPayload("pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk());

        // Verify payment record status
        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(Payment.PaymentStatus.SUCCEEDED, updatedPayment.getStatus());
    }

    @Test
    @DisplayName("Should clear booking expiry after successful payment")
    void handleWebhook_PaymentSucceeded_ClearsExpiresAt() throws Exception {
        // Ensure booking has expiry set
        assertNotNull(booking.getExpiresAt());

        String webhookPayload = createPaymentSucceededPayload("pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk());

        // Verify expiry is cleared
        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertNull(updatedBooking.getExpiresAt());
    }

    @Test
    @DisplayName("Should send confirmation email after successful payment")
    void handleWebhook_PaymentSucceeded_SendsConfirmationEmail() throws Exception {
        String webhookPayload = createPaymentSucceededPayload("pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk());

        // Verify email service was called
        verify(emailService, times(1)).sendBookingConfirmation(any(Booking.class));
    }

    @Test
    @DisplayName("Should send confirmation SMS after successful payment")
    void handleWebhook_PaymentSucceeded_SendsConfirmationSms() throws Exception {
        String webhookPayload = createPaymentSucceededPayload("pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk());

        // Verify SMS service was called
        verify(smsService, times(1)).sendBookingConfirmation(any(Booking.class));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Should return 500 when payment intent not found in database")
    void handleWebhook_UnknownPaymentIntent_ReturnsError() throws Exception {
        String webhookPayload = createPaymentSucceededPayload("pi_unknown_999999");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Webhook processing failed"));

        // Verify original booking unchanged
        Booking unchangedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(Booking.BookingStatus.PENDING, unchangedBooking.getStatus());
    }

    @Test
    @DisplayName("Should return bad request for invalid JSON payload")
    void handleWebhook_InvalidJson_ReturnsBadRequest() throws Exception {
        String invalidPayload = "{ invalid json }";

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid payload"));
    }

    @Test
    @DisplayName("Should return OK for non-payment event types (ignored)")
    void handleWebhook_OtherEventType_ReturnsOkButIgnores() throws Exception {
        String webhookPayload = createWebhookPayload("customer.created", "pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        // Verify booking was NOT updated (event ignored)
        Booking unchangedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(Booking.BookingStatus.PENDING, unchangedBooking.getStatus());
    }

    @Test
    @DisplayName("Should not send notifications when payment fails to process")
    void handleWebhook_ProcessingFails_NoNotificationsSent() throws Exception {
        String webhookPayload = createPaymentSucceededPayload("pi_unknown_999999");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isInternalServerError());

        // Verify no notifications were sent
        verify(emailService, never()).sendBookingConfirmation(any());
        verify(smsService, never()).sendBookingConfirmation(any());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle payment_intent.payment_failed event gracefully")
    void handleWebhook_PaymentFailed_ReturnsOk() throws Exception {
        String webhookPayload = createWebhookPayload("payment_intent.payment_failed", "pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        // Booking should remain unchanged (failed events not processed yet)
        Booking unchangedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(Booking.BookingStatus.PENDING, unchangedBooking.getStatus());
    }

    @Test
    @DisplayName("Should handle empty payload")
    void handleWebhook_EmptyPayload_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should process multiple bookings independently")
    void handleWebhook_MultipleBookings_ProcessesCorrectOne() throws Exception {
        // Create a second booking with different payment intent
        Booking booking2 = new Booking();
        booking2.setCustomer(customer);
        booking2.setBarber(barber);
        booking2.setService(service);
        booking2.setBookingDate(LocalDate.now().plusDays(2));
        booking2.setStartTime(LocalTime.of(14, 0));
        booking2.setEndTime(LocalTime.of(14, 30));
        booking2.setStatus(Booking.BookingStatus.PENDING);
        booking2.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PENDING);
        booking2.setDepositAmount(new BigDecimal("5.00"));
        booking2.setOutstandingBalance(new BigDecimal("20.00"));
        booking2 = bookingRepository.save(booking2);

        Payment payment2 = new Payment();
        payment2.setBooking(booking2);
        payment2.setAmount(new BigDecimal("5.00"));
        payment2.setStripePaymentIntentId("pi_test_second_789");
        payment2.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment2);

        // Process webhook for first booking only
        String webhookPayload = createPaymentSucceededPayload("pi_test_123456");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk());

        // Verify first booking is confirmed
        Booking updatedBooking1 = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(Booking.BookingStatus.CONFIRMED, updatedBooking1.getStatus());

        // Verify second booking is still pending
        Booking updatedBooking2 = bookingRepository.findById(booking2.getId()).orElseThrow();
        assertEquals(Booking.BookingStatus.PENDING, updatedBooking2.getStatus());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a Stripe webhook payload for payment_intent.succeeded event.
     */
    private String createPaymentSucceededPayload(String paymentIntentId) {
        return createWebhookPayload("payment_intent.succeeded", paymentIntentId);
    }

    /**
     * Create a generic Stripe webhook payload with specified event type.
     */
    private String createWebhookPayload(String eventType, String paymentIntentId) {
        return String.format("""
                {
                    "id": "evt_test_%s",
                    "object": "event",
                    "type": "%s",
                    "data": {
                        "object": {
                            "id": "%s",
                            "object": "payment_intent",
                            "amount": 500,
                            "currency": "eur",
                            "status": "succeeded",
                            "metadata": {
                                "booking_id": "%d"
                            }
                        }
                    }
                }
                """, System.currentTimeMillis(), eventType, paymentIntentId, booking != null ? booking.getId() : 1);
    }
}

