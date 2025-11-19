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
      <div className="flex gap-2 bg-zinc-900 p-1 rounded-lg border border-zinc-800">
        <button
          onClick={() => setActiveTab("login")}
          className={`flex-1 py-2 px-4 rounded-md font-medium transition ${
            activeTab === "login"
              ? "bg-zinc-800 text-white shadow-sm"
              : "text-zinc-400 hover:text-zinc-200"
          }`}
        >
          Log In
        </button>
        <button
          onClick={() => setActiveTab("signup")}
          className={`flex-1 py-2 px-4 rounded-md font-medium transition ${
            activeTab === "signup"
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
        {activeTab === "signup" && <SignupForm />}
      </div>
    </div>
  );
}
