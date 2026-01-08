package com.trim.booking.controller;

import com.trim.booking.dto.customer.BlacklistRequest;
import com.trim.booking.dto.customer.CustomerListResponse;
import com.trim.booking.dto.customer.CustomerResponse;
import com.trim.booking.service.customer.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing customer operations.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Get paginated list of customers.
     *
     * GET /api/admin/customers?page=0&size=20&sort=createdAt,desc
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @param sort Sort field and direction
     * @return CustomerListResponse with paginated customers
     */
    @GetMapping
    public ResponseEntity<CustomerListResponse> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        CustomerListResponse response = customerService.getCustomers(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single customer by ID.
     *
     * GET /api/admin/customers/{id}
     *
     * @param id Customer ID
     * @return CustomerResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Blacklist a customer.
     *
     * PUT /api/admin/customers/{id}/blacklist
     *
     * @param id Customer ID
     * @param request BlacklistRequest containing the reason
     * @return Updated CustomerResponse
     */
    @PutMapping("/{id}/blacklist")
    public ResponseEntity<CustomerResponse> blacklistCustomer(
            @PathVariable Long id,
            @Valid @RequestBody BlacklistRequest request) {
        CustomerResponse customer = customerService.blacklistCustomer(id, request.getReason());
        return ResponseEntity.ok(customer);
    }

    /**
     * Remove blacklist from a customer.
     *
     * PUT /api/admin/customers/{id}/unblacklist
     *
     * @param id Customer ID
     * @return Updated CustomerResponse
     */
    @PutMapping("/{id}/unblacklist")
    public ResponseEntity<CustomerResponse> unblacklistCustomer(@PathVariable Long id) {
        CustomerResponse customer = customerService.unblacklistCustomer(id);
        return ResponseEntity.ok(customer);
    }
}

