package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.exception.ConflictException;
import com.trim.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service responsible for detecting booking conflicts.
 * Single responsibility: conflict detection logic.
 */
@Service
public class BookingConflictDetectionService {
    private final BookingRepository bookingRepository;

    public BookingConflictDetectionService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Check if the time slot is available for booking.
     * Uses pessimistic locking to prevent race conditions.
     *
     * @param barberId    Barber ID
     * @param bookingDate Booking date
     * @param startTime   Start time
     * @param endTime     End time
     * @throws ConflictException if slot is taken
     */
    public void validateTimeSlotAvailable(Long barberId, LocalDate bookingDate,
                                          LocalTime startTime, LocalTime endTime) {
        List<Booking> existingBookings = bookingRepository
                .findByBarberIdAndBookingDateWithLock(barberId, bookingDate);

        for (Booking existing : existingBookings) {
            // Skip cancelled bookings
            if (existing.getStatus() == Booking.BookingStatus.CANCELLED) {
                continue;
            }

            // Skip expired pending bookings
            if (existing.isExpired()) {
                continue;
            }

            // Check for time overlap
            if (hasTimeOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                throw new ConflictException("This time slot is no longer available");
            }
        }
    }

    /**
     * Check if two time slots overlap.
     *
     * @param start1 Start time of first slot
     * @param end1   End time of first slot
     * @param start2 Start time of second slot
     * @param end2   End time of second slot
     * @return true if there is overlap
     */
    private boolean hasTimeOverlap(LocalTime start1, LocalTime end1,
                                   LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Get all conflicting bookings (without throwing exception).
     * Useful for UI to show busy slots.
     *
     * @param barberId    Barber ID
     * @param bookingDate Booking date
     * @param startTime   Start time
     * @param endTime     End time
     * @return List of conflicting bookings
     */
    public List<Booking> findConflictingBookings(Long barberId, LocalDate bookingDate,
                                                 LocalTime startTime, LocalTime endTime) {
        List<Booking> existingBookings = bookingRepository
                .findByBarberIdAndBookingDate(barberId, bookingDate);

        return existingBookings.stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .filter(b -> hasTimeOverlap(startTime, endTime, b.getStartTime(), b.getEndTime()))
                .toList();
    }
}

