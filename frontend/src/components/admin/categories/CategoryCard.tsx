import { useState } from "react";
import type { ServiceCategory } from "../../../types";

interface CategoryCardProps {
    category: ServiceCategory;
    servicesCount: number;
    onEdit: (category: ServiceCategory) => void;
    onDelete: (id: number) => void;
}

export default function CategoryCard({
    category,
    servicesCount,
    onEdit,
    onDelete,
}: CategoryCardProps) {
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const handleDelete = () => {
        if (servicesCount > 0) {
            // Don't allow deletion if services exist
            return;
        }
        onDelete(category.id);
        setShowDeleteConfirm(false);
    };

    return (
        <div className="p-4 bg-zinc-900 rounded-lg shadow border border-zinc-800">
            <div className="flex justify-between items-start">
                <div>
                    <h3 className="text-lg font-bold text-white">{category.name}</h3>
                    <p className="text-sm text-zinc-400 mt-1">
                        {servicesCount} {servicesCount === 1 ? "service" : "services"}
                    </p>
                </div>
                <div className="flex gap-2">
                    <button
                        onClick={() => onEdit(category)}
                        className="text-indigo-400 hover:text-indigo-300 text-sm font-medium"
                    >
                        Edit
                    </button>
                    {!showDeleteConfirm ? (
                        <button
                            onClick={() => setShowDeleteConfirm(true)}
                            className={`text-sm font-medium ${servicesCount > 0
                                    ? "text-zinc-600 cursor-not-allowed"
                                    : "text-red-400 hover:text-red-300"
                                }`}
                            disabled={servicesCount > 0}
                            title={
                                servicesCount > 0
                                    ? "Remove all services from this category first"
                                    : "Delete category"
                            }
                        >
                            Delete
                        </button>
                    ) : (
                        <div className="flex gap-1">
                            <button
                                onClick={handleDelete}
                                className="text-red-400 hover:text-red-300 text-sm font-medium"
                            >
                                Confirm
                            </button>
                            <button
                                onClick={() => setShowDeleteConfirm(false)}
                                className="text-zinc-400 hover:text-zinc-300 text-sm font-medium"
                            >
                                Cancel
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
