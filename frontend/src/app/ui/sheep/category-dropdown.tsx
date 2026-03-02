"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import type { Category } from "@/app/lib/definitions";

const CATEGORIES: Category[] = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];

export default function CategoryDropdown() {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();

    const current = (searchParams.get("category") as Category) ?? "SWIM";

    function onChange(next: string) {
        const params = new URLSearchParams(searchParams.toString());
        params.set("category", next);
        router.push(`${pathname}?${params.toString()}`);
    }

    return (
        <label className="flex items-center gap-2 m-1">
            <span className="text-sm text-gray-300">Show distributions for</span>
            <select
                value={current}
                onChange={(e) => onChange(e.target.value)}
                className="rounded bg-gray-800 border border-gray-600 px-2 py-1"
            >
                {CATEGORIES.map((c) => (
                    <option key={c} value={c}>
                        {c}
                    </option>
                ))}
            </select>
        </label>
    );
}
