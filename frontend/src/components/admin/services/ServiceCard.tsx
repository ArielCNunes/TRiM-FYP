import type { Service } from "../../../types";

interface ServiceCardProps {
  service: Service;
}

export default function ServiceCard({ service }: ServiceCardProps) {
  return (
    <div className="p-4 bg-zinc-900 rounded-lg shadow border border-zinc-800">
      <h3 className="text-lg font-bold mb-2 text-white">{service.name}</h3>
      <p className="text-zinc-400 mb-3 text-sm">{service.description}</p>
      <div className="space-y-1 text-sm text-zinc-500">
        <div className="flex justify-between">
          <span>Duration:</span>
          <span>{service.durationMinutes} min</span>
        </div>
        <div className="flex justify-between">
          <span>Price:</span>
          <span className="font-semibold text-zinc-300">
            €{service.price.toFixed(2)}
          </span>
        </div>
        <div className="flex justify-between">
          <span>Deposit:</span>
          <span>{service.depositPercentage}%</span>
        </div>
      </div>
    </div>
  );
}
