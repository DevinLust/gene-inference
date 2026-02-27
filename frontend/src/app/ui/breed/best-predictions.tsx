'use client';

import { useState, useEffect } from 'react';
import { BestPrediction } from '@/app/lib/definitions';
import { BreedState } from '@/app/lib/actions';
import { fetchBestPredictions } from '@/app/lib/data';
import CategoryTag from '@/app/ui/category-tag';
import BestPredictionBody from "./best-prediction-body";
import { Loader2 } from "lucide-react";

export default function BestPredictions() {
    const [bestPredictions, setBestPredictions] = useState<BestPrediction[]>([]);
    const [isFetching, setIsFetching] = useState(false);
    const [expandedIdx, setExpandedIdx] = useState(-1);
    const initialState: BreedState = { message: null };
    const [state, setState] = useState<BreedState>(initialState);

    useEffect(() => {
        if (expandedIdx !== -1) {
            document.getElementById(`accordion-btn-${expandedIdx}`)?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    }, [expandedIdx]);

    async function handleBestPredictions() {
        setState(initialState);
        setBestPredictions([]);
        setExpandedIdx(-1);
        setIsFetching(true);

        const data = await fetchBestPredictions();
        if (Array.isArray(data)) {
            setBestPredictions(data);
        } else if ("message" in data) {
            setState(data);
        } else {
            setState({ message: "an unknown error has occurred" });
        }

        setIsFetching(false);
    }

    function handleExpandedId(id: number) {
        if (id === expandedIdx) {
            setExpandedIdx(-1);
        } else {
            setExpandedIdx(id);
        }
    }

    return (
        <div className="flex-1 max-w-md space-y-4 p-4 rounded-lg bg-gray-600">
            <h2 className="text-xl font-bold">Best Predictions</h2>

            <button
                onClick={handleBestPredictions}
                disabled={isFetching}
                className="w-full rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600"
            >
                <span className="inline-flex items-center justify-center w-full">
                    {isFetching ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                        bestPredictions.length === 0 ? "Generate" : "Regenerate"
                    )}
                </span>
            </button>

            {/* Server response */}
            {state?.message && <p className="text-red-500 font-medium">{state.message}</p>}

            {/* Prediction List */}
            {bestPredictions.length > 0 && (
                <div>
                    <h2 className="text-xl font-bold">Best Categories</h2>
                    <div className="overflow-y-auto max-h-[60vh] rounded-lg border-y border-gray-400">

                        {bestPredictions.map((prediction, index) => (
                            <div
                                key={index}
                                className={`
                                  border bg-gray-800
                                  ${index === 0 ? 'rounded-t-lg' : ''}
                                  ${index === bestPredictions.length - 1 ? 'rounded-b-lg' : ''}
                                  ${index === expandedIdx ? 'border-gray-400' : 'border-gray-600'}
                                `}
                            >
                                {/* Prediction Summary Header */}
                                <div
                                    className={`
                                        flex items-center justify-between px-2 py-2
                                        ${index === expandedIdx ? 'border-b border-dashed border-gray-400' : ''}
                                    `}
                                >
                                    <div key={index} className="flex justify-start gap-1">
                                        <p>{`${index + 1}.`}</p>
                                        {prediction.bestCategoriesSet.map((category, catIdx) => (
                                            <CategoryTag category={category} key={catIdx}/>
                                        ))}
                                    </div>

                                    <button
                                        onClick={() => handleExpandedId(index)}
                                        id={`accordion-btn-${index}`}
                                        className="rounded-full bg-blue-500 px-2 py-1/2 text-white hover:bg-blue-600"
                                    >
                                        {expandedIdx === index ? "Hide" : "Show"}
                                    </button>
                                </div>

                                {/* Prediction Body */}
                                {expandedIdx === index && <BestPredictionBody bestPrediction={bestPredictions[index]} />}
                            </div>
                        ))}

                    </div>
                </div>
            )}
        </div>
    );
}
