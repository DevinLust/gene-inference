'use server';

import { Sheep, Prediction, BestPrediction, Relationship } from "./definitions";
import { BreedState } from "./actions";
import { notFound } from "next/navigation";

if (!process.env.NEXT_PUBLIC_API_BASE_URL) {
  throw new Error("NEXT_PUBLIC_API_BASE_URL is not defined");
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

// data fetching functions
export async function fetchAllSheep(): Promise<Sheep[]> {
    const res = await fetch(`${API_BASE_URL}/sheep`, {
        cache: "force-cache",
    });

    await checkStatus(res);

    return await res.json();
}

export async function fetchSheepById(id: string): Promise<Sheep> {
    const res = await fetch(`${API_BASE_URL}/sheep/${id}`);

    await checkStatus(res);

    return await res.json() as Promise<Sheep>;
}

// both prediction fetches need to return objects for inline error handling
export async function fetchPrediction(sheep1Id: string, sheep2Id: string): Promise<Prediction | BreedState> {
    const res = await fetch(`${API_BASE_URL}/breed/${sheep1Id}/${sheep2Id}/predict`);

    if (!res.ok) {
        return await res.json();
    }

    return await res.json() as Prediction;
}

// both prediction fetches need to return objects for inline error handling
export async function fetchBestPredictions(): Promise<BestPrediction[] | BreedState> {
    const res = await fetch(`${API_BASE_URL}/breed/best-predictions`);

    if (!res.ok) {
        return await res.json();
    }

    return await res.json() as BestPrediction[];
}

export async function fetchAllRelationships(): Promise<Relationship[]> {
    const res = await fetch(`${API_BASE_URL}/relationship`);

    await checkStatus(res);

    return await res.json() as Relationship[];
}

export async function fetchRelationshipById(id: string): Promise<Relationship> {
    const res = await fetch(`${API_BASE_URL}/relationship/${id}`);

    await checkStatus(res);

    return await res.json() as Promise<Relationship>;
}

async function checkStatus(res: Response) {
    if (res.status === 404) {
        notFound(); // renders not-found.tsx
    }
    if (!res.ok) {
        // Attempt to parse the error message from JSON
        let errorMessage;
        try {
            const data = await res.json(); // or res.text() if backend sends plain text
            errorMessage = data.message || JSON.stringify(data);
        } catch {
            // Fallback if response is not JSON
            errorMessage = await res.text();
        }
        throw new Error(`Failed to fetch data: ${res.status} - ${errorMessage}`);
    }
}
