package com.trim.booking.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardStats {

    private Long totalBookings;
    private Long todaysBookings;
    private Long upcomingBookings;
    private BigDecimal totalRevenue;
    private BigDecimal thisMonthRevenue;
    private Long activeCustomers;
    private Long activeBarbers;
    private List<Map<String, Object>> popularServices;
    private List<Map<String, Object>> recentBookings;

    // Constructor
    public DashboardStats() {
    }

    // Getters and Setters
    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public Long getTodaysBookings() {
        return todaysBookings;
    }

    public void setTodaysBookings(Long todaysBookings) {
        this.todaysBookings = todaysBookings;
    }

    public Long getUpcomingBookings() {
        return upcomingBookings;
    }

    public void setUpcomingBookings(Long upcomingBookings) {
        this.upcomingBookings = upcomingBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getThisMonthRevenue() {
        return thisMonthRevenue;
    }

    public void setThisMonthRevenue(BigDecimal thisMonthRevenue) {
        this.thisMonthRevenue = thisMonthRevenue;
    }

    public Long getActiveCustomers() {
        return activeCustomers;
    }

    public void setActiveCustomers(Long activeCustomers) {
        this.activeCustomers = activeCustomers;
    }

    public Long getActiveBarbers() {
        return activeBarbers;
    }

    public void setActiveBarbers(Long activeBarbers) {
        this.activeBarbers = activeBarbers;
    }

    public List<Map<String, Object>> getPopularServices() {
        return popularServices;
    }

    public void setPopularServices(List<Map<String, Object>> popularServices) {
        this.popularServices = popularServices;
    }

    public List<Map<String, Object>> getRecentBookings() {
        return recentBookings;
    }

    public void setRecentBookings(List<Map<String, Object>> recentBookings) {
        this.recentBookings = recentBookings;
    }
}