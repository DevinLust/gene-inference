"use client";

import Link from "next/link";
import { detectNode } from "@/app/lib/helpers";
import { usePathname, useSearchParams } from "next/navigation";
import { useGraphNav, GraphEntry } from "./graph-nav";

export function GraphLink({
                              destination,
                              children,
                              className,
                          }: {
    destination: GraphEntry;
    children: React.ReactNode;
    className?: string;
}) {
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const { navigateFrom } = useGraphNav();

    const current = detectNode(pathname);

    const fullCurrentHref =
        pathname + (searchParams.toString() ? `?${searchParams.toString()}` : "");

    if (current) {
        current.href = fullCurrentHref;
    }

    return (
        <Link
            href={destination.href}
            className={className}
            onClick={() => {
                if (current) {
                    navigateFrom(current, destination);
                }
            }}
        >
            {children}
        </Link>
    );
}
