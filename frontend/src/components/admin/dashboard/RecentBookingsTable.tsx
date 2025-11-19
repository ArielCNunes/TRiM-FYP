import type { DashboardStats } from "../../../types";
import EmptyState from "../../shared/EmptyState";
import StatusBadge from "../../shared/StatusBadge";

interface RecentBookingsTableProps {
  recentBookings: DashboardStats["recentBookings"];
}

export default function RecentBookingsTable({
  recentBookings,
}: RecentBookingsTableProps) {
  return (
    <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
      <h3 className="text-xl font-bold mb-4 text-white">Recent Bookings</h3>
      {recentBookings.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-zinc-800">
              <tr>
                <th className="px-4 py-2 text-left text-xs font-semibold text-zinc-400">
                  Customer
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-zinc-400">
                  Barber
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-zinc-400">
                  Service
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-zinc-400">
                  Date
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-zinc-400">
                  Time
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-zinc-400">
                  Status
                </th>
              </tr>
            </thead>
            <tbody>
              {recentBookings.map((booking, index) => (
                <tr key={index} className="border-t border-zinc-800">
                  <td className="px-4 py-3 text-sm text-white">
                    {booking.customerName}
                  </td>
                  <td className="px-4 py-3 text-sm text-white">
                    {booking.barberName}
                  </td>
                  <td className="px-4 py-3 text-sm text-white">
                    {booking.serviceName}
                  </td>
                  <td className="px-4 py-3 text-sm text-zinc-400">
                    {new Date(booking.date).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3 text-sm text-zinc-400">
                    {booking.time}
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge status={booking.status} type="booking" />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState message="No recent bookings" />
      )}
    </div>
  );
}
