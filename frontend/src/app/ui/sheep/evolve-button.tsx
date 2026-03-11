"use client";

import { useState, useTransition } from "react";
import { evolveSheep } from "@/app/lib/actions";
import { Category } from "@/app/lib/definitions";
import ConfirmActionModal from "@/app/ui/confirm-modal";

type Props = {
    sheepId: number;
    category: Category;
};

export default function EvolveButton({ sheepId, category }: Props) {
    const [open, setOpen] = useState(false);
    const [isPending, startTransition] = useTransition();
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const handleConfirm = () => {
        setErrorMessage(null);

        startTransition(async () => {
            try {
                await evolveSheep(sheepId, category);
                setOpen(false);
            } catch (error) {
                setErrorMessage("Failed to evolve sheep.");
            }
        });
    };

    return (
        <>
            <button
                onClick={() => setOpen(true)}
                disabled={isPending}
                className="text-sm px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
            >
                Evolve
            </button>

            <ConfirmActionModal
                open={open}
                title="Confirm Evolution"
                description={
                    <>
                        <p>
                            This will raise the phenotype for <span className="font-semibold">{category}</span>.
                        </p>
                        <p className="mt-3 font-semibold text-yellow-300">
                            This action cannot be undone automatically.
                        </p>
                    </>
                }
                confirmLabel="Confirm Evolve"
                pendingLabel="Evolving..."
                confirmClassName="bg-blue-600 hover:bg-blue-700 text-white"
                isPending={isPending}
                errorMessage={errorMessage}
                onCancel={() => setOpen(false)}
                onConfirm={handleConfirm}
            />
        </>
    );
}