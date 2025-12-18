package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingStatusService Unit Tests")
class BookingStatusServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingStatusService bookingStatusService;

    @BeforeEach
    void setUp() {
        bookingStatusService = new BookingStatusService(bookingRepository);
    }

    @Nested
    @DisplayName("markAsCompleted")
    class MarkAsCompletedTests {

        @Test
        @DisplayName("Should mark confirmed booking as completed")
        void shouldMarkConfirmedBookingAsCompleted() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Booking result = bookingStatusService.markAsCompleted(bookingId);

            // Then
            assertThat(result.getStatus()).isEqualTo(Booking.BookingStatus.COMPLETED);
            assertThat(result.getPaymentStatus()).isEqualTo(Booking.PaymentStatus.FULLY_PAID);
            assertThat(result.getOutstandingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw when booking not found")
        void shouldThrowWhenBookingNotFound() {
            // Given
            Long bookingId = 999L;
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.markAsCompleted(bookingId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Booking not found with id: " + bookingId);
        }

        @Test
        @DisplayName("Should throw when booking is cancelled")
        void shouldThrowWhenBookingIsCancelled() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CANCELLED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.markAsCompleted(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot complete a cancelled booking");
        }

        @Test
        @DisplayName("Should throw when booking is already completed")
        void shouldThrowWhenBookingAlreadyCompleted() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.COMPLETED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.markAsCompleted(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking is already completed");
        }
    }

    @Nested
    @DisplayName("markAsPaid")
    class MarkAsPaidTests {

        @Test
        @DisplayName("Should mark booking as fully paid")
        void shouldMarkBookingAsFullyPaid() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CONFIRMED);
            booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Booking result = bookingStatusService.markAsPaid(bookingId);

            // Then
            assertThat(result.getPaymentStatus()).isEqualTo(Booking.PaymentStatus.FULLY_PAID);
            assertThat(result.getOutstandingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw when booking is cancelled")
        void shouldThrowWhenBookingIsCancelled() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CANCELLED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.markAsPaid(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot mark cancelled booking as paid");
        }

        @Test
        @DisplayName("Should throw when booking is already fully paid")
        void shouldThrowWhenAlreadyFullyPaid() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CONFIRMED);
            booking.setPaymentStatus(Booking.PaymentStatus.FULLY_PAID);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.markAsPaid(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking is already fully paid");
        }
    }

    @Nested
    @DisplayName("markAsNoShow")
    class MarkAsNoShowTests {

        @Test
        @DisplayName("Should mark booking as no-show")
        void shouldMarkBookingAsNoShow() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Booking result = bookingStatusService.markAsNoShow(bookingId);

            // Then
            assertThat(result.getStatus()).isEqualTo(Booking.BookingStatus.NO_SHOW);
        }

        @Test
        @DisplayName("Should throw when booking is cancelled")
        void shouldThrowWhenBookingIsCancelled() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CANCELLED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.markAsNoShow(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot mark cancelled booking as no-show");
        }

        @Test
        @DisplayName("Should throw when booking is completed")
        void shouldThrowWhenBookingIsCompleted() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.COMPLETED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.markAsNoShow(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot mark completed booking as no-show");
        }
    }

    @Nested
    @DisplayName("cancelBooking")
    class CancelBookingTests {

        @Test
        @DisplayName("Should cancel confirmed booking")
        void shouldCancelConfirmedBooking() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Booking result = bookingStatusService.cancelBooking(bookingId);

            // Then
            assertThat(result.getStatus()).isEqualTo(Booking.BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should cancel pending booking")
        void shouldCancelPendingBooking() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.PENDING);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Booking result = bookingStatusService.cancelBooking(bookingId);

            // Then
            assertThat(result.getStatus()).isEqualTo(Booking.BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw when booking is already cancelled")
        void shouldThrowWhenAlreadyCancelled() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.CANCELLED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.cancelBooking(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking is already cancelled");
        }

        @Test
        @DisplayName("Should throw when booking is completed")
        void shouldThrowWhenBookingIsCompleted() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.COMPLETED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.cancelBooking(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot cancel a completed booking");
        }
    }

    @Nested
    @DisplayName("confirmBooking")
    class ConfirmBookingTests {

        @Test
        @DisplayName("Should confirm pending booking")
        void shouldConfirmPendingBooking() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBookingWithService(bookingId, Booking.BookingStatus.PENDING);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Booking result = bookingStatusService.confirmBooking(bookingId);

            // Then
            assertThat(result.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
            assertThat(result.getPaymentStatus()).isEqualTo(Booking.PaymentStatus.DEPOSIT_PAID);
        }

        @Test
        @DisplayName("Should throw when booking not found")
        void shouldThrowWhenBookingNotFound() {
            // Given
            Long bookingId = 999L;
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingStatusService.confirmBooking(bookingId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Booking not found");
        }
    }

    // Helper method to create booking with service
    private Booking createBookingWithService(Long id, Booking.BookingStatus status) {
        ServiceOffered service = new ServiceOffered();
        service.setId(1L);
        service.setPrice(new BigDecimal("25.00"));

        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(status);
        booking.setService(service);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        return booking;
    }
}

