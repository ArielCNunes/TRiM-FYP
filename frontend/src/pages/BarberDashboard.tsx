import { useState, useEffect } from "react";
import { useAppSelector } from "../store/hooks";
import { barbersApi, bookingsApi } from "../api/endpoints";

/**
 * Barber Dashboard - Main dashboard for barbers to manage their availability and bookings
 */
export default function BarberDashboard() {
  const user = useAppSelector((state) => state.auth.user);

  if (!user) {
    return (
      <div className="p-8 text-center">
        <p className="text-red-600">Please log in</p>
      </div>
    );
  }

  if (user.role !== "BARBER") {
    return (
      <div className="p-8 text-center">
        <p className="text-red-600">Access denied</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-4xl font-bold mb-2 text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mb-8">
          Welcome, {user.firstName} {user.lastName}
        </p>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white border border-gray-200 p-6 rounded-lg shadow-sm">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              My Availability
            </h2>
            <p className="text-gray-600 mb-4">
              Set your working hours and days off
            </p>
            <AvailabilitySection barberId={user.barberId} />
          </div>

          <div className="bg-white border border-gray-200 p-6 rounded-lg shadow-sm">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Your Upcoming Bookings
            </h2>
            <UpcomingBookings barberId={user.barberId} />
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * Availability Section - Allows barbers to manage their working hours for each day
 * Uses the barberId (separate from userId) to save/load availability settings
 */
function AvailabilitySection({ barberId }: { barberId?: number }) {
  // Track availability settings for each day of the week
  const [availability, setAvailability] = useState({
    monday: { start: "09:00", end: "17:00", enabled: true, id: null },
    tuesday: { start: "09:00", end: "17:00", enabled: true, id: null },
    wednesday: { start: "09:00", end: "17:00", enabled: true, id: null },
    thursday: { start: "09:00", end: "17:00", enabled: true, id: null },
    friday: { start: "09:00", end: "17:00", enabled: true, id: null },
    saturday: { start: "10:00", end: "16:00", enabled: false, id: null },
    sunday: { start: "09:00", end: "17:00", enabled: false, id: null },
  });
  const [loading, setLoading] = useState(false);

  const days = [
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "saturday",
    "sunday",
  ] as const;

  // Generate time options from 6:00 AM to 10:00 PM in 30-minute intervals
  const timeOptions: { value: string; label: string }[] = [];
  for (let hour = 6; hour <= 22; hour++) {
    for (let minute = 0; minute < 60; minute += 30) {
      const timeStr = `${String(hour).padStart(2, "0")}:${String(
        minute
      ).padStart(2, "0")}`;
      const displayTime = new Date(`2000-01-01T${timeStr}`).toLocaleTimeString(
        "en-US",
        {
          hour: "numeric",
          minute: "2-digit",
          hour12: true,
        }
      );
      timeOptions.push({ value: timeStr, label: displayTime });
    }
  }

  // Load existing availability
  useEffect(() => {
    if (!barberId) return;
    loadAvailability();
  }, [barberId]);

  const loadAvailability = async () => {
    try {
      const response = await barbersApi.getAvailability(barberId!);
      const data = response.data;

      // Map backend data to frontend state
      const mapped = { ...availability };
      data.forEach((item: any) => {
        const dayKey = item.dayOfWeek.toLowerCase();
        if (mapped[dayKey as (typeof days)[number]]) {
          mapped[dayKey as (typeof days)[number]] = {
            start: item.startTime,
            end: item.endTime,
            enabled: item.isAvailable,
            id: item.id,
          };
        }
      });
      setAvailability(mapped);
    } catch (error) {
      console.error("Failed to load availability", error);
    }
  };

  // Toggle day availability on/off
  const handleToggle = (day: (typeof days)[number]) => {
    setAvailability((prev) => ({
      ...prev,
      [day]: { ...prev[day], enabled: !prev[day].enabled },
    }));
  };

  // Update start or end time for a specific day
  const handleTimeChange = (
    day: (typeof days)[number],
    field: "start" | "end",
    value: string
  ) => {
    setAvailability((prev) => ({
      ...prev,
      [day]: { ...prev[day], [field]: value },
    }));
  };

  // Save availability settings to backend - uses barberId, not userId
  const handleSave = async () => {
    if (!barberId) return;
    setLoading(true);
    try {
      for (const day of days) {
        const slot = availability[day];
        const dayUpper = day.toUpperCase();

        if (slot.id) {
          // Update existing
          await barbersApi.updateAvailability(slot.id, {
            dayOfWeek: dayUpper,
            startTime: slot.start,
            endTime: slot.end,
            isAvailable: slot.enabled,
          });
        } else {
          // Create new - must not include 'id'
          await barbersApi.setAvailability({
            barberId,
            dayOfWeek: dayUpper,
            startTime: slot.start,
            endTime: slot.end,
            isAvailable: slot.enabled,
          });
        }
      }
      console.log("Availability saved successfully");
    } catch (error: any) {
      console.error("Failed to save:", error.response?.data || error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-3">
      {days.map((day) => (
        <div
          key={day}
          className={`border rounded-lg transition-all ${
            availability[day].enabled
              ? "border-blue-200 bg-blue-50"
              : "border-gray-200 bg-gray-50"
          }`}
        >
          {/* Day header with toggle */}
          <div className="flex items-center justify-between p-3 border-b border-gray-200">
            <label className="flex items-center gap-3 cursor-pointer flex-1">
              <input
                type="checkbox"
                checked={availability[day].enabled}
                onChange={() => handleToggle(day)}
                className="w-5 h-5 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="font-semibold text-gray-900 capitalize text-lg">
                {day}
              </span>
            </label>
            {!availability[day].enabled && (
              <span className="text-sm text-gray-500 italic">Day off</span>
            )}
          </div>

          {/* Time selection - only shown when enabled */}
          {availability[day].enabled && (
            <div className="p-4">
              <div className="flex items-center gap-4">
                <div className="flex-1">
                  <label className="block text-xs font-medium text-gray-600 mb-1">
                    Start Time
                  </label>
                  <select
                    value={availability[day].start}
                    onChange={(e) =>
                      handleTimeChange(day, "start", e.target.value)
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md bg-white text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    {timeOptions.map((time) => (
                      <option key={time.value} value={time.value}>
                        {time.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="text-gray-400 pt-6">→</div>

                <div className="flex-1">
                  <label className="block text-xs font-medium text-gray-600 mb-1">
                    End Time
                  </label>
                  <select
                    value={availability[day].end}
                    onChange={(e) =>
                      handleTimeChange(day, "end", e.target.value)
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md bg-white text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    {timeOptions.map((time) => (
                      <option key={time.value} value={time.value}>
                        {time.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          )}
        </div>
      ))}

      <button
        onClick={handleSave}
        disabled={loading}
        className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-3 rounded-md transition mt-4"
      >
        {loading ? "Saving..." : "Save Availability"}
      </button>
    </div>
  );
}

function UpcomingBookings({ barberId }: { barberId?: number }) {
  const [bookings, setBookings] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!barberId) return;
    loadBookings();
  }, [barberId]);

  const loadBookings = async () => {
    try {
      const response = await bookingsApi.getBarberBookings(barberId!);
      // Filter for upcoming bookings only
      const upcoming = response.data.filter(
        (b: any) => b.status === "PENDING" || b.status === "CONFIRMED"
      );
      // Sort by date ascending
      upcoming.sort(
        (a: any, b: any) =>
          new Date(a.bookingDate).getTime() - new Date(b.bookingDate).getTime()
      );
      setBookings(upcoming);
    } catch (error) {
      console.error("Failed to load bookings", error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <p className="text-gray-600">Loading bookings...</p>;
  }

  if (bookings.length === 0) {
    return <p className="text-gray-600">No upcoming bookings</p>;
  }

  return (
    <div className="space-y-4">
      {bookings.map((booking) => (
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
                with {booking.customer.firstName} {booking.customer.lastName}
              </p>
            </div>
            <span
              className={`px-2 py-1 rounded text-xs font-semibold ${
                booking.status === "CONFIRMED"
                  ? "bg-green-100 text-green-800"
                  : "bg-yellow-100 text-yellow-800"
              }`}
            >
              {booking.status}
            </span>
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
                  {booking.paymentStatus === "DEPOSIT_PAID" && "Deposit Paid"}
                  {booking.paymentStatus === "FULLY_PAID" && "Fully Paid"}
                  {booking.paymentStatus === "PENDING" && "Pending Payment"}
                  {booking.paymentStatus === "REFUNDED" && "Refunded"}
                  {![
                    "DEPOSIT_PAID",
                    "FULLY_PAID",
                    "PENDING",
                    "REFUNDED",
                  ].includes(booking.paymentStatus) && booking.paymentStatus}
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

            {/* Outstanding Balance Alert */}
            {booking.outstandingBalance !== undefined &&
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

            {/* Fully Paid Indicator */}
            {(booking.outstandingBalance === undefined ||
              booking.outstandingBalance === 0) &&
              booking.paymentStatus === "FULLY_PAID" && (
                <div className="mt-3 p-2 bg-green-50 border border-green-200 rounded-md text-center">
                  <p className="text-sm font-medium text-green-700">
                    Paid in Full
                  </p>
                </div>
              )}
          </div>
        </div>
      ))}
    </div>
  );
}
