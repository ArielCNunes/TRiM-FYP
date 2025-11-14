package com.trim.booking.controller;

import com.trim.booking.dto.dashboard.DashboardStats;
import com.trim.booking.service.dashboard.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Get admin dashboard statistics.
     * <p>
     * GET /api/dashboard/admin
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStats> getAdminDashboard() {
        DashboardStats stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }
}