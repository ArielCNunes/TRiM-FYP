type AdminTab = "dashboard" | "services" | "barbers";

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
    { id: "services", label: "Services" },
    { id: "barbers", label: "Barbers" },
  ];

  return (
    <div className="flex gap-4 mb-8 border-b border-zinc-800">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => onTabChange(tab.id)}
          className={`px-6 py-3 font-semibold transition ${
            activeTab === tab.id
              ? "border-b-2 border-indigo-500 text-indigo-400"
              : "text-zinc-400 hover:text-white"
          }`}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}

export type { AdminTab };
