import { useState, useEffect } from "react";
import { BrowserRouter } from "react-router-dom";
import Navbar from "./components/Sidebar";
import AppRoutes from "./routes/AppRoutes";
import { useAppDispatch } from "./store/hooks";
import { exchangeTokenFromUrl } from "./features/auth/authSlice";

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

  // Handle secure token exchange from URL on app load
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.has("exchangeToken")) {
      dispatch(exchangeTokenFromUrl());
    }
  }, [dispatch]);

  const toggleSidebar = () => {
    setIsSidebarCollapsed((prev) => !prev);
  };

  return (
    <div className="min-h-screen bg-zinc-950">
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
