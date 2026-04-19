import { Sheep, Category, TRAIT_CATEGORIES, displayAllele } from '@/app/lib/definitions';
import CategoryCard from '@/app/ui/category-card';

const GRADE_ORDER = ["S", "A", "B", "C", "D", "E"] as const;

function getProbability(distribution: Record<string, number>, alleleCode: string) {
    return distribution[alleleCode] ?? 0;
}

function getAlleleColumns(typeMap: Record<string, Record<string, number>>, category: Category): string[] {
    const alleleSet = new Set<string>();

    Object.values(typeMap).forEach((distribution) => {
        Object.keys(distribution).forEach((alleleCode) => alleleSet.add(alleleCode));
    });

    const alleles = Array.from(alleleSet);

    if (TRAIT_CATEGORIES.includes(category)) {
        return GRADE_ORDER.filter((grade) => alleleSet.has(grade));
    }

    return alleles.sort();
}

export default function DistributionTable({ sheep }: { sheep: Sheep }) {
    return (
        <div className="grid grid-cols-1 gap-2">
            {Object.entries(sheep.distributions).map(([category, typeMap]) => {
                const typedCategory = category as Category;
                const rows = Object.entries(typeMap);
                const alleleColumns = getAlleleColumns(typeMap, typedCategory);

                return (
                    <CategoryCard category={typedCategory} key={category}>
                        <div className="overflow-x-auto">
                            <table className="min-w-full table-fixed border-separate border-spacing-0 text-sm">
                                <thead>
                                <tr className="text-center">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-left w-40">
                                        Distribution Type
                                    </th>
                                    <th
                                        className="bg-transparent px-2 py-2 font-semibold text-center"
                                        colSpan={alleleColumns.length}
                                    >
                                        Probability
                                    </th>
                                </tr>
                                <tr className="text-center">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold"></th>
                                    {alleleColumns.map((alleleCode) => (
                                        <th key={alleleCode} className="px-2 py-2 text-center font-semibold">
                                            {displayAllele(typedCategory, alleleCode)}
                                        </th>
                                    ))}
                                </tr>
                                </thead>
                                <tbody>
                                {rows.map(([type, distribution]) => (
                                    <tr
                                        key={type}
                                        className="border-t border-white/10 odd:bg-white/5 hover:bg-white/20 transition-colors"
                                    >
                                        <td className="sticky left-0 bg-transparent px-2 py-2 font-mono w-40">
                                            {type}
                                        </td>
                                        {alleleColumns.map((alleleCode) => {
                                            const prob = getProbability(distribution, alleleCode);
                                            const uniform = 1 / alleleColumns.length;
                                            const isStrong = prob > uniform * 1.5;
                                            return (
                                                <td
                                                    key={alleleCode}
                                                    className={`px-2 py-2 text-center tabular-nums ${
                                                        isStrong ? "font-bold text-white" : "text-white/80"
                                                    }`}
                                                >
                                                    {(prob * 100).toFixed(2)}%
                                                </td>
                                            );
                                        })}
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </CategoryCard>
                );
            })}
        </div>
    );
}
