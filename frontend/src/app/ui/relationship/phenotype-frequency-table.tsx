import { Grade, Relationship, Category } from "@/app/lib/definitions";
import CategoryCard from "@/app/ui/category-card";

const ALL_GRADES: Grade[] = ["S", "A", "B", "C", "D", "E"];

function getFreq(freqMap: Partial<Record<Grade, number>>, g: Grade) {
    return freqMap[g] ?? 0;
}

export default function PhenotypeFrequencyTable({ relationship }: { relationship: Relationship }) {
    return (
        <div className="grid grid-cols-1 gap-2">
            <div className="opacity-90">
                Each row label like <span className="font-mono">(B, C)</span> means
                Parent1 = B and Parent2 = C at the time the child was born.
            </div>
            {Object.entries(relationship.phenotypeFrequencies).map(([cat, epochMap]) => {
                const rows = Object.entries(epochMap); // [pairKey, freqMap][]

                return (
                    <CategoryCard category={cat as Category} key={cat}>
                        {/* Legend / title block */}
                        <div className="mb-3 rounded-lg bg-white/10 p-3 text-sm">
                            <div className="font-semibold">Parent phenotypes at birth</div>
                        </div>

                        <div className="overflow-x-auto">
                            <table className="min-w-full border-separate border-spacing-0 text-sm">
                                <thead>
                                <tr className="text-left">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold">
                                        Parents at birth (P1, P2)
                                    </th>
                                    {ALL_GRADES.map((g) => (
                                        <th key={g} className="px-2 py-2 text-center font-semibold">
                                            {g}
                                        </th>
                                    ))}
                                    <th className="px-2 py-2 text-center font-semibold">Total</th>
                                </tr>
                                </thead>

                                <tbody>
                                {rows.map(([pairKey, freqMap]) => {
                                    const total = ALL_GRADES.reduce(
                                        (sum, g) => sum + getFreq(freqMap, g),
                                        0
                                    );

                                    return (
                                        <tr key={pairKey} className="border-t border-white/10">
                                            <td className="sticky left-0 bg-transparent px-2 py-2 font-mono">
                                                {pairKey}
                                            </td>

                                            {ALL_GRADES.map((g) => (
                                                <td key={g} className="px-2 py-2 text-center tabular-nums">
                                                    {getFreq(freqMap, g)}
                                                </td>
                                            ))}

                                            <td className="px-2 py-2 text-center font-semibold tabular-nums">
                                                {total}
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        </div>
                    </CategoryCard>
                );
            })}
        </div>
    );
}