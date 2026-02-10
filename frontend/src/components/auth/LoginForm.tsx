import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authApi } from "../../api/endpoints";
import { getBusinessSlug } from "../../api/axios";
import { useAppDispatch } from "../../store/hooks";
import { setCredentials } from "../../features/auth/authSlice";

/**
 * LoginForm Component
 *
 * Handles user login with email and password validation.
 * On successful login, stores credentials in Redux and redirects to home.
 */
export function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<{ email?: string; password?: string }>(
    {}
  );
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const dispatch = useAppDispatch();

  // Lightweight client-side guardrails before hitting the API
  const validateForm = () => {
    const newErrors: typeof errors = {};

    if (!email) {
      newErrors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = "Email must be valid";
    }

    if (!password) {
      newErrors.password = "Password is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Exchange credentials for a token and persist session details
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      const response = await authApi.login({ email, password });
      const {
        token,
        id,
        email: userEmail,
        firstName,
        lastName,
        role,
        barberId,
        businessSlug,
      } = response.data;

      dispatch(
        setCredentials({
          id,
          token,
          email: userEmail,
          firstName,
          lastName,
          role,
          barberId,
          businessSlug: businessSlug ?? getBusinessSlug(),
        })
      );

      navigate("/");
    } catch (error: any) {
      const message = error.response?.data?.message || "Login failed";
      setErrors({ email: message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-[var(--text-secondary)] mb-1">
          Email
        </label>
        <input
          type="email"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
            if (errors.email) setErrors({ ...errors, email: undefined });
          }}
          placeholder="your@email.com"
          className="w-full bg-[var(--bg-surface)] text-[var(--text-primary)] px-4 py-2 rounded-md border border-[var(--border-default)] focus:border-[var(--focus-ring)] focus:outline-none placeholder-[var(--text-subtle)]"
        />
        {errors.email && (
          <p className="text-[var(--danger-text)] text-sm mt-1">{errors.email}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-[var(--text-secondary)] mb-1">
          Password
        </label>
        <div className="relative">
          <input
            type={showPassword ? "text" : "password"}
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              if (errors.password)
                setErrors({ ...errors, password: undefined });
            }}
            placeholder="••••••••"
            className="w-full bg-[var(--bg-surface)] text-[var(--text-primary)] px-4 py-2 rounded-md border border-[var(--border-default)] focus:border-[var(--focus-ring)] focus:outline-none placeholder-[var(--text-subtle)]"
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-2.5 text-[var(--text-muted)] hover:text-[var(--text-primary)] text-sm"
          >
            {showPassword ? "Hide" : "Show"}
          </button>
        </div>
        {errors.password && (
          <p className="text-[var(--danger-text)] text-sm mt-1">{errors.password}</p>
        )}
      </div>

      <div className="text-right">
        <Link
          to="/forgot-password"
          className="text-sm text-[var(--accent-text)] hover:text-[var(--accent-text-light)]"
        >
          Forgot password?
        </Link>
      </div>

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-[var(--accent)] hover:bg-[var(--accent-hover)] disabled:bg-[var(--bg-muted)] disabled:text-[var(--text-muted)] text-[var(--text-primary)] font-medium py-2 rounded-md transition shadow-lg shadow-[var(--accent-shadow)]"
      >
        {loading ? "Logging in..." : "Log In"}
      </button>
    </form>
  );
}
