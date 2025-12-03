package com.trim.booking.service.barber;

import com.trim.booking.entity.BarberAvailability;
import com.trim.booking.entity.BarberBreak;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberAvailabilityRepository;
import com.trim.booking.repository.BarberBreakRepository;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AvailabilityService {
    private final BarberAvailabilityRepository barberAvailabilityRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final BarberBreakRepository barberBreakRepository;

    // Show slots every 15 minutes
    private static final int SLOT_INTERVAL_MINUTES = 15;

    public AvailabilityService(BarberAvailabilityRepository barberAvailabilityRepository,
                               BookingRepository bookingRepository,
                               ServiceRepository serviceRepository,
                               BarberBreakRepository barberBreakRepository) {
        this.barberAvailabilityRepository = barberAvailabilityRepository;
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.barberBreakRepository = barberBreakRepository;
    }

    /**
     * Get available time slots for a barber on a specific date for a given service.
     * <p>
     * Algorithm:
     * 1. Get barber's working hours for that day of week
     * 2. Get all existing bookings for that barber on that date
     * 3. Get all breaks for the barber
     * 4. Generate time slots at 15-minute intervals
     * 5. Filter out slots that conflict with existing bookings
     * 6. Filter out slots that conflict with barber breaks
     * 7. Filter out slots with insufficient time for the service
     * 8. Filter out past slots if date is today
     *
     * @param barberId  The ID of the barber
     * @param date      The date to check availability
     * @param serviceId The ID of the service being booked
     * @return List of available time slots
     */
    public List<String> getAvailableSlots(Long barberId, LocalDate date, Long serviceId) {
        // Step 1: Get the service to know its duration
        ServiceOffered service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + serviceId));

        // Step 2: Get barber's working hours for this day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<BarberAvailability> availabilityOpt =
                barberAvailabilityRepository.findByBarberIdAndDayOfWeek(barberId, dayOfWeek);

        // If barber doesn't work this day, return empty list
        if (availabilityOpt.isEmpty() || !availabilityOpt.get().getIsAvailable()) {
            return new ArrayList<>();
        }

        BarberAvailability availability = availabilityOpt.get();
        LocalTime workStart = availability.getStartTime();
        LocalTime workEnd = availability.getEndTime();

        // Step 3: Get all existing bookings for this barber on this date
        List<Booking> existingBookings = bookingRepository.findByBarberIdAndBookingDate(barberId, date);

        // Get all breaks for the barber
        List<BarberBreak> barberBreaks = barberBreakRepository.findByBarberId(barberId);

        // Step 4: Generate all possible time slots
        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime currentSlot = workStart;

        while (currentSlot.isBefore(workEnd)) {
            // Calculate when this service would end if started at currentSlot
            LocalTime slotEnd = currentSlot.plusMinutes(service.getDurationMinutes());

            // Check if there's enough time before work ends
            if (slotEnd.isAfter(workEnd)) {
                break; // Not enough time left in the day
            }

            // Check if this slot conflicts with any existing booking
            boolean isSlotAvailable = isSlotAvailable(currentSlot, slotEnd, existingBookings);

            // Check if this slot conflicts with any breaks
            if (isSlotAvailable) {
                isSlotAvailable = isSlotAvailableWithBreaks(currentSlot, slotEnd, barberBreaks);
            }

            // If today, filter out past times
            if (date.equals(LocalDate.now()) && currentSlot.isBefore(LocalTime.now())) {
                isSlotAvailable = false;
            }

            if (isSlotAvailable) {
                availableSlots.add(currentSlot);
            }

            // Move to next slot (e.g., 9:00 -> 9:15 -> 9:30)
            currentSlot = currentSlot.plusMinutes(SLOT_INTERVAL_MINUTES);
        }

        return availableSlots.stream()
                .map(LocalTime::toString)
                .collect(java.util.stream.Collectors.toList());

    }

    /**
     * Check if a proposed time slot conflicts with existing bookings.
     * <p>
     * A conflict occurs if the proposed slot overlaps with any existing booking.
     * Overlap means: proposed start < existing end AND proposed end > existing start
     *
     * @param proposedStart    Start time of the proposed slot
     * @param proposedEnd      End time of the proposed slot
     * @param existingBookings List of bookings already made for this barber/date
     * @return true if slot is available (no conflicts), false if there's a conflict
     */
    private boolean isSlotAvailable(LocalTime proposedStart, LocalTime proposedEnd,
                                    List<Booking> existingBookings) {
        for (Booking booking : existingBookings) {
            // Skip cancelled bookings - they don't block availability
            if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
                continue;
            }

            LocalTime bookingStart = booking.getStartTime();
            LocalTime bookingEnd = booking.getEndTime();

            // Check for overlap
            // Overlap formula: (proposedStart < bookingEnd) AND (proposedEnd > bookingStart)
            boolean hasOverlap = proposedStart.isBefore(bookingEnd) &&
                    proposedEnd.isAfter(bookingStart);

            if (hasOverlap) {
                return false; // Conflict found
            }
        }
        return true; // No conflicts, slot is available
    }

    /**
     * Check if a proposed time slot conflicts with barber breaks.
     * <p>
     * A conflict occurs if the proposed slot overlaps with any break.
     * Overlap means: proposed start < break end AND proposed end > break start
     *
     * @param proposedStart Start time of the proposed slot
     * @param proposedEnd   End time of the proposed slot
     * @param barberBreaks  List of breaks for the barber
     * @return true if slot is available (no conflicts), false if there's a conflict
     */
    private boolean isSlotAvailableWithBreaks(LocalTime proposedStart, LocalTime proposedEnd,
                                              List<BarberBreak> barberBreaks) {
        for (BarberBreak breakPeriod : barberBreaks) {
            LocalTime breakStart = breakPeriod.getStartTime();
            LocalTime breakEnd = breakPeriod.getEndTime();

            // Check for overlap
            // Overlap formula: (proposedStart < breakEnd) AND (proposedEnd > breakStart)
            boolean hasOverlap = proposedStart.isBefore(breakEnd) &&
                    proposedEnd.isAfter(breakStart);

            if (hasOverlap) {
                return false; // Conflict found
            }
        }
        return true; // No conflicts, slot is available
    }
}