import { AuthTabs } from "../components/auth";

/**
 * Auth Page
 *
 * Root authentication page that displays login and signup forms.
 * Uses tab switcher to toggle between login and signup flows.
 */
export default function Auth() {
  return (
    <div className="min-h-screen bg-[var(--bg-base)]">
      <div className="max-w-md mx-auto px-4 py-12">
        {/* Logo and heading */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-[var(--text-primary)] mb-2 tracking-tight">
            TRiM
          </h1>
          <p className="text-[var(--text-muted)] mb-2">Book your haircut with us!</p>
          <p className="text-sm text-[var(--text-subtle)]">
            Log in to manage your bookings and view appointment history
          </p>
        </div>

        {/* Auth tabs with forms */}
        <AuthTabs />
      </div>
    </div>
  );
}
