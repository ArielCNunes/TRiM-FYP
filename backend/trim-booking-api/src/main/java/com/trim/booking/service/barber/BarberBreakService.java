package com.trim.booking.service.barber;

import com.trim.booking.entity.Barber;
import com.trim.booking.entity.BarberBreak;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberBreakRepository;
import com.trim.booking.repository.BarberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class BarberBreakService {
    private final BarberBreakRepository barberBreakRepository;
    private final BarberRepository barberRepository;

    public BarberBreakService(BarberBreakRepository barberBreakRepository,
                             BarberRepository barberRepository) {
        this.barberBreakRepository = barberBreakRepository;
        this.barberRepository = barberRepository;
    }

    /**
     * Get all breaks for a specific barber.
     *
     * @param barberId The ID of the barber
     * @return List of all breaks for the barber
     */
    public List<BarberBreak> getBreaksByBarberId(Long barberId) {
        return barberBreakRepository.findByBarberId(barberId);
    }

    /**
     * Create a new break for a barber.
     *
     * @param barberId The ID of the barber
     * @param startTime The start time as a string in "HH:mm" format
     * @param endTime The end time as a string in "HH:mm" format
     * @param label Optional label for the break (e.g., "Lunch", "Coffee")
     * @return The created BarberBreak entity
     * @throws ResourceNotFoundException If barber is not found
     * @throws BadRequestException If startTime is not before endTime
     */
    @Transactional
    public BarberBreak createBreak(Long barberId, String startTime, String endTime, String label) {
        // Find barber
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + barberId));

        // Parse times
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        // Validate times
        if (!start.isBefore(end)) {
            throw new BadRequestException("Start time must be before end time");
        }

        // Create and save break
        BarberBreak barberBreak = new BarberBreak(barber, start, end, label);
        return barberBreakRepository.save(barberBreak);
    }

    /**
     * Update an existing break.
     *
     * @param breakId The ID of the break to update
     * @param startTime The new start time as a string in "HH:mm" format
     * @param endTime The new end time as a string in "HH:mm" format
     * @param label The new label for the break
     * @return The updated BarberBreak entity
     * @throws ResourceNotFoundException If break is not found
     * @throws BadRequestException If startTime is not before endTime
     */
    @Transactional
    public BarberBreak updateBreak(Long breakId, String startTime, String endTime, String label) {
        // Find break
        BarberBreak barberBreak = barberBreakRepository.findById(breakId)
                .orElseThrow(() -> new ResourceNotFoundException("Break not found with id: " + breakId));

        // Parse times
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        // Validate times
        if (!start.isBefore(end)) {
            throw new BadRequestException("Start time must be before end time");
        }

        // Update break
        barberBreak.setStartTime(start);
        barberBreak.setEndTime(end);
        barberBreak.setLabel(label);

        return barberBreakRepository.save(barberBreak);
    }

    /**
     * Delete a break.
     *
     * @param breakId The ID of the break to delete
     * @throws ResourceNotFoundException If break is not found
     */
    @Transactional
    public void deleteBreak(Long breakId) {
        if (!barberBreakRepository.existsById(breakId)) {
            throw new ResourceNotFoundException("Break not found with id: " + breakId);
        }
        barberBreakRepository.deleteById(breakId);
    }
}

