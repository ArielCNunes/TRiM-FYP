import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAppDispatch } from '../store/hooks';
import { setCredentials } from '../features/auth/authSlice';
import { authApi } from '../api/endpoints';
import toast from 'react-hot-toast';

/**
 * Login Component
 * 
 * Provides user authentication interface with email and password fields.
 * Handles login submission, stores credentials in Redux, and redirects users
 * to appropriate dashboards based on their role (ADMIN, BARBER, or CUSTOMER).
 */
export default function Login() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  
  // Form state for email and password inputs
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  
  // Loading state to disable form during authentication
  // Loading state to disable form during authentication
  const [loading, setLoading] = useState(false);

  /**
   * Handles login form submission
   * 
   * @param e - Form event
   * 
   * Authenticates user credentials, stores auth data in Redux store,
   * and redirects to role-specific pages:
   * - ADMIN -> /admin
   * - BARBER -> /barber
   * - CUSTOMER -> / (home)
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Call login API with email and password
      const response = await authApi.login(formData);
      const { token, email, firstName, lastName, role } = response.data;
      
      // Store credentials in Redux store for global access
      dispatch(setCredentials({ token, email, firstName, lastName, role }));
      toast.success('Login successful!');
      
      // Redirect based on user role
      if (role === 'ADMIN') {
        navigate('/admin');
      } else if (role === 'BARBER') {
        navigate('/barber');
      } else {
        navigate('/');
      }
    } catch (error: any) {
      // Display error message from API or generic fallback
      const message = error.response?.data || 'Login failed';
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
        <h2 className="text-2xl font-bold mb-6">Sign in to your account</h2>
        
        {/* Login form */}
        <form onSubmit={handleSubmit} className="space-y-4">
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

          {/* Password input field */}
          <div>
            <label className="block text-sm font-medium mb-1">Password</label>
            <input
              type="password"
              required
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
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        {/* Sign up link for new users */}
        <p className="mt-4 text-sm text-center">
          Don't have an account?{' '}
          <Link to="/register" className="text-blue-600 hover:underline">
            Sign up
          </Link>
        </p>
      </div>
    </div>
  );
}