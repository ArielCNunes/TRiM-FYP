import { Link, useNavigate } from "react-router-dom";
import { useAppSelector, useAppDispatch } from "../store/hooks";
import { logout } from "../features/auth/authSlice";

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
   * Dispatches logout action to clear auth state and redirects to login page
   */
  const handleLogout = () => {
    dispatch(logout());
    navigate("/auth");
  };

  return (
    <nav className="bg-zinc-900/90 backdrop-blur-sm border-b border-zinc-800 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* Logo and navigation links section */}
          <div className="flex">
            {/* Logo */}
            <Link to="/" className="flex-shrink-0 flex items-center">
              <span className="text-2xl font-bold text-white tracking-tight">
                TRiM
              </span>
            </Link>

            {/* Navigation links */}
            <div className="ml-6 flex space-x-8">
              {/* Book Appointment - Always visible for guests and customers only */}
              {(!user || user.role === "CUSTOMER") && (
                <Link
                  to="/booking"
                  className="inline-flex items-center px-1 pt-1 text-sm font-medium text-zinc-300 hover:text-white transition-colors"
                >
                  Book Appointment
                </Link>
              )}

              {/* Authenticated user links */}
              {isAuthenticated && (
                <>
                  {/* Customer navigation links */}
                  {user?.role === "CUSTOMER" && (
                    <Link
                      to="/my-bookings"
                      className="inline-flex items-center px-1 pt-1 text-sm font-medium text-zinc-400 hover:text-white transition-colors"
                    >
                      My Bookings
                    </Link>
                  )}

                  {/* Barber navigation link */}
                  {user?.role === "BARBER" && (
                    <Link
                      to="/barber"
                      className="inline-flex items-center px-1 pt-1 text-sm font-medium text-zinc-300 hover:text-white transition-colors"
                    >
                      Dashboard
                    </Link>
                  )}

                  {/* Admin navigation link */}
                  {user?.role === "ADMIN" && (
                    <Link
                      to="/admin"
                      className="inline-flex items-center px-1 pt-1 text-sm font-medium text-zinc-300 hover:text-white transition-colors"
                    >
                      Admin Dashboard
                    </Link>
                  )}
                </>
              )}
            </div>
          </div>

          {/* User authentication controls on the right side */}
          <div className="flex items-center">
            {isAuthenticated ? (
              <div className="flex items-center space-x-4">
                {/* Display user's full name */}
                <span className="text-sm text-zinc-300 font-medium">
                  {user?.firstName} {user?.lastName}
                </span>
                {/* Logout button */}
                <button
                  onClick={handleLogout}
                  className="inline-flex items-center px-4 py-2 border border-zinc-700 text-sm font-medium rounded-md text-zinc-300 bg-zinc-800 hover:bg-zinc-700 hover:text-white transition-all"
                >
                  Logout
                </button>
              </div>
            ) : (
              <Link
                to="/auth"
                className="inline-flex items-center px-6 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-500 shadow-lg shadow-indigo-500/20 transition-all"
              >
                Sign in / Sign up
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
