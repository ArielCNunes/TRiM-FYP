package com.trim.booking.service.booking;

import com.trim.booking.config.RlsBypass;
import com.trim.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingCleanupService Unit Tests")
class BookingCleanupServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RlsBypass rlsBypass;

    private BookingCleanupService bookingCleanupService;

    @BeforeEach
    void setUp() {
        bookingCleanupService = new BookingCleanupService(bookingRepository, rlsBypass);

        // Make rlsBypass.executeWithoutRls actually invoke the supplier
        when(rlsBypass.executeWithoutRls(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
    }

    @Test
    @DisplayName("Should cancel expired pending bookings")
    void shouldCancelExpiredPendingBookings() {
        // Given
        when(bookingRepository.cancelAllExpiredPendingBookings()).thenReturn(2);

        // When
        bookingCleanupService.cancelExpiredBookings();

        // Then
        verify(bookingRepository).cancelAllExpiredPendingBookings();
    }

    @Test
    @DisplayName("Should do nothing when no expired bookings")
    void shouldDoNothingWhenNoExpiredBookings() {
        // Given
        when(bookingRepository.cancelAllExpiredPendingBookings()).thenReturn(0);

        // When
        bookingCleanupService.cancelExpiredBookings();

        // Then
        verify(bookingRepository).cancelAllExpiredPendingBookings();
    }

    @Test
    @DisplayName("Should process bulk update and return cancelled count")
    void shouldProcessBulkUpdateAndReturnCancelledCount() {
        // Given
        when(bookingRepository.cancelAllExpiredPendingBookings()).thenReturn(3);

        // When
        bookingCleanupService.cancelExpiredBookings();

        // Then
        verify(bookingRepository, times(1)).cancelAllExpiredPendingBookings();
        verify(rlsBypass).executeWithoutRls(any());
    }
}
