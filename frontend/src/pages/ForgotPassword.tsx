import { ForgotPasswordForm } from "../components/auth/ForgotPasswordForm";

/**
 * ForgotPassword Page
 *
 * Wrapper page for the forgot password flow.
 * Displays the ForgotPasswordForm component with consistent styling.
 */
export default function ForgotPassword() {
  return (
    <div className="min-h-screen bg-[var(--bg-base)]">
      <div className="max-w-md mx-auto px-4 py-12">
        {/* Logo */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-[var(--text-primary)] mb-2 tracking-tight">TRiM</h1>
        </div>

        {/* Forgot Password Form */}
        <ForgotPasswordForm />
      </div>
    </div>
  );
}
