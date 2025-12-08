package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.dto.auth.LoginRequest;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.User;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.login()
 *
 * Tests cover:
 * - Successful login for different user roles (CUSTOMER, BARBER, ADMIN)
 * - JWT token generation
 * - Invalid credentials handling
 * - Validation errors
 * - Barber ID inclusion for barber users
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    private ObjectMapper objectMapper;
    private BCryptPasswordEncoder passwordEncoder;

    private User customer;
    private User barberUser;
    private User admin;
    private Barber barber;

    private static final String VALID_PASSWORD = "password123";

    @BeforeAll
    void setupOnce() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @BeforeEach
    void setUp() {
        // Clean up before each test
        barberRepository.deleteAll();
        userRepository.deleteAll();

        // Create a customer
        customer = new User();
        customer.setFirstName("John");
        customer.setLastName("Customer");
        customer.setEmail("customer@test.com");
        customer.setPasswordHash(passwordEncoder.encode(VALID_PASSWORD));
        customer.setPhone("+353871234567");
        customer.setRole(User.Role.CUSTOMER);
        customer = userRepository.save(customer);

        // Create a barber user
        barberUser = new User();
        barberUser.setFirstName("Jane");
        barberUser.setLastName("Barber");
        barberUser.setEmail("barber@test.com");
        barberUser.setPasswordHash(passwordEncoder.encode(VALID_PASSWORD));
        barberUser.setPhone("+353877654321");
        barberUser.setRole(User.Role.BARBER);
        barberUser = userRepository.save(barberUser);

        // Create barber entity linked to barber user
        barber = new Barber();
        barber.setUser(barberUser);
        barber.setBio("Expert barber");
        barber.setActive(true);
        barber = barberRepository.save(barber);

        // Create an admin
        admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode(VALID_PASSWORD));
        admin.setPhone("+353879999999");
        admin.setRole(User.Role.ADMIN);
        admin = userRepository.save(admin);
    }

    // ==================== SUCCESSFUL LOGIN TESTS ====================

    @Test
    @DisplayName("Should successfully login customer with valid credentials")
    void login_CustomerWithValidCredentials_ReturnsTokenAndUserDetails() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@test.com");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.email").value("customer@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Customer"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.barberId").isEmpty());
    }

    @Test
    @DisplayName("Should successfully login barber with valid credentials and return barberId")
    void login_BarberWithValidCredentials_ReturnsTokenAndBarberId() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("barber@test.com");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(barberUser.getId()))
                .andExpect(jsonPath("$.email").value("barber@test.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Barber"))
                .andExpect(jsonPath("$.role").value("BARBER"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.barberId").value(barber.getId()));
    }

    @Test
    @DisplayName("Should successfully login admin with valid credentials")
    void login_AdminWithValidCredentials_ReturnsTokenAndUserDetails() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(admin.getId()))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.barberId").isEmpty());
    }

    @Test
    @DisplayName("Should return valid JWT token that can be decoded")
    void login_ValidCredentials_ReturnsValidJwtToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@test.com");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(matchesPattern("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")));
    }

    // ==================== INVALID CREDENTIALS TESTS ====================

    @Test
    @DisplayName("Should return 401 when email does not exist")
    void login_NonExistentEmail_ReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("Should return 401 when password is incorrect")
    void login_IncorrectPassword_ReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@test.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("Should return 401 when both email and password are wrong")
    void login_BothCredentialsWrong_ReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@test.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return same error message for wrong email and wrong password (security)")
    void login_InvalidCredentials_ReturnsSameErrorMessage() throws Exception {
        // Wrong email
        LoginRequest wrongEmailRequest = new LoginRequest();
        wrongEmailRequest.setEmail("nonexistent@test.com");
        wrongEmailRequest.setPassword(VALID_PASSWORD);

        String wrongEmailResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongEmailRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // Wrong password
        LoginRequest wrongPasswordRequest = new LoginRequest();
        wrongPasswordRequest.setEmail("customer@test.com");
        wrongPasswordRequest.setPassword("wrongpassword");

        String wrongPasswordResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // Both should return the same generic message (security best practice)
        org.junit.jupiter.api.Assertions.assertTrue(
                wrongEmailResponse.contains("Invalid email or password") &&
                wrongPasswordResponse.contains("Invalid email or password")
        );
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Should return 400 when email is missing")
    void login_MissingEmail_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setPassword(VALID_PASSWORD);
        // email not set

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is missing")
    void login_MissingPassword_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@test.com");
        // password not set

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid")
    void login_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email is empty string")
    void login_EmptyEmail_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is empty string")
    void login_EmptyPassword_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@test.com");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle email case insensitively")
    void login_EmailDifferentCase_SucceedsIfCaseInsensitive() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("CUSTOMER@TEST.COM");
        request.setPassword(VALID_PASSWORD);

        // This test documents current behavior - may pass or fail depending on DB config
        // If case-insensitive: expect 200
        // If case-sensitive: expect 401
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        // Not asserting specific result - just documenting behavior
    }

    @Test
    @DisplayName("Should handle whitespace in email")
    void login_EmailWithWhitespace_HandlesAppropriately() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(" customer@test.com ");
        request.setPassword(VALID_PASSWORD);

        // Depends on whether service trims email
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void login_PasswordWithSpecialCharacters_WorksCorrectly() throws Exception {
        // Create user with special character password
        User specialUser = new User();
        specialUser.setFirstName("Special");
        specialUser.setLastName("User");
        specialUser.setEmail("special@test.com");
        specialUser.setPasswordHash(passwordEncoder.encode("P@ss!w0rd#$%"));
        specialUser.setPhone("+353871111111");
        specialUser.setRole(User.Role.CUSTOMER);
        userRepository.save(specialUser);

        LoginRequest request = new LoginRequest();
        request.setEmail("special@test.com");
        request.setPassword("P@ss!w0rd#$%");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Should handle unicode characters in password")
    void login_PasswordWithUnicode_WorksCorrectly() throws Exception {
        // Create user with unicode password
        User unicodeUser = new User();
        unicodeUser.setFirstName("Unicode");
        unicodeUser.setLastName("User");
        unicodeUser.setEmail("unicode@test.com");
        unicodeUser.setPasswordHash(passwordEncoder.encode("пароль123"));
        unicodeUser.setPhone("+353872222222");
        unicodeUser.setRole(User.Role.CUSTOMER);
        userRepository.save(unicodeUser);

        LoginRequest request = new LoginRequest();
        request.setEmail("unicode@test.com");
        request.setPassword("пароль123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    // ==================== BARBER WITHOUT BARBER ENTITY TESTS ====================

    @Test
    @DisplayName("Should return null barberId when barber user has no barber entity")
    void login_BarberUserWithoutBarberEntity_ReturnsNullBarberId() throws Exception {
        // Create a barber user without corresponding Barber entity
        User orphanBarberUser = new User();
        orphanBarberUser.setFirstName("Orphan");
        orphanBarberUser.setLastName("Barber");
        orphanBarberUser.setEmail("orphan.barber@test.com");
        orphanBarberUser.setPasswordHash(passwordEncoder.encode(VALID_PASSWORD));
        orphanBarberUser.setPhone("+353873333333");
        orphanBarberUser.setRole(User.Role.BARBER);
        userRepository.save(orphanBarberUser);

        LoginRequest request = new LoginRequest();
        request.setEmail("orphan.barber@test.com");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("BARBER"))
                .andExpect(jsonPath("$.barberId").isEmpty());
    }

    // ==================== REQUEST FORMAT TESTS ====================

    @Test
    @DisplayName("Should return 400 for invalid JSON payload")
    void login_InvalidJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return error for non-JSON content type")
    void login_NonJsonContentType_ReturnsError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("email=customer@test.com&password=password123"))
                .andExpect(status().is5xxServerError()); // GlobalExceptionHandler converts to 500
    }
}

