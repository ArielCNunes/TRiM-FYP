import { useState, useEffect } from "react";
import { useAppSelector } from "../store/hooks";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { bookingsApi } from "../api/endpoints";
import type { BookingResponse } from "../types";

/**
 * My Bookings Page
 *
 * Displays the authenticated customer's upcoming bookings, ordered by date,
 * with the option to cancel an appointment directly from the list.
 */
export default function MyBookings() {
  const navigate = useNavigate();
  const user = useAppSelector((state) => state.auth.user);

  // Local state for bookings list, loading indicator, and in-flight cancellation
  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState<number | null>(null);

  // Redirect if not authenticated
  if (!user) {
    return (
      <div className="p-8 text-center">
        <p className="text-red-600">Please log in to view your bookings</p>
        <button
          onClick={() => navigate("/login")}
          className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md"
        >
          Go to Login
        </button>
      </div>
    );
  }

  // Fetch bookings once when the page mounts
  useEffect(() => {
    fetchBookings();
  }, []);

  /**
   * Load the authenticated customer's upcoming bookings and update local state.
   * Filters out completed/cancelled entries, sorts upcoming ones by date, and
   * toggles the loading indicator once data is fetched.
   */
  const fetchBookings = async () => {
    try {
      const response = await bookingsApi.getCustomerBookings(user.id || 0);
      // Filter for upcoming bookings only (not completed, cancelled, or no-show)
      const upcomingBookings = response.data.filter(
        (booking) =>
          booking.status === "PENDING" || booking.status === "CONFIRMED"
      );
      // Sort by date ascending (nearest first)
      upcomingBookings.sort(
        (a, b) =>
          new Date(a.bookingDate).getTime() - new Date(b.bookingDate).getTime()
      );
      setBookings(upcomingBookings);
    } catch (error) {
      toast.error("Failed to load bookings");
    } finally {
      setLoading(false);
    }
  };

  /**
   * Cancel the specified booking after user confirmation and refresh the list.
   */
  const handleCancel = async (bookingId: number) => {
    if (!window.confirm("Are you sure you want to cancel this booking?")) {
      return;
    }

    setCancelling(bookingId);
    try {
      await bookingsApi.cancelBooking(bookingId);
      toast.success("Booking cancelled");
      // Remove from local state immediately
      setBookings(bookings.filter((b) => b.id !== bookingId));
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to cancel booking";
      toast.error(String(message));
    } finally {
      setCancelling(null);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* Page header */}
      <h1 className="text-4xl font-bold mb-8">My Bookings</h1>

      {loading ? (
        /* Loading state */
        <div className="text-center py-12">
          <p className="text-gray-600">Loading your bookings...</p>
        </div>
      ) : bookings.length === 0 ? (
        /* Empty state encouraging user to create a booking */
        <div className="text-center py-12 bg-gray-50 rounded-lg">
          <p className="text-gray-600 mb-4">You have no upcoming bookings</p>
          <button
            onClick={() => navigate("/booking")}
            className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
          >
            Book Now
          </button>
        </div>
      ) : (
        /* Bookings list */
        <div className="space-y-4">
          {bookings.map((booking) => (
            <div
              key={booking.id}
              className="p-6 bg-white border border-gray-200 rounded-lg shadow hover:shadow-md transition"
            >
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-4">
                {/* Service & Barber */}
                <div>
                  <p className="text-sm text-gray-600 mb-1">Service</p>
                  <p className="text-lg font-semibold">
                    {booking.service.name}
                  </p>
                  <p className="text-sm text-gray-600 mt-2">Barber</p>
                  <p className="text-base">
                    {booking.barber.user.firstName}{" "}
                    {booking.barber.user.lastName}
                  </p>
                </div>

                {/* Date & Time */}
                <div>
                  <p className="text-sm text-gray-600 mb-1">Date & Time</p>
                  <p className="text-lg font-semibold">
                    {new Date(booking.bookingDate).toLocaleDateString()} at{" "}
                    {booking.startTime}
                  </p>
                  <p className="text-sm text-gray-600 mt-2">Duration</p>
                  <p className="text-base">
                    {booking.service.durationMinutes} minutes
                  </p>
                </div>

                {/* Price & Status */}
                <div>
                  <p className="text-sm text-gray-600 mb-1">Price</p>
                  <p className="text-lg font-semibold text-blue-600">
                    €{booking.service.price.toFixed(2)}
                  </p>
                  <p className="text-sm text-gray-600 mt-2">Status</p>
                  <p className="text-base">
                    <span
                      className={`px-3 py-1 rounded-full text-sm font-semibold ${
                        booking.status === "CONFIRMED"
                          ? "bg-green-100 text-green-800"
                          : "bg-yellow-100 text-yellow-800"
                      }`}
                    >
                      {booking.status}
                    </span>
                  </p>
                </div>
              </div>

              {/* Payment Status */}
              <div className="mb-4 pb-4 border-t">
                <p className="text-sm text-gray-600">
                  Payment: {booking.paymentStatus}
                </p>
              </div>

              {/* Cancel Button */}
              <button
                onClick={() => handleCancel(booking.id)}
                disabled={cancelling === booking.id}
                className="w-full bg-red-600 text-white py-2 rounded-md hover:bg-red-700 disabled:bg-gray-400 transition"
              >
                {cancelling === booking.id ? "Cancelling..." : "Cancel Booking"}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
