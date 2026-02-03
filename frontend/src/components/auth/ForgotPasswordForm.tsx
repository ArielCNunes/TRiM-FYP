import { useState } from "react";
import { Link } from "react-router-dom";
import { authApi } from "../../api/endpoints";

/**
 * ForgotPasswordForm Component
 *
 * Handles password reset request by accepting user's email.
 * Sends a reset link to the provided email address.
 * Shows generic success message for security (doesn't reveal if email exists).
 */
export function ForgotPasswordForm() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  // Validate email format
  const validateEmail = () => {
    if (!email) {
      setError("Email is required");
      return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError("Please enter a valid email address");
      return false;
    }
    return true;
  };

  // Submit password reset request
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateEmail()) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      await authApi.forgotPassword({ email });
      setSubmitted(true);
    } catch (error: any) {
      // For security, we'll show a generic message even if API returns error
      // This prevents email enumeration attacks
      setSubmitted(true);
    } finally {
      setLoading(false);
    }
  };

  // Show success message after submission
  if (submitted) {
    return (
      <div className="bg-zinc-900 rounded-lg border border-zinc-800 p-8">
        <div className="text-center mb-6">
          <div className="w-16 h-16 bg-green-900/30 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg
              className="w-8 h-8 text-green-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-white mb-2">
            Check your email
          </h2>
          <p className="text-zinc-400">
            If an account exists with <strong className="text-white">{email}</strong>, you will receive
            password reset instructions shortly.
          </p>
          <p className="text-sm text-zinc-500 mt-4">
            Please check your spam folder if you don't see the email within a
            few minutes.
          </p>
        </div>

        <Link
          to="/auth"
          className="block w-full text-center bg-white hover:bg-zinc-100 text-zinc-900 font-medium py-2 rounded-md transition"
        >
          Back to Login
        </Link>
      </div>
    );
  }

  // Show email input form
  return (
    <div className="bg-zinc-900 rounded-lg border border-zinc-800 p-8">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-white mb-2">
          Forgot password?
        </h2>
        <p className="text-zinc-400">
          Enter your email address and we'll send you instructions to reset your
          password.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-zinc-300 mb-1">
            Email Address
          </label>
          <input
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (error) setError("");
            }}
            placeholder="your@email.com"
            className="w-full bg-zinc-800 text-white px-4 py-2 rounded-md border border-zinc-700 focus:border-zinc-500 focus:outline-none placeholder-zinc-500"
            disabled={loading}
          />
          {error && <p className="text-red-400 text-sm mt-1">{error}</p>}
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-white hover:bg-zinc-100 disabled:bg-zinc-600 text-zinc-900 font-medium py-2 rounded-md transition"
        >
          {loading ? "Sending..." : "Send Reset Link"}
        </button>
      </form>

      <div className="mt-6 text-center">
        <Link
          to="/auth"
          className="text-sm text-zinc-400 hover:text-white font-medium"
        >
          ← Back to Login
        </Link>
      </div>
    </div>
  );
}
