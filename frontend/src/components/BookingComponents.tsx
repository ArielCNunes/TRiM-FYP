/**
 * StatusBanner Component
 * Displays inline success or error status messages (green/red background)
 * Returns null if no status to avoid empty DOM elements
 */
export function StatusBanner({
  status,
}: {
  status: { type: "success" | "error"; message: string } | null;
}) {
  if (!status) return null;

  return (
    <div
      className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
        status.type === "success"
          ? "border-emerald-800 bg-emerald-900/20 text-emerald-300"
          : "border-red-800 bg-red-900/20 text-red-300"
      }`}
    >
      {status.message}
    </div>
  );
}

/**
 * StepNavigation Component
 * Reusable back/continue button pair for booking steps
 * Supports custom labels, conditional visibility, and loading states
 */
export function StepNavigation({
  onBack,
  onContinue,
  continueLabel = "Continue",
  showContinue = true,
  loading = false,
}: {
  onBack: () => void;
  onContinue?: () => void;
  continueLabel?: string;
  showContinue?: boolean;
  loading?: boolean;
}) {
  return (
    <div className="mt-8 flex gap-4">
      <button
        onClick={onBack}
        className="px-6 py-2 border border-zinc-700 rounded-md hover:bg-zinc-800 text-zinc-300"
      >
        Back
      </button>
      {showContinue && onContinue && (
        <button
          onClick={onContinue}
          disabled={loading}
          className="bg-indigo-600 text-white px-6 py-2 rounded-md hover:bg-indigo-500 disabled:bg-zinc-700 shadow-lg shadow-indigo-500/20"
        >
          {continueLabel}
        </button>
      )}
    </div>
  );
}
