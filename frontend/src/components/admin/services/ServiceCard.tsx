import type { Service } from "../../../types";

interface ServiceCardProps {
  service: Service;
}

export default function ServiceCard({ service }: ServiceCardProps) {
  return (
    <div className="p-4 bg-zinc-900 rounded-lg shadow border border-zinc-800">
      <div className="flex justify-between items-start mb-2">
        <h3 className="text-lg font-bold text-white">{service.name}</h3>
        {service.categoryName && (
          <span className="text-xs bg-indigo-600/30 text-indigo-300 px-2 py-1 rounded-full">
            {service.categoryName}
          </span>
        )}
      </div>
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
