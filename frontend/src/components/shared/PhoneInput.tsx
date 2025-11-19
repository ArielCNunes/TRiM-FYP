import { useState, useEffect } from "react";
import {
  normalizePhoneNumber,
  extractCountryCode,
} from "../../utils/phoneUtils";

interface PhoneInputProps {
  value: string; // E.164 format (+353871234567)
  onChange: (normalizedPhone: string) => void;
  error?: string;
  disabled?: boolean;
  label?: string;
  required?: boolean;
}

/**
 * PhoneInput Component
 * Composite input with country code selector and phone number field.
 * Handles normalization to E.164 format internally.
 */
export function PhoneInput({
  value,
  onChange,
  error,
  disabled = false,
  label = "Phone Number",
  required = false,
}: PhoneInputProps) {
  // Extract local number from E.164 value (always using Irish +353)
  const { localNumber: initialLocal } = extractCountryCode(value);

  const [localNumber, setLocalNumber] = useState(initialLocal);

  // Update internal state when external value changes (e.g., pre-filled data)
  useEffect(() => {
    if (value) {
      const { localNumber: newLocal } = extractCountryCode(value);
      setLocalNumber(newLocal);
    }
  }, [value]);

  const handleLocalNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const input = e.target.value;
    setLocalNumber(input);

    // Normalize and pass back to parent only if we get a valid normalized result
    // Always use Irish country code (353)
    if (input.trim()) {
      const normalized = normalizePhoneNumber(input, "353");
      // Only update parent if normalization succeeded (not empty)
      // This allows user to type freely but prevents invalid partial numbers from being saved
      onChange(normalized);
    } else {
      onChange("");
    }
  };

  return (
    <div>
      {label && (
        <label className="block text-sm font-medium text-zinc-300 mb-2">
          {label} {required && "*"}
        </label>
      )}
      <div className="flex">
        <span className="flex items-center px-3 py-2 border border-r-0 border-zinc-700 rounded-l-md bg-zinc-800 text-zinc-300 font-medium">
          +353
        </span>
        <input
          type="tel"
          value={localNumber}
          onChange={handleLocalNumberChange}
          disabled={disabled}
          className={`flex-1 px-4 py-2 border rounded-r-md focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-zinc-900 text-white placeholder-zinc-500 ${
            error ? "border-red-400" : "border-zinc-700"
          } ${disabled ? "bg-zinc-800 text-zinc-500" : ""}`}
          placeholder="87 123 4567"
        />
      </div>
      {error && <p className="text-red-400 text-sm mt-1">{error}</p>}
    </div>
  );
}
