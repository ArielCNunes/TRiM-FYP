import { ForgotPasswordForm } from "../components/auth/ForgotPasswordForm";

/**
 * ForgotPassword Page
 *
 * Wrapper page for the forgot password flow.
 * Displays the ForgotPasswordForm component with consistent styling.
 */
export default function ForgotPassword() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <div className="max-w-md mx-auto px-4 py-12">
        {/* Logo */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">TRiM</h1>
        </div>

        {/* Forgot Password Form */}
        <ForgotPasswordForm />
      </div>
    </div>
  );
}
