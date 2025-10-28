import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../api/endpoints";
import { useAppDispatch } from "../store/hooks";
import { setCredentials } from "../features/auth/authSlice";

type AuthTab = "login" | "signup";

/** Root auth screen toggles between login and signup flows. */
export default function Auth() {
  const [activeTab, setActiveTab] = useState<AuthTab>("login");

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <div className="max-w-md mx-auto px-4 py-12">
        {/* Logo and heading */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">TRiM</h1>
          <p className="text-gray-600">
            Book your haircut with us!
          </p>
        </div>

        {/* Tab switcher */}
        <div className="flex gap-2 mb-6 bg-gray-100 p-1 rounded-lg">
          <button
            onClick={() => setActiveTab("login")}
            className={`flex-1 py-2 px-4 rounded-md font-medium transition ${
              activeTab === "login"
                ? "bg-white text-gray-900 shadow-sm"
                : "text-gray-600 hover:text-gray-900"
            }`}
          >
            Log In
          </button>
          <button
            onClick={() => setActiveTab("signup")}
            className={`flex-1 py-2 px-4 rounded-md font-medium transition ${
              activeTab === "signup"
                ? "bg-white text-gray-900 shadow-sm"
                : "text-gray-600 hover:text-gray-900"
            }`}
          >
            Sign Up
          </button>
        </div>

        {/* Forms */}
        <div className="bg-white border border-gray-200 p-6 rounded-lg shadow-sm">
          {activeTab === "login" && <LoginForm />}
          {activeTab === "signup" && <SignupForm />}
        </div>
      </div>
    </div>
  );
}

function LoginForm() {
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

type SignupErrors = {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  password?: string;
  confirmPassword?: string;
};

function SignupForm() {
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
    } else if (!/^[0-9]{10,15}$/.test(phone.replace(/\D/g, ""))) {
      newErrors.phone = "Phone must be 10-15 digits";
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
          <label className="block text-sm font-medium text-gray-900 mb-1">
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
            className="w-full bg-gray-50 text-gray-900 px-4 py-2 rounded-md border border-gray-300 focus:border-blue-500 focus:outline-none"
          />
          {errors.firstName && (
            <p className="text-red-600 text-sm mt-1">{errors.firstName}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-900 mb-1">
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
            className="w-full bg-gray-50 text-gray-900 px-4 py-2 rounded-md border border-gray-300 focus:border-blue-500 focus:outline-none"
          />
          {errors.lastName && (
            <p className="text-red-600 text-sm mt-1">{errors.lastName}</p>
          )}
        </div>
      </div>

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
          Phone
        </label>
        <input
          type="tel"
          value={phone}
          onChange={(e) => {
            setPhone(e.target.value);
            if (errors.phone) setErrors({ ...errors, phone: undefined });
          }}
          placeholder="1234567890"
          className="w-full bg-gray-50 text-gray-900 px-4 py-2 rounded-md border border-gray-300 focus:border-blue-500 focus:outline-none"
        />
        {errors.phone && (
          <p className="text-red-600 text-sm mt-1">{errors.phone}</p>
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

      <div>
        <label className="block text-sm font-medium text-gray-900 mb-1">
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
            className="w-full bg-gray-50 text-gray-900 px-4 py-2 rounded-md border border-gray-300 focus:border-blue-500 focus:outline-none"
          />
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-3 top-2.5 text-gray-600 hover:text-gray-900"
          >
            {showConfirmPassword ? "Hide" : "Show"}
          </button>
        </div>
        {errors.confirmPassword && (
          <p className="text-red-600 text-sm mt-1">{errors.confirmPassword}</p>
        )}
      </div>

      <p className="text-xs text-gray-600">
        By signing up, you agree to our Terms of Service and Privacy Policy
      </p>

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-medium py-2 rounded-md transition"
      >
        {loading ? "Creating account..." : "Create Account"}
      </button>
    </form>
  );
}
