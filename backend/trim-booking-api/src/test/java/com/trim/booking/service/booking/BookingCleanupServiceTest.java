package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingCleanupService Unit Tests")
class BookingCleanupServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;

    private BookingCleanupService bookingCleanupService;

    @BeforeEach
    void setUp() {
        bookingCleanupService = new BookingCleanupService(bookingRepository);
    }

    @Test
    @DisplayName("Should cancel expired pending bookings")
    void shouldCancelExpiredPendingBookings() {
        // Given
        Booking expiredBooking1 = createPendingBooking(1L);
        Booking expiredBooking2 = createPendingBooking(2L);
        when(bookingRepository.findExpiredPendingBookings())
                .thenReturn(List.of(expiredBooking1, expiredBooking2));

        // When
        bookingCleanupService.cancelExpiredBookings();

        // Then
        verify(bookingRepository, times(2)).save(bookingCaptor.capture());
        List<Booking> savedBookings = bookingCaptor.getAllValues();

        assertThat(savedBookings).hasSize(2);
        assertThat(savedBookings).allMatch(b -> b.getStatus() == Booking.BookingStatus.CANCELLED);
        assertThat(savedBookings).allMatch(b -> b.getPaymentStatus() == Booking.PaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should do nothing when no expired bookings")
    void shouldDoNothingWhenNoExpiredBookings() {
        // Given
        when(bookingRepository.findExpiredPendingBookings()).thenReturn(Collections.emptyList());

        // When
        bookingCleanupService.cancelExpiredBookings();

        // Then
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should set correct status and payment status")
    void shouldSetCorrectStatusAndPaymentStatus() {
        // Given
        Booking expiredBooking = createPendingBooking(1L);
        when(bookingRepository.findExpiredPendingBookings()).thenReturn(List.of(expiredBooking));

        // When
        bookingCleanupService.cancelExpiredBookings();

        // Then
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();

        assertThat(savedBooking.getStatus()).isEqualTo(Booking.BookingStatus.CANCELLED);
        assertThat(savedBooking.getPaymentStatus()).isEqualTo(Booking.PaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should process multiple expired bookings individually")
    void shouldProcessMultipleExpiredBookingsIndividually() {
        // Given
        Booking booking1 = createPendingBooking(1L);
        Booking booking2 = createPendingBooking(2L);
        Booking booking3 = createPendingBooking(3L);
        when(bookingRepository.findExpiredPendingBookings())
                .thenReturn(List.of(booking1, booking2, booking3));

        // When
        bookingCleanupService.cancelExpiredBookings();

        // Then
        verify(bookingRepository, times(3)).save(any(Booking.class));
    }

    // Helper method
    private Booking createPendingBooking(Long id) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        return booking;
    }
}

