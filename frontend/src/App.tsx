import { BrowserRouter } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Navbar from './components/Navbar';
import AppRoutes from './routes/AppRoutes';

/**
 * App Component
 * 
 * Root component of the application.
 * Sets up the application structure with routing, navigation, and toast notifications.
 * 
 * Structure:
 * - BrowserRouter: Enables client-side routing
 * - Navbar: Persistent navigation bar across all pages
 * - AppRoutes: Main routing configuration for all application pages
 * - Toaster: Global toast notification system for user feedback
 */
function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        {/* Navigation bar - displayed on all pages */}
        <Navbar />
        
        {/* Main application routes */}
        <AppRoutes />
        
        {/* Toast notification container - positioned at top-right */}
        <Toaster position="top-right" />
      </div>
    </BrowserRouter>
  );
}

export default App;