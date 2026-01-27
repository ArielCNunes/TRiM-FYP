package com.trim.booking.service.dashboard;

import com.trim.booking.dto.dashboard.DashboardStats;
import com.trim.booking.entity.Booking;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.UserRepository;
import com.trim.booking.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;

    public DashboardService(BookingRepository bookingRepository, UserRepository userRepository, BarberRepository barberRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.barberRepository = barberRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    public DashboardStats getAdminDashboardStats() {
        DashboardStats stats = new DashboardStats();
        Long businessId = getBusinessId();

        // Basic counts
        stats.setTotalBookings((long) bookingRepository.findByBusinessIdAndBookingDate(businessId, LocalDate.now()).size());
        stats.setTodaysBookings(bookingRepository.countTodaysBookingsByBusinessId(businessId));
        stats.setUpcomingBookings(bookingRepository.countUpcomingBookingsByBusinessId(businessId));
        stats.setActiveCustomers(userRepository.countByBusinessIdAndRole(businessId));
        stats.setActiveBarbers(barberRepository.countByBusinessIdAndActiveTrue(businessId));

        // Revenue calculations
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenueByBusinessId(businessId);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal monthRevenue = bookingRepository.calculateThisMonthRevenueByBusinessId(businessId);
        stats.setThisMonthRevenue(monthRevenue != null ? monthRevenue : BigDecimal.ZERO);

        // Popular services
        List<Object[]> popularServicesData = bookingRepository.findPopularServicesByBusinessId(businessId);
        List<Map<String, Object>> popularServices = popularServicesData.stream()
                .limit(5)
                .map(row -> {
                    Map<String, Object> service = new HashMap<>();
                    service.put("name", row[0]);
                    service.put("count", row[1]);
                    return service;
                })
                .collect(Collectors.toList());
        stats.setPopularServices(popularServices);

        // Recent bookings (last 5 by creation date)
        List<Booking> recentBookingsList = bookingRepository.findRecentByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream()
                .limit(5)
                .toList();

        List<Map<String, Object>> recentBookings = recentBookingsList.stream()
                .map(booking -> {
                    Map<String, Object> b = new HashMap<>();
                    b.put("id", booking.getId());
                    b.put("customerName", booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName());
                    b.put("serviceName", booking.getService().getName());
                    b.put("barberName", booking.getBarber().getUser().getFirstName() + " " + booking.getBarber().getUser().getLastName());
                    b.put("date", booking.getBookingDate().toString());
                    b.put("time", booking.getStartTime().toString());
                    b.put("status", booking.getStatus().toString());
                    return b;
                })
                .collect(Collectors.toList());
        stats.setRecentBookings(recentBookings);

        return stats;
    }
}