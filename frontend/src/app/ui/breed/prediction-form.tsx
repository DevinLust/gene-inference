"use client";

import { useState } from "react";
import { Prediction } from "@/app/lib/definitions";
import { BreedState } from "@/app/lib/actions";
import { fetchPrediction } from "@/app/lib/data";
import PhenotypeDistributions from "./phentoype-distributions";

export default function SheepPredictionForm() {
    const [sheep1, setSheep1] = useState("");
    const [sheep2, setSheep2] = useState("");
    const [prediction, setPrediction] = useState<Prediction | null>(null);
    const initialState = { message: null, errors: {} }
    const [state, setState] = useState<BreedState>(initialState);

    async function handlePredict() {
        if (!sheep1 || !sheep2) {
            setState({ message:"Both Sheep IDs are required", errors: {} });
            return;
        }

        // clear errors and prediction
        setState(initialState);
        setPrediction(null);

        const data = await fetchPrediction(encodeURIComponent(sheep1), encodeURIComponent(sheep2));
        if ("phenotypeDistributions" in data) {
            setPrediction(data);
        } else if ("message" in data) {
            setState(data);
        } else {
            setState({message: "an unknown error has occurred", errors: {} });
        }

    }

    return (
        <div className="flex-1 max-w-md space-y-4 p-4 rounded-lg bg-gray-600">
            <h2 className="text-xl font-bold">Predict Child Phenotype</h2>

            <div>
                <input
                    type="text"
                    placeholder="Sheep 1 ID"
                    value={sheep1}
                    onChange={(e) => setSheep1(e.target.value)}
                    className="w-full rounded border border-gray-500 p-2 bg-gray-800"
                />
                <div id="sheep2Id-error" aria-live="polite" aria-atomic="true">
                    {state.errors?.sheep1Id && <p className="mt-2 text-sm text-red-500">{state.errors.sheep1Id}</p>}
                </div>
            </div>

            <div>
                <input
                    type="text"
                    placeholder="Sheep 2 ID"
                    value={sheep2}
                    onChange={(e) => setSheep2(e.target.value)}
                    className="w-full rounded border border-gray-500 p-2 bg-gray-800"
                />
                <div id="sheep2Id-error" aria-live="polite" aria-atomic="true">
                    {state.errors?.sheep2Id && <p className="mt-2 text-sm text-red-500">{state.errors.sheep2Id}</p>}
                </div>
            </div>

            <button
                onClick={handlePredict}
                className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600"
            >
                Predict
            </button>

            {/* Server response */}
            {state?.message && <p className="text-red-500 font-medium">{state.message}</p>}

            {/* Prediction display */}
            {prediction && <PhenotypeDistributions phenotypeDistributions={prediction.phenotypeDistributions} />}
        </div>
    );
}
