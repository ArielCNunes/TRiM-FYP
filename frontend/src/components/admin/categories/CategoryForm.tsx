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
        <div className="mb-8 p-6 bg-zinc-900 rounded-lg shadow border border-zinc-800">
            <h3 className="text-xl font-bold mb-4 text-white">
                {editingCategory ? "Edit Category" : "Create New Category"}
            </h3>

            {error && (
                <div className="mb-4 p-3 bg-red-900/50 border border-red-800 rounded text-red-300 text-sm">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium mb-1 text-zinc-300">
                        Category Name
                    </label>
                    <input
                        type="text"
                        required
                        className="w-full border border-zinc-700 bg-zinc-800 text-white rounded-md p-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="e.g., Haircuts, Beard Care, Treatments"
                    />
                </div>

                {editingCategory && (
                    <div className="flex items-center justify-between p-3 bg-zinc-800 rounded-md border border-zinc-700">
                        <div>
                            <label className="text-sm font-medium text-zinc-300">
                                Active
                            </label>
                            <p className="text-xs text-zinc-500">
                                Inactive categories won't show in the booking flow
                            </p>
                        </div>
                        <button
                            type="button"
                            onClick={() => setActive(!active)}
                            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${active ? "bg-indigo-600" : "bg-zinc-600"
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
                        className="flex-1 bg-indigo-600 text-white py-2 rounded-md hover:bg-indigo-500 disabled:bg-zinc-700 disabled:cursor-not-allowed transition"
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
                        className="px-6 bg-zinc-800 text-zinc-300 py-2 rounded-md hover:bg-zinc-700 transition border border-zinc-700"
                    >
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}
