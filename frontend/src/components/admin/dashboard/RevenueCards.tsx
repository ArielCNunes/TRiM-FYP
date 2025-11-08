import type { DashboardStats } from "../../../types";

interface RevenueCardsProps {
  stats: DashboardStats;
}

export default function RevenueCards({ stats }: RevenueCardsProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div className="bg-white p-6 rounded-lg shadow">
        <p className="text-sm text-gray-600 mb-1">Total Revenue</p>
        <p className="text-3xl font-bold text-gray-900">
          €{stats.totalRevenue.toFixed(2)}
        </p>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <p className="text-sm text-gray-600 mb-1">This Month Revenue</p>
        <p className="text-3xl font-bold text-blue-600">
          €{stats.thisMonthRevenue.toFixed(2)}
        </p>
      </div>
    </div>
  );
}
