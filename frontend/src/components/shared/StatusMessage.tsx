interface StatusMessageProps {
  type: "success" | "error";
  message: string;
}

export default function StatusMessage({ type, message }: StatusMessageProps) {
  return (
    <div
      className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${type === "success"
          ? "border-[var(--success-border)] bg-[var(--success-muted)]/20 text-[var(--success-text-light)]"
          : "border-[var(--danger-border)] bg-[var(--danger-muted)]/20 text-[var(--danger-text-light)]"
        }`}
    >
      {message}
    </div>
  );
}
