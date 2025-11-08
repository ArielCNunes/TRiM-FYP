import type { DashboardStats } from "../../../types";
import EmptyState from "../../shared/EmptyState";

interface PopularServicesListProps {
  popularServices: DashboardStats["popularServices"];
}

export default function PopularServicesList({ popularServices }: PopularServicesListProps) {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-xl font-bold mb-4">Popular Services</h3>
      {popularServices.length > 0 ? (
        <div className="space-y-2">
          {popularServices.map((service, index) => (
            <div
              key={index}
              className="flex justify-between items-center p-3 bg-gray-50 rounded"
            >
              <span className="font-medium text-gray-900">{service.name}</span>
              <span className="text-sm text-gray-600">
                {service.count} bookings
              </span>
            </div>
          ))}
        </div>
      ) : (
        <EmptyState message="No services data yet" />
      )}
    </div>
  );
}
