import { Routes, Route } from 'react-router-dom';
import Login from '../pages/login';
import Register from '../pages/Register';

/**
 * Placeholder Home component
 */
const Home = () => <div className="p-8 text-center">Home Page - Coming Soon</div>;

/**
 * AppRoutes Component
 * 
 * Defines the main application routing configuration using React Router.
 * Currently includes basic routes for home and login pages.
 * 
 */
export default function AppRoutes() {
  return (
    <Routes>
      {/* Home page route */}
      <Route path="/" element={<Home />} />
      
      {/* Login page route */}
      <Route path="/login" element={<Login />} />

      {/* Register page route */}
      <Route path="/register" element={<Register />} />
    </Routes>
  );
}