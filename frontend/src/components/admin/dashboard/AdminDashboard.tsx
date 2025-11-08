import { useEffect, useState } from "react";
import { dashboardApi } from "../../../api/endpoints";
import type { DashboardStats } from "../../../types";
import LoadingSpinner from "../../shared/LoadingSpinner";
import MetricsGrid from "./MetricsGrid";
import RevenueCards from "./RevenueCards";
import PopularServicesList from "./PopularServicesList";
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
    } catch (err) {
      setError("Failed to load dashboard statistics");
      console.error("Error fetching stats:", err);
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
        <p className="text-red-600">{error || "Failed to load statistics"}</p>
        <button
          onClick={fetchStats}
          className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
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
      <PopularServicesList popularServices={stats.popularServices} />
      <RecentBookingsTable recentBookings={stats.recentBookings} />
    </div>
  );
}
