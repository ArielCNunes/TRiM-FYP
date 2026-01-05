import { useState } from "react";
import { LoginForm } from "./LoginForm";
import { SignupForm } from "./SignupForm";
import { BusinessSignupForm } from "./BusinessSignupForm";

type AuthTab = "login" | "signup";

/**
 * AuthTabs Component
 *
 * Displays tab switcher for login and signup forms.
 * Manages which form is currently displayed.
 * Includes option to switch to business registration.
 */
export function AuthTabs() {
  const [activeTab, setActiveTab] = useState<AuthTab>("login");
  const [showBusinessSignup, setShowBusinessSignup] = useState(false);

  // If showing business signup, render that instead
  if (showBusinessSignup) {
    return (
      <div className="bg-zinc-900 border border-zinc-800 p-6 rounded-lg shadow-xl shadow-black/20">
        <BusinessSignupForm onBack={() => setShowBusinessSignup(false)} />
      </div>
    );
  }

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
                onClick={() => setShowBusinessSignup(true)}
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
