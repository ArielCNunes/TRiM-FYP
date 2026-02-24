import { CalendarCheck, CalendarClock, CalendarDays, Scissors, Users } from "lucide-react";
import type { DashboardStats } from "../../../types";

interface MetricsGridProps {
  stats: DashboardStats;
}

export default function MetricsGrid({ stats }: MetricsGridProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)] transition-transform duration-200 hover:-translate-y-1 hover:shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm text-[var(--text-muted)]">Total Bookings</p>
          <CalendarCheck className="w-5 h-5 text-[var(--text-muted)]" />
        </div>
        <p className="text-3xl font-bold text-[var(--text-primary)]">{stats.totalBookings.toLocaleString()}</p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)] transition-transform duration-200 hover:-translate-y-1 hover:shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm text-[var(--text-muted)]">Today's Bookings</p>
          <CalendarDays className="w-5 h-5 text-[var(--accent-text)]" />
        </div>
        <p className="text-3xl font-bold text-[var(--accent-text)]">
          {stats.todaysBookings.toLocaleString()}
        </p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)] transition-transform duration-200 hover:-translate-y-1 hover:shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm text-[var(--text-muted)]">Upcoming Bookings</p>
          <CalendarClock className="w-5 h-5 text-[var(--warning-text)]" />
        </div>
        <p className="text-3xl font-bold text-[var(--warning-text)]">
          {stats.upcomingBookings.toLocaleString()}
        </p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)] transition-transform duration-200 hover:-translate-y-1 hover:shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm text-[var(--text-muted)]">Active Customers</p>
          <Users className="w-5 h-5 text-[var(--success-text)]" />
        </div>
        <p className="text-3xl font-bold text-[var(--success-text)]">
          {stats.activeCustomers.toLocaleString()}
        </p>
      </div>

      <div className="bg-[var(--bg-surface)] p-6 rounded-lg shadow border border-[var(--border-subtle)] transition-transform duration-200 hover:-translate-y-1 hover:shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm text-[var(--text-muted)]">Active Barbers</p>
          <Scissors className="w-5 h-5 text-[var(--purple-text)]" />
        </div>
        <p className="text-3xl font-bold text-[var(--purple-text)]">
          {stats.activeBarbers.toLocaleString()}
        </p>
      </div>
    </div>
  );
}
