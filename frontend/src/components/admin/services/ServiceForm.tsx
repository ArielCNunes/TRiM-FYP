import { useState, useEffect } from "react";
import { servicesApi, categoriesApi } from "../../../api/endpoints";
import type { Service, ServiceCategory } from "../../../types";

interface ServiceFormProps {
  editingService?: Service | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function ServiceForm({
  editingService,
  onSuccess,
  onCancel,
}: ServiceFormProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [categories, setCategories] = useState<ServiceCategory[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    durationMinutes: 30,
    price: 0,
    depositPercentage: 50,
    categoryId: 0,
    active: true,
  });

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    if (editingService) {
      setFormData({
        name: editingService.name,
        description: editingService.description || "",
        durationMinutes: editingService.durationMinutes,
        price: editingService.price,
        depositPercentage: editingService.depositPercentage,
        categoryId: editingService.categoryId || 0,
        active: editingService.active,
      });
    }
  }, [editingService]);

  const fetchCategories = async () => {
    try {
      const response = await categoriesApi.getAll();
      setCategories(response.data);
      // Set default category if available and not editing
      if (response.data.length > 0 && !editingService) {
        setFormData((prev) => ({ ...prev, categoryId: response.data[0].id }));
      }
    } catch {
      // Error handled silently - categories will remain empty
    } finally {
      setLoadingCategories(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const serviceData = {
        name: formData.name,
        description: formData.description,
        durationMinutes: parseInt(String(formData.durationMinutes)),
        price: parseFloat(String(formData.price)),
        depositPercentage: parseInt(String(formData.depositPercentage)),
        active: formData.active,
        categoryId: formData.categoryId,
      };

      if (editingService) {
        await servicesApi.update(editingService.id, serviceData);
      } else {
        await servicesApi.create(serviceData);
      }

      setFormData({
        name: "",
        description: "",
        durationMinutes: 30,
        price: 0,
        depositPercentage: 50,
        categoryId: categories.length > 0 ? categories[0].id : 0,
        active: true,
      });
      onSuccess();
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        `Failed to ${editingService ? "update" : "create"} service`;
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  if (loadingCategories) {
    return (
      <div className="mb-8 p-6 bg-[var(--bg-surface)] rounded-lg shadow border border-[var(--border-subtle)]">
        <p className="text-[var(--text-muted)]">Loading categories...</p>
      </div>
    );
  }

  if (categories.length === 0) {
    return (
      <div className="mb-8 p-6 bg-[var(--bg-surface)] rounded-lg shadow border border-[var(--border-subtle)]">
        <p className="text-amber-400">Please create a category first before adding services.</p>
        <button
          type="button"
          onClick={onCancel}
          className="mt-4 px-6 bg-[var(--bg-elevated)] text-[var(--text-secondary)] py-2 rounded-md hover:bg-[var(--bg-muted)] transition border border-[var(--border-default)]"
        >
          Cancel
        </button>
      </div>
    );
  }

  return (
    <div className="mb-8 p-6 bg-[var(--bg-surface)] rounded-lg shadow border border-[var(--border-subtle)]">
      <h3 className="text-xl font-bold mb-4 text-[var(--text-primary)]">
        {editingService ? "Edit Service" : "Create New Service"}
      </h3>

      {error && (
        <div className="mb-4 p-3 bg-[var(--danger-muted)]/50 border border-[var(--danger-border)] rounded text-[var(--danger-text-light)] text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1 text-[var(--text-secondary)]">
            Category
          </label>
          <select
            required
            className="w-full border border-[var(--border-default)] bg-[var(--bg-elevated)] text-[var(--text-primary)] rounded-md p-2 focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
            value={formData.categoryId}
            onChange={(e) =>
              setFormData({ ...formData, categoryId: parseInt(e.target.value) })
            }
          >
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-[var(--text-secondary)]">
            Service Name
          </label>
          <input
            type="text"
            required
            className="w-full border border-[var(--border-default)] bg-[var(--bg-elevated)] text-[var(--text-primary)] rounded-md p-2 focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-[var(--text-secondary)]">
            Description
          </label>
          <textarea
            required
            className="w-full border border-[var(--border-default)] bg-[var(--bg-elevated)] text-[var(--text-primary)] rounded-md p-2 focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
            rows={3}
            value={formData.description}
            onChange={(e) =>
              setFormData({ ...formData, description: e.target.value })
            }
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1 text-[var(--text-secondary)]">
              Duration (minutes)
            </label>
            <input
              type="number"
              required
              min="15"
              step="15"
              className="w-full border border-[var(--border-default)] bg-[var(--bg-elevated)] text-[var(--text-primary)] rounded-md p-2 focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
              value={formData.durationMinutes}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  durationMinutes: parseInt(e.target.value),
                })
              }
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1 text-[var(--text-secondary)]">
              Price (€)
            </label>
            <input
              type="number"
              required
              min="0"
              step="0.01"
              className="w-full border border-[var(--border-default)] bg-[var(--bg-elevated)] text-[var(--text-primary)] rounded-md p-2 focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
              value={formData.price}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  price: parseFloat(e.target.value),
                })
              }
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-[var(--text-secondary)]">
            Deposit Percentage (%)
          </label>
          <input
            type="number"
            required
            min="0"
            max="100"
            step="1"
            className="w-full border border-[var(--border-default)] bg-[var(--bg-elevated)] text-[var(--text-primary)] rounded-md p-2 focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
            value={formData.depositPercentage}
            onChange={(e) =>
              setFormData({
                ...formData,
                depositPercentage: parseInt(e.target.value),
              })
            }
          />
          <p className="text-xs text-[var(--text-subtle)] mt-1">
            Percentage of the total price required as deposit (0-100%)
          </p>
        </div>

        <div className="flex items-center justify-between p-3 bg-[var(--bg-elevated)] rounded-md border border-[var(--border-default)]">
          <div>
            <label className="text-sm font-medium text-[var(--text-secondary)]">
              Active
            </label>
            <p className="text-xs text-[var(--text-subtle)]">
              Inactive services won't appear in the booking flow
            </p>
          </div>
          <button
            type="button"
            onClick={() => setFormData({ ...formData, active: !formData.active })}
            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${formData.active ? "bg-[var(--accent)]" : "bg-[var(--bg-subtle)]"
              }`}
          >
            <span
              className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${formData.active ? "translate-x-6" : "translate-x-1"
                }`}
            />
          </button>
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={loading}
            className="flex-1 bg-[var(--accent)] text-[var(--text-primary)] py-2 rounded-md hover:bg-[var(--accent-hover)] disabled:bg-[var(--bg-muted)] transition"
          >
            {loading
              ? editingService
                ? "Updating..."
                : "Creating..."
              : editingService
                ? "Update Service"
                : "Create Service"}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="px-6 bg-[var(--bg-elevated)] text-[var(--text-secondary)] py-2 rounded-md hover:bg-[var(--bg-muted)] transition border border-[var(--border-default)]"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
