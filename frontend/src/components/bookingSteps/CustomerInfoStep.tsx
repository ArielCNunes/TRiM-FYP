import { useState } from "react";
import { StatusBanner } from "../BookingComponents";
import { PhoneInput } from "../shared/PhoneInput";
import { validatePhoneNumber } from "../../utils/phoneUtils";

interface CustomerInfo {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

interface CustomerInfoStepProps {
  initialData?: CustomerInfo;
  onSubmit: (data: CustomerInfo) => void;
  status: { type: "success" | "error"; message: string } | null;
  submitting: boolean;
  onBack: () => void;
}

/**
 * CustomerInfoStep Component
 * Step 4 of booking wizard: collect or confirm customer information
 *
 * If user is logged in: fields are pre-populated and read-only
 * If guest: fields are empty and required
 */
export function CustomerInfoStep({
  initialData,
  onSubmit,
  status,
  submitting,
  onBack,
}: CustomerInfoStepProps) {
  const isPreFilled = !!initialData;

  const [formData, setFormData] = useState<CustomerInfo>(
    initialData || {
      firstName: "",
      lastName: "",
      email: "",
      phone: "",
    }
  );

  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = "First name is required";
    } else if (formData.firstName.trim().length < 2) {
      newErrors.firstName = "First name must be at least 2 characters";
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = "Last name is required";
    } else if (formData.lastName.trim().length < 2) {
      newErrors.lastName = "Last name must be at least 2 characters";
    }

    if (!formData.email.trim()) {
      newErrors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email";
    }

    if (!formData.phone.trim()) {
      newErrors.phone = "Phone is required";
    } else if (!validatePhoneNumber(formData.phone)) {
      newErrors.phone = "Phone must be a valid international number";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (field: keyof CustomerInfo, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8 text-white">Your Information</h1>
      <StatusBanner status={status} />

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* First Name */}
        <div>
          <label className="block text-sm font-medium text-zinc-300 mb-2">
            First Name *
          </label>
          <input
            type="text"
            value={formData.firstName}
            onChange={(e) => handleChange("firstName", e.target.value)}
            disabled={isPreFilled || submitting}
            className={`w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-zinc-900 text-white placeholder-zinc-500 ${
              errors.firstName ? "border-red-400" : "border-zinc-700"
            } ${isPreFilled ? "bg-zinc-800 text-zinc-500" : ""}`}
            placeholder="John"
          />
          {errors.firstName && (
            <p className="text-red-400 text-sm mt-1">{errors.firstName}</p>
          )}
        </div>

        {/* Last Name */}
        <div>
          <label className="block text-sm font-medium text-zinc-300 mb-2">
            Last Name *
          </label>
          <input
            type="text"
            value={formData.lastName}
            onChange={(e) => handleChange("lastName", e.target.value)}
            disabled={isPreFilled || submitting}
            className={`w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-zinc-900 text-white placeholder-zinc-500 ${
              errors.lastName ? "border-red-400" : "border-zinc-700"
            } ${isPreFilled ? "bg-zinc-800 text-zinc-500" : ""}`}
            placeholder="Doe"
          />
          {errors.lastName && (
            <p className="text-red-400 text-sm mt-1">{errors.lastName}</p>
          )}
        </div>

        {/* Email */}
        <div>
          <label className="block text-sm font-medium text-zinc-300 mb-2">
            Email *
          </label>
          <input
            type="email"
            value={formData.email}
            onChange={(e) => handleChange("email", e.target.value)}
            disabled={isPreFilled || submitting}
            className={`w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-zinc-900 text-white placeholder-zinc-500 ${
              errors.email ? "border-red-400" : "border-zinc-700"
            } ${isPreFilled ? "bg-zinc-800 text-zinc-500" : ""}`}
            placeholder="john@example.com"
          />
          {errors.email && (
            <p className="text-red-400 text-sm mt-1">{errors.email}</p>
          )}
        </div>

        {/* Phone */}
        <PhoneInput
          value={formData.phone}
          onChange={(normalizedPhone) => handleChange("phone", normalizedPhone)}
          error={errors.phone}
          disabled={isPreFilled || submitting}
          label="Phone Number"
          required
        />

        {/* Info Banner for Pre-filled Users */}
        {isPreFilled && (
          <div className="bg-indigo-900/20 border border-indigo-800 rounded-md p-4">
            <p className="text-sm text-indigo-300">
              Your information is pre-filled from your account. You cannot edit
              it here.
            </p>
          </div>
        )}

        {/* Buttons */}
        <div className="flex gap-4 pt-4">
          <button
            type="button"
            onClick={onBack}
            disabled={submitting}
            className="px-6 py-2 border border-zinc-700 rounded-md hover:bg-zinc-800 text-zinc-300 disabled:opacity-50"
          >
            Back
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="flex-1 px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-500 disabled:opacity-50 flex items-center justify-center gap-2 shadow-lg shadow-indigo-500/20"
          >
            {submitting ? (
              <>
                <span className="animate-spin">⏳</span>
                Processing...
              </>
            ) : (
              "Continue to Payment"
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
