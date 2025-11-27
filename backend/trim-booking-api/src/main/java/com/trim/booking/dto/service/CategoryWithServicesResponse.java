package com.trim.booking.dto.service;

import com.trim.booking.entity.ServiceCategory;
import com.trim.booking.entity.ServiceOffered;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryWithServicesResponse {
    private Long id;
    private String name;
    private List<ServiceSummary> services;

    public CategoryWithServicesResponse(ServiceCategory category) {
        this.id = category.getId();
        this.name = category.getName();
        this.services = category.getServices().stream()
                .map(ServiceSummary::new)
                .collect(Collectors.toList());
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public List<ServiceSummary> getServices() { return services; }

    // Nested class for service summary
    public static class ServiceSummary {
        private Long id;
        private String name;
        private String description;
        private Integer durationMinutes;
        private BigDecimal price;
        private Integer depositPercentage;
        private Boolean active;

        public ServiceSummary(ServiceOffered service) {
            this.id = service.getId();
            this.name = service.getName();
            this.description = service.getDescription();
            this.durationMinutes = service.getDurationMinutes();
            this.price = service.getPrice();
            this.depositPercentage = service.getDepositPercentage();
            this.active = service.getActive();
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public BigDecimal getPrice() { return price; }
        public Integer getDepositPercentage() { return depositPercentage; }
        public Boolean getActive() { return active; }
    }
}

