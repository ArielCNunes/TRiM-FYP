import { StatusBanner, StepNavigation } from "../BookingComponents";

/**
 * DateTimeSelectionStep Component
 * Step 3 of booking wizard: date picker and time slot selection
 * Shows barber name, handles date/time selection with availability fetching
 */
export function DateTimeSelectionStep({
  selectedDate,
  availableSlots,
  selectedTime,
  loading,
  status,
  selectedBarberName,
  minDate,
  onDateChange,
  onTimeSelect,
  onContinue,
  onBack,
}: {
  selectedDate: string;
  availableSlots: string[];
  selectedTime: string;
  loading: boolean;
  status: { type: "success" | "error"; message: string } | null;
  selectedBarberName?: string;
  minDate: string;
  onDateChange: (date: string) => void;
  onTimeSelect: (time: string) => void;
  onContinue: () => void;
  onBack: () => void;
}) {
  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-2 text-white">Select Date & Time</h1>
      <p className="text-zinc-400 mb-8">
        Barber: <strong className="text-white">{selectedBarberName}</strong>
      </p>
      <StatusBanner status={status} />

      <div className="space-y-6">
        {/* Date picker: constrained to future dates (minDate = tomorrow) */}
        <div>
          <label className="block text-lg font-semibold mb-2 text-zinc-300">
            Date
          </label>
          <input
            type="date"
            min={minDate}
            value={selectedDate}
            onChange={(e) => onDateChange(e.target.value)}
            className="w-full border border-zinc-700 rounded-lg p-3 bg-zinc-900 text-white focus:border-indigo-500 focus:outline-none"
          />
        </div>

        {/* Time slots: appears only after date selection, shows loading or available times */}
        {selectedDate && (
          <div>
            <label className="block text-lg font-semibold mb-2 text-zinc-300">
              Available Times
            </label>
            {loading ? (
              <p className="text-zinc-500">Loading available slots...</p>
            ) : availableSlots.length === 0 ? (
              <p className="text-zinc-500">No available slots for this date</p>
            ) : (
              // Time slot buttons: selectable grid with highlight for selected time
              <div className="grid grid-cols-4 gap-2">
                {availableSlots.map((slot) => (
                  <button
                    key={slot}
                    onClick={() => onTimeSelect(slot)}
                    className={`border rounded-lg p-3 text-center font-semibold transition ${
                      selectedTime === slot
                        ? "bg-indigo-600 text-white border-indigo-600"
                        : "bg-zinc-900 text-zinc-300 border-zinc-700 hover:border-indigo-500 hover:text-white"
                    }`}
                  >
                    {slot}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      <StepNavigation
        onBack={onBack}
        onContinue={selectedTime ? onContinue : undefined}
        continueLabel="Continue to Confirmation"
        showContinue={!!selectedTime}
      />
    </div>
  );
}
