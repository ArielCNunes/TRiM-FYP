import { useState, useEffect } from "react";
import { barbersApi } from "../../api/endpoints";

/**
 * Availability Manager - Allows barbers to manage their working hours for each day
 * Uses the barberId (separate from userId) to save/load availability settings
 */
export default function BarberAvailabilityManager({
  barberId,
}: {
  barberId?: number;
}) {
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

      // Create a fresh mapped object with default values
      const mapped: any = {
        monday: { start: "09:00", end: "17:00", enabled: true, id: null },
        tuesday: { start: "09:00", end: "17:00", enabled: true, id: null },
        wednesday: { start: "09:00", end: "17:00", enabled: true, id: null },
        thursday: { start: "09:00", end: "17:00", enabled: true, id: null },
        friday: { start: "09:00", end: "17:00", enabled: true, id: null },
        saturday: { start: "10:00", end: "16:00", enabled: false, id: null },
        sunday: { start: "09:00", end: "17:00", enabled: false, id: null },
      };

      // Map backend data to the state
      data.forEach((item: any) => {
        const dayKey = item.dayOfWeek.toLowerCase();
        if (mapped[dayKey]) {
          // Remove seconds from time format (e.g., "08:30:00" -> "08:30")
          const startTime = item.startTime.substring(0, 5);
          const endTime = item.endTime.substring(0, 5);

          mapped[dayKey] = {
            start: startTime,
            end: endTime,
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
