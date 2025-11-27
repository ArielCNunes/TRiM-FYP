import { useEffect, useState } from "react";
import { categoriesApi } from "../../../api/endpoints";
import type { CategoryWithServices } from "../../../types";
import StatusMessage from "../../shared/StatusMessage";
import EmptyState from "../../shared/EmptyState";
import ServiceForm from "./ServiceForm";
import ServiceCard from "./ServiceCard";

export default function ServicesManager() {
  const [categories, setCategories] = useState<CategoryWithServices[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  useEffect(() => {
    fetchServices();
  }, []);

  const fetchServices = async () => {
    try {
      setLoading(true);
      const response = await categoriesApi.getWithServices();
      setCategories(response.data);
      setStatus(null);
    } catch (error) {
      setStatus({ type: "error", message: "Failed to load services" });
    } finally {
      setLoading(false);
    }
  };

  const handleSuccess = () => {
    setShowForm(false);
    setStatus({ type: "success", message: "Service created successfully" });
    fetchServices();
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
          onClick={() => setShowForm(!showForm)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-500 transition shadow-lg shadow-indigo-500/20"
        >
          {showForm ? "Cancel" : "Add Service"}
        </button>
      </div>

      {showForm && (
        <ServiceForm
          onSuccess={handleSuccess}
          onCancel={() => setShowForm(false)}
        />
      )}

      {status && <StatusMessage type={status.type} message={status.message} />}

      {categories.length === 0 ? (
        <EmptyState message="No categories yet. Create a category first to add services." />
      ) : totalServices === 0 ? (
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
                      service={{ ...service, categoryName: category.name }}
                    />
                  ))}
                </div>
              </div>
            ))}
        </div>
      )}
    </div>
  );
}
