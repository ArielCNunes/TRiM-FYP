import { useState } from "react";
import { useNavigate } from "react-router-dom";
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
        <label className="block text-sm font-medium text-gray-900 mb-1">
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
          className="w-full bg-gray-50 text-gray-900 px-4 py-2 rounded-md border border-gray-300 focus:border-blue-500 focus:outline-none"
        />
        {errors.email && (
          <p className="text-red-600 text-sm mt-1">{errors.email}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-900 mb-1">
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
            className="w-full bg-gray-50 text-gray-900 px-4 py-2 rounded-md border border-gray-300 focus:border-blue-500 focus:outline-none"
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-2.5 text-gray-600 hover:text-gray-900"
          >
            {showPassword ? "Hide" : "Show"}
          </button>
        </div>
        {errors.password && (
          <p className="text-red-600 text-sm mt-1">{errors.password}</p>
        )}
      </div>

      <div className="text-right">
        <a href="#" className="text-sm text-blue-600 hover:text-blue-700">
          Forgot password?
        </a>
      </div>

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-medium py-2 rounded-md transition"
      >
        {loading ? "Logging in..." : "Log In"}
      </button>
    </form>
  );
}
