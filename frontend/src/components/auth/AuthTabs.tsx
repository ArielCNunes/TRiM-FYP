import { useState } from "react";
import { LoginForm } from "./LoginForm";
import { SignupForm } from "./SignupForm";

type AuthTab = "login" | "signup";

/**
 * AuthTabs Component
 *
 * Displays tab switcher for login and signup forms.
 * Manages which form is currently displayed.
 */
export function AuthTabs() {
  const [activeTab, setActiveTab] = useState<AuthTab>("login");

  return (
    <div className="space-y-6">
      {/* Tab switcher */}
      <div className="flex gap-2 bg-gray-100 p-1 rounded-lg">
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
  );
}
