import { Link, useNavigate } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '../store/hooks';
import { logout } from '../features/auth/authSlice';
import toast from 'react-hot-toast';

/**
 * Navbar Component
 * 
 * Displays the navigation bar with role-based navigation links and authentication controls.
 * Shows different navigation options based on user role (CUSTOMER, BARBER, ADMIN).
 * Handles user logout and displays user information when authenticated.
 */
export default function Navbar() {
  // Get authentication state and user info from Redux store
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  /**
   * Handles user logout
   * Dispatches logout action to clear auth state, shows success toast, and redirects to login page
   */
  const handleLogout = () => {
    dispatch(logout());
    toast.success('Logged out successfully');
    navigate('/login');
  };

  return (
    <nav className="bg-white shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* Logo and navigation links section */}
          <div className="flex">
            {/* Logo */}
            <Link to="/" className="flex-shrink-0 flex items-center">
              <span className="text-2xl font-bold text-primary-600">Trim</span>
            </Link>
            
            {/* Conditional navigation links based on authentication and user role */}
            {isAuthenticated && (
              <div className="ml-6 flex space-x-8">
                {/* Customer navigation links */}
                {user?.role === 'CUSTOMER' && (
                  <>
                    <Link
                      to="/booking"
                      className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-900"
                    >
                      Book Appointment
                    </Link>
                    <Link
                      to="/my-bookings"
                      className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-500 hover:text-gray-900"
                    >
                      My Bookings
                    </Link>
                  </>
                )}
                
                {/* Barber navigation link */}
                {user?.role === 'BARBER' && (
                  <Link
                    to="/barber"
                    className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-900"
                  >
                    Dashboard
                  </Link>
                )}
                
                {/* Admin navigation link */}
                {user?.role === 'ADMIN' && (
                  <Link
                    to="/admin"
                    className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-900"
                  >
                    Admin Dashboard
                  </Link>
                )}
              </div>
            )}
          </div>

          {/* User authentication controls on the right side */}
          <div className="flex items-center">
            {isAuthenticated ? (
              <div className="flex items-center space-x-4">
                {/* Display user's full name */}
                <span className="text-sm text-gray-700">
                  {user?.firstName} {user?.lastName}
                </span>
                {/* Logout button */}
                <button
                  onClick={handleLogout}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                >
                  Logout
                </button>
              </div>
            ) : (
              <div className="flex space-x-4">
                {/* Sign in link for unauthenticated users */}
                <Link
                  to="/login"
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-primary-600 bg-white hover:bg-gray-50"
                >
                  Sign in
                </Link>
                {/* Sign up link for new users */}
                <Link
                  to="/register"
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                >
                  Sign up
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}