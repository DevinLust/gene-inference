import { fetchAllSheep } from "@/app/lib/data";
import EmptyState from "@/app/ui/empty-state";
import { BreedSheepProvider } from "./breed-sheep-provider";

export default async function BreedLayout({ children }: { children: React.ReactNode }) {
    const sheep = await fetchAllSheep({}); // already auth’d server fetch

    if (sheep.length < 2) {
        return (
            <div className="p-6">
                <EmptyState
                    title="Ready to breed?"
                    description="Breeding needs at least two sheep. Create a couple of sheep to get started."
                    actionLabel="Create sheep"
                    actionHref="/sheep/create"
                />
            </div>
        );
    }

    // If you need to pass sheep to children pages, use a context provider (below),
    // or just let each page receive it by re-fetching (not ideal).
    return <BreedSheepProvider sheep={sheep}>{children}</BreedSheepProvider>;
}
