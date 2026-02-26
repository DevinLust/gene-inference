"use server";

import SheepBreedForm from "./breed-form";
import { fetchAllSheep } from "@/app/lib/data";
import { Sheep } from "@/app/lib/definitions";

export default async function SheepBreedFormServer() {
    const sheep: Sheep[] = await fetchAllSheep();
    return <SheepBreedForm sheep={sheep} />;
}
