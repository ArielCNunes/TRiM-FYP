import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authApi } from "../../api/endpoints";
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
        <label className="block text-sm font-medium text-zinc-300 mb-1">
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
          className="w-full bg-zinc-900 text-white px-4 py-2 rounded-md border border-zinc-700 focus:border-indigo-500 focus:outline-none placeholder-zinc-500"
        />
        {errors.email && (
          <p className="text-red-400 text-sm mt-1">{errors.email}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-zinc-300 mb-1">
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
            className="w-full bg-zinc-900 text-white px-4 py-2 rounded-md border border-zinc-700 focus:border-indigo-500 focus:outline-none placeholder-zinc-500"
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-2.5 text-zinc-400 hover:text-white text-sm"
          >
            {showPassword ? "Hide" : "Show"}
          </button>
        </div>
        {errors.password && (
          <p className="text-red-400 text-sm mt-1">{errors.password}</p>
        )}
      </div>

      <div className="text-right">
        <Link
          to="/forgot-password"
          className="text-sm text-indigo-400 hover:text-indigo-300"
        >
          Forgot password?
        </Link>
      </div>

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:bg-zinc-700 disabled:text-zinc-400 text-white font-medium py-2 rounded-md transition shadow-lg shadow-indigo-500/20"
      >
        {loading ? "Logging in..." : "Log In"}
      </button>
    </form>
  );
}
