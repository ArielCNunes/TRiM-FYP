import type { DashboardStats } from "../../../types";

interface RevenueCardsProps {
  stats: DashboardStats;
}

export default function RevenueCards({ stats }: RevenueCardsProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
        <p className="text-sm text-zinc-400 mb-1">Total Revenue</p>
        <p className="text-3xl font-bold text-white">
          €{stats.totalRevenue.toFixed(2)}
        </p>
      </div>

      <div className="bg-zinc-900 p-6 rounded-lg shadow border border-zinc-800">
        <p className="text-sm text-zinc-400 mb-1">This Month Revenue</p>
        <p className="text-3xl font-bold text-indigo-400">
          €{stats.thisMonthRevenue.toFixed(2)}
        </p>
      </div>
    </div>
  );
}
