import Link from "next/link";
import { CreateSheep } from "@/app/ui/sheep/buttons";
import SheepList from "@/app/ui/sheep/sheep-list";
import { Suspense } from 'react';

// app/sheep/page.tsx
export default function SheepPage() {
    return (
        <div>
            <h1 className="text-2xl font-bold">Sheep List</h1>

            <div className="mt-4 mb-4 flex items-center justify-between gap-2 md:mt-8">
                <CreateSheep />
            </div>

            <Suspense fallback={<p>Loading List...</p>}>
                <SheepList />
            </Suspense>
        </div>
    );
}
