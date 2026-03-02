import { Sheep, Grade, Category } from '@/app/lib/definitions';
import CategoryCard from '@/app/ui/category-card';

const ALL_GRADES: Grade[] = ["S", "A", "B", "C", "D", "E"];

function getProbability(distribution: Record<Grade, number>, grade: Grade) {
    return distribution[grade];
}

export default function DistributionTable({ sheep }: { sheep: Sheep }) {
    return (
        <div className="grid grid-cols-1 gap-2">
            {Object.entries(sheep.distributions).map(([category, typeMap]) => {
                const rows = Object.entries(typeMap);

                return (
                    <CategoryCard category={category as Category} key={category}>
                        <div className="overflow-x-auto">
                            <table className="min-w-full border-separate border-spacing-0 text-sm">
                                <thead>
                                <tr className="text-center">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-left">
                                        Distribution Type
                                    </th>
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-center" colSpan={ALL_GRADES.length}>
                                        Probability
                                    </th>
                                </tr>
                                <tr className="text-center">
                                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold"></th>
                                    {ALL_GRADES.map((g) => (
                                        <th key={g} className="px-2 py-2 text-center font-semibold">
                                            {g}
                                        </th>
                                    ))}
                                </tr>
                                </thead>
                                <tbody>
                                {rows.map(([type, distribution]) => (
                                    <tr key={type} className="border-t border-white/10 odd:bg-white/5 hover:bg-white/20 transition-colors">
                                        <td className="sticky left-0 bg-transparent px-2 py-2 font-mono">
                                            {type}
                                        </td>
                                        {ALL_GRADES.map((g) => {
                                            const prob = getProbability(distribution, g);
                                            return (
                                                <td
                                                    key={g}
                                                    className={`px-2 py-2 text-center tabular-nums ${
                                                        prob >= 0.2 ? "font-bold text-white" : "text-white/80"
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