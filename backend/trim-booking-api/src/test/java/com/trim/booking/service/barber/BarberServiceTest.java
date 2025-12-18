package com.trim.booking.service.barber;

import com.trim.booking.entity.Barber;
import com.trim.booking.entity.User;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BarberService Unit Tests")
class BarberServiceTest {

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<Barber> barberCaptor;

    private BarberService barberService;

    @BeforeEach
    void setUp() {
        barberService = new BarberService(barberRepository, userRepository);
    }

    @Nested
    @DisplayName("createBarber")
    class CreateBarberTests {

        @Test
        @DisplayName("Should create barber with valid data")
        void shouldCreateBarberWithValidData() {
            // Given
            when(userRepository.existsByEmail("barber@test.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setId(1L);
                return u;
            });
            when(barberRepository.save(any(Barber.class))).thenAnswer(i -> {
                Barber b = i.getArgument(0);
                b.setId(1L);
                return b;
            });

            // When
            Barber result = barberService.createBarber(
                    "John", "Doe", "barber@test.com",
                    "+353851234567", "password123", "Expert barber", "http://image.url"
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBio()).isEqualTo("Expert barber");
            assertThat(result.getActive()).isTrue();

            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(User.Role.BARBER);
            assertThat(savedUser.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should throw when email already exists")
        void shouldThrowWhenEmailAlreadyExists() {
            // Given
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> barberService.createBarber(
                    "John", "Doe", "existing@test.com",
                    "+353851234567", "password123", "Bio", null
            ))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email already registered");
        }

        @Test
        @DisplayName("Should throw when phone number is invalid")
        void shouldThrowWhenPhoneNumberIsInvalid() {
            // Given
            when(userRepository.existsByEmail("barber@test.com")).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> barberService.createBarber(
                    "John", "Doe", "barber@test.com",
                    "invalid", "password123", "Bio", null
            ))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid phone number");
        }
    }

    @Nested
    @DisplayName("getAllBarbers")
    class GetAllBarbersTests {

        @Test
        @DisplayName("Should return all barbers")
        void shouldReturnAllBarbers() {
            // Given
            Barber barber1 = createBarber(1L, true);
            Barber barber2 = createBarber(2L, false);
            when(barberRepository.findAll()).thenReturn(List.of(barber1, barber2));

            // When
            List<Barber> result = barberService.getAllBarbers();

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no barbers")
        void shouldReturnEmptyListWhenNoBarbers() {
            // Given
            when(barberRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<Barber> result = barberService.getAllBarbers();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getActiveBarbers")
    class GetActiveBarbersTests {

        @Test
        @DisplayName("Should return only active barbers")
        void shouldReturnOnlyActiveBarbers() {
            // Given
            Barber activeBarber = createBarber(1L, true);
            when(barberRepository.findByActiveTrue()).thenReturn(List.of(activeBarber));

            // When
            List<Barber> result = barberService.getActiveBarbers();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getBarberById")
    class GetBarberByIdTests {

        @Test
        @DisplayName("Should return barber when found")
        void shouldReturnBarberWhenFound() {
            // Given
            Long barberId = 1L;
            Barber barber = createBarber(barberId, true);
            when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));

            // When
            Optional<Barber> result = barberService.getBarberById(barberId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(barberId);
        }

        @Test
        @DisplayName("Should return empty when barber not found")
        void shouldReturnEmptyWhenBarberNotFound() {
            // Given
            Long barberId = 999L;
            when(barberRepository.findById(barberId)).thenReturn(Optional.empty());

            // When
            Optional<Barber> result = barberService.getBarberById(barberId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateBarber")
    class UpdateBarberTests {

        @Test
        @DisplayName("Should update barber fields")
        void shouldUpdateBarberFields() {
            // Given
            Long barberId = 1L;
            Barber existingBarber = createBarberWithUser(barberId);
            when(barberRepository.findById(barberId)).thenReturn(Optional.of(existingBarber));
            when(barberRepository.save(any(Barber.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Barber result = barberService.updateBarber(
                    barberId, "NewFirst", "NewLast", "new@email.com",
                    "+353851111111", "New bio", "http://new.image"
            );

            // Then
            assertThat(result.getUser().getFirstName()).isEqualTo("NewFirst");
            assertThat(result.getUser().getLastName()).isEqualTo("NewLast");
            assertThat(result.getBio()).isEqualTo("New bio");
        }

        @Test
        @DisplayName("Should throw when barber not found")
        void shouldThrowWhenBarberNotFound() {
            // Given
            Long barberId = 999L;
            when(barberRepository.findById(barberId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> barberService.updateBarber(
                    barberId, "First", "Last", "email@test.com",
                    "+353851234567", "Bio", null
            ))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Barber not found with id: " + barberId);
        }

        @Test
        @DisplayName("Should only update non-null fields")
        void shouldOnlyUpdateNonNullFields() {
            // Given
            Long barberId = 1L;
            Barber existingBarber = createBarberWithUser(barberId);
            existingBarber.getUser().setFirstName("OriginalFirst");
            existingBarber.setBio("Original bio");
            when(barberRepository.findById(barberId)).thenReturn(Optional.of(existingBarber));
            when(barberRepository.save(any(Barber.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Barber result = barberService.updateBarber(
                    barberId, null, null, null, null, "New bio", null
            );

            // Then
            assertThat(result.getUser().getFirstName()).isEqualTo("OriginalFirst");
            assertThat(result.getBio()).isEqualTo("New bio");
        }
    }

    @Nested
    @DisplayName("deactivateBarber")
    class DeactivateBarberTests {

        @Test
        @DisplayName("Should deactivate barber")
        void shouldDeactivateBarber() {
            // Given
            Long barberId = 1L;
            Barber barber = createBarber(barberId, true);
            when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));
            when(barberRepository.save(any(Barber.class))).thenAnswer(i -> i.getArgument(0));

            // When
            barberService.deactivateBarber(barberId);

            // Then
            verify(barberRepository).save(barberCaptor.capture());
            assertThat(barberCaptor.getValue().getActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw when barber not found")
        void shouldThrowWhenBarberNotFound() {
            // Given
            Long barberId = 999L;
            when(barberRepository.findById(barberId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> barberService.deactivateBarber(barberId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Barber not found with id: " + barberId);
        }
    }

    @Nested
    @DisplayName("deleteBarber")
    class DeleteBarberTests {

        @Test
        @DisplayName("Should delete barber")
        void shouldDeleteBarber() {
            // Given
            Long barberId = 1L;

            // When
            barberService.deleteBarber(barberId);

            // Then
            verify(barberRepository).deleteById(barberId);
        }
    }

    // Helper methods
    private Barber createBarber(Long id, boolean active) {
        Barber barber = new Barber();
        barber.setId(id);
        barber.setActive(active);
        return barber;
    }

    private Barber createBarberWithUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Test");
        user.setLastName("Barber");
        user.setEmail("test@barber.com");

        Barber barber = new Barber();
        barber.setId(id);
        barber.setUser(user);
        barber.setActive(true);
        barber.setBio("Test bio");
        return barber;
    }
}

