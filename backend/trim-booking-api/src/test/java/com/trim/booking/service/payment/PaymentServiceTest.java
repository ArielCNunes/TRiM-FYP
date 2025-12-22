package com.trim.booking.service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.trim.booking.entity.*;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.PaymentRepository;
import com.trim.booking.service.notification.EmailService;
import com.trim.booking.service.notification.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DepositCalculationService depositCalculationService;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                bookingRepository,
                depositCalculationService,
                emailService,
                smsService
        );
    }

    @Nested
    @DisplayName("createDepositPaymentIntent")
    class CreateDepositPaymentIntentTests {

        @Test
        @DisplayName("Should throw RuntimeException when booking not found")
        void shouldThrowWhenBookingNotFound() {
            // Given
            Long bookingId = 999L;
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> paymentService.createDepositPaymentIntent(bookingId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Booking not found");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when deposit below 50 cents")
        void shouldThrowWhenDepositBelowMinimum() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, BigDecimal.valueOf(0.30), 50);

            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            // Return deposit of 0.15 EUR (15 cents - below Stripe minimum)
            when(depositCalculationService.calculateDeposit(any(), any()))
                    .thenReturn(BigDecimal.valueOf(0.15));
            when(depositCalculationService.calculateOutstandingBalance(any(), any()))
                    .thenReturn(BigDecimal.valueOf(0.15));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            // When/Then
            assertThatThrownBy(() -> paymentService.createDepositPaymentIntent(bookingId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Deposit amount must be at least 0.50 EUR");
        }

        @Test
        @DisplayName("Should successfully create payment intent and return correct response")
        void shouldCreatePaymentIntentSuccessfully() throws StripeException {
            // Given
            Long bookingId = 1L;
            BigDecimal servicePrice = BigDecimal.valueOf(50.00);
            Integer depositPercentage = 50;
            BigDecimal depositAmount = BigDecimal.valueOf(25.00);
            BigDecimal outstandingBalance = BigDecimal.valueOf(25.00);
            String paymentIntentId = "pi_test_123456";
            String clientSecret = "pi_test_123456_secret_abc";

            Booking booking = createBookingWithService(bookingId, servicePrice, depositPercentage);

            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(depositCalculationService.calculateDeposit(servicePrice, depositPercentage))
                    .thenReturn(depositAmount);
            when(depositCalculationService.calculateOutstandingBalance(servicePrice, depositPercentage))
                    .thenReturn(outstandingBalance);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

            // Mock Stripe PaymentIntent static method
            PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
            when(mockPaymentIntent.getId()).thenReturn(paymentIntentId);
            when(mockPaymentIntent.getClientSecret()).thenReturn(clientSecret);

            try (MockedStatic<PaymentIntent> paymentIntentMockedStatic = mockStatic(PaymentIntent.class)) {
                paymentIntentMockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockPaymentIntent);

                // When
                Map<String, Object> response = paymentService.createDepositPaymentIntent(bookingId);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.get("clientSecret")).isEqualTo(clientSecret);
                assertThat(response.get("paymentIntentId")).isEqualTo(paymentIntentId);
                assertThat(response.get("depositAmount")).isEqualTo(depositAmount);
                assertThat(response.get("outstandingBalance")).isEqualTo(outstandingBalance);
                assertThat(response.get("bookingId")).isEqualTo(bookingId);

                // Verify booking was updated with amounts
                ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
                verify(bookingRepository).save(bookingCaptor.capture());
                Booking savedBooking = bookingCaptor.getValue();
                assertThat(savedBooking.getDepositAmount()).isEqualByComparingTo(depositAmount);
                assertThat(savedBooking.getOutstandingBalance()).isEqualByComparingTo(outstandingBalance);
                assertThat(savedBooking.getPaymentStatus()).isEqualTo(Booking.PaymentStatus.DEPOSIT_PENDING);

                // Verify payment record was created
                ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
                verify(paymentRepository).save(paymentCaptor.capture());
                Payment savedPayment = paymentCaptor.getValue();
                assertThat(savedPayment.getBooking()).isEqualTo(booking);
                assertThat(savedPayment.getAmount()).isEqualByComparingTo(depositAmount);
                assertThat(savedPayment.getStripePaymentIntentId()).isEqualTo(paymentIntentId);
                assertThat(savedPayment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
            }
        }
    }

    @Nested
    @DisplayName("handlePaymentSuccess")
    class HandlePaymentSuccessTests {

        @Test
        @DisplayName("Should throw RuntimeException when payment not found")
        void shouldThrowWhenPaymentNotFound() {
            // Given
            String paymentIntentId = "pi_nonexistent_123";
            when(paymentRepository.findByStripePaymentIntentId(paymentIntentId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> paymentService.handlePaymentSuccess(paymentIntentId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Payment not found");
        }

        @Test
        @DisplayName("Should update payment status to SUCCEEDED and booking to CONFIRMED")
        void shouldUpdatePaymentAndBookingOnSuccess() {
            // Given
            String paymentIntentId = "pi_success_123";
            Long bookingId = 1L;

            Booking booking = createBookingWithService(bookingId, BigDecimal.valueOf(50.00), 50);
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PENDING);
            booking.setExpiresAt(LocalDateTime.now().plusMinutes(15));

            Payment payment = new Payment();
            payment.setId(1L);
            payment.setBooking(booking);
            payment.setAmount(BigDecimal.valueOf(25.00));
            payment.setStripePaymentIntentId(paymentIntentId);
            payment.setStatus(Payment.PaymentStatus.PENDING);

            when(paymentRepository.findByStripePaymentIntentId(paymentIntentId))
                    .thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            // When
            paymentService.handlePaymentSuccess(paymentIntentId);

            // Then - Verify payment status updated
            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());
            Payment savedPayment = paymentCaptor.getValue();
            assertThat(savedPayment.getStatus()).isEqualTo(Payment.PaymentStatus.SUCCEEDED);

            // Then - Verify booking status updated
            ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(bookingCaptor.capture());
            Booking savedBooking = bookingCaptor.getValue();
            assertThat(savedBooking.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
            assertThat(savedBooking.getPaymentStatus()).isEqualTo(Booking.PaymentStatus.DEPOSIT_PAID);
            assertThat(savedBooking.getExpiresAt()).isNull();

            // Then - Verify notifications sent
            verify(emailService).sendBookingConfirmation(booking);
            verify(smsService).sendBookingConfirmation(booking);
        }
    }

    // Helper methods

    private Booking createBookingWithService(Long bookingId, BigDecimal servicePrice, Integer depositPercentage) {
        User customer = new User();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");

        User barberUser = new User();
        barberUser.setId(2L);
        barberUser.setFirstName("Mike");
        barberUser.setLastName("Barber");

        Barber barber = new Barber();
        barber.setId(1L);
        barber.setUser(barberUser);

        ServiceOffered service = new ServiceOffered();
        service.setId(1L);
        service.setName("Haircut");
        service.setPrice(servicePrice);
        service.setDepositPercentage(depositPercentage);
        service.setDurationMinutes(30);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(customer);
        booking.setBarber(barber);
        booking.setService(service);
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(10, 30));
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setDepositAmount(BigDecimal.ZERO);
        booking.setOutstandingBalance(BigDecimal.ZERO);

        return booking;
    }
}

