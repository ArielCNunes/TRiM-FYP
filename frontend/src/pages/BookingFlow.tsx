import { useState, useEffect } from 'react';
import { servicesApi } from '../api/endpoints';
import type { Service } from '../types';
import toast from 'react-hot-toast';

/**
 * BookingFlow Component
 * 
 * First step in the booking process where customers select a service.
 * Displays all active services with their details (name, description, duration, price).
 * Once a service is selected, allows user to proceed to barber selection.
 * 
 */
export default function BookingFlow() {
  // List of available services fetched from API
  const [services, setServices] = useState<Service[]>([]);
  
  // Loading state while fetching services
  const [loading, setLoading] = useState(true);
  
  // Currently selected service by the user
  const [selectedService, setSelectedService] = useState<Service | null>(null);

  // Fetch services on component mount
  useEffect(() => {
    fetchServices();
  }, []);

  /**
   * Fetches active services from the API
   * Sets loading state and handles errors with toast notifications
   */
  const fetchServices = async () => {
    try {
      const response = await servicesApi.getActive();
      setServices(response.data);
    } catch (error) {
      toast.error('Failed to load services');
    } finally {
      // Ensure loading state is cleared regardless of success/failure
      setLoading(false);
    }
  };

  // Show loading indicator while fetching services
  if (loading) {
    return <div className="p-8 text-center">Loading services...</div>;
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* Page title */}
      <h1 className="text-3xl font-bold mb-8">Select a Service</h1>

      {/* Empty state when no services are available */}
      {services.length === 0 ? (
        <p className="text-center text-gray-500">No services available</p>
      ) : (
        // Responsive grid layout for service cards
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {services.map((service) => (
            // Individual service card - clickable to select
            <div
              key={service.id}
              onClick={() => setSelectedService(service)}
              className="border rounded-lg p-6 hover:shadow-lg cursor-pointer transition"
            >
              {/* Service name */}
              <h3 className="text-xl font-bold mb-2">{service.name}</h3>
              
              {/* Service description */}
              <p className="text-gray-600 mb-4">{service.description}</p>
              
              {/* Service duration and price */}
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-500">{service.durationMinutes} min</span>
                <span className="text-lg font-bold text-primary-600">€{service.price.toFixed(2)}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Selected service confirmation and next step button */}
      {selectedService && (
        <div className="mt-8 p-4 bg-blue-50 rounded-lg">
          <p className="text-lg">
            Selected: <strong>{selectedService.name}</strong>
          </p>
          {/* Continue button - currently shows placeholder toast */}
          <button
            className="mt-4 bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
            onClick={() => toast.success('Next: Select a barber (coming soon)')}
          >
            Continue to Barber Selection
          </button>
        </div>
      )}
    </div>
  );
}