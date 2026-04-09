import { useState } from "react";
import { useAppSelector } from "../store/hooks";
import BarberAvailabilityManager from "../components/barber/BarberAvailabilityManager";
import BarberBookingsManager from "../components/barber/BarberBookingsManager";

/**
 * Type definition for barber dashboard tab navigation
 */
type BarberTab = "availability" | "bookings";

/**
 * Barber Dashboard - Main dashboard for barbers to manage their availability and bookings
 */
export default function BarberDashboard() {
  const user = useAppSelector((state) => state.auth.user)!;
  const [activeTab, setActiveTab] = useState<BarberTab>("bookings");

  return (
    <div className="min-h-screen bg-[var(--bg-base)]">
      <div className="max-w-6xl mx-auto px-4 py-6 md:p-6">
        {/* Page header */}
        <h1 className="text-3xl md:text-4xl font-bold mb-2 text-[var(--text-primary)]">Barber Dashboard</h1>
        <p className="text-[var(--text-muted)] mb-6 md:mb-8">
          Welcome, {user.firstName} {user.lastName}
        </p>

        {/* Tab Navigation */}
        <div className="flex gap-2 md:gap-4 mb-6 md:mb-8 border-b border-[var(--border-subtle)]">
          <button
            onClick={() => setActiveTab("bookings")}
            className={`px-4 md:px-6 py-3 font-semibold transition text-sm md:text-base ${activeTab === "bookings"
              ? "border-b-2 border-[var(--focus-ring)] text-[var(--accent-text)]"
              : "text-[var(--text-muted)] hover:text-[var(--text-primary)]"
              }`}
          >
            My Bookings
          </button>
          <button
            onClick={() => setActiveTab("availability")}
            className={`px-4 md:px-6 py-3 font-semibold transition text-sm md:text-base ${activeTab === "availability"
              ? "border-b-2 border-[var(--focus-ring)] text-[var(--accent-text)]"
              : "text-[var(--text-muted)] hover:text-[var(--text-primary)]"
              }`}
          >
            Availability
          </button>
        </div>

        {/* Bookings Tab */}
        {activeTab === "bookings" && (
          <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
            <h2 className="text-2xl font-bold mb-6 text-[var(--text-primary)]">My Bookings</h2>
            <BarberBookingsManager barberId={user.barberId} />
          </div>
        )}

        {/* Availability Tab */}
        {activeTab === "availability" && (
          <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
            <h2 className="text-2xl font-bold mb-4 text-[var(--text-primary)]">
              My Availability
            </h2>
            <p className="text-[var(--text-muted)] mb-6">
              Set your working hours and days off
            </p>
            <BarberAvailabilityManager barberId={user.barberId} />
          </div>
        )}
      </div>
    </div>
  );
}
