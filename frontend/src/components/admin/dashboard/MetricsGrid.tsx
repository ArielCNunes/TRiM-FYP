import type { DashboardStats } from "../../../types";

interface MetricsGridProps {
  stats: DashboardStats;
}

export default function MetricsGrid({ stats }: MetricsGridProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
        <p className="text-sm text-zinc-400 mb-1">Total Bookings</p>
        <p className="text-3xl font-bold text-white">{stats.totalBookings}</p>
      </div>

      <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
        <p className="text-sm text-zinc-400 mb-1">Today's Bookings</p>
        <p className="text-3xl font-bold text-indigo-400">
          {stats.todaysBookings}
        </p>
      </div>

      <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
        <p className="text-sm text-zinc-400 mb-1">Active Customers</p>
        <p className="text-3xl font-bold text-emerald-400">
          {stats.activeCustomers}
        </p>
      </div>

      <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
        <p className="text-sm text-zinc-400 mb-1">Active Barbers</p>
        <p className="text-3xl font-bold text-purple-400">
          {stats.activeBarbers}
        </p>
      </div>
    </div>
  );
}
