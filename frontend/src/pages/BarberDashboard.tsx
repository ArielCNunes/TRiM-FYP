import { useState, useEffect } from "react";
import { useAppSelector } from "../store/hooks";
import { barbersApi } from "../api/endpoints";

/**
 * Barber Dashboard - Main dashboard for barbers to manage their availability and bookings
 */
export default function BarberDashboard() {
  const user = useAppSelector((state) => state.auth.user);

  // Guard: Require authentication
  if (!user) {
    return (
      <div className="p-8 text-center">
        <p className="text-red-600">Please log in</p>
      </div>
    );
  }

  // Guard: Require BARBER role
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
        <p className="text-gray-600 mb-8">Welcome, {user.firstName}</p>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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
              Upcoming Bookings
            </h2>
            <p className="text-gray-600">Coming soon...</p>
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
    <div className="space-y-4">
      {days.map((day) => (
        <div
          key={day}
          className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg"
        >
          <label className="flex items-center gap-2 flex-1">
            <input
              type="checkbox"
              checked={availability[day].enabled}
              onChange={() => handleToggle(day)}
              className="w-4 h-4 rounded border-gray-300"
            />
            <span className="font-medium text-gray-900 capitalize w-20">
              {day}
            </span>
          </label>

          {availability[day].enabled && (
            <div className="flex gap-2">
              <input
                type="time"
                value={availability[day].start}
                onChange={(e) => handleTimeChange(day, "start", e.target.value)}
                className="px-3 py-1 border border-gray-300 rounded bg-white text-sm"
              />
              <span className="text-gray-600">to</span>
              <input
                type="time"
                value={availability[day].end}
                onChange={(e) => handleTimeChange(day, "end", e.target.value)}
                className="px-3 py-1 border border-gray-300 rounded bg-white text-sm"
              />
            </div>
          )}
        </div>
      ))}

      <button
        onClick={handleSave}
        disabled={loading}
        className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-medium py-2 rounded-md transition mt-4"
      >
        {loading ? "Saving..." : "Save Availability"}
      </button>
    </div>
  );
}
