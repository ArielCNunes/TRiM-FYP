interface EmptyStateProps {
  message: string;
}

export default function EmptyState({ message }: EmptyStateProps) {
  return <p className="text-center text-zinc-500 py-4">{message}</p>;
}
