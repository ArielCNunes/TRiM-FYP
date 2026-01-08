package com.trim.booking.dto.customer;

import java.util.List;

/**
 * Response DTO for paginated customer list.
 */
public class CustomerListResponse {
    private List<CustomerResponse> customers;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    // Constructors
    public CustomerListResponse() {
    }

    public CustomerListResponse(List<CustomerResponse> customers, int page, int size,
                                long totalElements, int totalPages) {
        this.customers = customers;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // Getters and Setters
    public List<CustomerResponse> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerResponse> customers) {
        this.customers = customers;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

