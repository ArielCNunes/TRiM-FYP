import { useState, useEffect } from "react";
import { useAppSelector } from "../store/hooks";
import { useNavigate } from "react-router-dom";
import { bookingsApi } from "../api/endpoints";
import type { BookingResponse } from "../types";
import StatusBadge from "../components/shared/StatusBadge";
import { formatPaymentStatus } from "../utils/statusUtils";
import RescheduleBookingModal from "../components/booking/RescheduleBookingModal";

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

  // Reschedule modal state
  const [rescheduleModalOpen, setRescheduleModalOpen] = useState(false);
  const [bookingToReschedule, setBookingToReschedule] =
    useState<BookingResponse | null>(null);

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
        const bookingDateTime = new Date(
          `${booking.bookingDate}T${booking.startTime}`
        );
        const isUpcoming =
          booking.status === "PENDING" || booking.status === "CONFIRMED";

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

  /**
   * Open the reschedule modal for the specified booking.
   */
  const handleReschedule = (booking: BookingResponse) => {
    setBookingToReschedule(booking);
    setRescheduleModalOpen(true);
  };

  /**
   * Handle successful reschedule - refresh bookings and show success message.
   */
  const handleRescheduleSuccess = () => {
    setStatus({ type: "success", message: "Booking rescheduled successfully" });
    fetchBookings();
  };

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* Page header */}
      <h1 className="text-4xl font-bold mb-2 text-white">My Bookings</h1>
      <p className="text-zinc-400 mb-8">
        Your upcoming appointments, sorted by date
      </p>

      {status && (
        <div
          className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${status.type === "success"
              ? "border-emerald-800 bg-emerald-900/20 text-emerald-300"
              : "border-red-800 bg-red-900/20 text-red-300"
            }`}
        >
          {status.message}
        </div>
      )}

      {loading ? (
        /* Loading state */
        <div className="text-center py-12">
          <p className="text-zinc-400">Loading your bookings...</p>
        </div>
      ) : bookings.length === 0 ? (
        /* Empty state encouraging user to create a booking */
        <div className="text-center py-12 bg-zinc-900 rounded-lg border border-zinc-800">
          <p className="text-zinc-400 mb-4">You have no upcoming bookings</p>
          <button
            onClick={() => navigate("/booking")}
            className="bg-indigo-600 text-white px-6 py-2 rounded-md hover:bg-indigo-500 shadow-lg shadow-indigo-500/20"
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
              className="bg-zinc-900 border border-zinc-800 rounded-lg shadow-sm hover:border-zinc-700 transition overflow-hidden"
            >
              {/* Header section with date - most important info */}
              <div className="bg-gradient-to-r from-indigo-900/40 to-indigo-900/20 px-6 py-4 border-b border-indigo-900/30">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-indigo-400 font-medium mb-1">
                      Appointment Date
                    </p>
                    <p className="text-2xl font-bold text-white">
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
                    <p className="text-lg font-semibold text-zinc-300 mt-1">
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
                    <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                      Service
                    </p>
                    <p className="text-lg font-semibold text-white">
                      {booking.service.name}
                    </p>
                    <p className="text-sm text-zinc-400 mt-1">
                      {booking.service.durationMinutes} minutes
                    </p>
                  </div>

                  {/* Barber */}
                  <div>
                    <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                      Barber
                    </p>
                    <p className="text-lg font-semibold text-white">
                      {booking.barber.user.firstName}{" "}
                      {booking.barber.user.lastName}
                    </p>
                  </div>

                  {/* Total Price */}
                  <div>
                    <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                      Total Price
                    </p>
                    <p className="text-2xl font-bold text-indigo-400">
                      €{booking.service.price.toFixed(2)}
                    </p>
                  </div>

                  {/* Payment Status */}
                  <div>
                    <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                      Payment Status
                    </p>
                    <p className="text-sm font-medium text-white">
                      {formatPaymentStatus(booking.paymentStatus)}
                    </p>
                    {booking.depositAmount !== undefined && (
                      <p className="text-xs text-zinc-400 mt-1">
                        Deposit: €{booking.depositAmount.toFixed(2)}
                      </p>
                    )}
                  </div>

                  {/* Outstanding Balance */}
                  {booking.outstandingBalance !== undefined &&
                    booking.outstandingBalance > 0 && (
                      <div>
                        <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                          Outstanding
                        </p>
                        <p className="text-lg font-medium text-zinc-300">
                          €{booking.outstandingBalance.toFixed(2)}
                        </p>
                        <p className="text-xs text-zinc-500 mt-1">
                          Pay at shop
                        </p>
                      </div>
                    )}

                  {/* Show nothing in 5th column if fully paid */}
                  {(booking.outstandingBalance === undefined ||
                    booking.outstandingBalance === 0) && (
                      <div>
                        <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                          Balance
                        </p>
                        <p className="text-lg font-medium text-emerald-400">
                          Paid in Full
                        </p>
                      </div>
                    )}
                </div>
              </div>

              {/* Action footer */}
              <div className="px-6 py-4 bg-zinc-900/50 border-t border-zinc-800 flex justify-end gap-3">
                <button
                  onClick={() => handleReschedule(booking)}
                  disabled={cancelling === booking.id}
                  className="px-6 py-2 bg-indigo-600 text-white font-medium rounded-md hover:bg-indigo-500 disabled:bg-zinc-700 transition shadow-lg shadow-indigo-500/20"
                >
                  Reschedule
                </button>
                <button
                  onClick={() => handleCancel(booking.id)}
                  disabled={cancelling === booking.id}
                  className="px-6 py-2 bg-red-600 text-white font-medium rounded-md hover:bg-red-500 disabled:bg-zinc-700 transition shadow-lg shadow-red-500/20"
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
            className="w-full bg-zinc-800 hover:bg-zinc-700 text-zinc-300 font-semibold py-3 px-6 rounded-lg transition flex items-center justify-between border border-zinc-700"
          >
            <span className="text-lg">
              Past Bookings ({pastBookings.length})
            </span>
            <svg
              className={`w-5 h-5 transform transition-transform ${showPastBookings ? "rotate-180" : ""
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
                  className="bg-zinc-900 border border-zinc-800 rounded-lg shadow-sm overflow-hidden opacity-75"
                >
                  {/* Header section with date */}
                  <div className="bg-gradient-to-r from-zinc-800 to-zinc-900 px-6 py-4 border-b border-zinc-800">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm text-zinc-400 font-medium mb-1">
                          Appointment Date
                        </p>
                        <p className="text-2xl font-bold text-zinc-300">
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
                        <p className="text-lg font-semibold text-zinc-400 mt-1">
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
                        <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                          Service
                        </p>
                        <p className="text-lg font-semibold text-zinc-300">
                          {booking.service.name}
                        </p>
                        <p className="text-sm text-zinc-400 mt-1">
                          {booking.service.durationMinutes} minutes
                        </p>
                      </div>

                      {/* Barber */}
                      <div>
                        <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                          Barber
                        </p>
                        <p className="text-lg font-semibold text-zinc-300">
                          {booking.barber.user.firstName}{" "}
                          {booking.barber.user.lastName}
                        </p>
                      </div>

                      {/* Total Price */}
                      <div>
                        <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                          Total Price
                        </p>
                        <p className="text-2xl font-bold text-zinc-400">
                          €{booking.service.price.toFixed(2)}
                        </p>
                      </div>

                      {/* Payment Status */}
                      <div>
                        <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                          Payment Status
                        </p>
                        <p className="text-sm font-medium text-zinc-300">
                          {formatPaymentStatus(booking.paymentStatus)}
                        </p>
                        {booking.depositAmount !== undefined && (
                          <p className="text-xs text-zinc-400 mt-1">
                            Deposit: €{booking.depositAmount.toFixed(2)}
                          </p>
                        )}
                      </div>

                      {/* Outstanding Balance */}
                      {booking.outstandingBalance !== undefined &&
                        booking.outstandingBalance > 0 && (
                          <div>
                            <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                              Outstanding
                            </p>
                            <p className="text-lg font-medium text-zinc-400">
                              €{booking.outstandingBalance.toFixed(2)}
                            </p>
                            <p className="text-xs text-zinc-500 mt-1">Unpaid</p>
                          </div>
                        )}

                      {/* Show nothing in 5th column if fully paid */}
                      {(booking.outstandingBalance === undefined ||
                        booking.outstandingBalance === 0) && (
                          <div>
                            <p className="text-xs uppercase text-zinc-500 font-semibold mb-2">
                              Balance
                            </p>
                            <p className="text-lg font-medium text-emerald-400/70">
                              {booking.status === "COMPLETED"
                                ? "Paid in Full"
                                : "N/A"}
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

      {/* Reschedule Modal */}
      {bookingToReschedule && (
        <RescheduleBookingModal
          booking={bookingToReschedule}
          isOpen={rescheduleModalOpen}
          onClose={() => {
            setRescheduleModalOpen(false);
            setBookingToReschedule(null);
          }}
          onSuccess={handleRescheduleSuccess}
        />
      )}
    </div>
  );
}
