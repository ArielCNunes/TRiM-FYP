import { BrowserRouter } from 'react-router-dom';
import Navbar from './components/Navbar';
import AppRoutes from './routes/AppRoutes';

/**
 * App Component
 * 
 * Root component of the application.
 * Sets up the application structure with routing and navigation.
 * 
 * Structure:
 * - BrowserRouter: Enables client-side routing
 * - Navbar: Persistent navigation bar across all pages
 * - AppRoutes: Main routing configuration for all application pages
 */
function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        {/* Navigation bar - displayed on all pages */}
        <Navbar />
        
        {/* Main application routes */}
        <AppRoutes />
      </div>
    </BrowserRouter>
  );
}

export default App;