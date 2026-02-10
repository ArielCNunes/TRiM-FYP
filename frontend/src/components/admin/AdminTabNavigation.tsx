type AdminTab = "dashboard" | "categories" | "services" | "barbers" | "customers";

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
  ];

  return (
    <div className="flex gap-4 mb-8 border-b border-[var(--border-subtle)]">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => onTabChange(tab.id)}
          className={`px-6 py-3 font-semibold transition ${activeTab === tab.id
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
