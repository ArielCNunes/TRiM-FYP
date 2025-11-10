package com.trim.booking.service.dashboard;

import com.trim.booking.dto.DashboardStats;
import com.trim.booking.entity.Booking;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public DashboardStats getAdminDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Basic counts
        stats.setTotalBookings(bookingRepository.count());
        stats.setTodaysBookings(bookingRepository.countTodaysBookings());
        stats.setUpcomingBookings(bookingRepository.countUpcomingBookings());
        stats.setActiveCustomers(userRepository.countActiveCustomers());
        stats.setActiveBarbers(barberRepository.countActiveBarbers());

        // Revenue calculations
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal monthRevenue = bookingRepository.calculateThisMonthRevenue();
        stats.setThisMonthRevenue(monthRevenue != null ? monthRevenue : BigDecimal.ZERO);

        // Popular services
        List<Object[]> popularServicesData = bookingRepository.findPopularServices();
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

        // Recent bookings (last 5)
        List<Booking> recentBookingsList = bookingRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

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