import { useState } from "react";

const categories = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];
const grades = ["S", "A", "B", "C", "D", "E"];

export default function SheepDistributionsForm() {
    // Track which categories are expanded
    const [expandedCategories, setExpandedCategories] = useState<Record<string, boolean>>({});

    const toggleCategory = (cat: string) => {
        setExpandedCategories((prev) => ({
            ...prev,
            [cat]: !prev[cat],
        }));
    };

    return (
        <div className="space-y-6">
            {categories.map((cat) => (
                <div key={cat} className="border rounded p-4">
                    <button
                        type="button"
                        className="font-medium text-blue-600 hover:underline"
                        onClick={() => toggleCategory(cat)}
                    >
                        {expandedCategories[cat]
                            ? `Hide ${cat} distribution`
                            : `Set ${cat} distribution`}
                    </button>

                    {expandedCategories[cat] && (
                        <div className="grid grid-cols-3 gap-2 mt-2">
                            {grades.map((g) => (
                                <label key={g} className="flex flex-col">
                                    <span className="text-sm">{g}</span>
                                    <input
                                        type="number"
                                        name={`distributions.${cat}.${g}`}
                                        placeholder="Probability"
                                        className="border rounded p-1 w-20"
                                        min={0}
                                        max={1}
                                        step={0.01}
                                    />
                                </label>
                            ))}
                        </div>
                    )}
                </div>
            ))}
        </div>
    );
}
