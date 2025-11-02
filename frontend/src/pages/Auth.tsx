import { AuthTabs } from "../components/auth";

/**
 * Auth Page
 *
 * Root authentication page that displays login and signup forms.
 * Uses tab switcher to toggle between login and signup flows.
 */
export default function Auth() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <div className="max-w-md mx-auto px-4 py-12">
        {/* Logo and heading */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">TRiM</h1>
          <p className="text-gray-600">Book your haircut with us!</p>
        </div>

        {/* Auth tabs with forms */}
        <AuthTabs />
      </div>
    </div>
  );
}
