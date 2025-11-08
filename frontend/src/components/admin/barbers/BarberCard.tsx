import type { Barber } from "../../../types";

interface BarberCardProps {
  barber: Barber;
}

export default function BarberCard({ barber }: BarberCardProps) {
  return (
    <div className="p-4 bg-white rounded-lg shadow">
      <h3 className="text-lg font-bold mb-1">
        {barber.user.firstName} {barber.user.lastName}
      </h3>
      <p className="text-sm text-gray-600 mb-2">{barber.user.email}</p>
      {barber.bio && (
        <p className="text-sm text-gray-700 mb-2">{barber.bio}</p>
      )}
      <p className="text-xs text-gray-500">{barber.user.phone}</p>
    </div>
  );
}
