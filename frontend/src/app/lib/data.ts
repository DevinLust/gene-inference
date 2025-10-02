'use server';

import { Sheep, Prediction } from "./definitions";

// data fetching functions
export async function fetchAllSheep(): Promise<Sheep[]> {
    const res = await fetch("http://localhost:8080/sheep", {
        cache: "force-cache",
    });

    if (!res.ok) {
        console.error("Failed to fetch all sheep");
        throw new Error(`Failed to fetch all sheep, status: ${res.statusText}`);
    }

    return await res.json();
}

export async function fetchSheepById(id: string): Promise<Sheep> {
    const res = await fetch(`http://localhost:8080/sheep/${id}`);

    if (!res.ok) {
        console.error("Failed to fetchSheepById: " + id);
        throw new Error(`Failed to fetch sheep ${id}, status: ${res.status}`);
    }

    return await res.json() as Promise<Sheep>;
}

export async function fetchPrediction(sheep1Id: string, sheep2Id: string): Promise<Prediction> {
    const res = await fetch(`http://localhost:8080/breed/${sheep1Id}/${sheep2Id}/predict`);

    if (!res.ok) {
        console.error("Failed to fetch prediction: " + res.status);
        throw new Error(`Failed to fetch prediction from sheep ${sheep1Id} and ${sheep2Id}, status: ${res.status}`);
    }

    return await res.json() as Prediction;
}
