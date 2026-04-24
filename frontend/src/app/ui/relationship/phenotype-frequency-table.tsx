import {
    Relationship,
    Category,
    AlleleCodePairKey,
    parseAlleleCodePair,
    CATEGORY_ALLELE_OPTIONS,
    displayAllele,
} from "@/app/lib/definitions";
import CategoryCard from "@/app/ui/category-card";
import { EpochRecordButton } from "@/app/ui/buttons";

function getFreq(freqMap: Partial<Record<string, number>>, alleleCode: string) {
    return freqMap[alleleCode] ?? 0;
}

export default function PhenotypeFrequencyTable({ relationship }: { relationship: Relationship }) {
    return (
        <div className="grid grid-cols-1 gap-2">
            {Object.entries(relationship.phenotypeFrequencies).map(([cat, epochMap]) => {
                const typedCategory = cat as Category;
                const rows = Object.entries(epochMap);
                const alleleColumns = CATEGORY_ALLELE_OPTIONS[typedCategory];

                return (
                    <CategoryCard category={typedCategory} key={typedCategory}>
                        <div className="overflow-x-auto">
                            <table className="min-w-full table-fixed border-separate border-spacing-0 text-sm">
                                <colgroup>
                                    <col className="w-48" />
                                    {alleleColumns.map((opt) => (
                                        <col key={opt.value} />
                                    ))}
                                    <col className="w-20" />
                                    <col className="w-16" />
                                </colgroup>

                                <thead>
                                <tr className="text-center">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-left">
                                        Parent phenotypes at birth
                                    </th>
                                    <th
                                        className="bg-transparent px-2 py-2 font-semibold"
                                        colSpan={alleleColumns.length}
                                    >
                                        Offspring phenotype frequency
                                    </th>
                                    <th className="bg-transparent px-2 py-2 font-semibold" colSpan={2}>
                                        Summary
                                    </th>
                                </tr>
                                <tr className="text-left">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold">
                                        (P1, P2)
                                    </th>

                                    {alleleColumns.map((opt) => (
                                        <th
                                            key={opt.value}
                                            className="px-2 py-2 text-center font-semibold whitespace-nowrap"
                                        >
                                            {opt.label}
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
                                    const total = alleleColumns.reduce(
                                        (sum, opt) => sum + getFreq(freqMap, opt.value),
                                        0
                                    );

                                    const [p1, p2] = parseAlleleCodePair(pairKey as AlleleCodePairKey);

                                    return (
                                        <tr
                                            key={pairKey}
                                            className="border-t border-white/10 odd:bg-white/5 hover:bg-white/20 transition-colors"
                                        >
                                            <td className="sticky left-0 bg-transparent px-2 py-2 font-mono whitespace-nowrap">
                                                {pairKey}
                                            </td>

                                            {alleleColumns.map((opt) => (
                                                <td
                                                    key={opt.value}
                                                    className="px-2 py-2 text-center tabular-nums"
                                                >
                                                    {getFreq(freqMap, opt.value)}
                                                </td>
                                            ))}

                                            <td className="px-2 py-2 text-center font-semibold tabular-nums">
                                                {total}
                                            </td>

                                            <td className="px-2 py-2 text-center font-semibold tabular-nums">
                                                <EpochRecordButton
                                                    relationshipId={relationship.id}
                                                    category={typedCategory}
                                                    p1={p1}
                                                    p2={p2}
                                                />
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
