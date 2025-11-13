package com.trim.booking.util;

import com.trim.booking.exception.InvalidPhoneNumberException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PhoneNumberUtil
 */
class PhoneNumberUtilTest {

    @Test
    void testNormalizePhoneNumber_WithLeadingZero() {
        // Irish local format: 0871234567
        String result = PhoneNumberUtil.normalizePhoneNumber("0871234567", "353");
        assertEquals("+353871234567", result);
    }

    @Test
    void testNormalizePhoneNumber_WithSingleLeadingZero() {
        // Only first zero removed: 00871234567 becomes +3500871234567
        String result = PhoneNumberUtil.normalizePhoneNumber("00871234567", "353");
        assertEquals("+3500871234567", result);
    }

    @Test
    void testNormalizePhoneNumber_TooShort() {
        // Phone too short (less than 7 digits after removing leading zero)
        InvalidPhoneNumberException exception = assertThrows(InvalidPhoneNumberException.class, () -> {
            PhoneNumberUtil.normalizePhoneNumber("087123456", "353");
        });
        assertTrue(exception.getMessage().contains("Phone number too short - minimum 7 digits required"));
    }

    @Test
    void testNormalizePhoneNumber_OnlyZeros() {
        // Only zeros should give specific error message
        InvalidPhoneNumberException exception = assertThrows(InvalidPhoneNumberException.class, () -> {
            PhoneNumberUtil.normalizePhoneNumber("000", "353");
        });
        assertTrue(exception.getMessage().contains("Phone number contains only zeros"));
    }

    @Test
    void testNormalizePhoneNumber_WithSpacesAndDashes() {
        // Format with spaces and dashes: 087-123-4567
        String result = PhoneNumberUtil.normalizePhoneNumber("087-123-4567", "353");
        assertEquals("+353871234567", result);
    }

    @Test
    void testNormalizePhoneNumber_AlreadyE164() {
        // Already in E.164 format
        String result = PhoneNumberUtil.normalizePhoneNumber("+353871234567", "353");
        assertEquals("+353871234567", result);
    }

    @Test
    void testNormalizePhoneNumber_WithCountryCodeNoPlus() {
        // Has country code but no plus: 353871234567
        String result = PhoneNumberUtil.normalizePhoneNumber("353871234567", "353");
        assertEquals("+353871234567", result);
    }

    @Test
    void testNormalizePhoneNumber_JustDigits() {
        // Just the number without country code: 871234567
        String result = PhoneNumberUtil.normalizePhoneNumber("871234567", "353");
        assertEquals("+353871234567", result);
    }

    @Test
    void testNormalizePhoneNumber_WithParentheses() {
        // Format with parentheses: (087) 123-4567
        String result = PhoneNumberUtil.normalizePhoneNumber("(087) 123-4567", "353");
        assertEquals("+353871234567", result);
    }

    @Test
    void testNormalizePhoneNumber_DefaultCountryCode() {
        // Test default country code (Ireland)
        String result = PhoneNumberUtil.normalizePhoneNumber("0871234567");
        assertEquals("+353871234567", result);
    }

    @Test
    void testNormalizePhoneNumber_EmptyString() {
        // Empty string should throw exception
        assertThrows(InvalidPhoneNumberException.class, () -> {
            PhoneNumberUtil.normalizePhoneNumber("", "353");
        });
    }

    @Test
    void testNormalizePhoneNumber_NullString() {
        // Null should throw exception
        assertThrows(InvalidPhoneNumberException.class, () -> {
            PhoneNumberUtil.normalizePhoneNumber(null, "353");
        });
    }

    @Test
    void testValidateE164Format_Valid() {
        // Valid E.164 format
        assertTrue(PhoneNumberUtil.validateE164Format("+353871234567"));
        assertTrue(PhoneNumberUtil.validateE164Format("+14155552671"));
        assertTrue(PhoneNumberUtil.validateE164Format("+442071838750"));
    }

    @Test
    void testValidateE164Format_Invalid() {
        // Invalid formats
        assertFalse(PhoneNumberUtil.validateE164Format("0871234567")); // No country code
        assertFalse(PhoneNumberUtil.validateE164Format("353871234567")); // No plus
        assertFalse(PhoneNumberUtil.validateE164Format("+0871234567")); // Country code starts with 0
        assertFalse(PhoneNumberUtil.validateE164Format("871234567")); // No country code or plus
        assertFalse(PhoneNumberUtil.validateE164Format("")); // Empty
        assertFalse(PhoneNumberUtil.validateE164Format(null)); // Null
    }

    @Test
    void testNormalizePhoneNumber_USNumber() {
        // US phone number: (415) 555-2671
        String result = PhoneNumberUtil.normalizePhoneNumber("(415) 555-2671", "1");
        assertEquals("+14155552671", result);
    }

    @Test
    void testNormalizePhoneNumber_UKNumber() {
        // UK phone number: 020 7183 8750
        String result = PhoneNumberUtil.normalizePhoneNumber("020 7183 8750", "44");
        assertEquals("+442071838750", result);
    }

    @Test
    void testNormalizePhoneNumber_MinimumLength() {
        // Exactly 7 digits should work
        String result = PhoneNumberUtil.normalizePhoneNumber("0871234", "353");
        assertEquals("+353871234", result);
    }

    @Test
    void testNormalizePhoneNumber_JustUnderMinimum() {
        // 6 digits should fail
        InvalidPhoneNumberException exception = assertThrows(InvalidPhoneNumberException.class, () -> {
            PhoneNumberUtil.normalizePhoneNumber("087123", "353");
        });
        assertTrue(exception.getMessage().contains("Phone number too short"));
    }
}

