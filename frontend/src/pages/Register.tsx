import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/endpoints';
import toast from 'react-hot-toast';

/**
 * Register Component
 * 
 * Provides user registration interface for new customers.
 * Collects user information (name, email, phone, password) and creates
 * a new account via the authentication API.
 * Upon successful registration, redirects user to login page.
 */
export default function Register() {
  const navigate = useNavigate();
  
  // Form state containing all registration fields
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
  });
  
  // Loading state to disable form during registration process
  const [loading, setLoading] = useState(false);

  /**
   * Handles registration form submission
   * 
   * @param e - Form event
   * 
   * Creates a new user account with the provided information.
   * On success, redirects to login page for authentication.
   * On error, displays error message via toast notification.
   */
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setLoading(true);

  try {
    // Call registration API with user data
    await authApi.register(formData);
    toast.success('Registration successful! Please sign in.');
    
    // Redirect to login page after successful registration
    navigate('/login');
  } catch (error: any) {
    // Display error message from API or generic fallback
    const message = error.response?.data?.message || error.response?.data || 'Registration failed. Please try again.';
    toast.error(message);
  } finally {
    // Reset loading state regardless of success or failure
    setLoading(false);
  }
};

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="max-w-md w-full p-6">
        {/* Page heading */}
        <h2 className="text-2xl font-bold mb-6">Create your account</h2>
        
        {/* Registration form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* First name and last name fields in a 2-column grid */}
          <div className="grid grid-cols-2 gap-4">
            {/* First name input */}
            <div>
              <label className="block text-sm font-medium mb-1">First Name</label>
              <input
                type="text"
                required
                className="w-full px-3 py-2 border rounded-md"
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              />
            </div>
            
            {/* Last name input */}
            <div>
              <label className="block text-sm font-medium mb-1">Last Name</label>
              <input
                type="text"
                required
                className="w-full px-3 py-2 border rounded-md"
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              />
            </div>
          </div>

          {/* Email input field */}
          <div>
            <label className="block text-sm font-medium mb-1">Email</label>
            <input
              type="email"
              required
              className="w-full px-3 py-2 border rounded-md"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
          </div>

          {/* Phone number input field */}
          <div>
            <label className="block text-sm font-medium mb-1">Phone</label>
            <input
              type="tel"
              required
              placeholder="+353 123 456 789"
              className="w-full px-3 py-2 border rounded-md"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            />
          </div>

          {/* Password input field with minimum length validation */}
          <div>
            <label className="block text-sm font-medium mb-1">Password</label>
            <input
              type="password"
              required
              minLength={6}
              className="w-full px-3 py-2 border rounded-md"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            />
          </div>

          {/* Submit button - disabled during loading */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        {/* Sign in link for existing users */}
        <p className="mt-4 text-sm text-center">
          Already have an account?{' '}
          <Link to="/login" className="text-blue-600 hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}