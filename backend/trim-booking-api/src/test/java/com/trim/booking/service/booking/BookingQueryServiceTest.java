package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingQueryService Unit Tests")
class BookingQueryServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingQueryService bookingQueryService;

    @BeforeEach
    void setUp() {
        bookingQueryService = new BookingQueryService(bookingRepository);
    }

    @Nested
    @DisplayName("getBookingById")
    class GetBookingByIdTests {

        @Test
        @DisplayName("Should return booking when found")
        void shouldReturnBookingWhenFound() {
            // Given
            Long bookingId = 1L;
            Booking booking = createBooking(bookingId, LocalDate.now().plusDays(1));
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            // When
            Booking result = bookingQueryService.getBookingById(bookingId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(bookingId);
        }

        @Test
        @DisplayName("Should throw when booking not found")
        void shouldThrowWhenBookingNotFound() {
            // Given
            Long bookingId = 999L;
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingQueryService.getBookingById(bookingId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Booking not found with id: " + bookingId);
        }
    }

    @Nested
    @DisplayName("getCustomerBookings")
    class GetCustomerBookingsTests {

        @Test
        @DisplayName("Should return customer bookings")
        void shouldReturnCustomerBookings() {
            // Given
            Long customerId = 1L;
            List<Booking> bookings = List.of(
                    createBooking(1L, LocalDate.now().plusDays(1)),
                    createBooking(2L, LocalDate.now().plusDays(2))
            );
            when(bookingRepository.findByCustomerId(customerId)).thenReturn(bookings);

            // When
            List<Booking> result = bookingQueryService.getCustomerBookings(customerId);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no bookings")
        void shouldReturnEmptyListWhenNoBookings() {
            // Given
            Long customerId = 1L;
            when(bookingRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

            // When
            List<Booking> result = bookingQueryService.getCustomerBookings(customerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBarberBookings")
    class GetBarberBookingsTests {

        @Test
        @DisplayName("Should return barber bookings")
        void shouldReturnBarberBookings() {
            // Given
            Long barberId = 1L;
            List<Booking> bookings = List.of(createBooking(1L, LocalDate.now().plusDays(1)));
            when(bookingRepository.findByBarberId(barberId)).thenReturn(bookings);

            // When
            List<Booking> result = bookingQueryService.getBarberBookings(barberId);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getBarberScheduleForDate")
    class GetBarberScheduleForDateTests {

        @Test
        @DisplayName("Should return barber schedule for date")
        void shouldReturnBarberScheduleForDate() {
            // Given
            Long barberId = 1L;
            LocalDate date = LocalDate.now().plusDays(1);
            List<Booking> bookings = List.of(createBooking(1L, date));
            when(bookingRepository.findByBarberIdAndBookingDate(barberId, date)).thenReturn(bookings);

            // When
            List<Booking> result = bookingQueryService.getBarberScheduleForDate(barberId, date);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getBookingDate()).isEqualTo(date);
        }
    }

    @Nested
    @DisplayName("getUpcomingBookingsForCustomer")
    class GetUpcomingBookingsForCustomerTests {

        @Test
        @DisplayName("Should return only future and today's non-cancelled bookings")
        void shouldReturnOnlyFutureAndTodayNonCancelledBookings() {
            // Given
            Long customerId = 1L;
            LocalDate today = LocalDate.now();

            Booking futureBooking = createBooking(1L, today.plusDays(1));
            futureBooking.setStatus(Booking.BookingStatus.CONFIRMED);

            Booking todayBooking = createBooking(2L, today);
            todayBooking.setStatus(Booking.BookingStatus.CONFIRMED);

            Booking pastBooking = createBooking(3L, today.minusDays(1));
            pastBooking.setStatus(Booking.BookingStatus.COMPLETED);

            Booking cancelledBooking = createBooking(4L, today.plusDays(2));
            cancelledBooking.setStatus(Booking.BookingStatus.CANCELLED);

            when(bookingRepository.findByCustomerId(customerId))
                    .thenReturn(List.of(futureBooking, todayBooking, pastBooking, cancelledBooking));

            // When
            List<Booking> result = bookingQueryService.getUpcomingBookingsForCustomer(customerId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Booking::getId).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("Should return empty list when no upcoming bookings")
        void shouldReturnEmptyListWhenNoUpcomingBookings() {
            // Given
            Long customerId = 1L;
            when(bookingRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

            // When
            List<Booking> result = bookingQueryService.getUpcomingBookingsForCustomer(customerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPastBookingsForCustomer")
    class GetPastBookingsForCustomerTests {

        @Test
        @DisplayName("Should return only past bookings")
        void shouldReturnOnlyPastBookings() {
            // Given
            Long customerId = 1L;
            LocalDate today = LocalDate.now();

            Booking pastBooking = createBooking(1L, today.minusDays(1));
            Booking futureBooking = createBooking(2L, today.plusDays(1));
            Booking todayBooking = createBooking(3L, today);

            when(bookingRepository.findByCustomerId(customerId))
                    .thenReturn(List.of(pastBooking, futureBooking, todayBooking));

            // When
            List<Booking> result = bookingQueryService.getPastBookingsForCustomer(customerId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("getAllBookings")
    class GetAllBookingsTests {

        @Test
        @DisplayName("Should filter by date and status")
        void shouldFilterByDateAndStatus() {
            // Given
            LocalDate date = LocalDate.now().plusDays(1);
            Booking.BookingStatus status = Booking.BookingStatus.CONFIRMED;
            List<Booking> bookings = List.of(createBooking(1L, date));
            when(bookingRepository.findByBookingDateAndStatus(date, status)).thenReturn(bookings);

            // When
            List<Booking> result = bookingQueryService.getAllBookings(status, date);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by date only")
        void shouldFilterByDateOnly() {
            // Given
            LocalDate date = LocalDate.now().plusDays(1);
            List<Booking> bookings = List.of(createBooking(1L, date));
            when(bookingRepository.findByBookingDate(date)).thenReturn(bookings);

            // When
            List<Booking> result = bookingQueryService.getAllBookings(null, date);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by status only")
        void shouldFilterByStatusOnly() {
            // Given
            Booking.BookingStatus status = Booking.BookingStatus.CONFIRMED;
            List<Booking> bookings = List.of(createBooking(1L, LocalDate.now()));
            when(bookingRepository.findByStatus(status)).thenReturn(bookings);

            // When
            List<Booking> result = bookingQueryService.getAllBookings(status, null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return all bookings when no filters")
        void shouldReturnAllBookingsWhenNoFilters() {
            // Given
            List<Booking> bookings = List.of(
                    createBooking(1L, LocalDate.now()),
                    createBooking(2L, LocalDate.now().plusDays(1))
            );
            when(bookingRepository.findAll()).thenReturn(bookings);

            // When
            List<Booking> result = bookingQueryService.getAllBookings(null, null);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    // Helper method
    private Booking createBooking(Long id, LocalDate date) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setBookingDate(date);
        booking.setStatus(Booking.BookingStatus.PENDING);
        return booking;
    }
}

