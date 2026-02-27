import { Grade, Relationship, Category, GradePairKey, parseGradePair } from "@/app/lib/definitions";
import CategoryCard from "@/app/ui/category-card";
import { EpochRecordButton } from "@/app/ui/buttons";

const ALL_GRADES: Grade[] = ["S", "A", "B", "C", "D", "E"];

function getFreq(freqMap: Partial<Record<Grade, number>>, g: Grade) {
    return freqMap[g] ?? 0;
}

export default function PhenotypeFrequencyTable({ relationship }: { relationship: Relationship }) {
    return (
        <div className="grid grid-cols-1 gap-2">
            {Object.entries(relationship.phenotypeFrequencies).map(([cat, epochMap]) => {
                const rows = Object.entries(epochMap); // [pairKey, freqMap][]

                return (
                    <CategoryCard category={cat as Category} key={cat}>
                        <div className="overflow-x-auto">
                            <table className="min-w-full border-separate border-spacing-0 text-sm">
                                <thead>
                                <tr className="text-center">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-left">
                                        Parent phenotypes at birth
                                    </th>
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold" colSpan={ALL_GRADES.length}>
                                        Offspring phenotype frequency
                                    </th>
                                </tr>
                                <tr className="text-left">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold">
                                        (P1, P2)
                                    </th>
                                    {ALL_GRADES.map((g) => (
                                        <th key={g} className="px-2 py-2 text-center font-semibold">
                                            {g}
                                        </th>
                                    ))}
                                    <th className="px-2 py-2 text-center font-semibold">Total</th>
                                    <th className="px-2 py-2 text-center font-semibold">
                                        <span className="sr-only">Epoch Records</span>
                                    </th>
                                </tr>
                                </thead>

                                <tbody>
                                {rows.map(([pairKey, freqMap]) => {
                                    const total = ALL_GRADES.reduce(
                                        (sum, g) => sum + getFreq(freqMap, g),
                                        0
                                    );
                                    const [p1, p2] = parseGradePair(pairKey as GradePairKey);

                                    return (
                                        <tr key={pairKey} className="border-t border-white/10 odd:bg-white/5 hover:bg-white/20 transition-colors">
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
                                            <td className="px-2 py-2 text-center font-semibold tabular-nums">
                                                <EpochRecordButton relationshipId={relationship.id} category={cat} p1={p1} p2={p2} />
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