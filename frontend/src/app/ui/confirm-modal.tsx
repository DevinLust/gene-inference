"use client";

import { ReactNode } from "react";

type ConfirmActionModalProps = {
    open: boolean;
    title: string;
    description: ReactNode;
    confirmLabel: string;
    pendingLabel: string;
    confirmClassName?: string;
    isPending?: boolean;
    errorMessage?: string | null;
    onCancel: () => void;
    onConfirm: () => void;
};

export default function ConfirmActionModal({
                                               open,
                                               title,
                                               description,
                                               confirmLabel,
                                               pendingLabel,
                                               confirmClassName = "bg-blue-600 hover:bg-blue-700 text-white",
                                               isPending = false,
                                               errorMessage,
                                               onCancel,
                                               onConfirm,
                                           }: ConfirmActionModalProps) {
    if (!open) return null;

    return (
        <div
            className="fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm flex items-center justify-center"
            onMouseDown={(e) => {
                if (e.target === e.currentTarget) {
                    onCancel();
                }
            }}
        >
            <div className="bg-gray-800 p-6 rounded-xl w-full max-w-md shadow-xl">
                <h2 className="text-xl font-semibold mb-3">{title}</h2>

                <div className="mb-6 text-gray-300">
                    {description}
                </div>

                <div className="flex justify-end gap-3">
                    <button
                        type="button"
                        disabled={isPending}
                        onClick={onCancel}
                        className="px-3 py-2 bg-gray-600 hover:bg-gray-500 rounded"
                    >
                        Cancel
                    </button>

                    <button
                        type="button"
                        disabled={isPending}
                        onClick={onConfirm}
                        className={`px-3 py-2 rounded ${confirmClassName} disabled:opacity-50`}
                    >
                        <div className="flex gap-2 items-center">
                            {isPending ? pendingLabel : confirmLabel}
                            {isPending && (
                                <svg className="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none">
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
                </div>

                {errorMessage && (
                    <p className="text-red-400 mt-4">{errorMessage}</p>
                )}
            </div>
        </div>
    );
}
