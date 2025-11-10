import { useState, useEffect } from "react";
import { bookingsApi } from "../../api/endpoints";
import BookingActionButton from "../booking/BookingActionButton";
import StatusBadge from "../shared/StatusBadge";
import { formatPaymentStatus } from "../../utils/statusUtils";
import type { BookingResponse } from "../../types";

/**
 * Bookings Manager - Shows all bookings for the barber with management actions
 */
export default function BarberBookingsManager({
  barberId,
}: {
  barberId?: number;
}) {
  const [bookings, setBookings] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterView, setFilterView] = useState<
    "active" | "completed" | "cancelled"
  >("active");

  useEffect(() => {
    if (!barberId) return;
    loadBookings();
  }, [barberId]);

  const loadBookings = async () => {
    try {
      const response = await bookingsApi.getBarberBookings(barberId!);
      // Sort by date descending (most recent first)
      const sorted = response.data.sort(
        (a: any, b: any) =>
          new Date(b.bookingDate).getTime() - new Date(a.bookingDate).getTime()
      );
      setBookings(sorted);
    } catch (error) {
      console.error("Failed to load bookings", error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <p className="text-center text-gray-500">Loading bookings...</p>;
  }

  if (bookings.length === 0) {
    return (
      <p className="text-center text-gray-500 py-8">
        No bookings found. Bookings will appear here once customers make
        appointments.
      </p>
    );
  }

  const handleBookingUpdate = (updatedBooking: BookingResponse) => {
    setBookings((prev) =>
      prev.map((b) => (b.id === updatedBooking.id ? updatedBooking : b))
    );
  };

  // Filter bookings based on the selected view
  const filteredBookings = bookings.filter((b) => {
    if (filterView === "active") {
      // Active: PENDING and CONFIRMED only
      return b.status === "PENDING" || b.status === "CONFIRMED";
    } else if (filterView === "completed") {
      // Completed bookings
      return b.status === "COMPLETED";
    } else {
      // Cancelled: CANCELLED and NO_SHOW
      return b.status === "CANCELLED" || b.status === "NO_SHOW";
    }
  });

  return (
    <div>
      {/* Filter Dropdown */}
      <div className="mb-6 flex justify-between items-center">
        <div className="flex items-center gap-2">
          <label
            htmlFor="booking-filter"
            className="text-sm font-medium text-gray-700"
          >
            Show:
          </label>
          <select
            id="booking-filter"
            value={filterView}
            onChange={(e) =>
              setFilterView(
                e.target.value as "active" | "completed" | "cancelled"
              )
            }
            className="px-3 py-2 border border-gray-300 rounded-md bg-white text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="active">Active Bookings</option>
            <option value="completed">Completed Bookings</option>
            <option value="cancelled">Cancelled/No-Show</option>
          </select>
        </div>
        <div className="text-sm text-gray-500">
          {filteredBookings.length}{" "}
          {filteredBookings.length === 1 ? "booking" : "bookings"}
        </div>
      </div>

      {/* Bookings List */}
      {filteredBookings.length === 0 ? (
        <p className="text-center text-gray-500 py-8">
          {filterView === "active" &&
            "No active bookings found. Bookings will appear here once customers make appointments."}
          {filterView === "completed" && "No completed bookings found."}
          {filterView === "cancelled" &&
            "No cancelled or no-show bookings found."}
        </p>
      ) : (
        <div className="space-y-4">
          {filteredBookings.map((booking) => (
            <div
              key={booking.id}
              className="p-4 border border-gray-200 rounded-lg bg-gray-50 hover:bg-gray-100 transition"
            >
              <div className="flex justify-between items-start mb-3">
                <div>
                  <p className="font-semibold text-gray-900">
                    {booking.service.name}
                  </p>
                  <p className="text-sm text-gray-600">
                    with {booking.customer.firstName}{" "}
                    {booking.customer.lastName}
                  </p>
                </div>
                <StatusBadge status={booking.status} type="booking" />
              </div>

              <div className="space-y-1 mb-3">
                <p className="text-sm text-gray-600">
                  {new Date(booking.bookingDate).toLocaleDateString()} at{" "}
                  {booking.startTime}
                </p>
                <p className="text-sm text-gray-600">
                  Duration: {booking.service.durationMinutes} min
                </p>
              </div>

              {/* Payment Information */}
              <div className="pt-3 border-t border-gray-300">
                <div className="flex justify-between items-center">
                  <div>
                    <p className="text-xs text-gray-500 uppercase font-semibold">
                      Payment Status
                    </p>
                    <p className="text-sm font-medium text-gray-900">
                      {formatPaymentStatus(booking.paymentStatus)}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-gray-500 uppercase font-semibold">
                      Total Price
                    </p>
                    <p className="text-lg font-bold text-blue-600">
                      €{booking.service.price.toFixed(2)}
                    </p>
                  </div>
                </div>

                {/* Outstanding Balance Alert - Only show for active bookings */}
                {(booking.status === "PENDING" ||
                  booking.status === "CONFIRMED") &&
                  booking.outstandingBalance !== undefined &&
                  booking.outstandingBalance > 0 && (
                    <div className="mt-3 p-3 bg-orange-50 border border-orange-200 rounded-md">
                      <div className="flex justify-between items-center">
                        <div>
                          <p className="text-xs text-orange-700 font-semibold">
                            OUTSTANDING BALANCE
                          </p>
                        </div>
                        <p className="text-xl font-bold text-orange-600">
                          €{booking.outstandingBalance.toFixed(2)}
                        </p>
                      </div>
                    </div>
                  )}

                {/* Fully Paid Indicator - Only show for active bookings */}
                {(booking.status === "PENDING" ||
                  booking.status === "CONFIRMED") &&
                  (booking.outstandingBalance === undefined ||
                    booking.outstandingBalance === 0) &&
                  booking.paymentStatus === "FULLY_PAID" && (
                    <div className="mt-3 p-2 bg-green-50 border border-green-200 rounded-md text-center">
                      <p className="text-sm font-medium text-green-700">
                        Paid in Full
                      </p>
                    </div>
                  )}
              </div>

              {/* Action Buttons - Only show for active bookings */}
              {(booking.status === "PENDING" ||
                booking.status === "CONFIRMED") && (
                <div className="mt-4 pt-4 border-t border-gray-300">
                  <div className="grid grid-cols-2 gap-3">
                    <BookingActionButton
                      bookingId={booking.id}
                      bookingStatus={booking.status}
                      actionType="complete"
                      onSuccess={handleBookingUpdate}
                      onError={(error: string) => {
                        console.error("Error marking complete:", error);
                      }}
                    />
                    <BookingActionButton
                      bookingId={booking.id}
                      bookingStatus={booking.status}
                      actionType="no-show"
                      onSuccess={handleBookingUpdate}
                      onError={(error: string) => {
                        console.error("Error marking no-show:", error);
                      }}
                    />
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
