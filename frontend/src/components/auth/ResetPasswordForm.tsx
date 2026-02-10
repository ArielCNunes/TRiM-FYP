import { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { authApi } from "../../api/endpoints";

/**
 * ResetPasswordForm Component
 *
 * Handles password reset with token validation.
 * Validates the reset token on mount and allows user to set new password.
 * Includes password strength requirements and confirmation matching.
 */
export function ResetPasswordForm() {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [validatingToken, setValidatingToken] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<{
    password?: string;
    confirmPassword?: string;
    general?: string;
  }>({});
  const [success, setSuccess] = useState(false);

  // Validate token on component mount
  useEffect(() => {
    const validateToken = async () => {
      if (!token) {
        setValidatingToken(false);
        setTokenValid(false);
        return;
      }

      try {
        await authApi.validateResetToken(token);
        setTokenValid(true);
      } catch (error) {
        setTokenValid(false);
        setErrors({
          general: "This reset link is invalid or has expired.",
        });
      } finally {
        setValidatingToken(false);
      }
    };

    validateToken();
  }, [token]);

  // Validate password requirements
  const validatePassword = () => {
    const newErrors: typeof errors = {};

    if (!newPassword) {
      newErrors.password = "Password is required";
    } else if (newPassword.length < 8) {
      newErrors.password = "Password must be at least 8 characters";
    } else if (!/(?=.*[a-z])/.test(newPassword)) {
      newErrors.password =
        "Password must contain at least one lowercase letter";
    } else if (!/(?=.*[A-Z])/.test(newPassword)) {
      newErrors.password =
        "Password must contain at least one uppercase letter";
    } else if (!/(?=.*\d)/.test(newPassword)) {
      newErrors.password = "Password must contain at least one number";
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = "Please confirm your password";
    } else if (newPassword !== confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Submit new password
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validatePassword() || !token) {
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      await authApi.resetPassword({
        token,
        newPassword,
      });
      setSuccess(true);

      // Redirect to login after 3 seconds
      setTimeout(() => {
        navigate("/auth", {
          state: { message: "Password reset successful! Please log in." },
        });
      }, 3000);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        "Failed to reset password. Please try again.";
      setErrors({ general: message });
    } finally {
      setLoading(false);
    }
  };

  // Show loading state while validating token
  if (validatingToken) {
    return (
      <div className="bg-[var(--bg-surface)] rounded-lg border border-[var(--border-subtle)] p-8">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[var(--text-primary)] mx-auto mb-4"></div>
          <p className="text-[var(--text-muted)]">Validating reset link...</p>
        </div>
      </div>
    );
  }

  // Show error if token is invalid
  if (!tokenValid) {
    return (
      <div className="bg-[var(--bg-surface)] rounded-lg border border-[var(--border-subtle)] p-8">
        <div className="text-center">
          <div className="w-16 h-16 bg-[var(--danger-muted)] rounded-full flex items-center justify-center mx-auto mb-4">
            <svg
              className="w-8 h-8 text-[var(--danger-text)]"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-[var(--text-primary)] mb-2">
            Invalid Reset Link
          </h2>
          <p className="text-[var(--text-muted)] mb-6">{errors.general}</p>
          <div className="space-y-3">
            <Link
              to="/forgot-password"
              className="block w-full text-center bg-white hover:bg-zinc-100 text-zinc-900 font-medium py-2 rounded-md transition"
            >
              Request New Reset Link
            </Link>
            <Link
              to="/auth"
              className="block w-full text-center text-[var(--text-muted)] hover:text-[var(--text-primary)] font-medium py-2"
            >
              Back to Login
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // Show success message
  if (success) {
    return (
      <div className="bg-[var(--bg-surface)] rounded-lg border border-[var(--border-subtle)] p-8">
        <div className="text-center">
          <div className="w-16 h-16 bg-[var(--success-muted)] rounded-full flex items-center justify-center mx-auto mb-4">
            <svg
              className="w-8 h-8 text-[var(--green-text)]"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5 13l4 4L19 7"
              />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-[var(--text-primary)] mb-2">
            Password Reset Successful!
          </h2>
          <p className="text-[var(--text-muted)] mb-4">
            Your password has been successfully reset.
          </p>
          <p className="text-sm text-[var(--text-subtle)]">Redirecting to login page...</p>
        </div>
      </div>
    );
  }

  // Show password reset form
  return (
    <div className="bg-[var(--bg-surface)] rounded-lg border border-[var(--border-subtle)] p-8">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-[var(--text-primary)] mb-2">
          Reset your password
        </h2>
        <p className="text-[var(--text-muted)]">Please enter your new password below.</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Password Requirements Info */}
        <div className="bg-[var(--bg-elevated)] border border-[var(--border-default)] rounded-md p-3 mb-4">
          <p className="text-sm font-medium text-[var(--text-secondary)] mb-2">
            Password Requirements:
          </p>
          <ul className="text-xs text-[var(--text-muted)] space-y-1">
            <li>• At least 8 characters long</li>
            <li>• Contains uppercase and lowercase letters</li>
            <li>• Contains at least one number</li>
          </ul>
        </div>

        {/* New Password Input */}
        <div>
          <label className="block text-sm font-medium text-[var(--text-secondary)] mb-1">
            New Password
          </label>
          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              value={newPassword}
              onChange={(e) => {
                setNewPassword(e.target.value);
                if (errors.password)
                  setErrors({ ...errors, password: undefined });
              }}
              placeholder="••••••••"
              className="w-full bg-[var(--bg-elevated)] text-[var(--text-primary)] px-4 py-2 rounded-md border border-[var(--border-default)] focus:border-[var(--border-strong)] focus:outline-none placeholder-[var(--text-subtle)]"
              disabled={loading}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-2.5 text-[var(--text-muted)] hover:text-[var(--text-primary)]"
            >
              {showPassword ? "Hide" : "Show"}
            </button>
          </div>
          {errors.password && (
            <p className="text-[var(--danger-text)] text-sm mt-1">{errors.password}</p>
          )}
        </div>

        {/* Confirm Password Input */}
        <div>
          <label className="block text-sm font-medium text-[var(--text-secondary)] mb-1">
            Confirm Password
          </label>
          <div className="relative">
            <input
              type={showConfirmPassword ? "text" : "password"}
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value);
                if (errors.confirmPassword)
                  setErrors({ ...errors, confirmPassword: undefined });
              }}
              placeholder="••••••••"
              className="w-full bg-[var(--bg-elevated)] text-[var(--text-primary)] px-4 py-2 rounded-md border border-[var(--border-default)] focus:border-[var(--border-strong)] focus:outline-none placeholder-[var(--text-subtle)]"
              disabled={loading}
            />
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-3 top-2.5 text-[var(--text-muted)] hover:text-[var(--text-primary)]"
            >
              {showConfirmPassword ? "Hide" : "Show"}
            </button>
          </div>
          {errors.confirmPassword && (
            <p className="text-[var(--danger-text)] text-sm mt-1">
              {errors.confirmPassword}
            </p>
          )}
        </div>

        {/* General Error Message */}
        {errors.general && (
          <div className="bg-[var(--danger-muted)] border border-[var(--danger-border)] rounded-md p-3">
            <p className="text-[var(--danger-text)] text-sm">{errors.general}</p>
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-white hover:bg-zinc-100 disabled:bg-zinc-600 text-zinc-900 font-medium py-2 rounded-md transition"
        >
          {loading ? "Resetting Password..." : "Reset Password"}
        </button>
      </form>

      <div className="mt-6 text-center">
        <Link
          to="/auth"
          className="text-sm text-[var(--text-muted)] hover:text-[var(--text-primary)] font-medium"
        >
          ← Back to Login
        </Link>
      </div>
    </div>
  );
}
