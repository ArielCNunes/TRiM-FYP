package com.trim.booking.service.customer;

import com.trim.booking.dto.customer.CustomerListResponse;
import com.trim.booking.dto.customer.CustomerResponse;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.User;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(userRepository, bookingRepository);
    }

    private User createCustomer(Long id, String firstName, String lastName, String email) {
        User customer = new User();
        customer.setId(id);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone("+353871234567");
        customer.setRole(User.Role.CUSTOMER);
        customer.setBlacklisted(false);
        customer.setCreatedAt(LocalDateTime.now());
        return customer;
    }

    @Nested
    @DisplayName("getCustomers")
    class GetCustomersTests {

        @Test
        @DisplayName("Should return paginated list of customers")
        void shouldReturnPaginatedCustomers() {
            // Given
            User customer1 = createCustomer(1L, "John", "Doe", "john@test.com");
            User customer2 = createCustomer(2L, "Jane", "Smith", "jane@test.com");
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> customerPage = new PageImpl<>(Arrays.asList(customer1, customer2), pageable, 2);

            when(userRepository.findByRole(User.Role.CUSTOMER, pageable)).thenReturn(customerPage);
            when(bookingRepository.countByCustomerIdAndStatus(eq(1L), eq(Booking.BookingStatus.NO_SHOW))).thenReturn(2L);
            when(bookingRepository.countByCustomerIdAndStatus(eq(2L), eq(Booking.BookingStatus.NO_SHOW))).thenReturn(0L);

            // When
            CustomerListResponse response = customerService.getCustomers(pageable);

            // Then
            assertThat(response.getCustomers()).hasSize(2);
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(20);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(1);

            assertThat(response.getCustomers().get(0).getFirstName()).isEqualTo("John");
            assertThat(response.getCustomers().get(0).getNoShowCount()).isEqualTo(2L);
            assertThat(response.getCustomers().get(1).getFirstName()).isEqualTo("Jane");
            assertThat(response.getCustomers().get(1).getNoShowCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should return empty list when no customers exist")
        void shouldReturnEmptyListWhenNoCustomers() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            when(userRepository.findByRole(User.Role.CUSTOMER, pageable)).thenReturn(emptyPage);

            // When
            CustomerListResponse response = customerService.getCustomers(pageable);

            // Then
            assertThat(response.getCustomers()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getCustomerById")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("Should return customer when found")
        void shouldReturnCustomerWhenFound() {
            // Given
            User customer = createCustomer(1L, "John", "Doe", "john@test.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(bookingRepository.countByCustomerIdAndStatus(1L, Booking.BookingStatus.NO_SHOW)).thenReturn(3L);

            // When
            CustomerResponse response = customerService.getCustomerById(1L);

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");
            assertThat(response.getEmail()).isEqualTo("john@test.com");
            assertThat(response.getNoShowCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> customerService.getCustomerById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer not found with id: 999");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user is not a customer")
        void shouldThrowWhenUserIsNotCustomer() {
            // Given
            User admin = new User();
            admin.setId(1L);
            admin.setRole(User.Role.ADMIN);
            when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> customerService.getCustomerById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer not found with id: 1");
        }
    }

    @Nested
    @DisplayName("blacklistCustomer")
    class BlacklistCustomerTests {

        @Test
        @DisplayName("Should blacklist customer successfully")
        void shouldBlacklistCustomerSuccessfully() {
            // Given
            User customer = createCustomer(1L, "John", "Doe", "john@test.com");
            String reason = "Repeated no-shows";

            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(bookingRepository.countByCustomerIdAndStatus(1L, Booking.BookingStatus.NO_SHOW)).thenReturn(0L);

            // When
            CustomerResponse response = customerService.blacklistCustomer(1L, reason);

            // Then
            assertThat(response.getBlacklisted()).isTrue();
            assertThat(response.getBlacklistReason()).isEqualTo(reason);
            assertThat(response.getBlacklistedAt()).isNotNull();

            // Verify the user was saved with correct values
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getBlacklisted()).isTrue();
            assertThat(savedUser.getBlacklistReason()).isEqualTo(reason);
            assertThat(savedUser.getBlacklistedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> customerService.blacklistCustomer(999L, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer not found with id: 999");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user is not a customer")
        void shouldThrowWhenUserIsNotCustomer() {
            // Given
            User barber = new User();
            barber.setId(1L);
            barber.setRole(User.Role.BARBER);
            when(userRepository.findById(1L)).thenReturn(Optional.of(barber));

            // When/Then
            assertThatThrownBy(() -> customerService.blacklistCustomer(1L, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer not found with id: 1");
        }
    }

    @Nested
    @DisplayName("unblacklistCustomer")
    class UnblacklistCustomerTests {

        @Test
        @DisplayName("Should unblacklist customer successfully")
        void shouldUnblacklistCustomerSuccessfully() {
            // Given
            User customer = createCustomer(1L, "John", "Doe", "john@test.com");
            customer.setBlacklisted(true);
            customer.setBlacklistReason("Previous offense");
            customer.setBlacklistedAt(LocalDateTime.now());

            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(bookingRepository.countByCustomerIdAndStatus(1L, Booking.BookingStatus.NO_SHOW)).thenReturn(0L);

            // When
            CustomerResponse response = customerService.unblacklistCustomer(1L);

            // Then
            assertThat(response.getBlacklisted()).isFalse();
            assertThat(response.getBlacklistReason()).isNull();
            assertThat(response.getBlacklistedAt()).isNull();

            // Verify the user was saved with correct values
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getBlacklisted()).isFalse();
            assertThat(savedUser.getBlacklistReason()).isNull();
            assertThat(savedUser.getBlacklistedAt()).isNull();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> customerService.unblacklistCustomer(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer not found with id: 999");
        }
    }

    @Nested
    @DisplayName("isBlacklisted")
    class IsBlacklistedTests {

        @Test
        @DisplayName("Should return true when customer is blacklisted")
        void shouldReturnTrueWhenBlacklisted() {
            // Given
            User customer = createCustomer(1L, "John", "Doe", "john@test.com");
            customer.setBlacklisted(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

            // When
            boolean result = customerService.isBlacklisted(1L);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when customer is not blacklisted")
        void shouldReturnFalseWhenNotBlacklisted() {
            // Given
            User customer = createCustomer(1L, "John", "Doe", "john@test.com");
            customer.setBlacklisted(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

            // When
            boolean result = customerService.isBlacklisted(1L);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> customerService.isBlacklisted(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getBlacklistReason")
    class GetBlacklistReasonTests {

        @Test
        @DisplayName("Should return blacklist reason when customer is blacklisted")
        void shouldReturnReasonWhenBlacklisted() {
            // Given
            User customer = createCustomer(1L, "John", "Doe", "john@test.com");
            customer.setBlacklisted(true);
            customer.setBlacklistReason("Too many no-shows");
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

            // When
            String reason = customerService.getBlacklistReason(1L);

            // Then
            assertThat(reason).isEqualTo("Too many no-shows");
        }

        @Test
        @DisplayName("Should return null when customer is not blacklisted")
        void shouldReturnNullWhenNotBlacklisted() {
            // Given
            User customer = createCustomer(1L, "John", "Doe", "john@test.com");
            customer.setBlacklisted(false);
            customer.setBlacklistReason(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

            // When
            String reason = customerService.getBlacklistReason(1L);

            // Then
            assertThat(reason).isNull();
        }
    }
}

