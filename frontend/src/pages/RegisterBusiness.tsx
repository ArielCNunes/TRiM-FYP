import { useEffect } from "react";
import { BusinessSignupForm } from "../components/auth/BusinessSignupForm";

/**
 * Checks if we're on a subdomain and returns the main domain URL if so.
 * Returns null if already on the main domain.
 */
function getMainDomainUrl(): string | null {
    const { protocol, hostname, port } = window.location;
    const parts = hostname.split(".");

    // Check if we're on a subdomain
    let isSubdomain = false;
    let baseDomain: string;

    if (hostname === "localhost" || parts.length === 1) {
        // Already on main domain
        baseDomain = hostname;
        isSubdomain = false;
    } else if (parts[parts.length - 1] === "localhost") {
        // e.g., v7.localhost - this is a subdomain
        baseDomain = "localhost";
        isSubdomain = parts.length > 1 && parts[0] !== "";
    } else {
        // e.g., subdomain.example.com
        baseDomain = parts.slice(-2).join(".");
        isSubdomain = parts.length > 2;
    }

    if (!isSubdomain) {
        return null;
    }

    const portSuffix = port ? `:${port}` : "";
    return `${protocol}//${baseDomain}${portSuffix}/register-business`;
}

/**
 * RegisterBusiness Page
 *
 * Standalone page for business registration.
 * Includes a subdomain guard to redirect to main domain if accessed from a subdomain.
 */
export default function RegisterBusiness() {
    useEffect(() => {
        // Guard: If on a subdomain, redirect to main domain
        const mainDomainUrl = getMainDomainUrl();
        if (mainDomainUrl) {
            window.location.href = mainDomainUrl;
        }
    }, []);

    return (
        <div className="min-h-screen bg-zinc-950">
            <div className="max-w-md mx-auto px-4 py-12">
                {/* Logo and heading */}
                <div className="text-center mb-8">
                    <h1 className="text-4xl font-bold text-white mb-2 tracking-tight">
                        TRiM
                    </h1>
                    <p className="text-zinc-400 mb-2">Register Your Business</p>
                    <p className="text-sm text-zinc-500">
                        Create an account to manage your barbershop
                    </p>
                </div>

                {/* Business signup form */}
                <div className="bg-zinc-900 border border-zinc-800 p-6 rounded-lg shadow-xl shadow-black/20">
                    <BusinessSignupForm />
                </div>
            </div>
        </div>
    );
}
