package com.trim.booking.service.booking;

import com.trim.booking.entity.Barber;
import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.entity.User;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.ServiceRepository;
import com.trim.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingValidationService Unit Tests")
class BookingValidationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private ServiceRepository serviceRepository;

    private BookingValidationService bookingValidationService;

    @BeforeEach
    void setUp() {
        bookingValidationService = new BookingValidationService(
                userRepository, barberRepository, serviceRepository);
    }

    @Nested
    @DisplayName("validateAndGetCustomer")
    class ValidateAndGetCustomerTests {

        @Test
        @DisplayName("Should return customer when found")
        void shouldReturnCustomerWhenFound() {
            // Given
            Long customerId = 1L;
            User customer = new User();
            customer.setId(customerId);
            customer.setFirstName("John");
            customer.setLastName("Doe");
            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

            // When
            User result = bookingValidationService.validateAndGetCustomer(customerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(customerId);
            assertThat(result.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            // Given
            Long customerId = 999L;
            when(userRepository.findById(customerId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingValidationService.validateAndGetCustomer(customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer not found with ID: " + customerId);
        }
    }

    @Nested
    @DisplayName("validateAndGetBarber")
    class ValidateAndGetBarberTests {

        @Test
        @DisplayName("Should return barber when found")
        void shouldReturnBarberWhenFound() {
            // Given
            Long barberId = 1L;
            Barber barber = new Barber();
            barber.setId(barberId);
            barber.setBio("Expert barber");
            when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));

            // When
            Barber result = bookingValidationService.validateAndGetBarber(barberId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(barberId);
            assertThat(result.getBio()).isEqualTo("Expert barber");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when barber not found")
        void shouldThrowExceptionWhenBarberNotFound() {
            // Given
            Long barberId = 999L;
            when(barberRepository.findById(barberId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingValidationService.validateAndGetBarber(barberId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Barber not found with ID: " + barberId);
        }
    }

    @Nested
    @DisplayName("validateAndGetService")
    class ValidateAndGetServiceTests {

        @Test
        @DisplayName("Should return service when found")
        void shouldReturnServiceWhenFound() {
            // Given
            Long serviceId = 1L;
            ServiceOffered service = new ServiceOffered();
            service.setId(serviceId);
            service.setName("Haircut");
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

            // When
            ServiceOffered result = bookingValidationService.validateAndGetService(serviceId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(serviceId);
            assertThat(result.getName()).isEqualTo("Haircut");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when service not found")
        void shouldThrowExceptionWhenServiceNotFound() {
            // Given
            Long serviceId = 999L;
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingValidationService.validateAndGetService(serviceId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Service not found with ID: " + serviceId);
        }
    }

    @Nested
    @DisplayName("validateBookingTimeInFuture")
    class ValidateBookingTimeInFutureTests {

        @Test
        @DisplayName("Should pass when booking date is in the future")
        void shouldPassWhenBookingDateIsInFuture() {
            // Given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(10, 0);

            // When/Then - should not throw
            bookingValidationService.validateBookingTimeInFuture(futureDate, startTime);
        }

        @Test
        @DisplayName("Should pass when booking is today but time is in the future")
        void shouldPassWhenBookingIsTodayWithFutureTime() {
            // Given
            LocalDate today = LocalDate.now();
            LocalTime futureTime = LocalTime.now().plusHours(2);

            // When/Then - should not throw
            bookingValidationService.validateBookingTimeInFuture(today, futureTime);
        }

        @Test
        @DisplayName("Should throw when booking date is in the past")
        void shouldThrowWhenBookingDateIsInPast() {
            // Given
            LocalDate pastDate = LocalDate.now().minusDays(1);
            LocalTime startTime = LocalTime.of(10, 0);

            // When/Then
            assertThatThrownBy(() ->
                    bookingValidationService.validateBookingTimeInFuture(pastDate, startTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot book in the past");
        }

        @Test
        @DisplayName("Should throw when booking is today but time is in the past")
        void shouldThrowWhenBookingIsTodayWithPastTime() {
            // Given
            LocalDate today = LocalDate.now();
            LocalTime pastTime = LocalTime.now().minusHours(2);

            // When/Then
            assertThatThrownBy(() ->
                    bookingValidationService.validateBookingTimeInFuture(today, pastTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot book in the past");
        }
    }
}

