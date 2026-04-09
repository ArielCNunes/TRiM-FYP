type AdminTab = "dashboard" | "categories" | "services" | "barbers" | "customers" | "payments";

interface AdminTabNavigationProps {
  activeTab: AdminTab;
  onTabChange: (tab: AdminTab) => void;
}

export default function AdminTabNavigation({
  activeTab,
  onTabChange,
}: AdminTabNavigationProps) {
  const tabs: { id: AdminTab; label: string }[] = [
    { id: "dashboard", label: "Dashboard" },
    { id: "categories", label: "Categories" },
    { id: "services", label: "Services" },
    { id: "barbers", label: "Barbers" },
    { id: "customers", label: "Customers" },
    { id: "payments", label: "Payments" },
  ];

  return (
    <div className="flex gap-2 md:gap-4 mb-6 md:mb-8 border-b border-[var(--border-subtle)] overflow-x-auto scrollbar-hide -mx-4 px-4 md:mx-0 md:px-0">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => onTabChange(tab.id)}
          className={`px-3 md:px-6 py-3 font-semibold transition whitespace-nowrap text-sm md:text-base ${activeTab === tab.id
            ? "border-b-2 border-b-[var(--focus-ring)] text-[var(--accent-text)]"
            : "text-[var(--text-muted)] hover:text-[var(--text-primary)]"
            }`}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}

export type { AdminTab };
