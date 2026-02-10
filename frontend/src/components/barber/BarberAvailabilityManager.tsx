import { useState, useEffect } from "react";
import { barbersApi } from "../../api/endpoints";
import BarberBreaksManager from "./BarberBreaksManager";

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
  const [activeTab, setActiveTab] = useState<"hours" | "breaks">("hours");

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
    } catch {
      // Error handled silently - availability will use defaults
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
    } catch {
      // Error handled silently - save operation failed
    } finally {
      setLoading(false);
    }
  };

  // Calculate earliest start and latest end time for break validation
  const getWorkingHoursBounds = () => {
    let earliest = "22:00";
    let latest = "06:00";

    days.forEach((day) => {
      if (availability[day].enabled) {
        if (availability[day].start < earliest) {
          earliest = availability[day].start;
        }
        if (availability[day].end > latest) {
          latest = availability[day].end;
        }
      }
    });

    return { earliest, latest };
  };

  const { earliest: workStartTime, latest: workEndTime } = getWorkingHoursBounds();

  return (
    <div className="space-y-4">
      {/* Tabs */}
      <div className="flex border-b border-[var(--border-default)]">
        <button
          onClick={() => setActiveTab("hours")}
          className={`px-4 py-2 text-sm font-medium transition-colors ${activeTab === "hours"
            ? "text-[var(--accent-text)] border-b-2 border-[var(--accent-text)]"
            : "text-[var(--text-muted)] hover:text-[var(--text-secondary)]"
            }`}
        >
          Working Hours
        </button>
        <button
          onClick={() => setActiveTab("breaks")}
          className={`px-4 py-2 text-sm font-medium transition-colors ${activeTab === "breaks"
            ? "text-[var(--success-text)] border-b-2 border-[var(--success-text)]"
            : "text-[var(--text-muted)] hover:text-[var(--text-secondary)]"
            }`}
        >
          Breaks
        </button>
      </div>

      {/* Working Hours Tab */}
      {activeTab === "hours" && (
        <div className="space-y-3">
          {days.map((day) => (
            <div
              key={day}
              className={`border rounded-lg transition-all ${availability[day].enabled
                ? "border-[var(--border-strong)] bg-[var(--bg-elevated)]/50"
                : "border-[var(--border-default)] bg-[var(--bg-elevated)]"
                }`}
            >
              {/* Day header with toggle */}
              <div className="flex items-center justify-between p-3 border-b border-[var(--border-default)]">
                <label className="flex items-center gap-3 cursor-pointer flex-1">
                  <input
                    type="checkbox"
                    checked={availability[day].enabled}
                    onChange={() => handleToggle(day)}
                    className="w-5 h-5 rounded border-[var(--border-strong)] text-indigo-600 focus:ring-[var(--focus-ring)] bg-[var(--bg-surface)]"
                  />
                  <span className="font-semibold text-[var(--text-primary)] capitalize text-lg">
                    {day}
                  </span>
                </label>
                {!availability[day].enabled && (
                  <span className="text-sm text-[var(--text-subtle)] italic">Day off</span>
                )}
              </div>

              {/* Time selection - only shown when enabled */}
              {availability[day].enabled && (
                <div className="p-4">
                  <div className="flex items-center gap-4">
                    <div className="flex-1">
                      <label className="block text-xs font-medium text-[var(--text-muted)] mb-1">
                        Start Time
                      </label>
                      <select
                        value={availability[day].start}
                        onChange={(e) =>
                          handleTimeChange(day, "start", e.target.value)
                        }
                        className="w-full px-3 py-2 border border-[var(--border-strong)] rounded-md bg-[var(--bg-surface)] text-[var(--text-primary)] focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
                      >
                        {timeOptions.map((time) => (
                          <option key={time.value} value={time.value}>
                            {time.label}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="text-[var(--text-subtle)] pt-6">→</div>

                    <div className="flex-1">
                      <label className="block text-xs font-medium text-[var(--text-muted)] mb-1">
                        End Time
                      </label>
                      <select
                        value={availability[day].end}
                        onChange={(e) =>
                          handleTimeChange(day, "end", e.target.value)
                        }
                        className="w-full px-3 py-2 border border-[var(--border-strong)] rounded-md bg-[var(--bg-surface)] text-[var(--text-primary)] focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
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
            className="w-full bg-[var(--accent)] hover:bg-[var(--accent-hover)] disabled:bg-[var(--bg-muted)] text-[var(--text-primary)] font-semibold py-3 rounded-md transition mt-4 shadow-lg shadow-[var(--accent-shadow)]"
          >
            {loading ? "Saving..." : "Save Availability"}
          </button>
        </div>
      )}

      {/* Breaks Tab */}
      {activeTab === "breaks" && (
        <BarberBreaksManager
          barberId={barberId}
          workStartTime={workStartTime}
          workEndTime={workEndTime}
        />
      )}
    </div>
  );
}
