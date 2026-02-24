"use client";

import { useState, useActionState, useEffect, useRef } from "react";
import type { UpdateSheepState } from "@/app/lib/actions";
import { updateSheep } from "@/app/lib/actions";
import { Loader2 } from "lucide-react";

export default function EditableSheepName({
                                              sheepId,
                                              initialName,
                                          }: {
    sheepId: number;
    initialName: string;
}) {
    const action = updateSheep.bind(null, sheepId);
    const [state, formAction, isPending] = useActionState<UpdateSheepState, FormData>(action, {});
    const [editing, setEditing] = useState(false);
    const [dismissed, setDismissed] = useState(false);

    const prevPendingRef = useRef<boolean>(false);

    useEffect(() => {
        const success = !!state?.success;

        // show error message if failed
        if (editing && !success && !isPending && prevPendingRef.current) {
            setDismissed(false);
        }

        // close only when success JUST turned true
        if (editing && success && !isPending && prevPendingRef.current) {
            setEditing(false);
        }

        prevPendingRef.current = isPending;
    }, [state?.success, isPending, editing]);

    useEffect(() => {
        if (state?.message) {
            setDismissed(false); // new message → show it
        }
    }, [state?.message]);

    return (
        <div>
            {!editing ? (
                <div className="flex items-center gap-2">
                    <h1 className="text-2xl font-bold">
                        {initialName || <span className="text-gray-400">(unnamed)</span>}
                    </h1>
                    <button
                        className="text-sm px-2 py-1 rounded bg-white/10 hover:bg-white/5"
                        onClick={() => setEditing(true)}
                    >
                        Edit
                    </button>
                </div>
            ) : (
                <form action={formAction} className="flex items-center gap-2">
                    <input
                        name="name"
                        defaultValue={initialName}
                        className="px-2 py-1 rounded bg-white/10 border border-white/20"
                    />
                    <button
                        type="submit"
                        disabled={isPending}
                        className={`text-sm px-2 py-1 rounded bg-green-600/80 transition-colors
                            ${isPending ? "opacity-50 cursor-not-allowed" : "hover:bg-green-600"}`}
                    >
                        Save
                    </button>
                    <button
                        type="button"
                        className="text-sm px-2 py-1 rounded bg-white/10"
                        onClick={() => {
                            setEditing(false)
                            setDismissed(true);
                        }}
                    >
                        Cancel
                    </button>

                    {state?.message && !dismissed && (
                        <div className="flex items-center gap-2">
                            <p className="text-sm text-red-400">{state.message}</p>
                            <button
                                type="button"
                                className="text-xs text-gray-400"
                                onClick={() => setDismissed(true)}
                            >
                                ✕
                            </button>
                        </div>
                    )}
                </form>
            )}
        </div>
    );
}