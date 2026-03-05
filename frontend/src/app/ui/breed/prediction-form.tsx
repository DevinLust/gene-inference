"use client";

import { useState } from "react";
import { Prediction, SheepSummary } from "@/app/lib/definitions";
import { BreedState } from "@/app/lib/actions";
import { fetchPrediction } from "@/app/lib/data";
import PhenotypeDistributions from "./phentoype-distributions";
import SheepComboBox from "./sheep-combo-box";
import { useBreedSheep } from "@/app/(main)/breed/breed-sheep-provider";

export default function SheepPredictionForm() {
    const sheep: SheepSummary[] = useBreedSheep();
    const [sheep1, setSheep1] = useState<number | null>(null);
    const [sheep2, setSheep2] = useState<number | null>(null);
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

            <SheepComboBox inputLabel={"Parent 1"} sheep={sheep} selectedId={sheep1} onSelect={setSheep1}  />

            <SheepComboBox inputLabel={"Parent 2"} sheep={sheep} selectedId={sheep2} onSelect={setSheep2} />

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
