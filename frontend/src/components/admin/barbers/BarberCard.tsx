import type { Barber } from "../../../types";

interface BarberCardProps {
  barber: Barber;
}

export default function BarberCard({ barber }: BarberCardProps) {
  return (
    <div className="p-4 bg-zinc-900 rounded-lg shadow border border-zinc-800">
      <h3 className="text-lg font-bold mb-1 text-white">
        {barber.user.firstName} {barber.user.lastName}
      </h3>
      <p className="text-sm text-zinc-400 mb-2">{barber.user.email}</p>
      {barber.bio && <p className="text-sm text-zinc-300 mb-2">{barber.bio}</p>}
      <p className="text-xs text-zinc-500">{barber.user.phone}</p>
    </div>
  );
}
