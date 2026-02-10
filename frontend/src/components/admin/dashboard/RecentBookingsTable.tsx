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
    <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
      <h3 className="text-xl font-bold mb-4 text-[var(--text-primary)]">Recent Bookings</h3>
      {recentBookings.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-[var(--bg-elevated)]">
              <tr>
                <th className="px-4 py-2 text-left text-xs font-semibold text-[var(--text-muted)]">
                  Customer
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-[var(--text-muted)]">
                  Barber
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-[var(--text-muted)]">
                  Service
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-[var(--text-muted)]">
                  Date
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-[var(--text-muted)]">
                  Time
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-[var(--text-muted)]">
                  Status
                </th>
              </tr>
            </thead>
            <tbody>
              {recentBookings.map((booking, index) => (
                <tr key={index} className="border-t border-[var(--border-subtle)]">
                  <td className="px-4 py-3 text-sm text-[var(--text-primary)]">
                    {booking.customerName}
                  </td>
                  <td className="px-4 py-3 text-sm text-[var(--text-primary)]">
                    {booking.barberName}
                  </td>
                  <td className="px-4 py-3 text-sm text-[var(--text-primary)]">
                    {booking.serviceName}
                  </td>
                  <td className="px-4 py-3 text-sm text-[var(--text-muted)]">
                    {new Date(booking.date).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3 text-sm text-[var(--text-muted)]">
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
