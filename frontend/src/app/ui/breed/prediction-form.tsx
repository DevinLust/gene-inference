"use client";

import { useState } from "react";
import { Prediction } from "@/app/lib/definitions";
import { fetchPrediction } from "@/app/lib/data";
import PhenotypeDistributions from "./phentoype-distributions";

export default function SheepPredictionForm() {
    const [sheep1, setSheep1] = useState("");
    const [sheep2, setSheep2] = useState("");
    const [prediction, setPrediction] = useState<Prediction | null>(null);

    async function handlePredict() {
        if (!sheep1 || !sheep2) return;

        try {
            const data: Prediction = await fetchPrediction(encodeURIComponent(sheep1), encodeURIComponent(sheep2));
            setPrediction(data);
        } catch (err) {
            console.error(err);
        }
    }

    return (
        <div className="flex-1 max-w-md space-y-4 p-4 border rounded">
            <h2 className="text-xl font-bold">Predict Child Phenotype</h2>

            <input
                type="text"
                placeholder="Sheep 1 ID"
                value={sheep1}
                onChange={(e) => setSheep1(e.target.value)}
                className="w-full rounded border p-2"
            />

            <input
                type="text"
                placeholder="Sheep 2 ID"
                value={sheep2}
                onChange={(e) => setSheep2(e.target.value)}
                className="w-full rounded border p-2"
            />

            <button
                onClick={handlePredict}
                className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600"
            >
                Predict
            </button>

            {/* Prediction display */}
            {prediction && <PhenotypeDistributions phenotypeDistributions={prediction.phenotypeDistributions} />}
        </div>
    );
}
