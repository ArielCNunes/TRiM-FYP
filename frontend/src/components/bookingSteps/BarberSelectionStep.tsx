import type { Barber } from "../../types";
import { StatusBanner, StepNavigation } from "../BookingComponents";

/**
 * BarberSelectionStep Component
 * Step 2 of booking wizard: displays grid of barber profiles with photos, bios, and contact info
 * Props: barbers list, selected barber, loading state, status, selected service name, callbacks
 */
export function BarberSelectionStep({
  barbers,
  selectedBarber,
  loading,
  status,
  selectedServiceName,
  onSelect,
  onContinue,
  onBack,
}: {
  barbers: Barber[];
  selectedBarber: Barber | null;
  loading: boolean;
  status: { type: "success" | "error"; message: string } | null;
  selectedServiceName?: string;
  onSelect: (barber: Barber) => void;
  onContinue: () => void;
  onBack: () => void;
}) {
  // Show loading while fetching barbers
  if (loading) {
    return <div className="p-8 text-center">Loading barbers...</div>;
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-2 text-white">Select a Barber</h1>
      <p className="text-zinc-400 mb-8">
        Service: <strong className="text-white">{selectedServiceName}</strong>
      </p>
      <StatusBanner status={status} />

      {barbers.length === 0 ? (
        <p className="text-center text-zinc-500">No barbers available</p>
      ) : (
        // Barber grid: clickable cards with profile image, name, bio, email
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {barbers.map((barber) => (
            <div
              key={barber.id}
              onClick={() => onSelect(barber)}
              className={`border rounded-lg p-6 cursor-pointer transition ${
                selectedBarber?.id === barber.id
                  ? "border-indigo-500 bg-indigo-900/20"
                  : "border-zinc-800 bg-zinc-900 hover:border-zinc-600"
              }`}
            >
              {/* Profile photo */}
              {barber.profileImageUrl && (
                <img
                  src={barber.profileImageUrl}
                  alt={barber.user.firstName}
                  className="w-full h-40 object-cover rounded-lg mb-4"
                />
              )}
              <h3 className="text-xl font-bold mb-2 text-white">
                {barber.user.firstName} {barber.user.lastName}
              </h3>
              {barber.bio && <p className="text-zinc-400 mb-4">{barber.bio}</p>}
              <p className="text-sm text-zinc-500">{barber.user.email}</p>
            </div>
          ))}
        </div>
      )}

      <StepNavigation
        onBack={onBack}
        onContinue={selectedBarber ? onContinue : undefined}
        continueLabel="Continue to Date & Time"
        showContinue={!!selectedBarber}
      />
    </div>
  );
}
