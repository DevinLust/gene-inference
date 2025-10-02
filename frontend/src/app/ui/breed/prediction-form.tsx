"use client";

import { useState } from "react";
import { Prediction } from "@/app/lib/definitions";
import { fetchPrediction } from "@/app/lib/data";

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
        <div className="max-w-md space-y-4 p-4 border rounded">
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
            {prediction && (
                <div className="mt-6 mb-4">
                    <h2 className="mb-1 text-xl">Distributions:</h2>
                    {Object.entries(prediction.phenotypeDistributions).map(([cat, distribution]) => (
                        <div key={`${cat}`} className="mt-4 border border-white p-4">
                            <h2 className="mt-1 mb-1">{cat}:</h2>
                            <div key={`${cat}.INFERRED`} className="grid grid-cols-6 gap-2 mb-2">
                                {Object.entries(distribution).map(([grade, prob]) => (
                                    <p key={`${cat}.INFERRED.${grade}`}>{grade}: {(prob * 100).toFixed(2)}%</p>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
