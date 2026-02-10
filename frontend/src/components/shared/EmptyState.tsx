interface EmptyStateProps {
  message: string;
}

export default function EmptyState({ message }: EmptyStateProps) {
  return <p className="text-center text-[var(--text-subtle)] py-4">{message}</p>;
}
