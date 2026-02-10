import type { DashboardStats } from "../../../types";

interface RevenueCardsProps {
  stats: DashboardStats;
}

export default function RevenueCards({ stats }: RevenueCardsProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
        <p className="text-sm text-[var(--text-muted)] mb-1">Total Revenue</p>
        <p className="text-3xl font-bold text-[var(--text-primary)]">
          €{stats.totalRevenue.toFixed(2)}
        </p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
        <p className="text-sm text-[var(--text-muted)] mb-1">This Month Revenue</p>
        <p className="text-3xl font-bold text-[var(--accent-text)]">
          €{stats.thisMonthRevenue.toFixed(2)}
        </p>
      </div>
    </div>
  );
}
