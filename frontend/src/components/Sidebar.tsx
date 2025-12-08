import { Link, useNavigate } from "react-router-dom";
import { useAppSelector, useAppDispatch } from "../store/hooks";
import { logout } from "../features/auth/authSlice";

interface SidebarProps {
  isCollapsed: boolean;
  onToggle: () => void;
}

/**
 * Sidebar Component
 *
 * Displays a fixed sidebar with role-based navigation links and authentication controls.
 * Shows different navigation options based on user role (CUSTOMER, BARBER, ADMIN).
 * Handles user logout and displays user information when authenticated.
 * Can be toggled open/closed with a floating button.
 */
export default function Navbar({ isCollapsed, onToggle }: SidebarProps) {
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
    <>
      {/* Toggle Button - visible when sidebar is hidden */}
      {isCollapsed && (
        <button
          onClick={onToggle}
          className="fixed top-4 left-4 z-50 p-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-300 hover:text-white rounded-lg border border-zinc-700 transition-all duration-300"
          aria-label="Show sidebar"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M13 5l7 7-7 7M5 5l7 7-7 7"
            />
          </svg>
        </button>
      )}

      {/* Fixed Sidebar */}
      <nav
        className={`fixed top-0 left-0 bottom-0 z-40 w-64 bg-zinc-900 border-r border-zinc-800 flex flex-col transition-transform duration-300 ${isCollapsed ? "-translate-x-full" : "translate-x-0"
          }`}
      >
        {/* Logo section with toggle button */}
        <div className="px-6 py-6 border-b border-zinc-800 flex items-center justify-between">
          <Link to="/" className="flex items-center">
            <span className="text-2xl font-bold text-white tracking-tight">
              TRiM
            </span>
          </Link>
          <button
            onClick={onToggle}
            className="p-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-300 hover:text-white rounded-lg border border-zinc-700 transition-all"
            aria-label="Hide sidebar"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M11 19l-7-7 7-7m8 14l-7-7 7-7"
              />
            </svg>
          </button>
        </div>

        {/* Navigation links - vertical layout */}
        <div className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
          {/* Home link - always visible */}
          <Link
            to="/"
            className="flex items-center px-4 py-3 text-sm font-medium text-zinc-300 hover:text-white hover:bg-zinc-800 rounded-xl transition-all"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            Home
          </Link>

          {/* Book Appointment - Always visible for guests and customers only */}
          {(!user || user.role === "CUSTOMER") && (
            <Link
              to="/booking"
              className="flex items-center px-4 py-3 text-sm font-medium text-zinc-300 hover:text-white hover:bg-zinc-800 rounded-xl transition-all"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
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
                  className="flex items-center px-4 py-3 text-sm font-medium text-zinc-300 hover:text-white hover:bg-zinc-800 rounded-xl transition-all"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                  </svg>
                  My Bookings
                </Link>
              )}

              {/* Barber navigation link */}
              {user?.role === "BARBER" && (
                <Link
                  to="/barber"
                  className="flex items-center px-4 py-3 text-sm font-medium text-zinc-300 hover:text-white hover:bg-zinc-800 rounded-xl transition-all"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                  </svg>
                  Dashboard
                </Link>
              )}

              {/* Admin navigation link */}
              {user?.role === "ADMIN" && (
                <Link
                  to="/admin"
                  className="flex items-center px-4 py-3 text-sm font-medium text-zinc-300 hover:text-white hover:bg-zinc-800 rounded-xl transition-all"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  Admin Dashboard
                </Link>
              )}
            </>
          )}
        </div>

        {/* User section at bottom */}
        <div className="px-4 py-4 border-t border-zinc-800">
          {isAuthenticated ? (
            <div className="space-y-3">
              {/* Display user's full name */}
              <div className="px-4 py-2">
                <p className="text-xs text-zinc-500 uppercase tracking-wider">Signed in as</p>
                <p className="text-sm text-zinc-300 font-medium truncate">
                  {user?.firstName} {user?.lastName}
                </p>
              </div>
              {/* Logout button */}
              <button
                onClick={handleLogout}
                className="w-full flex items-center justify-center px-4 py-3 border border-zinc-700 text-sm font-medium rounded-xl text-zinc-300 bg-zinc-800 hover:bg-zinc-700 hover:text-white transition-all"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
                Logout
              </button>
            </div>
          ) : (
            <Link
              to="/auth"
              className="w-full flex items-center justify-center px-4 py-3 border border-transparent text-sm font-medium rounded-xl text-white bg-indigo-600 hover:bg-indigo-500 shadow-lg shadow-indigo-500/20 transition-all"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
              </svg>
              Sign in / Sign up
            </Link>
          )}
        </div>
      </nav>
    </>
  );
}
