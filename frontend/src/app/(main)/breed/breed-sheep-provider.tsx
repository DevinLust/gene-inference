"use client";

import React, { createContext, useContext } from "react";
import type { Sheep } from "@/app/lib/definitions";

const BreedSheepContext = createContext<Sheep[] | null>(null);

export function BreedSheepProvider({ sheep, children }: { sheep: Sheep[]; children: React.ReactNode }) {
    return <BreedSheepContext.Provider value={sheep}>{children}</BreedSheepContext.Provider>;
}

export function useBreedSheep() {
    const ctx = useContext(BreedSheepContext);
    if (!ctx) throw new Error("useBreedSheep must be used within BreedSheepProvider");
    return ctx;
}
