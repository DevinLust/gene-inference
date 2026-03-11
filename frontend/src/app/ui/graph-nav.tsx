"use client";

import React, { createContext, useContext, useEffect, useMemo, useState } from "react";

export type GraphEntry = {
    type: "sheep" | "relationship";
    id: string;
    href: string;
    label?: string;
};

type GraphNavContextValue = {
    stack: GraphEntry[];
    navigateFrom: (current: GraphEntry, destination: GraphEntry) => void;
    back: () => GraphEntry | null;
    peek: () => GraphEntry | null;
    clear: () => void;
};

const GraphNavContext = createContext<GraphNavContextValue | null>(null);

const STORAGE_KEY = "graph-nav-stack";

function sameNode(a?: GraphEntry | null, b?: GraphEntry | null) {
    return !!a && !!b && a.type === b.type && a.id === b.id;
}

export function GraphNavProvider({ children }: { children: React.ReactNode }) {
    const [stack, setStack] = useState<GraphEntry[]>([]);

    useEffect(() => {
        const raw = sessionStorage.getItem(STORAGE_KEY);
        if (!raw) return;
        try {
            setStack(JSON.parse(raw));
        } catch {
            sessionStorage.removeItem(STORAGE_KEY);
        }
    }, []);

    useEffect(() => {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(stack));
    }, [stack]);

    const value = useMemo<GraphNavContextValue>(() => {
        return {
            stack,

            navigateFrom: (current, destination) => {
                setStack((prev) => {
                    const top = prev[prev.length - 1];

                    // Case 1: user is effectively going "back" to the top entry
                    if (sameNode(top, destination)) {
                        return prev.slice(0, -1);
                    }

                    // Case 2: destination already exists deeper in the stack
                    // Trim back to that point instead of creating loops
                    const existingIndex = prev.findIndex((entry) => sameNode(entry, destination));
                    if (existingIndex !== -1) {
                        return prev.slice(0, existingIndex);
                    }

                    // Case 3: normal forward navigation
                    // Avoid immediate duplicate current pushes
                    if (sameNode(top, current)) {
                        return prev;
                    }

                    return [...prev, current];
                });
            },

            back: () => {
                const last = stack[stack.length - 1] ?? null;
                if (!last) return null;

                setStack((prev) => prev.slice(0, -1));
                return last;
            },

            peek: () => stack[stack.length - 1] ?? null,

            clear: () => setStack([]),
        };
    }, [stack]);

    return <GraphNavContext.Provider value={value}>{children}</GraphNavContext.Provider>;
}

export function useGraphNav() {
    const ctx = useContext(GraphNavContext);
    if (!ctx) {
        throw new Error("useGraphNav must be used inside GraphNavProvider");
    }
    return ctx;
}
