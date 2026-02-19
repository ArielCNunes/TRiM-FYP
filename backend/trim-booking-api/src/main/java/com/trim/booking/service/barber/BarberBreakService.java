package com.trim.booking.service.barber;

import com.trim.booking.entity.Barber;
import com.trim.booking.entity.BarberBreak;
import com.trim.booking.entity.Business;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberBreakRepository;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class BarberBreakService {
    private final BarberBreakRepository barberBreakRepository;
    private final BarberRepository barberRepository;
    private final BusinessRepository businessRepository;

    public BarberBreakService(BarberBreakRepository barberBreakRepository,
                             BarberRepository barberRepository,
                             BusinessRepository businessRepository) {
        this.barberBreakRepository = barberBreakRepository;
        this.barberRepository = barberRepository;
        this.businessRepository = businessRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    /**
     * Get all breaks for a specific barber.
     *
     * @param barberId The ID of the barber
     * @return List of all breaks for the barber
     */
    public List<BarberBreak> getBreaksByBarberId(Long barberId) {
        return barberBreakRepository.findByBusinessIdAndBarberId(getBusinessId(), barberId);
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
        Long businessId = getBusinessId();

        // Find barber with tenant isolation
        Barber barber = barberRepository.findByIdAndBusinessId(barberId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + barberId));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        // Parse times
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        // Validate times
        if (!start.isBefore(end)) {
            throw new BadRequestException("Start time must be before end time");
        }

        // Create and save break
        BarberBreak barberBreak = new BarberBreak(barber, start, end, label);
        barberBreak.setBusiness(business);
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
        // Find break with tenant isolation
        BarberBreak barberBreak = barberBreakRepository.findByIdAndBusinessId(breakId, getBusinessId())
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
        int deleted = barberBreakRepository.deleteByIdAndBusinessId(breakId, getBusinessId());
        if (deleted == 0) {
            throw new ResourceNotFoundException("Break not found with id: " + breakId);
        }
    }
}

