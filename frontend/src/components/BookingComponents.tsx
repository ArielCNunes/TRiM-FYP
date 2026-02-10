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
      className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${status.type === "success"
          ? "border-[var(--success-border)] bg-[var(--success-muted)]/20 text-[var(--success-text-light)]"
          : "border-[var(--danger-border)] bg-[var(--danger-muted)]/20 text-[var(--danger-text-light)]"
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
        className="px-6 py-2 border border-[var(--border-default)] rounded-md hover:bg-[var(--bg-elevated)] text-[var(--text-secondary)]"
      >
        Back
      </button>
      {showContinue && onContinue && (
        <button
          onClick={onContinue}
          disabled={loading}
          className="bg-[var(--accent)] text-[var(--text-primary)] px-6 py-2 rounded-md hover:bg-[var(--accent-hover)] disabled:bg-[var(--bg-muted)] shadow-lg shadow-[var(--accent-shadow)]"
        >
          {continueLabel}
        </button>
      )}
    </div>
  );
}
