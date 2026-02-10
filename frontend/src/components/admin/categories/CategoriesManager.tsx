import { useEffect, useState } from "react";
import { categoriesApi } from "../../../api/endpoints";
import type { ServiceCategory, CategoryWithServices } from "../../../types";
import StatusMessage from "../../shared/StatusMessage";
import EmptyState from "../../shared/EmptyState";
import CategoryForm from "./CategoryForm";
import CategoryCard from "./CategoryCard";

interface CategoriesManagerProps {
    onCategoryClick?: (categoryId: number) => void;
}

export default function CategoriesManager({ onCategoryClick }: CategoriesManagerProps) {
    const [categories, setCategories] = useState<CategoryWithServices[]>([]);
    const [showForm, setShowForm] = useState(false);
    const [editingCategory, setEditingCategory] = useState<ServiceCategory | null>(null);
    const [status, setStatus] = useState<{
        type: "success" | "error";
        message: string;
    } | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        try {
            setLoading(true);
            const response = await categoriesApi.getWithServices();
            setCategories(response.data);
            setStatus(null);
        } catch (error) {
            setStatus({ type: "error", message: "Failed to load categories" });
        } finally {
            setLoading(false);
        }
    };

    const handleSuccess = () => {
        setShowForm(false);
        setEditingCategory(null);
        setStatus({
            type: "success",
            message: editingCategory
                ? "Category updated successfully"
                : "Category created successfully",
        });
        fetchCategories();
    };

    const handleEdit = (category: ServiceCategory) => {
        setEditingCategory(category);
        setShowForm(true);
    };

    const handleDeactivate = async (id: number) => {
        try {
            await categoriesApi.deactivate(id);
            setStatus({ type: "success", message: "Category deactivated successfully" });
            fetchCategories();
        } catch (error: any) {
            const message =
                error.response?.data?.message || "Failed to deactivate category";
            setStatus({ type: "error", message });
        }
    };

    const handleCancel = () => {
        setShowForm(false);
        setEditingCategory(null);
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[var(--focus-ring)]"></div>
            </div>
        );
    }

    return (
        <div>
            <div className="mb-6 flex justify-between items-center">
                <h2 className="text-2xl font-bold text-[var(--text-primary)]">Service Categories</h2>
                <button
                    onClick={() => {
                        setEditingCategory(null);
                        setShowForm(!showForm);
                    }}
                    className="bg-[var(--accent)] text-[var(--text-primary)] px-4 py-2 rounded-md hover:bg-[var(--accent-hover)] transition shadow-lg shadow-[var(--accent-shadow)]"
                >
                    {showForm && !editingCategory ? "Cancel" : "Add Category"}
                </button>
            </div>

            {showForm && (
                <CategoryForm
                    editingCategory={editingCategory}
                    onSuccess={handleSuccess}
                    onCancel={handleCancel}
                />
            )}

            {status && <StatusMessage type={status.type} message={status.message} />}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {categories.map((category) => (
                    <CategoryCard
                        key={category.id}
                        category={category}
                        servicesCount={category.services.length}
                        activeServicesCount={category.services.filter(s => s.active).length}
                        onEdit={handleEdit}
                        onDeactivate={handleDeactivate}
                        onClick={onCategoryClick}
                    />
                ))}
            </div>

            {categories.length === 0 && (
                <EmptyState message="No categories yet. Create one to organize your services." />
            )}
        </div>
    );
}
