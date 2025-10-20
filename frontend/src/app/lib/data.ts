'use server';

import { Sheep, Prediction, BestPrediction } from "./definitions";

// data fetching functions
export async function fetchAllSheep(): Promise<Sheep[]> {
    const res = await fetch("http://localhost:8080/sheep", {
        cache: "force-cache",
    });

    await checkStatus(res);

    return await res.json();
}

export async function fetchSheepById(id: string): Promise<Sheep> {
    const res = await fetch(`http://localhost:8080/sheep/${id}`);

    await checkStatus(res);

    return await res.json() as Promise<Sheep>;
}

export async function fetchPrediction(sheep1Id: string, sheep2Id: string): Promise<Prediction> {
    const res = await fetch(`http://localhost:8080/breed/${sheep1Id}/${sheep2Id}/predict`);

    await checkStatus(res);

    return await res.json() as Prediction;
}

export async function fetchBestPredictions(): Promise<BestPrediction[]> {
    const res = await fetch("http://localhost:8080/breed/best-predictions");

    await checkStatus(res);

    return await res.json() as BestPrediction[];
}

async function checkStatus(res: Response) {
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
        throw new Error(`Failed to fetch best predictions: ${res.status} - ${errorMessage}`);
    }
}
