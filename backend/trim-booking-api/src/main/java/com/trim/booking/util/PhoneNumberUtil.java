package com.trim.booking.util;

import com.trim.booking.exception.InvalidPhoneNumberException;

import java.util.regex.Pattern;

/**
 * Utility class for phone number normalization and validation.
 * Ensures all phone numbers are stored in E.164 format (+[country][number]).
 */
public class PhoneNumberUtil {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    /**
     * Normalize a phone number to E.164 format.
     *
     * @param rawPhone    Raw phone number input (may contain spaces, dashes, etc.)
     * @param countryCode Country code (default "353" for Ireland)
     * @return Normalized phone number in E.164 format (+[country][number])
     * @throws InvalidPhoneNumberException if phone number is invalid
     */
    public static String normalizePhoneNumber(String rawPhone, String countryCode) {
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            throw new InvalidPhoneNumberException("Phone number cannot be empty");
        }

        // Default to Ireland if country code not provided
        if (countryCode == null || countryCode.trim().isEmpty()) {
            countryCode = "353";
        }

        // Store original to check for plus sign
        String original = rawPhone.trim();

        // Strip all non-digit characters except leading plus
        String digitsOnly = rawPhone.replaceAll("[^0-9+]", "");

        // If it already has a plus sign, validate it's proper E.164
        if (original.startsWith("+")) {
            if (validateE164Format(digitsOnly)) {
                return digitsOnly;
            } else {
                throw new InvalidPhoneNumberException("Invalid E.164 format: " + rawPhone);
            }
        }

        // Remove SINGLE leading zero (not all leading zeros)
        if (digitsOnly.startsWith("0")) {
            digitsOnly = digitsOnly.substring(1);
        }

        // Check if it's empty after removing leading zero
        if (digitsOnly.isEmpty()) {
            throw new InvalidPhoneNumberException("Phone number contains only zeros");
        }

        // Check minimum length (7 digits required)
        if (digitsOnly.length() < 7) {
            throw new InvalidPhoneNumberException("Phone number too short - minimum 7 digits required");
        }

        // If it starts with the country code already, just add plus
        if (digitsOnly.startsWith(countryCode)) {
            String normalized = "+" + digitsOnly;
            if (validateE164Format(normalized)) {
                return normalized;
            }
        }

        // Otherwise prepend country code
        String normalized = "+" + countryCode + digitsOnly;

        // Final validation
        if (!validateE164Format(normalized)) {
            throw new InvalidPhoneNumberException("Invalid phone number format: " + rawPhone);
        }

        return normalized;
    }

    /**
     * Normalize phone number with default country code (Ireland).
     */
    public static String normalizePhoneNumber(String rawPhone) {
        return normalizePhoneNumber(rawPhone, "353");
    }

    /**
     * Validate if a string is in proper E.164 format.
     * Format: +[country code 1-9][number] with total length 7-15 digits after plus.
     *
     * @param phone Phone number to validate
     * @return true if valid E.164 format, false otherwise
     */
    public static boolean validateE164Format(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return E164_PATTERN.matcher(phone.trim()).matches();
    }
}

