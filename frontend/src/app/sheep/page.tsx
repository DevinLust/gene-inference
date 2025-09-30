import Link from "next/link";
import { CreateSheep, SheepDetails } from "@/app/ui/sheep/buttons";

// app/sheep/page.tsx
export default async function SheepPage() {
    let sheep: any[] | null = null;

    try {
        // fetch a more light-weight sheep list
        const res = await fetch("http://localhost:8080/sheep");
        sheep = await res.json();
    } catch (err) {
        console.error("Failed to fetch sheep", err);
    }

    return (
        <div>
            <h1 className="text-2xl font-bold">Sheep List</h1>

            {!sheep && <p className="text-red-500">Backend not running</p>}

            <div className="mt-4 mb-4 flex items-center justify-between gap-2 md:mt-8">
                {sheep && <CreateSheep />}
            </div>

            <ul className="list-disc pl-4 list-inside">
                {sheep && sheep.map((s: any) => (
                    <li key={s.id} className="flex list-item items-center justify-between">
                        <span>{s.name || <span className="text-gray-400">(unnamed)</span>}</span>
                        <SheepDetails sheepId={s.id} />
                    </li>
                ))}
            </ul>
        </div>
    );
}
