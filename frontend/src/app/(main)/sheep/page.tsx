import { Category, SheepFilter } from "@/app/lib/definitions";
import { CreateSheep } from "@/app/ui/buttons";
import SheepList from "@/app/ui/sheep/sheep-list";
import RecalculateBeliefsButton from "@/app/ui/sheep/recalculate-beliefs-button";
import { Suspense } from 'react';

const CATEGORIES: Category[] = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];

// app/sheep/page.tsx
export default async function SheepPage(props: {
    searchParams: Promise<{ category?: string, name?: string, grades?: string }>;
}) {
    const searchParams = await props.searchParams;

    const selectedCategory: Category =
        CATEGORIES.includes(searchParams.category as Category)
            ? (searchParams.category as Category)
            : "SWIM";

    const sheepFilter: SheepFilter = searchParams;
    return (
        <div>
            <h1 className="text-2xl font-bold">Sheep List</h1>

            <div className="mt-4 mb-4 flex items-center justify-start gap-2 md:mt-8">
                <CreateSheep />
                <RecalculateBeliefsButton />
            </div>

            <Suspense fallback={<p>Loading List...</p>}>
                <SheepList selectedCategory={selectedCategory as Category} filter={sheepFilter} />
            </Suspense>
        </div>
    );
}
