"use client";

import { useRouter } from "next/navigation";
import { useGraphNav } from "./graph-nav";
import { HiArrowLeft } from "react-icons/hi";

export default function GraphBackButton({ fallbackHref }: { fallbackHref?: string }) {
    const router = useRouter();
    const { back, peek } = useGraphNav();

    const previous = peek();

    // Nothing to go back to → render nothing
    if (!previous && !fallbackHref) return null;

    return (
        <button
            type="button"
            onClick={() => {
                const entry = back();
                router.push(entry?.href ?? fallbackHref!);
            }}
            className="text-blue-400 mb-2 px-1 rounded-lg hover:bg-gray-600"
        >
            {peek()?.label ?? <p><HiArrowLeft className={"inline"} /> Back</p>}
        </button>
    );
}
