"use client";

import { useState, useActionState } from "react";
import { deleteSheep } from "@/app/lib/actions";

export default function DeleteSheepButton({ sheepId }: { sheepId: number }) {
    const action = deleteSheep.bind(null, sheepId);
    const [state, formAction, isPending] = useActionState(action, {});
    const [open, setOpen] = useState(false);

    return (
        <>
            {/* Delete Button */}
            <button
                onClick={() => setOpen(true)}
                className="px-3 py-2 bg-red-400/15 border border-red-500 hover:bg-red-700 hover:text-white rounded text-red-500 rounded-full transition-colors"
            >
                Delete Sheep
            </button>

            {/* Modal */}
            {open && (
                <div className="fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm flex items-center justify-center" >
                    <div className="bg-gray-800 p-6 rounded-xl w-full max-w-md shadow-xl">
                        <h2 className="text-xl font-semibold mb-3 text-red-400">
                            Confirm Deletion
                        </h2>

                        <p className="mb-6 text-gray-300">
                            This will permanently delete this sheep and associated data including relationships and its birth record.
                        </p>
                        <p className="font-bold mb-4">This action cannot be undone.</p>

                        <form action={formAction} className="flex justify-end gap-3">
                            <button
                                type="button"
                                disabled={isPending}
                                onClick={() => setOpen(false)}
                                className="px-3 py-2 bg-gray-600 hover:bg-gray-500 rounded"
                            >
                                Cancel
                            </button>

                            <button
                                type="submit"
                                disabled={isPending}
                                className="px-3 py-2 bg-red-600 hover:bg-red-700 rounded text-white"
                            >
                                <div className="flex gap-2 items-center">
                                    {isPending ? "Deleting..." : "Confirm Delete"}
                                    {isPending && (
                                        <svg
                                            className="w-4 h-4 animate-spin"
                                            viewBox="0 0 24 24"
                                            fill="none"
                                        >
                                            <circle
                                                className="opacity-25"
                                                cx="12"
                                                cy="12"
                                                r="10"
                                                stroke="currentColor"
                                                strokeWidth="4"
                                            />
                                            <path
                                                className="opacity-75"
                                                fill="currentColor"
                                                d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
                                            />
                                        </svg>
                                    )}
                                </div>
                            </button>
                        </form>

                        {state?.errors && (
                            <p className="text-red-400 mt-4">{state.errors.join(", ")}</p>
                        )}
                    </div>
                </div>
            )}
        </>
    );
}
