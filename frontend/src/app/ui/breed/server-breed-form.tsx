"use server";

import SheepBreedForm from "./breed-form";
import { fetchAllSheep } from "@/app/lib/data";
import { SheepSummary } from "@/app/lib/definitions";

export default async function SheepBreedFormServer() {
    const sheep: SheepSummary[] = await fetchAllSheep({});
    return <SheepBreedForm sheep={sheep} />;
}
