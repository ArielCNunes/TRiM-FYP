package com.trim.booking.dto.service;

import com.trim.booking.entity.ServiceOffered;

import java.math.BigDecimal;

public class ServiceResponse {

    private Long id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private Integer depositPercentage;
    private Boolean active;
    private Long categoryId;
    private String categoryName;

    public ServiceResponse(ServiceOffered service) {
        this.id = service.getId();
        this.name = service.getName();
        this.description = service.getDescription();
        this.durationMinutes = service.getDurationMinutes();
        this.price = service.getPrice();
        this.depositPercentage = service.getDepositPercentage();
        this.active = service.getActive();
        this.categoryId = service.getCategory().getId();
        this.categoryName = service.getCategory().getName();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public Integer getDepositPercentage() { return depositPercentage; }
    public Boolean getActive() { return active; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
}