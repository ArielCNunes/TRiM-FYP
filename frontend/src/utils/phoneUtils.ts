/**
 * Phone number utility functions for normalizing and validating phone numbers.
 * All phone numbers are stored in E.164 format: +[country code][number]
 */

/**
 * Normalize a phone number to E.164 format.
 * @param rawPhone - Raw phone input from user (can include spaces, dashes, parentheses)
 * @param countryCode - Country code without plus (default: "353" for Ireland)
 * @returns Normalized phone number in E.164 format (+353871234567)
 */
export function normalizePhoneNumber(
  rawPhone: string,
  countryCode: string = "353"
): string {
  if (!rawPhone) return "";

  // Remove all non-digit characters except leading +
  const hasPlus = rawPhone.trim().startsWith("+");
  const digitsOnly = rawPhone.replace(/\D/g, "");

  if (!digitsOnly) return "";

  // If input already has +, it must have at least 7 digits total
  if (hasPlus) {
    if (digitsOnly.length < 7) {
      return ""; // Too short for valid E.164
    }
    const normalized = `+${digitsOnly}`;
    return normalized;
  }

  // For local input, require at least 7 digits before normalizing
  if (digitsOnly.length < 7) {
    return ""; // Too short for valid phone number
  }

  // Remove leading zero (common in local format: 087 -> 87)
  const withoutLeadingZero = digitsOnly.startsWith("0")
    ? digitsOnly.slice(1)
    : digitsOnly;

  // For Irish numbers, enforce exactly 9 digits after removing leading zero
  if (withoutLeadingZero.length !== 9) {
    return "";
  }

  // Check if it already starts with country code
  if (withoutLeadingZero.startsWith(countryCode)) {
    return `+${withoutLeadingZero}`;
  }

  // Prepend country code
  return `+${countryCode}${withoutLeadingZero}`;
}

/**
 * Validate if a phone number is in valid E.164 format.
 * @param phone - Phone number to validate
 * @returns true if valid E.164 format
 */
export function validatePhoneNumber(phone: string): boolean {
  if (!phone) return false;

  // Irish E.164 format: +353 followed by exactly 9 digits
  // Total length must be exactly 13 characters: +353XXXXXXXXX
  const irishE164Regex = /^\+353\d{9}$/;
  return irishE164Regex.test(phone);
}

/**
 * Format a phone number for display (more readable).
 * @param phone - Phone number in E.164 format (+353XXXXXXXXX)
 * @returns Formatted phone number: "+353 87 123 4567"
 */
export function formatPhoneDisplay(phone: string): string {
  if (!phone || !phone.startsWith("+353")) return phone;

  // Extract local number
  const { localNumber } = extractCountryCode(phone);

  // Irish format: +353 87 123 4567
  if (localNumber.length === 9) {
    return `+353 ${localNumber.slice(0, 2)} ${localNumber.slice(
      2,
      5
    )} ${localNumber.slice(5)}`;
  }

  // Fallback for invalid length
  return `+353 ${localNumber}`;
}

/**
 * Extract local number from E.164 format (+353XXXXXXXXX).
 * @param phone - Phone number in E.164 format
 * @returns Local number without country code
 */
export function extractCountryCode(phone: string): {
  countryCode: string;
  localNumber: string;
} {
  if (!phone || !phone.startsWith("+353")) {
    return { countryCode: "353", localNumber: "" };
  }

  // Remove +353 prefix
  const localNumber = phone.slice(4);
  return { countryCode: "353", localNumber };
}
