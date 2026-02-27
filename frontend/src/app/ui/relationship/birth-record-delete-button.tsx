"use client";

import { useState, useActionState } from "react";
import { deleteBirthRecord } from "@/app/lib/actions";

export default function DeleteBirthRecordButton({ brId }: { brId: number }) {
    const action = deleteBirthRecord.bind(null, brId);
    const [state, formAction] = useActionState(action, {});
    const [open, setOpen] = useState(false);

    return (
        <>
            {/* Delete Button */}
            <button
                onClick={() => setOpen(true)}
                className="px-3 py-2 bg-red-400/15 border border-red-500 hover:bg-red-700 hover:text-white rounded text-red-500 rounded-full transition-colors"
            >
                Delete Birth Record
            </button>

            {/* Modal */}
            {open && (
                <div className="fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm flex items-center justify-center" >
                    <div className="bg-gray-800 p-6 rounded-xl w-full max-w-md shadow-xl">
                        <h2 className="text-xl font-semibold mb-3 text-red-400">
                            Confirm Deletion
                        </h2>

                        <p className="mb-6 text-gray-300">
                            This will permanently delete this birth record, removing it from inference calculations, and deleting the connection between the parents and child.
                        </p>
                        <p className="font-bold mb-4">This action cannot be undone.</p>

                        <form action={formAction} className="flex justify-end gap-3">
                            <button
                                type="button"
                                onClick={() => setOpen(false)}
                                className="px-3 py-2 bg-gray-600 hover:bg-gray-500 rounded"
                            >
                                Cancel
                            </button>

                            <button
                                type="submit"
                                className="px-3 py-2 bg-red-600 hover:bg-red-700 rounded text-white"
                            >
                                Confirm Delete
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
