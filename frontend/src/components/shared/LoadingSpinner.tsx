interface LoadingSpinnerProps {
  message?: string;
}

export default function LoadingSpinner({
  message = "Loading...",
}: LoadingSpinnerProps) {
  return (
    <div className="text-center py-12">
      <p className="text-zinc-500">{message}</p>
    </div>
  );
}
