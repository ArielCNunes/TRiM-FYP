import type { Service } from "../../types";
import { StatusBanner, StepNavigation } from "../BookingComponents";

/**
 * ServiceSelectionStep Component
 * Step 1 of booking wizard: displays grid of services, handles selection
 * Props: services list, selected service, loading state, status, callbacks for select/continue
 */
export function ServiceSelectionStep({
  services,
  selectedService,
  loading,
  status,
  onSelect,
  onContinue,
}: {
  services: Service[];
  selectedService: Service | null;
  loading: boolean;
  status: { type: "success" | "error"; message: string } | null;
  onSelect: (service: Service) => void;
  onContinue: () => void;
}) {
  // Show loading while fetching services
  if (loading) {
    return <div className="p-8 text-center">Loading services...</div>;
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8 text-white">Select a Service</h1>
      <StatusBanner status={status} />

      {services.length === 0 ? (
        <p className="text-center text-zinc-500">No services available</p>
      ) : (
        // Service grid: clickable cards showing name, description, duration, price
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {services.map((service) => (
            <div
              key={service.id}
              onClick={() => onSelect(service)}
              className={`border rounded-lg p-6 cursor-pointer transition ${
                selectedService?.id === service.id
                  ? "border-indigo-500 bg-indigo-900/20"
                  : "border-zinc-800 bg-zinc-900 hover:border-zinc-600"
              }`}
            >
              <h3 className="text-xl font-bold mb-2 text-white">
                {service.name}
              </h3>
              <p className="text-zinc-400 mb-4">{service.description}</p>
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
      )}

      {/* Selection summary and continue button */}
      {selectedService && (
        <div className="mt-8 p-4 bg-zinc-900 border border-zinc-800 rounded-lg">
          <p className="text-lg mb-4 text-zinc-300">
            Selected:{" "}
            <strong className="text-white">{selectedService.name}</strong> -{" "}
            <span className="text-indigo-400">
              €{selectedService.price.toFixed(2)}
            </span>
          </p>
          <StepNavigation
            onBack={() => {}}
            onContinue={onContinue}
            continueLabel="Continue to Barber Selection"
            showContinue={true}
          />
        </div>
      )}
    </div>
  );
}
