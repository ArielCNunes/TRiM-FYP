package com.trim.booking.controller;

import com.trim.booking.repository.BusinessRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessRepository businessRepository;

    public BusinessController(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    /**
     * Public endpoint to check if a business exists by slug.
     * Used by the frontend to validate subdomains before rendering.
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkBusiness(@RequestParam String slug) {
        return businessRepository.findBySlug(slug)
                .map(business -> ResponseEntity.ok(Map.<String, Object>of(
                        "exists", true,
                        "name", business.getName()
                )))
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "exists", false
                )));
    }
}
