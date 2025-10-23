import { Routes, Route } from 'react-router-dom';
import BookingFlow from '@/pages/BookingFlow';
import Home from '@/pages/Home';
import Admin from '@/pages/Admin';
import MyBookings from '@/pages/MyBookings';
import Auth from '@/pages/Auth';

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

      {/* Booking Flow route */}
      <Route path="/booking" element={<BookingFlow />} />

      {/* Admin page route */}
      <Route path="/admin" element={<Admin />} />

      {/* My Bookings page route */}
      <Route path="/my-bookings" element={<MyBookings />} />

      {/* Auth page route */}
      <Route path="/auth" element={<Auth />} />
    </Routes>
  );
}