import type { DashboardStats } from "../../../types";
import EmptyState from "../../shared/EmptyState";

interface RecentBookingsTableProps {
  recentBookings: DashboardStats["recentBookings"];
}

export default function RecentBookingsTable({ recentBookings }: RecentBookingsTableProps) {
  const getStatusColor = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return "bg-green-100 text-green-800";
      case "CONFIRMED":
        return "bg-blue-100 text-blue-800";
      case "PENDING":
        return "bg-yellow-100 text-yellow-800";
      case "CANCELLED":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-xl font-bold mb-4">Recent Bookings</h3>
      {recentBookings.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                  Customer
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                  Barber
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                  Service
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                  Date
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                  Time
                </th>
                <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600">
                  Status
                </th>
              </tr>
            </thead>
            <tbody>
              {recentBookings.map((booking, index) => (
                <tr key={index} className="border-t">
                  <td className="px-4 py-3 text-sm text-gray-900">
                    {booking.customerName}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-900">
                    {booking.barberName}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-900">
                    {booking.serviceName}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {new Date(booking.date).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">{booking.time}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`px-2 py-1 rounded text-xs font-semibold ${getStatusColor(
                        booking.status
                      )}`}
                    >
                      {booking.status}
                    </span>
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
