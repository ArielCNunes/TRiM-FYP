import { useState } from "react";
import { authApi } from "../../api/endpoints";
import { PhoneInput } from "../shared/PhoneInput";
import { validatePhoneNumber } from "../../utils/phoneUtils";

type BusinessSignupErrors = {
    businessName?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    phone?: string;
    password?: string;
    confirmPassword?: string;
};

interface BusinessSignupFormProps {
    onBack: () => void;
}

/**
 * BusinessSignupForm Component
 *
 * Handles business/admin registration with comprehensive validation.
 * After successful registration, automatically logs in the user.
 */
export function BusinessSignupForm({ onBack }: BusinessSignupFormProps) {
    const [businessName, setBusinessName] = useState("");
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [errors, setErrors] = useState<BusinessSignupErrors>({});
    const [loading, setLoading] = useState(false);

    const validateForm = () => {
        const newErrors: Record<string, string> = {};

        if (!businessName) newErrors.businessName = "Business name is required";
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

    /**
     * Build the redirect URL for the business subdomain.
     * Uses exchange token for secure cross-subdomain auth (Not the actual JWT).
     */
    const getBusinessSubdomainUrl = (
        businessSlug: string,
        exchangeToken: string
    ): string => {
        const { protocol, hostname, port } = window.location;
        const parts = hostname.split(".");

        // Determine the base domain
        let baseDomain: string;
        if (hostname === "localhost" || parts.length === 1) {
            baseDomain = hostname;
        } else if (parts.length >= 2 && parts[parts.length - 1] === "localhost") {
            baseDomain = "localhost";
        } else {
            baseDomain = parts.slice(-2).join(".");
        }

        const portSuffix = port ? `:${port}` : "";
        const baseUrl = `${protocol}//${businessSlug}.${baseDomain}${portSuffix}/admin`;

        // Use exchange token only - will be exchanged for JWT on arrival
        // This is secure: token is single-use and expires in 60 seconds
        return `${baseUrl}?exchangeToken=${encodeURIComponent(exchangeToken)}`;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        setLoading(true);
        try {
            // Register admin, returns exchangeToken instead of JWT
            const response = await authApi.registerAdmin({
                businessName,
                firstName,
                lastName,
                email,
                phone,
                password,
            });

            const { exchangeToken, businessSlug } = response.data;

            // Redirect to business subdomain admin page with exchange token only
            // The actual JWT is retrieved securely via POST on the target domain
            window.location.href = getBusinessSubdomainUrl(businessSlug, exchangeToken);
        } catch (error: any) {
            const message = error.response?.data?.message || "Registration failed";
            setErrors({ email: message });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <button
                onClick={onBack}
                className="text-zinc-400 hover:text-white text-sm mb-4 flex items-center gap-1"
            >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
                Back to customer signup
            </button>

            <div className="mb-6">
                <h3 className="text-lg font-semibold text-white">Register Your Business</h3>
                <p className="text-sm text-zinc-400">Create an admin account to manage your barbershop</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-zinc-300 mb-1">
                        Business Name
                    </label>
                    <input
                        type="text"
                        value={businessName}
                        onChange={(e) => {
                            setBusinessName(e.target.value);
                            if (errors.businessName) setErrors({ ...errors, businessName: undefined });
                        }}
                        placeholder="John's Barbershop"
                        className="w-full bg-zinc-900 text-white px-4 py-2 rounded-md border border-zinc-700 focus:border-indigo-500 focus:outline-none placeholder-zinc-500"
                    />
                    {errors.businessName && (
                        <p className="text-red-400 text-sm mt-1">{errors.businessName}</p>
                    )}
                </div>

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
                                if (errors.firstName) setErrors({ ...errors, firstName: undefined });
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
                                if (errors.lastName) setErrors({ ...errors, lastName: undefined });
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
                                if (errors.password) setErrors({ ...errors, password: undefined });
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
                                if (errors.confirmPassword) setErrors({ ...errors, confirmPassword: undefined });
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
                    {loading ? "Creating account..." : "Register Business"}
                </button>
            </form>
        </div>
    );
}
