import { Euro, TrendingUp } from "lucide-react";
import type { DashboardStats } from "../../../types";

interface RevenueCardsProps {
  stats: DashboardStats;
}

export default function RevenueCards({ stats }: RevenueCardsProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)] transition-transform duration-200 hover:-translate-y-1 hover:shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm text-[var(--text-muted)]">Total Revenue</p>
          <Euro className="w-5 h-5 text-[var(--text-muted)]" />
        </div>
        <p className="text-2xl md:text-3xl font-bold text-[var(--text-primary)]">
          €{stats.totalRevenue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)] transition-transform duration-200 hover:-translate-y-1 hover:shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm text-[var(--text-muted)]">This Month Revenue</p>
          <TrendingUp className="w-5 h-5 text-[var(--accent-text)]" />
        </div>
        <p className="text-2xl md:text-3xl font-bold text-[var(--accent-text)]">
          €{stats.thisMonthRevenue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </p>
      </div>
    </div>
  );
}
