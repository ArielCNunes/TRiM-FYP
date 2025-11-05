import { useState } from "react";
import { StatusBanner } from "../BookingComponents";

interface SaveAccountDecisionStepProps {
  customerEmail: string;
  guestUserId: number;
  onSave: (userId: number, password: string) => Promise<boolean>;
  onSkip: () => void;
  status: { type: "success" | "error"; message: string } | null;
  submitting: boolean;
}

/**
 * SaveAccountDecisionStep Component
 * Shown after successful booking payment
 * User decides whether to save their account or continue as guest
 */
export function SaveAccountDecisionStep({
  customerEmail,
  guestUserId,
  onSave,
  onSkip,
  status,
  submitting,
}: SaveAccountDecisionStepProps) {
  const [selectedOption, setSelectedOption] = useState<"save" | "skip">("skip");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validatePasswords = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!password) {
      newErrors.password = "Password is required";
    } else if (password.length < 8) {
      newErrors.password = "Password must be at least 8 characters";
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = "Please confirm your password";
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSaveAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validatePasswords()) return;

    const success = await onSave(guestUserId, password);
    if (success) {
      // Redirect happens in parent component
      setTimeout(() => {
        window.location.href = "/auth?message=account-created";
      }, 1500);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8">🎉 Booking Confirmed!</h1>
      <StatusBanner status={status} />

      {/* Confirmation Message */}
      <div className="bg-green-50 border border-green-200 rounded-lg p-6 mb-8">
        <p className="text-green-800 font-medium">
          Your booking has been confirmed! A confirmation email has been sent to{" "}
          <span className="font-bold">{customerEmail}</span>
        </p>
      </div>

      {/* Save Account Decision */}
      <div className="border border-gray-200 rounded-lg p-6 mb-8">
        <h2 className="text-xl font-bold mb-6">Save Your Account?</h2>

        {/* Option 1: Save Account */}
        <div className="mb-6">
          <label className="flex items-start cursor-pointer">
            <input
              type="radio"
              name="account-decision"
              value="save"
              checked={selectedOption === "save"}
              onChange={() => setSelectedOption("save")}
              className="mt-1 mr-3"
              disabled={submitting}
            />
            <div className="flex-1">
              <p className="font-medium text-gray-800">Save My Account</p>
              <p className="text-sm text-gray-600">
                Create a password to save your account. You can use it to log in and view your bookings anytime.
              </p>
            </div>
          </label>
        </div>

        {/* Password Fields - Show Only if "Save" Selected */}
        {selectedOption === "save" && (
          <form onSubmit={handleSaveAccount} className="bg-gray-50 rounded-lg p-4 mb-6 space-y-4">
            {/* Password */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Password *
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (errors.password) {
                    setErrors(prev => {
                      const newErrors = { ...prev };
                      delete newErrors.password;
                      return newErrors;
                    });
                  }
                }}
                disabled={submitting}
                className={`w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.password ? "border-red-500" : "border-gray-300"
                }`}
                placeholder="Min. 8 characters"
              />
              {errors.password && (
                <p className="text-red-500 text-sm mt-1">{errors.password}</p>
              )}
            </div>

            {/* Confirm Password */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Confirm Password *
              </label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => {
                  setConfirmPassword(e.target.value);
                  if (errors.confirmPassword) {
                    setErrors(prev => {
                      const newErrors = { ...prev };
                      delete newErrors.confirmPassword;
                      return newErrors;
                    });
                  }
                }}
                disabled={submitting}
                className={`w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.confirmPassword ? "border-red-500" : "border-gray-300"
                }`}
                placeholder="Confirm password"
              />
              {errors.confirmPassword && (
                <p className="text-red-500 text-sm mt-1">{errors.confirmPassword}</p>
              )}
            </div>

            {/* Save Button */}
            <button
              type="submit"
              disabled={submitting}
              className="w-full px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {submitting ? (
                <>
                  <span className="animate-spin">⏳</span>
                  Saving...
                </>
              ) : (
                "Save Account"
              )}
            </button>
          </form>
        )}

        {/* Option 2: Skip for Now */}
        <div>
          <label className="flex items-start cursor-pointer">
            <input
              type="radio"
              name="account-decision"
              value="skip"
              checked={selectedOption === "skip"}
              onChange={() => setSelectedOption("skip")}
              className="mt-1 mr-3"
              disabled={submitting}
            />
            <div className="flex-1">
              <p className="font-medium text-gray-800">Not Now</p>
              <p className="text-sm text-gray-600">
                Continue as a guest. You can access your booking via the email link sent to you.
              </p>
            </div>
          </label>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-4">
        {selectedOption === "skip" && (
          <button
            onClick={onSkip}
            disabled={submitting}
            className="flex-1 px-6 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 disabled:opacity-50"
          >
            Continue as Guest
          </button>
        )}
      </div>
    </div>
  );
}
