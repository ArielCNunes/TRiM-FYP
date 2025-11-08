import type { DashboardStats } from "../../../types";

interface MetricsGridProps {
  stats: DashboardStats;
}

export default function MetricsGrid({ stats }: MetricsGridProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="bg-white p-6 rounded-lg shadow">
        <p className="text-sm text-gray-600 mb-1">Total Bookings</p>
        <p className="text-3xl font-bold text-gray-900">{stats.totalBookings}</p>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <p className="text-sm text-gray-600 mb-1">Today's Bookings</p>
        <p className="text-3xl font-bold text-blue-600">{stats.todaysBookings}</p>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <p className="text-sm text-gray-600 mb-1">Active Customers</p>
        <p className="text-3xl font-bold text-green-600">{stats.activeCustomers}</p>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <p className="text-sm text-gray-600 mb-1">Active Barbers</p>
        <p className="text-3xl font-bold text-purple-600">{stats.activeBarbers}</p>
      </div>
    </div>
  );
}
