import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../../api/endpoints";
import { getBusinessSlug } from "../../api/axios";
import { useAppDispatch } from "../../store/hooks";
import { setCredentials } from "../../features/auth/authSlice";
import { PhoneInput } from "../shared/PhoneInput";
import { validatePhoneNumber } from "../../utils/phoneUtils";

type SignupErrors = {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  password?: string;
  confirmPassword?: string;
};

/**
 * SignupForm Component
 *
 * Handles user registration with comprehensive validation.
 * After successful registration, automatically logs in the user.
 */
export function SignupForm() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState<SignupErrors>({});
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const dispatch = useAppDispatch();

  // Covers basic registration rules (email format, phone length, matching passwords)
  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!firstName) newErrors.firstName = "First name is required";
    if (!lastName) newErrors.lastName = "Last name is required";

    if (!email) {
      newErrors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = "Email must be valid";
    }

    if (!phone) {
      newErrors.phone = "Phone is required";
    } else if (!validatePhoneNumber(phone)) {
      newErrors.phone = "Phone must be a valid international number";
    }

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

  // Register then immediately authenticate so the user lands in the app signed-in
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      await authApi.register({
        firstName,
        lastName,
        email,
        phone,
        password,
      });

      const loginResponse = await authApi.login({ email, password });
      const {
        token,
        id,
        email: loginEmail,
        firstName: loginFirstName,
        lastName: loginLastName,
        role,
        barberId,
        businessSlug,
      } = loginResponse.data;

      dispatch(
        setCredentials({
          id,
          token,
          email: loginEmail,
          firstName: loginFirstName,
          lastName: loginLastName,
          role,
          barberId,
          businessSlug: businessSlug ?? getBusinessSlug(),
        })
      );

      navigate("/");
    } catch (error: any) {
      const message = error.response?.data?.message || "Registration failed";
      setErrors({ email: message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-zinc-300 mb-1">
            First Name
          </label>
          <input
            type="text"
            value={firstName}
            onChange={(e) => {
              setFirstName(e.target.value);
              if (errors.firstName)
                setErrors({ ...errors, firstName: undefined });
            }}
            placeholder="John"
            className="w-full bg-zinc-900 text-white px-4 py-2 rounded-md border border-zinc-700 focus:border-indigo-500 focus:outline-none placeholder-zinc-500"
          />
          {errors.firstName && (
            <p className="text-red-400 text-sm mt-1">{errors.firstName}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-zinc-300 mb-1">
            Last Name
          </label>
          <input
            type="text"
            value={lastName}
            onChange={(e) => {
              setLastName(e.target.value);
              if (errors.lastName)
                setErrors({ ...errors, lastName: undefined });
            }}
            placeholder="Doe"
            className="w-full bg-zinc-900 text-white px-4 py-2 rounded-md border border-zinc-700 focus:border-indigo-500 focus:outline-none placeholder-zinc-500"
          />
          {errors.lastName && (
            <p className="text-red-400 text-sm mt-1">{errors.lastName}</p>
          )}
        </div>
      </div>

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

      <PhoneInput
        value={phone}
        onChange={(normalizedPhone) => {
          setPhone(normalizedPhone);
          if (errors.phone) setErrors({ ...errors, phone: undefined });
        }}
        error={errors.phone}
        label="Phone"
        required
      />

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

      <div>
        <label className="block text-sm font-medium text-zinc-300 mb-1">
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
            className="w-full bg-zinc-900 text-white px-4 py-2 rounded-md border border-zinc-700 focus:border-indigo-500 focus:outline-none placeholder-zinc-500"
          />
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-3 top-2.5 text-zinc-400 hover:text-white text-sm"
          >
            {showConfirmPassword ? "Hide" : "Show"}
          </button>
        </div>
        {errors.confirmPassword && (
          <p className="text-red-400 text-sm mt-1">{errors.confirmPassword}</p>
        )}
      </div>

      <p className="text-xs text-zinc-500">
        By signing up, you agree to our Terms of Service and Privacy Policy
      </p>

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:bg-zinc-700 disabled:text-zinc-400 text-white font-medium py-2 rounded-md transition shadow-lg shadow-indigo-500/20"
      >
        {loading ? "Creating account..." : "Create Account"}
      </button>
    </form>
  );
}
