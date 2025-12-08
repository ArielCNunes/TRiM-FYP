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
        {/* Fixed sidebar navigation - displayed on all pages */}
        <Navbar />

        {/* Main application routes - offset by sidebar width */}
        <main className="min-h-screen ml-64">
          <AppRoutes />
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
