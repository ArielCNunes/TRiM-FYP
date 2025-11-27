import { useEffect, useState } from "react";
import { categoriesApi } from "../../../api/endpoints";
import type { ServiceCategory, CategoryWithServices } from "../../../types";
import StatusMessage from "../../shared/StatusMessage";
import EmptyState from "../../shared/EmptyState";
import CategoryForm from "./CategoryForm";
import CategoryCard from "./CategoryCard";

export default function CategoriesManager() {
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

    const handleDelete = async (id: number) => {
        try {
            await categoriesApi.delete(id);
            setStatus({ type: "success", message: "Category deleted successfully" });
            fetchCategories();
        } catch (error: any) {
            const message =
                error.response?.data?.message || "Failed to delete category";
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
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
            </div>
        );
    }

    return (
        <div>
            <div className="mb-6 flex justify-between items-center">
                <h2 className="text-2xl font-bold text-white">Service Categories</h2>
                <button
                    onClick={() => {
                        setEditingCategory(null);
                        setShowForm(!showForm);
                    }}
                    className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-500 transition shadow-lg shadow-indigo-500/20"
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
                        onEdit={handleEdit}
                        onDelete={handleDelete}
                    />
                ))}
            </div>

            {categories.length === 0 && (
                <EmptyState message="No categories yet. Create one to organize your services." />
            )}
        </div>
    );
}
