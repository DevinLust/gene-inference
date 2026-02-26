"use server";

import SheepPredictionForm from "./prediction-form";
import { fetchAllSheep } from "@/app/lib/data";
import { Sheep } from "@/app/lib/definitions";

export default async function SheepPredictionFormServer() {
    const sheep: Sheep[] = await fetchAllSheep();
    return <SheepPredictionForm sheep={sheep} />;
}
