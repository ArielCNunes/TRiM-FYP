import { useState } from "react";
import { useAppSelector } from "../store/hooks";
import { useNavigate } from "react-router-dom";
import AdminTabNavigation, {
  type AdminTab,
} from "../components/admin/AdminTabNavigation";
import AdminDashboard from "../components/admin/dashboard/AdminDashboard";
import CategoriesManager from "../components/admin/categories/CategoriesManager";
import ServicesManager from "../components/admin/services/ServicesManager";
import BarbersManager from "../components/admin/barbers/BarbersManager";

/**
 * Admin Dashboard
 *
 * Allows admins to:
 * 1. Create and manage services
 * 2. Create and manage barbers
 * 3. View all services and barbers in the system
 */
export default function Admin() {
  const navigate = useNavigate();
  const user = useAppSelector((state) => state.auth.user);

  // Verify admin role - redirect if unauthorized
  if (!user || user.role !== "ADMIN") {
    return (
      <div className="p-8 text-center">
        <p className="text-red-600">Access denied. Admin role required.</p>
        <button
          onClick={() => navigate("/")}
          className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md"
        >
          Go Home
        </button>
      </div>
    );
  }

  const [activeTab, setActiveTab] = useState<AdminTab>("dashboard");
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);

  // Handle category click - switch to services tab and filter by category
  const handleCategoryClick = (categoryId: number) => {
    setSelectedCategoryId(categoryId);
    setActiveTab("services");
  };

  // Clear filter when switching tabs manually
  const handleTabChange = (tab: AdminTab) => {
    if (tab !== "services") {
      setSelectedCategoryId(null);
    }
    setActiveTab(tab);
  };

  return (
    <div className="min-h-screen bg-zinc-950">
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-4xl font-bold mb-8 text-white">Admin Dashboard</h1>

        <AdminTabNavigation activeTab={activeTab} onTabChange={handleTabChange} />

        {activeTab === "dashboard" && <AdminDashboard />}
        {activeTab === "categories" && <CategoriesManager onCategoryClick={handleCategoryClick} />}
        {activeTab === "services" && (
          <ServicesManager
            filterByCategoryId={selectedCategoryId}
            onClearFilter={() => setSelectedCategoryId(null)}
          />
        )}
        {activeTab === "barbers" && <BarbersManager />}
      </div>
    </div>
  );
}
