import { useState } from "react";
import { LoginForm } from "./LoginForm";
import { SignupForm } from "./SignupForm";

type AuthTab = "login" | "signup";

/**
 * Redirects to the main domain for business registration.
 * Strips any subdomain to ensure clean tenant context.
 */
const handleBusinessSignupClick = () => {
  const { protocol, hostname, port } = window.location;
  const parts = hostname.split(".");

  // Get base domain (strip any subdomain)
  let baseDomain: string;
  if (hostname === "localhost" || parts.length === 1) {
    baseDomain = hostname;
  } else if (parts[parts.length - 1] === "localhost") {
    baseDomain = "localhost";
  } else {
    baseDomain = parts.slice(-2).join(".");
  }

  const portSuffix = port ? `:${port}` : "";
  window.location.href = `${protocol}//${baseDomain}${portSuffix}/register-business`;
};

/**
 * AuthTabs Component
 *
 * Displays tab switcher for login and signup forms.
 * Manages which form is currently displayed.
 * Includes option to switch to business registration.
 */
export function AuthTabs() {
  const [activeTab, setActiveTab] = useState<AuthTab>("login");

  return (
    <div className="space-y-6">
      {/* Tab switcher */}
      <div className="flex gap-2 bg-zinc-900 p-1 rounded-lg border border-zinc-800">
        <button
          onClick={() => setActiveTab("login")}
          className={`flex-1 py-2 px-4 rounded-md font-medium transition ${activeTab === "login"
            ? "bg-zinc-800 text-white shadow-sm"
            : "text-zinc-400 hover:text-zinc-200"
            }`}
        >
          Log In
        </button>
        <button
          onClick={() => setActiveTab("signup")}
          className={`flex-1 py-2 px-4 rounded-md font-medium transition ${activeTab === "signup"
            ? "bg-zinc-800 text-white shadow-sm"
            : "text-zinc-400 hover:text-zinc-200"
            }`}
        >
          Sign Up
        </button>
      </div>

      {/* Forms */}
      <div className="bg-zinc-900 border border-zinc-800 p-6 rounded-lg shadow-xl shadow-black/20">
        {activeTab === "login" && <LoginForm />}
        {activeTab === "signup" && (
          <>
            <SignupForm />
            <div className="mt-6 pt-4 border-t border-zinc-800 text-center">
              <p className="text-sm text-zinc-500 mb-2">Are you a business owner?</p>
              <button
                onClick={handleBusinessSignupClick}
                className="text-indigo-400 hover:text-indigo-300 text-sm font-medium"
              >
                Register your business here →
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
