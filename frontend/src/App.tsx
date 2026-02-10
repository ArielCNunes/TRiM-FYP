import { useState, useEffect } from "react";
import { BrowserRouter } from "react-router-dom";
import Navbar from "./components/Sidebar";
import AppRoutes from "./routes/AppRoutes";
import { useAppDispatch, useAppSelector } from "./store/hooks";
import { exchangeTokenFromUrl, logout } from "./features/auth/authSlice";
import { getBusinessSlug } from "./api/axios";
import LoadingSpinner from "./components/shared/LoadingSpinner";

/**
 * App Component
 *
 * Root component of the application.
 * Sets up the application structure with routing and navigation.
 *
 * Structure:
 * - BrowserRouter: Enables client-side routing
 * - Sidebar: Floating sidebar navigation displayed on all pages
 * - AppRoutes: Main routing configuration for all application pages
 */
function AppContent() {
  const dispatch = useAppDispatch();
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isExchangingToken, setIsExchangingToken] = useState(false);

  // Handle secure token exchange from URL on app load
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.has("exchangeToken")) {
      setIsExchangingToken(true);
      dispatch(exchangeTokenFromUrl())
        .finally(() => setIsExchangingToken(false));
    }
  }, [dispatch]);

  // Tenant mismatch guard: if the user's stored businessSlug does not match the
  // current subdomain, log them out so they don't see confusing 403 errors.
  const { businessSlug, isAuthenticated } = useAppSelector((state) => state.auth);

  useEffect(() => {
    if (!isAuthenticated || !businessSlug) return;

    const currentSlug = getBusinessSlug();
    if (businessSlug !== currentSlug) {
      dispatch(logout());
    }
  }, [isAuthenticated, businessSlug, dispatch]);

  const toggleSidebar = () => {
    setIsSidebarCollapsed((prev) => !prev);
  };

  // Show loading while exchanging token
  if (isExchangingToken) {
    return (
      <div className="min-h-screen bg-[var(--bg-base)] flex items-center justify-center">
        <LoadingSpinner message="Setting up your account..." />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[var(--bg-base)]">
      {/* Fixed sidebar navigation - displayed on all pages */}
      <Navbar isCollapsed={isSidebarCollapsed} onToggle={toggleSidebar} />

      {/* Main application routes - offset by sidebar width with transition */}
      <main className={`min-h-screen transition-all duration-300 ${isSidebarCollapsed ? "ml-14" : "ml-64"}`}>
        <AppRoutes />
      </main>
    </div>
  );
}

/**
 * App Component
 *
 * Root component that sets up BrowserRouter.
 * AppContent is separated to allow hooks that depend on router context.
 */
function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App;
