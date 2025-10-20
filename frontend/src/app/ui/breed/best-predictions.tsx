'use client';

import { useState } from 'react';
import { BestPrediction } from '@/app/lib/definitions';
import { fetchBestPredictions } from '@/app/lib/data';
import CategoryTag from '@/app/ui/category-tag';
import BestPredictionBody from "./best-prediction-body";

export default function BestPredictions() {
    const [bestPredictions, setBestPredictions] = useState<BestPrediction[]>([]);
    const [expandedIdx, setExpandedIdx] = useState(-1);

    async function handleBestPredictions() {
        try {
            const predictions: BestPrediction[] = await fetchBestPredictions();
            setBestPredictions(predictions);
            setExpandedIdx(-1);
        } catch (err) {
            console.error(err);
        }
    }

    function handleExpandedId(id: number) {
        if (id === expandedIdx) {
            setExpandedIdx(-1);
        } else {
            setExpandedIdx(id);
        }
    }

    return (
        <div className="flex-1 max-w-md space-y-4 p-4 border rounded">
            <h2 className="text-xl font-bold">Best Predictions</h2>

            <button
                onClick={handleBestPredictions}
                className="w-full rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600"
            >
                {bestPredictions.length === 0 ? "Generate" : "Regenerate"}
            </button>

            {/* Prediction List */}
            {bestPredictions.length > 0 && (
                <div>
                    <h2 className="text-xl font-bold">Best Categories</h2>

                    {bestPredictions.map((prediction, index) => (
                        <div key={index}>
                            {/* Prediction Summary Header */}
                            <div className="flex items-center justify-between px-2 py-2 border border-gray-500">
                                <div key={index} className="flex justify-start gap-1">
                                    {prediction.bestCategoriesSet.map((category, catIdx) => (
                                        <CategoryTag category={category} key={catIdx}/>
                                    ))}
                                </div>

                                <button
                                    onClick={() => handleExpandedId(index)}
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
            )}
        </div>
    );
}
