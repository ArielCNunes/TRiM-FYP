import { useState } from "react";
import { BrowserRouter } from "react-router-dom";
import Navbar from "./components/Sidebar";
import AppRoutes from "./routes/AppRoutes";

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
function App() {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);

  const toggleSidebar = () => {
    setIsSidebarCollapsed((prev) => !prev);
  };

  return (
    <BrowserRouter>
      <div className="min-h-screen bg-zinc-950">
        {/* Fixed sidebar navigation - displayed on all pages */}
        <Navbar isCollapsed={isSidebarCollapsed} onToggle={toggleSidebar} />

        {/* Main application routes - offset by sidebar width with transition */}
        <main className={`min-h-screen transition-all duration-300 ${isSidebarCollapsed ? "ml-14" : "ml-64"}`}>
          <AppRoutes />
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
