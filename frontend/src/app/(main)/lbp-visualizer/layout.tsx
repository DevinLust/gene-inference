import { fetchAllSheep } from "@/app/lib/data";
import { BreedSheepProvider } from "@/app/(main)/breed/breed-sheep-provider";

export default async function VisualizerLayout({ children }: { children: React.ReactNode }) {
    const sheep = await fetchAllSheep({}); // already auth’d server fetch

    // If you need to pass sheep to children pages, use a context provider (below),
    // or just let each page receive it by re-fetching (not ideal).
    return <BreedSheepProvider sheep={sheep}>{children}</BreedSheepProvider>;
}
