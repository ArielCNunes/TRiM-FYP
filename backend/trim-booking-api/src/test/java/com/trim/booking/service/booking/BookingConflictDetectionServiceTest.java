package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.exception.ConflictException;
import com.trim.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingConflictDetectionService Unit Tests")
class BookingConflictDetectionServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingConflictDetectionService conflictDetectionService;

    private static final Long BARBER_ID = 1L;
    private static final LocalDate BOOKING_DATE = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        conflictDetectionService = new BookingConflictDetectionService(bookingRepository);
    }

    @Nested
    @DisplayName("validateTimeSlotAvailable")
    class ValidateTimeSlotAvailableTests {

        @Test
        @DisplayName("Should pass when no existing bookings")
        void shouldPassWhenNoExistingBookings() {
            // Given
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(Collections.emptyList());

            // When/Then - should not throw
            conflictDetectionService.validateTimeSlotAvailable(BARBER_ID, BOOKING_DATE, startTime, endTime);
        }

        @Test
        @DisplayName("Should pass when existing booking does not overlap")
        void shouldPassWhenNoOverlap() {
            // Given
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);

            Booking existingBooking = createBooking(1L, LocalTime.of(11, 0), LocalTime.of(11, 30),
                    Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(existingBooking));

            // When/Then - should not throw
            conflictDetectionService.validateTimeSlotAvailable(BARBER_ID, BOOKING_DATE, startTime, endTime);
        }

        @Test
        @DisplayName("Should pass when existing booking is cancelled")
        void shouldPassWhenExistingBookingIsCancelled() {
            // Given
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);

            Booking cancelledBooking = createBooking(1L, LocalTime.of(10, 0), LocalTime.of(10, 30),
                    Booking.BookingStatus.CANCELLED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(cancelledBooking));

            // When/Then - should not throw
            conflictDetectionService.validateTimeSlotAvailable(BARBER_ID, BOOKING_DATE, startTime, endTime);
        }

        @Test
        @DisplayName("Should throw ConflictException when time slots overlap")
        void shouldThrowWhenTimeSlotsOverlap() {
            // Given
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);

            Booking existingBooking = createBooking(1L, LocalTime.of(10, 15), LocalTime.of(10, 45),
                    Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(existingBooking));

            // When/Then
            assertThatThrownBy(() ->
                    conflictDetectionService.validateTimeSlotAvailable(BARBER_ID, BOOKING_DATE, startTime, endTime))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("This time slot is no longer available");
        }

        @Test
        @DisplayName("Should throw when new booking starts during existing booking")
        void shouldThrowWhenNewBookingStartsDuringExisting() {
            // Given - new booking 10:15-10:45, existing 10:00-10:30
            LocalTime startTime = LocalTime.of(10, 15);
            LocalTime endTime = LocalTime.of(10, 45);

            Booking existingBooking = createBooking(1L, LocalTime.of(10, 0), LocalTime.of(10, 30),
                    Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(existingBooking));

            // When/Then
            assertThatThrownBy(() ->
                    conflictDetectionService.validateTimeSlotAvailable(BARBER_ID, BOOKING_DATE, startTime, endTime))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Should throw when new booking contains existing booking")
        void shouldThrowWhenNewBookingContainsExisting() {
            // Given - new booking 9:00-12:00, existing 10:00-11:00
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(12, 0);

            Booking existingBooking = createBooking(1L, LocalTime.of(10, 0), LocalTime.of(11, 0),
                    Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(existingBooking));

            // When/Then
            assertThatThrownBy(() ->
                    conflictDetectionService.validateTimeSlotAvailable(BARBER_ID, BOOKING_DATE, startTime, endTime))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Should pass when bookings are adjacent (no gap)")
        void shouldPassWhenBookingsAreAdjacent() {
            // Given - new booking 10:30-11:00, existing 10:00-10:30
            LocalTime startTime = LocalTime.of(10, 30);
            LocalTime endTime = LocalTime.of(11, 0);

            Booking existingBooking = createBooking(1L, LocalTime.of(10, 0), LocalTime.of(10, 30),
                    Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(existingBooking));

            // When/Then - should not throw (adjacent slots don't overlap)
            conflictDetectionService.validateTimeSlotAvailable(BARBER_ID, BOOKING_DATE, startTime, endTime);
        }
    }

    @Nested
    @DisplayName("validateTimeSlotAvailableForUpdate")
    class ValidateTimeSlotAvailableForUpdateTests {

        @Test
        @DisplayName("Should pass when updating same booking to same time")
        void shouldPassWhenUpdatingSameBookingToSameTime() {
            // Given
            Long bookingIdToExclude = 1L;
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);

            Booking existingBooking = createBooking(bookingIdToExclude, startTime, endTime,
                    Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(existingBooking));

            // When/Then - should not throw (excluding self)
            conflictDetectionService.validateTimeSlotAvailableForUpdate(
                    bookingIdToExclude, BARBER_ID, BOOKING_DATE, startTime, endTime);
        }

        @Test
        @DisplayName("Should throw when another booking conflicts")
        void shouldThrowWhenAnotherBookingConflicts() {
            // Given
            Long bookingIdToExclude = 1L;
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);

            Booking otherBooking = createBooking(2L, LocalTime.of(10, 15), LocalTime.of(10, 45),
                    Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(otherBooking));

            // When/Then
            assertThatThrownBy(() ->
                    conflictDetectionService.validateTimeSlotAvailableForUpdate(
                            bookingIdToExclude, BARBER_ID, BOOKING_DATE, startTime, endTime))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("This time slot is no longer available");
        }

        @Test
        @DisplayName("Should pass when conflicting booking is cancelled")
        void shouldPassWhenConflictingBookingIsCancelled() {
            // Given
            Long bookingIdToExclude = 1L;
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);

            Booking cancelledBooking = createBooking(2L, LocalTime.of(10, 0), LocalTime.of(10, 30),
                    Booking.BookingStatus.CANCELLED);
            when(bookingRepository.findByBarberIdAndBookingDateWithLock(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(cancelledBooking));

            // When/Then - should not throw
            conflictDetectionService.validateTimeSlotAvailableForUpdate(
                    bookingIdToExclude, BARBER_ID, BOOKING_DATE, startTime, endTime);
        }
    }

    @Nested
    @DisplayName("findConflictingBookings")
    class FindConflictingBookingsTests {

        @Test
        @DisplayName("Should return empty list when no conflicts")
        void shouldReturnEmptyListWhenNoConflicts() {
            // Given
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);
            when(bookingRepository.findByBarberIdAndBookingDate(BARBER_ID, BOOKING_DATE))
                    .thenReturn(Collections.emptyList());

            // When
            List<Booking> conflicts = conflictDetectionService.findConflictingBookings(
                    BARBER_ID, BOOKING_DATE, startTime, endTime);

            // Then
            assertThat(conflicts).isEmpty();
        }

        @Test
        @DisplayName("Should return conflicting bookings")
        void shouldReturnConflictingBookings() {
            // Given
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(11, 0);

            Booking conflicting = createBooking(1L, LocalTime.of(10, 30), LocalTime.of(11, 30),
                    Booking.BookingStatus.CONFIRMED);
            Booking nonConflicting = createBooking(2L, LocalTime.of(12, 0), LocalTime.of(12, 30),
                    Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByBarberIdAndBookingDate(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(conflicting, nonConflicting));

            // When
            List<Booking> conflicts = conflictDetectionService.findConflictingBookings(
                    BARBER_ID, BOOKING_DATE, startTime, endTime);

            // Then
            assertThat(conflicts).hasSize(1);
            assertThat(conflicts.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should exclude cancelled bookings from conflicts")
        void shouldExcludeCancelledBookingsFromConflicts() {
            // Given
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(10, 30);

            Booking cancelledBooking = createBooking(1L, LocalTime.of(10, 0), LocalTime.of(10, 30),
                    Booking.BookingStatus.CANCELLED);
            when(bookingRepository.findByBarberIdAndBookingDate(BARBER_ID, BOOKING_DATE))
                    .thenReturn(List.of(cancelledBooking));

            // When
            List<Booking> conflicts = conflictDetectionService.findConflictingBookings(
                    BARBER_ID, BOOKING_DATE, startTime, endTime);

            // Then
            assertThat(conflicts).isEmpty();
        }
    }

    // Helper method to create booking
    private Booking createBooking(Long id, LocalTime startTime, LocalTime endTime,
                                  Booking.BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(status);
        booking.setBookingDate(BOOKING_DATE);
        return booking;
    }
}

