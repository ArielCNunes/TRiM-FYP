import { useState } from "react";
import { useAppSelector } from "../store/hooks";
import AdminTabNavigation, {
  type AdminTab,
} from "../components/admin/AdminTabNavigation";
import AdminDashboard from "../components/admin/dashboard/AdminDashboard";
import CategoriesManager from "../components/admin/categories/CategoriesManager";
import ServicesManager from "../components/admin/services/ServicesManager";
import BarbersManager from "../components/admin/barbers/BarbersManager";
import CustomersManager from "../components/admin/customers/CustomersManager";
import PaymentsSettings from "../components/admin/payments/PaymentsSettings";

/**
 * Admin Dashboard
 *
 * Allows admins to:
 * 1. Create and manage services
 * 2. Create and manage barbers
 * 3. View all services and barbers in the system
 */
export default function Admin() {
  const user = useAppSelector((state) => state.auth.user)!;

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
    <div className="min-h-screen bg-[var(--bg-base)]">
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-4xl font-bold mb-8 text-[var(--text-primary)]">Admin Dashboard</h1>

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
        {activeTab === "customers" && <CustomersManager />}
        {activeTab === "payments" && <PaymentsSettings />}
      </div>
    </div>
  );
}
