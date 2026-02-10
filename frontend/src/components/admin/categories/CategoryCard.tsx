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
                ? "bg-[var(--bg-surface)] border-[var(--border-subtle)] hover:border-[var(--border-default)]"
                : "bg-[var(--bg-surface)]/50 border-[var(--border-subtle)]/50"
                } ${onClick && category.active ? "cursor-pointer" : ""}`}
            onClick={() => onClick && category.active && onClick(category.id)}
        >
            <div className="flex justify-between items-start">
                <div>
                    <div className="flex items-center gap-2">
                        <h3 className={`text-lg font-bold ${category.active ? "text-[var(--text-primary)]" : "text-[var(--text-subtle)]"
                            }`}>
                            {category.name}
                        </h3>
                        {!category.active && (
                            <span className="px-2 py-0.5 text-xs font-medium bg-[var(--bg-muted)] text-[var(--text-muted)] rounded">
                                Inactive
                            </span>
                        )}
                    </div>
                    <p className="text-sm text-[var(--text-muted)] mt-1">
                        {servicesCount} {servicesCount === 1 ? "service" : "services"}
                    </p>
                </div>
                <div className="flex gap-2">
                    <button
                        onClick={(e) => { e.stopPropagation(); onEdit(category); }}
                        className="text-[var(--accent-text)] hover:text-[var(--accent-hover)] text-sm font-medium"
                    >
                        Edit
                    </button>
                    {category.active && (
                        !showDeactivateConfirm ? (
                            <button
                                onClick={(e) => { e.stopPropagation(); setShowDeactivateConfirm(true); }}
                                className={`text-sm font-medium ${!canDeactivate
                                    ? "text-[var(--text-subtle)] cursor-not-allowed"
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
                                    className="text-[var(--text-muted)] hover:text-[var(--text-secondary)] text-sm font-medium"
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
