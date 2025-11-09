import { useState, useEffect } from "react";
import { useAppSelector } from "../store/hooks";
import { useNavigate } from "react-router-dom";
import { bookingsApi } from "../api/endpoints";
import type { BookingResponse } from "../types";
import StatusBadge from "../components/shared/StatusBadge";
import { formatPaymentStatus } from "../utils/statusUtils";

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
  const [pastBookings, setPastBookings] = useState<BookingResponse[]>([]);
  const [showPastBookings, setShowPastBookings] = useState(false);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState<number | null>(null);
  const [status, setStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

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
      const now = new Date();
      
      // Separate upcoming and past bookings
      const upcoming: BookingResponse[] = [];
      const past: BookingResponse[] = [];
      
      response.data.forEach((booking) => {
        const bookingDateTime = new Date(`${booking.bookingDate}T${booking.startTime}`);
        const isUpcoming = booking.status === "PENDING" || booking.status === "CONFIRMED";
        
        if (bookingDateTime > now && isUpcoming) {
          upcoming.push(booking);
        } else {
          past.push(booking);
        }
      });
      
      // Sort upcoming by date ascending (nearest first)
      upcoming.sort(
        (a, b) =>
          new Date(a.bookingDate).getTime() - new Date(b.bookingDate).getTime()
      );
      
      // Sort past by date descending (most recent first)
      past.sort(
        (a, b) =>
          new Date(b.bookingDate).getTime() - new Date(a.bookingDate).getTime()
      );
      
      setBookings(upcoming);
      setPastBookings(past);
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load bookings" });
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
      setBookings(bookings.filter((b) => b.id !== bookingId));
      setStatus({ type: "success", message: "Booking cancelled" });
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to cancel booking";
      setStatus({ type: "error", message: String(message) });
    } finally {
      setCancelling(null);
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* Page header */}
      <h1 className="text-4xl font-bold mb-2">My Bookings</h1>
      <p className="text-gray-600 mb-8">
        Your upcoming appointments, sorted by date
      </p>

      {status && (
        <div
          className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
            status.type === "success"
              ? "border-green-200 bg-green-50 text-green-700"
              : "border-red-200 bg-red-50 text-red-700"
          }`}
        >
          {status.message}
        </div>
      )}

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
        /* Bookings list - optimized horizontal card layout */
        <div className="space-y-4">
          {bookings.map((booking) => (
            <div
              key={booking.id}
              className="bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition overflow-hidden"
            >
              {/* Header section with date - most important info */}
              <div className="bg-gradient-to-r from-blue-50 to-blue-100 px-6 py-4 border-b border-blue-200">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-blue-600 font-medium mb-1">
                      Appointment Date
                    </p>
                    <p className="text-2xl font-bold text-gray-900">
                      {new Date(booking.bookingDate).toLocaleDateString(
                        "en-US",
                        {
                          weekday: "short",
                          month: "short",
                          day: "numeric",
                          year: "numeric",
                        }
                      )}
                    </p>
                    <p className="text-lg font-semibold text-gray-700 mt-1">
                      {booking.startTime.substring(0, 5)}
                    </p>
                  </div>
                  <div className="text-right">
                    <StatusBadge 
                      status={booking.status} 
                      type="booking" 
                      className="px-4 py-2 rounded-full border border-opacity-20"
                    />
                  </div>
                </div>
              </div>

              {/* Main content with service, barber, and pricing info */}
              <div className="px-6 py-5">
                <div className="grid grid-cols-1 md:grid-cols-5 gap-6">
                  {/* Service */}
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

                  {/* Barber */}
                  <div>
                    <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                      Barber
                    </p>
                    <p className="text-lg font-semibold text-gray-900">
                      {booking.barber.user.firstName}{" "}
                      {booking.barber.user.lastName}
                    </p>
                  </div>

                  {/* Total Price */}
                  <div>
                    <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                      Total Price
                    </p>
                    <p className="text-2xl font-bold text-blue-600">
                      €{booking.service.price.toFixed(2)}
                    </p>
                  </div>

                  {/* Payment Status */}
                  <div>
                    <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                      Payment Status
                    </p>
                    <p className="text-sm font-medium text-gray-900">
                      {formatPaymentStatus(booking.paymentStatus)}
                    </p>
                    {booking.depositAmount !== undefined && (
                      <p className="text-xs text-gray-600 mt-1">
                        Deposit: €{booking.depositAmount.toFixed(2)}
                      </p>
                    )}
                  </div>

                  {/* Outstanding Balance */}
                  {booking.outstandingBalance !== undefined &&
                    booking.outstandingBalance > 0 && (
                      <div>
                        <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                          Outstanding
                        </p>
                        <p className="text-2xl font-bold text-orange-600">
                          €{booking.outstandingBalance.toFixed(2)}
                        </p>
                        <p className="text-xs text-gray-600 mt-1">
                          Pay at shop
                        </p>
                      </div>
                    )}

                  {/* Show nothing in 5th column if fully paid */}
                  {(booking.outstandingBalance === undefined ||
                    booking.outstandingBalance === 0) && (
                    <div>
                      <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                        Balance
                      </p>
                      <p className="text-lg font-medium text-green-600">
                        Paid in Full
                      </p>
                    </div>
                  )}
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

      {/* Past Bookings Section */}
      {!loading && pastBookings.length > 0 && (
        <div className="mt-12">
          <button
            onClick={() => setShowPastBookings(!showPastBookings)}
            className="w-full bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-3 px-6 rounded-lg transition flex items-center justify-between"
          >
            <span className="text-lg">
              Past Bookings ({pastBookings.length})
            </span>
            <svg
              className={`w-5 h-5 transform transition-transform ${
                showPastBookings ? "rotate-180" : ""
              }`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M19 9l-7 7-7-7"
              />
            </svg>
          </button>

          {showPastBookings && (
            <div className="mt-4 space-y-4">
              {pastBookings.map((booking) => (
                <div
                  key={booking.id}
                  className="bg-gray-50 border border-gray-300 rounded-lg shadow-sm overflow-hidden opacity-75"
                >
                  {/* Header section with date */}
                  <div className="bg-gradient-to-r from-gray-100 to-gray-200 px-6 py-4 border-b border-gray-300">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm text-gray-600 font-medium mb-1">
                          Appointment Date
                        </p>
                        <p className="text-2xl font-bold text-gray-700">
                          {new Date(booking.bookingDate).toLocaleDateString(
                            "en-US",
                            {
                              weekday: "short",
                              month: "short",
                              day: "numeric",
                              year: "numeric",
                            }
                          )}
                        </p>
                        <p className="text-lg font-semibold text-gray-600 mt-1">
                          {booking.startTime.substring(0, 5)}
                        </p>
                      </div>
                      <div className="text-right">
                        <StatusBadge 
                          status={booking.status} 
                          type="booking" 
                          className="px-4 py-2 rounded-full border border-opacity-20"
                        />
                      </div>
                    </div>
                  </div>

                  {/* Main content with service, barber, and pricing info */}
                  <div className="px-6 py-5">
                    <div className="grid grid-cols-1 md:grid-cols-5 gap-6">
                      {/* Service */}
                      <div>
                        <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                          Service
                        </p>
                        <p className="text-lg font-semibold text-gray-700">
                          {booking.service.name}
                        </p>
                        <p className="text-sm text-gray-600 mt-1">
                          {booking.service.durationMinutes} minutes
                        </p>
                      </div>

                      {/* Barber */}
                      <div>
                        <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                          Barber
                        </p>
                        <p className="text-lg font-semibold text-gray-700">
                          {booking.barber.user.firstName}{" "}
                          {booking.barber.user.lastName}
                        </p>
                      </div>

                      {/* Total Price */}
                      <div>
                        <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                          Total Price
                        </p>
                        <p className="text-2xl font-bold text-gray-600">
                          €{booking.service.price.toFixed(2)}
                        </p>
                      </div>

                      {/* Payment Status */}
                      <div>
                        <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                          Payment Status
                        </p>
                        <p className="text-sm font-medium text-gray-700">
                          {formatPaymentStatus(booking.paymentStatus)}
                        </p>
                        {booking.depositAmount !== undefined && (
                          <p className="text-xs text-gray-600 mt-1">
                            Deposit: €{booking.depositAmount.toFixed(2)}
                          </p>
                        )}
                      </div>

                      {/* Outstanding Balance */}
                      {booking.outstandingBalance !== undefined &&
                        booking.outstandingBalance > 0 && (
                          <div>
                            <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                              Outstanding
                            </p>
                            <p className="text-2xl font-bold text-orange-600">
                              €{booking.outstandingBalance.toFixed(2)}
                            </p>
                            <p className="text-xs text-gray-600 mt-1">
                              Unpaid
                            </p>
                          </div>
                        )}

                      {/* Show nothing in 5th column if fully paid */}
                      {(booking.outstandingBalance === undefined ||
                        booking.outstandingBalance === 0) && (
                        <div>
                          <p className="text-xs uppercase text-gray-500 font-semibold mb-2">
                            Balance
                          </p>
                          <p className="text-lg font-medium text-green-600">
                            {booking.status === "COMPLETED" ? "Paid in Full" : "N/A"}
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
