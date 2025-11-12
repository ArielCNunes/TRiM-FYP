import { ResetPasswordForm } from "../components/auth/ResetPasswordForm";

/**
 * ResetPassword Page
 *
 * Wrapper page for the password reset flow.
 * Displays the ResetPasswordForm component which validates the token
 * and allows the user to set a new password.
 */
export default function ResetPassword() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <div className="max-w-md mx-auto px-4 py-12">
        {/* Logo */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">TRiM</h1>
        </div>

        {/* Reset Password Form */}
        <ResetPasswordForm />
      </div>
    </div>
  );
}
