"use client";

import { useActionState } from "react";
import { recalculateBeliefs } from "@/app/lib/actions";

export default function RecalcBeliefsButton() {
    const [state, formAction, isPending] = useActionState(recalculateBeliefs, {});

    return (
        <form action={formAction} className="inline-flex items-center gap-3">
            <button
                type="submit"
                disabled={isPending}
                className="px-4 py-2 rounded-lg bg-purple-600/80 hover:bg-purple-600
                   text-white font-medium
                   disabled:opacity-50 disabled:cursor-not-allowed
                   transition-colors"
            >
                {isPending ? (
                    <span className="flex items-center gap-2">
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
            Recalculating...
          </span>
                ) : (
                    "Recalculate Beliefs"
                )}
            </button>

            {state?.message && (
                <span className="text-sm text-red-400">{state.message}</span>
            )}
        </form>
    );
}
