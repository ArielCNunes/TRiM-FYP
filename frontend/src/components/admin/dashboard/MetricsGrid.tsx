import type { DashboardStats } from "../../../types";

interface MetricsGridProps {
  stats: DashboardStats;
}

export default function MetricsGrid({ stats }: MetricsGridProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
        <p className="text-sm text-[var(--text-muted)] mb-1">Total Bookings</p>
        <p className="text-3xl font-bold text-[var(--text-primary)]">{stats.totalBookings}</p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
        <p className="text-sm text-[var(--text-muted)] mb-1">Today's Bookings</p>
        <p className="text-3xl font-bold text-[var(--accent-text)]">
          {stats.todaysBookings}
        </p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
        <p className="text-sm text-[var(--text-muted)] mb-1">Active Customers</p>
        <p className="text-3xl font-bold text-[var(--success-text)]">
          {stats.activeCustomers}
        </p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)]">
        <p className="text-sm text-[var(--text-muted)] mb-1">Active Barbers</p>
        <p className="text-3xl font-bold text-[var(--purple-text)]">
          {stats.activeBarbers}
        </p>
      </div>
    </div>
  );
}
