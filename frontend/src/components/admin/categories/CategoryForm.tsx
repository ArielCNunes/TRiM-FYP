import { useState, useEffect } from "react";
import { categoriesApi } from "../../../api/endpoints";
import type { ServiceCategory } from "../../../types";

interface CategoryFormProps {
    editingCategory?: ServiceCategory | null;
    onSuccess: () => void;
    onCancel: () => void;
}

export default function CategoryForm({
    editingCategory,
    onSuccess,
    onCancel,
}: CategoryFormProps) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [name, setName] = useState("");
    const [active, setActive] = useState(true);

    useEffect(() => {
        if (editingCategory) {
            setName(editingCategory.name);
            setActive(editingCategory.active);
        } else {
            setName("");
            setActive(true);
        }
    }, [editingCategory]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            if (editingCategory) {
                await categoriesApi.update(editingCategory.id, { name: name.trim(), active });
            } else {
                await categoriesApi.create(name.trim());
            }
            setName("");
            setActive(true);
            onSuccess();
        } catch (err: any) {
            const message =
                err.response?.data?.message ||
                `Failed to ${editingCategory ? "update" : "create"} category`;
            setError(message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="mb-8 p-6 bg-[var(--bg-surface)] rounded-lg shadow border border-[var(--border-subtle)]">
            <h3 className="text-xl font-bold mb-4 text-[var(--text-primary)]">
                {editingCategory ? "Edit Category" : "Create New Category"}
            </h3>

            {error && (
                <div className="mb-4 p-3 bg-[var(--danger-muted)]/50 border border-[var(--danger-border)] rounded text-[var(--danger-text-light)] text-sm">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium mb-1 text-[var(--text-secondary)]">
                        Category Name
                    </label>
                    <input
                        type="text"
                        required
                        className="w-full border border-[var(--border-default)] bg-[var(--bg-elevated)] text-[var(--text-primary)] rounded-md p-2 focus:ring-2 focus:ring-[var(--focus-ring)] focus:border-[var(--focus-ring)]"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="e.g., Haircuts, Beard Care, Treatments"
                    />
                </div>

                {editingCategory && (
                    <div className="flex items-center justify-between p-3 bg-[var(--bg-elevated)] rounded-md border border-[var(--border-default)]">
                        <div>
                            <label className="text-sm font-medium text-[var(--text-secondary)]">
                                Active
                            </label>
                            <p className="text-xs text-[var(--text-subtle)]">
                                Inactive categories won't show in the booking flow
                            </p>
                        </div>
                        <button
                            type="button"
                            onClick={() => setActive(!active)}
                            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${active ? "bg-[var(--accent)]" : "bg-[var(--border-strong)]"
                                }`}
                        >
                            <span
                                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${active ? "translate-x-6" : "translate-x-1"
                                    }`}
                            />
                        </button>
                    </div>
                )}

                <div className="flex gap-2">
                    <button
                        type="submit"
                        disabled={loading || !name.trim()}
                        className="flex-1 bg-[var(--accent)] text-[var(--text-primary)] py-2 rounded-md hover:bg-[var(--accent-hover)] disabled:bg-[var(--bg-muted)] disabled:cursor-not-allowed transition"
                    >
                        {loading
                            ? editingCategory
                                ? "Updating..."
                                : "Creating..."
                            : editingCategory
                                ? "Update Category"
                                : "Create Category"}
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
