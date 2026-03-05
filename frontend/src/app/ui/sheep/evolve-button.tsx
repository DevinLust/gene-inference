"use client";

import { useTransition } from "react";
import { evolveSheep } from "@/app/lib/actions";
import { Category } from "@/app/lib/definitions";

type Props = {
    sheepId: number;
    category: Category;
};

export default function EvolveButton({ sheepId, category }: Props) {
    const [isPending, startTransition] = useTransition();

    const handleClick = () => {
        startTransition(async () => {
            await evolveSheep(sheepId, category);
        });
    };

    return (
        <button
            onClick={handleClick}
            disabled={isPending}
            className="text-xs px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
        >
            {isPending ? "Evolving..." : `Evolve ${category}`}
        </button>
    );
}