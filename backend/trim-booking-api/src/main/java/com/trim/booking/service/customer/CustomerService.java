package com.trim.booking.service.customer;

import com.trim.booking.dto.customer.CustomerListResponse;
import com.trim.booking.dto.customer.CustomerResponse;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.User;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing customer operations including blacklisting.
 */
@Service
public class CustomerService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public CustomerService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Get paginated list of customers with their no-show counts.
     *
     * @param pageable Pagination information
     * @return CustomerListResponse with paginated customers
     */
    public CustomerListResponse getCustomers(Pageable pageable) {
        Page<User> customerPage = userRepository.findByRole(User.Role.CUSTOMER, pageable);

        List<CustomerResponse> customers = customerPage.getContent().stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());

        return new CustomerListResponse(
                customers,
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages()
        );
    }

    /**
     * Get a single customer by ID.
     *
     * @param customerId Customer ID
     * @return CustomerResponse
     * @throws ResourceNotFoundException if customer not found
     */
    public CustomerResponse getCustomerById(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (customer.getRole() != User.Role.CUSTOMER) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        return mapToCustomerResponse(customer);
    }

    /**
     * Blacklist a customer.
     *
     * @param customerId Customer ID
     * @param reason     Reason for blacklisting
     * @return Updated CustomerResponse
     * @throws ResourceNotFoundException if customer not found
     */
    @Transactional
    public CustomerResponse blacklistCustomer(Long customerId, String reason) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (customer.getRole() != User.Role.CUSTOMER) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        customer.setBlacklisted(true);
        customer.setBlacklistReason(reason);
        customer.setBlacklistedAt(LocalDateTime.now());

        User savedCustomer = userRepository.save(customer);
        return mapToCustomerResponse(savedCustomer);
    }

    /**
     * Remove blacklist from a customer.
     *
     * @param customerId Customer ID
     * @return Updated CustomerResponse
     * @throws ResourceNotFoundException if customer not found
     */
    @Transactional
    public CustomerResponse unblacklistCustomer(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (customer.getRole() != User.Role.CUSTOMER) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        customer.setBlacklisted(false);
        customer.setBlacklistReason(null);
        customer.setBlacklistedAt(null);

        User savedCustomer = userRepository.save(customer);
        return mapToCustomerResponse(savedCustomer);
    }

    /**
     * Check if a customer is blacklisted.
     *
     * @param customerId Customer ID
     * @return true if blacklisted, false otherwise
     */
    public boolean isBlacklisted(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return Boolean.TRUE.equals(customer.getBlacklisted());
    }

    /**
     * Get the blacklist reason for a customer.
     *
     * @param customerId Customer ID
     * @return Blacklist reason or null if not blacklisted
     */
    public String getBlacklistReason(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return customer.getBlacklistReason();
    }

    /**
     * Map User entity to CustomerResponse DTO.
     */
    private CustomerResponse mapToCustomerResponse(User user) {
        Long noShowCount = bookingRepository.countByCustomerIdAndStatus(
                user.getId(), Booking.BookingStatus.NO_SHOW);

        return new CustomerResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getBlacklisted(),
                user.getBlacklistReason(),
                user.getBlacklistedAt(),
                noShowCount != null ? noShowCount : 0L
        );
    }
}

