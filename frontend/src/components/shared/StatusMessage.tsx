interface StatusMessageProps {
  type: "success" | "error";
  message: string;
}

export default function StatusMessage({ type, message }: StatusMessageProps) {
  return (
    <div
      className={`mb-6 rounded-md border px-4 py-3 text-sm font-medium ${
        type === "success"
          ? "border-emerald-800 bg-emerald-900/20 text-emerald-300"
          : "border-red-800 bg-red-900/20 text-red-300"
      }`}
    >
      {message}
    </div>
  );
}
