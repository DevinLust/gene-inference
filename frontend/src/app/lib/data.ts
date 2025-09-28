import { Sheep } from "./definitions";

// data fetching functions
export async function fetchSheepById(id: string): Promise<Sheep> {
    const res = await fetch(`http://localhost:8080/sheep/${id}`);

    if (!res.ok) {
        console.error("Failed to fetchSheepById: " + id);
        throw new Error(`Failed to fetch sheep ${id}, status: ${res.status}`);
    }

    return await res.json() as Promise<Sheep>;
}
