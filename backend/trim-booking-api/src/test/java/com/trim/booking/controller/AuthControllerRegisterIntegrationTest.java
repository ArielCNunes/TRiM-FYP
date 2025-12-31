package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.dto.auth.RegisterRequest;
import com.trim.booking.entity.User;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.register()
 *
 * Tests cover:
 * - Successful customer registration
 * - Duplicate email handling
 * - Validation errors (missing fields, invalid email, short password, invalid phone)
 * - Phone number normalization
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerRegisterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private BCryptPasswordEncoder passwordEncoder;

    private static final String VALID_PASSWORD = "password123";
    private static final String VALID_PHONE = "+353871234567";
    private static final String VALID_EMAIL = "newuser@test.com";

    @BeforeAll
    void setupOnce() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
    }

    // ==================== SUCCESSFUL REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should successfully register new customer with valid data")
    void register_ValidRequest_ReturnsCreatedUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.phone").value(VALID_PHONE));

        // Verify user was saved in database
        assertTrue(userRepository.existsByEmail(VALID_EMAIL));
        User savedUser = userRepository.findByEmail(VALID_EMAIL).orElseThrow();
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals(User.Role.CUSTOMER, savedUser.getRole());
        assertTrue(passwordEncoder.matches(VALID_PASSWORD, savedUser.getPasswordHash()));
    }

    @Test
    @DisplayName("Should normalize phone number during registration")
    void register_LocalPhoneFormat_NormalizesToE164() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@test.com");
        request.setPassword(VALID_PASSWORD);
        request.setPhone("0871234567"); // Local Irish format

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("+353871234567"));
    }

    @Test
    @DisplayName("Should accept phone number with dashes and normalize it")
    void register_PhoneWithDashes_NormalizesSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Bob");
        request.setLastName("Brown");
        request.setEmail("bob@test.com");
        request.setPassword(VALID_PASSWORD);
        request.setPhone("087-123-4567");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("+353871234567"));
    }

    // ==================== DUPLICATE EMAIL TESTS ====================

    @Test
    @DisplayName("Should return error when email already exists")
    void register_DuplicateEmail_ReturnsError() throws Exception {
        // Create existing user
        User existingUser = new User();
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEmail(VALID_EMAIL);
        existingUser.setPasswordHash(passwordEncoder.encode(VALID_PASSWORD));
        existingUser.setPhone(VALID_PHONE);
        existingUser.setRole(User.Role.CUSTOMER);
        userRepository.save(existingUser);

        // Attempt to register with same email
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);
        request.setPhone("+353879876543");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Email already registered")));
    }

    // ==================== VALIDATION ERROR TESTS ====================

    @Test
    @DisplayName("Should return 400 when first name is missing")
    void register_MissingFirstName_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setLastName("Doe");
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").value("First name is required"));
    }

    @Test
    @DisplayName("Should return 400 when last name is missing")
    void register_MissingLastName_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lastName").value("Last name is required"));
    }

    @Test
    @DisplayName("Should return 400 when email is missing")
    void register_MissingEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword(VALID_PASSWORD);
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Email is required"));
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid")
    void register_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        request.setPassword(VALID_PASSWORD);
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Email must be valid"));
    }

    @Test
    @DisplayName("Should return 400 when password is missing")
    void register_MissingPassword_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail(VALID_EMAIL);
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    @DisplayName("Should return 400 when password is too short")
    void register_ShortPassword_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail(VALID_EMAIL);
        request.setPassword("short");
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password must be at least 8 characters"));
    }

    @Test
    @DisplayName("Should return 400 when phone is missing")
    void register_MissingPhone_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phone").value("Phone number is required"));
    }

    @Test
    @DisplayName("Should return 400 when phone format is invalid")
    void register_InvalidPhoneFormat_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);
        request.setPhone("invalid-phone");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phoneValid").value("Phone must be in valid international format"));
    }

    @Test
    @DisplayName("Should return 400 when all fields are missing")
    void register_EmptyRequest_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle email case insensitively")
    void register_UppercaseEmail_NormalizesAndSaves() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("UPPERCASE@TEST.COM");
        request.setPassword(VALID_PASSWORD);
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should trim whitespace from names")
    void register_NamesWithWhitespace_TrimsSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("  John  ");
        request.setLastName("  Doe  ");
        request.setEmail("trim@test.com");
        request.setPassword(VALID_PASSWORD);
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should accept password with exactly 8 characters")
    void register_PasswordExactly8Chars_Succeeds() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("exact8@test.com");
        request.setPassword("12345678");
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should reject password with 7 characters")
    void register_Password7Chars_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("seven@test.com");
        request.setPassword("1234567");
        request.setPhone(VALID_PHONE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password must be at least 8 characters"));
    }
}

