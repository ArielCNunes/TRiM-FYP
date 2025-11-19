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
  const user = useAppSelector((state) => state.auth.user);
  const [activeTab, setActiveTab] = useState<BarberTab>("bookings");

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
    <div className="min-h-screen bg-zinc-950">
      <div className="max-w-6xl mx-auto p-6">
        {/* Page header */}
        <h1 className="text-4xl font-bold mb-2 text-white">Barber Dashboard</h1>
        <p className="text-zinc-400 mb-8">
          Welcome, {user.firstName} {user.lastName}
        </p>

        {/* Tab Navigation */}
        <div className="flex gap-4 mb-8 border-b border-zinc-800">
          <button
            onClick={() => setActiveTab("bookings")}
            className={`px-6 py-3 font-semibold transition ${
              activeTab === "bookings"
                ? "border-b-2 border-indigo-500 text-indigo-400"
                : "text-zinc-400 hover:text-white"
            }`}
          >
            My Bookings
          </button>
          <button
            onClick={() => setActiveTab("availability")}
            className={`px-6 py-3 font-semibold transition ${
              activeTab === "availability"
                ? "border-b-2 border-indigo-500 text-indigo-400"
                : "text-zinc-400 hover:text-white"
            }`}
          >
            Availability
          </button>
        </div>

        {/* Bookings Tab */}
        {activeTab === "bookings" && (
          <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
            <h2 className="text-2xl font-bold mb-6 text-white">My Bookings</h2>
            <BarberBookingsManager barberId={user.barberId} />
          </div>
        )}

        {/* Availability Tab */}
        {activeTab === "availability" && (
          <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
            <h2 className="text-2xl font-bold mb-4 text-white">
              My Availability
            </h2>
            <p className="text-zinc-400 mb-6">
              Set your working hours and days off
            </p>
            <BarberAvailabilityManager barberId={user.barberId} />
          </div>
        )}
      </div>
    </div>
  );
}
