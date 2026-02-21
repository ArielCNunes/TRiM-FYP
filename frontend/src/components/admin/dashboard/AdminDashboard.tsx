import { useEffect, useState } from "react";
import { dashboardApi } from "../../../api/endpoints";
import type { DashboardStats } from "../../../types";
import LoadingSpinner from "../../shared/LoadingSpinner";
import MetricsGrid from "./MetricsGrid";
import RevenueCards from "./RevenueCards";
import PopularServices from "./PopularServices";
import RecentBookingsTable from "./RecentBookingsTable";

export default function AdminDashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      setLoading(true);
      const response = await dashboardApi.getAdminStats();
      setStats(response.data);
      setError(null);
    } catch {
      setError("Failed to load dashboard statistics");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading dashboard..." />;
  }

  if (error || !stats) {
    return (
      <div className="text-center py-12">
        <p className="text-[var(--danger-text)]">{error || "Failed to load statistics"}</p>
        <button
          onClick={fetchStats}
          className="mt-4 bg-[var(--accent)] text-[var(--text-primary)] px-4 py-2 rounded-md hover:bg-[var(--accent-hover)]"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <MetricsGrid stats={stats} />
      <RevenueCards stats={stats} />
      <PopularServices popularServices={stats.popularServices} />
      <RecentBookingsTable recentBookings={stats.recentBookings} />
    </div>
  );
}
