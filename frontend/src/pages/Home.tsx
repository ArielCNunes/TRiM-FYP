import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { useState, useEffect } from "react";
import { bookingsApi } from "../api/endpoints";
import type { BookingResponse } from "../types";
import { Settings, Users, Calendar, Clock } from "lucide-react";

export default function Home() {
  const navigate = useNavigate();
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
  const user = useAppSelector((state) => state.auth.user);

  // State for bookings if user is authenticated customer
  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [loadingBookings, setLoadingBookings] = useState(false);
  const [cancelling, setCancelling] = useState<number | null>(null);

  // Fetch bookings only for customers
  useEffect(() => {
    if (isAuthenticated && user && user.role === "CUSTOMER") {
      fetchBookings();
    }
  }, [isAuthenticated, user]);

  const fetchBookings = async () => {
    if (!user) return;
    setLoadingBookings(true);
    try {
      const response = await bookingsApi.getCustomerBookings(user.id || 0);
      const upcomingBookings = response.data.filter(
        (booking) =>
          booking.status === "PENDING" || booking.status === "CONFIRMED"
      );
      upcomingBookings.sort(
        (a, b) =>
          new Date(a.bookingDate).getTime() - new Date(b.bookingDate).getTime()
      );
      setBookings(upcomingBookings);
    } catch (error) {
      console.error("Failed to load bookings", error);
    } finally {
      setLoadingBookings(false);
    }
  };

  const handleCancel = async (bookingId: number) => {
    if (!window.confirm("Are you sure you want to cancel this booking?")) {
      return;
    }

    setCancelling(bookingId);
    try {
      await bookingsApi.cancelBooking(bookingId);
      setBookings(bookings.filter((b) => b.id !== bookingId));
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to cancel booking");
    } finally {
      setCancelling(null);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      {/* Admin View */}
      {user?.role === "ADMIN" && (
        <div className="max-w-7xl mx-auto px-6 py-20">
          <div className="text-center mb-16">
            <h1 className="text-6xl font-bold mb-6 text-gray-900">
              TRiM Admin
            </h1>
            <p className="text-xl text-gray-600 mb-8">
              Manage your barbershop operations
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            {/* Services Management */}
            <div className="bg-white rounded-xl shadow-lg p-8 hover:shadow-xl transition">
              <div className="flex items-center mb-4">
                <div className="bg-blue-100 p-3 rounded-lg">
                  <Settings className="w-8 h-8 text-blue-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 ml-4">
                  Services
                </h2>
              </div>
              <p className="text-gray-600 mb-6">Create and manage services</p>
              <button
                onClick={() => navigate("/admin")}
                className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
              >
                Manage Services
              </button>
            </div>

            {/* Barbers Management */}
            <div className="bg-white rounded-xl shadow-lg p-8 hover:shadow-xl transition">
              <div className="flex items-center mb-4">
                <div className="bg-green-100 p-3 rounded-lg">
                  <Users className="w-8 h-8 text-green-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 ml-4">
                  Barbers
                </h2>
              </div>
              <p className="text-gray-600 mb-6">
                Add new barbers, manage profiles, and configure their
                availability
              </p>
              <button
                onClick={() => navigate("/admin")}
                className="w-full bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition"
              >
                Manage Barbers
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Barber View */}
      {user?.role === "BARBER" && (
        <div className="max-w-7xl mx-auto px-6 py-20">
          <div className="text-center mb-16">
            <h1 className="text-6xl font-bold mb-6 text-gray-900">
              Welcome, {user.firstName}!
            </h1>
            <p className="text-xl text-gray-600 mb-8">Your barber dashboard</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            {/* Today's Schedule */}
            <div className="bg-white rounded-xl shadow-lg p-8 hover:shadow-xl transition">
              <div className="flex items-center mb-4">
                <div className="bg-blue-100 p-3 rounded-lg">
                  <Calendar className="w-8 h-8 text-blue-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 ml-4">
                  Today's Schedule
                </h2>
              </div>
              <p className="text-gray-600 mb-6">
                View and manage your appointments for today
              </p>
              <button
                onClick={() => navigate("/barber")}
                className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
              >
                View Schedule
              </button>
            </div>

            {/* Availability */}
            <div className="bg-white rounded-xl shadow-lg p-8 hover:shadow-xl transition">
              <div className="flex items-center mb-4">
                <div className="bg-green-100 p-3 rounded-lg">
                  <Clock className="w-8 h-8 text-green-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 ml-4">
                  Availability
                </h2>
              </div>
              <p className="text-gray-600 mb-6">
                Set your working hours and manage your schedule
              </p>
              <button
                onClick={() => navigate("/barber")}
                className="w-full bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition"
              >
                Manage Availability
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Customer/Guest View */}
      {(!user || user.role === "CUSTOMER") && (
        <>
          {/* Hero Section */}
          <div className="max-w-7xl mx-auto px-6 py-20">
            <div className="text-center mb-16">
              <h1 className="text-6xl font-bold mb-6 text-gray-900">TRiM</h1>
              <p className="text-xl text-gray-600 mb-8">
                Book an appointment with us today!
              </p>

              <button
                onClick={() => navigate("/booking")}
                className="bg-blue-600 text-white px-12 py-4 rounded-lg font-semibold hover:bg-blue-700 transition text-lg shadow-lg hover:shadow-xl"
              >
                Book Now
              </button>
            </div>

            {/* My Bookings Section - Only for authenticated customers */}
            {isAuthenticated && user && user.role === "CUSTOMER" && (
              <div className="mb-16">
                <div className="bg-white rounded-xl shadow-lg p-8">
                  <div className="flex items-center justify-between mb-6">
                    <h2 className="text-3xl font-bold text-gray-900">
                      My Upcoming Appointments
                    </h2>
                    <button
                      onClick={fetchBookings}
                      className="text-blue-600 hover:text-blue-700 font-medium"
                    >
                      Refresh
                    </button>
                  </div>

                  {loadingBookings ? (
                    <div className="text-center py-8">
                      <p className="text-gray-600">Loading your bookings...</p>
                    </div>
                  ) : bookings.length === 0 ? (
                    <div className="text-center py-8 bg-gray-50 rounded-lg">
                      <p className="text-gray-600 mb-4">
                        You have no upcoming appointments
                      </p>
                      <button
                        onClick={() => navigate("/booking")}
                        className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
                      >
                        Book Your First Appointment
                      </button>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {bookings.map((booking) => (
                        <div
                          key={booking.id}
                          className="border border-gray-200 rounded-lg hover:shadow-md transition overflow-hidden"
                        >
                          {/* Header with date */}
                          <div className="bg-gradient-to-r from-blue-50 to-blue-100 px-6 py-4 border-b border-blue-200">
                            <div className="flex items-center justify-between">
                              <div>
                                <p className="text-sm text-blue-600 font-medium mb-1">
                                  Appointment Date
                                </p>
                                <p className="text-2xl font-bold text-gray-900">
                                  {new Date(
                                    booking.bookingDate
                                  ).toLocaleDateString("en-US", {
                                    weekday: "short",
                                    month: "short",
                                    day: "numeric",
                                    year: "numeric",
                                  })}
                                </p>
                                <p className="text-lg font-semibold text-gray-700 mt-1">
                                  {booking.startTime.substring(0, 5)}
                                </p>
                              </div>
                              <div className="text-right">
                                <span
                                  className={`px-4 py-2 rounded-full text-sm font-semibold ${
                                    booking.status === "CONFIRMED"
                                      ? "bg-green-100 text-green-800 border border-green-200"
                                      : "bg-yellow-100 text-yellow-800 border border-yellow-200"
                                  }`}
                                >
                                  {booking.status}
                                </span>
                              </div>
                            </div>
                          </div>

                          {/* Main content */}
                          <div className="px-6 py-5">
                            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                              <div>
                                <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                                  Service
                                </p>
                                <p className="text-lg font-semibold text-gray-900">
                                  {booking.service.name}
                                </p>
                                <p className="text-sm text-gray-600 mt-1">
                                  {booking.service.durationMinutes} minutes
                                </p>
                              </div>

                              <div>
                                <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                                  Barber
                                </p>
                                <p className="text-lg font-semibold text-gray-900">
                                  {booking.barber.user.firstName}{" "}
                                  {booking.barber.user.lastName}
                                </p>
                              </div>

                              <div>
                                <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                                  Price
                                </p>
                                <p className="text-2xl font-bold text-blue-600">
                                  €{booking.service.price.toFixed(2)}
                                </p>
                              </div>

                              <div>
                                <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                                  Payment
                                </p>
                                <p className="text-sm font-medium text-gray-900">
                                  {booking.paymentStatus}
                                </p>
                              </div>
                            </div>
                          </div>

                          {/* Action footer */}
                          <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 flex justify-end">
                            <button
                              onClick={() => handleCancel(booking.id)}
                              disabled={cancelling === booking.id}
                              className="px-6 py-2 bg-red-600 text-white font-medium rounded-md hover:bg-red-700 disabled:bg-gray-400 transition"
                            >
                              {cancelling === booking.id
                                ? "Cancelling..."
                                : "Cancel Booking"}
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
