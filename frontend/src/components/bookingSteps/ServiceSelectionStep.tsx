import { useState } from "react";
import type { Service, CategoryWithServices } from "../../types";
import { StatusBanner, StepNavigation } from "../BookingComponents";

/**
 * ServiceSelectionStep Component
 * Step 1 of booking wizard: displays services grouped by category, handles selection
 * Props: categories with services, selected service, loading state, status, callbacks for select/continue
 */
export function ServiceSelectionStep({
  categories,
  selectedService,
  loading,
  status,
  onSelect,
  onContinue,
}: {
  categories: CategoryWithServices[];
  services: Service[];
  selectedService: Service | null;
  loading: boolean;
  status: { type: "success" | "error"; message: string } | null;
  onSelect: (service: Service) => void;
  onContinue: () => void;
}) {
  const [expandedCategories, setExpandedCategories] = useState<Set<number>>(
    () => new Set(categories.map((c) => c.id))
  );

  const toggleCategory = (categoryId: number) => {
    setExpandedCategories((prev) => {
      const next = new Set(prev);
      if (next.has(categoryId)) {
        next.delete(categoryId);
      } else {
        next.add(categoryId);
      }
      return next;
    });
  };

  // Show loading while fetching services
  if (loading) {
    return (
      <div className="p-8 text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500 mx-auto mb-4"></div>
        <p className="text-zinc-400">Loading services...</p>
      </div>
    );
  }

  // Filter categories that have active services
  const categoriesWithServices = categories.filter(
    (cat) => cat.services.length > 0
  );

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8 text-white">Select a Service</h1>
      <StatusBanner status={status} />

      {categoriesWithServices.length === 0 ? (
        <p className="text-center text-zinc-500">No services available</p>
      ) : (
        // Services grouped by category with collapsible sections
        <div className="space-y-6">
          {categoriesWithServices.map((category) => (
            <div
              key={category.id}
              className="border border-zinc-800 rounded-lg overflow-hidden"
            >
              {/* Category header - clickable to expand/collapse */}
              <button
                onClick={() => toggleCategory(category.id)}
                className="w-full flex justify-between items-center p-4 bg-zinc-900/50 hover:bg-zinc-900 transition text-left"
              >
                <div>
                  <h2 className="text-xl font-semibold text-white">
                    {category.name}
                  </h2>
                  <p className="text-sm text-zinc-500">
                    {category.services.length}{" "}
                    {category.services.length === 1 ? "service" : "services"}
                  </p>
                </div>
                <svg
                  className={`w-5 h-5 text-zinc-400 transition-transform ${expandedCategories.has(category.id) ? "rotate-180" : ""
                    }`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>

              {/* Services grid - shown when category is expanded */}
              {expandedCategories.has(category.id) && (
                <div className="p-4 bg-zinc-950">
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {category.services.map((service) => (
                      <div
                        key={service.id}
                        onClick={() =>
                          onSelect({
                            ...service,
                            categoryId: category.id,
                            categoryName: category.name,
                          })
                        }
                        className={`border rounded-lg p-5 cursor-pointer transition ${selectedService?.id === service.id
                          ? "border-indigo-500 bg-indigo-900/20"
                          : "border-zinc-800 bg-zinc-900 hover:border-zinc-600"
                          }`}
                      >
                        <h3 className="text-lg font-bold mb-2 text-white">
                          {service.name}
                        </h3>
                        <p className="text-zinc-400 text-sm mb-4">
                          {service.description}
                        </p>
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-zinc-500">
                            {service.durationMinutes} min
                          </span>
                          <span className="text-lg font-bold text-indigo-400">
                            €{service.price.toFixed(2)}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Selection summary and continue button */}
      {selectedService && (
        <div className="mt-8 p-4 bg-zinc-900 border border-zinc-800 rounded-lg">
          <p className="text-lg mb-4 text-zinc-300">
            Selected:{" "}
            <strong className="text-white">{selectedService.name}</strong>
            {selectedService.categoryName && (
              <span className="text-zinc-500 text-sm ml-2">
                ({selectedService.categoryName})
              </span>
            )}
            {" - "}
            <span className="text-indigo-400">
              €{selectedService.price.toFixed(2)}
            </span>
          </p>
          <StepNavigation
            onBack={() => { }}
            onContinue={onContinue}
            continueLabel="Continue to Barber Selection"
            showContinue={true}
          />
        </div>
      )}
    </div>
  );
}
