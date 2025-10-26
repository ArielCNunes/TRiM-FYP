/**
 * StatusBanner Component
 * Displays inline success or error status messages (green/red background)
 * Returns null if no status to avoid empty DOM elements
 */
export function StatusBanner({ status }: { status: { type: 'success' | 'error'; message: string } | null }) {
  if (!status) return null;

  return (
    <div
      className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
        status.type === 'success'
          ? 'border-green-200 bg-green-50 text-green-700'
          : 'border-red-200 bg-red-50 text-red-700'
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
  continueLabel = 'Continue',
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
        className="px-6 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
      >
        Back
      </button>
      {showContinue && onContinue && (
        <button
          onClick={onContinue}
          disabled={loading}
          className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
        >
          {continueLabel}
        </button>
      )}
    </div>
  );
}
