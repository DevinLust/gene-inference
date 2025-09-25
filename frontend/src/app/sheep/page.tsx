import Link from "next/link";
import { CreateSheep } from "@/app/ui/sheep/buttons";

// app/sheep/page.tsx
export default async function SheepPage() {
    let sheep: any[] | null = null;

    try {
        // Replace with your backend URL
        const res = await fetch("http://localhost:8080/sheep");
        sheep = await res.json();
    } catch (err) {
        console.error("Failed to fetch sheep", err);
    }

    return (
        <div>
            <h1 className="text-2xl font-bold">Sheep List</h1>

            {!sheep && <p className="text-red-500">Backend not running</p>}

            <div className="mt-4 flex items-center justify-between gap-2 md:mt-8">
                {sheep && <CreateSheep />}
            </div>

            <ul className="list-disc pl-4">
                {sheep && sheep.map((s: any) => (
                    <li key={s.id}>{s.name ?? "(unnamed)"}</li>
                ))}
            </ul>
        </div>
    );
}
