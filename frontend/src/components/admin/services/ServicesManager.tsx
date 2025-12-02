import { useEffect, useState } from "react";
import { categoriesApi, servicesApi } from "../../../api/endpoints";
import type { CategoryWithServices, Service } from "../../../types";
import StatusMessage from "../../shared/StatusMessage";
import EmptyState from "../../shared/EmptyState";
import ServiceForm from "./ServiceForm";
import ServiceCard from "./ServiceCard";

interface ServicesManagerProps {
  filterByCategoryId?: number | null;
  onClearFilter?: () => void;
}

export default function ServicesManager({ filterByCategoryId, onClearFilter }: ServicesManagerProps) {
  const [categories, setCategories] = useState<CategoryWithServices[]>([]);
  const [allCategories, setAllCategories] = useState<CategoryWithServices[]>([]);
  const [allServices, setAllServices] = useState<{ active: Service[]; inactive: Service[] }>({ active: [], inactive: [] });
  const [showForm, setShowForm] = useState(false);
  const [editingService, setEditingService] = useState<Service | null>(null);
  const [loading, setLoading] = useState(true);
  const [showInactive, setShowInactive] = useState(false);
  const [status, setStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  useEffect(() => {
    fetchServices();
  }, []);

  // Apply category filter when filterByCategoryId changes
  useEffect(() => {
    if (filterByCategoryId) {
      setCategories(allCategories.filter(cat => cat.id === filterByCategoryId));
    } else {
      setCategories(allCategories);
    }
  }, [filterByCategoryId, allCategories]);

  const fetchServices = async () => {
    try {
      setLoading(true);
      const response = await categoriesApi.getWithServices();

      // Separate active and inactive services
      const activeServices: Service[] = [];
      const inactiveServices: Service[] = [];

      response.data.forEach((category) => {
        category.services.forEach((service) => {
          const serviceWithCategory = {
            ...service,
            categoryName: category.name,
            categoryId: category.id,
          };
          if (service.active !== false) {
            activeServices.push(serviceWithCategory);
          } else {
            inactiveServices.push(serviceWithCategory);
          }
        });
      });

      setAllServices({ active: activeServices, inactive: inactiveServices });

      // Keep categories with only active services for the main display
      const categoriesWithActiveServices = response.data.map((category) => ({
        ...category,
        services: category.services.filter((service) => service.active !== false),
      }));
      setAllCategories(categoriesWithActiveServices);
      setCategories(categoriesWithActiveServices);
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load services" });
    } finally {
      setLoading(false);
    }
  };

  const handleSuccess = () => {
    setShowForm(false);
    setStatus({
      type: "success",
      message: editingService
        ? "Service updated successfully"
        : "Service created successfully",
    });
    setEditingService(null);
    fetchServices();
  };

  const handleEdit = (service: Service) => {
    setEditingService(service);
    setShowForm(true);
  };

  const handleDeactivate = async (id: number) => {
    try {
      await servicesApi.deactivate(id);
      setStatus({ type: "success", message: "Service deactivated successfully" });
      fetchServices();
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to deactivate service";
      setStatus({ type: "error", message });
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingService(null);
  };

  const totalServices = categories.reduce(
    (acc, cat) => acc + cat.services.length,
    0
  );

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white">Services</h2>
        <button
          onClick={() => {
            setEditingService(null);
            setShowForm(!showForm);
          }}
          className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-500 transition shadow-lg shadow-indigo-500/20"
        >
          {showForm && !editingService ? "Cancel" : "Add Service"}
        </button>
      </div>

      {/* Filter indicator */}
      {filterByCategoryId && categories.length > 0 && (
        <div className="mb-4 px-4 py-2 bg-indigo-600/20 border border-indigo-500/30 rounded-lg flex items-center justify-between">
          <span className="text-indigo-300 text-sm">
            Showing services in: <strong>{categories[0]?.name}</strong>
          </span>
          <button
            onClick={onClearFilter}
            className="text-indigo-400 hover:text-indigo-300 text-sm font-medium flex items-center gap-1"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
            Show all
          </button>
        </div>
      )}

      {showForm && (
        <ServiceForm
          editingService={editingService}
          onSuccess={handleSuccess}
          onCancel={handleCancel}
        />
      )}

      {status && <StatusMessage type={status.type} message={status.message} />}

      {categories.length === 0 ? (
        <EmptyState message="No categories yet. Create a category first to add services." />
      ) : totalServices === 0 && allServices.inactive.length === 0 ? (
        <EmptyState message="No services yet. Create one to get started." />
      ) : (
        <div className="space-y-8">
          {categories
            .filter((category) => category.services.length > 0)
            .map((category) => (
              <div key={category.id}>
                <h3 className="text-lg font-semibold text-zinc-300 mb-4 border-b border-zinc-800 pb-2">
                  {category.name}
                  <span className="ml-2 text-sm text-zinc-500 font-normal">
                    ({category.services.length}{" "}
                    {category.services.length === 1 ? "service" : "services"})
                  </span>
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {category.services.map((service) => (
                    <ServiceCard
                      key={service.id}
                      service={{
                        ...service,
                        categoryName: category.name,
                        categoryId: category.id,
                      }}
                      onEdit={handleEdit}
                      onDeactivate={handleDeactivate}
                    />
                  ))}
                </div>
              </div>
            ))}

          {/* Inactive Services Dropdown */}
          {allServices.inactive.length > 0 && (
            <div className="mt-8 border border-zinc-800 rounded-lg overflow-hidden">
              <button
                onClick={() => setShowInactive(!showInactive)}
                className="w-full px-4 py-3 bg-zinc-900 flex items-center justify-between text-left hover:bg-zinc-800/50 transition-colors"
              >
                <div className="flex items-center gap-2">
                  <span className="text-zinc-400 font-medium">Inactive Services</span>
                  <span className="text-xs bg-zinc-700 text-zinc-400 px-2 py-0.5 rounded-full">
                    {allServices.inactive.length}
                  </span>
                </div>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className={`h-5 w-5 text-zinc-500 transition-transform duration-200 ${showInactive ? "rotate-180" : ""}`}
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>

              {showInactive && (
                <div className="p-4 bg-zinc-900/50 border-t border-zinc-800">
                  <p className="text-xs text-zinc-500 mb-4">
                    These services are hidden from the booking flow. Edit a service to reactivate it.
                  </p>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {allServices.inactive.map((service) => (
                      <ServiceCard
                        key={service.id}
                        service={service}
                        onEdit={handleEdit}
                        onDeactivate={handleDeactivate}
                        isInactive
                      />
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
