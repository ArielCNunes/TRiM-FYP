import { useState } from "react";
import type { ServiceCategory } from "../../../types";

interface CategoryCardProps {
    category: ServiceCategory & { active: boolean };
    servicesCount: number;
    activeServicesCount: number;
    onEdit: (category: ServiceCategory) => void;
    onDeactivate: (id: number) => void;
    onClick?: (categoryId: number) => void;
}

export default function CategoryCard({
    category,
    servicesCount,
    activeServicesCount,
    onEdit,
    onDeactivate,
    onClick,
}: CategoryCardProps) {
    const [showDeactivateConfirm, setShowDeactivateConfirm] = useState(false);

    // Allow deactivation only if there are no active services
    const canDeactivate = activeServicesCount === 0;

    const handleDeactivate = () => {
        if (!canDeactivate) {
            return;
        }
        onDeactivate(category.id);
        setShowDeactivateConfirm(false);
    };

    return (
        <div
            className={`p-4 rounded-lg shadow border transition-colors ${category.active
                ? "bg-zinc-900 border-zinc-800 hover:border-zinc-700"
                : "bg-zinc-900/50 border-zinc-800/50"
                } ${onClick && category.active ? "cursor-pointer" : ""}`}
            onClick={() => onClick && category.active && onClick(category.id)}
        >
            <div className="flex justify-between items-start">
                <div>
                    <div className="flex items-center gap-2">
                        <h3 className={`text-lg font-bold ${category.active ? "text-white" : "text-zinc-500"
                            }`}>
                            {category.name}
                        </h3>
                        {!category.active && (
                            <span className="px-2 py-0.5 text-xs font-medium bg-zinc-700 text-zinc-400 rounded">
                                Inactive
                            </span>
                        )}
                    </div>
                    <p className="text-sm text-zinc-400 mt-1">
                        {servicesCount} {servicesCount === 1 ? "service" : "services"}
                    </p>
                </div>
                <div className="flex gap-2">
                    <button
                        onClick={(e) => { e.stopPropagation(); onEdit(category); }}
                        className="text-indigo-400 hover:text-indigo-300 text-sm font-medium"
                    >
                        Edit
                    </button>
                    {category.active && (
                        !showDeactivateConfirm ? (
                            <button
                                onClick={(e) => { e.stopPropagation(); setShowDeactivateConfirm(true); }}
                                className={`text-sm font-medium ${!canDeactivate
                                    ? "text-zinc-600 cursor-not-allowed"
                                    : "text-amber-400 hover:text-amber-300"
                                    }`}
                                disabled={!canDeactivate}
                                title={
                                    !canDeactivate
                                        ? "Deactivate all services in this category first"
                                        : "Deactivate category"
                                }
                            >
                                Deactivate
                            </button>
                        ) : (
                            <div className="flex gap-1">
                                <button
                                    onClick={(e) => { e.stopPropagation(); handleDeactivate(); }}
                                    className="text-amber-400 hover:text-amber-300 text-sm font-medium"
                                >
                                    Confirm
                                </button>
                                <button
                                    onClick={(e) => { e.stopPropagation(); setShowDeactivateConfirm(false); }}
                                    className="text-zinc-400 hover:text-zinc-300 text-sm font-medium"
                                >
                                    Cancel
                                </button>
                            </div>
                        )
                    )}
                </div>
            </div>
        </div>
    );
}
