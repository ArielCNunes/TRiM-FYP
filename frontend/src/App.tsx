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
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-zinc-950">
        {/* Floating sidebar navigation - displayed on all pages */}
        <Navbar />

        {/* Main application routes - full width since sidebar is floating/overlay */}
        <main className="min-h-screen">
          <AppRoutes />
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
